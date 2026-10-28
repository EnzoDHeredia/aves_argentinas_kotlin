import re
import json

with open('app/build/t4/categorizacion.txt', encoding='utf-8', errors='ignore') as f:
    text = f.read()

# Apéndice 1 starts at page 75
# Let's extract species from Apéndice 1
# Format usually: ID NOMBRE VULGAR NOMBRE CIENTÍFICO CAT. 2015 UICN-N 2015 ...
# e.g.:
# 1 Ñandú Rhea americana VU NT - G. Fernández
# 2 Choique Rhea pennata* VU NT - D. De Lamo, M. Frixione, L. Bellis*

pattern = re.compile(r'^\s*(\d+)\s+([A-ZÁÉÍÓÚÑa-záéíóúñ\s\(\)\-\'\/\.\,\;]+?)\s+([A-Z][a-z]+(?:\s+[a-z]+)?)\*?\s+(VU|AM|NA|EC|EX|EP|NC|IN)', re.M)

found = []
for line in text.split('\n'):
    m = pattern.match(line)
    if m:
        id_num = int(m.group(1))
        vulgar = m.group(2).strip()
        sci = m.group(3).strip()
        cat = m.group(4).strip()
        found.append((id_num, vulgar, sci, cat))

print(f"Total lines matched: {len(found)}")
if found:
    print("First 10:", found[:10])
    print("Last 10:", found[-10:])
    
# Check missing IDs from 1 to max_id
ids = [x[0] for x in found]
missing = [i for i in range(1, max(ids)+1) if i not in ids]
print(f"Max ID: {max(ids)}, Total unique IDs: {len(set(ids))}, Missing count: {len(missing)}")
if missing:
    print(f"First 20 missing IDs: {missing[:20]}")
