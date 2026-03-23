INSERT OR IGNORE INTO finance_category(category_type, category_name, is_default, created_at)
VALUES
('INCOME',  '门诊诊疗', 1, datetime('now')),
('INCOME',  '药品销售', 1, datetime('now')),
('EXPENSE', '药品采购', 1, datetime('now')),
('EXPENSE', '房租水电', 1, datetime('now')),
('EXPENSE', '其他支出', 1, datetime('now'));