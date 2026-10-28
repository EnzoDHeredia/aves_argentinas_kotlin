import json

labels_map = json.load(open('app/src/main/assets/labels_map.json', encoding='utf-8'))
species = json.load(open('app/src/main/assets/birdnet/species.json', encoding='utf-8'))
canonical_map = {s['canonical_scientific_name'].lower().replace(' ', '_'): s for s in species if s.get('class') == 'Aves'}
sci_map = {s['scientific_name'].lower().replace(' ', '_'): s for s in species if s.get('class') == 'Aves'}

print(f"Total entries in labels_map: {len(labels_map)}")
for k, v in sorted(labels_map.items()):
    match = canonical_map.get(k) or sci_map.get(k)
    print(f"{k:32} -> {v:25} | In species.json: {'YES (idx ' + str(match['idx']) + ')' if match else 'NO'}")
