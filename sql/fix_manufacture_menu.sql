-- 修复制造管理菜单（不修改系统管理下已有的菜单）
-- 1. 添加"制造管理"父菜单
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time)
SELECT '制造管理', 0, 4, '/manufacture', NULL, 1, 0, 'M', '0', '0', '', 'build', 'admin', sysdate(), 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_name = '制造管理' AND parent_id = 0);
SET @pid = (SELECT menu_id FROM sys_menu WHERE menu_name = '制造管理' AND parent_id = 0);

-- 2. 供应商管理：把 parent_id 从 1 改为制造管理
UPDATE sys_menu SET parent_id = @pid, component = 'manufacture/supplier/index', perms = 'manufacture:supplier:list', visible = '0'
WHERE menu_id = 2031;

-- 3. 采购订单：把 parent_id 从 1 改为制造管理，修正 component 路径（文件夹已从 order 重命名为 purchaseorder）
UPDATE sys_menu SET parent_id = @pid, component = 'manufacture/purchaseorder/index', perms = 'manufacture:order:list', visible = '0'
WHERE menu_id = 2032;

-- 4. 新增单位管理
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time)
SELECT '单位管理', @pid, 3, 'unit', 'manufacture/unit/index', 1, 0, 'C', '0', '0', 'manufacture:unit:list', 'international', 'admin', sysdate(), 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_name = '单位管理' AND parent_id = @pid);

-- 5. 新增Part管理
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time)
SELECT 'Part管理', @pid, 4, 'part', 'manufacture/part/index', 1, 0, 'C', '0', '0', 'manufacture:part:list', 'component', 'admin', sysdate(), 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_name = 'Part管理' AND parent_id = @pid);
