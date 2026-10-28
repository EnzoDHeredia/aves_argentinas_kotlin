import urllib.request
import re

for url in [
    'https://www.avesargentinas.org.ar/lista-de-las-aves-argentinas',
    'https://www.avesargentinas.org.ar/listado-de-las-aves-argentinas',
    'https://www.avesargentinas.org.ar/caro'
]:
    try:
        req = urllib.request.Request(url, headers={'User-Agent': 'Mozilla/5.0'})
        with urllib.request.urlopen(req, timeout=10) as resp:
            print(url, '-> status', resp.status)
            html = resp.read().decode('utf-8', errors='ignore')
            for m in re.finditer(r'href=[\'"]([^\'"]*\.pdf)[\'"]', html):
                print('   PDF:', m.group(1))
    except Exception as e:
        print(url, '-> error', e)
