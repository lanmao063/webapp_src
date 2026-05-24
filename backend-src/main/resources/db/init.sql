-- 快递驿站管理系统 数据库初始化脚本
-- 对应C++项目的SQLite数据库迁移到MySQL

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

-- 包裹仓储表（对应C++的package表）
CREATE TABLE IF NOT EXISTS parcel (
    id              VARCHAR(24) PRIMARY KEY COMMENT '自动生成: D{星期}-{柜型}-{序号}',
    package_name    VARCHAR(100) NOT NULL,
    weight          DECIMAL(10,3),
    volume          DECIMAL(10,3),
    declared_value  DECIMAL(10,2),
    sender_name     VARCHAR(50),
    receiver_name   VARCHAR(50),
    receiver_phone  VARCHAR(20),
    receiver_address VARCHAR(200),
    notes           VARCHAR(500),
    status          VARCHAR(20) DEFAULT 'IN_WAREHOUSE' COMMENT 'IN_WAREHOUSE,PICKED_UP,ERROR',
    cabinet_type    VARCHAR(10) COMMENT 'SMALL,MEDIUM,LARGE',
    day_of_week     TINYINT,
    sequence_no     INT,
    entered_by      VARCHAR(50),
    enter_time      DATETIME DEFAULT CURRENT_TIMESTAMP,
    out_time        DATETIME,
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_status (status),
    INDEX idx_receiver_phone (receiver_phone),
    INDEX idx_enter_time (enter_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 寄件表（对应C++的sendPackage表）
CREATE TABLE IF NOT EXISTS send_package (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    tracking_number   VARCHAR(24) NOT NULL UNIQUE,
    package_name      VARCHAR(100),
    weight            DECIMAL(10,3),
    sender_name       VARCHAR(50),
    sender_phone      VARCHAR(20),
    sender_address    VARCHAR(200),
    receiver_name     VARCHAR(50),
    receiver_phone    VARCHAR(20),
    receiver_address  VARCHAR(200),
    fee               DECIMAL(10,2),
    is_paid           TINYINT DEFAULT 0,
    pickup_code       VARCHAR(10) COMMENT '取件码',
    notes             VARCHAR(500),
    status            VARCHAR(20) DEFAULT 'PENDING',
    pickup_method     VARCHAR(20) DEFAULT 'SELF_DROP' COMMENT 'SELF_DROP,DOOR_PICKUP',
    appointment_time  VARCHAR(20) COMMENT '预约时间段',
    created_by        BIGINT,
    created_at        DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at        DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_tracking (tracking_number),
    INDEX idx_sender_phone (sender_phone),
    INDEX idx_is_paid (is_paid),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 异常包裹表（对应C++的error_registration/error_handle功能）
CREATE TABLE IF NOT EXISTS error_parcel (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    parcel_id     VARCHAR(24) NOT NULL,
    error_type    VARCHAR(100) NOT NULL,
    description   VARCHAR(500),
    reported_by   VARCHAR(50),
    handler_name  VARCHAR(50),
    handle_result VARCHAR(500),
    handle_time   DATETIME,
    status        VARCHAR(20) DEFAULT 'UNRESOLVED',
    created_at    DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_parcel_id (parcel_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 发票表（对应C++的send_invoice功能）
CREATE TABLE IF NOT EXISTS invoice (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    invoice_number  VARCHAR(50) NOT NULL UNIQUE,
    send_package_id BIGINT,
    courier_id      BIGINT,
    amount          DECIMAL(10,2),
    status          VARCHAR(20) DEFAULT 'UNPAID',
    issue_date      DATE,
    due_date        DATE,
    paid_date       DATE,
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_courier (courier_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
