#!/usr/bin/env python3
"""Pruebas de integridad del catálogo distribuible de nombres para sonido (Spec 005 - T3)."""

import hashlib
import json
import re
import sys
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
sys.path.insert(0, str(ROOT))

from tools.inventory_species_coverage import TAXONOMIC_EQUIVALENTS, load_model_species
from tools.prepare_sound_names_catalog import EXPECTED_HASHES


class SoundNamesCatalogTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.catalog_path = ROOT / "app/src/main/assets/birdnet/sound_names_ar.json"
        cls.species_path = ROOT / "app/src/main/assets/birdnet/species.json"
        cls.labels_map_path = ROOT / "app/src/main/assets/labels_map.json"

        cls.catalog = json.loads(cls.catalog_path.read_text(encoding="utf-8"))
        _, cls.model_aves, cls.canon_map, cls.sci_map = load_model_species(cls.species_path)
        cls.labels_map = json.loads(cls.labels_map_path.read_text(encoding="utf-8"))

    def test_catalog_file_exists_and_is_non_empty(self):
        self.assertTrue(self.catalog_path.exists())
        self.assertGreater(self.catalog_path.stat().st_size, 40000)

    def test_total_count_matches_expected_coverage(self):
        # 988 especies reconocibles del modelo cubiertas con nombres argentinos verificados
        self.assertEqual(len(self.catalog), 988)

    def test_keys_are_unique_non_empty_and_snake_case(self):
        seen = set()
        for key in self.catalog.keys():
            self.assertTrue(bool(key.strip()), "Clave vacía encontrada")
            self.assertNotIn(key, seen, f"Clave duplicada: {key}")
            seen.add(key)
            self.assertTrue(
                bool(re.match(r"^[a-z]+(?:_[a-z]+)+$", key)),
                f"La clave '{key}' no sigue el formato snake_case",
            )

    def test_values_are_non_empty_and_clean(self):
        for key, value in self.catalog.items():
            self.assertIsInstance(value, str)
            self.assertTrue(bool(value.strip()), f"Nombre vacío para clave: {key}")
            # No deben contener notas de pie (*), indicadores de endemismo ( e ) ni población
            self.assertFalse(value.endswith("*"), f"Nombre con asterisco sin limpiar: {value}")
            self.assertFalse("( e )" in value or "(e)" in value, f"Nombre con marca de endemismo: {value}")
            self.assertFalse("(continental)" in value.lower(), f"Nombre con marca poblacional: {value}")
            self.assertFalse("(malvinas)" in value.lower(), f"Nombre con marca poblacional: {value}")

    def test_all_keys_correspond_to_valid_aves_in_species_json(self):
        for key in self.catalog.keys():
            canonical_space = key.replace("_", " ")
            self.assertIn(
                canonical_space,
                self.canon_map,
                f"La clave '{key}' no existe en species.json como canonical_scientific_name",
            )
            model_entry = self.canon_map[canonical_space]
            self.assertEqual(model_entry.get("class"), "Aves")

    def test_labels_map_all_46_species_are_covered(self):
        self.assertEqual(len(self.labels_map), 46)
        for local_key in self.labels_map.keys():
            target_key = local_key
            sci_space = local_key.replace("_", " ")
            if sci_space in TAXONOMIC_EQUIVALENTS:
                target_key = TAXONOMIC_EQUIVALENTS[sci_space][0].lower().replace(" ", "_")
            self.assertIn(
                target_key,
                self.catalog,
                f"Especie preexistente en labels_map '{local_key}' no cubierta en catálogo",
            )

    def test_buenos_aires_priority_and_spec_examples(self):
        spec_examples = {
            "vanellus_chilensis": "Tero común",
            "furnarius_rufus": "Hornero",
            "turdus_rufiventris": "Zorzal colorado",
            "pitangus_sulphuratus": "Benteveo común",
            "guira_guira": "Pirincho",
            "zonotrichia_capensis": "Chingolo",
            "zenaida_auriculata": "Torcaza",
            "daptrius_chimango": "Chimango",
            "caracara_plancus": "Carancho",
            "troglodytes_aedon": "Ratona común",
            "troglodytes_musculus": "Ratona común",
            "athene_cunicularia": "Lechucita vizcachera",
            "mimus_saturninus": "Calandria grande",
            "myiopsitta_monachus": "Cotorra",
            "sicalis_flaveola": "Jilguero dorado",
            "tyrannus_melancholicus": "Suirirí real",
            "nannopterum_brasilianum": "Biguá",
            "spatula_versicolor": "Pato capuchino",
            "spatula_cyanoptera": "Pato colorado",
            "spatula_platalea": "Pato cuchara",
            "mareca_sibilatrix": "Pato overo",
            "netta_peposaca": "Pato picazo",
            "rhea_americana": "Ñandú",
            "molothrus_bonariensis": "Tordo renegrido",
            "passer_domesticus": "Gorrión",
            "colaptes_campestris": "Carpintero campestre",
            "podiceps_major": "Macá grande",
        }
        for key, expected_name in spec_examples.items():
            self.assertIn(key, self.catalog)
            self.assertEqual(
                self.catalog[key],
                expected_name,
                f"Discrepancia en {key}: esperado '{expected_name}', obtenido '{self.catalog[key]}'",
            )

    def test_unmodified_assets_and_labels_map_integrity(self):
        for name, (path, expected_hash) in EXPECTED_HASHES.items():
            actual_hash = hashlib.sha256(path.read_bytes()).hexdigest()
            self.assertEqual(
                actual_hash,
                expected_hash,
                f"El activo {name} ({path.name}) fue modificado indebidamente!",
            )


if __name__ == "__main__":
    unittest.main()
