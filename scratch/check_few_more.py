import json

with open('app/src/main/assets/birdnet/species.json', encoding='utf-8') as f:
    model_species = json.load(f)

for s in model_species:
    canon = s.get('canonical_scientific_name', '')
    if any(k in canon.lower() for k in ['fascicauda', 'sephaniodes', 'rufum', 'rufus', 'rubetra', 'plebeja', 'plebejus', 'salinarum']):
        print(s['idx'], canon)
