import re

with open('app/build/t4/categorizacion.txt', encoding='utf-8') as f:
    text = f.read()

# Let's find start of Apéndice 1 and end of Apéndice 1
start_idx = text.find('PAGE 75')
end_idx = text.find('PAGE 144')

apendice1 = text[start_idx:end_idx]

lines = apendice1.split('\n')
print(f"Apéndice 1 has {len(lines)} lines")

# Let's see lines that begin with digits
entries = {}
current_id = None

# Regex to find lines starting with an ID number
id_line_re = re.compile(r'^\s*(\d+)\s+(.+)$')
cat_tokens = {'VU', 'AM', 'NA', 'EC', 'EX', 'EP', 'NC', 'CR', 'EN', 'NT', 'LC', 'DD', 'IN'}

# Let's test parsing
for line in lines:
    line_s = line.strip()
    if not line_s or line_s.startswith('PAGE ') or line_s.startswith('Apéndice') or line_s.startswith('ID NOMBRE'):
        continue
    # Check if line starts with a number
    m = id_line_re.match(line_s)
    if m:
        num = int(m.group(1))
        rest = m.group(2)
        # Check if rest has scientific name and categories
        entries[num] = rest

print(f"Total entries found starting with digits: {len(entries)}")
missing_ids = [i for i in range(1, 1034) if i not in entries]
print(f"Missing IDs out of 1033: {len(missing_ids)}")
if missing_ids:
    print(f"Missing IDs: {missing_ids[:20]}")
