with open('scratch/all_unmatched.txt', encoding='utf-8') as f:
    lines = f.readlines()

for i, l in enumerate(lines[100:]):
    print(f"{i+101:3d}: {l.strip()}")
