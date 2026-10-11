"""Fetch one publisher-provided matching shader pack, preserving hashes and release metadata."""
from pathlib import Path
import hashlib,json,urllib.request
R=Path(__file__).resolve().parents[1]/'qa-artifacts/integration-matrix/shader-input'
R.mkdir(parents=True,exist_ok=True)
def fetch(url):
    with urllib.request.urlopen(urllib.request.Request(url,headers={'User-Agent':'Seamless Rowing isolated shader validation'}),timeout=120) as response:return response.read()
versions=json.loads(fetch('https://api.modrinth.com/v2/project/complementary-reimagined/version'))
versions.sort(key=lambda v:v['date_published'],reverse=True)
version=next(v for v in versions if all(m in v['game_versions'] for m in ('26.3','26.2','1.21.1','1.20.1')))
file=next(f for f in version['files'] if f['primary']);data=fetch(file['url'])
if hashlib.sha512(data).hexdigest()!=file['hashes']['sha512']:raise ValueError('Publisher hash mismatch')
(R/file['filename']).write_bytes(data)
(R/'manifest.json').write_text(json.dumps({'version':version,'sha256':hashlib.sha256(data).hexdigest()},indent=2))
print(file['filename'],version['version_number'])
