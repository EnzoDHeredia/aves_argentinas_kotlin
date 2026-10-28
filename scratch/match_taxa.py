import json
import re
import sys
from pathlib import Path

sys.stdout.reconfigure(encoding='utf-8', errors='replace')

# 1. Load species.json
with open('app/src/main/assets/birdnet/species.json', encoding='utf-8') as f:
    model_species = json.load(f)

model_aves = [s for s in model_species if s.get('class') == 'Aves']
model_by_canonical = {s['canonical_scientific_name'].strip(): s for s in model_aves}
model_by_sci = {s['scientific_name'].strip(): s for s in model_aves}
model_by_canonical_lower = {s['canonical_scientific_name'].strip().lower(): s for s in model_aves}
model_by_sci_lower = {s['scientific_name'].strip().lower(): s for s in model_aves}

# 2. Parse all 1043 species from categorizacion.txt
with open('app/build/t4/categorizacion.txt', encoding='utf-8', errors='ignore') as f:
    cat_text = f.read()

start_idx = cat_text.find('PAGE 75')
end_idx = cat_text.find('PAGE 144')
apendice1 = cat_text[start_idx:end_idx]

# Pattern for entries
lines = apendice1.split('\n')

arg_list = {}
cat_re = re.compile(r'\s+(VU|AM|NA|EC|EX|EP|NC|EN|IC|DD|n/e)\b')

i = 0
while i < len(lines):
    line_s = lines[i].strip()
    i += 1
    if not line_s or line_s.startswith('PAGE ') or line_s.startswith('Apéndice') or line_s.startswith('ID NOMBRE'):
        continue
    m = re.match(r'^(\d+)\s+(.+)$', line_s)
    if not m:
        continue
    id_num = int(m.group(1))
    if id_num < 1 or id_num > 1033:
        continue
    rest = m.group(2).strip()
    
    # Check if next line continues a hyphenated name
    if rest.endswith('-') and i < len(lines):
        rest = rest[:-1] + lines[i].strip()
        i += 1
        
    cm = cat_re.search(rest)
    if cm:
        before_cat = rest[:cm.start()].strip()
        cat = cm.group(1)
        sm = re.search(r'([A-Z][a-z]+(?:\s+[a-z]+(?:\s+[a-z]+)?))\*?\s*$', before_cat)
        if sm:
            sci = sm.group(1).strip()
            vulgar = before_cat[:sm.start()].strip()
            arg_list[id_num] = {
                'id': id_num,
                'vulgar': vulgar,
                'scientific_name': sci,
                'category': cat,
                'origin': 'native'
            }
    elif id_num == 154:
        # 154 Pelícano pardo Pelecanus thagus n/e
        arg_list[154] = {
            'id': 154,
            'vulgar': 'Pelícano pardo',
            'scientific_name': 'Pelecanus thagus',
            'category': 'n/e',
            'origin': 'native'
        }

# Apéndice 3
start_ap3 = cat_text.find('PAGE 144')
end_ap3 = cat_text.find('PAGE 145')
ap3_text = cat_text[start_ap3:end_ap3]

for line in ap3_text.split('\n'):
    m = re.match(r'^(\d+)\s+([A-ZÁÉÍÓÚÑa-záéíóúñ\s]+?)\s+([A-Z][a-z]+\s+[a-z]+)\s+IN\b', line.strip())
    if m:
        id_num = int(m.group(1))
        arg_list[id_num] = {
            'id': id_num,
            'vulgar': m.group(2).strip(),
            'scientific_name': m.group(3).strip(),
            'category': 'IN',
            'origin': 'introduced'
        }

print(f"Total parsed species in official Argentine reference: {len(arg_list)} / 1043")

# Check which ones match directly vs need synonym mapping vs absent
exact_matches = {}
unmatched = {}

for id_num, entry in sorted(arg_list.items()):
    sci = entry['scientific_name']
    sci_lower = sci.lower()
    # Check exact canonical or scientific
    match = model_by_canonical_lower.get(sci_lower) or model_by_sci_lower.get(sci_lower)
    if match:
        exact_matches[id_num] = (entry, match)
    else:
        unmatched[id_num] = entry

print(f"Direct exact matches with model: {len(exact_matches)} ({len(exact_matches)/len(arg_list)*100:.1f}%)")
print(f"Unmatched (synonyms or absent from model): {len(unmatched)}")
