-- ============================================================
-- 快递驿站管理系统 测试数据
-- 所有测试用户密码: 123456
-- ============================================================

-- ============================================================
-- 1. 用户 (system_user)
-- ============================================================
INSERT INTO system_user (username, password, role, real_name, phone, address) VALUES
('zhangsan',  '$2a$10$iaNp2.xxlTtqWyDaZjKP0OBfPr2.gBYgkirsWmhPUrbXGc7h9kd7.', 'REGULAR', '张三', '13800001111', 'China'),
('lisi',      '$2a$10$iaNp2.xxlTtqWyDaZjKP0OBfPr2.gBYgkirsWmhPUrbXGc7h9kd7.', 'REGULAR', '李四', '13800002222', 'China'),
('wangwu',    '$2a$10$iaNp2.xxlTtqWyDaZjKP0OBfPr2.gBYgkirsWmhPUrbXGc7h9kd7.', 'REGULAR', '王五', '13800003333', 'China'),
('courier1',  '$2a$10$iaNp2.xxlTtqWyDaZjKP0OBfPr2.gBYgkirsWmhPUrbXGc7h9kd7.', 'COURIER', '快递员老赵', '13900001111', NULL),
('courier2',  '$2a$10$iaNp2.xxlTtqWyDaZjKP0OBfPr2.gBYgkirsWmhPUrbXGc7h9kd7.', 'COURIER', '快递员小钱', '13900002222', NULL),
('manager1',  '$2a$10$iaNp2.xxlTtqWyDaZjKP0OBfPr2.gBYgkirsWmhPUrbXGc7h9kd7.', 'MANAGER', '管理员孙姐', '13700001111', NULL)
ON DUPLICATE KEY UPDATE username=username;

-- ============================================================
-- 2. 入库包裹 (package + inbound_package)
-- ============================================================

-- 包裹1: 张三的iPhone (小件, 已入库, 待取)
INSERT INTO package (id, tracking_number, package_name, weight, volume,
                     sender_name, sender_phone, sender_address,
                     receiver_name, receiver_phone, receiver_address, notes)
VALUES (101, 'SF1234567890', 'iPhone 15', 0.5, 2000,
        '苹果旗舰店', '4006668800', '深圳市南山区',
        '张三', '13800001111', '北京市朝阳区1号', '易碎品，轻拿轻放');

INSERT INTO inbound_package (package_id, pickup_code, cabinet_type, status, entered_by, enter_time)
VALUES (101, '01-3-0001', 'SMALL', 'IN_WAREHOUSE', 4, '2026-05-24 09:30:00');

-- 包裹2: 李四的书 (小件, 已入库, 王五代取)
INSERT INTO package (id, tracking_number, package_name, weight, volume,
                     sender_name, sender_phone, sender_address,
                     receiver_name, receiver_phone, receiver_address, notes)
VALUES (102, 'YTO1234567890', 'Java编程思想', 2.0, 8000,
        '当当网', '010-12345678', '北京市大兴区',
        '李四', '13800002222', '上海市浦东新区2号', NULL);

INSERT INTO inbound_package (package_id, pickup_code, cabinet_type, proxy_phone, status, entered_by, enter_time)
VALUES (102, '02-5-0023', 'SMALL', '13800003333', 'IN_WAREHOUSE', 4, '2026-05-24 10:00:00');

-- 包裹3: 王五的电视机 (大重件, 已入库, 待取)
INSERT INTO package (id, tracking_number, package_name, weight, volume,
                     sender_name, sender_phone, sender_address,
                     receiver_name, receiver_phone, receiver_address, notes)
VALUES (103, 'ZTO1234567890', '小米电视 65寸', 15.0, 200000,
        '小米商城', '4001005678', '北京市海淀区',
        '王五', '13800003333', '广州市天河区3号', '大件物品，需两人搬运');

INSERT INTO inbound_package (package_id, pickup_code, cabinet_type, status, entered_by, enter_time)
VALUES (103, '101-2-0045', 'LARGE', 'IN_WAREHOUSE', 5, '2026-05-24 11:00:00');

-- 包裹4: 张三的电饭煲 (中件, 已出库 - 测试历史记录)
INSERT INTO package (id, tracking_number, package_name, weight, volume,
                     sender_name, sender_phone, sender_address,
                     receiver_name, receiver_phone, receiver_address, notes)
VALUES (104, 'YD1234567890', '美的电饭煲', 3.5, 50000,
        '京东自营', '4006069999', '北京市通州区',
        '张三', '13800001111', '北京市朝阳区1号', NULL);

INSERT INTO inbound_package (package_id, pickup_code, cabinet_type, status, entered_by, enter_time, out_time)
VALUES (104, '55-4-0012', 'MEDIUM', 'CHECKED_OUT', 4, '2026-05-23 14:00:00', '2026-05-24 08:30:00');

-- ============================================================
-- 3. 寄件包裹 (package + send_package)
-- ============================================================

-- 寄件1: 张三寄出 → 待审核
INSERT INTO package (id, tracking_number, package_name, weight, volume,
                     sender_name, sender_phone, sender_address,
                     receiver_name, receiver_phone, receiver_address, notes)
VALUES (201, 'SP20260524A001', '手工饼干礼盒', 1.5, 15000,
        '张三', '13800001111', '北京市朝阳区1号',
        '赵六', '13800006666', '杭州市西湖区6号', '生日礼物');

INSERT INTO send_package (package_id, fee, is_paid, pickup_method, appointment_time, status, created_by, courier_id)
VALUES (201, 15.00, 0, 'SELF_DROP', '2026-05-25 上午', 'SUBMITTED', 1, NULL);

-- 寄件2: 李四寄出 → 已审核待付款
INSERT INTO package (id, tracking_number, package_name, weight, volume,
                     sender_name, sender_phone, sender_address,
                     receiver_name, receiver_phone, receiver_address, notes)
VALUES (202, 'SP20260524A002', '秋冬外套', 3.0, 30000,
        '李四', '13800002222', '上海市浦东新区2号',
        '钱七', '13800007777', '南京市鼓楼区7号', NULL);

INSERT INTO send_package (package_id, fee, is_paid, pickup_method, appointment_time, status, created_by, courier_id)
VALUES (202, 15.00, 0, 'DOOR_PICKUP', '2026-05-25 下午', 'APPROVED', 2, 4);

-- 寄件3: 王五寄出 → 已付款待揽收
INSERT INTO package (id, tracking_number, package_name, weight, volume,
                     sender_name, sender_phone, sender_address,
                     receiver_name, receiver_phone, receiver_address, notes)
VALUES (203, 'SP20260524A003', '机械键盘', 8.0, 8000,
        '王五', '13800003333', '广州市天河区3号',
        '孙八', '13800008888', '深圳市南山区8号', NULL);

INSERT INTO send_package (package_id, fee, is_paid, pickup_method, appointment_time, status, created_by, courier_id)
VALUES (203, 25.00, 1, 'DOOR_PICKUP', '2026-05-24 下午', 'PAID', 3, 4);

-- 寄件4: 张三寄出 → 已完成(已揽收)
INSERT INTO package (id, tracking_number, package_name, weight, volume,
                     sender_name, sender_phone, sender_address,
                     receiver_name, receiver_phone, receiver_address, notes)
VALUES (204, 'SP20260523A004', '旧书一批', 12.0, 60000,
        '张三', '13800001111', '北京市朝阳区1号',
        '周九', '13800009999', '武汉市洪山区9号', NULL);

INSERT INTO send_package (package_id, fee, is_paid, pickup_method, appointment_time, status, created_by, courier_id)
VALUES (204, 40.00, 1, 'SELF_DROP', '2026-05-23 上午', 'COLLECTED', 1, 4);

-- 寄件5: 李四寄出 → 已驳回
INSERT INTO package (id, tracking_number, package_name, weight, volume,
                     sender_name, sender_phone, sender_address,
                     receiver_name, receiver_phone, receiver_address, notes)
VALUES (205, 'SP20260523A005', '液体化妆品套装', 25.0, 5000,
        '李四', '13800002222', '上海市浦东新区2号',
        '吴十', '13800001010', '成都市锦江区10号', '内含液体，不确定能否寄送');

INSERT INTO send_package (package_id, fee, is_paid, pickup_method, appointment_time, status, created_by, courier_id)
VALUES (205, 75.00, 0, 'SELF_DROP', '2026-05-23 下午', 'REJECTED', 2, NULL);

-- ============================================================
-- 4. 异常包裹 (error_parcel)
-- ============================================================

INSERT INTO error_parcel (id, package_id, error_type, description, reported_by, status)
VALUES (1, 101, '包裹破损', '外包装有明显挤压痕迹，内部物品状态待确认', '张三', 'UNRESOLVED');

INSERT INTO error_parcel (id, package_id, error_type, description, reported_by, handler_name, handle_result, handle_time, status)
VALUES (2, 104, '面单模糊', '快递单号印刷不清无法扫描', '李四', '管理员孙姐', '已联系发件人确认单号，手动录入系统', '2026-05-24 09:00:00', 'RESOLVED');

-- ============================================================
-- 验证查询
-- ============================================================
-- SELECT '=== 用户 ===' AS '';
-- SELECT id, username, role, real_name, phone FROM system_user ORDER BY id;
-- SELECT '=== 入库包裹(在库) ===' AS '';
-- SELECT p.tracking_number, p.package_name, p.receiver_name, ib.pickup_code, ib.cabinet_type, ib.status
-- FROM package p JOIN inbound_package ib ON p.id = ib.package_id WHERE ib.status = 'IN_WAREHOUSE';
-- SELECT '=== 寄件包裹 ===' AS '';
-- SELECT p.tracking_number, p.package_name, p.sender_name, sp.fee, sp.status
-- FROM package p JOIN send_package sp ON p.id = sp.package_id ORDER BY sp.id;
-- SELECT '=== 异常包裹 ===' AS '';
-- SELECT ep.id, p.tracking_number, ep.error_type, ep.status FROM error_parcel ep LEFT JOIN package p ON ep.package_id = p.id;
