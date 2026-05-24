-- 为 send_package 表添加揽收快递员字段
ALTER TABLE send_package ADD COLUMN courier_id BIGINT COMMENT '揽收快递员ID' AFTER created_by;
