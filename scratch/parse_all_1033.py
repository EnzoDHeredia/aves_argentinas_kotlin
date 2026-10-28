import re

with open('app/build/t4/categorizacion.txt', encoding='utf-8') as f:
    text = f.read()

start_idx = text.find('PAGE 75')
end_idx = text.find('PAGE 144')
apendice1 = text[start_idx:end_idx]

# In Apéndice 1, each entry starts with an ID at the start of a line
# followed by the vulgar name, scientific name, and national category (VU, AM, NA, EC, EX, EP, NC)
# Note: sometimes scientific name has an asterisk (e.g. Rhea pennata*) or subspecific notation or (E)

# Let's inspect entries 1 to 20, 100 to 110, 500 to 510, 1025 to 1033
lines = apendice1.split('\n')

parsed = {}
for i in range(1, 1034):
    # Find line starting with str(i) + ' '
    pattern = re.compile(rf'^\s*{i}\s+(.+)$')
    matches = []
    for line in lines:
        m = pattern.match(line)
        if m:
            matches.append(m.group(1).strip())
    if len(matches) == 1:
        parsed[i] = matches[0]
    else:
        # Multiple or none?
        # Filter matches that look like an entry (contain a known category)
        valid = [m for m in matches if any(f" {c} " in f" {m} " for c in ['VU', 'AM', 'NA', 'EC', 'EX', 'EP', 'NC'])]
        if len(valid) == 1:
            parsed[i] = valid[0]
        else:
            print(f"ID {i} has {len(matches)} matches, {len(valid)} valid: {matches}")

print(f"Cleanly matched: {len(parsed)} / 1033")
