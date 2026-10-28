import json

with open('app/src/main/assets/birdnet/species.json', encoding='utf-8') as f:
    model_species = json.load(f)

for s in model_species:
    if 'phaeomyias' in s.get('canonical_scientific_name', '').lower() or 'phaeomyias' in s.get('scientific_name', '').lower():
        print(s)
