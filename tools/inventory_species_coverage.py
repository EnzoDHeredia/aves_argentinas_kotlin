#!/usr/bin/env python3
"""Inventario reproducible de cobertura y correspondencias de especies - Spec 005 (T2).

Compara la lista oficial de aves argentinas (MAyDS / Aves Argentinas) con las
especies reconocibles del modelo BirdNET+ (app/src/main/assets/birdnet/species.json),
clasificando coincidencias exactas, equivalencias taxonómicas verificadas, ausencias
del modelo y casos taxonómicos ambiguos, distinguiendo la prioridad de Buenos Aires.
"""

import json
import re
import sys
from pathlib import Path

# Ensure stdout handles UTF-8 on Windows
if sys.stdout and hasattr(sys.stdout, "reconfigure"):
    try:
        sys.stdout.reconfigure(encoding="utf-8", errors="replace")
    except Exception:
        pass

ROOT = Path(__file__).resolve().parent.parent

# 1. Equivalencias taxonómicas verificadas (Nombre lista nacional -> Nombre canónico modelo)
# Respaldadas por revisiones de SACC / Clements / eBird y taxonomía oficial BirdNET
TAXONOMIC_EQUIVALENTS = {
    # Erratas y variantes ortográficas / de género en la fuente de referencia
    "crypturellus tatuapa": ("Crypturellus tataupa", "Corrección ortográfica de epíteto (tataupa vs tatuapa)"),
    "pygoscelis adelia": ("Pygoscelis adeliae", "Variante ortográfica clásica (adeliae vs adelia)"),
    "thalassarche melanophrys": ("Thalassarche melanophris", "Grafía canónica en Clements/BirdNET (melanophris)"),
    "thalasarche salvini": ("Thalassarche salvini", "Corrección de errata tipográfica en lista (Thalasarche -> Thalassarche)"),
    "fulmarius glacialoides": ("Fulmarus glacialoides", "Corrección de errata tipográfica en lista (Fulmarius -> Fulmarus)"),
    "phalacocorax magallanicus": ("Leucocarbo magellanicus", "Revisión genérica Phalacrocoracidae y corrección ortográfica"),
    "gampsonyx swansonii": ("Gampsonyx swainsonii", "Corrección ortográfica de dedicatoria (swainsonii)"),
    "dromococcyx phasianelus": ("Dromococcyx phasianellus", "Corrección ortográfica (phasianellus con doble l)"),
    "phaetornis pretrei": ("Phaethornis pretrei", "Corrección tipográfica de género (Phaethornis con h)"),
    "phaetornis eurynome": ("Phaethornis eurynome", "Corrección tipográfica de género (Phaethornis con h)"),
    "chlorostilbon lucidius": ("Chlorostilbon lucidus", "Variante de género gramatical canónico (lucidus)"),
    "ochetoprhynchus andaecola": ("Ochetorhynchus andaecola", "Corrección tipográfica (Ochetorhynchus sin prh)"),
    "piprites cholris": ("Piprites chloris", "Corrección tipográfica en epíteto (chloris)"),
    "icterus pyrroptherus": ("Icterus pyrrhopterus", "Corrección tipográfica en epíteto (pyrrhopterus)"),
    "dryocopus schulzi": ("Dryocopus schulzii", "Variante ortográfica de dedicatoria (schulzii con doble i)"),
    "cinclus schulzi": ("Cinclus schulzii", "Variante ortográfica de dedicatoria (schulzii con doble i)"),
    "cissopis leveriana": ("Cissopis leverianus", "Acuerdo de género gramatical canónico (leverianus)"),
    "sappho sparganura": ("Sappho sparganurus", "Acuerdo de género gramatical canónico (sparganurus)"),
    "ramphotrigon megacephala": ("Ramphotrigon megacephalum", "Acuerdo de género neutro canónico (megacephalum)"),
    "spiziapteryx circumcinctus": ("Spiziapteryx circumcincta", "Acuerdo de género femenino canónico (circumcincta)"),
    "poospizopsis hypochondria": ("Poospizopsis hypocondria", "Grafía canónica en BirdNET/Clements (hypocondria)"),
    "poospiza hypochondria": ("Poospizopsis hypocondria", "Transferencia genérica y grafía canónica (Poospizopsis hypocondria)"),
    "philydor rufum": ("Philydor rufus", "Acuerdo de género gramatical canónico (rufus)"),
    "pyrrhomyias cinnamomea": ("Pyrrhomyias cinnamomeus", "Acuerdo de género gramatical canónico (cinnamomeus)"),
    "agriornis murinus": ("Agriornis murina", "Acuerdo de género femenino canónico (Agriornis murina)"),
    "glaucidium nanum": ("Glaucidium nana", "Acuerdo de género femenino canónico en BirdNET (Glaucidium nana)"),
    "sephanoides sephanoides": ("Sephanoides sephaniodes", "Grafía canónica en Clements/BirdNET (sephaniodes)"),

    # Anatidae (segregaciones del género Anas a Spatula y Mareca)
    "anas sibilatrix": ("Mareca sibilatrix", "Segregación del género Anas hacia Mareca"),
    "anas versicolor": ("Spatula versicolor", "Segregación del género Anas hacia Spatula"),
    "anas puna": ("Spatula puna", "Segregación del género Anas hacia Spatula"),
    "anas discors": ("Spatula discors", "Segregación del género Anas hacia Spatula"),
    "anas cyanoptera": ("Spatula cyanoptera", "Segregación del género Anas hacia Spatula"),
    "anas platalea": ("Spatula platalea", "Segregación del género Anas hacia Spatula"),

    # Phalacrocoracidae (segregación en Nannopterum y Poikilocarbo)
    "phalacrocorax brasilianus": ("Nannopterum brasilianum", "Transferencia de cormorán biguá a Nannopterum"),
    "phalacrocorax gaimardi": ("Poikilocarbo gaimardi", "Segregación del cormorán gris hacia Poikilocarbo"),

    # Charadriiformes (segregaciones a Anarhynchus, Calidris, Zonibyx)
    "charadrius falklandicus": ("Anarhynchus falklandicus", "Transferencia a Anarhynchus (WACC/Clements 2024)"),
    "charadrius collaris": ("Anarhynchus collaris", "Transferencia genérica a Anarhynchus"),
    "charadrius alticola": ("Anarhynchus alticola", "Transferencia genérica a Anarhynchus"),
    "charadrius modestus": ("Zonibyx modestus", "Transferencia genérica a Zonibyx"),
    "charadrius mongolus": ("Anarhynchus mongolus", "Transferencia genérica a Anarhynchus"),
    "aphriza virgata": ("Calidris virgata", "Fusión del género Aphriza en Calidris"),
    "tryngites subruficollis": ("Calidris subruficollis", "Fusión del género Tryngites en Calidris"),

    # Falconiformes (fusión en Daptrius)
    "milvago chimachima": ("Daptrius chimachima", "Fusión sistemática en Daptrius"),
    "milvago chimango": ("Daptrius chimango", "Fusión sistemática en Daptrius"),
    "phalcoboenus megalopterus": ("Daptrius megalopterus", "Fusión sistemática en Daptrius"),
    "phalcoboenus australis": ("Daptrius australis", "Fusión sistemática en Daptrius"),

    # Strigiformes
    "ciccaba virgata": ("Strix virgata", "Inclusión de Ciccaba en Strix"),
    "ciccaba huhula": ("Strix huhula", "Inclusión de Ciccaba en Strix"),
    "pseudoscops clamator": ("Asio clamator", "Inclusión de Pseudoscops en Asio"),

    # Accipitriformes
    "accipiter superciliosus": ("Microspizias superciliosus", "Transferencia a Microspizias"),
    "accipiter bicolor": ("Astur bicolor", "Transferencia de gavilán común a Astur"),
    "accipiter bicolor chilensis": ("Astur chilensis", "Tratamiento a nivel de especie en Clements/BirdNET"),

    # Columbiformes
    "geotrygon frenata": ("Zentrygon frenata", "Segregación de palomas montaraces en Zentrygon"),

    # Trochilidae
    "amazilia chionogaster": ("Elliotomyia chionogaster", "Transferencia genérica a Elliotomyia"),
    "amazilia versicolor": ("Chrysuronia versicolor", "Transferencia genérica a Chrysuronia"),
    "amazilia lactea": ("Chionomesa lactea", "Transferencia genérica a Chionomesa"),
    "hylocharis cyanus": ("Chlorestes cyanus", "Transferencia genérica a Chlorestes"),

    # Picidae
    "picoides fumigatus": ("Leuconotopicus fumigatus", "Transferencia genérica a Leuconotopicus"),
    "dryocopus galeatus": ("Celeus galeatus", "Reclasificación filogenética a Celeus"),

    # Suboscines (Furnariidae, Grallariidae, Tyrannidae, etc.)
    "hylopezus nattereri": ("Cryptopezus nattereri", "Segregación monotípica de chululú chico en Cryptopezus"),
    "cranioleuca sulphurifera": ("Limnoctites sulphuriferus", "Transferencia genérica a Limnoctites"),
    "leptasthenura yanacensis": ("Sylviorthorhynchus yanacensis", "Transferencia genérica a Sylviorthorhynchus"),
    "phyllomyias burmeisteri": ("Acrochordopus burmeisteri", "Resurrección del género Acrochordopus"),
    "phylloscartes paulista": ("Pogonotriccus paulista", "Transferencia genérica a Pogonotriccus"),
    "polioxolmis rufipennis": ("Cnemarchus rufipennis", "Fusión genérica en Cnemarchus"),
    "xolmis pyrope": ("Pyrope pyrope", "Segregación monotípica de diucón en Pyrope"),
    "xolmis cinereus": ("Nengetus cinereus", "Transferencia de monjita gris a Nengetus"),
    "xolmis coronatus": ("Neoxolmis coronatus", "Transferencia de monjita coronada a Neoxolmis"),
    "xolmis dominicanus": ("Heteroxolmis dominicana", "Segregación genérica de monjita dominica a Heteroxolmis"),
    "pitangus lictor": ("Philohydor lictor", "Segregación monotípica a Philohydor"),

    # Oscines (Hirundinidae, Thraupidae, Fringillidae, Icteridae, etc.)
    "tachycineta meyeni": ("Tachycineta leucopyga", "Epíteto prioritario leucopyga para golondrina patagónica"),
    "saltator rufiventris": ("Pseudosaltator rufiventris", "Segregación monotípica en Pseudosaltator"),
    "pyrrhocoma ruficeps": ("Thlypopsis pyrrhocoma", "Fusión sistemática en Thlypopsis (evitando homonimia: Thlypopsis pyrrhocoma, idx: 9107)"),
    "pipraeidea bonariensis": ("Rauenia bonariensis", "Resurrección de Rauenia para naranjero"),
    "tangara preciosa": ("Stilpnia preciosa", "Segregación de Stilpnia de Tangara"),
    "tangara cayana": ("Stilpnia cayana", "Segregación de Stilpnia de Tangara"),
    "phrygilus fruticeti": ("Rhopospina fruticeti", "Segregación filogenética a Rhopospina"),
    "phrygilus unicolor": ("Geospizopsis unicolor", "Segregación filogenética a Geospizopsis"),
    "phrygilus carbonarius": ("Rhopospina carbonaria", "Transferencia genérica a Rhopospina (Clements)"),
    "phrygilus alaudinus": ("Rhopospina alaudina", "Transferencia genérica a Rhopospina (Clements)"),
    "phrygilus plebejus": ("Geospizopsis plebejus", "Segregación filogenética a Geospizopsis"),
    "phrygilus dorsalis": ("Idiopsar dorsalis", "Transferencia a Idiopsar"),
    "poospiza erythrophrys": ("Microspingus erythrophrys", "Segregación de monteritas en Microspingus"),
    "poospiza lateralis": ("Microspingus lateralis", "Segregación de monteritas en Microspingus"),
    "poospiza torquata": ("Microspingus torquatus", "Segregación de monteritas en Microspingus"),
    "poospiza melanoleuca": ("Microspingus melanoleucus", "Segregación de monteritas en Microspingus"),
    "compsospiza baeri": ("Poospiza baeri", "Fusión de Compsospiza en Poospiza"),
    "tiaris obscurus": ("Asemospiza obscura", "Segregación de semilleros Tiaris en Asemospiza"),
    "tiaris fuliginosus": ("Asemospiza fuliginosa", "Segregación de semilleros Tiaris en Asemospiza"),
    "cyanocompsa brissonii": ("Cyanoloxia brissonii", "Fusión en Cyanoloxia"),
    "sturnella superciliaris": ("Leistes superciliaris", "Segregación de pechos colorados en Leistes"),
    "sturnella defilippii": ("Leistes defilippii", "Segregación de pechos colorados en Leistes"),
    "sturnella loyca": ("Leistes loyca", "Segregación de pechos colorados en Leistes"),
    "sporagra crassirostris": ("Spinus crassirostris", "Reunificación en Spinus"),
    "sporagra magellanica": ("Spinus magellanicus", "Reunificación en Spinus"),
    "sporagra atrata": ("Spinus atratus", "Reunificación en Spinus"),
    "sporagra uropygialis": ("Spinus uropygialis", "Reunificación en Spinus"),
    "sporagra barbata": ("Spinus barbatus", "Reunificación en Spinus"),
    "carduelis chloris": ("Chloris chloris", "Segregación del verderón europeo en Chloris"),
    "euphonia cyanocephala": ("Chlorophonia cyanocephala", "Transferencia filogenética a Chlorophonia"),

    # Rallidae / Ardeidae / otros acuáticos
    "bubulcus ibis": ("Ardea ibis", "Inclusión de Bubulcus en Ardea (BirdNET)"),
    "ixobrychus exilis": ("Botaurus exilis", "Inclusión de Ixobrychus en Botaurus"),
    "ixobrychus involucris": ("Botaurus involucris", "Inclusión de Ixobrychus en Botaurus"),
    "laterallus leucopyrrhus": ("Rufirallus leucopyrrhus", "Segregación genérica en Rufirallus"),
    "porzana flaviventer": ("Laterallus flaviventer", "Transferencia genérica a Laterallus"),
    "porzana albicollis": ("Mustelirallus albicollis", "Transferencia genérica a Mustelirallus"),
    "neocrex erythrops": ("Mustelirallus erythrops", "Transferencia genérica a Mustelirallus"),
    "gallinula melanops": ("Porphyriops melanops", "Segregación genérica en Porphyriops"),
    "vanellus cayanus": ("Hoploxypterus cayanus", "Segregación monotípica en Hoploxypterus"),
    "hydropsalis maculicaudus": ("Antiurus maculicaudus", "Segregación monotípica en Antiurus"),
    "anthus lutescens": ("Anthus chii", "Uso prioritario del epíteto chii según dictamen SACC/ICZN"),
}


def load_model_species(path: Path):
    with path.open(encoding="utf-8") as f:
        data = json.load(f)
    aves = [s for s in data if s.get("class") == "Aves"]
    canonical_map = {s["canonical_scientific_name"].strip().lower(): s for s in aves}
    sci_map = {s["scientific_name"].strip().lower(): s for s in aves}
    return data, aves, canonical_map, sci_map


def load_argentine_reference(cat_path: Path):
    with cat_path.open(encoding="utf-8", errors="ignore") as f:
        cat_text = f.read()

    start_idx = cat_text.find("PAGE 75")
    end_idx = cat_text.find("PAGE 144")
    lines = cat_text[start_idx:end_idx].split("\n")

    raw_entries = {}
    for i, line in enumerate(lines):
        line_s = line.strip()
        m = re.match(r"^(\d+)\s+(.+)$", line_s)
        if m:
            num = int(m.group(1))
            if 1 <= num <= 1033 and num not in raw_entries:
                raw_entries[num] = (i, m.group(2).strip())

    arg_ref = {}
    special_cases = {
        154: ("Pelícano pardo", "Pelecanus thagus", "n/e"),
        598: ("Picapalo colorado", "Campylorhamphus trochilirostris", "NA"),
        668: ("Chotoy", "Schoeniophylax phryganophilus", "NA"),
        725: ("Mosqueta ojo dorado", "Hemitriccus margaritaceiventer", "NA"),
        795: ("Tuquito gris", "Empidonomus aurantioatrocristatus", "NA"),
    }

    for num in range(1, 1034):
        if num in special_cases:
            v, s, c = special_cases[num]
            arg_ref[num] = {"id": num, "vulgar": v, "scientific_name": s, "category": c, "origin": "native"}
            continue
        line_idx, text_line = raw_entries[num]
        m = re.search(r"([A-Z][a-z]{2,}(?:\s+[a-z]{3,}(?:\s+[a-z]{3,})?)?)\*?\s+(VU|AM|NA|EC|EX|EP|NC|EN|IC|DD|n/e)\b", text_line)
        if not m:
            m = re.search(r"([A-Z][a-z]{2,}\s+[a-z]{3,})\*?\s+(VU|AM|NA|EC|EX|EP|NC|EN|IC|DD|n/e)\b", text_line)
        if m:
            sci = m.group(1).strip()
            cat = m.group(2)
            vulgar = text_line[:m.start()].strip()
            vulgar = re.sub(r"\s*\(\s*E\s*\)\s*$", "", vulgar).strip()
            arg_ref[num] = {"id": num, "vulgar": vulgar, "scientific_name": sci, "category": cat, "origin": "native"}
        else:
            raise ValueError(f"No se pudo parsear la especie con ID {num}: {text_line}")

    # Apéndice 3: Especies introducidas documentadas con nidificación silvestre (IDs 1034 a 1043)
    start_ap3 = cat_text.find("PAGE 144")
    end_ap3 = cat_text.find("PAGE 145")
    for line in cat_text[start_ap3:end_ap3].split("\n"):
        m = re.match(r"^(\d+)\s+([A-ZÁÉÍÓÚÑa-záéíóúñ\s]+?)\s+([A-Z][a-z]+\s+[a-z]+)\s+IN\b", line.strip())
        if m:
            id_num = int(m.group(1))
            arg_ref[id_num] = {"id": id_num, "vulgar": m.group(2).strip(), "scientific_name": m.group(3).strip(), "category": "IN", "origin": "introduced"}

    return arg_ref


def build_inventory():
    species_path = ROOT / "app" / "src" / "main" / "assets" / "birdnet" / "species.json"
    cat_path = ROOT / "app" / "build" / "t4" / "categorizacion.txt"
    labels_map_path = ROOT / "app" / "src" / "main" / "assets" / "labels_map.json"

    model_all, model_aves, canon_map, sci_map = load_model_species(species_path)
    arg_ref = load_argentine_reference(cat_path)
    labels_map = json.loads(labels_map_path.read_text(encoding="utf-8"))

    exact_matches = []
    equivalent_matches = []
    absent_species = []

    for id_num, sp in sorted(arg_ref.items()):
        sci = sp["scientific_name"].strip()
        sci_lower = sci.lower()

        # Coincidencia directa
        m = canon_map.get(sci_lower) or sci_map.get(sci_lower)
        if m:
            exact_matches.append((sp, m))
            continue

        # Equivalencia taxonómica verificada
        if sci_lower in TAXONOMIC_EQUIVALENTS:
            target_name, reason = TAXONOMIC_EQUIVALENTS[sci_lower]
            m = canon_map.get(target_name.lower()) or sci_map.get(target_name.lower())
            if m:
                equivalent_matches.append((sp, m, target_name, reason))
                continue
            else:
                absent_species.append((sp, f"Sinonimia '{target_name}' verificada, pero la especie no fue incluida en el modelo BirdNET 10K"))
                continue

        # Ausente
        absent_species.append((sp, "Especie no incluida en el modelo BirdNET+ (10K pruned)"))

    return {
        "total_model_classes": len(model_all),
        "total_model_aves": len(model_aves),
        "total_arg_reference": len(arg_ref),
        "exact_matches": exact_matches,
        "equivalent_matches": equivalent_matches,
        "absent_species": absent_species,
        "labels_map_count": len(labels_map),
    }


def main():
    inv = build_inventory()
    tot_ref = inv["total_arg_reference"]
    n_exact = len(inv["exact_matches"])
    n_equiv = len(inv["equivalent_matches"])
    n_cov = n_exact + n_equiv
    n_abs = len(inv["absent_species"])

    print("=================================================================")
    print("INVENTARIO DE COBERTURA Y CORRESPONDENCIAS (SPEC 005 - T2)")
    print("=================================================================")
    print(f"Total clases en modelo species.json:        {inv['total_model_classes']}")
    print(f"Total clase Aves en modelo:                 {inv['total_model_aves']}")
    print(f"Total lista nacional argentina referencia:  {tot_ref}")
    print("-----------------------------------------------------------------")
    print(f"1. Coincidencias exactas directas:          {n_exact:4d} ({n_exact/tot_ref*100:5.1f}%)")
    print(f"2. Equivalencias taxonómicas verificadas:   {n_equiv:4d} ({n_equiv/tot_ref*100:5.1f}%)")
    print(f"   TOTAL COBERTURA EFECTIVA EN MODELO:      {n_cov:4d} ({n_cov/tot_ref*100:5.1f}%)")
    print(f"3. Especies argentinas ausentes del modelo: {n_abs:4d} ({n_abs/tot_ref*100:5.1f}%)")
    print(f"Especies preexistentes en labels_map.json:  {inv['labels_map_count']}")
    print("=================================================================")


if __name__ == "__main__":
    main()
