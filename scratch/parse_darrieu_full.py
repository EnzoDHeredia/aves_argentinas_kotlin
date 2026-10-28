import re
import sys

sys.stdout.reconfigure(encoding='utf-8', errors='replace')

with open('scratch/darrieu2013_clean.txt', encoding='utf-8', errors='replace') as f:
    text = f.read()

# Let's locate the species in Darrieu et al. 2013
# In Darrieu, each entry has:
# Genus species (or Genus species subsp.)
# Status/Category (AM, NA, VU, EN, CR, DD, EX)
# Common name in Spanish - English
# Distribution notes
lines = text.split('\n')

# Start after "Familia RHEIDAE" (line ~477)
# End at Apéndice or references (search for "Bibliografía" or "Especies accidentales")
darrieu_species = []

# Pattern: binomial name followed within 1-3 lines by a Spanish common name
# Let's inspect the text structure
start = 475
end = len(lines)
for i in range(start, len(lines)):
    if 'BIBLIOGRAFÍA' in lines[i].upper():
        end = i
        break

print(f"Systematic list between line {start} and {end}")

# Let's inspect all lines in this range
cur_sci = None
cur_cat = None
for i in range(start, end):
    l = lines[i].strip()
    # Is it a binomial?
    # Genus species (both Latin alphabet)
    m = re.match(r'^([A-Z][a-z]+)\s+([a-z]+(?:\s+[a-z]+)?)\s*$', l)
    if m:
        genus = m.group(1)
        if genus not in ['Orden', 'Familia', 'Subfamilia', 'Toda', 'Áreas', 'Área', 'Buenos', 'Aires']:
            # Candidate scientific name
            # Check if next few lines have vulgar name with dash
            found_vulgar = None
            for j in range(i+1, min(end, i+6)):
                if '\x96' in lines[j] or '–' in lines[j] or '-' in lines[j]:
                    parts = re.split(r'[\x96–\-]', lines[j])
                    if len(parts) >= 2 and len(parts[0].strip()) > 2:
                        found_vulgar = parts[0].strip()
                        break
            darrieu_species.append((genus + ' ' + m.group(2), found_vulgar))

print(f"Total binomial entries detected in Darrieu: {len(darrieu_species)}")
print("First 15:")
for s, v in darrieu_species[:15]:
    print(f"  {s:30} -> {v}")
