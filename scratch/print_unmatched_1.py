with open('scratch/all_unmatched.txt', encoding='utf-8') as f:
    lines = f.readlines()

for i, l in enumerate(lines[:50]):
    print(f"{i+1:3d}: {l.strip()}")
