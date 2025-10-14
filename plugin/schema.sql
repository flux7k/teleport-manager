CREATE TABLE IF NOT EXISTS warp_point
(
    id         INT AUTO_INCREMENT PRIMARY KEY,
    name       VARCHAR(63) NOT NULL,
    server     VARCHAR(31) NOT NULL,
    world      VARCHAR(31) NOT NULL,
    x          DOUBLE       NOT NULL,
    y          DOUBLE       NOT NULL,
    z          DOUBLE       NOT NULL,
    yaw        FLOAT        NOT NULL,
    pitch      FLOAT        NOT NULL,
    UNIQUE KEY uk_warp_point_name (name),
    INDEX idx_warp_point_server (server)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;