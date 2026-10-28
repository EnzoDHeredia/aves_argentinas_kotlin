with open('scratch/darrieu2013_clean.txt', encoding='utf-8', errors='replace') as f:
    lines = f.readlines()

for i in range(475, 520):
    print(f"{i}: {repr(lines[i])}")
