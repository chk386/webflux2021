drop table if exists member;

CREATE TABLE member
(
    id    BIGINT(10) UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name  VARCHAR(50) NOT NULL,
    phone VARCHAR(50)
)