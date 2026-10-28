import json

with open('app/src/main/assets/birdnet/species.json', encoding='utf-8') as f:
    model_species = json.load(f)

model_aves = {s['canonical_scientific_name']: s for s in model_species if s.get('class') == 'Aves'}

check = [
    'Microspizias superciliosus',
    'Astur bicolor',
    'Zonibyx modestus',
    'Zentrygon frenata',
    'Elliotomyia chionogaster',
    'Chrysuronia versicolor',
    'Chionomesa lactea',
    'Chlorestes cyanus',
    'Leuconotopicus fumigatus',
    'Daptrius megalopterus',
    'Cryptopezus nattereri',
    'Sylviorthorhynchus yanacensis',
    'Acrochordopus burmeisteri',
    'Pogonotriccus paulista',
    'Cnemarchus rufipennis',
    'Geospizopsis plebejus',
    'Chlorophonia cyanocephala'
]

for c in check:
    print(c, '->', 'FOUND' if c in model_aves else 'NOT FOUND', f"(idx {model_aves[c]['idx']})" if c in model_aves else "")
