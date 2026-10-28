#!/usr/bin/env python3
"""Preparación del catálogo distribuible de nombres argentinos para sonido (Spec 005 - T3).

Genera `app/src/main/assets/birdnet/sound_names_ar.json` a partir del inventario de
cobertura y fuentes verificadas (T1 y T2), aplicando la prioridad bonaerense (RF-2)
sobre nombres nacionales (RF-3) y validando la integridad referencial contra
`species.json` sin modificar el modelo, las etiquetas ni `labels_map.json`.
"""

import hashlib
import json
import re
import sys
from pathlib import Path

if sys.stdout and hasattr(sys.stdout, "reconfigure"):
    try:
        sys.stdout.reconfigure(encoding="utf-8", errors="replace")
    except Exception:
        pass

ROOT = Path(__file__).resolve().parent.parent
sys.path.insert(0, str(ROOT))

from tools.inventory_species_coverage import (
    TAXONOMIC_EQUIVALENTS,
    load_argentine_reference,
    load_model_species,
)

# Hashes de integridad de activos preexistentes que NO deben modificarse
EXPECTED_HASHES = {
    "model_onnx": (
        ROOT / "app/src/main/assets/birdnet/BirdNET+_V3.0-preview3.1_Global_10K-pruned_FP16_CPU.onnx",
        "cc696820bc4882f2469a2cd0071b299aa400858aad7843e1f65879867bf5eaa5",
    ),
    "labels_csv": (
        ROOT / "app/src/main/assets/birdnet/BirdNET+_V3.0-preview3.1_Global_10K-pruned_Labels.csv",
        "b2ba8b1383fc6a03ae417f735afb9a5d02cd623e49d82ab147d2b6aaaeea7de9",
    ),
    "species_json": (
        ROOT / "app/src/main/assets/birdnet/species.json",
        "5e3598bdbc08a0684ba4edc47e7760c44515ce6febb6e5cfd0b0668f778244b7",
    ),
    "labels_map_json": (
        ROOT / "app/src/main/assets/labels_map.json",
        "ef23d3a6006c98f8ad54af80d1b19361b51aaa0846e8dc351c0133f7f974db25",
    ),
}

# Nombres bonaerenses con evidencia documental (Darrieu et al. 2013 / labels_map.json)
# Prioridad 1 (RF-2) frente a denominaciones nacionales generales
BUENOS_AIRES_PRIORITY = {
    "podiceps_major": "Macá grande",         # En lista nacional figura como Huala (nombre mapuche)
    "netta_peposaca": "Pato picazo",         # Grafía tradicional bonaerense (vs Pato picaso)
    "furnarius_rufus": "Hornero",            # Denominación simplificada bonaerense tradicional
    "pitangus_sulphuratus": "Benteveo común", # Fonética pampeana consagrada por el uso
    "myiopsitta_monachus": "Cotorra",        # Uso tradicional pampeano frente a Cotorra común
    "troglodytes_aedon": "Ratona común",     # Tradición ornitológica argentina frente a modismos
    "troglodytes_musculus": "Ratona común",  # Taxón biológico pampeano residente escindido en Clements
    "vanellus_chilensis": "Tero común",      # Tradición rioplatense unánime (rechazo de Avefría)
    "nannopterum_brasilianum": "Biguá",      # Consagrado en labels_map y Darrieu (vs Cormorán biguá)
    "spatula_versicolor": "Pato capuchino",  # Consagrado en labels_map y Darrieu (vs Cerceta capuchina)
    "spatula_cyanoptera": "Pato colorado",   # Consagrado en labels_map y Darrieu (vs Cerceta colorada)
    "spatula_platalea": "Pato cuchara",      # Consagrado en labels_map y Darrieu (vs Cuchara argentino)
    "mareca_sibilatrix": "Pato overo",       # Consagrado en labels_map y Darrieu (vs Silbón overo)
    "rhea_americana": "Ñandú",               # Simplificación tradicional bonaerense
    "passer_domesticus": "Gorrión",          # Uso popular unánime frente a Gorrión común
    "daptrius_chimango": "Chimango",         # Rechazo del prefijo Caracara
    "caracara_plancus": "Carancho",          # Rechazo del prefijo Caracara
    "athene_cunicularia": "Lechucita vizcachera", # Rechazo de Mochuelo de madriguera
    "mimus_saturninus": "Calandria grande",  # Rechazo de Sinsonte
    "sicalis_flaveola": "Jilguero dorado",   # Rechazo de Chirigüe azafranado
    "tyrannus_melancholicus": "Suirirí real", # Rechazo de Tirano melancólico
    "colaptes_campestris": "Carpintero campestre",
    "zonotrichia_capensis": "Chingolo",      # Uso tradicional simplificado
    "zenaida_auriculata": "Torcaza",         # Rechazo del prefijo Zenaida
    "molothrus_bonariensis": "Tordo renegrido",
    "guira_guira": "Pirincho",
}


def clean_vulgar_name(name: str) -> str:
    """Limpia anotaciones de notas al pie, endemismos o marcadores poblacionales."""
    cleaned = re.sub(r"\s*\(\s*[Ee]\s*\)\s*$", "", name)
    cleaned = re.sub(r"\s*\(\s*(?:continental|malvinas)\s*\)\*?$", "", cleaned, flags=re.IGNORECASE)
    cleaned = cleaned.rstrip("*").strip()
    return cleaned


def verify_unmodified_assets():
    """Verifica que los activos preexistentes permanezcan intactos."""
    for name, (path, expected_hash) in EXPECTED_HASHES.items():
        if not path.exists():
            raise FileNotFoundError(f"Activo preexistente no encontrado: {path}")
        actual_hash = hashlib.sha256(path.read_bytes()).hexdigest()
        if actual_hash != expected_hash:
            raise ValueError(
                f"El activo {name} ({path.name}) fue modificado indebidamente!\n"
                f"  Esperado: {expected_hash}\n"
                f"  Actual:   {actual_hash}"
            )


def build_sound_names_catalog():
    """Construye el diccionario de nombres argentinos para sonido."""
    species_path = ROOT / "app/src/main/assets/birdnet/species.json"
    cat_path = ROOT / "app/build/t4/categorizacion.txt"
    labels_map_path = ROOT / "app/src/main/assets/labels_map.json"

    _, aves, canon_map, sci_map = load_model_species(species_path)
    arg_ref = load_argentine_reference(cat_path)
    labels_map = json.loads(labels_map_path.read_text(encoding="utf-8"))

    catalog = {}
    sources_summary = {
        "buenos_aires_priority": 0,
        "national_reference_exact": 0,
        "national_reference_taxonomic_equivalent": 0,
        "clements_subspecies_split": 0,
    }

    # 1. Base nacional: Coincidencias directas
    for id_num, sp in sorted(arg_ref.items()):
        vulgar = clean_vulgar_name(sp["vulgar"])
        sci_lower = sp["scientific_name"].strip().lower()
        m = canon_map.get(sci_lower) or sci_map.get(sci_lower)
        if m:
            key = m["canonical_scientific_name"].strip().lower().replace(" ", "_")
            catalog[key] = vulgar

    # 2. Base nacional: Equivalencias taxonómicas verificadas
    for id_num, sp in sorted(arg_ref.items()):
        vulgar = clean_vulgar_name(sp["vulgar"])
        sci_lower = sp["scientific_name"].strip().lower()
        if sci_lower in TAXONOMIC_EQUIVALENTS:
            target, _ = TAXONOMIC_EQUIVALENTS[sci_lower]
            m = canon_map.get(target.lower()) or sci_map.get(target.lower())
            if m:
                key = m["canonical_scientific_name"].strip().lower().replace(" ", "_")
                catalog[key] = vulgar

    # 3. Tratamiento especial de Troglodytes musculus (Clements split / Ratona común bonaerense)
    if "troglodytes_musculus" not in catalog and "troglodytes musculus" in canon_map:
        catalog["troglodytes_musculus"] = "Ratona común"

    # 4. Aplicar prioridad de Buenos Aires (RF-2)
    for key, ba_name in BUENOS_AIRES_PRIORITY.items():
        if key in catalog:
            catalog[key] = ba_name

    # Validaciones rigurosas
    for k, v in catalog.items():
        if not k or not k.strip():
            raise ValueError("Se detectó una clave vacía en el catálogo!")
        if not v or not v.strip():
            raise ValueError(f"Se detectó un nombre común vacío para la clave '{k}'!")
        if not re.match(r"^[a-z]+(?:_[a-z]+)+$", k):
            raise ValueError(f"Formato de clave inválido (debe ser snake_case): '{k}'")
        # Asegurar correspondencia estricta con el modelo
        canonical_space = k.replace("_", " ")
        if canonical_space not in canon_map:
            raise ValueError(f"La clave '{k}' no corresponde a una clase Aves de species.json!")

    # Verificar que las 46 especies de labels_map.json estén 100% cubiertas
    for local_key in labels_map:
        target_key = local_key
        sci_space = local_key.replace("_", " ")
        if sci_space in TAXONOMIC_EQUIVALENTS:
            target_key = TAXONOMIC_EQUIVALENTS[sci_space][0].lower().replace(" ", "_")
        if target_key not in catalog:
            raise ValueError(f"La especie local '{local_key}' ({target_key}) no está presente en el catálogo!")

    return catalog


def main():
    print("=================================================================")
    print("PREPARACIÓN DEL CATÁLOGO DISTRIBUIBLE DE SONIDO (SPEC 005 - T3)")
    print("=================================================================")

    # 1. Verificar integridad de activos preexistentes
    print("1. Verificando que activos preexistentes no hayan sido alterados...")
    verify_unmodified_assets()
    print("   [OK] Modelo ONNX, Etiquetas CSV, species.json y labels_map.json íntegros.")

    # 2. Construir catálogo
    print("2. Construyendo catálogo con prioridad bonaerense y base nacional...")
    catalog = build_sound_names_catalog()
    output_path = ROOT / "app/src/main/assets/birdnet/sound_names_ar.json"

    # 3. Guardar catálogo ordenado por clave de forma determinista
    sorted_catalog = {k: catalog[k] for k in sorted(catalog.keys())}
    output_json = json.dumps(sorted_catalog, ensure_ascii=False, indent=2) + "\n"
    output_path.write_text(output_json, encoding="utf-8")

    file_bytes = output_path.stat().st_size
    file_sha256 = hashlib.sha256(output_path.read_bytes()).hexdigest()

    print(f"   [OK] Catálogo generado en: {output_path.relative_to(ROOT)}")
    print(f"   Total especies cubiertas en el catálogo: {len(sorted_catalog)}")
    print(f"   Tamaño del archivo:                      {file_bytes} bytes")
    print(f"   Huella SHA-256:                          {file_sha256}")
    print("=================================================================")


if __name__ == "__main__":
    main()
