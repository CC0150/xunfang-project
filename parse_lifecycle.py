import json, urllib.request, ssl, sys
ssl._create_default_https_context = ssl._create_unverified_context

# Get token
req = urllib.request.Request('https://iam.myhuaweicloud.com/v3/auth/tokens',
    data=json.dumps({"auth":{"identity":{"methods":["password"],"password":{"user":{"name":"gzlg020","domain":{"name":"sziit2024"},"password":"Hngy@123456"}}},"scope":{"project":{"name":"cn-north-4"}}}}).encode(),
    headers={'Content-Type':'application/json;charset=utf8'})
resp = urllib.request.urlopen(req)
token = resp.headers.get('X-Subject-Token','')
print(f"Token: {token[:30]}...")

BASE = "http://7ecd0cfa-83e9-493a-b9a1-c0e4d5e80fb4.xdm.runtime.cn-north-4.huaweicloud-idme.com/rdm_5eb684ad0fcc42e0b16ff67259692e7d_app/services/dynamic/api"

def dme_post(api, data):
    req = urllib.request.Request(f"{BASE}/{api}",
        data=json.dumps({"params":data}).encode(),
        headers={'X-Auth-Token':token,'Content-Type':'application/json;charset=utf8'})
    return json.load(urllib.request.urlopen(req))

# Get product
r = dme_post("XfProduct_20/get", {"id":"913119254369284096"})
data = r['data'][0]
lt = data.get('lifecycleTemplate',{})
print(f"\nTemplate: id={lt.get('id')}, name={lt.get('name','')}, nameEn={lt.get('nameEn','')}")

# Parse lifecycle phases
phases = lt.get('lifecyclePhaseList',[])
print(f"Phases: {len(phases)}")

for ph in phases[:2]:
    pid = ph.get('id')
    pname = ph.get('internalName','') or ph.get('name','')
    print(f"\n  Phase: id={pid}, name={pname}")
    # Show all keys
    for k in sorted(ph.keys()):
        if not k.startswith('rdm') and k not in ('tenant','className','creator','modifier','createTime','lastUpdateTime','clazz','securityLevel','kiaguid','description','descriptionEn'):
            v = ph[k]
            if isinstance(v, (str,int,float,bool)) or v is None:
                print(f"    {k}: {v}")
            elif isinstance(v, list):
                print(f"    {k}: list[{len(v)}]")
            elif isinstance(v, dict):
                print(f"    {k}: {{{','.join(list(v.keys())[:5])}}}")

# Find all states
print("\n=== All LifecycleStates in template ===")
def find_states(obj, path=""):
    if isinstance(obj, dict):
        if obj.get('className') == 'LifecycleState':
            print(f"  {path}: id={obj.get('id')}, internalName={obj.get('internalName','')}")
        for k,v in obj.items():
            find_states(v, f"{path}.{k}")
    elif isinstance(obj, list):
        for i,v in enumerate(obj):
            find_states(v, f"{path}[{i}]")

find_states(lt)

print("\n=== All LifecycleBusinessOperations in template ===")
def find_ops(obj, path=""):
    if isinstance(obj, dict):
        if obj.get('className') == 'LifecycleBusinessOperation':
            print(f"  {path}: id={obj.get('id')}, internalName={obj.get('internalName','')}, operId={obj.get('operId','')}")
        for k,v in obj.items():
            find_ops(v, f"{path}.{k}")
    elif isinstance(obj, list):
        for i,v in enumerate(obj):
            find_ops(v, f"{path}[{i}]")

find_ops(lt)
