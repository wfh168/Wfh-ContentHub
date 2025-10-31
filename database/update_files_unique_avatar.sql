-- 为files表添加唯一索引，确保一个用户只有一个头像记录
-- 执行前自动清理重复数据

-- 1. 查看是否有重复的头像记录（可选，用于检查）
SELECT '====== 重复的头像记录 ======' AS info;
SELECT user_id, COUNT(*) as count 
FROM files 
WHERE file_category = 'AVATAR' AND deleted = 0
GROUP BY user_id 
HAVING count > 1;

-- 2. 删除重复记录，保留ID最大的（最新的）
SELECT '====== 开始清理重复记录 ======' AS info;

DELETE f1 FROM files f1
INNER JOIN (
    SELECT user_id, MAX(id) as max_id
    FROM files
    WHERE file_category = 'AVATAR' AND deleted = 0
    GROUP BY user_id
    HAVING COUNT(*) > 1
) f2 ON f1.user_id = f2.user_id
WHERE f1.file_category = 'AVATAR' 
  AND f1.deleted = 0
  AND f1.id < f2.max_id;

SELECT '====== 清理完成 ======' AS info;

-- 3. 验证清理结果
SELECT '====== 验证：应该没有重复记录了 ======' AS info;
SELECT user_id, COUNT(*) as count 
FROM files 
WHERE file_category = 'AVATAR' AND deleted = 0
GROUP BY user_id 
HAVING count > 1;

-- 4. 添加唯一索引
SELECT '====== 添加唯一索引 ======' AS info;

-- 先检查索引是否已存在，如果存在则删除
DROP INDEX IF EXISTS uk_user_avatar ON files;

-- 添加新的唯一索引
ALTER TABLE files 
ADD UNIQUE INDEX uk_user_avatar (user_id, file_category, deleted) 
USING BTREE 
COMMENT '用户头像唯一索引（一个用户只能有一个头像记录）';

SELECT '====== 完成！唯一索引已添加 ======' AS info;

-- 注意：这个唯一索引包含了deleted字段，确保软删除后可以重新上传

