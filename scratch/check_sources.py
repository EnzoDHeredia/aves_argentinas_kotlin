import urllib.request
import re

url = 'http://sedici.unlp.edu.ar/handle/10915/134340'
req = urllib.request.Request(url, headers={'User-Agent': 'Mozilla/5.0'})
try:
    with urllib.request.urlopen(req, timeout=10) as resp:
        html = resp.read().decode('utf-8', errors='ignore')
    for m in re.finditer(r'href=[\'"]([^\'"]*bitstream[^\'"]*)[\'"]', html):
        print('Found bitstream:', m.group(1))
except Exception as e:
    print('Error:', e)
