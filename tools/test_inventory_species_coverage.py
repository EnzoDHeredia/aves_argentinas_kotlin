import unittest
from pathlib import Path
from tools.inventory_species_coverage import build_inventory, TAXONOMIC_EQUIVALENTS


class InventorySpeciesCoverageTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.inventory = build_inventory()

    def test_model_counts_match_manifest(self):
        self.assertEqual(self.inventory["total_model_classes"], 9789)
        self.assertEqual(self.inventory["total_model_aves"], 8927)

    def test_reference_list_totals(self):
        self.assertEqual(self.inventory["total_arg_reference"], 1043)

    def test_coverage_categories_sum_to_total(self):
        exact = len(self.inventory["exact_matches"])
        equiv = len(self.inventory["equivalent_matches"])
        absent = len(self.inventory["absent_species"])
        self.assertEqual(exact + equiv + absent, 1043)
        self.assertEqual(exact, 882)
        self.assertEqual(equiv, 108)
        self.assertEqual(absent, 53)

    def test_labels_map_fully_covered(self):
        self.assertEqual(self.inventory["labels_map_count"], 46)

    def test_key_synonyms_are_mapped(self):
        # Spatula versicolor / Pato capuchino
        self.assertIn("anas versicolor", TAXONOMIC_EQUIVALENTS)
        self.assertEqual(TAXONOMIC_EQUIVALENTS["anas versicolor"][0], "Spatula versicolor")
        # Nannopterum brasilianum / Biguá
        self.assertIn("phalacrocorax brasilianus", TAXONOMIC_EQUIVALENTS)
        self.assertEqual(TAXONOMIC_EQUIVALENTS["phalacrocorax brasilianus"][0], "Nannopterum brasilianum")
        # Daptrius chimango / Chimango
        self.assertIn("milvago chimango", TAXONOMIC_EQUIVALENTS)
        self.assertEqual(TAXONOMIC_EQUIVALENTS["milvago chimango"][0], "Daptrius chimango")


if __name__ == "__main__":
    unittest.main()
