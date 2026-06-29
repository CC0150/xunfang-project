#!/bin/bash

# ==================== 获取 DME Token ====================
echo ">>> 获取 DME Token..."
TOKEN_RESP=$(curl -s -X POST "https://iam.myhuaweicloud.com/v3/auth/tokens" \
  -H "Content-Type: application/json;charset=utf8" \
  -H "User-Agent: Mozilla/5.0" \
  -d '{"auth":{"identity":{"methods":["password"],"password":{"user":{"name":"gzlg020","domain":{"name":"sziit2024"},"password":"Hngy@123456"}}},"scope":{"project":{"name":"cn-north-4"}}}}' \
  -D - 2>&1)

TOKEN=$(echo "$TOKEN_RESP" | grep -i "X-Subject-Token" | sed 's/.*: //' | tr -d '\r')

if [ -z "$TOKEN" ]; then
    echo "FAILED to get token!"
    echo "$TOKEN_RESP" | tail -5
    exit 1
fi
echo "Token obtained: ${TOKEN:0:30}..."

BASE="http://7ecd0cfa-83e9-493a-b9a1-c0e4d5e80fb4.xdm.runtime.cn-north-4.huaweicloud-idme.com/rdm_5eb684ad0fcc42e0b16ff67259692e7d_app/services/dynamic/api"

# ==================== 创建产品族 ====================
echo ""
echo ">>> 创建产品族..."

callDme() {
    local api="$1" data="$2"
    curl -s -X POST "${BASE}/${api}" \
      -H "X-Auth-Token: $TOKEN" \
      -H "Content-Type: application/json;charset=utf8" \
      -d "{\"params\":$data}"
}

# 产品族1: 电子产品族
echo -n "  电子产品族... "
RES=$(callDme "XfProductFamily_20/create" '{"productFamilyNameCn":"电子产品族","productFamilyNameEn":"Electronic Products","description":"手机、电脑、平板等电子产品"}')
echo "$RES" | grep -q '"SUCCESS"' && echo "OK" || echo "FAILED: $RES"

# 产品族2: 机械零件族
echo -n "  机械零件族... "
RES=$(callDme "XfProductFamily_20/create" '{"productFamilyNameCn":"机械零件族","productFamilyNameEn":"Mechanical Parts","description":"轴承、齿轮、法兰等机械零件"}')
echo "$RES" | grep -q '"SUCCESS"' && echo "OK" || echo "FAILED: $RES"

# 产品族3: 化工材料族
echo -n "  化工材料族... "
RES=$(callDme "XfProductFamily_20/create" '{"productFamilyNameCn":"化工材料族","productFamilyNameEn":"Chemical Materials","description":"涂料、胶水、密封材料等化工产品"}')
echo "$RES" | grep -q '"SUCCESS"' && echo "OK" || echo "FAILED: $RES"

# 产品族4: 电气元件族
echo -n "  电气元件族... "
RES=$(callDme "XfProductFamily_20/create" '{"productFamilyNameCn":"电气元件族","productFamilyNameEn":"Electrical Components","description":"电阻、电容、继电器等电气元件"}')
echo "$RES" | grep -q '"SUCCESS"' && echo "OK" || echo "FAILED: $RES"

# 产品族5: 软件产品族
echo -n "  软件产品族... "
RES=$(callDme "XfProductFamily_20/create" '{"productFamilyNameCn":"软件产品族","productFamilyNameEn":"Software Products","description":"操作系统、工具软件、行业应用等软件产品"}')
echo "$RES" | grep -q '"SUCCESS"' && echo "OK" || echo "FAILED: $RES"

# ==================== 查询生命周期模板 ====================
echo ""
echo ">>> 查询生命周期模板..."
LC_RES=$(callDme "LifecycleTemplate/find/10/1" '{"filter":{"conditions":[{"conditionName":"master.businessCode","conditionValues":["ProductLifecycle"],"operator":"=","ignoreStr":false,"multi":false}],"joiner":"and","ignoreStr":false,"multi":false},"isNeedTotal":true,"isPresentAll":true,"publicData":"INCLUDE_PUBLIC_DATA"}')
echo "$LC_RES" | python3 -c "
import sys,json
d=json.load(sys.stdin) if sys.stdin else {}
templates = d.get('data',[])
print(f'  找到 {len(templates)} 个生命周期模板:')
for t in templates:
    tid=t.get('id','')
    tn=t.get('name','') or t.get('alias','') or tid
    print(f'    id={tid}, name={tn}')
" 2>/dev/null || echo "$LC_RES" | grep -o '"id":"[^"]*"' | head -3

echo ""
echo "========== 测试数据创建完成 =========="
echo "前端刷新页面即可看到 5 个产品族"
