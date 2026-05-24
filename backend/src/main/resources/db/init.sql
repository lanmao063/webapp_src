-- 快递驿站管理系统 数据库初始化脚本

-- 统一用户表（合并C++的user/deliver/manager三张表）
CREATE TABLE IF NOT EXISTS system_user (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    username    VARCHAR(50) NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    role        VARCHAR(20) NOT NULL COMMENT 'REGULAR,COURIER,MANAGER',
    real_name   VARCHAR(50),
    phone       VARCHAR(20),
    address     VARCHAR(200),
    id_number   VARCHAR(18)  COMMENT '快递员/分拣员工号',
    message     VARCHAR(500),
    status      TINYINT DEFAULT 1,
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_role (role),
    INDEX idx_phone (phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 包裹基础信息表（入库包裹和寄件包裹的公共字段）
CREATE TABLE IF NOT EXISTS package (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    tracking_number VARCHAR(50) NOT NULL UNIQUE COMMENT '快递单号',
    package_name    VARCHAR(100) COMMENT '包裹名称',
    weight          DECIMAL(10,3) COMMENT '重量(kg)',
    volume          DECIMAL(10,3) COMMENT '体积(cm³)',
    sender_name     VARCHAR(50) COMMENT '寄件人',
    sender_phone    VARCHAR(20) COMMENT '寄件人电话',
    sender_address  VARCHAR(200) COMMENT '寄件人地址',
    receiver_name   VARCHAR(50) COMMENT '收件人',
    receiver_phone  VARCHAR(20) NOT NULL COMMENT '收件人电话',
    receiver_address VARCHAR(200) COMMENT '收件人地址',
    notes           VARCHAR(500) COMMENT '备注',
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_tracking (tracking_number),
    INDEX idx_receiver_phone (receiver_phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 入库包裹表（到达驿站的包裹）
CREATE TABLE IF NOT EXISTS inbound_package (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    package_id      BIGINT NOT NULL UNIQUE COMMENT '关联 package.id',
    pickup_code     VARCHAR(10) COMMENT '取件码（柜号-排号-序号）',
    cabinet_type    VARCHAR(10) COMMENT '柜型 SMALL/MEDIUM/LARGE',
    proxy_phone     VARCHAR(20) COMMENT '代取人手机号',
    status          VARCHAR(20) DEFAULT 'IN_WAREHOUSE' COMMENT 'IN_WAREHOUSE,CHECKED_OUT',
    is_auto_checkout TINYINT DEFAULT 0 COMMENT '是否自动出库',
    entered_by      BIGINT COMMENT '入库快递员ID',
    enter_time      DATETIME COMMENT '入库时间',
    out_time        DATETIME COMMENT '出库时间',
    auto_checkout_time DATETIME COMMENT '自动出库时间',
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_package_id (package_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 寄件包裹表（用户向外寄出的包裹）
CREATE TABLE IF NOT EXISTS send_package (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    package_id        BIGINT NOT NULL UNIQUE COMMENT '关联 package.id',
    fee               DECIMAL(10,2) COMMENT '费用',
    is_paid           TINYINT DEFAULT 0,
    pickup_method     VARCHAR(20) DEFAULT 'SELF_DROP' COMMENT 'SELF_DROP,DOOR_PICKUP',
    appointment_time  VARCHAR(20) COMMENT '预约时间段',
    status            VARCHAR(20) DEFAULT 'SUBMITTED' COMMENT 'SUBMITTED,APPROVED,PAID,COLLECTED,REJECTED',
    created_by        BIGINT COMMENT '创建人(用户)ID',
    courier_id        BIGINT COMMENT '揽收快递员ID',
    created_at        DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at        DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_package_id (package_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 异常包裹表
CREATE TABLE IF NOT EXISTS error_parcel (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    package_id    BIGINT NOT NULL COMMENT '关联 package.id',
    error_type    VARCHAR(100) NOT NULL,
    description   VARCHAR(500),
    reported_by   VARCHAR(50),
    handler_name  VARCHAR(50),
    handle_result VARCHAR(500),
    handle_time   DATETIME,
    status        VARCHAR(20) DEFAULT 'UNRESOLVED',
    created_at    DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_package_id (package_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
