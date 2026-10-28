import urllib.request
import re

box_url = 'https://cornell.box.com/s/zjci66divvqnz00k98r7pmmb6kpc69zs'
req = urllib.request.Request(box_url, headers={'User-Agent': 'Mozilla/5.0'})
try:
    with urllib.request.urlopen(req, timeout=10) as resp:
        html = resp.read().decode('utf-8', errors='ignore')
        print(f"Box page size: {len(html)}")
        # Look for download link or filename
        for m in re.finditer(r'href=[\'"]([^\'"]*download[^\'"]*)[\'"]', html):
            print("Download link:", m.group(1))
        for m in re.finditer(r'[\'"]name[\'"]\s*:\s*[\'"]([^\'"]+\.(?:csv|xlsx|zip))[\'"]', html):
            print("File name:", m.group(1))
except Exception as e:
    print("Box error:", e)
