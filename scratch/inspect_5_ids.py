import re

with open('app/build/t4/categorizacion.txt', encoding='utf-8', errors='ignore') as f:
    lines = f.readlines()

for target in [154, 598, 668, 725, 795]:
    pat = re.compile(rf'^\s*{target}\s+')
    for i, l in enumerate(lines):
        if pat.match(l):
            for j in range(i, min(len(lines), i+3)):
                print(f"[{target}] {lines[j].strip()}")
