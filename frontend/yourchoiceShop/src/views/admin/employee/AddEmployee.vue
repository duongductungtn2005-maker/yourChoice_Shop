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
              <input 
                type="file" ref="fileInput" class="hidden-input" 
                accept="image/*" @change="handleFileChange"
              />
            </div>

            <div class="form-group">
              <label class="required-label">Họ và tên</label>
              <input type="text" v-model="employee.tenNhanVien" class="form-control" placeholder="Nhập họ tên" required />
            </div>
          </div>

          <div class="right-section">
            <div class="section-header">
              <h3 class="section-title">Thông tin chi tiết</h3>
              <div style="display: flex; gap: 10px">
                  <button type="button" class="btn-qr" @click="startScan">
                    <i class="fas fa-qrcode"></i> Quét QR
                  </button>
              </div>
            </div>
            
            <div class="form-grid">
              <div class="form-group">
                <label class="required-label">Số CCCD</label>
                <input type="text" v-model="employee.cccd" class="form-control" required />
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
                <input type="date" v-model="employee.ngaySinh" class="form-control" required />
              </div>

              <div class="form-group">
                <label class="required-label">Email</label>
                <input type="email" v-model="employee.email" class="form-control" required />
              </div>

              <div class="form-group">
                <label>Tỉnh/thành phố</label> <select v-model="address.provinceId" @change="onProvinceChange" class="form-control">
                  <option value="">Chọn Tỉnh/Thành</option>
                  <option v-for="p in locationData.provinces" :key="p.code" :value="p.code">{{ p.name }}</option>
                </select>
              </div>

              <div class="form-group">
                <label>Quận/huyện</label> <select v-model="address.districtId" @change="onDistrictChange" class="form-control" :disabled="!address.provinceId">
                  <option value="">Chọn Quận/Huyện</option>
                  <option v-for="d in locationData.districts" :key="d.code" :value="d.code">{{ d.name }}</option>
                </select>
              </div>

              <div class="form-group">
                <label>Xã/phường/thị trấn</label> <select v-model="address.wardCode" class="form-control" :disabled="!address.districtId">
                  <option value="">Chọn Xã/Phường</option>
                  <option v-for="w in locationData.wards" :key="w.code" :value="w.code">{{ w.name }}</option>
                </select>
              </div>
              <div class="form-group">
                <label class="required-label">Số điện thoại</label>
                <input type="text" v-model="employee.soDienThoai" class="form-control" required />
              </div>

              <div class="form-group full-width">
                <label class="required-label">Địa chỉ cụ thể</label>
                <input type="text" v-model="employee.diaChiCuThe" class="form-control" placeholder="Số nhà, đường..." required />
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
import BasePage from '@/views/BasePage.vue';
// Gộp tất cả vào một dòng này
import { ref, reactive, onMounted, onBeforeUnmount } from 'vue'; 
import { useRouter, useRoute } from 'vue-router'; 
import axios from 'axios';
import { Html5QrcodeScanner } from "html5-qrcode";

// ... (Phần còn lại giữ nguyên)

const router = useRouter();
const route = useRoute(); 

// --- 1. State quản lý dữ liệu Form ---
const employee = reactive({
  tenNhanVien: '',
  cccd: '',
  gioiTinh: true, // true: Nam, false: Nữ
  ngaySinh: '',
  email: '',
  soDienThoai: '',
  diaChiCuThe: ''
});

// State quản lý địa chỉ riêng để xử lý dropdown
const address = reactive({
    provinceId: '',
    districtId: '',
    wardCode: ''
});

// State quản lý ảnh
const fileInput = ref(null);
const selectedFile = ref(null);
const previewImage = ref(null);

// State quản lý Camera QR
const showScanner = ref(false);
let html5QrcodeScanner = null;

// --- 2. Xử lý Ảnh đại diện ---
const triggerFileInput = () => {
  fileInput.value.click();
};

const handleFileChange = (event) => {
  const file = event.target.files[0];
  if (file) {
    selectedFile.value = file;
    previewImage.value = URL.createObjectURL(file);
  }
};

// --- 3. LOGIC QUÉT MÃ QR (NEW) ---
// --- LOGIC CAMERA AN TOÀN HƠN ---
// Thay thế toàn bộ hàm startScan này vào code cũ
const startScan = async () => {
    // 1. Dọn dẹp camera cũ
    if (html5QrcodeScanner) {
        try { await html5QrcodeScanner.clear(); } catch (e) {}
    }

    showScanner.value = true;
    
    setTimeout(() => {
        try {
            // Cấu hình Camera chi tiết
            const config = { 
                fps: 10,
                // qrbox: { width: 300, height: 300 }, <--- Xóa hoặc comment dòng này
                rememberLastUsedCamera: false,
                videoConstraints: {
                    width: { min: 640, ideal: 1280 }, // HD 720p là đủ cho Laptop
                    height: { min: 480, ideal: 720 }
                }
            };

            html5QrcodeScanner = new Html5QrcodeScanner("reader", config, false);
            
            html5QrcodeScanner.render(onScanSuccess, (errorMessage) => {
                // Đang quét...
            });
        } catch (e) {
            console.error("Lỗi:", e);
            showScanner.value = false;
        }
    }, 500);
};

const stopScan = () => {
    if (html5QrcodeScanner) {
        html5QrcodeScanner.clear().then(() => {
            console.log("Đã tắt camera thành công.");
        }).catch(error => {
            console.error("Lỗi khi tắt camera:", error);
        });
    }
    showScanner.value = false;
};

// --- QUAN TRỌNG: Tắt camera khi người dùng rời khỏi trang ---
onBeforeUnmount(() => {
    if (html5QrcodeScanner) {
        html5QrcodeScanner.clear().catch(e => console.error(e));
    }
});
const onScanSuccess = (decodedText, decodedResult) => {
    // Tắt camera ngay khi quét được
    stopScan();
    
    // Cấu trúc CCCD: SốCCCD|CMND cũ|Tên|NgàySinh|GiớiTinh|ĐịaChỉ|NgàyCấp
    const parts = decodedText.split('|');

    if (parts.length >= 6) {
        // 1. Số CCCD
        employee.cccd = parts[0];

        // 2. Họ tên (Viết hoa)
        employee.tenNhanVien = parts[2].toUpperCase();

        // 3. Ngày sinh: Convert từ ddMMyyyy -> yyyy-MM-dd
        const rawDate = parts[3];
        if (rawDate && rawDate.length === 8) {
            const day = rawDate.substring(0, 2);
            const month = rawDate.substring(2, 4);
            const year = rawDate.substring(4, 8);
            employee.ngaySinh = `${year}-${month}-${day}`;
        }

        // 4. Giới tính
        employee.gioiTinh = parts[4].trim() === 'Nam';

        // 5. Địa chỉ
        // Do không map được ID tỉnh/huyện tự động, ta điền vào ô địa chỉ cụ thể
        employee.diaChiCuThe = parts[5];

        alert(`Đã quét thành công CCCD của: ${employee.tenNhanVien}.\nVui lòng chọn lại Tỉnh/Huyện/Xã thủ công.`);
    } else {
        alert("Mã QR không đúng định dạng CCCD gắn chip!");
    }
};

const onScanFailure = (error) => {
    // Hàm này chạy liên tục khi camera đang quét mà chưa bắt được mã -> Không cần làm gì
};


// --- 4. Validate & Helper ---

const isValidEmail = (email) => {
    const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return re.test(String(email).toLowerCase());
};

const isValidCCCD = (cccd) => {
    return /^\d{9}$|^\d{12}$/.test(cccd);
};

const isValidBirthDate = (dateString) => {
    if (!dateString) return false;
    const birthDate = new Date(dateString);
    const today = new Date();
    if (birthDate >= today) return false;
    return true;
};

// Hàm helper: Chuyển ngày về chuẩn yyyy-MM-dd
const formatDate = (dateValue) => {
  if (!dateValue) return null;
  const d = new Date(dateValue);
  if (isNaN(d.getTime())) return null; 
  return d.toISOString().split('T')[0];
};

const getNameFromId = (id, list) => {
    if (!id || !list || list.length === 0) return "";
    const item = list.find(x => x.code == id || x.id == id);
    return item ? item.name : "";
};

// --- 5. Xử lý Địa chỉ (Mock Data) ---
const locationData = reactive({
    provinces: [], districts: [], wards: []
});

const fetchProvinces = async () => {
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
    
    if (address.provinceId == 1) locationData.districts = [{code: 101, name: 'Ba Đình'}, {code: 102, name: 'Cầu Giấy'}];
    else if (address.provinceId == 2) locationData.districts = [{code: 201, name: 'TP. Thanh Hóa'}, {code: 202, name: 'Huyện 36'}];
    else locationData.districts = [];
};

const onDistrictChange = () => {
    address.wardCode = '';
    if (address.districtId == 202) locationData.wards = [{code: 301, name: '36 Trấn'}, {code: 302, name: 'Xã Mới'}];
    else locationData.wards = [{code: 401, name: 'Phường A'}, {code: 402, name: 'Phường B'}];
};


// --- 6. HÀM SUBMIT FORM ---
const handleSubmit = async () => {
    // --- Validate ---
    if (!employee.tenNhanVien.trim()) return alert("Vui lòng nhập họ tên!");
    if (!employee.cccd || !isValidCCCD(employee.cccd)) return alert("CCCD không hợp lệ!");
    if (!isValidBirthDate(employee.ngaySinh)) return alert("Ngày sinh không hợp lệ!");
    if (!isValidEmail(employee.email)) return alert("Email sai định dạng!");
    // if (!address.provinceId || !address.districtId || !address.wardCode) return alert("Vui lòng chọn đầy đủ địa chỉ hành chính!");
    if (!employee.soDienThoai || !/^\d{10,11}$/.test(employee.soDienThoai)) return alert("SĐT không hợp lệ!");
    if (!employee.diaChiCuThe.trim()) return alert("Nhập địa chỉ cụ thể!");

    try {
        const formData = new FormData();
        formData.append("tenNhanVien", employee.tenNhanVien.trim());
        formData.append("cccd", employee.cccd.trim());
        formData.append("email", employee.email.trim());
        formData.append("soDienThoai", employee.soDienThoai.trim());
        formData.append("gioiTinh", employee.gioiTinh);
        
        const formattedDate = formatDate(employee.ngaySinh);
        if (formattedDate) formData.append("ngaySinh", formattedDate);

        // Lấy tên địa chỉ
        const provinceName = getNameFromId(address.provinceId, locationData.provinces);
        const districtName = getNameFromId(address.districtId, locationData.districts);
        const wardName = getNameFromId(address.wardCode, locationData.wards);

        formData.append("city", provinceName);
        formData.append("district", districtName);
        formData.append("ward", wardName);
        formData.append("address", employee.diaChiCuThe.trim());

        if (selectedFile.value) {
            formData.append("avatarFile", selectedFile.value);
        }

        // Call API
        const id = route.params.id;
        const config = { headers: { "Content-Type": "multipart/form-data" } };
        
        if (id) {
            await axios.put(`http://localhost:8080/api/v1/nhan-vien/${id}`, formData, config);
            alert("Cập nhật thành công!");
        } else {
            await axios.post("http://localhost:8080/api/v1/nhan-vien", formData, config);
            alert("Thêm mới thành công!");
        }

        router.push({ name: "admin-employee-list" });

    } catch (error) {
        console.error("Lỗi submit:", error);
        const msg = error.response?.data?.message || "Có lỗi xảy ra!";
        alert(`Lỗi: ${msg}`);
    }
};

const goBack = () => {
    router.push({ name: 'admin-employee-list' });
};

// --- 7. Logic Load dữ liệu (Edit Mode) ---
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

    if (data.ngaySinh) {
        employee.ngaySinh = Array.isArray(data.ngaySinh) 
            ? new Date(data.ngaySinh[0], data.ngaySinh[1]-1, data.ngaySinh[2]).toISOString().split('T')[0]
            : data.ngaySinh;
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
</style>