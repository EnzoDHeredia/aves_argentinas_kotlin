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

# Load catalog logic identical to BirdSoundCatalog.kt
# Species: idx, scientific_name, canonical_scientific_name, class, common_name_es
# Aves only, grouped by canonical_scientific_name (lowercase, spaces to _)
def key(name):
    return name.strip().lower().replace(" ", "_")

session = ort.InferenceSession(MODEL_PATH, providers=['CPUExecutionProvider'])

def classify_pcm(pcm_path):
    raw_shorts = np.fromfile(pcm_path, dtype=np.int16)
    assert len(raw_shorts) == 320000
    floats = raw_shorts.astype(np.float32) / 32768.0
    
    window_starts = [0, 96000, 192000, 224000]
    scores = np.zeros(9789, dtype=np.float32)
    
    for start in window_starts:
        chunk = floats[start:start+96000]
        # check constant
        if np.all(chunk == chunk[0]):
            continue
        out = session.run(["predictions"], {"input": chunk.reshape(1, 96000)})
        pred = out[0][0]
        scores = np.maximum(scores, pred)
    return scores

def select_catalog(scores, minimum_score, limit=5):
    # Filter Aves
    candidates = {}
    for idx, s in enumerate(species_list):
        if s.get("class") != "Aves":
            continue
        canon_key = key(s["canonical_scientific_name"])
        score = float(scores[idx])
        if canon_key not in candidates:
            candidates[canon_key] = (score, idx, s)
        else:
            prev_score, prev_idx, prev_s = candidates[canon_key]
            if score > prev_score or (score == prev_score and idx < prev_idx):
                candidates[canon_key] = (score, idx, s)

    # Filter >= minimum_score
    filtered = [v for v in candidates.values() if v[0] >= minimum_score]
    # Sort descending by score, ascending by idx
    filtered.sort(key=lambda x: (-x[0], x[1]))
    return filtered[:limit]

# Run calibration partition
cal_items = [item for item in manifest["items"] if item["partition"] == "calibration"]
print(f"Calibration items: {len(cal_items)}")

cal_results = {}
for item in cal_items:
    pcm_path = os.path.join(CORPUS_DIR, item["filename"])
    scores = classify_pcm(pcm_path)
    cal_results[item["id"]] = {
        "item": item,
        "scores": scores
    }

candidate_thresholds = [0.35, 0.50, 0.65, 0.80]

print("\n=== CALIBRATION THRESHOLD SWEEP ===")
for T in candidate_thresholds:
    print(f"\n--- Testing Threshold T = {T:.2f} ---")
    
    # 1. Check positive birds (10 target species)
    positives_passed = 0
    pos_details = []
    for item in cal_items:
        if item["target_canonical"] is not None:
            scores = cal_results[item["id"]]["scores"]
            top = select_catalog(scores, minimum_score=T, limit=5)
            target_key = key(item["target_canonical"])
            target_rank = None
            target_score = None
            for rank, (sc, idx, s) in enumerate(top):
                if key(s["canonical_scientific_name"]) == target_key:
                    target_rank = rank + 1
                    target_score = sc
                    break
            
            top_names = [f"{s['canonical_scientific_name']} ({sc:.3f})" for sc, idx, s in top]
            if target_rank is not None:
                positives_passed += 1
                pos_details.append(f"  [OK] {item['species']}: rank #{target_rank} score={target_score:.3f} | Top: {top_names}")
            else:
                pos_details.append(f"  [FAIL] {item['species']}: NOT in top 5 >= {T:.2f} | Top: {top_names}")

    # 2. Check calibration real controls (2 wind + 2 voice = 4 controls)
    control_false_positives = 0
    ctrl_details = []
    for item in cal_items:
        if item["target_canonical"] is None:
            scores = cal_results[item["id"]]["scores"]
            top = select_catalog(scores, minimum_score=T, limit=5)
            if len(top) > 0:
                control_false_positives += 1
                top_names = [f"{s['canonical_scientific_name']} ({sc:.3f})" for sc, idx, s in top]
                ctrl_details.append(f"  [DETECTION] {item['id']} ({item['common_name']}): {top_names}")
            else:
                ctrl_details.append(f"  [CLEAN] {item['id']} ({item['common_name']}): 0 detections")

    for d in pos_details:
        print(d)
    for c in ctrl_details:
        print(c)
        
    print(f"Summary for T={T:.2f}: Positives in top 5: {positives_passed}/10 (required >=8), Real control false positives: {control_false_positives}/4 (required ==0)")
    meets_criteria = (positives_passed >= 8 and control_false_positives == 0)
    print(f"Meets calibration criteria: {meets_criteria}")
