-- Preload a resident user and its house binding for end-to-end test
INSERT INTO user (id, phone, password, name, type, status, created_at)
VALUES (1, '10000000001', 'pwd', 'Test Resident', 'resident', 1, NOW());

-- Ensure a house exists for the resident
INSERT INTO house (id, building_id, house_no, created_at) VALUES (1, 1, '101', NOW());

-- Bind resident to a house (resident_house table)
INSERT INTO resident_house (user_id, house_id, created_at) VALUES (1, 1, NOW());
