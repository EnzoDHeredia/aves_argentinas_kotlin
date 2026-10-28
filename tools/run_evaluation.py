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
        if np.all(chunk == chunk[0]):
            continue
        out = session.run(["predictions"], {"input": chunk.reshape(1, 96000)})
        pred = out[0][0]
        scores = np.maximum(scores, pred)
    return scores

def select_catalog(scores, minimum_score=0.35, limit=5):
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

    filtered = [v for v in candidates.values() if v[0] >= minimum_score]
    filtered.sort(key=lambda x: (-x[0], x[1]))
    return filtered[:limit]

# Fixed threshold after calibration
FIXED_THRESHOLD = 0.35

eval_items = [item for item in manifest["items"] if item["partition"] == "evaluation"]
print(f"Evaluation reserved items: {len(eval_items)}")
print(f"Fixed threshold: {FIXED_THRESHOLD:.2f}\n")

# Separate items
pos_items = [i for i in eval_items if i["target_canonical"] is not None]
ctrl_real = [i for i in eval_items if i["target_canonical"] is None and "silencio" not in i["id"]]
ctrl_silence = [i for i in eval_items if "silencio" in i["id"]]

assert len(pos_items) == 20
assert len(ctrl_real) == 4
assert len(ctrl_silence) == 4

print("=== 1. POSITIVE TARGET BIRDS (20 items, required >= 16 in top 5) ===")
pos_in_top5 = 0
top1_count = 0
pos_results = []

for item in pos_items:
    pcm_path = os.path.join(CORPUS_DIR, item["filename"])
    scores = classify_pcm(pcm_path)
    top = select_catalog(scores, minimum_score=FIXED_THRESHOLD, limit=5)
    
    target_key = key(item["target_canonical"])
    target_rank = None
    target_score = None
    for rank, (sc, idx, s) in enumerate(top):
        if key(s["canonical_scientific_name"]) == target_key:
            target_rank = rank + 1
            target_score = sc
            break
            
    is_top5 = target_rank is not None
    is_top1 = target_rank == 1
    if is_top5:
        pos_in_top5 += 1
    if is_top1:
        top1_count += 1
        
    top_str = [f"#{r+1} {s['canonical_scientific_name']} ({sc:.3f})" for r, (sc, idx, s) in enumerate(top)]
    status = "OK-TOP1" if is_top1 else ("OK-TOP5" if is_top5 else "MISS")
    print(f"[{status}] {item['id']} ({item['species']}): rank={target_rank}, score={target_score} | Top: {top_str}")
    pos_results.append({
        "id": item["id"],
        "species": item["species"],
        "target_canonical": item["target_canonical"],
        "target_rank": target_rank,
        "target_score": target_score,
        "top5": [(s["canonical_scientific_name"], float(sc)) for sc, idx, s in top]
    })

print(f"\nPositive results: {pos_in_top5}/20 in top 5 (required >= 16). Top 1 accuracy: {top1_count}/20 ({top1_count*100/20:.1f}%)")

print("\n=== 2. REAL CONTROLS (4 items: 2 wind + 2 voice, required <= 1 false positive) ===")
ctrl_real_fps = 0
for item in ctrl_real:
    pcm_path = os.path.join(CORPUS_DIR, item["filename"])
    scores = classify_pcm(pcm_path)
    top = select_catalog(scores, minimum_score=FIXED_THRESHOLD, limit=5)
    if len(top) > 0:
        ctrl_real_fps += 1
        top_str = [f"#{r+1} {s['canonical_scientific_name']} ({sc:.3f})" for r, (sc, idx, s) in enumerate(top)]
        print(f"[FALSE POSITIVE] {item['id']} ({item['common_name']}): {top_str}")
    else:
        print(f"[CLEAN] {item['id']} ({item['common_name']}): 0 detections")

print(f"Real control false positives: {ctrl_real_fps}/4 (required <= 1)")

print("\n=== 3. CONSTANT SILENCE CONTROLS (4 items, required == 0 false positives) ===")
ctrl_silence_fps = 0
for item in ctrl_silence:
    pcm_path = os.path.join(CORPUS_DIR, item["filename"])
    scores = classify_pcm(pcm_path)
    top = select_catalog(scores, minimum_score=FIXED_THRESHOLD, limit=5)
    if len(top) > 0:
        ctrl_silence_fps += 1
        top_str = [f"#{r+1} {s['canonical_scientific_name']} ({sc:.3f})" for r, (sc, idx, s) in enumerate(top)]
        print(f"[FALSE POSITIVE] {item['id']}: {top_str}")
    else:
        print(f"[CLEAN] {item['id']}: 0 detections")

print(f"Constant silence false positives: {ctrl_silence_fps}/4 (required == 0)")

all_passed = (pos_in_top5 >= 16 and ctrl_real_fps <= 1 and ctrl_silence_fps == 0)
print(f"\nALL RESERVED EVALUATION CRITERIA MET: {all_passed}")

with open("app/build/birdnet-verification/evaluation_results.json", "w", encoding="utf-8") as f:
    json.dump({
        "threshold": FIXED_THRESHOLD,
        "pos_in_top5": pos_in_top5,
        "pos_top1": top1_count,
        "ctrl_real_fps": ctrl_real_fps,
        "ctrl_silence_fps": ctrl_silence_fps,
        "all_passed": all_passed,
        "pos_results": pos_results
    }, f, indent=2)
