import json
import re
import sys
from pathlib import Path

sys.stdout.reconfigure(encoding='utf-8', errors='replace')

# 1. Load species.json
with open('app/src/main/assets/birdnet/species.json', encoding='utf-8') as f:
    model_species = json.load(f)

model_aves = [s for s in model_species if s.get('class') == 'Aves']
total_classes = len(model_species)
total_aves_model = len(model_aves)

model_by_canonical = {s['canonical_scientific_name'].strip().lower(): s for s in model_aves}
model_by_sci = {s['scientific_name'].strip().lower(): s for s in model_aves}

# 2. Load labels_map.json (local Buenos Aires)
with open('app/src/main/assets/labels_map.json', encoding='utf-8') as f:
    labels_map = json.load(f)

# 3. Load reference list (1043 species from categorizacion.txt)
with open('app/build/t4/categorizacion.txt', encoding='utf-8', errors='ignore') as f:
    cat_text = f.read()

start_idx = cat_text.find('PAGE 75')
end_idx = cat_text.find('PAGE 144')
lines = cat_text[start_idx:end_idx].split('\n')

raw_entries = {}
for i, line in enumerate(lines):
    line_s = line.strip()
    m = re.match(r'^(\d+)\s+(.+)$', line_s)
    if m:
        num = int(m.group(1))
        if 1 <= num <= 1033 and num not in raw_entries:
            raw_entries[num] = (i, m.group(2).strip())

arg_ref = {}
special_cases = {
    154: ('Pelícano pardo', 'Pelecanus thagus', 'n/e'),
    598: ('Picapalo colorado', 'Campylorhamphus trochilirostris', 'NA'),
    668: ('Chotoy', 'Schoeniophylax phryganophilus', 'NA'),
    725: ('Mosqueta ojo dorado', 'Hemitriccus margaritaceiventer', 'NA'),
    795: ('Tuquito gris', 'Empidonomus aurantioatrocristatus', 'NA'),
}

for num in range(1, 1034):
    if num in special_cases:
        v, s, c = special_cases[num]
        arg_ref[num] = {'id': num, 'vulgar': v, 'scientific_name': s, 'category': c, 'origin': 'native'}
        continue
    line_idx, text_line = raw_entries[num]
    m = re.search(r'([A-Z][a-z]{2,}(?:\s+[a-z]{3,}(?:\s+[a-z]{3,})?)?)\*?\s+(VU|AM|NA|EC|EX|EP|NC|EN|IC|DD|n/e)\b', text_line)
    if not m:
        m = re.search(r'([A-Z][a-z]{2,}\s+[a-z]{3,})\*?\s+(VU|AM|NA|EC|EX|EP|NC|EN|IC|DD|n/e)\b', text_line)
    if m:
        sci = m.group(1).strip()
        cat = m.group(2)
        vulgar = text_line[:m.start()].strip()
        vulgar = re.sub(r'\s*\(\s*E\s*\)\s*$', '', vulgar).strip()
        arg_ref[num] = {'id': num, 'vulgar': vulgar, 'scientific_name': sci, 'category': cat, 'origin': 'native'}

# Apéndice 3
start_ap3 = cat_text.find('PAGE 144')
end_ap3 = cat_text.find('PAGE 145')
for line in cat_text[start_ap3:end_ap3].split('\n'):
    m = re.match(r'^(\d+)\s+([A-ZÁÉÍÓÚÑa-záéíóúñ\s]+?)\s+([A-Z][a-z]+\s+[a-z]+)\s+IN\b', line.strip())
    if m:
        id_num = int(m.group(1))
        arg_ref[id_num] = {'id': id_num, 'vulgar': m.group(2).strip(), 'scientific_name': m.group(3).strip(), 'category': 'IN', 'origin': 'introduced'}

# Taxonomic equivalents map (scientific name in ref -> canonical in model)
TAXON_MAP = {
    # Orthographic / Typo corrections
    'crypturellus tatuapa': ('Crypturellus tataupa', 'Corrección ortográfica del epíteto (tataupa vs tatuapa)'),
    'pygoscelis adelia': ('Pygoscelis adeliae', 'Variante ortográfica clásica (adeliae vs adelia)'),
    'thalassarche melanophrys': ('Thalassarche melanophris', 'Grafía canónica en Clements/BirdNET (melanophris)'),
    'thalasarche salvini': ('Thalassarche salvini', 'Corrección de errata tipográfica en lista (Thalasarche -> Thalassarche)'),
    'fulmarius glacialoides': ('Fulmarus glacialoides', 'Corrección de errata tipográfica en lista (Fulmarius -> Fulmarus)'),
    'phalacocorax magallanicus': ('Leucocarbo magellanicus', 'Revisión genérica Phalacrocoracidae y corrección ortográfica'),
    'gampsonyx swansonii': ('Gampsonyx swainsonii', 'Corrección ortográfica de dedicatoria (swainsonii)'),
    'dromococcyx phasianelus': ('Dromococcyx phasianellus', 'Corrección ortográfica (phasianellus con doble l)'),
    'phaetornis pretrei': ('Phaethornis pretrei', 'Corrección tipográfica de género (Phaethornis con h)'),
    'phaetornis eurynome': ('Phaethornis eurynome', 'Corrección tipográfica de género (Phaethornis con h)'),
    'chlorostilbon lucidius': ('Chlorostilbon lucidus', 'Variante de género gramatical (lucidus)'),
    'ochetoprhynchus andaecola': ('Ochetorhynchus andaecola', 'Corrección tipográfica (Ochetorhynchus sin prh)'),
    'piprites cholris': ('Piprites chloris', 'Corrección tipográfica en epíteto (chloris)'),
    'icterus pyrroptherus': ('Icterus pyrrhopterus', 'Corrección tipográfica en epíteto (pyrrhopterus)'),
    'dryocopus schulzi': ('Dryocopus schulzii', 'Variante ortográfica de dedicatoria (schulzii con doble i)'),
    'cinclus schulzi': ('Cinclus schulzii', 'Variante ortográfica de dedicatoria (schulzii con doble i)'),
    'cissopis leveriana': ('Cissopis leverianus', 'Acuerdo de género gramatical canónico (leverianus)'),
    'sappho sparganura': ('Sappho sparganurus', 'Acuerdo de género gramatical canónico (sparganurus)'),
    'ramphotrigon megacephala': ('Ramphotrigon megacephalum', 'Acuerdo de género neutro canónico (megacephalum)'),
    'spiziapteryx circumcinctus': ('Spiziapteryx circumcincta', 'Acuerdo de género femenino canónico (circumcincta)'),
    'poospizopsis hypochondria': ('Poospizopsis hypocondria', 'Grafía canónica en BirdNET/Clements (hypocondria)'),
    'poospiza hypochondria': ('Poospizopsis hypocondria', 'Transferencia genérica y grafía canónica (Poospizopsis hypocondria)'),
    'philydor rufum': ('Philydor rufus', 'Acuerdo de género gramatical canónico (rufus)'),
    'pyrrhomyias cinnamomea': ('Pyrrhomyias cinnamomeus', 'Acuerdo de género gramatical canónico (cinnamomeus)'),
    'agriornis murinus': ('Agriornis murina', 'Acuerdo de género femenino canónico (murina)'),

    # Generic splits and modern revisions (SACC / Clements)
    'anas sibilatrix': ('Mareca sibilatrix', 'Segregación del género Anas hacia Mareca'),
    'anas versicolor': ('Spatula versicolor', 'Segregación del género Anas hacia Spatula'),
    'anas puna': ('Spatula puna', 'Segregación del género Anas hacia Spatula'),
    'anas discors': ('Spatula discors', 'Segregación del género Anas hacia Spatula'),
    'anas cyanoptera': ('Spatula cyanoptera', 'Segregación del género Anas hacia Spatula'),
    'anas platalea': ('Spatula platalea', 'Segregación del género Anas hacia Spatula'),

    'phalacrocorax brasilianus': ('Nannopterum brasilianum', 'Transferencia de Phalacrocorax neotropicales a Nannopterum'),
    'phalacrocorax gaimardi': ('Poikilocarbo gaimardi', 'Segregación del cormorán gris hacia Poikilocarbo'),
    'phalacrocorax atriceps': ('Leucocarbo atriceps', 'Transferencia genérica a Leucocarbo'),
    'phalacrocorax bougainvillii': ('Leucocarbo bougainvillii', 'Transferencia genérica a Leucocarbo'),
    'phalacrocorax bransfieldensis': ('Leucocarbo bransfieldensis', 'Transferencia genérica a Leucocarbo'),
    'phalacrocorax georgianus': ('Leucocarbo georgianus', 'Transferencia genérica a Leucocarbo'),

    'charadrius falklandicus': ('Anarhynchus falklandicus', 'Transferencia de chorlitos neotropicales a Anarhynchus (WACC/Clements 2024)'),
    'charadrius collaris': ('Anarhynchus collaris', 'Transferencia genérica a Anarhynchus'),
    'charadrius alticola': ('Anarhynchus alticola', 'Transferencia genérica a Anarhynchus'),
    'charadrius modestus': ('Zonibyx modestus', 'Transferencia genérica a Zonibyx'),
    'charadrius mongolus': ('Anarhynchus mongolus', 'Transferencia genérica a Anarhynchus'),
    'aphriza virgata': ('Calidris virgata', 'Fusión del género Aphriza en Calidris'),
    'tryngites subruficollis': ('Calidris subruficollis', 'Fusión del género Tryngites en Calidris'),

    'milvago chimachima': ('Daptrius chimachima', 'Fusión sistemática de caracaras Milvago/Phalcoboenus en Daptrius'),
    'milvago chimango': ('Daptrius chimango', 'Fusión sistemática en Daptrius (modelo BirdNET)'),
    'phalcoboenus megalopterus': ('Daptrius megalopterus', 'Fusión sistemática en Daptrius'),
    'phalcoboenus australis': ('Daptrius australis', 'Fusión sistemática en Daptrius'),

    'ciccaba virgata': ('Strix virgata', 'Inclusión de Ciccaba en Strix'),
    'ciccaba huhula': ('Strix huhula', 'Inclusión de Ciccaba en Strix'),
    'pseudoscops clamator': ('Asio clamator', 'Inclusión de Pseudoscops en Asio'),

    'accipiter superciliosus': ('Microspizias superciliosus', 'Transferencia de esparvero chico a Microspizias'),
    'accipiter bicolor': ('Astur bicolor', 'Transferencia de gavilanes neotropicales a Astur'),
    'accipiter bicolor chilensis': ('Astur chilensis', 'Tratamiento a nivel de especie en Clements/BirdNET (Astur chilensis)'),

    'geotrygon frenata': ('Zentrygon frenata', 'Segregación de palomas montaraces en Zentrygon'),
    'amazilia chionogaster': ('Elliotomyia chionogaster', 'Transferencia genérica a Elliotomyia'),
    'amazilia versicolor': ('Chrysuronia versicolor', 'Transferencia genérica a Chrysuronia'),
    'amazilia lactea': ('Chionomesa lactea', 'Transferencia genérica a Chionomesa'),
    'hylocharis cyanus': ('Chlorestes cyanus', 'Transferencia genérica a Chlorestes'),
    'picoides fumigatus': ('Leuconotopicus fumigatus', 'Transferencia genérica a Leuconotopicus'),
    'dryocopus galeatus': ('Celeus galeatus', 'Reclasificación filogenética de carpintero cara canela a Celeus'),
    'hylopezus nattereri': ('Cryptopezus nattereri', 'Segregación monotípica de chululú chico en Cryptopezus'),
    'cranioleuca sulphurifera': ('Limnoctites sulphuriferus', 'Transferencia genérica a Limnoctites'),
    'leptasthenura yanacensis': ('Sylviorthorhynchus yanacensis', 'Transferencia genérica a Sylviorthorhynchus'),
    'phyllomyias burmeisteri': ('Acrochordopus burmeisteri', 'Resurrección del género Acrochordopus'),
    'phylloscartes paulista': ('Pogonotriccus paulista', 'Transferencia genérica a Pogonotriccus'),
    'polioxolmis rufipennis': ('Cnemarchus rufipennis', 'Fusión genérica en Cnemarchus'),

    'xolmis pyrope': ('Pyrope pyrope', 'Segregación monotípica de diucón en Pyrope'),
    'xolmis cinereus': ('Nengetus cinereus', 'Transferencia de monjita gris a Nengetus'),
    'xolmis coronatus': ('Neoxolmis coronatus', 'Transferencia de monjita coronada a Neoxolmis'),
    'xolmis rubetra': ('Neoxolmis rubetra', 'Transferencia de monjita castaña a Neoxolmis'),
    'xolmis dominicanus': ('Heteroxolmis dominicana', 'Segregación genérica de monjita dominica a Heteroxolmis'),
    'pitangus lictor': ('Philohydor lictor', 'Segregación monotípica de benteveo pico fino a Philohydor'),

    'tachycineta meyeni': ('Tachycineta leucopyga', 'Uso del epíteto prioritario leucopyga para golondrina patagónica'),
    'saltator rufiventris': ('Pseudosaltator rufiventris', 'Segregación monotípica de pepitero colorado en Pseudosaltator'),
    'pyrrhocoma ruficeps': ('Thlypopsis ruficeps', 'Fusión de Pyrrhocoma en Thlypopsis'),
    'pipraeidea bonariensis': ('Rauenia bonariensis', 'Resurrección del género Rauenia para naranjero'),
    'tangara preciosa': ('Stilpnia preciosa', 'Segregación del género Stilpnia de Tangara'),
    'tangara cayana': ('Stilpnia cayana', 'Segregación del género Stilpnia de Tangara'),

    'phrygilus fruticeti': ('Rhopospina fruticeti', 'Segregación filogenética de Phrygilus a Rhopospina'),
    'phrygilus unicolor': ('Geospizopsis unicolor', 'Segregación de Phrygilus a Geospizopsis'),
    'phrygilus carbonarius': ('Rhopospina carbonaria', 'Transferencia genérica a Rhopospina (Clements)'),
    'phrygilus alaudinus': ('Rhopospina alaudina', 'Transferencia genérica a Rhopospina (Clements)'),
    'phrygilus plebejus': ('Geospizopsis plebeja', 'Segregación de Phrygilus a Geospizopsis'),
    'phrygilus dorsalis': ('Idiopsar dorsalis', 'Transferencia de comesebos cordilleranos a Idiopsar'),

    'poospiza erythrophrys': ('Microspingus erythrophrys', 'Segregación de monteritas en Microspingus'),
    'poospiza lateralis': ('Microspingus lateralis', 'Segregación de monteritas en Microspingus'),
    'poospiza torquata': ('Microspingus torquatus', 'Segregación de monteritas en Microspingus'),
    'poospiza melanoleuca': ('Microspingus melanoleucus', 'Segregación de monteritas en Microspingus'),
    'compsospiza baeri': ('Poospiza baeri', 'Fusión de Compsospiza en Poospiza'),

    'tiaris obscurus': ('Asemospiza obscura', 'Segregación de semilleros Tiaris en Asemospiza'),
    'tiaris fuliginosus': ('Asemospiza fuliginosa', 'Segregación de semilleros Tiaris en Asemospiza'),
    'cyanocompsa brissonii': ('Cyanoloxia brissonii', 'Fusión de Cyanocompsa sudamericanas en Cyanoloxia'),
    'sturnella superciliaris': ('Leistes superciliaris', 'Segregación de pechos colorados en Leistes'),
    'sturnella defilippii': ('Leistes defilippii', 'Segregación de pechos colorados en Leistes'),
    'sturnella loyca': ('Leistes loyca', 'Segregación de pechos colorados en Leistes'),

    'sporagra crassirostris': ('Spinus crassirostris', 'Reunificación de jilgueros y cabecitas negras en Spinus'),
    'sporagra magellanica': ('Spinus magellanicus', 'Reunificación en Spinus'),
    'sporagra atrata': ('Spinus atratus', 'Reunificación en Spinus'),
    'sporagra uropygialis': ('Spinus uropygialis', 'Reunificación en Spinus'),
    'sporagra barbata': ('Spinus barbatus', 'Reunificación en Spinus'),
    'carduelis chloris': ('Chloris chloris', 'Segregación del verderón europeo en Chloris'),
    'euphonia cyanocephala': ('Chlorophonia cyanocephala', 'Transferencia filogenética de tangará cabeza celeste a Chlorophonia'),

    'bubulcus ibis': ('Ardea ibis', 'Inclusión de Bubulcus en Ardea (modelo BirdNET)'),
    'ixobrychus exilis': ('Botaurus exilis', 'Inclusión de mirasoles Ixobrychus en Botaurus'),
    'ixobrychus involucris': ('Botaurus involucris', 'Inclusión de mirasoles Ixobrychus en Botaurus'),
    'laterallus leucopyrrhus': ('Rufirallus leucopyrrhus', 'Segregación genérica en Rufirallus'),
    'porzana flaviventer': ('Laterallus flaviventer', 'Transferencia genérica a Laterallus'),
    'porzana albicollis': ('Mustelirallus albicollis', 'Transferencia genérica a Mustelirallus'),
    'neocrex erythrops': ('Mustelirallus erythrops', 'Transferencia genérica a Mustelirallus'),
    'gallinula melanops': ('Porphyriops melanops', 'Segregación genérica en Porphyriops'),
    'vanellus cayanus': ('Hoploxypterus cayanus', 'Segregación monotípica en Hoploxypterus'),
    'claravis geoffroyi': ('Paraclaravis geoffroyi', 'Segregación monotípica de palomita morada en Paraclaravis'),
    'hydropsalis maculicaudus': ('Antiurus maculicaudus', 'Segregación monotípica de atajacaminos ceja blanca en Antiurus'),
    'anthus lutescens': ('Anthus chii', 'Uso prioritario del epíteto chii según dictamen SACC/ICZN'),
}

# Run the categorization
exact_matches = []
equivalent_matches = []
absent_species = []
ambiguous_cases = []

# Ambiguous cases definition:
# Species where Clements split/lump or extreme taxonomic flux makes 1:1 mapping non-trivial:
# e.g.:
# 1. Troglodytes aedon / Troglodytes musculus:
#    In Argentina the house wren is Troglodytes aedon chilensis/bonariae/musculus.
#    In BirdNET, both Troglodytes aedon (idx 9323) and Troglodytes musculus (idx 9338) exist!
#    In Argentina it is Ratona común.
# 2. Xolmis salinarum (Monjita salinera, endemic):
#    In Clements often treated as Neoxolmis salinarum or subspecies. Absent in BirdNET 10K.
# 3. Phalacrocorax atriceps vs Leucocarbo atriceps / Leucocarbo bransfieldensis / georgianus:
#    The blue-eyed shag complex has been split into Leucocarbo atriceps, L. bransfieldensis, L. georgianus,
#    which in BirdNET are separate classes or pruned.
# 4. Accipiter bicolor chilensis vs Astur bicolor:
#    Astur chilensis is recognized as full species in BirdNET (idx 32).

for id_num, sp in sorted(arg_ref.items()):
    sci = sp['scientific_name'].strip()
    sci_lower = sci.lower()
    
    # Check exact match first
    m = model_by_canonical.get(sci_lower) or model_by_sci.get(sci_lower)
    if m:
        exact_matches.append((sp, m))
        continue
    
    # Check taxon map
    if sci_lower in TAXON_MAP:
        target_name, reason = TAXON_MAP[sci_lower]
        m = model_by_canonical.get(target_name.lower()) or model_by_sci.get(target_name.lower())
        if m:
            equivalent_matches.append((sp, m, target_name, reason))
            continue
        else:
            # Equivalent is known, but absent from model
            absent_species.append((sp, f"Sinonimia '{target_name}' verificada, pero la especie no está en el modelo BirdNET"))
            continue
    
    # Not exact and not in taxon map: absent from model
    absent_species.append((sp, "Ausente de la taxonomía del modelo BirdNET+ (10K pruned)"))

print(f"=== RESULTADOS DE COBERTURA (TOTAL REFERENCIA: {len(arg_ref)}) ===")
print(f"1. Coincidencias exactas directas: {len(exact_matches)} ({len(exact_matches)/len(arg_ref)*100:.1f}%)")
print(f"2. Equivalencias taxonómicas verificadas: {len(equivalent_matches)} ({len(equivalent_matches)/len(arg_ref)*100:.1f}%)")
total_covered = len(exact_matches) + len(equivalent_matches)
print(f"   -> TOTAL COBERTURA MODELO: {total_covered} ({total_covered/len(arg_ref)*100:.1f}%)")
print(f"3. Especies ausentes del modelo: {len(absent_species)} ({len(absent_species)/len(arg_ref)*100:.1f}%)")
