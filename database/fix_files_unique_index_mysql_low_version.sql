-- ============================================
-- 修复files表的唯一索引问题（MySQL 8.0.13以下版本）
-- 方案：完全删除唯一索引，依赖代码逻辑保证头像唯一性
-- ============================================

USE `content_hub`;

-- 1. 删除旧的唯一索引
DROP INDEX IF EXISTS `uk_user_avatar` ON `files`;

SELECT '====== 已删除旧的唯一索引 ======' AS info;

-- 2. 验证索引已删除
SELECT 
    TABLE_NAME,
    INDEX_NAME,
    COLUMN_NAME
FROM information_schema.STATISTICS
WHERE TABLE_SCHEMA = 'content_hub'
  AND TABLE_NAME = 'files'
  AND INDEX_NAME = 'uk_user_avatar';

SELECT '====== 修复完成！现在可以上传多个同类型的文件 ======' AS info;
SELECT '====== 注意：头像的唯一性现在完全由代码逻辑保证（uploadAvatar方法） ======' AS info;

-- 说明：
-- 1. 删除唯一索引后，所有文件类型都可以上传多个
-- 2. 头像的唯一性由 FileServiceImpl.uploadAvatar() 方法的逻辑保证：
--    - 上传前会查询是否存在旧头像
--    - 如果存在，会先删除MinIO中的旧文件，然后更新数据库记录
--    - 如果不存在，才会插入新记录
-- 3. 这种方式不需要数据库层面的约束，但需要确保代码逻辑正确

