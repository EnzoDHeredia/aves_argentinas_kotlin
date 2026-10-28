with open('scratch/darrieu2013.txt', encoding='utf-8') as f:
    text = f.read()

lines = text.split('\n')
print(f"Total lines: {len(lines)}")
# Find some known species or genus
for i, l in enumerate(lines):
    if any(k in l for k in ['Rhea', 'Anas', 'Vanellus', 'Furnarius', 'Pitangus']):
        print(f"L{i}: {l}")
        for j in range(max(0, i-2), min(len(lines), i+8)):
            print(f"   [{j}] {lines[j]}")
        break
