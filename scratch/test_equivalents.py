import json
import re
import sys

sys.stdout.reconfigure(encoding='utf-8', errors='replace')

with open('app/src/main/assets/birdnet/species.json', encoding='utf-8') as f:
    model_species = json.load(f)

model_aves = [s for s in model_species if s.get('class') == 'Aves']
model_canon_map = {s['canonical_scientific_name'].strip().lower(): s for s in model_aves}
model_sci_map = {s['scientific_name'].strip().lower(): s for s in model_aves}

# Read unmatched list
with open('scratch/all_unmatched.txt', encoding='utf-8') as f:
    unmatched_lines = [l.strip().split('\t') for l in f if l.strip()]

# Test taxonomic mapping rules
# Dictionary of known taxonomic equivalents / orthographic corrections
TAXONOMIC_EQUIVALENTS = {
    # Typo corrections in reference list
    'crypturellus tatuapa': 'Crypturellus tataupa',
    'pygoscelis adelia': 'Pygoscelis adeliae',
    'thalassarche melanophrys': 'Thalassarche melanophris',
    'thalasarche salvini': 'Thalassarche salvini',
    'fulmarius glacialoides': 'Fulmarus glacialoides',
    'phalacocorax magallanicus': 'Leucocarbo magellanicus',
    'pelecanoides magellani': 'Pelecanoides magellanicus',
    'gampsonyx swansonii': 'Gampsonyx swainsonii',
    'dromococcyx phasianelus': 'Dromococcyx phasianellus',
    'phaetornis pretrei': 'Phaethornis pretrei',
    'phaetornis eurynome': 'Phaethornis eurynome',
    'chlorostilbon lucidius': 'Chlorostilbon lucidus',
    'ochetoprhynchus andaecola': 'Ochetorhynchus andaecola',
    'piprites cholris': 'Piprites chloris',
    'icterus pyrroptherus': 'Icterus pyrrhopterus',
    'dryocopus schulzi': 'Dryocopus schulzii',
    'monerita cabeza negra': 'Poospiza melanoleuca',

    # Anatidae revisions (Anas split into Spatula, Mareca)
    'anas sibilatrix': 'Mareca sibilatrix',
    'anas versicolor': 'Spatula versicolor',
    'anas puna': 'Spatula puna',
    'anas discors': 'Spatula discors',
    'anas cyanoptera': 'Spatula cyanoptera',
    'anas platalea': 'Spatula platalea',

    # Phalacrocoracidae revisions (split into Nannopterum, Leucocarbo, Poikilocarbo)
    'phalacrocorax brasilianus': 'Nannopterum brasilianum',
    'phalacrocorax gaimardi': 'Poikilocarbo gaimardi',
    'phalacrocorax bougainvillii': 'Leucocarbo bougainvillii',
    'phalacrocorax atriceps': 'Leucocarbo atriceps',
    'phalacrocorax bransfieldensis': 'Leucocarbo bransfieldensis',
    'phalacrocorax georgianus': 'Leucocarbo georgianus',

    # Charadriidae / Scolopacidae / Laridae revisions
    'charadrius falklandicus': 'Anarhynchus falklandicus',
    'charadrius collaris': 'Anarhynchus collaris',
    'charadrius alticola': 'Anarhynchus alticola',
    'charadrius modestus': 'Anarhynchus modestus',
    'charadrius mongolus': 'Anarhynchus mongolus',
    'aphriza virgata': 'Calidris virgata',
    'tryngites subruficollis': 'Calidris subruficollis',

    # Falconidae revisions
    'milvago chimachima': 'Daptrius chimachima',
    'milvago chimango': 'Daptrius chimango',

    # Strigidae revisions
    'ciccaba virgata': 'Strix virgata',
    'ciccaba huhula': 'Strix huhula',
    'pseudoscops clamator': 'Asio clamator',

    # Picidae revisions
    'picoides fumigatus': 'Dryobates fumigatus',
    'dryocopus galeatus': 'Celeus galeatus',

    # Furnariidae revisions
    'cranioleuca sulphurifera': 'Limnoctites sulphuriferus',

    # Tyrannidae / Cotingidae / Pipridae revisions
    'xolmis pyrope': 'Pyrope pyrope',
    'xolmis cinereus': 'Nengetus cinereus',
    'xolmis coronatus': 'Neoxolmis coronatus',
    'xolmis rubetra': 'Neoxolmis rubetra',
    'xolmis salinarum': 'Neoxolmis salinarum',
    'xolmis dominicanus': 'Heteroxolmis dominicana',
    'pitangus lictor': 'Philohydor lictor',
    'pipra fascicauda': 'Pseudopipra pipra', # wait, check pipra fascicauda

    # Thraupidae / Fringillidae / Icteridae revisions
    'tachycineta meyeni': 'Tachycineta leucopyga',
    'saltator rufiventris': 'Pseudosaltator rufiventris',
    'pyrrhocoma ruficeps': 'Thlypopsis ruficeps',
    'pipraeidea bonariensis': 'Rauenia bonariensis',
    'tangara preciosa': 'Stilpnia preciosa',
    'tangara cayana': 'Stilpnia cayana',
    'phrygilus fruticeti': 'Rhopospina fruticeti',
    'phrygilus unicolor': 'Geospizopsis unicolor',
    'phrygilus carbonarius': 'Porphyrospiza carbonaria',
    'phrygilus alaudinus': 'Porphyrospiza alaudina',
    'phrygilus plebejus': 'Geospizopsis plebeja',
    'phrygilus dorsalis': 'Idiopsar dorsalis',
    'poospiza hypochondria': 'Poospizopsis hypochondria',
    'poospiza erythrophrys': 'Microspingus erythrophrys',
    'poospiza lateralis': 'Microspingus lateralis',
    'poospiza torquata': 'Microspingus torquatus',
    'poospiza melanoleuca': 'Microspingus melanoleucus',
    'compsospiza baeri': 'Poospiza baeri',
    'tiaris obscurus': 'Asemospiza obscura',
    'tiaris fuliginosus': 'Asemospiza fuliginosa',
    'cyanocompsa brissonii': 'Cyanoloxia brissonii',
    'sturnella superciliaris': 'Leistes superciliaris',
    'sturnella defilippii': 'Leistes defilippii',
    'sturnella loyca': 'Leistes loyca',
    'sporagra crassirostris': 'Spinus crassirostris',
    'sporagra magellanica': 'Spinus magellanicus',
    'sporagra atrata': 'Spinus atratus',
    'sporagra uropygialis': 'Spinus uropygialis',
    'sporagra barbata': 'Spinus barbatus',
    'carduelis chloris': 'Chloris chloris',
    'bubulcus ibis': 'Ardea ibis',
    'ixobrychus exilis': 'Botaurus exilis',
    'ixobrychus involucris': 'Botaurus involucris',
    'laterallus leucopyrrhus': 'Rufirallus leucopyrrhus',
    'porzana flaviventer': 'Laterallus flaviventer',
    'porzana albicollis': 'Mustelirallus albicollis',
    'neocrex erythrops': 'Mustelirallus erythrops',
    'gallinula melanops': 'Porphyriops melanops',
    'vanellus cayanus': 'Hoploxypterus cayanus',
    'claravis geoffroyi': 'Paraclaravis geoffroyi',
    'sephanoides sephanoides': 'Sephanoides sephaniodes', # check spelling
    'hydropsalis maculicaudus': 'Antiurus maculicaudus',
    'anthus lutescens': 'Anthus chii',
}

# Test these equivalents against species.json
resolved_count = 0
still_unmatched = []

for u_id, u_vulg, u_sci in unmatched_lines:
    sci_key = u_sci.strip().lower()
    equiv = TAXONOMIC_EQUIVALENTS.get(sci_key)
    if equiv:
        # Check if equiv in model
        m = model_canon_map.get(equiv.lower()) or model_sci_map.get(equiv.lower())
        if m:
            resolved_count += 1
            continue
        else:
            still_unmatched.append((u_id, u_vulg, u_sci, f"equiv '{equiv}' not in model"))
    else:
        still_unmatched.append((u_id, u_vulg, u_sci, "no equiv defined"))

print(f"Total unmatched originally: {len(unmatched_lines)}")
print(f"Resolved via taxonomic equivalents in model: {resolved_count}")
print(f"Remaining unmatched: {len(still_unmatched)}")

with open('scratch/remaining_unmatched.txt', 'w', encoding='utf-8') as f:
    for item in still_unmatched:
        f.write(f"{item[0]}\t{item[1]}\t{item[2]}\t{item[3]}\n")
