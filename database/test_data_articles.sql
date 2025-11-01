-- ============================================
-- 测试文章数据插入脚本
-- 用于测试评论功能
-- ============================================

USE `content_hub`;

-- 插入测试文章（如果不存在）
-- 注意：需要先确保用户ID 1存在（admin用户）

INSERT INTO `articles` (
    `id`,
    `user_id`,
    `category_id`,
    `title`,
    `slug`,
    `summary`,
    `cover_image`,
    `content`,
    `html_content`,
    `status`,
    `view_count`,
    `like_count`,
    `comment_count`,
    `collect_count`,
    `share_count`,
    `is_top`,
    `is_recommend`,
    `published_at`,
    `created_at`,
    `updated_at`,
    `deleted`
) VALUES
(
    1,
    1,
    1,
    '测试文章1：Spring Boot微服务架构实践',
    'spring-boot-microservices-practice',
    '本文介绍如何使用Spring Boot构建微服务架构，包括服务拆分、服务注册与发现、配置管理等内容。',
    NULL,
    '# Spring Boot微服务架构实践\n\n## 1. 微服务架构概述\n\n微服务架构是一种将单一应用程序开发为一套小型服务的方法，每个服务运行在自己的进程中，并通过轻量级机制（通常是HTTP RESTful API）进行通信。\n\n## 2. 服务拆分原则\n\n- 单一职责原则\n- 高内聚低耦合\n- 业务领域驱动\n\n## 3. Spring Cloud组件\n\n- Nacos：服务注册与发现\n- Sentinel：流量控制\n- Feign：服务间通信\n\n这篇文章用于测试评论功能。',
    NULL,
    1,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    NOW(),
    NOW(),
    NOW(),
    0
),
(
    2,
    1,
    2,
    '测试文章2：生活中的小确幸',
    'little-happiness-in-life',
    '记录生活中那些让人感到温暖和快乐的小事。',
    NULL,
    '# 生活中的小确幸\n\n生活中有很多小确幸，只要我们用心去发现。\n\n## 早上的第一杯咖啡\n\n每天早上醒来，冲泡一杯香浓的咖啡，是开启美好一天的方式。\n\n## 阳光洒进窗户\n\n阳光透过窗户洒在书桌上，温暖而明亮。\n\n这篇文章用于测试评论功能。',
    NULL,
    1,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    NOW(),
    NOW(),
    NOW(),
    0
),
(
    3,
    1,
    3,
    '测试文章3：职场新人指南',
    'newcomer-guide-to-workplace',
    '分享一些职场新人的经验，帮助新人快速适应职场环境。',
    NULL,
    '# 职场新人指南\n\n作为职场新人，如何快速适应工作环境，建立良好的人际关系？\n\n## 1. 主动学习\n\n保持学习的心态，不断学习新知识和技能。\n\n## 2. 积极沟通\n\n与同事保持良好沟通，建立信任关系。\n\n## 3. 承担责任\n\n认真对待每一个任务，勇于承担责任。\n\n这篇文章用于测试评论功能。',
    NULL,
    1,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    NOW(),
    NOW(),
    NOW(),
    0
)
ON DUPLICATE KEY UPDATE
    `title` = VALUES(`title`),
    `content` = VALUES(`content`),
    `summary` = VALUES(`summary`),
    `status` = VALUES(`status`),
    `published_at` = VALUES(`published_at`),
    `updated_at` = NOW();

-- 查询插入的文章ID
SELECT `id`, `title`, `user_id`, `status` FROM `articles` WHERE `id` IN (1, 2, 3);

