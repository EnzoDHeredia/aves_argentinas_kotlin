import urllib.request
import re

url = 'https://support.ebird.org/es/support/solutions/articles/48000804865'
try:
    req = urllib.request.Request(url, headers={'User-Agent': 'Mozilla/5.0'})
    with urllib.request.urlopen(req, timeout=10) as resp:
        print(url, '-> status', resp.status)
        html = resp.read().decode('utf-8', errors='ignore')
        for m in re.finditer(r'href=[\'"]([^\'"]*(?:csv|xlsx|xls|drive|dropbox|box)[^\'"]*)[\'"]', html, re.I):
            print('   Link:', m.group(1))
except Exception as e:
    print(url, '-> error', e)
