import re
import sys

sys.stdout.reconfigure(encoding='utf-8', errors='replace')

with open('scratch/darrieu2013_clean.txt', encoding='utf-8', errors='replace') as f:
    text = f.read()

# Let's find the species in Darrieu et al. (2013)
# In Darrieu 2013, the systematic list lists each species with:
# Binomial name (italic in original, e.g. "Rhea americana")
# National category (e.g. AM, NA, VU, etc.)
# Common name in Spanish - English (e.g. "Ñandú – Greater Rhea")
# Distribution in Buenos Aires (e.g. "Toda la provincia", "Áreas 1, 2", etc.)

# Let's write a parser for Darrieu
# Find all occurrences of Genus species followed by category and vulgar name
pattern = re.compile(r'([A-Z][a-z]+)\s+([a-z]+)\s*\n\s*(AM|NA|VU|EN|CR|DD|EX)\s*\n\s*([^–\n]+?)\s*–\s*([^\n]+)', re.M)

matches = pattern.findall(text)
print(f"Direct Darrieu matches: {len(matches)}")
darrieu_species = {}
for m in matches:
    sci = f"{m[0]} {m[1]}"
    cat = m[2]
    vulgar_es = m[3].strip()
    vulgar_en = m[4].strip()
    darrieu_species[sci.lower()] = {
        'scientific_name': sci,
        'vulgar_es': vulgar_es,
        'vulgar_en': vulgar_en,
        'category': cat
    }

print("Sample 10 Darrieu species:")
for k in list(darrieu_species.keys())[:10]:
    print(f"  {darrieu_species[k]['scientific_name']} -> {darrieu_species[k]['vulgar_es']}")
