import urllib.request
from pathlib import Path

url = 'http://sedici.unlp.edu.ar/bitstream/handle/10915/134340/Documento_completo.pdf-PDFA.pdf?sequence=1&isAllowed=y'
dest = Path('scratch/darrieu2013.pdf')
if not dest.exists():
    print("Downloading Darrieu et al. 2013...")
    req = urllib.request.Request(url, headers={'User-Agent': 'Mozilla/5.0'})
    with urllib.request.urlopen(req, timeout=30) as resp:
        data = resp.read()
        dest.write_bytes(data)
    print(f"Downloaded {len(data)} bytes to {dest}")
else:
    print(f"Already exists: {dest.stat().st_size} bytes")
