import json, subprocess, re, sys, os

# Get token
token_resp = subprocess.run([
    'curl', '-s', '-X', 'POST',
    'https://iam.myhuaweicloud.com/v3/auth/tokens',
    '-H', 'Content-Type: application/json;charset=utf8',
    '-H', 'User-Agent: Mozilla/5.0',
    '-d', json.dumps({
        "auth": {
            "identity": {
                "methods": ["password"],
                "password": {
                    "user": {
                        "name": "gzlg020",
                        "domain": {"name": "sziit2024"},
                        "password": "Hngy@123456"
                    }
                }
            },
            "scope": {"project": {"name": "cn-north-4"}}
        }
    })
], capture_output=True, text=True)

headers = token_resp.stdout
token_match = re.search(r'X-Subject-Token:\s*(\S+)', headers)
if not token_match:
    print("Failed to get token!")
    sys.exit(1)
token = token_match.group(1).strip()
print(f"Token: {token[:30]}...")

BASE = "http://7ecd0cfa-83e9-493a-b9a1-c0e4d5e80fb4.xdm.runtime.cn-north-4.huaweicloud-idme.com/rdm_5eb684ad0fcc42e0b16ff67259692e7d_app/services/dynamic/api"

def call_dme(api, data):
    resp = subprocess.run([
        'curl', '-s', '-X', 'POST',
        f'{BASE}/{api}',
        '-H', f'X-Auth-Token: {token}',
        '-H', 'Content-Type: application/json;charset=utf8',
        '-d', json.dumps({"params": data}, ensure_ascii=False)
    ], capture_output=True, text=True)
    return json.loads(resp.stdout)

# Get family info
print("\n=== Product Families ===")
families_resp = call_dme("XfProductFamily_20/find/10/1", {
    "isNeedTotal": True, "isPresentAll": True, "publicData": "INCLUDE_PUBLIC_DATA"
})
families = {}
for f in families_resp.get("data", []):
    cn = f.get("productFamilyNameCn", "")
    families[cn] = f["id"]
    print(f"  {cn}: {f['id']}")

# Products to create
products = [
    ("智能手机", "电子产品族", "ST", "iPhone-15-Pro", "高端智能手机产品"),
    ("精密轴承", "机械零件族", "CU", "6205-2RS", "深沟球轴承，双面密封"),
    ("不锈钢法兰", "机械零件族", "ST", "DN100-PN16", "DN100不锈钢法兰，PN16压力等级"),
    ("环氧树脂胶", "化工材料族", "ST", "EP-502", "双组分环氧树脂结构胶"),
    ("固态继电器", "电气元件族", "CU", "SSR-40DA", "40A固态继电器，直流控交流"),
]

print("\n=== Creating Products ===")
for name, family, cat, spec, desc in products:
    data = {
        "productName": name,
        "productFamily": family,
        "category": cat,
        "specificationModels": spec,
        "productDescribe": desc,
        "master": {},
        "branch": {},
        "createTime": "2026-06-29T10:15:00.000+0000"
    }
    resp = call_dme("XfProduct_20/create", data)
    result = resp.get("result", "?")
    vid = ""
    mid = ""
    if result == "SUCCESS" and resp.get("data"):
        vid = resp["data"][0].get("id", "")
    # Check category in response
    cat_val = ""
    if resp.get("data") and len(resp["data"]) > 0:
        d0 = resp["data"][0]
        cat_val = json.dumps(d0.get("category"), ensure_ascii=False)
    print(f"  {name}: {result}, versionId={vid}, category={cat_val}")

print("\nDone!")
