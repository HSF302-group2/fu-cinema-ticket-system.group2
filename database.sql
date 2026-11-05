-- ==========================================================
-- FU CINEMA TICKET SYSTEM - DATABASE SCRIPT (HSF302)
-- ==========================================================

-- 1. Tao Database (Chay lenh nay dau tien trong SSMS neu chua co)
IF NOT EXISTS (SELECT * FROM sys.databases WHERE name = 'FUCinemaTicketDB')
BEGIN
    CREATE DATABASE FUCinemaTicketDB;
    PRINT 'Da tao Database FUCinemaTicketDB thanh cong!';
END
GO

USE FUCinemaTicketDB;
GO

-- LUU Y:
-- Du an nay su dung Spring Boot JPA / Hibernate (ddl-auto = update)
-- va DataSeeder.java tu dong tao bang & insert toan bo du lieu mau
-- (Admin, Khach hang, The loai, Phim, Phong chieu, Suat chieu, Don ve)
-- ngay khi ung dung duoc chay lan dau tien.

-- Duoi day la cac cau lenh truy van kiem tra du lieu sau khi chay app:
-- SELECT * FROM accounts;
-- SELECT * FROM customers;
-- SELECT * FROM genres;
-- SELECT * FROM movies;
-- SELECT * FROM rooms;
-- SELECT * FROM showtimes;
-- SELECT * FROM bookings;
-- SELECT * FROM tickets;
-- SELECT * FROM reviews;
