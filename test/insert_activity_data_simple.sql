USE future_community;

INSERT INTO activity (title, description, poster_url, start_time, end_time, location, quota, enrolled_count, status, created_at, updated_at) 
VALUES 
('Community Yoga Class', 'Weekend yoga session for all levels. Bring your own mat.', 'https://images.unsplash.com/photo-1544367563-12123d8965cd?w=800', '2026-03-15 09:00:00', '2026-03-15 10:30:00', 'Community Fitness Center', 30, 12, 0, NOW(), NOW()),
('Spring Family Sports Day', 'Fun sports activities for families. Relay races, tug of war and more!', 'https://images.unsplash.com/photo-1574629810360-7efbbe195018?w=800', '2026-03-16 14:00:00', '2026-03-16 17:00:00', 'Community Plaza', 50, 28, 0, NOW(), NOW()),
('Health & Wellness Seminar', 'Free health checkup and wellness advice from TCM experts.', 'https://images.unsplash.com/photo-1576091160399-112ba8d25d1d?w=800', '2026-03-18 19:00:00', '2026-03-18 20:30:00', 'Community Meeting Room', 100, 67, 0, NOW(), NOW()),
('Book Club Meeting', 'Monthly book sharing session. Bring your favorite book to share!', 'https://images.unsplash.com/photo-1524995997946-a1c2e315a42f?w=800', '2026-03-22 15:00:00', '2026-03-22 17:00:00', 'Community Library', 40, 15, 0, NOW(), NOW()),
('Cooking Competition', 'Show off your cooking skills and win prizes!', 'https://images.unsplash.com/photo-1556910103-1c02745a30bf?w=800', '2026-03-25 10:00:00', '2026-03-25 12:00:00', 'Community Activity Center', 20, 18, 0, NOW(), NOW());

SELECT id, title, location, quota, enrolled_count, (quota - enrolled_count) as remaining FROM activity;
