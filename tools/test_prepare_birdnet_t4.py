import tempfile
import unittest
from pathlib import Path

from prepare_birdnet_t4 import prepare
from prepare_birdnet_assets import MODEL_SIZE, validate_derived_model


class PrepareBirdNetT4Test(unittest.TestCase):
    def test_packaging_rejects_an_unverified_derivative(self):
        with tempfile.TemporaryDirectory() as directory:
            candidate = Path(directory) / "candidate.onnx"
            candidate.write_bytes(b"wrong model")
            with self.assertRaises(ValueError):
                validate_derived_model(candidate)

    def test_valid_source_does_not_overwrite_existing_output(self):
        source = Path(__file__).resolve().parents[1] / "app/build/birdnet-verification/model.onnx"
        if not source.exists():
            self.skipTest("verified original model not available under build/")
        with tempfile.TemporaryDirectory() as directory:
            output = Path(directory) / "candidate.onnx"
            output.write_bytes(b"keep")
            with self.assertRaises(FileExistsError):
                prepare(source, output)
            self.assertEqual(b"keep", output.read_bytes())

    def test_rejects_wrong_size_without_creating_output(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            source = root / "wrong.onnx"
            source.write_bytes(b"not an onnx model")
            output = root / "candidate.onnx"
            with self.assertRaises(ValueError):
                prepare(source, output)
            self.assertFalse(output.exists())

    def test_rejects_wrong_hash_without_overwriting_output(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            source = root / "wrong.onnx"
            with source.open("wb") as stream:
                stream.truncate(MODEL_SIZE)
            output = root / "candidate.onnx"
            output.write_bytes(b"keep")
            with self.assertRaises(ValueError):
                prepare(source, output)
            self.assertEqual(b"keep", output.read_bytes())


if __name__ == "__main__":
    unittest.main()
