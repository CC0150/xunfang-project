#!/bin/bash
TOKEN_RESP=$(curl -s -X POST "https://iam.myhuaweicloud.com/v3/auth/tokens" \
  -H "Content-Type: application/json;charset=utf8" \
  -d '{"auth":{"identity":{"methods":["password"],"password":{"user":{"name":"gzlg020","domain":{"name":"sziit2024"},"password":"Hngy@123456"}}},"scope":{"project":{"name":"cn-north-4"}}}}' \
  -D - 2>&1)
TOKEN=$(echo "$TOKEN_RESP" | grep -i "X-Subject-Token" | sed 's/.*: //' | tr -d '\r')
BASE="http://7ecd0cfa-83e9-493a-b9a1-c0e4d5e80fb4.xdm.runtime.cn-north-4.huaweicloud-idme.com/rdm_5eb684ad0fcc42e0b16ff67259692e7d_app/services/dynamic/api"

echo "=== 查询产品族（不带过滤条件）==="
curl -s -X POST "${BASE}/XfProductFamily_20/find/3/1" \
  -H "X-Auth-Token: $TOKEN" \
  -H "Content-Type: application/json;charset=utf8" \
  -d '{"params":{"isNeedTotal":true,"isPresentAll":true,"publicData":"INCLUDE_PUBLIC_DATA"}}' | python3 -c "
import sys,json
r=json.load(sys.stdin)
print('result:', r.get('result'))
arr=r.get('data',[])
print('count:', len(arr))
if arr:
    for k,v in arr[0].items():
        if not k.startswith('rdm') and k not in ('tenant','className','extAttrMap'):
            print(f'  {k}: {json.dumps(v,ensure_ascii=False)[:100]}')
" 2>/dev/null

echo ""
echo "=== 查询产品（latest=true）==="
curl -s -X POST "${BASE}/XfProduct_20/find/3/1" \
  -H "X-Auth-Token: $TOKEN" \
  -H "Content-Type: application/json;charset=utf8" \
  -d '{"params":{"filter":{"conditions":[{"conditionName":"latest","conditionValues":["true"],"operator":"=","ignoreStr":false,"multi":false}],"joiner":"and","ignoreStr":false,"multi":false},"isNeedTotal":true,"isPresentAll":true,"publicData":"INCLUDE_PUBLIC_DATA"}}' | python3 -c "
import sys,json
r=json.load(sys.stdin)
print('result:', r.get('result'))
arr=r.get('data',[])
print('count:', len(arr))
if arr:
    for k,v in arr[0].items():
        if not k.startswith('rdm') and k not in ('tenant','className','extAttrMap','branch','master'):
            print(f'  {k}: {json.dumps(v,ensure_ascii=False)[:100]}')
" 2>/dev/null

echo ""
echo "=== 生命周期模板 ==="
curl -s -X POST "${BASE}/LifecycleTemplate/find/5/1" \
  -H "X-Auth-Token: $TOKEN" \
  -H "Content-Type: application/json;charset=utf8" \
  -d '{"params":{"filter":{"conditions":[{"conditionName":"master.businessCode","conditionValues":["ProductLifecycle"],"operator":"=","ignoreStr":false,"multi":false}],"joiner":"and","ignoreStr":false,"multi":false},"isNeedTotal":true,"isPresentAll":true,"publicData":"INCLUDE_PUBLIC_DATA"}}' | python3 -c "
import sys,json
r=json.load(sys.stdin)
print('result:', r.get('result'))
arr=r.get('data',[])
print('count:', len(arr))
for t in arr[:3]:
    name=t.get('name','') or t.get('alias','') or ''
    print(f'  id={t.get(\"id\",\"\")}, name={name}')
" 2>/dev/null
