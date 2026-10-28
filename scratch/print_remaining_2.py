with open('scratch/remaining_unmatched.txt', encoding='utf-8') as f:
    lines = f.readlines()

for i, l in enumerate(lines[45:]):
    print(f"{i+46:2d}: {l.strip()}")
