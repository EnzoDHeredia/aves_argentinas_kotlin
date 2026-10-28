import urllib.request
import json
import urllib.parse
import time

headers = {'User-Agent': 'AvesArgentinasEvaluationBot/1.0 (contact@avesargentinas.org.ar)'}

def search_files(term):
    params = {
        'action': 'query',
        'list': 'search',
        'srsearch': f'{term} filetype:audio',
        'format': 'json',
        'srnamespace': 6,
        'srlimit': 20
    }
    url = 'https://commons.wikimedia.org/w/api.php?' + urllib.parse.urlencode(params)
    req = urllib.request.Request(url, headers=headers)
    with urllib.request.urlopen(req) as resp:
        d = json.loads(resp.read().decode('utf-8'))
    titles = [x['title'] for x in d.get('query', {}).get('search', [])]
    if not titles:
        return []
    
    time.sleep(1)
    params2 = {
        'action': 'query',
        'titles': '|'.join(titles),
        'prop': 'imageinfo',
        'iiprop': 'url|size|duration|extmetadata',
        'format': 'json'
    }
    url2 = 'https://commons.wikimedia.org/w/api.php?' + urllib.parse.urlencode(params2)
    req2 = urllib.request.Request(url2, headers=headers)
    with urllib.request.urlopen(req2) as resp2:
        d2 = json.loads(resp2.read().decode('utf-8'))
    
    results = []
    for p in d2.get('query', {}).get('pages', {}).values():
        t = p.get('title')
        ii = p.get('imageinfo', [{}])[0]
        dur = ii.get('duration', 0)
        meta = ii.get('extmetadata', {})
        results.append({
            'title': t,
            'duration': dur,
            'license': meta.get('LicenseShortName', {}).get('value'),
            'artist': meta.get('Artist', {}).get('value'),
            'desc': meta.get('ImageDescription', {}).get('value'),
            'url': ii.get('url')
        })
    return results

for category, query in [
    ('WIND', 'wind sound'),
    ('WIND2', 'ventisca'),
    ('VOICE', 'discurso'),
    ('AMBIENT', 'rain sound'),
    ('AMBIENT2', 'stream water')
]:
    print(f'=== {category} ===')
    res = search_files(query)
    for r in res:
        if r['duration'] and r['duration'] >= 300:
            print(f"  {r['title']} | Dur: {r['duration']:.1f}s | Lic: {r['license']} | Url: {r['url']}")
    time.sleep(2)
