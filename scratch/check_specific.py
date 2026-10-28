import json

with open('app/src/main/assets/birdnet/species.json', encoding='utf-8') as f:
    model_species = json.load(f)

for name in ['Tigrisoma fasciatum', 'Cathartes burrovianus', 'Sarcoramphus papa', 'Heteronetta atricapilla']:
    found = [s for s in model_species if name.lower() in s['canonical_scientific_name'].lower() or name.lower() in s['scientific_name'].lower()]
    print(name, '->', found)
