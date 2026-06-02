-- ============================================================
--  photogear.app – Esquema de base de datos
--  Compatible con H2 (MODE=MySQL) y MariaDB
--  Autor: Edgar F. Flores M.
-- ============================================================

CREATE TABLE IF NOT EXISTS equipment (
    id                      VARCHAR(36)     NOT NULL,
    category                VARCHAR(50)     NOT NULL COMMENT 'camera|lens|tripod|lighting|bag|accessory',
    brand                   VARCHAR(100)    NOT NULL,
    model                   VARCHAR(200)    NOT NULL,
    serial_number           VARCHAR(100),

    -- Compra
    purchase_date           DATE,
    purchase_price          DECIMAL(12, 2),

    -- Estado
    -- Nota: 'condition' es palabra reservada en MySQL/MariaDB; usamos equipment_condition
    equipment_condition     VARCHAR(20)     NOT NULL DEFAULT 'good'
                            COMMENT 'excellent|good|fair|poor',
    status                  VARCHAR(20)     NOT NULL DEFAULT 'active'
                            COMMENT 'active|repair|lost|stolen|sold',

    -- Garantía
    warranty_has            TINYINT(1)      NOT NULL DEFAULT 0,
    warranty_expiry         DATE,
    warranty_provider       VARCHAR(200),

    -- Seguro
    insurance_has           TINYINT(1)      NOT NULL DEFAULT 0,
    insurance_provider      VARCHAR(200),
    insurance_policy_number VARCHAR(100),
    insurance_expiry        DATE,

    -- Reporte de incidente (perdido / robado)
    report_date             DATE,
    report_details          TEXT,

    -- Metadatos
    notes                   TEXT,
    created_at              TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    INDEX idx_category (category),
    INDEX idx_status   (status)
)
ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COMMENT='Inventario de equipo fotográfico';

-- ============================================================
--  Usuarios autenticados via Google OAuth
-- ============================================================
CREATE TABLE IF NOT EXISTS users (
    id          VARCHAR(36)  NOT NULL,
    google_id   VARCHAR(100) NOT NULL,
    email       VARCHAR(255) NOT NULL,
    name        VARCHAR(255) NOT NULL,
    picture     VARCHAR(500),
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_login  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_google_id (google_id)
)
ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COMMENT='Usuarios registrados'
