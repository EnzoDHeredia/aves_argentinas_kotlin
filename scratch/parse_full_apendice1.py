import re
import sys

sys.stdout.reconfigure(encoding='utf-8', errors='replace')

with open('app/build/t4/categorizacion.txt', encoding='utf-8', errors='ignore') as f:
    cat_text = f.read()

start_idx = cat_text.find('PAGE 75')
end_idx = cat_text.find('PAGE 144')
apendice1 = cat_text[start_idx:end_idx]
lines = apendice1.split('\n')

raw_entries = {}
for i, line in enumerate(lines):
    line_s = line.strip()
    m = re.match(r'^(\d+)\s+(.+)$', line_s)
    if m:
        num = int(m.group(1))
        if 1 <= num <= 1033 and num not in raw_entries:
            raw_entries[num] = (i, m.group(2).strip())

parsed = {}
failed = []

# Pattern for scientific name in text: Capitalized Genus followed by lowercase species epithet
# Note: sometimes has subspecific or (continental) in vulgar name or ( E )
# e.g.:
# "Ñandú Rhea americana VU NT  - G. Fernández"
# "Choique Rhea pennata* VU NT  - D. De Lamo, M. Frixione, L. Bellis*"
# "Cauquén colorado (malvinas) ( E )Chloephaga rubidiceps* NA LC  - G. Aprile..."

sci_re = re.compile(r'([A-Z][a-z]+(?:\s+[a-z]+(?:\s+[a-z]+)?)?)\*?(?:\s+|\s*\()')

for num in range(1, 1034):
    line_idx, text_line = raw_entries[num]
    # If the text_line ends with hyphen, check next line
    if text_line.endswith('-'):
        text_line = text_line[:-1] + lines[line_idx+1].strip()
    
    # Try finding scientific name
    # The categories in Apéndice 1 are VU, AM, NA, EC, EX, EP, NC, EN, IC, DD, n/e
    # Often after scientific name: "Rhea americana VU ..."
    tokens = text_line.split()
    
    # Let's find genus (starts with capital, at least 4 letters, in second half of name or middle)
    # Or match with regex
    m = re.search(r'([A-Z][a-z]{2,}(?:\s+[a-z]{3,}(?:\s+[a-z]{3,})?)?)\*?\s+(VU|AM|NA|EC|EX|EP|NC|EN|IC|DD|n/e)\b', text_line)
    if not m:
        # maybe no space before genus, e.g. ( E )Chloephaga rubidiceps* NA
        m = re.search(r'([A-Z][a-z]{2,}\s+[a-z]{3,})\*?\s+(VU|AM|NA|EC|EX|EP|NC|EN|IC|DD|n/e)\b', text_line)
    
    if m:
        sci = m.group(1).strip()
        cat = m.group(2)
        vulgar = text_line[:m.start()].strip()
        # Clean vulgar
        vulgar = re.sub(r'\s*\(\s*E\s*\)\s*$', '', vulgar).strip()
        parsed[num] = (vulgar, sci, cat)
    else:
        failed.append((num, text_line))

print(f"Parsed: {len(parsed)} / 1033, Failed: {len(failed)}")
for f_id, f_text in failed[:20]:
    print(f"Failed {f_id}: {f_text}")
