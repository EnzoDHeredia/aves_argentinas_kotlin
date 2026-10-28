import urllib.request
from pathlib import Path

url = 'https://www.avesargentinas.org.ar/sites/default/files/documentos/LISTADO%20DE%20LAS%20AVES%20ARGENTINAS.pdf'
dest = Path('scratch/lista_aves_argentinas.pdf')
if not dest.exists():
    print("Downloading Aves Argentinas PDF...")
    req = urllib.request.Request(url, headers={'User-Agent': 'Mozilla/5.0'})
    with urllib.request.urlopen(req, timeout=30) as resp:
        data = resp.read()
        dest.write_bytes(data)
    print(f"Downloaded {len(data)} bytes to {dest}")
else:
    print(f"Already exists: {dest.stat().st_size} bytes")
