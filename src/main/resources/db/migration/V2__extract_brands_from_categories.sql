-- 1. 브랜드 데이터 추출 및 중복 제거
INSERT INTO brands (name, created_at, updated_at)
SELECT DISTINCT c.name, NOW(), NOW()
FROM categories c
WHERE c.depth = 3
AND c.name IN ('애플', '삼성전자', 'LG전자', 'HP', 'OPPO', '구글', '델',
              '레노버', '마이크로소프트', '모토로라', '비보', '샤오미',
              '화웨이', '원플러스', 'MSI', '에이서');

-- 2. 카테고리-브랜드 매핑 생성
INSERT INTO category_brand_mappings (category_id, brand_id, created_at, updated_at)
SELECT
    parent.id AS category_id,
    b.id AS brand_id,
    NOW() AS created_at,
    NOW() AS updated_at
FROM categories c
JOIN categories parent ON c.parent_id = parent.id
JOIN brands b ON c.name = b.name
WHERE c.depth = 3
AND c.name IN ('애플', '삼성전자', 'LG전자', 'HP', 'OPPO', '구글', '델',
              '레노버', '마이크로소프트', '모토로라', '비보', '샤오미',
              '화웨이', '원플러스', 'MSI', '에이서');

-- 3. 제품 테이블 업데이트 (H2 호환 버전)
UPDATE products
SET
    category_id = (
        SELECT parent_cat.id
        FROM categories brand_cat
        JOIN categories parent_cat ON brand_cat.parent_id = parent_cat.id
        WHERE brand_cat.id = products.category_id
        AND brand_cat.depth = 3
        AND brand_cat.name IN ('애플', '삼성전자', 'LG전자', 'HP', 'OPPO', '구글', '델',
                            '레노버', '마이크로소프트', '모토로라', '비보', '샤오미',
                            '화웨이', '원플러스', 'MSI', '에이서')
    ),
    brand_id = (
        SELECT b.id
        FROM categories brand_cat
        JOIN brands b ON brand_cat.name = b.name
        WHERE brand_cat.id = products.category_id
        AND brand_cat.depth = 3
    ),
    updated_at = NOW()
WHERE category_id IN (
    SELECT id
    FROM categories
    WHERE depth = 3
    AND name IN ('애플', '삼성전자', 'LG전자', 'HP', 'OPPO', '구글', '델',
                '레노버', '마이크로소프트', '모토로라', '비보', '샤오미',
                '화웨이', '원플러스', 'MSI', '에이서')
);

-- 4. 브랜드로 사용된 카테고리 삭제 표시
UPDATE categories
SET
    is_deleted = true,
    updated_at = NOW()
WHERE depth = 3
AND name IN ('애플', '삼성전자', 'LG전자', 'HP', 'OPPO', '구글', '델',
            '레노버', '마이크로소프트', '모토로라', '비보', '샤오미',
            '화웨이', '원플러스', 'MSI', '에이서');

-- 5. 카테고리 클로저 테이블에서 삭제된 카테고리 관련 항목 제거
DELETE FROM category_closure
WHERE ancestor_id IN (
    SELECT id FROM categories
    WHERE depth = 3
    AND is_deleted = true
    AND name IN ('애플', '삼성전자', 'LG전자', 'HP', 'OPPO', '구글', '델',
                '레노버', '마이크로소프트', '모토로라', '비보', '샤오미',
                '화웨이', '원플러스', 'MSI', '에이서')
)
OR descendant_id IN (
    SELECT id FROM categories
    WHERE depth = 3
    AND is_deleted = true
    AND name IN ('애플', '삼성전자', 'LG전자', 'HP', 'OPPO', '구글', '델',
                '레노버', '마이크로소프트', '모토로라', '비보', '샤오미',
                '화웨이', '원플러스', 'MSI', '에이서')
);
