import json
import re
import sys

sys.stdout.reconfigure(encoding='utf-8', errors='replace')

with open('app/src/main/assets/birdnet/species.json', encoding='utf-8') as f:
    model_species = json.load(f)

model_aves = [s for s in model_species if s.get('class') == 'Aves']
model_canon_map = {s['canonical_scientific_name'].strip().lower(): s for s in model_aves}
model_sci_map = {s['scientific_name'].strip().lower(): s for s in model_aves}

# Load arg_ref from categorizacion.txt
with open('app/build/t4/categorizacion.txt', encoding='utf-8', errors='ignore') as f:
    cat_text = f.read()

start_idx = cat_text.find('PAGE 75')
end_idx = cat_text.find('PAGE 144')
lines = cat_text[start_idx:end_idx].split('\n')

raw_entries = {}
for i, line in enumerate(lines):
    line_s = line.strip()
    m = re.match(r'^(\d+)\s+(.+)$', line_s)
    if m:
        num = int(m.group(1))
        if 1 <= num <= 1033 and num not in raw_entries:
            raw_entries[num] = (i, m.group(2).strip())

arg_ref = {}
special_cases = {
    154: ('Pelícano pardo', 'Pelecanus thagus', 'n/e'),
    598: ('Picapalo colorado', 'Campylorhamphus trochilirostris', 'NA'),
    668: ('Chotoy', 'Schoeniophylax phryganophilus', 'NA'),
    725: ('Mosqueta ojo dorado', 'Hemitriccus margaritaceiventer', 'NA'),
    795: ('Tuquito gris', 'Empidonomus aurantioatrocristatus', 'NA'),
}

for num in range(1, 1034):
    if num in special_cases:
        v, s, c = special_cases[num]
        arg_ref[num] = {'id': num, 'vulgar': v, 'scientific_name': s, 'category': c, 'origin': 'native'}
        continue
    line_idx, text_line = raw_entries[num]
    m = re.search(r'([A-Z][a-z]{2,}(?:\s+[a-z]{3,}(?:\s+[a-z]{3,})?)?)\*?\s+(VU|AM|NA|EC|EX|EP|NC|EN|IC|DD|n/e)\b', text_line)
    if not m:
        m = re.search(r'([A-Z][a-z]{2,}\s+[a-z]{3,})\*?\s+(VU|AM|NA|EC|EX|EP|NC|EN|IC|DD|n/e)\b', text_line)
    if m:
        sci = m.group(1).strip()
        cat = m.group(2)
        vulgar = text_line[:m.start()].strip()
        vulgar = re.sub(r'\s*\(\s*E\s*\)\s*$', '', vulgar).strip()
        arg_ref[num] = {'id': num, 'vulgar': vulgar, 'scientific_name': sci, 'category': cat, 'origin': 'native'}

start_ap3 = cat_text.find('PAGE 144')
end_ap3 = cat_text.find('PAGE 145')
for line in cat_text[start_ap3:end_ap3].split('\n'):
    m = re.match(r'^(\d+)\s+([A-ZÁÉÍÓÚÑa-záéíóúñ\s]+?)\s+([A-Z][a-z]+\s+[a-z]+)\s+IN\b', line.strip())
    if m:
        id_num = int(m.group(1))
        arg_ref[id_num] = {'id': id_num, 'vulgar': m.group(2).strip(), 'scientific_name': m.group(3).strip(), 'category': 'IN', 'origin': 'introduced'}

# Let's check all unmatched entries
unmatched = []
exact_matches = []

for id_num, sp in sorted(arg_ref.items()):
    sci_lower = sp['scientific_name'].strip().lower()
    m = model_canon_map.get(sci_lower) or model_sci_map.get(sci_lower)
    if m:
        exact_matches.append((sp, m))
    else:
        unmatched.append(sp)

# Let's inspect all 161 unmatched and build a map of verified equivalences
with open('scratch/all_unmatched.txt', 'w', encoding='utf-8') as f:
    for u in unmatched:
        f.write(f"{u['id']}\t{u['vulgar']}\t{u['scientific_name']}\n")

print(f"Written {len(unmatched)} unmatched to scratch/all_unmatched.txt")
