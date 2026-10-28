import re

with open('app/build/t4/categorizacion.txt', encoding='utf-8', errors='ignore') as f:
    cat_text = f.read()

start_idx = cat_text.find('PAGE 75')
end_idx = cat_text.find('PAGE 144')
apendice1 = cat_text[start_idx:end_idx]

# In Apéndice 1, let's find every line starting with ID
lines = apendice1.split('\n')

raw_entries = {}
for i, line in enumerate(lines):
    line_s = line.strip()
    m = re.match(r'^(\d+)\s+(.+)$', line_s)
    if m:
        num = int(m.group(1))
        if 1 <= num <= 1033:
            raw_entries[num] = (i, m.group(2).strip())

print("Found raw entries:", len(raw_entries))
# Let's inspect the ones not in raw_entries
missing = [i for i in range(1, 1034) if i not in raw_entries]
print("Missing from raw:", missing)
