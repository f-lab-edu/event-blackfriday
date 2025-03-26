IF EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.SETTINGS WHERE NAME = 'MODE' AND VALUE != 'MySQL') THEN
RETURN;
END IF;

-- MySQL 전용 코드
UPDATE products p
JOIN categories brand_cat ON p.category_id = brand_cat.id
JOIN categories parent_cat ON brand_cat.parent_id = parent_cat.id
JOIN brands b ON brand_cat.name = b.name
SET
  p.category_id = parent_cat.id,
  p.brand_id = b.id,
  p.updated_at = NOW()
WHERE brand_cat.depth = 3
AND brand_cat.name IN ('애플', '삼성전자', 'LG전자', 'HP', 'OPPO', '구글', '델',
                    '레노버', '마이크로소프트', '모토로라', '비보', '샤오미',
                    '화웨이', '원플러스', 'MSI', '에이서');
