import json

with open('app/src/main/assets/birdnet/species.json', encoding='utf-8') as f:
    model_species = json.load(f)

for s in model_species:
    for q in ['porphyrospiza', 'rhopospina', 'idiopsar', 'poospizopsis', 'anodorhynchus', 'numenius borealis', 'spiziapteryx', 'falco deiroleucus']:
        if q in s.get('canonical_scientific_name', '').lower():
            print(q, '->', s['canonical_scientific_name'], s['idx'])
