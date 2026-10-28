import zlib
import re

with open('scratch/darrieu2013.pdf', 'rb') as f:
    content = f.read()

streams = re.findall(rb'stream\r?\n(.*?)\r?\nendstream', content, re.DOTALL)
print(f"Total streams: {len(streams)}")

all_text = []
for s in streams:
    try:
        decompressed = zlib.decompress(s)
        # Extract text from BT ... ET or TJ / Tj operators
        text_matches = re.findall(rb'\((.*?)\)\s*Tj', decompressed)
        for tm in text_matches:
            try:
                all_text.append(tm.decode('latin1'))
            except:
                pass
        # Also [ (t) 20 (e) ... ] TJ
        array_matches = re.findall(rb'\[(.*?)\]\s*TJ', decompressed)
        for am in array_matches:
            parts = re.findall(rb'\((.*?)\)', am)
            all_text.append(''.join(p.decode('latin1', errors='ignore') for p in parts))
    except Exception:
        pass

full_txt = '\n'.join(all_text)
print(f"Extracted characters: {len(full_txt)}")
with open('scratch/darrieu2013.txt', 'w', encoding='utf-8') as out:
    out.write(full_txt)

print("Sample lines:")
for l in [x for x in all_text if len(x) > 10][:20]:
    print("  ", l)
