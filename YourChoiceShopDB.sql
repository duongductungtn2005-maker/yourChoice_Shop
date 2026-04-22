IF DB_ID(N'YourChoiceShopDB') IS NULL
BEGIN
    CREATE DATABASE YourChoiceShopDB;
END
GO

USE YourChoiceShopDB;
GO

DROP TABLE IF EXISTS chat_message;
DROP TABLE IF EXISTS chat_session;
DROP TABLE IF EXISTS giao_dich_thanh_toan;
DROP TABLE IF EXISTS lich_su_thanh_toan;
DROP TABLE IF EXISTS lich_su_hoa_don;
DROP TABLE IF EXISTS hoa_don_chi_tiet;
DROP TABLE IF EXISTS phieu_giam_gia_ca_nhan;
DROP TABLE IF EXISTS chi_tiet_dot_giam_gia;
DROP TABLE IF EXISTS hinh_anh;
DROP TABLE IF EXISTS hoa_don;
DROP TABLE IF EXISTS chi_tiet_san_pham;
DROP TABLE IF EXISTS giao_ca;
DROP TABLE IF EXISTS lich_lam_viec;
DROP TABLE IF EXISTS ca_lam_viec;
DROP TABLE IF EXISTS dia_chi_khach_hang;
DROP TABLE IF EXISTS san_pham;
DROP TABLE IF EXISTS nhan_vien;
DROP TABLE IF EXISTS khach_hang;
DROP TABLE IF EXISTS phieu_giam_gia;
DROP TABLE IF EXISTS dot_giam_gia;
DROP TABLE IF EXISTS phuong_thuc_thanh_toan;
DROP TABLE IF EXISTS quyen_han;
DROP TABLE IF EXISTS mau_sac;
DROP TABLE IF EXISTS kich_thuoc;
DROP TABLE IF EXISTS tay_ao;
DROP TABLE IF EXISTS co_ao;
DROP TABLE IF EXISTS chat_lieu;
DROP TABLE IF EXISTS xuat_xu;
DROP TABLE IF EXISTS thuong_hieu;
DROP TABLE IF EXISTS chat_knowledge;
DROP TABLE IF EXISTS thong_bao;
DROP TABLE IF EXISTS danh_ba_email;
GO

CREATE TABLE thuong_hieu (
    id INT IDENTITY(1,1) PRIMARY KEY,
    ma_thuong_hieu NVARCHAR(50) UNIQUE,
    ten_thuong_hieu NVARCHAR(255),
    ngay_tao DATETIME2,
    ngay_cap_nhat DATETIME2,
    nguoi_tao NVARCHAR(100),
    nguoi_cap_nhat NVARCHAR(100),
    trang_thai INT
);

CREATE TABLE xuat_xu (
    id INT IDENTITY(1,1) PRIMARY KEY,
    ma_xuat_xu NVARCHAR(50) UNIQUE,
    ten_xuat_xu NVARCHAR(255),
    ngay_tao DATETIME2,
    ngay_cap_nhat DATETIME2,
    nguoi_tao NVARCHAR(100),
    nguoi_cap_nhat NVARCHAR(100),
    trang_thai INT
);

CREATE TABLE chat_lieu (
    id INT IDENTITY(1,1) PRIMARY KEY,
    ma_chat_lieu NVARCHAR(50) UNIQUE,
    ten_chat_lieu NVARCHAR(255),
    id_chat_lieu INT,
    ngay_tao DATETIME2,
    ngay_cap_nhat DATETIME2,
    nguoi_tao NVARCHAR(100),
    nguoi_cap_nhat NVARCHAR(100),
    trang_thai INT,
    CONSTRAINT FK_chat_lieu_parent FOREIGN KEY (id_chat_lieu) REFERENCES chat_lieu(id)
);

CREATE TABLE co_ao (
    id INT IDENTITY(1,1) PRIMARY KEY,
    ma_co_ao NVARCHAR(50) UNIQUE,
    ten_co_ao NVARCHAR(255),
    ngay_tao DATETIME2,
    ngay_cap_nhat DATETIME2,
    nguoi_tao NVARCHAR(100),
    nguoi_cap_nhat NVARCHAR(100),
    trang_thai INT
);

CREATE TABLE tay_ao (
    id INT IDENTITY(1,1) PRIMARY KEY,
    ma_tay_ao NVARCHAR(50) UNIQUE,
    ten_tay_ao NVARCHAR(255),
    ngay_tao DATETIME2,
    ngay_cap_nhat DATETIME2,
    nguoi_tao NVARCHAR(100),
    nguoi_cap_nhat NVARCHAR(100),
    trang_thai INT
);

CREATE TABLE mau_sac (
    id INT IDENTITY(1,1) PRIMARY KEY,
    ma_mau_sac NVARCHAR(50) UNIQUE,
    ten_mau_sac NVARCHAR(255),
    ngay_tao DATETIME2,
    ngay_cap_nhat DATETIME2,
    nguoi_tao NVARCHAR(100),
    nguoi_cap_nhat NVARCHAR(100),
    trang_thai INT
);

CREATE TABLE kich_thuoc (
    id INT IDENTITY(1,1) PRIMARY KEY,
    ma_kich_thuoc NVARCHAR(50) UNIQUE,
    ten_kich_thuoc NVARCHAR(255),
    ngay_tao DATETIME2,
    ngay_cap_nhat DATETIME2,
    nguoi_tao NVARCHAR(100),
    nguoi_cap_nhat NVARCHAR(100),
    trang_thai INT
);

CREATE TABLE quyen_han (
    id INT IDENTITY(1,1) PRIMARY KEY,
    ten_quyen_han NVARCHAR(100),
    trang_thai INT
);

CREATE TABLE phuong_thuc_thanh_toan (
    id INT IDENTITY(1,1) PRIMARY KEY,
    ma_phuong_thuc NVARCHAR(50) UNIQUE,
    ten_phuong_thuc NVARCHAR(255),
    ghi_chu NVARCHAR(MAX),
    trang_thai INT
);

CREATE TABLE dot_giam_gia (
    id INT IDENTITY(1,1) PRIMARY KEY,
    ma_dot_giam_gia NVARCHAR(50) UNIQUE,
    ten_dot_giam_gia NVARCHAR(255),
    gia_tri_giam DECIMAL(18,2),
    loai_giam_gia NVARCHAR(50),
    ngay_bat_dau DATETIME2,
    ngay_ket_thuc DATETIME2,
    trang_thai INT
);

CREATE TABLE phieu_giam_gia (
    id INT IDENTITY(1,1) PRIMARY KEY,
    ma_phieu_giam_gia NVARCHAR(50) UNIQUE,
    ten_phieu_giam_gia NVARCHAR(255),
    loai_phieu NVARCHAR(50),
    kieu NVARCHAR(50),
    gia_tri_giam DECIMAL(18,2),
    gia_tri_giam_toi_da DECIMAL(18,2),
    don_hang_toi_thieu DECIMAL(18,2),
    so_luong INT,
    ngay_bat_dau DATETIME2,
    ngay_ket_thuc DATETIME2,
    trang_thai INT,
    mo_ta NVARCHAR(MAX),
    gioi_han_moi_khach INT
);

CREATE TABLE khach_hang (
    id INT IDENTITY(1,1) PRIMARY KEY,
    ma_khach_hang NVARCHAR(50) UNIQUE,
    ten_khach_hang NVARCHAR(255),
    ten_tai_khoan NVARCHAR(100),
    mat_khau NVARCHAR(255),
    email NVARCHAR(255),
    so_dien_thoai NVARCHAR(20),
    gioi_tinh BIT,
    ngay_sinh DATE,
    avatar NVARCHAR(500),
    ngay_tao DATETIME2,
    ngay_cap_nhat DATETIME2,
    nguoi_tao NVARCHAR(100),
    nguoi_cap_nhat NVARCHAR(100),
    trang_thai INT
);

CREATE TABLE nhan_vien (
    id INT IDENTITY(1,1) PRIMARY KEY,
    ma_nhan_vien NVARCHAR(50) UNIQUE,
    ten_nhan_vien NVARCHAR(255),
    ten_tai_khoan NVARCHAR(100),
    mat_khau NVARCHAR(255),
    email NVARCHAR(255),
    so_dien_thoai NVARCHAR(20),
    gioi_tinh BIT,
    ngay_sinh DATE,
    dia_chi NVARCHAR(MAX),
    anh_dai_dien NVARCHAR(500),
    cccd NVARCHAR(20),
    id_quyen_han INT,
    ngay_tao DATETIME2,
    ngay_cap_nhat DATETIME2,
    nguoi_tao NVARCHAR(100),
    nguoi_cap_nhat NVARCHAR(100),
    trang_thai INT,
    CONSTRAINT FK_nhan_vien_quyen_han FOREIGN KEY (id_quyen_han) REFERENCES quyen_han(id)
);

CREATE TABLE ca_lam_viec (
    id INT IDENTITY(1,1) PRIMARY KEY,
    ma_ca NVARCHAR(50),
    ten_ca NVARCHAR(255),
    thoi_gian_bat_dau TIME,
    thoi_gian_ket_thuc TIME,
    ghi_chu NVARCHAR(MAX),
    ngay_tao DATETIME2,
    ngay_cap_nhat DATETIME2,
    nguoi_tao NVARCHAR(100),
    nguoi_cap_nhat NVARCHAR(100),
    trang_thai INT
);

CREATE TABLE lich_lam_viec (
    id INT IDENTITY(1,1) PRIMARY KEY,
    id_nhan_vien INT,
    id_ca_lam_viec INT,
    ngay_lam_viec DATE,
    ghi_chu NVARCHAR(MAX),
    ngay_tao DATETIME2,
    ngay_cap_nhat DATETIME2,
    nguoi_tao NVARCHAR(100),
    nguoi_cap_nhat NVARCHAR(100),
    trang_thai INT,
    CONSTRAINT FK_lich_lam_viec_nhan_vien FOREIGN KEY (id_nhan_vien) REFERENCES nhan_vien(id),
    CONSTRAINT FK_lich_lam_viec_ca_lam_viec FOREIGN KEY (id_ca_lam_viec) REFERENCES ca_lam_viec(id)
);

CREATE TABLE giao_ca (
    id INT IDENTITY(1,1) PRIMARY KEY,
    ma_giao_ca NVARCHAR(50),
    id_nhan_vien_trong_ca INT,
    id_nhan_vien_nhan_ca INT,
    id_lich_lam_viec INT,
    thoi_gian_nhan_ca DATETIME2,
    thoi_gian_giao_ca DATETIME2,
    tien_ban_dau DECIMAL(18,2),
    tong_thu_trong_ca DECIMAL(18,2),
    tong_tien_mat DECIMAL(18,2),
    tong_tien_chuyen_khoan DECIMAL(18,2),
    tien_phat_sinh DECIMAL(18,2),
    tien_thuc_te_kiem_dem DECIMAL(18,2),
    tien_chenh_lech DECIMAL(18,2),
    ly_do_chenh_lech NVARCHAR(MAX),
    ghi_chu NVARCHAR(MAX),
    dong_som BIT,
    ngay_tao DATETIME2,
    ngay_cap_nhat DATETIME2,
    nguoi_tao NVARCHAR(100),
    nguoi_cap_nhat NVARCHAR(100),
    trang_thai INT,
    CONSTRAINT FK_giao_ca_nhan_vien_trong_ca FOREIGN KEY (id_nhan_vien_trong_ca) REFERENCES nhan_vien(id),
    CONSTRAINT FK_giao_ca_nhan_vien_nhan_ca FOREIGN KEY (id_nhan_vien_nhan_ca) REFERENCES nhan_vien(id),
    CONSTRAINT FK_giao_ca_lich_lam_viec FOREIGN KEY (id_lich_lam_viec) REFERENCES lich_lam_viec(id)
);

CREATE TABLE san_pham (
    id INT IDENTITY(1,1) PRIMARY KEY,
    ma_san_pham NVARCHAR(50) UNIQUE,
    ten_san_pham NVARCHAR(255),
    mo_ta_chi_tiet NVARCHAR(MAX),
    id_thuong_hieu INT,
    id_xuat_xu INT,
    id_chat_lieu INT,
    id_co_ao INT,
    id_tay_ao INT,
    ngay_tao DATETIME2,
    ngay_cap_nhat DATETIME2,
    nguoi_tao NVARCHAR(100),
    nguoi_cap_nhat NVARCHAR(100),
    trang_thai INT,
    CONSTRAINT FK_san_pham_thuong_hieu FOREIGN KEY (id_thuong_hieu) REFERENCES thuong_hieu(id),
    CONSTRAINT FK_san_pham_xuat_xu FOREIGN KEY (id_xuat_xu) REFERENCES xuat_xu(id),
    CONSTRAINT FK_san_pham_chat_lieu FOREIGN KEY (id_chat_lieu) REFERENCES chat_lieu(id),
    CONSTRAINT FK_san_pham_co_ao FOREIGN KEY (id_co_ao) REFERENCES co_ao(id),
    CONSTRAINT FK_san_pham_tay_ao FOREIGN KEY (id_tay_ao) REFERENCES tay_ao(id)
);

CREATE TABLE chi_tiet_san_pham (
    id INT IDENTITY(1,1) PRIMARY KEY,
    ma_ctsp NVARCHAR(50) UNIQUE,
    so_luong INT,
    gia_nhap DECIMAL(18,2),
    gia_ban DECIMAL(18,2),
    id_san_pham INT,
    id_mau_sac INT,
    id_kich_thuoc INT,
    id_chat_lieu INT,
    id_thuong_hieu INT,
    id_co_ao INT,
    id_tay_ao INT,
    id_xuat_xu INT,
    id_dot_giam_gia INT,
    ngay_tao DATETIME2,
    ngay_cap_nhat DATETIME2,
    nguoi_tao NVARCHAR(100),
    nguoi_cap_nhat NVARCHAR(100),
    trang_thai INT,
    CONSTRAINT FK_ctsp_san_pham FOREIGN KEY (id_san_pham) REFERENCES san_pham(id),
    CONSTRAINT FK_ctsp_mau_sac FOREIGN KEY (id_mau_sac) REFERENCES mau_sac(id),
    CONSTRAINT FK_ctsp_kich_thuoc FOREIGN KEY (id_kich_thuoc) REFERENCES kich_thuoc(id),
    CONSTRAINT FK_ctsp_chat_lieu FOREIGN KEY (id_chat_lieu) REFERENCES chat_lieu(id),
    CONSTRAINT FK_ctsp_thuong_hieu FOREIGN KEY (id_thuong_hieu) REFERENCES thuong_hieu(id),
    CONSTRAINT FK_ctsp_co_ao FOREIGN KEY (id_co_ao) REFERENCES co_ao(id),
    CONSTRAINT FK_ctsp_tay_ao FOREIGN KEY (id_tay_ao) REFERENCES tay_ao(id),
    CONSTRAINT FK_ctsp_xuat_xu FOREIGN KEY (id_xuat_xu) REFERENCES xuat_xu(id),
    CONSTRAINT FK_ctsp_dot_giam_gia FOREIGN KEY (id_dot_giam_gia) REFERENCES dot_giam_gia(id)
);

CREATE TABLE dia_chi_khach_hang (
    id INT IDENTITY(1,1) PRIMARY KEY,
    ma_dia_chi NVARCHAR(50),
    ten_nguoi_nhan NVARCHAR(255),
    so_dien_thoai NVARCHAR(20),
    thanh_pho NVARCHAR(100),
    quan NVARCHAR(100),
    phuong NVARCHAR(100),
    dia_chi_cu_the NVARCHAR(MAX),
    mac_dinh BIT,
    id_khach_hang INT,
    trang_thai INT,
    CONSTRAINT FK_dia_chi_khach_hang_khach_hang FOREIGN KEY (id_khach_hang) REFERENCES khach_hang(id)
);

CREATE TABLE hoa_don (
    id INT IDENTITY(1,1) PRIMARY KEY,
    ma_hoa_don NVARCHAR(50) UNIQUE,
    ngay_thanh_toan DATETIME2,
    tong_tien DECIMAL(18,2),
    tien_giam_gia DECIMAL(18,2),
    phi_van_chuyen DECIMAL(18,2),
    tong_tien_sau_giam DECIMAL(18,2),
    hinh_thuc_thanh_toan NVARCHAR(100),
    loai_hoa_don NVARCHAR(50),
    ten_nguoi_nhan NVARCHAR(255),
    sdt_nguoi_nhan NVARCHAR(20),
    email_khach_hang NVARCHAR(255),
    dia_chi_nguoi_nhan NVARCHAR(MAX),
    ghi_chu NVARCHAR(MAX),
    id_khach_hang INT,
    id_nhan_vien INT,
    id_phieu_giam_gia INT,
    id_giao_ca INT,
    ngay_tao DATETIME2,
    ngay_cap_nhat DATETIME2,
    nguoi_tao NVARCHAR(100),
    nguoi_cap_nhat NVARCHAR(100),
    trang_thai INT,
    CONSTRAINT FK_hoa_don_khach_hang FOREIGN KEY (id_khach_hang) REFERENCES khach_hang(id),
    CONSTRAINT FK_hoa_don_nhan_vien FOREIGN KEY (id_nhan_vien) REFERENCES nhan_vien(id),
    CONSTRAINT FK_hoa_don_phieu_giam_gia FOREIGN KEY (id_phieu_giam_gia) REFERENCES phieu_giam_gia(id),
    CONSTRAINT FK_hoa_don_giao_ca FOREIGN KEY (id_giao_ca) REFERENCES giao_ca(id)
);

CREATE TABLE hinh_anh (
    id INT IDENTITY(1,1) PRIMARY KEY,
    duong_dan_anh NVARCHAR(MAX),
    ten_anh NVARCHAR(255),
    anh_chinh BIT,
    id_ctsp INT,
    id_chi_tiet_san_pham AS id_ctsp PERSISTED,
    trang_thai INT,
    CONSTRAINT FK_hinh_anh_ctsp FOREIGN KEY (id_ctsp) REFERENCES chi_tiet_san_pham(id)
);

CREATE TABLE chi_tiet_dot_giam_gia (
    id INT IDENTITY(1,1) PRIMARY KEY,
    id_dot_giam_gia INT,
    id_chi_tiet_san_pham INT,
    so_luong_ap_dung INT,
    gia_tri_giam_rieng DECIMAL(18,2),
    so_tien_toi_da_giam_rieng DECIMAL(18,2),
    ghi_chu NVARCHAR(MAX),
    ngay_tao DATETIME2,
    ngay_cap_nhat DATETIME2,
    nguoi_tao NVARCHAR(100),
    nguoi_cap_nhat NVARCHAR(100),
    trang_thai INT,
    CONSTRAINT FK_ctdgg_dot_giam_gia FOREIGN KEY (id_dot_giam_gia) REFERENCES dot_giam_gia(id),
    CONSTRAINT FK_ctdgg_ctsp FOREIGN KEY (id_chi_tiet_san_pham) REFERENCES chi_tiet_san_pham(id)
);

CREATE TABLE phieu_giam_gia_ca_nhan (
    id INT IDENTITY(1,1) PRIMARY KEY,
    ma_phieu_khach_hang NVARCHAR(50),
    ngay_nhan DATETIME2,
    da_su_dung BIT,
    id_khach_hang INT,
    id_phieu_giam_gia INT,
    trang_thai INT,
    CONSTRAINT FK_pggcn_khach_hang FOREIGN KEY (id_khach_hang) REFERENCES khach_hang(id),
    CONSTRAINT FK_pggcn_phieu_giam_gia FOREIGN KEY (id_phieu_giam_gia) REFERENCES phieu_giam_gia(id)
);

CREATE TABLE hoa_don_chi_tiet (
    id INT IDENTITY(1,1) PRIMARY KEY,
    ma_hoa_don_chi_tiet NVARCHAR(50),
    so_luong INT,
    don_gia DECIMAL(18,2),
    thanh_tien DECIMAL(18,2),
    ghi_chu NVARCHAR(MAX),
    id_hoa_don INT,
    id_chi_tiet_san_pham INT,
    trang_thai INT,
    CONSTRAINT FK_hdct_hoa_don FOREIGN KEY (id_hoa_don) REFERENCES hoa_don(id),
    CONSTRAINT FK_hdct_ctsp FOREIGN KEY (id_chi_tiet_san_pham) REFERENCES chi_tiet_san_pham(id)
);

CREATE TABLE lich_su_hoa_don (
    id INT IDENTITY(1,1) PRIMARY KEY,
    hanh_dong NVARCHAR(255),
    thoi_gian DATETIME2,
    ghi_chu NVARCHAR(MAX),
    id_hoa_don INT,
    id_nhan_vien INT,
    trang_thai INT,
    CONSTRAINT FK_lshd_hoa_don FOREIGN KEY (id_hoa_don) REFERENCES hoa_don(id),
    CONSTRAINT FK_lshd_nhan_vien FOREIGN KEY (id_nhan_vien) REFERENCES nhan_vien(id)
);

CREATE TABLE giao_dich_thanh_toan (
    id INT IDENTITY(1,1) PRIMARY KEY,
    id_hoa_don INT,
    id_phuong_thuc_thanh_toan INT,
    ma_giao_dich_thanh_toan NVARCHAR(100),
    so_tien DECIMAL(18,2),
    trang_thai INT,
    ma_yeu_cau NVARCHAR(100),
    ma_giao_dich_ngoai NVARCHAR(100),
    ma_tham_chieu NVARCHAR(100),
    duong_dan_thanh_toan NVARCHAR(MAX),
    du_lieu_qr NVARCHAR(MAX),
    thoi_gian_het_han DATETIME2,
    du_lieu_phan_hoi NVARCHAR(MAX),
    thoi_gian_tao DATETIME2,
    thoi_gian_cap_nhat DATETIME2,
    ghi_chu NVARCHAR(MAX),
    CONSTRAINT FK_gdtt_hoa_don FOREIGN KEY (id_hoa_don) REFERENCES hoa_don(id),
    CONSTRAINT FK_gdtt_pttt FOREIGN KEY (id_phuong_thuc_thanh_toan) REFERENCES phuong_thuc_thanh_toan(id)
);

CREATE TABLE lich_su_thanh_toan (
    id INT IDENTITY(1,1) PRIMARY KEY,
    id_hoa_don INT,
    ma_giao_dich NVARCHAR(100),
    so_tien DECIMAL(18,2),
    ngay_thanh_toan DATETIME2,
    hinh_thuc_thanh_toan NVARCHAR(100),
    loai_thanh_toan NVARCHAR(100),
    ghi_chu NVARCHAR(MAX),
    trang_thai INT,
    CONSTRAINT FK_lstt_hoa_don FOREIGN KEY (id_hoa_don) REFERENCES hoa_don(id)
);

CREATE TABLE chat_knowledge (
    id INT IDENTITY(1,1) PRIMARY KEY,
    cau_hoi_mau NVARCHAR(500) NOT NULL,
    tu_khoa NVARCHAR(500),
    cau_tra_loi NVARCHAR(MAX) NOT NULL,
    do_uu_tien INT,
    trang_thai BIT,
    so_lan_su_dung INT,
    lan_su_dung_cuoi DATETIME2
);

CREATE TABLE chat_session (
    id INT IDENTITY(1,1) PRIMARY KEY,
    loai_chat NVARCHAR(50),
    trang_thai INT,
    ngay_tao DATETIME2,
    ngay_cap_nhat DATETIME2,
    ten_hien_thi NVARCHAR(255),
    session_id NVARCHAR(255),
    nguoi_xu_ly NVARCHAR(255),
    id_khach_hang INT,
    id_nhan_vien INT,
    CONSTRAINT FK_chat_session_khach_hang FOREIGN KEY (id_khach_hang) REFERENCES khach_hang(id),
    CONSTRAINT FK_chat_session_nhan_vien FOREIGN KEY (id_nhan_vien) REFERENCES nhan_vien(id)
);

CREATE TABLE chat_message (
    id INT IDENTITY(1,1) PRIMARY KEY,
    sender_role NVARCHAR(50),
    sender_name NVARCHAR(255),
    noi_dung NVARCHAR(MAX),
    loai_tin_nhan NVARCHAR(50),
    ngay_gui DATETIME2,
    id_chat_session INT,
    id_khach_hang INT,
    id_nhan_vien INT,
    CONSTRAINT FK_chat_message_session FOREIGN KEY (id_chat_session) REFERENCES chat_session(id),
    CONSTRAINT FK_chat_message_khach_hang FOREIGN KEY (id_khach_hang) REFERENCES khach_hang(id),
    CONSTRAINT FK_chat_message_nhan_vien FOREIGN KEY (id_nhan_vien) REFERENCES nhan_vien(id)
);

CREATE TABLE thong_bao (
    id INT IDENTITY(1,1) PRIMARY KEY,
    tieu_de NVARCHAR(255),
    noi_dung NVARCHAR(MAX),
    loai NVARCHAR(100),
    ma_hoa_don NVARCHAR(50),
    da_doc BIT,
    ngay_tao DATETIME2
);

CREATE TABLE danh_ba_email (
    id INT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(100) NOT NULL,
    email NVARCHAR(150) NOT NULL UNIQUE,
    created_at DATETIME2
);
GO

IF NOT EXISTS (SELECT 1 FROM quyen_han WHERE ten_quyen_han = N'Nhan vien')
BEGIN
    INSERT INTO quyen_han (ten_quyen_han, trang_thai)
    VALUES (N'Nhan vien', 1);
END;

IF NOT EXISTS (SELECT 1 FROM quyen_han WHERE ten_quyen_han = N'Admin')
BEGIN
    INSERT INTO quyen_han (ten_quyen_han, trang_thai)
    VALUES (N'Admin', 1);
END;

IF NOT EXISTS (SELECT 1 FROM nhan_vien WHERE ten_tai_khoan = N'admin')
BEGIN
    INSERT INTO nhan_vien (
        ma_nhan_vien,
        ten_nhan_vien,
        ten_tai_khoan,
        mat_khau,
        email,
        so_dien_thoai,
        gioi_tinh,
        ngay_sinh,
        dia_chi,
        id_quyen_han,
        ngay_tao,
        ngay_cap_nhat,
        trang_thai
    )
    VALUES (
        N'NV001',
        N'Quan tri vien',
        N'admin',
        N'admin123',
        N'admin@yourchoice.com',
        N'0123456789',
        1,
        '1990-01-01',
        N'Ha Noi',
        (SELECT TOP 1 id FROM quyen_han WHERE ten_quyen_han = N'Admin'),
        SYSDATETIME(),
        SYSDATETIME(),
        1
    );
END;