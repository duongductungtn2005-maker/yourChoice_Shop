<template>
    <BasePage>
    <div class="create-employee-container">
        
        <div class="page-header">
        <h2 class="breadcrumb">
            <span class="back-link" @click="goBack">Nhân viên</span> 
            <span class="divider">/</span> 
            <span class="current">Thêm nhân viên</span>
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
                type="file" 
                ref="fileInput" 
                class="hidden-input" 
                accept="image/*" 
                @change="handleFileChange"
                />
            </div>

            <div class="form-group">
                <label class="required-label">Họ và tên</label>
                <input 
                type="text" 
                v-model="employee.tenNhanVien" 
                class="form-control" 
                placeholder="Nhập họ tên" 
                required
                />
            </div>
            </div>

            <div class="right-section">
            <div class="section-header">
                <h3 class="section-title">Thông tin chi tiết</h3>
                <button type="button" class="btn-qr" @click="scanQR">Quét QR</button>
            </div>
            
            <div class="form-grid">
                <div class="form-group">
                <label class="required-label">Số CCCD</label>
                <input type="text" v-model="employee.cccd" class="form-control" required />
                </div>

                <div class="form-group">
                <label class="required-label">Giới tính</label>
                <div class="radio-group">
                    <label class="radio-label">
                    <input type="radio" v-model="employee.gioiTinh" :value="true" /> Nam
                    </label>
                    <label class="radio-label">
                    <input type="radio" v-model="employee.gioiTinh" :value="false" /> Nữ
                    </label>
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
                <label class="required-label">Tỉnh/thành phố</label>
                <select v-model="address.provinceId" @change="onProvinceChange" class="form-control">
                    <option value="">Chọn Tỉnh/Thành</option>
                    <option v-for="p in locationData.provinces" :key="p.code" :value="p.code">{{ p.name }}</option>
                </select>
                </div>

                <div class="form-group">
                <label class="required-label">Quận/huyện</label>
                <select v-model="address.districtId" @change="onDistrictChange" class="form-control" :disabled="!address.provinceId">
                    <option value="">Chọn Quận/Huyện</option>
                    <option v-for="d in locationData.districts" :key="d.code" :value="d.code">{{ d.name }}</option>
                </select>
                </div>

                <div class="form-group">
                <label class="required-label">Xã/phường/thị trấn</label>
                <select v-model="address.wardCode" class="form-control" :disabled="!address.districtId">
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
                <button type="button" class="btn-submit" @click="handleSubmit">
                    Thêm nhân viên
                </button>
            </div>
            </div>
        </form>
        </div>
    </div>
  </BasePage>
</template>

<script setup>
import BasePage from '@/views/BasePage.vue';
import { ref, reactive, onMounted } from 'vue';
// 1. Thêm useRoute vào đây
import { useRouter, useRoute } from 'vue-router'; 
import axios from 'axios';

const router = useRouter();
const route = useRoute(); // 2. Khởi tạo route để lấy params
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

// --- 2. Xử lý Ảnh đại diện ---
const triggerFileInput = () => {
  fileInput.value.click();
};

const handleFileChange = (event) => {
  const file = event.target.files[0];
  if (file) {
    selectedFile.value = file;
    // Tạo URL để preview ảnh ngay lập tức
    previewImage.value = URL.createObjectURL(file);
  }
};

// --- 3. Xử lý Địa chỉ (Mock Data hoặc gọi API GHN) ---
const locationData = reactive({
    provinces: [],
    districts: [],
    wards: []
});

// Mock data giả lập (Thực tế bạn sẽ gọi API)
const fetchProvinces = async () => {
    // Demo: axios.get('https://provinces.open-api.vn/api/p/')
    locationData.provinces = [
        { code: 1, name: 'Hà Nội' }, 
        { code: 2, name: 'Thanh Hóa' },
        { code: 3, name: 'TP. Hồ Chí Minh' }
    ]; 
};

const onProvinceChange = () => {
    // Reset huyện/xã khi đổi tỉnh
    address.districtId = '';
    address.wardCode = '';
    locationData.wards = [];
    
    // Demo logic lấy huyện theo tỉnh
    if (address.provinceId == 1) locationData.districts = [{code: 101, name: 'Ba Đình'}, {code: 102, name: 'Cầu Giấy'}];
    else if (address.provinceId == 2) locationData.districts = [{code: 201, name: 'TP. Thanh Hóa'}, {code: 202, name: 'Huyện 36'}];
    else locationData.districts = [];
};

const onDistrictChange = () => {
    // Reset xã khi đổi huyện
    address.wardCode = '';
    
    // Demo logic lấy xã theo huyện
    if (address.districtId == 202) locationData.wards = [{code: 301, name: '36 Trấn'}, {code: 302, name: 'Xã Mới'}];
    else locationData.wards = [{code: 401, name: 'Phường A'}, {code: 402, name: 'Phường B'}];
};

// Hàm helper: Chuyển ngày về chuẩn yyyy-MM-dd
const formatDate = (dateValue) => {
  if (!dateValue) return null;
  const d = new Date(dateValue);
  if (isNaN(d.getTime())) return null; 
  // Lấy chuỗi yyyy-MM-dd (cắt bỏ phần giờ phía sau T)
  return d.toISOString().split('T')[0];
};

const handleSubmit = async () => {
  try {
    const formData = new FormData();

    // 1. Sửa lỗi: Bỏ ".value" vì employee là reactive
    formData.append("tenNhanVien", employee.tenNhanVien || "");
    formData.append("email", employee.email || "");
    formData.append("soDienThoai", employee.soDienThoai || "");
    
    if (employee.cccd) {
        formData.append("cccd", employee.cccd);
    }

    // 2. Xử lý ngày sinh
    // Lưu ý: formatDate là hàm phụ trợ bác đã có
    const formattedDate = formatDate(employee.ngaySinh);
    if (formattedDate) {
        formData.append("ngaySinh", formattedDate); 
    }

    // 3. Giới tính
    formData.append("gioiTinh", employee.gioiTinh);

    // 4. Địa chỉ (Lấy từ biến reactive employee, không có .value)
    // Lưu ý: Trong code template bác dùng v-model="employee.diaChiCuThe" 
    // Nhưng API backend cần "address", "ward", "district", "city"
    // Bác cần map đúng biến address bác đã khai báo riêng
    formData.append("address", employee.diaChiCuThe || ""); 
    
    // address là reactive riêng bác khai báo ở dưới (const address = reactive...)
    // Vì nó là reactive nên cũng KHÔNG CẦN .value
    formData.append("ward", address.wardCode || "");       
    formData.append("district", address.districtId || ""); 
    formData.append("city", address.provinceId || "");      

    // 5. File ảnh
    // selectedFile là ref (const selectedFile = ref(null)) -> CÁI NÀY CẦN .value
    if (selectedFile.value) {
      formData.append("avatarFile", selectedFile.value);
    }

    // --- Gửi API ---
    // route cần import { useRoute } from 'vue-router';
    const id = route.params.id; 
    
    if (id) {
      console.log("Đang cập nhật ID:", id);
      await axios.put(`http://localhost:8080/api/v1/nhan-vien/${id}`, formData, {
        headers: { "Content-Type": "multipart/form-data" },
      });
      alert("Cập nhật thành công!");
    } else {
      console.log("Đang tạo mới...");
      await axios.post("http://localhost:8080/api/v1/nhan-vien", formData, {
        headers: { "Content-Type": "multipart/form-data" },
      });
      alert("Thêm mới thành công!");
    }

    router.push({ name: "admin-employee-list" });

  } catch (error) {
    console.error("Lỗi submit:", error);
    if (error.response && error.response.data) {
        alert("Lỗi Backend: " + JSON.stringify(error.response.data));
    } else {
        alert("Có lỗi xảy ra, vui lòng kiểm tra console.");
    }
  }
};

const goBack = () => {
    router.push({ name: 'admin-employee-list' });
};

const scanQR = () => {
    alert("Chức năng quét QR Căn cước công dân (Đang phát triển)");
};

// --- 4. Logic Load dữ liệu khi Sửa (Edit Mode) ---
const fillFormData = async (id) => {
  try {
    const response = await axios.get(`http://localhost:8080/api/v1/nhan-vien/${id}`);
    const data = response.data;

    // 1. Fill thông tin cơ bản
    employee.tenNhanVien = data.tenNhanVien;
    employee.email = data.email;
    employee.soDienThoai = data.soDienThoai;
    employee.cccd = data.cccd || ''; // Nếu backend chưa có thì để rỗng
    employee.gioiTinh = data.gioiTinh;
    employee.diaChiCuThe = data.diaChi || ''; // Backend trả về chuỗi địa chỉ gộp

    // 2. Xử lý ngày sinh (Convert từ Array/String về yyyy-MM-dd)
    // Nếu backend trả về [2000, 1, 30] hoặc "2000-01-30"
    if (data.ngaySinh) {
        employee.ngaySinh = Array.isArray(data.ngaySinh) 
            ? new Date(data.ngaySinh[0], data.ngaySinh[1]-1, data.ngaySinh[2]).toISOString().split('T')[0]
            : data.ngaySinh;
    }

    // 3. Hiển thị ảnh cũ (Preview)
    if (data.maNhanVien) {
        // Gọi lại hàm lấy ảnh từ trang danh sách
        previewImage.value = `http://localhost:8080/api/v1/nhan-vien/images/${data.maNhanVien}.jpg`;
    }

    // 4. Lưu ý về Địa chỉ (Dropdown): 
    // Vì DB chỉ lưu chuỗi text (ví dụ: "Hà Nội, Ba Đình..."), 
    // nên rất khó để tự động chọn lại đúng Dropdown Tỉnh/Huyện/Xã trừ khi Backend lưu riêng ID của chúng.
    // Tạm thời ta chỉ hiển thị địa chỉ cũ vào ô "Địa chỉ cụ thể" để người dùng tham khảo.
    
  } catch (error) {
    console.error("Không tải được thông tin nhân viên:", error);
    alert("Không tìm thấy nhân viên này!");
    router.push({ name: 'admin-employee' });
  }
};
onMounted(async () => {
    // 1. Load danh sách tỉnh thành
    await fetchProvinces();

    // 2. Lấy ID từ URL
    const id = route.params.id; // Bây giờ biến route đã tồn tại, sẽ không lỗi nữa
    
    // 3. Nếu có ID -> Gọi hàm đổ dữ liệu cũ vào form (Chế độ Sửa)
    if (id) {
        // Đảm bảo bác đã copy hàm fillFormData mình gửi ở câu trả lời trước nhé
        if (typeof fillFormData === 'function') {
             await fillFormData(id);
        } else {
             console.warn("Chưa có hàm fillFormData, vui lòng bổ sung logic load dữ liệu cũ.");
        }
    }
});
</script>

<style scoped>
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
  align-items: center; /* Căn giữa ảnh */
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

.avatar-circle:hover {
  border-color: #2c3e50;
}

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
.right-section {
  width: 70%;
}

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

.btn-qr {
  background: white;
  border: 1px solid #2c3e50;
  color: #2c3e50;
  padding: 6px 15px;
  border-radius: 4px;
  font-weight: bold;
  cursor: pointer;
}
.btn-qr:hover { background-color: #f0f4f8; }

/* Form Grid */
.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr; /* 3 cột */
  gap: 20px;
}

/* Các trường chiếm 1 cột, hoặc full width */
.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

/* Riêng phần địa chỉ cụ thể chiếm hết dòng */
.form-group.full-width {
  grid-column: span 3;
}

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
  height: 40px; /* Để bằng chiều cao input */
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
  background-color: #2c3e50; /* Màu Navy chủ đạo */
  color: white;
  padding: 12px 24px;
  border: none;
  border-radius: 6px;
  font-weight: bold;
  font-size: 16px;
  cursor: pointer;
  transition: background 0.3s;
}

.btn-submit:hover {
  background-color: #1a252f;
}

/* Responsive */
@media (max-width: 992px) {
  .form-layout { flex-direction: column; }
  .left-section, .right-section { width: 100%; border-right: none; }
  .left-section { border-bottom: 1px solid #eee; padding-bottom: 20px; }
  .form-grid { grid-template-columns: 1fr; } /* Mobile về 1 cột */
  .form-group.full-width { grid-column: span 1; }
}
</style>