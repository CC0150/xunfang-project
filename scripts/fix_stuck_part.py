#!/usr/bin/env python3
"""Fix stuck Part: pass the exact modifier from the working copy"""
import requests
import sys
import io
sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8', errors='replace')

IAM_URL = "https://iam.myhuaweicloud.com/v3/auth/tokens"
DME_BASE = "http://7ecd0cfa-83e9-493a-b9a1-c0e4d5e80fb4.xdm.runtime.cn-north-4.huaweicloud-idme.com/rdm_5eb684ad0fcc42e0b16ff67259692e7d_app/services"
DYNAMIC_API = DME_BASE + "/dynamic/api"
ENTITY = "XfPart01_20"
MASTER_ID = "913550306338422785"
VERSION_ID = "913835190755794944"
MODIFIER = "gzlg020@sxxgyrj.orgid.top 1008600001763653361"

# Auth
print("Auth...")
resp = requests.post(IAM_URL, json={
    "auth":{"identity":{"methods":["password"],"password":{"user":{"name":"gzlg020","domain":{"name":"sziit2024"},"password":"Hngy@123456"}}},"scope":{"project":{"name":"cn-north-4"}}}
}, headers={"Content-Type":"application/json;charset=utf8","User-Agent":"Mozilla/5.0"})
token = resp.headers.get("X-Subject-Token")
H = {"X-Auth-Token":token, "Content-Type":"application/json;charset=UTF-8"}
print(f"Token OK")

def post(url, body, label=""):
    r = requests.post(url, json=body, headers=H)
    j = r.json()
    result = j.get('result')
    msg = j.get('error_msg', '')
    print(f"  {label}: {result} {msg[:80] if msg else ''}")
    return j

# 1. Update the working copy with the exact modifier
print("\n[1] Updating working copy with exact modifier...")
url = f"{DYNAMIC_API}/{ENTITY}/update"
body = {"params": {
    "id": VERSION_ID,
    "modifier": MODIFIER,
    "partName": "螺丝刀",
    "partType": "Ma",
    "status": "Enable",
    "purchaseOrManufacture": "Manu",
    "partDeclaration": "fixed by admin",
    "master": {"id": MASTER_ID},
    "branch": {}
}}
post(url, body, "update")

# 2. Checkin with the exact modifier
print("\n[2] Checking in with exact modifier...")
url = f"{DYNAMIC_API}/{ENTITY}/checkin"
body = {"params": {"masterId": MASTER_ID, "modifier": MODIFIER}}
j = post(url, body, "checkin")

if j.get("result") == "SUCCESS":
    print("\n[3] Deleting...")
    url = f"{DYNAMIC_API}/{ENTITY}/delete"
    body = {"params": {"masterId": MASTER_ID}}
    post(url, body, "delete")

print("\nDone!")
