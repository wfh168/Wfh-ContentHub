-- ============================================
-- 修复files表的唯一索引问题
-- 问题：唯一索引 uk_user_avatar (user_id, file_category, deleted) 
--       对所有文件类型都生效，导致无法上传多个同类型的文件（如多个图片）
-- 解决方案：修改唯一索引，只对 AVATAR 类型应用唯一约束
-- ============================================

USE `content_hub`;

-- 1. 删除旧的唯一索引
DROP INDEX IF EXISTS `uk_user_avatar` ON `files`;

SELECT '====== 已删除旧的唯一索引 ======' AS info;

-- 2. 方案一：使用虚拟列创建部分索引（MySQL 8.0.13+支持）
-- 创建一个虚拟列，只对 AVATAR 类型的记录有值
ALTER TABLE `files` 
ADD COLUMN `avatar_user_id` BIGINT AS (CASE WHEN `file_category` = 'AVATAR' THEN `user_id` ELSE NULL END) VIRTUAL;

-- 在虚拟列上创建唯一索引（只对 AVATAR 类型生效）
ALTER TABLE `files` 
ADD UNIQUE INDEX `uk_user_avatar` (`avatar_user_id`, `deleted`);

SELECT '====== 已创建新的唯一索引（使用虚拟列） ======' AS info;

-- 3. 验证索引是否正确
SELECT 
    TABLE_NAME,
    INDEX_NAME,
    COLUMN_NAME,
    INDEX_TYPE
FROM information_schema.STATISTICS
WHERE TABLE_SCHEMA = 'content_hub'
  AND TABLE_NAME = 'files'
  AND INDEX_NAME = 'uk_user_avatar';

SELECT '====== 修复完成！现在可以上传多个同类型的文件（除头像外） ======' AS info;

-- 注意：
-- 1. 头像仍然只能有一个（通过唯一索引保证）
-- 2. 其他文件类型（IMAGE, DOCUMENT等）可以上传多个
-- 3. 如果MySQL版本低于8.0.13，请使用下面的替代方案

