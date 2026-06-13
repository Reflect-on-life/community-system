-- 插入活动测试数据
USE future_community;

-- 先清空现有数据（可选）
-- TRUNCATE TABLE activity_comment;
-- TRUNCATE TABLE activity_photo;
-- TRUNCATE TABLE activity_signup;
-- TRUNCATE TABLE activity;

-- 插入测试活动数据
INSERT INTO activity (title, description, poster_url, start_time, end_time, location, quota, enrolled_count, status, created_at, updated_at) 
VALUES 
('社区周末瑜伽课', '欢迎各位邻居参加本周六的公益瑜伽课，专业教练指导，适合各年龄段。请自带瑜伽垫。', 'https://images.unsplash.com/photo-1544367563-12123d8965cd?w=800', '2026-03-15 09:00:00', '2026-03-15 10:30:00', '社区健身中心一楼', 30, 12, 0, NOW(), NOW()),
('春季亲子运动会', '春季亲子运动会开始报名啦！有趣味接力、拔河比赛等项目，欢迎家长带小朋友一起参加。', 'https://images.unsplash.com/photo-1574629810360-7efbbe195018?w=800', '2026-03-16 14:00:00', '2026-03-16 17:00:00', '社区中心广场', 50, 28, 0, NOW(), NOW()),
('健康养生讲座', '邀请知名中医专家为大家讲解春季养生知识，现场还有免费把脉问诊。', 'https://images.unsplash.com/photo-1576091160399-112ba8d25d1d?w=800', '2026-03-18 19:00:00', '2026-03-18 20:30:00', '社区会议室', 100, 67, 0, NOW(), NOW()),
('社区读书分享会', '每月一度的读书分享会，本期主题：'我最喜欢的一本书'。欢迎大家带上自己的好书来分享。', 'https://images.unsplash.com/photo-1524995997946-a1c2e315a42f?w=800', '2026-03-22 15:00:00', '2026-03-22 17:00:00', '社区图书馆', 40, 15, 0, NOW(), NOW()),
('美食制作大赛', '社区美食制作大赛开始报名！展示你的厨艺，赢取精美奖品。', 'https://images.unsplash.com/photo-1556910103-1c02745a30bf?w=800', '2026-03-25 10:00:00', '2026-03-25 12:00:00', '社区活动中心', 20, 18, 0, NOW(), NOW());

-- 查看插入的数据
SELECT id, title, location, quota, enrolled_count, 
       (quota - enrolled_count) as remaining_slots,
       CASE status 
           WHEN 0 THEN '未开始'
           WHEN 1 THEN '进行中'
           WHEN 2 THEN '已结束'
       END as status_text
FROM activity
ORDER BY created_at DESC;
