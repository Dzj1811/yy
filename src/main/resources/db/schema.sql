PRAGMA foreign_keys = ON;

CREATE TABLE IF NOT EXISTS patient (
                                       id                INTEGER PRIMARY KEY AUTOINCREMENT,
                                       patient_code      TEXT NOT NULL UNIQUE,
                                       name              TEXT NOT NULL,
                                       gender            TEXT,
                                       birth_date        TEXT,
                                       phone             TEXT,
                                       id_card           TEXT,
                                       address           TEXT,
                                       allergy_note      TEXT,
                                       chronic_note      TEXT,
                                       remark            TEXT,
                                       created_at        TEXT NOT NULL,
                                       updated_at        TEXT NOT NULL,
                                       is_deleted        INTEGER NOT NULL DEFAULT 0
);
CREATE INDEX IF NOT EXISTS idx_patient_name ON patient(name);
CREATE INDEX IF NOT EXISTS idx_patient_phone ON patient(phone);

CREATE TABLE IF NOT EXISTS medicine (
                                        id                INTEGER PRIMARY KEY AUTOINCREMENT,
                                        medicine_code     TEXT NOT NULL UNIQUE,
                                        name              TEXT NOT NULL,
                                        spec              TEXT,
                                        unit              TEXT NOT NULL,
                                        category          TEXT,
                                        purchase_price    REAL NOT NULL DEFAULT 0,
                                        sale_price        REAL NOT NULL DEFAULT 0,
                                        stock_qty         REAL NOT NULL DEFAULT 0,
                                        min_stock_qty     REAL NOT NULL DEFAULT 0,
                                        supplier          TEXT,
                                        expire_date       TEXT,
                                        remark            TEXT,
                                        created_at        TEXT NOT NULL,
                                        updated_at        TEXT NOT NULL,
                                        is_deleted        INTEGER NOT NULL DEFAULT 0
);
CREATE INDEX IF NOT EXISTS idx_medicine_name ON medicine(name);
CREATE INDEX IF NOT EXISTS idx_medicine_stock ON medicine(stock_qty);

CREATE TABLE IF NOT EXISTS visit_record (
                                            id                INTEGER PRIMARY KEY AUTOINCREMENT,
                                            visit_no          TEXT NOT NULL UNIQUE,
                                            patient_id        INTEGER NOT NULL,
                                            visit_date        TEXT NOT NULL,
                                            complaint         TEXT,
                                            diagnosis         TEXT,
                                            treatment_plan    TEXT,
                                            advice            TEXT,
                                            remark            TEXT,
                                            created_at        TEXT NOT NULL,
                                            updated_at        TEXT NOT NULL,
                                            FOREIGN KEY(patient_id) REFERENCES patient(id)
    );
CREATE INDEX IF NOT EXISTS idx_visit_patient_id ON visit_record(patient_id);
CREATE INDEX IF NOT EXISTS idx_visit_date ON visit_record(visit_date);

CREATE TABLE IF NOT EXISTS visit_prescription_item (
                                                       id                INTEGER PRIMARY KEY AUTOINCREMENT,
                                                       visit_id          INTEGER NOT NULL,
                                                       medicine_id       INTEGER NOT NULL,
                                                       quantity          REAL NOT NULL,
                                                       unit              TEXT,
                                                       unit_price        REAL NOT NULL,
                                                       amount            REAL NOT NULL,
                                                       usage_note        TEXT,
                                                       created_at        TEXT NOT NULL,
                                                       FOREIGN KEY(visit_id) REFERENCES visit_record(id),
    FOREIGN KEY(medicine_id) REFERENCES medicine(id)
    );
CREATE INDEX IF NOT EXISTS idx_vpi_visit_id ON visit_prescription_item(visit_id);

CREATE TABLE IF NOT EXISTS inventory_txn (
                                             id                INTEGER PRIMARY KEY AUTOINCREMENT,
                                             txn_no            TEXT NOT NULL UNIQUE,
                                             medicine_id       INTEGER NOT NULL,
                                             txn_type          TEXT NOT NULL,      -- IN / OUT / ADJUST
                                             quantity          REAL NOT NULL,
                                             unit_price        REAL,
                                             amount            REAL,
                                             related_visit_id  INTEGER,
                                             txn_time          TEXT NOT NULL,
                                             operator          TEXT,
                                             remark            TEXT,
                                             created_at        TEXT NOT NULL,
                                             FOREIGN KEY(medicine_id) REFERENCES medicine(id),
    FOREIGN KEY(related_visit_id) REFERENCES visit_record(id)
    );
CREATE INDEX IF NOT EXISTS idx_inventory_medicine_id ON inventory_txn(medicine_id);
CREATE INDEX IF NOT EXISTS idx_inventory_time ON inventory_txn(txn_time);

CREATE TABLE IF NOT EXISTS finance_category (
                                                id                INTEGER PRIMARY KEY AUTOINCREMENT,
                                                category_type     TEXT NOT NULL,      -- INCOME / EXPENSE
                                                category_name     TEXT NOT NULL,
                                                is_default        INTEGER NOT NULL DEFAULT 0,
                                                created_at        TEXT NOT NULL
);
CREATE UNIQUE INDEX IF NOT EXISTS uk_fin_cat_type_name
    ON finance_category(category_type, category_name);

CREATE TABLE IF NOT EXISTS finance_txn (
                                           id                INTEGER PRIMARY KEY AUTOINCREMENT,
                                           txn_no            TEXT NOT NULL UNIQUE,
                                           txn_type          TEXT NOT NULL,      -- INCOME / EXPENSE
                                           category_id       INTEGER NOT NULL,
                                           amount            REAL NOT NULL,
                                           txn_date          TEXT NOT NULL,
                                           related_visit_id  INTEGER,
                                           payment_method    TEXT,
                                           counterparty      TEXT,
                                           remark            TEXT,
                                           created_at        TEXT NOT NULL,
                                           FOREIGN KEY(category_id) REFERENCES finance_category(id),
    FOREIGN KEY(related_visit_id) REFERENCES visit_record(id)
    );
CREATE INDEX IF NOT EXISTS idx_finance_date ON finance_txn(txn_date);
CREATE INDEX IF NOT EXISTS idx_finance_type ON finance_txn(txn_type);