-- ============================================
-- 修复files表索引
-- 删除所有索引后重新创建，修复唯一索引问题
-- ============================================

USE `content_hub`;

-- 1. 删除旧的唯一索引（如果存在）
SET @index_exists = 0;
SELECT COUNT(*) INTO @index_exists
FROM information_schema.STATISTICS
WHERE TABLE_SCHEMA = 'content_hub'
  AND TABLE_NAME = 'files'
  AND INDEX_NAME = 'uk_user_avatar';

SET @sql = IF(@index_exists > 0,
    'ALTER TABLE `files` DROP INDEX `uk_user_avatar`;',
    'SELECT ''索引 uk_user_avatar 不存在，跳过删除'' AS info;'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SELECT '====== 已删除旧的唯一索引 uk_user_avatar ======' AS info;

-- 2. 删除其他普通索引（保留主键和外键）
-- 使用存储过程来安全删除索引
DELIMITER $$

CREATE PROCEDURE IF NOT EXISTS drop_index_if_exists(
    IN table_name VARCHAR(64),
    IN index_name VARCHAR(64)
)
BEGIN
    DECLARE index_count INT DEFAULT 0;
    SELECT COUNT(*) INTO index_count
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = table_name
      AND INDEX_NAME = index_name;
    
    IF index_count > 0 THEN
        SET @sql = CONCAT('ALTER TABLE `', table_name, '` DROP INDEX `', index_name, '`');
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END$$

DELIMITER ;

-- 删除普通索引
CALL drop_index_if_exists('files', 'idx_created_at');
CALL drop_index_if_exists('files', 'idx_file_category');
CALL drop_index_if_exists('files', 'idx_file_type');
CALL drop_index_if_exists('files', 'idx_status');
CALL drop_index_if_exists('files', 'idx_user_id');

-- 删除临时存储过程
DROP PROCEDURE IF EXISTS drop_index_if_exists;

SELECT '====== 已删除所有普通索引 ======' AS info;

-- 3. 检查虚拟列是否存在，如果不存在则创建
SET @col_exists = 0;
SELECT COUNT(*) INTO @col_exists
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = 'content_hub'
  AND TABLE_NAME = 'files'
  AND COLUMN_NAME = 'avatar_user_id';

SET @sql = IF(@col_exists = 0,
    'ALTER TABLE `files` ADD COLUMN `avatar_user_id` BIGINT AS (CASE WHEN `file_category` = ''AVATAR'' THEN `user_id` ELSE NULL END) VIRTUAL COMMENT ''虚拟列：只在AVATAR类型时有值'';',
    'SELECT ''====== 虚拟列 avatar_user_id 已存在，跳过创建 ======'' AS info;'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 4. 在虚拟列上创建新的唯一索引（只对 AVATAR 类型生效）
-- 这样每个用户只能有一个AVATAR类型的文件，但可以有多个IMAGE、DOCUMENT等其他类型
-- 注意：先检查索引是否已存在，避免重复创建
SET @index_exists = 0;
SELECT COUNT(*) INTO @index_exists
FROM information_schema.STATISTICS
WHERE TABLE_SCHEMA = 'content_hub'
  AND TABLE_NAME = 'files'
  AND INDEX_NAME = 'uk_user_avatar';

SET @sql = IF(@index_exists = 0,
    'ALTER TABLE `files` ADD UNIQUE INDEX `uk_user_avatar` (`avatar_user_id`, `deleted`) COMMENT ''用户头像唯一索引（只对AVATAR类型生效，确保一个用户只有一个头像）'';',
    'SELECT ''唯一索引 uk_user_avatar 已存在，跳过创建'' AS info;'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SELECT '====== 已创建新的唯一索引 uk_user_avatar（基于虚拟列） ======' AS info;

-- 5. 重新创建所有普通索引（如果不存在则创建）
DELIMITER $$

CREATE PROCEDURE IF NOT EXISTS add_index_if_not_exists(
    IN table_name VARCHAR(64),
    IN index_name VARCHAR(64),
    IN index_type VARCHAR(20),
    IN index_columns TEXT,
    IN index_comment TEXT
)
BEGIN
    DECLARE index_count INT DEFAULT 0;
    SELECT COUNT(*) INTO index_count
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = table_name
      AND INDEX_NAME = index_name;
    
    IF index_count = 0 THEN
        SET @sql = CONCAT('ALTER TABLE `', table_name, '` ADD ', 
                         IF(index_type = 'UNIQUE', 'UNIQUE ', ''),
                         'INDEX `', index_name, '` (', index_columns, ')',
                         IF(index_comment IS NOT NULL AND index_comment != '', 
                            CONCAT(' COMMENT ''', index_comment, ''''), 
                            ''));
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END$$

DELIMITER ;

-- 创建普通索引
CALL add_index_if_not_exists('files', 'idx_user_id', 'INDEX', '`user_id`', '用户ID索引');
CALL add_index_if_not_exists('files', 'idx_file_category', 'INDEX', '`file_category`', '文件分类索引');
CALL add_index_if_not_exists('files', 'idx_file_type', 'INDEX', '`file_type`', '文件类型索引');
CALL add_index_if_not_exists('files', 'idx_status', 'INDEX', '`status`', '状态索引');
CALL add_index_if_not_exists('files', 'idx_created_at', 'INDEX', '`created_at`', '创建时间索引');

-- 删除临时存储过程
DROP PROCEDURE IF EXISTS add_index_if_not_exists;

SELECT '====== 已重新创建所有普通索引 ======' AS info;

-- 6. 验证索引创建结果
SELECT 
    '====== 索引创建结果 ======' AS info;

SELECT 
    INDEX_NAME AS '索引名称',
    GROUP_CONCAT(COLUMN_NAME ORDER BY SEQ_IN_INDEX SEPARATOR ', ') AS '索引字段',
    INDEX_TYPE AS '索引类型',
    NON_UNIQUE AS '是否唯一(0=唯一,1=普通)'
FROM information_schema.STATISTICS
WHERE TABLE_SCHEMA = 'content_hub'
  AND TABLE_NAME = 'files'
GROUP BY INDEX_NAME, INDEX_TYPE, NON_UNIQUE
ORDER BY INDEX_NAME;

-- 7. 验证虚拟列
SELECT 
    '====== 虚拟列信息 ======' AS info;

SELECT 
    COLUMN_NAME AS '列名',
    COLUMN_TYPE AS '列类型',
    EXTRA AS '额外信息',
    COLUMN_DEFAULT AS '默认值'
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = 'content_hub'
  AND TABLE_NAME = 'files'
  AND COLUMN_NAME = 'avatar_user_id';

SELECT '====== 修复完成！现在可以上传多个同类型的文件（除头像外） ======' AS info;

-- 说明：
-- 1. 新的唯一索引 uk_user_avatar 只对 AVATAR 类型生效（通过虚拟列实现）
-- 2. 用户可以上传多个 IMAGE、DOCUMENT、VIDEO 等类型的文件
-- 3. 每个用户仍然只能有一个头像（AVATAR类型）
-- 4. 所有普通索引已重新创建，查询性能不受影响

