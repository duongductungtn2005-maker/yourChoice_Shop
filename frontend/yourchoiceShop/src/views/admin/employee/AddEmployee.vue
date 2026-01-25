<template>
  <BasePage>
    <div class="create-employee-container">
      
      <div class="page-header">
        <h2 class="breadcrumb">
          <span class="back-link" @click="goBack">Nhân viên</span> 
          <span class="divider">/</span> 
          <span class="current">{{ isEditMode ? 'Cập nhật nhân viên' : 'Thêm nhân viên' }}</span>
        </h2>
      </div>

      <div class="main-card">
        <form @submit.prevent="handleSubmit" class="form-layout">
          
          <div class="left-section">
            <h3 class="section-title">Thông tin nhân viên</h3>
            
            <div class="avatar-upload">
              <div class="avatar-circle" @click="triggerFileInput">
                <img v-if="previewImage" :src="previewImage" alt="Avatar Preview" />
                <span v-else class="placeholder-text">Chọn ảnh</span>
              </div>
              <input type="file" ref="fileInput" class="hidden-input" accept="image/*" @change="handleFileChange"/>
            </div>

            <div class="form-group">
              <label class="required-label">Họ và tên</label>
              <input 
                type="text" 
                v-model="employee.tenNhanVien" 
                class="form-control" 
                :class="{ 'is-invalid': errors.tenNhanVien }"
                @input="clearError('tenNhanVien')"
                placeholder="Nhập họ tên" 
              />
              <span class="error-msg" v-if="errors.tenNhanVien">{{ errors.tenNhanVien }}</span>
            </div>
          </div>

          <div class="right-section">
            <div class="section-header">
              <h3 class="section-title">Thông tin chi tiết</h3>
              <button type="button" class="btn-qr" @click="startScan">
                 <i class="fas fa-qrcode"></i> Quét QR
              </button>
            </div>
            
            <div class="form-grid">
              <div class="form-group">
                <label class="required-label">Số CCCD</label>
                <input 
                    type="text" v-model="employee.cccd" class="form-control" 
                    :class="{ 'is-invalid': errors.cccd }" @input="clearError('cccd')"
                />
                <span class="error-msg" v-if="errors.cccd">{{ errors.cccd }}</span>
              </div>

              <div class="form-group">
                <label class="required-label">Giới tính</label>
                <div class="radio-group">
                  <label class="radio-label"><input type="radio" v-model="employee.gioiTinh" :value="true" /> Nam</label>
                  <label class="radio-label"><input type="radio" v-model="employee.gioiTinh" :value="false" /> Nữ</label>
                </div>
              </div>

              <div class="form-group">
                <label class="required-label">Ngày sinh</label>
                <input 
                    type="date" v-model="employee.ngaySinh" class="form-control" 
                    :class="{ 'is-invalid': errors.ngaySinh }" @change="clearError('ngaySinh')"
                />
                <span class="error-msg" v-if="errors.ngaySinh">{{ errors.ngaySinh }}</span>
              </div>

              <div class="form-group">
                <label class="required-label">Email</label>
                <input 
                    type="email" v-model="employee.email" class="form-control" 
                    :class="{ 'is-invalid': errors.email }" @input="clearError('email')"
                />
                <span class="error-msg" v-if="errors.email">{{ errors.email }}</span>
              </div>

              <div class="form-group">
                <label>Tỉnh/thành phố</label> 
                <select v-model="address.provinceId" @change="onProvinceChange" :class="{ 'is-invalid': errors.address && !address.provinceId }">
                  <option value="">Chọn Tỉnh/Thành</option>
                  <option v-for="p in locationData.provinces" :key="p.code" :value="p.code">{{ p.name }}</option>
                </select>
              </div>

              <div class="form-group">
                <label>Quận/huyện</label> 
                <select v-model="address.districtId" @change="onDistrictChange" :disabled="!address.provinceId" :class="{ 'is-invalid': errors.address && !address.districtId }">
                  <option value="">Chọn Quận/Huyện</option>
                  <option v-for="d in locationData.districts" :key="d.code" :value="d.code">{{ d.name }}</option>
                </select>
              </div>

              <div class="form-group">
                <label>Xã/phường/thị trấn</label> 
                <select v-model="address.wardCode" @change="clearError('address')" :disabled="!address.districtId" :class="{ 'is-invalid': errors.address && !address.wardCode }">
                  <option value="">Chọn Xã/Phường</option>
                  <option v-for="w in locationData.wards" :key="w.code" :value="w.code">{{ w.name }}</option>
                </select>
                <span class="error-msg" v-if="errors.address">{{ errors.address }}</span>
              </div>

              <div class="form-group">
                <label class="required-label">Số điện thoại</label>
                <input 
                    type="text" v-model="employee.soDienThoai" class="form-control" 
                    :class="{ 'is-invalid': errors.soDienThoai }" @input="clearError('soDienThoai')"
                />
                <span class="error-msg" v-if="errors.soDienThoai">{{ errors.soDienThoai }}</span>
              </div>

              <div class="form-group full-width">
                <label class="required-label">Địa chỉ cụ thể</label>
                <input 
                    type="text" v-model="employee.diaChiCuThe" class="form-control" placeholder="Số nhà, đường..." 
                    :class="{ 'is-invalid': errors.diaChiCuThe }" @input="clearError('diaChiCuThe')"
                />
                <span class="error-msg" v-if="errors.diaChiCuThe">{{ errors.diaChiCuThe }}</span>
              </div>
            </div>

            <div class="form-footer">
              <button type="submit" class="btn-submit">
                {{ isEditMode ? 'Cập nhật' : 'Thêm mới' }}
              </button>
            </div>
          </div>
        </form>
      </div>

      <div v-if="showScanner" class="qr-overlay">
         <div class="qr-modal">
            <h3>Đưa mã QR CCCD vào khung hình</h3>
            <div id="reader"></div>
            <button class="btn-close-qr" @click="stopScan">Đóng Camera</button>
         </div>
      </div>

    </div>
  </BasePage>
</template>

<script setup>
// ==========================================
// 1. IMPORT THƯ VIỆN & CẤU HÌNH
// ==========================================
import BasePage from '@/views/BasePage.vue';
import { ref, reactive, onMounted, onBeforeUnmount, computed } from 'vue'; 
import { useRouter, useRoute } from 'vue-router'; 
import axios from 'axios';
import { Html5QrcodeScanner } from "html5-qrcode";

const router = useRouter();
const route = useRoute(); 
const isEditMode = computed(() => !!route.params.id); // Kiểm tra đang ở chế độ sửa hay thêm

// ==========================================
// 2. KHAI BÁO STATE (DỮ LIỆU)
// ==========================================

// Dữ liệu nhân viên
const employee = reactive({
  tenNhanVien: '',
  cccd: '',
  gioiTinh: true, // true: Nam, false: Nữ
  ngaySinh: '',
  email: '',
  soDienThoai: '',
  diaChiCuThe: ''
});

// Dữ liệu địa chỉ hành chính (Dropdown)
const address = reactive({
    provinceId: '',
    districtId: '',
    wardCode: ''
});

// State quản lý LỖI (Validation) - MỚI
const errors = reactive({
    tenNhanVien: '',
    cccd: '',
    ngaySinh: '',
    email: '',
    soDienThoai: '',
    diaChiCuThe: '',
    address: '' // Lỗi chung cho 3 ô địa chỉ
});

// State quản lý file ảnh
const fileInput = ref(null);
const selectedFile = ref(null);
const previewImage = ref(null);

// State quản lý Camera QR
const showScanner = ref(false);
let html5QrcodeScanner = null;

// Mock Data cho Địa chỉ (Bạn thay bằng API thật nếu có)
const locationData = reactive({
    provinces: [], 
    districts: [], 
    wards: []
});

// ==========================================
// 3. XỬ LÝ VALIDATION (QUAN TRỌNG)
// ==========================================

// Hàm xóa lỗi khi người dùng nhập lại
const clearError = (field) => {
    errors[field] = '';
};

// Hàm kiểm tra hợp lệ toàn bộ form
const validateForm = () => {
    let isValid = true;
    
    // Reset toàn bộ lỗi trước khi check
    Object.keys(errors).forEach(key => errors[key] = '');

    // 1. Validate Họ tên
    if (!employee.tenNhanVien.trim()) {
        errors.tenNhanVien = 'Họ tên không được để trống';
        isValid = false;
    } else if (employee.tenNhanVien.length < 2) {
        errors.tenNhanVien = 'Họ tên quá ngắn';
        isValid = false;
    } else if (/\d/.test(employee.tenNhanVien)) {
        errors.tenNhanVien = 'Họ tên không được chứa số';
        isValid = false;
    }

    // 2. Validate CCCD (9 hoặc 12 số)
    if (!employee.cccd) {
        errors.cccd = 'Vui lòng nhập số CCCD';
        isValid = false;
    } else if (!/^\d{9}$|^\d{12}$/.test(employee.cccd)) {
        errors.cccd = 'CCCD phải là 9 hoặc 12 chữ số';
        isValid = false;
    }

    // 3. Validate Ngày sinh (Phải >= 18 tuổi)
    if (!employee.ngaySinh) {
        errors.ngaySinh = 'Vui lòng chọn ngày sinh';
        isValid = false;
    } else {
        const birthDate = new Date(employee.ngaySinh);
        const today = new Date();
        let age = today.getFullYear() - birthDate.getFullYear();
        const m = today.getMonth() - birthDate.getMonth();
        if (m < 0 || (m === 0 && today.getDate() < birthDate.getDate())) {
            age--;
        }
        if (age < 18) {
            errors.ngaySinh = 'Nhân viên chưa đủ 18 tuổi';
            isValid = false;
        }
    }

    // 4. Validate Email
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!employee.email) {
        errors.email = 'Email không được để trống';
        isValid = false;
    } else if (!emailRegex.test(employee.email)) {
        errors.email = 'Email không đúng định dạng';
        isValid = false;
    }

    // 5. Validate Số điện thoại (VN)
    const phoneRegex = /(84|0[3|5|7|8|9])+([0-9]{8})\b/;
    if (!employee.soDienThoai) {
        errors.soDienThoai = 'SĐT không được để trống';
        isValid = false;
    } else if (!phoneRegex.test(employee.soDienThoai)) {
        errors.soDienThoai = 'SĐT không hợp lệ (VD: 09...)';
        isValid = false;
    }

    // 7. Validate Địa chỉ cụ thể
    if (!employee.diaChiCuThe.trim()) {
        errors.diaChiCuThe = 'Địa chỉ cụ thể không được để trống';
        isValid = false;
    }

    return isValid;
};

// ==========================================
// 4. XỬ LÝ ẢNH & FILE
// ==========================================
const triggerFileInput = () => {
  fileInput.value.click();
};

const handleFileChange = (event) => {
  const file = event.target.files[0];
  if (file) {
    // Check dung lượng ảnh (ví dụ 5MB)
    if (file.size > 5 * 1024 * 1024) {
        alert("File ảnh quá lớn (tối đa 5MB)");
        return;
    }
    selectedFile.value = file;
    previewImage.value = URL.createObjectURL(file);
  }
};

// ==========================================
// 5. XỬ LÝ QUÉT MÃ QR (CCCD)
// ==========================================
const startScan = async () => {
    if (html5QrcodeScanner) {
        try { await html5QrcodeScanner.clear(); } catch (e) {}
    }
    showScanner.value = true;
    setTimeout(() => {
        try {
            const config = { 
                fps: 10,
                rememberLastUsedCamera: false,
                videoConstraints: { width: { min: 640 }, height: { min: 480 } }
            };
            html5QrcodeScanner = new Html5QrcodeScanner("reader", config, false);
            html5QrcodeScanner.render(onScanSuccess, (err) => {});
        } catch (e) {
            console.error("Lỗi Camera:", e);
            showScanner.value = false;
        }
    }, 500);
};

const stopScan = () => {
    if (html5QrcodeScanner) {
        html5QrcodeScanner.clear().catch(console.error);
    }
    showScanner.value = false;
};

const onScanSuccess = (decodedText) => {
    stopScan(); // Tắt cam ngay khi quét được
    
    // Format QR CCCD: Số|CMND cũ|Tên|NgàySinh|GiớiTinh|ĐịaChỉ|NgàyCấp
    const parts = decodedText.split('|');
    if (parts.length >= 6) {
        employee.cccd = parts[0];
        employee.tenNhanVien = parts[2].toUpperCase();
        
        // Convert ngày sinh ddMMyyyy -> yyyy-MM-dd
        const rawDate = parts[3];
        if (rawDate && rawDate.length === 8) {
            employee.ngaySinh = `${rawDate.substring(4,8)}-${rawDate.substring(2,4)}-${rawDate.substring(0,2)}`;
        }
        
        employee.gioiTinh = parts[4].trim() === 'Nam';
        employee.diaChiCuThe = parts[5];
        
        // Reset lỗi các trường vừa điền tự động
        clearError('cccd');
        clearError('tenNhanVien');
        clearError('ngaySinh');
        clearError('diaChiCuThe');
        
        // Thông báo nhỏ (hoặc bỏ đi cũng được vì điền rồi)
        alert(`Đã quét CCCD của: ${employee.tenNhanVien}`);
    } else {
        alert("Mã QR không đúng định dạng CCCD!");
    }
};

// ==========================================
// 6. XỬ LÝ ĐỊA CHỈ (MOCK DATA)
// ==========================================
const fetchProvinces = async () => {
    // Giả lập API
    locationData.provinces = [
        { code: 1, name: 'Hà Nội' }, 
        { code: 2, name: 'Thanh Hóa' },
        { code: 3, name: 'TP. Hồ Chí Minh' }
    ]; 
};

const onProvinceChange = () => {
    address.districtId = '';
    address.wardCode = '';
    locationData.wards = [];
    
    // Logic giả lập
    if (address.provinceId == 1) locationData.districts = [{code: 101, name: 'Ba Đình'}, {code: 102, name: 'Cầu Giấy'}];
    else if (address.provinceId == 2) locationData.districts = [{code: 201, name: 'TP. Thanh Hóa'}, {code: 202, name: 'Huyện Thọ Xuân'}];
    else locationData.districts = [];
};

const onDistrictChange = () => {
    address.wardCode = '';
    // Logic giả lập
    if (address.districtId == 101) locationData.wards = [{code: 1011, name: 'Kim Mã'}, {code: 1012, name: 'Đội Cấn'}];
    else if (address.districtId == 201) locationData.wards = [{code: 2011, name: 'Lam Sơn'}, {code: 2012, name: 'Đông Vệ'}];
    else locationData.wards = [{code: 999, name: 'Xã mẫu'}];
};

const getNameFromId = (id, list) => {
    if (!id || !list) return "";
    const item = list.find(x => x.code == id || x.id == id);
    return item ? item.name : "";
};

// ==========================================
// 7. SUBMIT FORM (LƯU DỮ LIỆU)
// ==========================================
const handleSubmit = async () => {
    // BƯỚC 1: Gọi Validation
    if (!validateForm()) {
        console.log("Form chưa hợp lệ, vui lòng kiểm tra lại");
        return; // Dừng lại nếu có lỗi
    }

    // BƯỚC 2: Chuẩn bị dữ liệu gửi đi
    try {
        const formData = new FormData();
        formData.append("tenNhanVien", employee.tenNhanVien.trim());
        formData.append("cccd", employee.cccd.trim());
        formData.append("email", employee.email.trim());
        formData.append("soDienThoai", employee.soDienThoai.trim());
        formData.append("gioiTinh", employee.gioiTinh);
        formData.append("ngaySinh", employee.ngaySinh); // Đã là yyyy-MM-dd
        
        // Ghép địa chỉ
        const pName = getNameFromId(address.provinceId, locationData.provinces);
        const dName = getNameFromId(address.districtId, locationData.districts);
        const wName = getNameFromId(address.wardCode, locationData.wards);
        
        formData.append("city", pName);
        formData.append("district", dName);
        formData.append("ward", wName);
        formData.append("address", employee.diaChiCuThe.trim());

        if (selectedFile.value) {
            formData.append("avatarFile", selectedFile.value);
        }

        // BƯỚC 3: Gửi API
        const id = route.params.id;
        const config = { headers: { "Content-Type": "multipart/form-data" } };
        
        if (id) {
            await axios.put(`http://localhost:8080/api/v1/nhan-vien/${id}`, formData, config);
            alert("Cập nhật thành công!"); // Thành công thì alert cũng được
        } else {
            await axios.post("http://localhost:8080/api/v1/nhan-vien", formData, config);
            alert("Thêm mới thành công!");
        }

        router.push({ name: "admin-employee-list" });

    } catch (error) {
        console.error("Lỗi submit:", error);
        // Hiển thị lỗi từ backend (nếu có)
        if (error.response?.data?.message) {
            alert("Lỗi server: " + error.response.data.message);
        } else {
            alert("Có lỗi xảy ra khi kết nối server!");
        }
    }
};

// ==========================================
// 8. LIFECYCLE & HELPERS
// ==========================================
const goBack = () => {
    router.push({ name: 'admin-employee-list' });
};

const fillFormData = async (id) => {
  try {
    const response = await axios.get(`http://localhost:8080/api/v1/nhan-vien/${id}`);
    const data = response.data;

    employee.tenNhanVien = data.tenNhanVien;
    employee.email = data.email;
    employee.soDienThoai = data.soDienThoai;
    employee.cccd = data.cccd || '';
    employee.gioiTinh = data.gioiTinh;
    employee.diaChiCuThe = data.diaChi || ''; 
    
    // Xử lý ngày sinh trả về (Array hoặc String)
    if (data.ngaySinh) {
        if (Array.isArray(data.ngaySinh)) {
            // Backend trả về [2000, 1, 15]
            const y = data.ngaySinh[0];
            const m = String(data.ngaySinh[1]).padStart(2, '0');
            const d = String(data.ngaySinh[2]).padStart(2, '0');
            employee.ngaySinh = `${y}-${m}-${d}`;
        } else {
            employee.ngaySinh = data.ngaySinh;
        }
    }

    if (data.maNhanVien) {
        previewImage.value = `http://localhost:8080/api/v1/nhan-vien/images/${data.maNhanVien}.jpg`;
    }
  } catch (error) {
    console.error("Lỗi tải thông tin:", error);
  }
};

onMounted(async () => {
    await fetchProvinces();
    const id = route.params.id;
    if (id) {
         await fillFormData(id);
    }
});

onBeforeUnmount(() => {
    if (html5QrcodeScanner) {
        html5QrcodeScanner.clear().catch(e => console.error(e));
    }
});
</script>

<style scoped>
/* ========================================= */
/* 1. CODE GỐC CỦA BẠN (GIỮ NGUYÊN)          */
/* ========================================= */
.create-employee-container {
  padding: 20px;
  background-color: #f5f5f5;
  min-height: 100vh;
  font-family: Arial, sans-serif;
}

/* Header */
.page-header { margin-bottom: 20px; }
.breadcrumb { font-size: 18px; color: #333; font-weight: bold; }
.back-link { cursor: pointer; color: #666; transition: 0.2s; }
.back-link:hover { color: #2c3e50; text-decoration: underline; }
.divider { margin: 0 5px; color: #999; }
.current { color: #999; }

/* Main Card */
.main-card {
  background: white;
  border-radius: 8px;
  padding: 30px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.05);
}

.form-layout {
  display: flex;
  gap: 40px;
}

/* --- Left Section (30%) --- */
.left-section {
  width: 30%;
  border-right: 1px solid #eee;
  padding-right: 30px;
  display: flex;
  flex-direction: column;
  align-items: center; 
}

.avatar-upload {
  margin: 20px 0;
  cursor: pointer;
}

.avatar-circle {
  width: 150px;
  height: 150px;
  border: 2px dashed #ddd;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  position: relative;
  transition: border-color 0.3s;
}

.avatar-circle:hover { border-color: #2c3e50; }

.avatar-circle img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.placeholder-text {
  color: #999;
  font-size: 14px;
}

.hidden-input { display: none; }

/* --- Right Section (70%) --- */
.right-section { width: 70%; }

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-title {
  font-size: 18px;
  font-weight: bold;
  color: #2c3e50;
  margin: 0;
}

/* Nút mở QR */
.btn-qr {
  background: white;
  border: 1px solid #2c3e50;
  color: #2c3e50;
  padding: 6px 15px;
  border-radius: 4px;
  font-weight: bold;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 5px;
  transition: 0.3s;
}
.btn-qr:hover { background-color: #2c3e50; color: white; }

/* Form Grid */
.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
  gap: 20px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-group.full-width { grid-column: span 3; }

.required-label {
  font-weight: 600;
  font-size: 14px;
  color: #555;
}
.required-label::before {
  content: "*";
  color: red;
  margin-right: 4px;
}

.form-control {
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
  outline: none;
  width: 100%;
}
.form-control:focus { border-color: #2c3e50; }

/* Radio Gender */
.radio-group {
  display: flex;
  gap: 20px;
  align-items: center;
  height: 40px; 
}
.radio-label {
  display: flex;
  align-items: center;
  gap: 5px;
  cursor: pointer;
}

/* Submit Button */
.form-footer {
  margin-top: 30px;
  display: flex;
  justify-content: flex-end;
}

.btn-submit {
  background-color: #2c3e50;
  color: white;
  padding: 12px 24px;
  border: none;
  border-radius: 6px;
  font-weight: bold;
  font-size: 16px;
  cursor: pointer;
  transition: background 0.3s;
}

.btn-submit:hover { background-color: #1a252f; }

/* Responsive */
@media (max-width: 992px) {
  .form-layout { flex-direction: column; }
  .left-section, .right-section { width: 100%; border-right: none; }
  .left-section { border-bottom: 1px solid #eee; padding-bottom: 20px; }
  .form-grid { grid-template-columns: 1fr; } 
  .form-group.full-width { grid-column: span 1; }
}

/* ========================================= */
/* 2. PHẦN BỔ SUNG: CSS CHO QR MODAL         */
/* ========================================= */

/* Màn hình đen mờ che phủ toàn trang */
.qr-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background-color: rgba(0, 0, 0, 0.7); /* Đen mờ 70% */
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 9999; /* Đảm bảo nằm trên cùng */
}

/* Hộp thoại chứa camera */
.qr-modal {
  background: white;
  padding: 20px;
  border-radius: 12px;
  width: 450px;
  max-width: 90%;
  box-shadow: 0 10px 25px rgba(0,0,0,0.5);
  display: flex;
  flex-direction: column;
  align-items: center;
}

.qr-modal h3 {
  margin-top: 0;
  margin-bottom: 15px;
  color: #333;
  font-size: 18px;
  font-weight: bold;
}

/* Khung hiển thị Camera (id="reader") */
#reader {
  width: 100%;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #ddd;
  margin-bottom: 15px;
}

/* Nút đóng camera */
.btn-close-qr {
  background-color: #ef4444; /* Màu đỏ */
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 6px;
  font-weight: bold;
  cursor: pointer;
  transition: background 0.2s;
  width: 100%;
}

.btn-close-qr:hover {
  background-color: #dc2626;
}
/* Màu viền đỏ khi có lỗi */
.form-control.is-invalid {
  border-color: #dc3545 !important;
  background-color: #fff8f8;
}

/* Chữ thông báo lỗi bên dưới */
.error-msg {
  color: #dc3545;
  font-size: 0.85rem;
  margin-top: 4px;
  display: block;
}
</style>