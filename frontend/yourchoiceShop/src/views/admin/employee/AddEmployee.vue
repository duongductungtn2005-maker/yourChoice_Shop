<template>
  <div class="create-employee-page">
    <div class="header-section">
      <!-- <div class="breadcrumb">
        <span class="text-gray cursor-pointer" @click="goBack">Nhân viên</span>
        <span class="divider">/</span>
        <span class="active">{{ isEditMode ? 'Cập nhật nhân viên' : 'Thêm nhân viên' }}</span>
      </div> -->
            <h1 class="page-title">Quản lý nhân viên / Thêm nhân viên</h1>

    </div>

    <div class="card">
      <div class="card-body">
        <form @submit.prevent="handleSubmit">
          <div class="layout-grid">
            
            <div class="left-col">
              <h3 class="section-title">Thông tin nhân viên</h3>
              
              <div class="avatar-upload-container">
                <div class="avatar-preview" @click="triggerFileInput">
                    <img v-if="previewImage" :src="previewImage" alt="Avatar Preview" class="avatar-img">
                    
                    <div v-else class="upload-placeholder">
                        <i class="fas fa-camera"></i>
                        <span>Chọn ảnh</span>
                    </div>
                </div>

                <input 
                    type="file" 
                    ref="fileInput" 
                    class="hidden-input" 
                    accept="image/*" 
                    @change="handleFileChange"
                >
              </div>

              <div class="form-group">
                <label class="required">Họ và tên</label>
                <input type="text" v-model="employee.tenNhanVien" class="form-control" placeholder="Nhập họ tên" 
                       :class="{ 'is-invalid': errors.tenNhanVien }" @input="clearError('tenNhanVien')">
                <span class="error-msg" v-if="errors.tenNhanVien">{{ errors.tenNhanVien }}</span>
              </div>
            </div>

            <div class="right-col">
              <div class="section-header-row">
                 <h3 class="section-title">Thông tin chi tiết</h3>
                 <button type="button" class="btn btn-outline btn-sm" @click="startScan">
                    <i class="fas fa-qrcode"></i> Quét QR
                 </button>
              </div>

              <div class="form-row">
                <div class="form-group half">
                  <label class="required">Số CCCD</label>
                  <input type="text" v-model="employee.cccd" class="form-control" :class="{ 'is-invalid': errors.cccd }" @input="clearError('cccd')">
                  <span class="error-msg" v-if="errors.cccd">{{ errors.cccd }}</span>
                </div>
                <div class="form-group half">
                  <label class="required">Giới tính</label>
                  <div class="radio-group">
                    <label class="radio-item"><input type="radio" :value="true" v-model="employee.gioiTinh"> Nam</label>
                    <label class="radio-item"><input type="radio" :value="false" v-model="employee.gioiTinh"> Nữ</label>
                  </div>
                </div>
              </div>

              <div class="form-row">
                <div class="form-group half">
                  <label class="required">Ngày sinh</label>
                  <input type="date" v-model="employee.ngaySinh" class="form-control" :class="{ 'is-invalid': errors.ngaySinh }" @change="clearError('ngaySinh')">
                  <span class="error-msg" v-if="errors.ngaySinh">{{ errors.ngaySinh }}</span>
                </div>
                <div class="form-group half">
                  <label class="required">Email</label>
                  <input type="email" v-model="employee.email" class="form-control" :class="{ 'is-invalid': errors.email }" @input="clearError('email')">
                  <span class="error-msg" v-if="errors.email">{{ errors.email }}</span>
                </div>
              </div>

              <div class="form-row">
                <div class="form-group third">
                   <label>Tỉnh/Thành</label>
                   <select v-model="address.provinceId" @change="onProvinceChange" class="form-control">
                      <option value="">-- Tỉnh --</option>
                      <option v-for="p in locationData.provinces" :key="p.code" :value="p.code">{{ p.name }}</option>
                   </select>
                </div>
                <div class="form-group third">
                   <label>Quận/Huyện</label>
                   <select v-model="address.districtId" @change="onDistrictChange" class="form-control" :disabled="!address.provinceId">
                      <option value="">-- Huyện --</option>
                      <option v-for="d in locationData.districts" :key="d.code" :value="d.code">{{ d.name }}</option>
                   </select>
                </div>
                <div class="form-group third">
                   <label>Xã/Phường</label>
                   <select v-model="address.wardCode" class="form-control" :disabled="!address.districtId">
                      <option value="">-- Xã --</option>
                      <option v-for="w in locationData.wards" :key="w.code" :value="w.code">{{ w.name }}</option>
                   </select>
                </div>
              </div>

              <div class="form-row">
                 <div class="form-group half">
                    <label class="required">Số điện thoại</label>
                    <input type="text" v-model="employee.soDienThoai" class="form-control" :class="{ 'is-invalid': errors.soDienThoai }" @input="clearError('soDienThoai')">
                    <span class="error-msg" v-if="errors.soDienThoai">{{ errors.soDienThoai }}</span>
                 </div>
                 <div class="form-group half">
                    <label class="required">Địa chỉ cụ thể</label>
                    <input type="text" v-model="employee.diaChiCuThe" class="form-control" placeholder="Số nhà, đường..." :class="{ 'is-invalid': errors.diaChiCuThe }" @input="clearError('diaChiCuThe')">
                    <span class="error-msg" v-if="errors.diaChiCuThe">{{ errors.diaChiCuThe }}</span>
                 </div>
              </div>

              <div class="form-actions">
                 <button type="button" class="btn btn-outline" @click="goBack">Hủy</button>
                 <button type="submit" class="btn btn-primary">
                    {{ isEditMode ? 'Cập nhật' : 'Thêm nhân viên' }}
                 </button>
              </div>
            </div>
          </div>
        </form>
      </div>
    </div>

    <div v-if="showScanner" class="qr-overlay">
       <div class="qr-modal">
          <h3>Quét mã QR CCCD</h3>
          <div id="reader"></div>
          <button class="btn btn-danger w-100 mt-3" @click="stopScan">Đóng</button>
       </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount, computed } from 'vue'; 
import { useRouter, useRoute } from 'vue-router'; 
import request from '@/services/request'; 
import axios from 'axios'; 
import Swal from 'sweetalert2';
import { Html5QrcodeScanner } from "html5-qrcode";

const router = useRouter();
const route = useRoute(); 
const isEditMode = computed(() => !!route.params.id);

// STATE
const employee = reactive({ tenNhanVien: '', cccd: '', gioiTinh: true, ngaySinh: '', email: '', soDienThoai: '', diaChiCuThe: '' });
const address = reactive({ provinceId: '', districtId: '', wardCode: '' });
const errors = reactive({});

// STATE ẢNH
const fileInput = ref(null);
const selectedFile = ref(null);
const previewImage = ref(null); // Biến chứa URL ảnh để hiển thị

const showScanner = ref(false);
let html5QrcodeScanner = null;
const locationData = reactive({ provinces: [], districts: [], wards: [] });

// VALIDATION
const clearError = (f) => errors[f] = '';
const validateForm = () => {
    let isValid = true;
    Object.keys(errors).forEach(k => errors[k] = '');
    if (!employee.tenNhanVien.trim()) { errors.tenNhanVien = 'Họ tên trống'; isValid = false; }
    if (!employee.cccd) { errors.cccd = 'CCCD trống'; isValid = false; }
    if (!employee.email) { errors.email = 'Email trống'; isValid = false; }
    if (!employee.soDienThoai) { errors.soDienThoai = 'SĐT trống'; isValid = false; }
    return isValid;
};

// LOCATION API
const getNameFromId = (id, list) => {
    const item = list.find(x => x.code == id);
    return item ? item.name : "";
};
const normalizeName = (str) => {
    if (!str) return "";
    str = str.toLowerCase().trim();
    // Bỏ các từ khóa hành chính thông dụng
    const prefixes = ['tỉnh ', 'thành phố ', 'tp. ', 'tp ', 'quận ', 'huyện ', 'thị xã ', 'tx. ', 'xã ', 'phường ', 'thị trấn ', 'tt. '];
    for (const p of prefixes) {
        if (str.startsWith(p)) {
            return str.replace(p, '').trim();
        }
    }
    return str;
};
const findLocationCode = (inputName, list) => {
    if (!inputName || !list || list.length === 0) return "";
    
    const coreInput = normalizeName(inputName);

    // Vòng 1: Tìm chính xác theo tên API (Ưu tiên cao nhất)
    const exactMatch = list.find(x => x.name.toLowerCase() === inputName.toLowerCase());
    if (exactMatch) return exactMatch.code;

    // Vòng 2: Tìm theo tên cốt lõi (Ví dụ: QR="Phú Lương" khớp API="Huyện Phú Lương")
    const coreMatch = list.find(x => normalizeName(x.name) === coreInput);
    if (coreMatch) return coreMatch.code;

    // Vòng 3: Tìm tương đối (Chứa nhau)
    const relativeMatch = list.find(x => x.name.toLowerCase().includes(coreInput) || coreInput.includes(x.name.toLowerCase()));
    if (relativeMatch) return relativeMatch.code;

    return "";
};
const fetchProvinces = async () => {
    try { const res = await axios.get('https://provinces.open-api.vn/api/?depth=1'); locationData.provinces = res.data; } catch (e) {}
};
const onProvinceChange = async () => {
    address.districtId = ''; address.wardCode = ''; locationData.districts = []; locationData.wards = [];
    if(address.provinceId) { 
        try {
            const res = await axios.get(`https://provinces.open-api.vn/api/p/${address.provinceId}?depth=2`); 
            locationData.districts = res.data.districts; 
        } catch (e) { console.error(e); }
    }
};
const onDistrictChange = async () => {
    address.wardCode = ''; locationData.wards = [];
    if(address.districtId) { 
        try {
            const res = await axios.get(`https://provinces.open-api.vn/api/d/${address.districtId}?depth=2`); 
            locationData.wards = res.data.wards; 
        } catch (e) { console.error(e); }
    }
};

// --- XỬ LÝ ẢNH (QUAN TRỌNG) ---
const triggerFileInput = () => fileInput.value.click();

const handleFileChange = (event) => {
  const file = event.target.files[0];
  if (file) {
    selectedFile.value = file;
    // Tạo URL ảo để xem trước ảnh ngay lập tức
    previewImage.value = URL.createObjectURL(file);
  }
};

// LOAD DỮ LIỆU KHI EDIT
const loadEmployeeData = async () => {
    if (!isEditMode.value) return;
    try {
        const res = await request.get(`/nhan-vien/${route.params.id}`);
        const data = res.data;
        Object.assign(employee, data);
        
        // Nếu nhân viên đã có ảnh trên server -> Hiển thị ảnh đó
        if (data.anhDaiDien) {
            previewImage.value = `http://localhost:8080/api/v1/nhan-vien/images/${data.anhDaiDien}`;
        }
        
        // (Lưu ý: Logic map lại Địa chỉ Tỉnh/Huyện/Xã từ chuỗi string là khá phức tạp 
        // nếu bạn không lưu ID địa chỉ vào DB. Tạm thời ta chỉ load thông tin cơ bản).
    } catch (e) { console.error(e); }
};

// SUBMIT
const handleSubmit = async () => {
    if (!validateForm()) return;
    try {
        const fd = new FormData();
        fd.append("tenNhanVien", employee.tenNhanVien); fd.append("cccd", employee.cccd);
        fd.append("email", employee.email); fd.append("soDienThoai", employee.soDienThoai);
        fd.append("gioiTinh", employee.gioiTinh); fd.append("ngaySinh", employee.ngaySinh);
        
        const p = getNameFromId(address.provinceId, locationData.provinces);
        const d = getNameFromId(address.districtId, locationData.districts);
        const w = getNameFromId(address.wardCode, locationData.wards);
        fd.append("city", p); fd.append("district", d); fd.append("ward", w);
        fd.append("address", employee.diaChiCuThe);

        if (selectedFile.value) fd.append("avatarFile", selectedFile.value);

        if (isEditMode.value) {
            await request.put(`/nhan-vien/${route.params.id}`, fd);
            Swal.fire('Thành công', 'Đã cập nhật', 'success');
        } else {
            await request.post("/nhan-vien", fd);
            Swal.fire('Thành công', 'Đã thêm mới', 'success');
        }
        router.push({ name: "admin-employee-list" });
    } catch (e) {
        Swal.fire('Lỗi', e.response?.data?.message || 'Có lỗi xảy ra', 'error');
    }
};

// QR CODE
const startScan = () => { showScanner.value = true; setTimeout(() => {
    html5QrcodeScanner = new Html5QrcodeScanner("reader", { fps: 10, qrbox: 250 });
    html5QrcodeScanner.render(onScanSuccess);
}, 500)};
const stopScan = () => { if(html5QrcodeScanner) html5QrcodeScanner.clear(); showScanner.value = false; };
const onScanSuccess = async (decodedText) => {
    stopScan();
    // QR Format: CCCD|CMND|Tên|NgàySinh|GiớiTinh|ĐịaChỉ|NgàyCấp
    const parts = decodedText.split('|');
    if (parts.length >= 6) {
        // 1. Thông tin cơ bản
        employee.cccd = parts[0];
        employee.tenNhanVien = parts[2];
        employee.gioiTinh = parts[4].trim() === 'Nam';
        
        // 2. Ngày sinh
        const rawDate = parts[3];
        if (rawDate && rawDate.length === 8) {
            employee.ngaySinh = `${rawDate.substring(4,8)}-${rawDate.substring(2,4)}-${rawDate.substring(0,2)}`;
        }

        // 3. Xử lý địa chỉ (QUAN TRỌNG)
        const fullAddress = parts[5]; 
        if (fullAddress) {
            console.log("Địa chỉ QR:", fullAddress); // Debug xem chuỗi địa chỉ
            const addrParts = fullAddress.split(',').map(p => p.trim());
            
            // Logic: Lấy ngược từ cuối lên: Tỉnh -> Huyện -> Xã
            if (addrParts.length >= 3) {
                const pName = addrParts[addrParts.length - 1]; // Tỉnh
                const dName = addrParts[addrParts.length - 2]; // Huyện
                const wName = addrParts[addrParts.length - 3]; // Xã
                
                // --- BƯỚC 1: TÌM TỈNH ---
                const pCode = findLocationCode(pName, locationData.provinces);
                if (pCode) {
                    address.provinceId = pCode;
                    await onProvinceChange(); // Chờ tải danh sách Huyện
                    
                    // --- BƯỚC 2: TÌM HUYỆN ---
                    // Lưu ý: Đôi khi API chưa kịp render DOM nhưng dữ liệu đã có
                    const dCode = findLocationCode(dName, locationData.districts);
                    if (dCode) {
                        address.districtId = dCode;
                        await onDistrictChange(); // Chờ tải danh sách Xã

                        // --- BƯỚC 3: TÌM XÃ ---
                        const wCode = findLocationCode(wName, locationData.wards);
                        if (wCode) {
                            address.wardCode = wCode;
                        }
                    }
                }

                // --- BƯỚC 4: ĐỊA CHỈ CỤ THỂ ---
                // Ghép các phần còn lại ở đầu
                const detailParts = addrParts.slice(0, addrParts.length - 3);
                employee.diaChiCuThe = detailParts.join(', ');
            } else {
                employee.diaChiCuThe = fullAddress;
            }
        }

        Swal.fire({ icon: 'success', title: 'Đã quét xong!', text: `Xin chào: ${employee.tenNhanVien}`, timer: 1500, showConfirmButton: false });
    } else {
        Swal.fire('Lỗi', 'QR không đúng định dạng CCCD!', 'error');
    }
};

const goBack = () => router.push({ name: 'admin-employee-list' });

onMounted(async () => { 
    await fetchProvinces(); 
    if (isEditMode.value) await loadEmployeeData(); 
});
</script>

<style scoped>
/* CSS ĐỒNG BỘ */
.page-title { color: #2b4360; font-weight: 700; font-size: 24px; margin-bottom: 20px; }

.create-employee-page { font-family: 'Segoe UI', sans-serif; background-color: #f8fafc; min-height: 100vh; padding: 20px; }
.header-section { margin-bottom: 20px; }
.breadcrumb { font-size: 14px; color: #64748b; } .active { font-weight: 600; color: #0f172a; margin-left: 5px; }
.text-gray { color: #64748b; } .cursor-pointer { cursor: pointer; }

.card { background: #fff; border-radius: 8px; box-shadow: 0 1px 3px rgba(0,0,0,0.05); padding: 30px; }
.layout-grid { display: grid; grid-template-columns: 300px 1fr; gap: 40px; }

.left-col { border-right: 1px solid #f1f5f9; padding-right: 30px; }
.section-title { font-size: 16px; font-weight: 700; color: #0f172a; margin-bottom: 20px; text-transform: uppercase; }

/* AVATAR STYLES */
.avatar-upload-container { display: flex; justify-content: center; margin-bottom: 25px; }
.avatar-preview { 
    width: 180px; height: 180px; 
    border-radius: 50%; border: 2px dashed #cbd5e1; 
    display: flex; align-items: center; justify-content: center; 
    cursor: pointer; overflow: hidden; background: #f8fafc; position: relative;
    flex-direction: column; /* Để icon và chữ nằm dọc */
}
.avatar-preview:hover { border-color: #3b82f6; background: #eff6ff; }
.avatar-img { width: 100%; height: 100%; object-fit: cover; }
.upload-placeholder { display: flex; flex-direction: column; align-items: center; color: #64748b; }
.upload-placeholder i { font-size: 24px; margin-bottom: 5px; }
.hidden-input { display: none; }

/* FORM STYLES */
.form-row { display: flex; gap: 20px; margin-bottom: 15px; }
.form-group { margin-bottom: 15px; } .form-group.half { flex: 1; } .form-group.third { flex: 1; }
label { display: block; margin-bottom: 8px; font-weight: 600; font-size: 14px; color: #334155; }
.required::after { content: " *"; color: #ef4444; }
.form-control { width: 100%; padding: 10px 12px; border: 1px solid #e2e8f0; border-radius: 6px; font-size: 14px; outline: none; }
.form-control.is-invalid { border-color: #dc3545; }
.error-msg { color: #dc3545; font-size: 12px; margin-top: 4px; }

.radio-group { display: flex; gap: 20px; align-items: center; height: 42px; }
.radio-item { display: flex; align-items: center; gap: 6px; cursor: pointer; }

.section-header-row { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.btn { padding: 10px 24px; border-radius: 6px; font-weight: 600; cursor: pointer; font-size: 14px; border: 1px solid transparent; }
.btn-primary { background: #0f172a; color: #fff; }
.btn-outline { background: #fff; border-color: #cbd5e1; color: #475569; }
.btn-danger { background: #ef4444; color: #fff; }
.btn-sm { padding: 6px 12px; font-size: 13px; }

.form-actions { display: flex; justify-content: flex-end; gap: 10px; margin-top: 30px; border-top: 1px solid #f1f5f9; padding-top: 20px; }

.qr-overlay { position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.7); display: flex; justify-content: center; align-items: center; z-index: 9999; }
.qr-modal { background: #fff; padding: 20px; border-radius: 12px; width: 400px; text-align: center; }
</style>