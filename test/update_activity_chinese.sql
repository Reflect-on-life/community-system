USE future_community;

-- 更新活动数据为中文
UPDATE activity SET 
    title = '社区周末瑜伽课',
    description = '欢迎各位邻居参加本周六的公益瑜伽课，专业教练指导，适合各年龄段。请自带瑜伽垫。',
    location = '社区健身中心一楼',
    poster_url = 'https://images.unsplash.com/photo-1544367563-12123d8965cd?w=800&h=600&fit=crop'
WHERE id = 1;

UPDATE activity SET 
    title = '春季亲子运动会',
    description = '春季亲子运动会开始报名啦！有趣味接力、拔河比赛等项目，欢迎家长带小朋友一起参加。',
    location = '社区中心广场',
    poster_url = 'https://images.unsplash.com/photo-1574629810360-7efbbe195018?w=800&h=600&fit=crop'
WHERE id = 2;

UPDATE activity SET 
    title = '健康养生讲座',
    description = '邀请知名中医专家为大家讲解春季养生知识，现场还有免费把脉问诊。',
    location = '社区会议室',
    poster_url = 'https://images.unsplash.com/photo-1576091160399-112ba8d25d1d?w=800&h=600&fit=crop'
WHERE id = 3;

UPDATE activity SET 
    title = '社区读书分享会',
    description = '每月一度的读书分享会，本期主题：我最喜欢的一本书。欢迎大家带上自己的好书来分享。',
    location = '社区图书馆',
    poster_url = 'https://images.unsplash.com/photo-1524995997946-a1c2e315a42f?w=800&h=600&fit=crop'
WHERE id = 4;

UPDATE activity SET 
    title = '美食制作大赛',
    description = '社区美食制作大赛开始报名！展示你的厨艺，赢取精美奖品。',
    location = '社区活动中心',
    poster_url = 'https://images.unsplash.com/photo-1556910103-1c02745a30bf?w=800&h=600&fit=crop'
WHERE id = 5;

-- 查看更新后的数据
SELECT id, title, description, location, poster_url FROM activity;
