CREATE DATABASE YourChoiceShopDB;
GO
USE YourChoiceShopDB;
GO
CREATE TABLE thuong_hieu (
    id INT PRIMARY KEY IDENTITY(1,1),
    ma_thuong_hieu VARCHAR(50) UNIQUE,
    ten_thuong_hieu NVARCHAR(255),
    trang_thai INT
);

CREATE TABLE xuat_xu (
    id INT PRIMARY KEY IDENTITY(1,1),
    ma_xuat_xu VARCHAR(50) UNIQUE,
    ten_xuat_xu NVARCHAR(255),
    trang_thai INT
);

CREATE TABLE chat_lieu (
    id INT PRIMARY KEY IDENTITY(1,1),
    ma_chat_lieu VARCHAR(50) UNIQUE,
    ten_chat_lieu NVARCHAR(255),
    trang_thai INT
);

CREATE TABLE co_ao (
    id INT PRIMARY KEY IDENTITY(1,1),
    ma_co_ao VARCHAR(50) UNIQUE,
    ten_co_ao NVARCHAR(255),
    trang_thai INT
);

CREATE TABLE tay_ao (
    id INT PRIMARY KEY IDENTITY(1,1),
    ma_tay_ao VARCHAR(50) UNIQUE,
    ten_tay_ao NVARCHAR(255),
    trang_thai INT
);

CREATE TABLE mau_sac (
    id INT PRIMARY KEY IDENTITY(1,1),
    ma_mau_sac VARCHAR(50) UNIQUE,
    ten_mau_sac NVARCHAR(255),
    trang_thai INT
);

CREATE TABLE kich_thuoc (
    id INT PRIMARY KEY IDENTITY(1,1),
    ma_kich_thuoc VARCHAR(50) UNIQUE,
    ten_kich_thuoc NVARCHAR(255),
    trang_thai INT
);

CREATE TABLE quyen_han (
    id INT PRIMARY KEY IDENTITY(1,1),
    ten_quyen_han NVARCHAR(100),
    trang_thai INT
);

CREATE TABLE phuong_thuc_thanh_toan (
    id INT PRIMARY KEY IDENTITY(1,1),
    ma_phuong_thuc VARCHAR(50) UNIQUE,
    ten_phuong_thuc NVARCHAR(255),
    ghi_chu NVARCHAR(MAX),
    trang_thai INT
);

CREATE TABLE dot_giam_gia (
    id INT PRIMARY KEY IDENTITY(1,1),
    ma_dot_giam_gia VARCHAR(50) UNIQUE,
    ten_dot_giam_gia NVARCHAR(255),
    gia_tri_giam DECIMAL(18,2),
    loai_giam_gia NVARCHAR(50),
    ngay_bat_dau DATETIME,
    ngay_ket_thuc DATETIME,
    trang_thai INT
);

CREATE TABLE phieu_giam_gia (
    id INT PRIMARY KEY IDENTITY(1,1),
    ma_phieu_giam_gia VARCHAR(50) UNIQUE,
    ten_phieu_giam_gia NVARCHAR(255),
    loai_phieu NVARCHAR(50),
    gia_tri_giam DECIMAL(18,2),
    gia_tri_giam_toi_da DECIMAL(18,2),
    don_hang_toi_thieu DECIMAL(18,2),
    so_luong INT,
    ngay_bat_dau DATETIME,
    ngay_ket_thuc DATETIME,
    trang_thai INT
);

CREATE TABLE san_pham (
    id INT PRIMARY KEY IDENTITY(1,1),
    ma_san_pham VARCHAR(50) UNIQUE,
    ten_san_pham NVARCHAR(255),
    mo_ta_chi_tiet NVARCHAR(MAX),
    id_thuong_hieu INT FOREIGN KEY REFERENCES thuong_hieu(id),
    id_xuat_xu INT FOREIGN KEY REFERENCES xuat_xu(id),
    id_chat_lieu INT FOREIGN KEY REFERENCES chat_lieu(id),
    id_co_ao INT FOREIGN KEY REFERENCES co_ao(id),
    id_tay_ao INT FOREIGN KEY REFERENCES tay_ao(id),
    ngay_tao DATETIME,
    ngay_cap_nhat DATETIME,
    nguoi_tao NVARCHAR(100),
    nguoi_cap_nhat NVARCHAR(100),
    trang_thai INT
);

CREATE TABLE khach_hang (
    id INT PRIMARY KEY IDENTITY(1,1),
    ma_khach_hang VARCHAR(50) UNIQUE,
    ten_khach_hang NVARCHAR(255),
    ten_tai_khoan VARCHAR(100),
    mat_khau VARCHAR(255),
    email VARCHAR(255),
    so_dien_thoai VARCHAR(15),
    gioi_tinh BIT,
    ngay_sinh DATE,
    ngay_tao DATETIME,
    ngay_cap_nhat DATETIME,
    nguoi_tao NVARCHAR(100),
    nguoi_cap_nhat NVARCHAR(100),
    trang_thai INT
);

CREATE TABLE nhan_vien (
    id INT PRIMARY KEY IDENTITY(1,1),
    ma_nhan_vien VARCHAR(50) UNIQUE,
    ten_nhan_vien NVARCHAR(255),
    mat_khau VARCHAR(255),
    email VARCHAR(255),
    so_dien_thoai VARCHAR(15),
    gioi_tinh BIT,
    ngay_sinh DATE,
    dia_chi NVARCHAR(MAX),
    id_quyen_han INT FOREIGN KEY REFERENCES quyen_han(id),
    ngay_tao DATETIME,
    ngay_cap_nhat DATETIME,
    nguoi_tao NVARCHAR(100),
    nguoi_cap_nhat NVARCHAR(100),
    trang_thai INT
);

CREATE TABLE chi_tiet_san_pham (
    id INT PRIMARY KEY IDENTITY(1,1),
    ma_ctsp VARCHAR(50) UNIQUE,
    so_luong INT,
    gia_nhap DECIMAL(18,2),
    gia_ban DECIMAL(18,2),
    id_san_pham INT FOREIGN KEY REFERENCES san_pham(id),
    id_mau_sac INT FOREIGN KEY REFERENCES mau_sac(id),
    id_kich_thuoc INT FOREIGN KEY REFERENCES kich_thuoc(id),
    ngay_tao DATETIME,
    ngay_cap_nhat DATETIME,
    nguoi_tao NVARCHAR(100),
    nguoi_cap_nhat NVARCHAR(100),
    trang_thai INT
);

CREATE TABLE dia_chi_khach_hang (
    id INT PRIMARY KEY IDENTITY(1,1),
    ma_dia_chi VARCHAR(50),
    ten_nguoi_nhan NVARCHAR(255),
    so_dien_thoai VARCHAR(15),
    thanh_pho NVARCHAR(100),
    quan NVARCHAR(100),
    phuong NVARCHAR(100),
    dia_chi_cu_the NVARCHAR(MAX),
    mac_dinh BIT,
    id_khach_hang INT FOREIGN KEY REFERENCES khach_hang(id),
    trang_thai INT
);

CREATE TABLE hoa_don (
    id INT PRIMARY KEY IDENTITY(1,1),
    ma_hoa_don VARCHAR(50) UNIQUE,
    ngay_thanh_toan DATETIME,
    tong_tien DECIMAL(18,2),
    tien_giam_gia DECIMAL(18,2),
    phi_van_chuyen DECIMAL(18,2),
    tong_tien_sau_giam DECIMAL(18,2),
    hinh_thuc_thanh_toan NVARCHAR(100),
    loai_hoa_don NVARCHAR(50),
    ten_nguoi_nhan NVARCHAR(255),
    sdt_nguoi_nhan VARCHAR(15),
    email_khach_hang VARCHAR(255),
    dia_chi_nguoi_nhan NVARCHAR(MAX),
    ghi_chu NVARCHAR(MAX),
    id_khach_hang INT FOREIGN KEY REFERENCES khach_hang(id),
    id_nhan_vien INT FOREIGN KEY REFERENCES nhan_vien(id),
    id_phieu_giam_gia INT FOREIGN KEY REFERENCES phieu_giam_gia(id),
    ngay_tao DATETIME,
    ngay_cap_nhat DATETIME,
    nguoi_tao NVARCHAR(100),
    nguoi_cap_nhat NVARCHAR(100),
    trang_thai INT
);

CREATE TABLE hinh_anh (
    id INT PRIMARY KEY IDENTITY(1,1),
    duong_dan_anh NVARCHAR(MAX),
    ten_anh NVARCHAR(255),
    anh_chinh BIT,
    id_chi_tiet_san_pham INT FOREIGN KEY REFERENCES chi_tiet_san_pham(id),
    trang_thai INT
);

CREATE TABLE chi_tiet_dot_giam_gia (
    id INT PRIMARY KEY IDENTITY(1,1),
    id_dot_giam_gia INT FOREIGN KEY REFERENCES dot_giam_gia(id),
    id_chi_tiet_san_pham INT FOREIGN KEY REFERENCES chi_tiet_san_pham(id),
    so_luong_ap_dung INT,
    gia_tri_giam_rieng DECIMAL(18,2),
    so_tien_toi_da_giam_rieng DECIMAL(18,2),
    ghi_chu NVARCHAR(MAX),
    trang_thai INT,
    ngay_tao DATETIME,
    ngay_cap_nhat DATETIME,
    nguoi_tao NVARCHAR(100),
    nguoi_cap_nhat NVARCHAR(100)
);

CREATE TABLE phieu_giam_gia_ca_nhan (
    id INT PRIMARY KEY IDENTITY(1,1),
    ma_phieu_khach_hang VARCHAR(50),
    ngay_nhan DATETIME,
    da_su_dung BIT,
    id_khach_hang INT FOREIGN KEY REFERENCES khach_hang(id),
    id_phieu_giam_gia INT FOREIGN KEY REFERENCES phieu_giam_gia(id),
    trang_thai INT
);

CREATE TABLE hoa_don_chi_tiet (
    id INT PRIMARY KEY IDENTITY(1,1),
    ma_hoa_don_chi_tiet VARCHAR(50),
    so_luong INT,
    don_gia DECIMAL(18,2),
    thanh_tien DECIMAL(18,2),
    ghi_chu NVARCHAR(MAX),
    id_hoa_don INT FOREIGN KEY REFERENCES hoa_don(id),
    id_chi_tiet_san_pham INT FOREIGN KEY REFERENCES chi_tiet_san_pham(id),
    trang_thai INT
);

CREATE TABLE lich_su_hoa_don (
    id INT PRIMARY KEY IDENTITY(1,1),
    hanh_dong NVARCHAR(255),
    thoi_gian DATETIME,
    ghi_chu NVARCHAR(MAX),
    id_hoa_don INT FOREIGN KEY REFERENCES hoa_don(id),
    id_nhan_vien INT FOREIGN KEY REFERENCES nhan_vien(id),
    trang_thai INT
);

CREATE TABLE giao_dich_thanh_toan (
    id INT PRIMARY KEY IDENTITY(1,1),
    id_hoa_don INT FOREIGN KEY REFERENCES hoa_don(id),
    id_phuong_thuc_thanh_toan INT FOREIGN KEY REFERENCES phuong_thuc_thanh_toan(id),
    ma_giao_dich_thanh_toan VARCHAR(100),
    so_tien DECIMAL(18,2),
    trang_thai INT,
    ma_yeu_cau VARCHAR(100),
    ma_giao_dich_ngoai VARCHAR(100),
    ma_tham_chieu VARCHAR(100),
    duong_dan_thanh_toan NVARCHAR(MAX),
    du_lieu_qr NVARCHAR(MAX),
    thoi_gian_het_han DATETIME,
    du_lieu_phan_hoi NVARCHAR(MAX),
    thoi_gian_tao DATETIME,
    thoi_gian_cap_nhat DATETIME,
    ghi_chu NVARCHAR(MAX)
);

CREATE TABLE lich_su_thanh_toan (
    id INT PRIMARY KEY IDENTITY(1,1),
    id_hoa_don INT FOREIGN KEY REFERENCES hoa_don(id),
    ma_giao_dich VARCHAR(100),
    so_tien DECIMAL(18,2),
    ngay_thanh_toan DATETIME,
    hinh_thuc_thanh_toan NVARCHAR(100),
    loai_thanh_toan NVARCHAR(100),
    ghi_chu NVARCHAR(MAX),
    trang_thai INT
);
INSERT INTO quyen_han (ten_quyen_han, trang_thai) VALUES (N'Nhân viên', 1);

select * from nhan_vien;
<<<<<<< HEAD
=======
select * from khach_hang
>>>>>>> nope/bach

INSERT INTO quyen_han (ten_quyen_han, trang_thai) VALUES (N'Nhân viên', 1);
select * from quyen_han;
ALTER TABLE nhan_vien ADD anh_dai_dien VARCHAR(255);
ALTER TABLE nhan_vien ADD cccd VARCHAR(20);