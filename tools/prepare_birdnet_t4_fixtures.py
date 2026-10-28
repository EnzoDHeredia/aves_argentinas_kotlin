"""Generate synthetic T4 inputs and independent ONNX Runtime 1.22 references."""
import hashlib
import argparse
import json
import math
from pathlib import Path

import numpy as np
import onnxruntime as ort

root = Path(__file__).resolve().parents[1]
parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument("--model", type=Path, required=True, help="Verified original official model")
parser.add_argument("--output", type=Path, required=True, help="New directory for references")
args = parser.parse_args()
model, target = args.model, args.output
assert hashlib.sha256(model.read_bytes()).hexdigest() == "306e74f3edc5b95acc6e87ea7f3f1c701fdc0b18ec868b7615208cd6fc2f9457"
assert ort.__version__ == "1.22.0"
target.mkdir(parents=True, exist_ok=False)
options = ort.SessionOptions()
options.execution_mode = ort.ExecutionMode.ORT_SEQUENTIAL
options.intra_op_num_threads = 2
session = ort.InferenceSession(str(model), options, providers=["CPUExecutionProvider"])
manifest = {"runtime": ort.__version__, "model_sha256": hashlib.sha256(model.read_bytes()).hexdigest(), "fixtures": []}
for number, frequencies in enumerate(((440, 1200), (750, 2700), (1800, 5100)), 1):
    samples = np.array([0.2 * math.sin(2 * math.pi * frequencies[0] * i / 32000) +
                        0.1 * math.sin(2 * math.pi * frequencies[1] * i / 32000)
                        for i in range(96000)], dtype="<f4")
    scores = session.run(["predictions"], {"input": samples.reshape(1, -1)})[0].astype("<f4")
    assert scores.shape == (1, 9789) and np.isfinite(scores).all() and ((scores >= 0) & (scores <= 1)).all()
    files = {}
    for name, data in ((f"signal-{number}.f32", samples.tobytes()), (f"reference-{number}.f32", scores.tobytes())):
        (target / name).write_bytes(data)
        files[name] = hashlib.sha256(data).hexdigest()
    manifest["fixtures"].append({"frequencies_hz": frequencies, "sample_rate_hz": 32000, "files": files})
(target / "manifest.json").write_text(json.dumps(manifest, indent=2) + "\n", encoding="utf-8")
