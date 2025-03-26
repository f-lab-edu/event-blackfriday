-- 예: 검색 성능 향상을 위한 H2 전용 인덱스
CREATE INDEX IF NOT EXISTS idx_categories_name_depth ON categories(name, depth);
