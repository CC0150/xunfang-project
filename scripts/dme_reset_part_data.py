#!/usr/bin/env python3
"""
Clear all Part data and insert test data via IDME API
"""
import requests
import json
import time
import sys
import io

# Force UTF-8 on Windows
sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8', errors='replace')

IAM_URL = "https://iam.myhuaweicloud.com/v3/auth/tokens"
DME_BASE = "http://7ecd0cfa-83e9-493a-b9a1-c0e4d5e80fb4.xdm.runtime.cn-north-4.huaweicloud-idme.com/rdm_5eb684ad0fcc42e0b16ff67259692e7d_app/services"
DYNAMIC_API = DME_BASE + "/dynamic/api"
ENTITY = "XfPart01_20"

USERNAME = "gzlg020"
PASSWORD = "Hngy@123456"
ACCOUNT = "sziit2024"
PROJECT = "cn-north-4"

# ===== 1. Get Token =====
print(">>> [1/4] Getting DME auth token...")
auth_body = {
    "auth": {
        "identity": {
            "methods": ["password"],
            "password": {
                "user": {
                    "name": USERNAME,
                    "domain": {"name": ACCOUNT},
                    "password": PASSWORD
                }
            }
        },
        "scope": {
            "project": {"name": PROJECT}
        }
    }
}

resp = requests.post(IAM_URL, json=auth_body,
                     headers={"Content-Type": "application/json;charset=utf8",
                              "User-Agent": "Mozilla/5.0"})
if resp.status_code != 201:
    print(f"   FAIL auth HTTP {resp.status_code}: {resp.text}")
    sys.exit(1)

token = resp.headers.get("X-Subject-Token")
print(f"   OK Token: {token[:30]}...")

HEADERS = {
    "X-Auth-Token": token,
    "Content-Type": "application/json;charset=UTF-8"
}

# ===== 2. Find all Parts =====
print(">>> [2/4] Finding all Parts...")
url = f"{DYNAMIC_API}/{ENTITY}/find/100/1"
body = {
    "params": {
        "characterSet": "UTF8",
        "decrypt": False,
        "isPresentAll": True,
        "isNeedTotal": True,
        "publicData": "INCLUDE_PUBLIC_DATA",
        "sorts": [],
        "filter": {
            "conditions": [
                {"conditionName": "latest", "conditionValues": ["true"],
                 "ignoreStr": False, "multi": False, "operator": "="}
            ],
            "ignoreStr": False, "joiner": "and", "multi": False
        }
    }
}

resp = requests.post(url, json=body, headers=HEADERS)
data = resp.json()
if data.get("result") != "SUCCESS":
    print(f"   FAIL find: {data}")
    sys.exit(1)

items = data.get("data", [])
print(f"   OK Found {len(items)} Part records")

if len(items) == 0:
    print("   No data to delete, skipping...")
else:
    # ===== 3. Batch delete =====
    print(">>> [3/4] Deleting all Parts...")
    master_ids = []
    for item in items:
        master = item.get("master")
        if master and master.get("id"):
            master_ids.append(master["id"])

    master_ids = list(set(master_ids))
    print(f"   masterIds ({len(master_ids)}): {master_ids}")

    url = f"{DYNAMIC_API}/{ENTITY}/batchDelete"
    body = {"params": {"masterIds": master_ids}}
    resp = requests.post(url, json=body, headers=HEADERS)
    result = resp.json()
    if result.get("result") == "SUCCESS":
        print(f"   OK Deleted {len(master_ids)} records")
    else:
        print(f"   Batch delete failed: {result}")
        print("   Trying one by one...")
        for mid in master_ids:
            url2 = f"{DYNAMIC_API}/{ENTITY}/delete"
            body2 = {"params": {"masterId": mid}}
            r2 = requests.post(url2, json=body2, headers=HEADERS)
            r2j = r2.json()
            status = "OK" if r2j.get("result") == "SUCCESS" else f"FAIL {r2j}"
            print(f"     {mid}: {status}")

# ===== 4. Create test data =====
print(">>> [4/4] Creating test Parts...")

test_parts = [
    {"partName": "CPU-Intel-i7", "partType": "Ma", "partNameEn": "CPU i7 Processor",
     "specificationsModel": "i7-13700K", "unit": "PCS", "status": "Enable",
     "purchaseOrManufacture": "Manu", "partDeclaration": "Intel 13th Gen Core i7"},
    {"partName": "Cooler-Air-12cm", "partType": "Ma", "partNameEn": "Air Cooler 12cm",
     "specificationsModel": "FC-120", "unit": "PCS", "status": "Enable",
     "purchaseOrManufacture": "Pur", "partDeclaration": "12cm air cooler LGA1700"},
    {"partName": "RAM-DDR5-16GB", "partType": "Ma", "partNameEn": "DDR5 16GB RAM",
     "specificationsModel": "DDR5-5600-16G", "unit": "PCS", "status": "Enable",
     "purchaseOrManufacture": "Pur", "partDeclaration": "DDR5 5600MHz 16GB"},
    {"partName": "SSD-NVMe-1TB", "partType": "Ma", "partNameEn": "SSD 1TB NVMe",
     "specificationsModel": "NVMe-M.2-1TB", "unit": "PCS", "status": "Enable",
     "purchaseOrManufacture": "Pur", "partDeclaration": "NVMe M.2 1TB SSD"},
    {"partName": "PSU-850W-Gold", "partType": "Ma", "partNameEn": "Power Supply 850W Gold",
     "specificationsModel": "PSU-850G", "unit": "PCS", "status": "Enable",
     "purchaseOrManufacture": "Pur", "partDeclaration": "850W 80Plus Gold Full Modular"},
]

create_url = f"{DYNAMIC_API}/{ENTITY}/create"
success_count = 0
for i, p in enumerate(test_parts):
    body = {
        "params": {
            "partName": p["partName"],
            "partNameEn": p["partNameEn"],
            "partType": p["partType"],
            "specificationsModel": p["specificationsModel"],
            "unit": p["unit"],
            "status": p["status"],
            "purchaseOrManufacture": p["purchaseOrManufacture"],
            "partDeclaration": p["partDeclaration"],
            "createTime": time.strftime("%Y-%m-%dT%H:%M:%S.000+0000", time.gmtime()),
            "master": {},
            "branch": {}
        }
    }
    resp = requests.post(create_url, json=body, headers=HEADERS)
    result = resp.json()
    if result.get("result") == "SUCCESS":
        instance_id = result.get("data", [{}])[0].get("id", "?")
        print(f"   [{i+1}/5] OK {p['partName']} -> instanceId={instance_id}")
        success_count += 1
    else:
        print(f"   [{i+1}/5] FAIL {p['partName']}: {result}")

print(f"\n===== DONE =====")
print(f"Deleted: all old data cleared")
print(f"Created: {success_count}/{len(test_parts)} test records")
print(f"\nNOTE: Redis file metadata may need manual cleanup:")
print(f"  redis-cli DEL manufacture:part:file")
