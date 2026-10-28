import urllib.request
from pathlib import Path

url = 'https://cornell.box.com/shared/static/zjci66divvqnz00k98r7pmmb6kpc69zs.xlsx'
dest = Path('scratch/ebird_taxonomy.xlsx')
try:
    req = urllib.request.Request(url, headers={'User-Agent': 'Mozilla/5.0'})
    print("Testing direct Box download...")
    with urllib.request.urlopen(req, timeout=15) as resp:
        print("Status:", resp.status)
        print("Content-Type:", resp.headers.get('Content-Type'))
        print("Content-Length:", resp.headers.get('Content-Length'))
except Exception as e:
    print("Download error:", e)
