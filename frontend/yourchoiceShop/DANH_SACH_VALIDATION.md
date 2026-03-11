# DANH SÁCH CÁC VALIDATION HIỆN CÓ - YourChoice Shop

## 1. VALIDATION UTILITY (src/utils/validate.js)
Các hàm validation đã được định nghĩa sẵn:

### 1.1 isRequired(value)
- **Mục đích**: Kiểm tra trường không được để trống
- **Thông báo lỗi**: "Trường này không được để trống"
- **Sử dụng**: Tên khách hàng, Tên nhân viên, Tên sản phẩm, v.v.

### 1.2 isEmail(value)
- **Mục đích**: Kiểm tra định dạng email hợp lệ
- **Regex**: `/^[^\s@]+@[^\s@]+\.[^\s@]+$/`
- **Thông báo lỗi**: "Email không đúng định dạng"
- **Sử dụng**: Email khách hàng, Email nhân viên

### 1.3 isPhone(value)
- **Mục đích**: Kiểm tra số điện thoại Việt Nam (10 số)
- **Regex**: `/(84|0[3|5|7|8|9])+([0-9]{8})\b/`
- **Thông báo lỗi**: "Số điện thoại không hợp lệ (10 số, đầu 03,05,07,08,09)"
- **Sử dụng**: SĐT khách hàng, SĐT nhân viên

### 1.4 isPositiveInteger(value)
- **Mục đích**: Kiểm tra số nguyên dương
- **Điều kiện**: Number.isInteger() && value > 0
- **Thông báo lỗi**: "Vui lòng nhập số nguyên dương lớn hơn 0"
- **Sử dụng**: Số lượng sản phẩm

### 1.5 isPositiveNumber(value)
- **Mục đích**: Kiểm tra số thực dương (không âm)
- **Điều kiện**: !isNaN(value) && value >= 0
- **Thông báo lỗi**: "Giá trị không được âm"
- **Sử dụng**: Giá bán, Tiền voucher, Mức giảm giá

### 1.6 isAfterDate(startDate, endDate)
- **Mục đích**: Kiểm tra ngày kết thúc > ngày bắt đầu
- **Điều kiện**: new Date(endDate) > new Date(startDate)
- **Thông báo lỗi**: "Ngày kết thúc phải sau ngày bắt đầu"
- **Sử dụng**: Promotions, Vouchers, Discount periods

### 1.7 minLength(value, min)
- **Mục đích**: Kiểm tra độ dài tối thiểu
- **Thông báo lỗi**: "Phải nhập tối thiểu {min} ký tự"
- **Sử dụng**: Tên chất liệu, Tên cổ áo, Tên thương hiệu (min: 2)

---

## 2. VALIDATION TẠI CÁC MÀN HÌNH

### 2.1 LOGIN VIEW (/views/LoginView.vue)
**Validations:**
- **isRequired**: Username (không được để trống)
- **isRequired**: Password (không được để trống)
- **Message**: "Vui lòng nhập đầy đủ tài khoản và mật khẩu"
- **Message**: "Mật khẩu hoặc tài khoản không đúng. Vui lòng thử lại"

### 2.2 CUSTOMER MANAGEMENT

#### 2.2.1 CustomerCreate.vue
**Basic Info Validations:**
- `tenKhachHang`: isRequired
  - Message: "Thiếu tên khách hàng"
- `username`: isRequired (trim)
  - Message: "Thiếu tên tài khoản"
- `soDienThoai`: isRequired, length === 10 số
  - Message: "Thiếu số điện thoại"
  - Message: "Số điện thoại phải đúng 10 chữ số"
- `ngaySinh`: 
  - custom: Kiểm tra ngày không được là tương lai
    - Message: "Ngày sinh không thể là ngày tương lai"
  - custom: Tuổi phải >= 18
    - Message: "Khách hàng phải từ đủ 18 tuổi trở lên"

**Address Validations:**
- `tenNguoiNhan`: required
- `sdt`: required (10 chữ số)
- `duong`: required
- `thanhPho`: required
- `quan`: required
- `phuong`: required

**Duplicate Check:**
- Check username trùng (API call)
- Check soDienThoai trùng (API call)

#### 2.2.2 CustomerDetail.vue
- Same as CustomerCreate

### 2.3 EMPLOYEE MANAGEMENT

#### 2.3.1 AddEmployee.vue
**Form Validations:**
- `tenNhanVien`: isRequired
  - Message: "Vui lòng nhập họ và tên"
- `ngaySinh`: 
  - custom: Phải chọn ngày sinh
    - Message: "Vui lòng chọn ngày sinh"
  - custom: Tuổi phải >= 18
    - Message: "Nhân viên phải từ đủ 18 tuổi trở lên"
- `soDienThoai`: 
  - custom: Kiểm tra trùng (API call)
    - Message: "Lỗi kết nối khi kiểm tra số điện thoại!"
- `qrCCCD`: 
  - custom: Format kiểm tra (>= 6 parts)
    - Message: "QR không đúng định dạng CCCD!"

#### 2.3.2 EditEmployee.vue
- Same as AddEmployee

### 2.4 ATTRIBUTE MANAGEMENT

#### 2.4.1 ChatLieuIndex.vue, CoAoIndex.vue, TayAoIndex.vue, ThuongHieuIndex.vue, XuatXuIndex.vue
**Form Validation:**
- `ten`: isRequired && minLength(2)
  - Message: "Trường này không được để trống"
  - Message: "Phải nhập tối thiểu 2 ký tự"

#### 2.4.2 KichThuoc.vue, MauSac.vue
**Form Validation:**
- `ten`: isRequired
  - Message: "Trường này không được để trống"

### 2.5 PRODUCT MANAGEMENT

#### 2.5.1 ProductCreate.vue
**Product Info:**
- `tenSanPham`: required (displayed with * label)
- `moTa`: optional
- `idThuongHieu`: required
- `idChatLieu`: required
- `idCoAo`: required
- `idTayAo`: required
- `idXuatXu`: required
- `selectedColors`: required (at least 1 color)
- `selectedSizes`: required (at least 1 size)

#### 2.5.2 ProductUpdateModal.vue
- `giaBan`: isRequired && isPositiveNumber
  - Message: "Trường này không được để trống"
  - Message: "Giá trị không được âm"
- `soLuong`: isRequired && >= 0
  - Message: "Trường này không được để trống"
  - Message: "Số lượng không được âm"

### 2.6 PROMOTION MANAGEMENT (Discount)

#### 2.6.1 SaleCreate.vue, EditDotGiamGia.vue
**Form Validations:**
- `tenDotGiamGia`: isRequired (trim)
  - Message: "Thiếu tên đợt giảm giá"
- `giaTriGiam`: > 0
  - Message: "Mức giảm phải > 0"
- `ngayBatDau` & `ngayKetThuc`: both required
  - Message: "Chọn thời gian"
- `idChiTietSanPhams`: at least 1 product selected
  - Message: "Chưa chọn sản phẩm nào"
- `ngayKetThuc`: isAfterDate (must be after ngayBatDau)

### 2.7 VOUCHER MANAGEMENT

#### 2.7.1 VoucherCreate.vue
- Similar to Promotions

#### 2.7.2 VoucherEdit.vue
- `tenPhieuGiamGia`: isRequired (trim)
  - Message: "Thiếu tên phiếu giảm"
- `giaTriGiam`: > 0
  - Message: " Mức giảm phải > 0"
- `soLuong`: >= 0
  - validateSoLuong() function: if (soLuong < 0) -> set to 0
- `ngayKetThuc`: isAfterDate (must be after ngayBatDau)

### 2.8 POS (POINT OF SALE) MANAGEMENT

#### 2.8.1 BanHangTaiQuay.vue
**Order Processing:**
- `qtyWarning`: Kiểm tra số lượng sản phẩm kho
  - UI class: `.qty-input-error` when qtyWarning exists
- `reserveProductStock()`: Check stock availability
  - Message: "Không thể giữ kho. Vui lòng thử lại."
- `releaseProductStock()`: Check stock release
  - Message: "Không thể hoàn kho. Vui lòng thử lại."

**Address Validations (Shipping):**
- `thanhPho`: required
- `quan`: required  
- `phuong`: required
- `diaChiCuThe`: required
- `tenNguoiNhan`: required
- `sdt`: required

---

## 3. VALIDATION PATTERNS KHÔNG DÙNG validate.js

### 3.1 Date Validations
```
- Tuổi >= 18 (birthdate calculation)
- Ngày sinh không tương lai
- Ngày kết thúc > ngày bắt đầu
- Ngày kết thúc không quá hạn khi bật promotion
```

### 3.2 Business Logic Validations
```
- Username trùng lặp (khách hàng, nhân viên)
- Số điện thoại trùng lặp
- QR Code CCCD format (6 parts)
- Số lượng sản phẩm trong kho
- Trạng thái voucher/promotion khi kích hoạt
```

### 3.3 Selection/Requirement Validations
```
- Ít nhất 1 màu sắc được chọn (Product)
- Ít nhất 1 kích cỡ được chọn (Product)
- Ít nhất 1 sản phẩm được chọn (Promotion/Voucher)
```

---

## 4. MÀN HÌNH SỬ DỤNG VALIDATION

| Màn hình | Component | Kiểu Validation |
|----------|-----------|-----------------|
| Login | LoginView.vue | isRequired, Custom Auth |
| Khách hàng | CustomerCreate/Detail | isRequired, isPhone, Date, Duplicate Check |
| Nhân viên | AddEmployee/EditEmployee | isRequired, Date (age >= 18), Duplicate Check, QR Format |
| Chất liệu, Cổ áo, Tay áo, Thương hiệu, Xuất xứ | Attribute Index | isRequired, minLength(2) |
| Kích thước, Màu sắc | Size/Color Index | isRequired |
| Sản phẩm | ProductCreate/Update | isRequired, isPositiveNumber, Selection |
| Promotion | SaleCreate/EditDotGiamGia | isRequired, isPositiveNumber, isAfterDate, Selection |
| Voucher | VoucherCreate/Edit | isRequired, isPositiveNumber, isAfterDate, Selection |
| Bán hàng | BanHangTaiQuay | Stock Check, Address Required |

---

## 5. VALIDATION MIXIN/COMPOSABLE KHÔNG CÓ
- Hệ thống hiện tại sử dụng **inline validation** tại mỗi component
- Không có shared validation composable (có thể cấu trúc lại)

---

## 6. ĐỀ XUẤT CẤU TRÚC UNIFIED VALIDATION LIST

Để tạo một danh sách validation tập trung, bạn có thể:</br>

### Option 1: Mở rộng validate.js
```javascript
export const validate = {
  // Existing...
  isRequired,
  isEmail,
  isPhone,
  // Thêm mới:
  isMinAge,
  isNotFutureDate,
  isPositiveInteger,
  // ... etc
}
```

### Option 2: Tạo validationRules.js
```javascript
export const validationRules = {
  customer: { ... },
  employee: { ... },
  product: { ... },
  promotion: { ... },
  voucher: { ... }
}
```

### Option 3: Tạo composable useFormValidation.js
```javascript
export function useFormValidation() {
  return {
    validateField,
    validateForm,
    validateAsync
  }
}
```

---

## 7. NHỮNG VALIDATIONS CÒN THIẾU/CÓ THỂ THÊM

- [ ] Password strength validation (khi tạo password)
- [ ] Confirm password validation
- [ ] URL validation (nếu có upload URL)
- [ ] File size validation (khi upload image)
- [ ] Image dimension validation
- [ ] Credit card validation
- [ ] Tổng tiền validation (không vượt quá giới hạn)
- [ ] SKU uniqueness validation (sản phẩm)
- [ ] Rich validation messages
- [ ] Real-time validation feedback

---

*Generated: 2026-03-09*
