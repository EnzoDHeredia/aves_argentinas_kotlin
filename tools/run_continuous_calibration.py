import json
import os
import numpy as np
import onnxruntime as ort

MODEL_PATH = "app/src/main/assets/birdnet/BirdNET+_V3.0-preview3.1_Global_10K-pruned_FP16_CPU.onnx"
SPECIES_PATH = "app/src/main/assets/birdnet/species.json"
MANIFEST_PATH = "app/src/androidTest/assets/birdnet/corpus_manifest.json"
CORPUS_DIR = "app/src/androidTest/assets/birdnet/corpus"

with open(SPECIES_PATH, "r", encoding="utf-8") as f:
    species_list = json.load(f)

with open(MANIFEST_PATH, "r", encoding="utf-8") as f:
    manifest = json.load(f)

def canonical_key(name):
    return name.strip().lower().replace(" ", "_")

session = ort.InferenceSession(MODEL_PATH, providers=['CPUExecutionProvider'])

def classify_window(chunk_floats):
    if np.all(chunk_floats == chunk_floats[0]):
        return np.zeros(9789, dtype=np.float32)
    out = session.run(["predictions"], {"input": chunk_floats.reshape(1, 96000)})
    return out[0][0]

def select_all_catalog(scores, minimum_score):
    candidates = {}
    for idx, s in enumerate(species_list):
        if s.get("class") != "Aves":
            continue
        canon = canonical_key(s["canonical_scientific_name"])
        score = float(scores[idx])
        if canon not in candidates:
            candidates[canon] = (score, idx, s)
        else:
            prev_score, prev_idx, prev_s = candidates[canon]
            if score > prev_score or (score == prev_score and idx < prev_idx):
                candidates[canon] = (score, idx, s)

    filtered = [v for v in candidates.values() if v[0] >= minimum_score]
    # Sort descending by score, ascending by idx (catalog deterministic order)
    filtered.sort(key=lambda x: (-x[0], x[1]))
    return filtered

def run_clip_continuous(pcm_path, threshold):
    raw_shorts = np.fromfile(pcm_path, dtype=np.int16)
    assert len(raw_shorts) == 320000, f"Expected 320000 samples, got {len(raw_shorts)}"
    floats = raw_shorts.astype(np.float32) / 32768.0

    accumulated = {} # canon_key -> {"score": float, "species": dict, "first_window": int}
    
    # 8 windows: window 0 (0..96000), window 1 (32000..128000), ... window 7 (224000..320000)
    for w_idx in range(8):
        start = w_idx * 32000
        end = start + 96000
        chunk = floats[start:end]
        scores = classify_window(chunk)
        detections = select_all_catalog(scores, minimum_score=threshold)

        for score, idx, s in detections:
            canon = canonical_key(s["canonical_scientific_name"])
            if canon not in accumulated:
                accumulated[canon] = {
                    "score": score,
                    "species": s,
                    "first_window": w_idx,
                    "latency_s": 3.0 + w_idx * 1.0
                }
            else:
                if score > accumulated[canon]["score"]:
                    accumulated[canon]["score"] = score

    return accumulated

cal_items = [item for item in manifest["items"] if item["partition"] == "calibration"]
print(f"Calibration partition items: {len(cal_items)}")

candidate_thresholds = [0.35, 0.50, 0.65, 0.80]

print("\n=======================================================")
print("=== CONTINUOUS AUDIO CALIBRATION SWEEP (Spec 004) ===")
print("=======================================================")

chosen_threshold = None

for T in candidate_thresholds:
    print(f"\n>>> EVALUATING THRESHOLD T = {T:.2f} ON CALIBRATION PARTITION <<<")
    
    positives_passed = 0
    pos_results = []
    
    for item in cal_items:
        if item["target_canonical"] is not None:
            pcm_path = os.path.join(CORPUS_DIR, item["filename"])
            acc = run_clip_continuous(pcm_path, threshold=T)
            target_canon = canonical_key(item["target_canonical"])
            
            is_detected = target_canon in acc
            detected_score = acc[target_canon]["score"] if is_detected else 0.0
            latency = acc[target_canon]["latency_s"] if is_detected else None
            
            # Additional false candidate species in positive clip
            false_candidates = [
                f"{v['species']['canonical_scientific_name']} ({v['score']:.3f})"
                for k, v in acc.items() if k != target_canon
            ]
            
            if is_detected:
                positives_passed += 1
                pos_results.append(f"  [OK] {item['id']} ({item['species']}): DETECTED score={detected_score:.3f} at {latency:.1f}s | Other birds in list: {len(false_candidates)} {false_candidates}")
            else:
                pos_results.append(f"  [FAIL] {item['id']} ({item['species']}): NOT DETECTED (list size={len(acc)}: {list(acc.keys())})")

    # 4 Real controls in calibration
    control_false_positives = 0
    ctrl_results = []
    
    for item in cal_items:
        if item["target_canonical"] is None:
            pcm_path = os.path.join(CORPUS_DIR, item["filename"])
            acc = run_clip_continuous(pcm_path, threshold=T)
            
            if len(acc) > 0:
                control_false_positives += 1
                birds = [f"{v['species']['canonical_scientific_name']} ({v['score']:.3f})" for k, v in acc.items()]
                ctrl_results.append(f"  [DETECTION] {item['id']} ({item['common_name']}): {len(acc)} false birds: {birds}")
            else:
                ctrl_results.append(f"  [CLEAN] {item['id']} ({item['common_name']}): 0 detections")

    for r in pos_results:
        print(r)
    for c in ctrl_results:
        print(c)

    meets = (positives_passed >= 8 and control_false_positives == 0)
    print(f"\nSummary T={T:.2f}: Positives={positives_passed}/10 (required >=8), Real control FPs={control_false_positives}/4 (required ==0)")
    print(f"Meets calibration criteria: {meets}")

    if meets and chosen_threshold is None:
        chosen_threshold = T

print("\n=======================================================")
print(f"CALIBRATION OUTCOME: Chosen threshold = {chosen_threshold}")
print("=======================================================")
