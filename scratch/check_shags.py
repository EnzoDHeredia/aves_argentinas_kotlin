import json

with open('app/src/main/assets/birdnet/species.json', encoding='utf-8') as f:
    model_species = json.load(f)

for s in model_species:
    canon = s.get('canonical_scientific_name', '')
    if 'atriceps' in canon.lower() or 'phalacrocorax' in canon.lower() or 'leucocarbo' in canon.lower():
        print(s['idx'], canon)
