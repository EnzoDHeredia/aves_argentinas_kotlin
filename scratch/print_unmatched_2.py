with open('scratch/all_unmatched.txt', encoding='utf-8') as f:
    lines = f.readlines()

for i, l in enumerate(lines[50:100]):
    print(f"{i+51:3d}: {l.strip()}")
