#!/usr/bin/env python3
"""Prepare a reproducible, non-production BirdNET graph for T4a evaluation."""
import argparse
from datetime import datetime, timezone
import json
import platform
from pathlib import Path
import sys

from prepare_birdnet_assets import file_info, validate_model


def prepare(source: Path, output: Path):
    validate_model(source)
    if output.exists():
        raise FileExistsError(f"Refusing to overwrite {output}")
    if platform.system() != "Windows" or platform.machine().lower() not in ("amd64", "x86_64"):
        raise RuntimeError("Approved preparation requires Windows x86-64")
    import onnxruntime as ort

    if ort.__version__ != "1.22.0":
        raise RuntimeError("Approved preparation requires ONNX Runtime 1.22.0")
    output.parent.mkdir(parents=True, exist_ok=True)
    options = ort.SessionOptions()
    options.execution_mode = ort.ExecutionMode.ORT_SEQUENTIAL
    options.intra_op_num_threads = 2
    options.graph_optimization_level = ort.GraphOptimizationLevel.ORT_DISABLE_ALL
    options.optimized_model_filepath = str(output.resolve())
    session = ort.InferenceSession(str(source.resolve()), options, providers=["CPUExecutionProvider"])
    if session.get_providers() != ["CPUExecutionProvider"]:
        raise RuntimeError("Unexpected execution provider")
    del session
    return {
        "source": file_info(source), "candidate": file_info(output),
        "source_commit": "2b4dbfe6b3ff4e5120b6b2abb1141bd318e1b846",
        "generated_at_utc": datetime.now(timezone.utc).isoformat(),
        "distribution": "onnxruntime", "runtime": ort.__version__,
        "python": platform.python_version(), "platform": platform.platform(),
        "machine": platform.machine(), "provider": "CPUExecutionProvider",
        "execution": "sequential", "intra_op_threads": 2,
        "graph_optimization": "ORT_DISABLE_ALL",
        "transformation": "ONNX Runtime session-prepared graph with precision casts",
    }


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("source", type=Path)
    parser.add_argument("output", type=Path)
    args = parser.parse_args()
    json.dump(prepare(args.source, args.output), sys.stdout, indent=2, sort_keys=True)
    print()
