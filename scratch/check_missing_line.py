with open('app/build/t4/categorizacion.txt', encoding='utf-8', errors='ignore') as f:
    lines = f.readlines()

for i, l in enumerate(lines):
    if l.strip().startswith('31 ') or l.strip().startswith('32 ') or l.strip().startswith('33 '):
        for j in range(max(0, i-2), min(len(lines), i+5)):
            print(f"{j+1}: {lines[j].rstrip()}")
        print("---")
