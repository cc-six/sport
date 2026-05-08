-- Test data (quote reserved words: USER, ORDER)
INSERT INTO role (id, name) VALUES (1, 'admin'), (2, 'user');

INSERT INTO "user" (id, username, password, phone, role_id) VALUES
(1, 'admin', 'admin123', '13800000001', 1),
(2, 'testuser', 'user123', '13800000002', 2),
(3, 'user2', 'user456', '13800000003', 2);

INSERT INTO venue (id, name, type, status, open_time, close_time, price_per_hour) VALUES
(1, '羽毛球场地1', 'badminton', 1, '08:00:00', '22:00:00', 50.00),
(2, '羽毛球场地2', 'badminton', 1, '08:00:00', '22:00:00', 60.00),
(3, '篮球馆', 'basketball', 1, '09:00:00', '21:00:00', 200.00),
(4, '乒乓球桌1', 'tabletennis', 1, '08:00:00', '20:00:00', 30.00),
(5, '网球场', 'tennis', 0, '06:00:00', '18:00:00', 100.00);

INSERT INTO equipment (id, name, total_qty, available_qty, price_per_hour) VALUES
(1, '羽毛球拍', 20, 15, 10.00),
(2, '篮球', 10, 8, 15.00),
(3, '乒乓球拍', 15, 10, 5.00);
