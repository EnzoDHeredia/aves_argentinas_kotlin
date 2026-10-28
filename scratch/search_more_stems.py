import json
import re

with open('app/src/main/assets/birdnet/species.json', encoding='utf-8') as f:
    model_species = json.load(f)

model_aves = {s['canonical_scientific_name'].lower(): s for s in model_species if s.get('class') == 'Aves'}

def search_model(query):
    query = query.lower()
    res = []
    for k, v in model_aves.items():
        if query in k:
            res.append(v['canonical_scientific_name'])
    return res

remaining_names = [
    ('Cinclus schulzi', 'Mirlo de agua'),
    ('Cissopis leveriana', 'Frutero overo'),
    ('Molothrus rufaxillaris', 'Tordo pico corto'),
    ('Sappho sparganura', 'Picaflor cometa'),
    ('Eriocnemis glaucopoides', 'Picaflor frente azul'),
    ('Falco deiroleucus', 'Halcón negro grande'),
    ('Serpophaga munda', 'Piojito vientre blanco'),
    ('Ramphotrigon megacephala', 'Picochato cabezón'),
    ('Glaucidium nanum', 'Caburé grande'),
    ('Macropsalis forcipata', 'Atajacaminos coludo'),
    ('Pelagodroma marina', 'Paíño cara blanca'),
    ('Pelecanoides georgicus', 'Yunco geórgico'),
    ('Pelecanoides magellani', 'Yunco magallanico'),
    ('Fregetta grallaria', 'Paíño vientre blanco'),
    ('Garrodia nereis', 'Paíño gris'),
    ('Oceanodroma leucorrhoa', 'Paíño boreal'),
    ('Thalassarche chrysostoma', 'Albatros cabeza gris'),
    ('Thalassarche steadi', 'Albatros corona blanca'),
    ('Thalassoica antarctica', 'Petrel antártico'),
    ('Aphrodroma brevirostris', 'Petrel pizarra'),
    ('Pterodroma macroptera', 'Petrel alas grandes'),
    ('Pterodroma incerta', 'Petrel cabeza parda'),
    ('Pterodroma lessonii', 'Petrel cabeza blanca'),
    ('Pterodroma arminjoniana', 'Petrel de Trinidad'),
    ('Procellaria conspicillata', 'Petrel de anteojos'),
    ('Diomedea dabbenena', 'Albatros de Tristán'),
    ('Diomedea sanfordi', 'Albatros real del norte'),
    ('Phoebetria fusca', 'Albatros oscuro'),
    ('Eudyptes moseleyi', 'Pingüino de Tristán'),
    ('Eudyptes robustus', 'Pingüino de Snares'),
    ('Eudyptes sclateri', 'Pingüino crestudo'),
    ('Phalcoboenus albogularis', 'Matamico blanco'),
    ('Phalcoboenus australis', 'Matamico grande'),
    ('Philydor rufum', 'Ticotico grande'),
    ('Asthenes sclateri', 'Espartillero serrano'),
    ('Phylloscartes eximius', 'Mosqueta media luna'),
    ('Pyrrhomyias cinnamomea', 'Birro chico'),
    ('Agriornis murinus', 'Gaucho chico'),
    ('Neoxolmis rubetra', 'Monjita castaña'),
    ('Neoxolmis salinarum', 'Monjita salinera'),
    ('Coturnicops notatus', 'Burrito enano'),
    ('Porzana spiloptera', 'Burrito negruzco'),
    ('Columbina squamata', 'Torcacita escamada'),
    ('Claravis geoffroyi', 'Palomita morada'),
    ('Cypseloides rothschildi', 'Vencejo parduzco'),
    ('Veniliornis frontalis', 'Carpintero oliva yungeño'),
]

for sci, vulg in remaining_names:
    genus, ep = sci.split()[0], sci.split()[1]
    # search genus
    g_res = search_model(genus)
    # search ep stem
    stem = ep[:-2] if len(ep) > 4 else ep
    e_res = search_model(stem)
    print(f"{vulg:25} | {sci:28}: Genus matches={len(g_res)}, Stem '{stem}' matches={e_res[:3]}")
