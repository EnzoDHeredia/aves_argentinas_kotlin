import json

with open('app/src/main/assets/birdnet/species.json', encoding='utf-8') as f:
    model_species = json.load(f)

model_aves = [s for s in model_species if s.get('class') == 'Aves']

with open('scratch/remaining_unmatched.txt', encoding='utf-8') as f:
    remaining = [l.strip().split('\t') for l in f if l.strip()]

found_in_model = []
genuinely_absent = []

for r_id, r_vulg, r_sci, note in remaining:
    parts = r_sci.split()
    epithet = parts[1].lower() if len(parts) > 1 else ""
    # search any species in model with this epithet
    matches = [s for s in model_aves if epithet in s['canonical_scientific_name'].lower().split() or epithet in s['scientific_name'].lower().split()]
    if matches:
        found_in_model.append((r_id, r_vulg, r_sci, [m['canonical_scientific_name'] for m in matches]))
    else:
        genuinely_absent.append((r_id, r_vulg, r_sci))

print(f"Total remaining: {len(remaining)}")
print(f"Has matches by epithet: {len(found_in_model)}")
print(f"No match in model even by epithet (genuinely absent): {len(genuinely_absent)}")

print("\n--- FOUND BY EPITHET ---")
for r_id, r_vulg, r_sci, m_list in found_in_model:
    print(f"{r_id:4s}: {r_vulg:25} | {r_sci:30} -> {m_list[:3]}")

print("\n--- ABSENT FROM MODEL ---")
for r_id, r_vulg, r_sci in genuinely_absent:
    print(f"{r_id:4s}: {r_vulg:25} | {r_sci}")
