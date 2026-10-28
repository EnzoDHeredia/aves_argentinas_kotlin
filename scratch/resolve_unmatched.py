import json
import csv
import re
import sys
import difflib

sys.stdout.reconfigure(encoding='utf-8', errors='replace')

# 1. Load model species
with open('app/src/main/assets/birdnet/species.json', encoding='utf-8') as f:
    model_species = json.load(f)

model_aves = [s for s in model_species if s.get('class') == 'Aves']
canonical_to_model = {s['canonical_scientific_name'].strip().lower(): s for s in model_aves}
sci_to_model = {s['scientific_name'].strip().lower(): s for s in model_aves}
all_model_names = {s['canonical_scientific_name'].strip() for s in model_aves}

# Index by epithet
model_by_species_epithet = {}
for s in model_aves:
    canon = s['canonical_scientific_name'].strip()
    parts = canon.split()
    if len(parts) >= 2:
        ep = parts[1].lower()
        model_by_species_epithet.setdefault(ep, []).append(s)

# 2. Parse Argentine reference list from categorizacion.txt
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

# Apéndice 3
start_ap3 = cat_text.find('PAGE 144')
end_ap3 = cat_text.find('PAGE 145')
for line in cat_text[start_ap3:end_ap3].split('\n'):
    m = re.match(r'^(\d+)\s+([A-ZÁÉÍÓÚÑa-záéíóúñ\s]+?)\s+([A-Z][a-z]+\s+[a-z]+)\s+IN\b', line.strip())
    if m:
        id_num = int(m.group(1))
        arg_ref[id_num] = {'id': id_num, 'vulgar': m.group(2).strip(), 'scientific_name': m.group(3).strip(), 'category': 'IN', 'origin': 'introduced'}

direct_matches = []
unmatched = []

for id_num, sp in sorted(arg_ref.items()):
    sci = sp['scientific_name'].strip()
    sci_lower = sci.lower()
    m = canonical_to_model.get(sci_lower) or sci_to_model.get(sci_lower)
    if m:
        direct_matches.append((sp, m))
    else:
        unmatched.append(sp)

synonyms_found = []
absent_candidates = []

for u in unmatched:
    sci = u['scientific_name'].strip()
    vulgar = u['vulgar']
    parts = sci.split()
    genus = parts[0]
    epithet = parts[1].lower() if len(parts) > 1 else ""
    
    candidates = model_by_species_epithet.get(epithet, [])
    close_names = difflib.get_close_matches(sci, list(all_model_names), n=1, cutoff=0.88)
    
    resolved = None
    if close_names:
        resolved = ('orthographic_or_close', close_names[0])
    elif candidates:
        # Check if any candidate has matching family / sound or is obvious genus transfer
        resolved = ('genus_change', [c['canonical_scientific_name'] for c in candidates])
    
    if resolved:
        synonyms_found.append((u, resolved))
    else:
        absent_candidates.append(u)

print(f"Direct matches: {len(direct_matches)}")
print(f"Unmatched: {len(unmatched)}")
print(f"Potential synonyms / orthographic variants: {len(synonyms_found)}")
print(f"Likely absent from model: {len(absent_candidates)}")

print("\n--- SAMPLE RESOLVED ---")
for u, r in synonyms_found[:25]:
    print(f"ID {u['id']:4d}: {u['vulgar']:25} | {u['scientific_name']:28} -> {r}")

print("\n--- SAMPLE ABSENT CANDIDATES ---")
for u in absent_candidates[:25]:
    print(f"ID {u['id']:4d}: {u['vulgar']:25} | {u['scientific_name']}")
