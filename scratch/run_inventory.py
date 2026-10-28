import json
import re
import sys
from pathlib import Path

sys.stdout.reconfigure(encoding='utf-8', errors='replace')

# 1. Load species.json
with open('app/src/main/assets/birdnet/species.json', encoding='utf-8') as f:
    model_species = json.load(f)

# Total model classes: 9789
# Filter class Aves
model_aves = [s for s in model_species if s.get('class') == 'Aves']
print(f"Total model classes: {len(model_species)}, Aves: {len(model_aves)}")

# Map by canonical_scientific_name and scientific_name
model_by_canonical = {s['canonical_scientific_name'].strip(): s for s in model_aves}
model_by_sci = {s['scientific_name'].strip(): s for s in model_aves}
# Also case-insensitive
model_by_canonical_lower = {s['canonical_scientific_name'].strip().lower(): s for s in model_aves}

# 2. Parse Argentine reference list from categorizacion.txt
with open('app/build/t4/categorizacion.txt', encoding='utf-8', errors='ignore') as f:
    cat_text = f.read()

start_idx = cat_text.find('PAGE 75')
end_idx = cat_text.find('PAGE 144')
apendice1 = cat_text[start_idx:end_idx]

# Pattern for Apéndice 1
# ID vulgar sci CAT
lines = apendice1.split('\n')

arg_list = {}
# Known categories
cat_re = re.compile(r'\s+(VU|AM|NA|EC|EX|EP|NC|EN|IC|DD)\b')

for line in lines:
    line_s = line.strip()
    if not line_s or line_s.startswith('PAGE ') or line_s.startswith('Apéndice') or line_s.startswith('ID NOMBRE'):
        continue
    # check line starting with digit
    m = re.match(r'^(\d+)\s+(.+)$', line_s)
    if not m:
        continue
    id_num = int(m.group(1))
    if id_num < 1 or id_num > 1033:
        continue
    rest = m.group(2).strip()
    # Check if this line has a category
    # Sometimes it wraps, but in Apéndice 1 the first line of an entry has the vulgar name, sci name and category
    cm = cat_re.search(rest)
    if cm:
        before_cat = rest[:cm.start()].strip()
        cat = cm.group(1)
        # before_cat has vulgar and scientific name
        # scientific name is typically genus + species (2 words, capitalized genus, lowercase species)
        # e.g. "Ñandú Rhea americana" or "Choique Rhea pennata*" or "Cauquén colorado (continental) Chloephaga rubidiceps*"
        # Let's extract scientific name
        sm = re.search(r'([A-Z][a-z]+(?:\s+[a-z]+(?:\s+[a-z]+)?))\*?\s*$', before_cat)
        if sm:
            sci = sm.group(1).strip()
            vulgar = before_cat[:sm.start()].strip()
            # Clean up trailing punctuation or notes in vulgar
            arg_list[id_num] = {
                'id': id_num,
                'vulgar': vulgar,
                'scientific_name': sci,
                'category': cat,
                'origin': 'native'
            }
        else:
            # Let's inspect failed parsing
            pass

print(f"Parsed native species: {len(arg_list)} / 1033")

# Parse Apéndice 3 (Introducidas)
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

print(f"Total parsed with introduced: {len(arg_list)} / 1043")

# Find any unparsed between 1 and 1043
missing_ids = [i for i in range(1, 1044) if i not in arg_list]
print(f"Unparsed IDs: {missing_ids}")
