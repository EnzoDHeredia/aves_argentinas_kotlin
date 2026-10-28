#!/usr/bin/env python3
"""Prepare the approved BirdNET package from local, commit-pinned official files."""

import argparse
import csv
import hashlib
import json
from pathlib import Path
import re
import shutil
import tempfile
import unittest

MODEL_NAME = "BirdNET+_V3.0-preview3.1_Global_10K-pruned_FP16.onnx"
LABELS_NAME = "BirdNET+_V3.0-preview3.1_Global_10K-pruned_Labels.csv"
MODEL_SIZE = 66_991_322
MODEL_SHA256 = "306e74f3edc5b95acc6e87ea7f3f1c701fdc0b18ec868b7615208cd6fc2f9457"
DERIVED_NAME = "BirdNET+_V3.0-preview3.1_Global_10K-pruned_FP16_CPU.onnx"
DERIVED_SIZE = 65_830_813
DERIVED_SHA256 = "cc696820bc4882f2469a2cd0071b299aa400858aad7843e1f65879867bf5eaa5"
SPECIES_COUNT = 9789
ERRATA_SOURCE_SHA256 = {
    "labels": "b2ba8b1383fc6a03ae417f735afb9a5d02cd623e49d82ab147d2b6aaaeea7de9",
    "taxonomy": "c1052902a6a003f4040548644bbbc02e8dd24a42425585842b97900ac4e3ad16",
}
# Erratas verificadas contra taxonomy.csv del mismo commit y la API oficial Cornell.
CLASS_ERRATA = {
    410: dict(id=767, scientific_name="Anarhynchus montanus", label_class="Insecta",
              label_order="Diptera", taxonomy_id="BN00678", **{"class": "Aves"},
              evidence_sha256="b0234c1c1b8d0e954eba0c5711344fabd30365bb7f5ab055fbafe476984e062d"),
    411: dict(id=768, scientific_name="Anarhynchus pallidus", label_class="Insecta",
              label_order="Diptera", taxonomy_id="BN00681", **{"class": "Aves"},
              evidence_sha256="371ff69f09a7ef7e49d1e2b81a3974c5169fe3e1021c9014673a95d555a330e2"),
    2510: dict(id=4442, scientific_name="Corvus celebensis", label_class="Mammalia",
               label_order="Artiodactyla", taxonomy_id="BN03724", **{"class": "Aves"},
               evidence_sha256="99b4c336963c5741d7e4e605364cbe269842ea1d105d5e52998933759c571bc2"),
    5592: dict(id=10203, scientific_name="Microtarsus urostictus", label_class="Insecta",
               label_order="Hemiptera", taxonomy_id="BN08926", **{"class": "Aves"},
               evidence_sha256="11eb5d2517542ddae24c6eef91176bf8c5606322b8e04a6f443f7c1a382652a7"),
}


def resolve_class(label, taxon, source_hashes):
    group = label["class"].strip()
    if group == taxon["taxon_group"].strip():
        return group
    erratum = CLASS_ERRATA.get(int(label["idx"]))
    if (erratum is None or source_hashes != ERRATA_SOURCE_SHA256
            or (int(label["id"]), label["sci_name"].strip(), group, label["order"].strip())
            != (erratum["id"], erratum["scientific_name"], erratum["label_class"], erratum["label_order"])
            or (taxon["scientific_name"], taxon["canonical_scientific_name"],
                taxon["birdnet_id"], taxon["taxon_group"])
            != (erratum["scientific_name"], erratum["scientific_name"],
                erratum["taxonomy_id"], erratum["class"])):
        raise ValueError(f"Unreviewed taxonomy class discrepancy: {label['sci_name']}")
    return erratum["class"]


def validate_model(path):
    with path.open("rb") as stream:
        if stream.read(128).startswith(b"version https://git-lfs.github.com/spec/v1"):
            raise ValueError("LFS pointer is not a model")
    if path.stat().st_size != MODEL_SIZE:
        raise ValueError(f"Model size must be {MODEL_SIZE} bytes")
    if file_info(path)["sha256"] != MODEL_SHA256:
        raise ValueError("Model SHA-256 differs from the approved binary")


def validate_derived_model(path):
    if file_info(path) != dict(bytes=DERIVED_SIZE, sha256=DERIVED_SHA256):
        raise ValueError("Derived model size or SHA-256 differs from the verified candidate")


def build_species(labels_path, taxonomy_path):
    with labels_path.open(encoding="utf-8-sig", newline="") as stream:
        labels = list(csv.DictReader(stream, delimiter=";"))
    with taxonomy_path.open(encoding="utf-8-sig", newline="") as stream:
        taxonomy_rows = list(csv.DictReader(stream))
    if len(labels) != SPECIES_COUNT:
        raise ValueError(f"Expected {SPECIES_COUNT} labels")
    source_hashes = dict(labels=file_info(labels_path)["sha256"],
                         taxonomy=file_info(taxonomy_path)["sha256"])
    taxonomy = {}
    for row in taxonomy_rows:
        name = row["scientific_name"].strip()
        if not name or name in taxonomy:
            raise ValueError(f"Ambiguous taxonomy correspondence: {name}")
        taxonomy[name] = row
    species, seen_ids, seen_names = [], set(), set()
    for position, label in enumerate(labels):
        idx, taxon_id = int(label["idx"]), int(label["id"])
        name, group = label["sci_name"].strip(), label["class"].strip()
        if idx != position or taxon_id < 0 or taxon_id in seen_ids or name in seen_names:
            raise ValueError(f"Shifted/duplicate index or identity at position {position}")
        row = taxonomy.get(name)
        if not name or not group or row is None:
            raise ValueError(f"Missing/incompatible taxonomy correspondence: {name}")
        group = resolve_class(label, row, source_hashes)
        canonical = row["canonical_scientific_name"].strip()
        if not canonical:
            raise ValueError(f"Missing canonical scientific name: {name}")
        seen_ids.add(taxon_id)
        seen_names.add(name)
        species.append(dict(idx=idx, id=taxon_id, scientific_name=name,
                            canonical_scientific_name=canonical,
                            **{"class": group},
                            common_name_es=row["common_name_es"].strip() or None))
    return species


def json_bytes(value):
    return (json.dumps(value, ensure_ascii=False, sort_keys=True, indent=2) + "\n").encode("utf-8")


def file_info(path):
    digest = hashlib.sha256()
    with path.open("rb") as stream:
        for chunk in iter(lambda: stream.read(1024 * 1024), b""):
            digest.update(chunk)
    return dict(bytes=path.stat().st_size, sha256=digest.hexdigest())


def prepare(args):
    if not re.fullmatch(r"[0-9a-f]{40}", args.source_commit):
        raise ValueError("Source commit must be a full Git commit hash")
    validate_model(args.model)
    validate_derived_model(args.derived)
    species = build_species(args.labels, args.taxonomy)
    if "Apache License" not in args.model_license.read_text(encoding="utf-8"):
        raise ValueError("Missing official model license")
    if "MIT License" not in args.source_license.read_text(encoding="utf-8"):
        raise ValueError("Missing source repository license; review metadata licensing")
    raw = f"https://raw.githubusercontent.com/birdnet-team/birdnet-live-app/{args.source_commit}"
    source_paths = {
        "model": (args.model, f"assets/models/{MODEL_NAME}"),
        "labels": (args.labels, f"assets/models/{LABELS_NAME}"),
        "taxonomy": (args.taxonomy, "assets/models/taxonomy.csv"),
        "model_license": (args.model_license, "MODEL_LICENSE"),
        "source_license": (args.source_license, "LICENSE"),
    }
    sources = {}
    for key, (path, upstream_path) in source_paths.items():
        url = f"{raw}/{upstream_path}"
        if key == "model":
            url = url.replace("raw.githubusercontent.com", "media.githubusercontent.com/media")
        sources[key] = dict(url=url.replace("+", "%2B"), path=upstream_path, **file_info(path))
    applied_errata = []
    if {key: sources[key]["sha256"] for key in ERRATA_SOURCE_SHA256} == ERRATA_SOURCE_SHA256:
        for idx, erratum in CLASS_ERRATA.items():
            name = erratum["scientific_name"].replace(" ", "%20")
            applied_errata.append(dict(idx=idx, **erratum,
                taxonomy_source=sources["taxonomy"]["url"],
                evidence_url=f"https://birdnet.cornell.edu/taxonomy/api/species/{name}"
                             "?fields=scientific_name,birdnet_id,taxon_group,ebird_code,ncbi_id",
                evidence_taxonomy_version="v0.3-Jul2026"))
    attribution = (
        "Reconocimiento con BirdNET\n"
        "BirdNET-Team (Cornell Lab of Ornithology / Chemnitz University of Technology).\n"
        f"Modelo fuente: {MODEL_NAME}\nCommit de origen: {args.source_commit}\n"
        "Pesos: Apache-2.0; texto oficial en MODEL_LICENSE.\n"
        "Etiquetas y campos de taxonomía: repositorio BirdNET Live; licencia raíz MIT\n"
        "conservada en SOURCE_LICENSE. Se extrajeron solo nombres, identidad y clase;\n"
        "no se incorporaron fotografías, descripciones ni sus licencias.\n"
        "Las traducciones españolas ausentes son null; no se tradujeron ni inventaron.\n"
        "idx es la posición de salida; id es un identificador distinto.\n"
        f"Clases corregidas con evidencia oficial: {len(applied_errata)}; detalle en manifest.json.\n"
        "El modelo empaquetado es un grafo derivado del original, preparado con\n"
        "ONNX Runtime 1.22.0 CPU, optimización desactivada y conversiones de precisión.\n"
        "El CSV de etiquetas se conserva íntegro, sin modificar.\n"
        "Runtime: Microsoft ONNX Runtime Android 1.22.0 (MIT); conservar también\n"
        "ONNX_LICENSE y ONNX_ThirdPartyNotices.txt adquiridos de la etiqueta v1.22.0.\n"
        "Fuente y huellas de cada entrada en manifest.json. Sin descarga en Android.\n"
    )
    args.output.mkdir(parents=True, exist_ok=True)
    if (args.output / MODEL_NAME).exists():
        raise ValueError("Output contains the original model; use an empty package directory")
    for source, name in ((args.derived, DERIVED_NAME), (args.labels, LABELS_NAME),
                         (args.model_license, "MODEL_LICENSE"), (args.source_license, "SOURCE_LICENSE")):
        destination = args.output / name
        if source.resolve() != destination.resolve():
            shutil.copyfile(source, destination)
    (args.output / "species.json").write_bytes(json_bytes(species))
    (args.output / "ATTRIBUTION.txt").write_bytes(attribution.encode("utf-8"))
    manifest = dict(schema_version=1, source_commit=args.source_commit,
                    model_file=DERIVED_NAME, labels_file=LABELS_NAME,
                    derivation=dict(source_sha256=MODEL_SHA256, runtime="onnxruntime:1.22.0",
                                    provider="CPUExecutionProvider", execution="sequential",
                                    intra_op_threads=2, graph_optimization="ORT_DISABLE_ALL",
                                    platform="Windows x86-64", python="3.10.11",
                                    generated_at_utc="2026-10-27T02:24:32.873525+00:00",
                                    preparer="tools/prepare_birdnet_t4.py",
                                    command="python tools/prepare_birdnet_t4.py <official-model> <output>",
                                    runtime_wheel_sha256="20bca6495d06925631e201f2b257cc37086752e8fe7b6c83a67c6509f4759bc9"),
                    species_count=SPECIES_COUNT, sources=sources,
                    runtime="com.microsoft.onnxruntime:onnxruntime-android:1.22.0",
                    class_corrections=applied_errata,
                    missing_spanish_indices=[r["idx"] for r in species if r["common_name_es"] is None],
                    files={name: file_info(args.output / name) for name in
                           (DERIVED_NAME, LABELS_NAME, "MODEL_LICENSE", "SOURCE_LICENSE",
                            "species.json", "ATTRIBUTION.txt")})
    (args.output / "manifest.json").write_bytes(json_bytes(manifest))
    print(f"Prepared {len(species)} aligned entries; "
          f"missing Spanish names: {len(manifest['missing_spanish_indices'])}")
    print(f"Model SHA-256: {MODEL_SHA256}")
    print(f"species.json SHA-256: {manifest['files']['species.json']['sha256']}")


class PreparationTest(unittest.TestCase):
    def test_only_four_verified_source_errata_become_birds(self):
        source_hashes = {
            "labels": "b2ba8b1383fc6a03ae417f735afb9a5d02cd623e49d82ab147d2b6aaaeea7de9",
            "taxonomy": "c1052902a6a003f4040548644bbbc02e8dd24a42425585842b97900ac4e3ad16",
        }
        cases = [
            (410, 767, "Anarhynchus montanus", "Insecta", "Diptera", "BN00678"),
            (411, 768, "Anarhynchus pallidus", "Insecta", "Diptera", "BN00681"),
            (2510, 4442, "Corvus celebensis", "Mammalia", "Artiodactyla", "BN03724"),
            (5592, 10203, "Microtarsus urostictus", "Insecta", "Hemiptera", "BN08926"),
        ]
        for idx, taxon_id, name, group, order, birdnet_id in cases:
            with self.subTest(idx=idx):
                label = dict(idx=str(idx), id=str(taxon_id), sci_name=name,
                             **{"class": group, "order": order})
                taxon = dict(scientific_name=name, canonical_scientific_name=name,
                             taxon_group="Aves", birdnet_id=birdnet_id)
                self.assertEqual(resolve_class(label, taxon, source_hashes), "Aves")
                self.assertEqual(label["class"], group)
                for field, changed in (("idx", "999"), ("id", "999"),
                                       ("sci_name", "Unknown bird"),
                                       ("class", "Amphibia"), ("order", "Unknown")):
                    with self.assertRaises(ValueError):
                        resolve_class({**label, field: changed}, taxon, source_hashes)
                for field, changed in (("scientific_name", "Unknown bird"),
                                       ("canonical_scientific_name", "Unknown bird"),
                                       ("birdnet_id", "BN99999"), ("taxon_group", "Amphibia")):
                    with self.assertRaises(ValueError):
                        resolve_class(label, {**taxon, field: changed}, source_hashes)
                for source in source_hashes:
                    with self.assertRaises(ValueError):
                        resolve_class(label, taxon, {**source_hashes, source: "0" * 64})

    def test_model_rejects_lfs_wrong_size_and_wrong_hash(self):
        with tempfile.TemporaryDirectory() as folder:
            path = Path(folder) / "model.onnx"
            path.write_text("version https://git-lfs.github.com/spec/v1\n")
            with self.assertRaisesRegex(ValueError, "LFS"):
                validate_model(path)
            path.write_bytes(b"invalid model")
            with self.assertRaisesRegex(ValueError, "size"):
                validate_model(path)
            with path.open("wb") as stream:
                stream.seek(MODEL_SIZE - 1)
                stream.write(b"\0")
            with self.assertRaisesRegex(ValueError, "SHA-256"):
                validate_model(path)

    def test_positions_ids_missing_translation_and_invalid_catalogues(self):
        labels = [dict(idx=str(i), id=str(i + 100), sci_name=f"Bird {i}",
                       com_name=f"English {i}", **{"class": "Aves", "order": "Test"})
                  for i in range(SPECIES_COUNT)]
        taxonomy = [dict(scientific_name=f"Bird {i}", canonical_scientific_name=f"Bird {i}",
                         taxon_group="Aves", common_name_es="Ave" if i == 0 else "")
                    for i in range(SPECIES_COUNT)]
        with tempfile.TemporaryDirectory() as folder:
            labels_path, taxonomy_path = Path(folder) / "labels.csv", Path(folder) / "taxonomy.csv"

            def write_rows(path, rows, delimiter):
                with path.open("w", encoding="utf-8", newline="") as stream:
                    writer = csv.DictWriter(stream, fieldnames=rows[0], delimiter=delimiter)
                    writer.writeheader()
                    writer.writerows(rows)

            def load(label_rows, taxonomy_rows):
                write_rows(labels_path, label_rows, ";")
                write_rows(taxonomy_path, taxonomy_rows, ",")
                return build_species(labels_path, taxonomy_path)

            species = load(labels, taxonomy)
            self.assertEqual((species[0]["idx"], species[0]["id"]), (0, 100))
            self.assertEqual(species[0]["common_name_es"], "Ave")
            self.assertIsNone(species[-1]["common_name_es"])
            self.assertEqual(species[-1]["idx"], 9788)
            self.assertEqual(json_bytes(species), json_bytes(load(labels, taxonomy)))
            swapped = [labels[1], labels[0], *labels[2:]]
            duplicate_idx = [labels[0], labels[0], *labels[2:]]
            duplicate_id = [labels[0], {**labels[1], "id": "100"}, *labels[2:]]
            for invalid in (swapped, duplicate_idx, duplicate_id, labels[:-1]):
                with self.subTest(indices=invalid[1]["idx"], length=len(invalid)):
                    with self.assertRaises(ValueError):
                        load(invalid, taxonomy)
            for invalid in (taxonomy + [{**taxonomy[0], "common_name_es": "Otra ave"}],
                            taxonomy[1:],
                            [{**taxonomy[0], "taxon_group": "Mammalia"}, *taxonomy[1:]]):
                with self.assertRaises(ValueError):
                    load(labels, invalid)


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--self-test", action="store_true")
    for name in ("model", "derived", "labels", "taxonomy", "model-license", "source-license", "output"):
        parser.add_argument(f"--{name}", type=Path)
    parser.add_argument("--source-commit")
    args = parser.parse_args()
    if args.self_test:
        unittest.main(argv=[__file__], verbosity=2)
    else:
        if any(value is None for name, value in vars(args).items() if name != "self_test"):
            parser.error("All input paths, --source-commit and --output are required")
        try:
            prepare(args)
        except (ValueError, KeyError, OSError, csv.Error) as error:
            parser.exit(1, f"Preparation failed: {error}\n")
