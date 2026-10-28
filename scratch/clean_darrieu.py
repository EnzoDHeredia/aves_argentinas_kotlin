import re
import sys

sys.stdout.reconfigure(encoding='utf-8', errors='replace')

with open('scratch/darrieu2013_clean.txt', encoding='utf-8', errors='replace') as f:
    lines = f.readlines()

for i, l in enumerate(lines):
    if 'Familia RHEIDAE' in l:
        for j in range(i, min(len(lines), i+35)):
            print(f"[{j}] {lines[j].strip()}")
        break
