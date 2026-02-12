<template>
  <div class="create-employee-page">
    <div class="header-section">
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
                <input type="file" ref="fileInput" class="hidden-input" accept="image/*" @change="handleFileChange">
              </div>

              <div class="form-group">
                <label class="required">Họ và tên</label>
                <input type="text" v-model="employee.tenNhanVien" class="form-control" placeholder="Nhập họ tên">
              </div>
            </div>

            <div class="right-col">
              <div class="section-header-row">
                 <h3 class="section-title">Thông tin chi tiết</h3>
                 <button type="button" class="btn btn-orange" @click="startScan">
                    <i class="fas fa-qrcode"></i> Quét QR
                 </button>
              </div>
              <div class="form-row">
                <div class="form-group half">
                    <label class="required">Số điện thoại</label>
                    <input type="text" v-model="employee.soDienThoai" class="form-control">
                 </div>
                 <div class="form-group half">
                    <label class="required">Email</label>
                    <input type="email" v-model="employee.email" class="form-control">
                 </div>
              </div>
              <div class="form-row">
                <div class="form-group half">
                  <label class="required">Số CCCD</label>
                  <input type="text" v-model="employee.cccd" class="form-control">
                </div>
                <div class="form-group half">
                  <label class="required">Giới tính</label>
                  <div class="radio-group">
                    <label class="radio-item"><input type="radio" :value="true" v-model="employee.gioiTinh"> Nam</label>
                    <label class="radio-item"><input type="radio" :value="false" v-model="employee.gioiTinh"> Nữ</label>
                  </div>
                </div>
                <div class="form-group half">
                  <label class="required">Ngày sinh</label>
                  <input type="date" v-model="employee.ngaySinh" class="form-control">
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
                   <label class="required">Quyền hạn</label>
                   <select v-model="employee.chucVu" class="form-control">
                      <option value="STAFF">Nhân viên</option>
                      <option value="ADMIN">Quản lý (Admin)</option>
                   </select>
                </div>
              </div>
              <div class="form-group">
                 <label class="required">Địa chỉ cụ thể</label>
                 <input type="text" v-model="employee.diaChiCuThe" class="form-control" placeholder="Số nhà, đường...">
              </div>

              <div class="form-actions">
                 <button type="button" class="btn btn-outline" @click="goBack">Hủy</button>
                 <button type="submit" class="btn btn-gradient">
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
import { ref, reactive, onMounted, computed } from 'vue'; 
import { useRouter, useRoute } from 'vue-router'; 
import request from '@/services/request'; 
import axios from 'axios'; 
import { Html5QrcodeScanner } from "html5-qrcode";
import { toastSuccess, toastError, Toast } from '@/utils/toast';

const router = useRouter();
const route = useRoute(); 
const isEditMode = computed(() => !!route.params.id);

// STATE
const employee = reactive({ 
    tenNhanVien: '', cccd: '', gioiTinh: true, ngaySinh: '', 
    email: '', soDienThoai: '', diaChiCuThe: '', 
    chucVu: 'STAFF' 
});
const address = reactive({ provinceId: '', districtId: '', wardCode: '' });

// ẢNH
const fileInput = ref(null);
const selectedFile = ref(null);
const previewImage = ref(null); 

// QR & LOCATION DATA
const showScanner = ref(false);
let html5QrcodeScanner = null;
const locationData = reactive({ provinces: [], districts: [], wards: [] });

// --- HÀM HỖ TRỢ LOCATION (CẦN CHO QR) ---
const getNameFromId = (id, list) => { const item = list.find(x => x.code == id); return item ? item.name : ""; };

const normalizeName = (str) => {
    if (!str) return "";
    str = str.toLowerCase().trim();
    const prefixes = ['tỉnh ', 'thành phố ', 'tp. ', 'tp ', 'quận ', 'huyện ', 'thị xã ', 'tx. ', 'xã ', 'phường ', 'thị trấn ', 'tt. '];
    for (const p of prefixes) {
        if (str.startsWith(p)) return str.replace(p, '').trim();
    }
    return str;
};

const findLocationCode = (inputName, list) => {
    if (!inputName || !list || list.length === 0) return "";
    const coreInput = normalizeName(inputName);
    const exactMatch = list.find(x => x.name.toLowerCase() === inputName.toLowerCase());
    if (exactMatch) return exactMatch.code;
    const coreMatch = list.find(x => normalizeName(x.name) === coreInput);
    if (coreMatch) return coreMatch.code;
    const relativeMatch = list.find(x => x.name.toLowerCase().includes(coreInput) || coreInput.includes(x.name.toLowerCase()));
    if (relativeMatch) return relativeMatch.code;
    return "";
};

// VALIDATION
const validateForm = () => {
    if (!employee.tenNhanVien.trim()) return Toast.fire({ icon: 'warning', title: 'Thiếu tên nhân viên' });
    if (!employee.cccd) return Toast.fire({ icon: 'warning', title: 'Thiếu số CCCD' });
    if (!employee.ngaySinh) return Toast.fire({ icon: 'warning', title: 'Thiếu ngày sinh' });
    if (!employee.soDienThoai) return Toast.fire({ icon: 'warning', title: 'Thiếu số điện thoại' });
    if (!employee.email) return Toast.fire({ icon: 'warning', title: 'Thiếu Email' });
    return true;
};

// API LOCATION
const fetchProvinces = async () => { try { const res = await axios.get('https://provinces.open-api.vn/api/?depth=1'); locationData.provinces = res.data; } catch (e) {} };
const onProvinceChange = async () => {
    address.districtId = ''; address.wardCode = ''; locationData.districts = []; locationData.wards = [];
    if(address.provinceId) { 
        try { const res = await axios.get(`https://provinces.open-api.vn/api/p/${address.provinceId}?depth=2`); locationData.districts = res.data.districts; } catch (e) {}
    }
};
const onDistrictChange = async () => {
    address.wardCode = ''; locationData.wards = [];
    if(address.districtId) { 
        try { const res = await axios.get(`https://provinces.open-api.vn/api/d/${address.districtId}?depth=2`); locationData.wards = res.data.wards; } catch (e) {}
    }
};

// XỬ LÝ ẢNH
const triggerFileInput = () => fileInput.value.click();
const handleFileChange = (event) => {
  const file = event.target.files[0];
  if (file) {
    selectedFile.value = file;
    previewImage.value = URL.createObjectURL(file);
  }
};

// --- LOGIC QR CODE (ĐÃ KHÔI PHỤC) ---
const startScan = () => { 
    showScanner.value = true; 
    setTimeout(() => { 
        html5QrcodeScanner = new Html5QrcodeScanner("reader", { fps: 10, qrbox: 250 }); 
        html5QrcodeScanner.render(onScanSuccess); 
    }, 500);
};

const stopScan = () => { 
    if(html5QrcodeScanner) html5QrcodeScanner.clear(); 
    showScanner.value = false; 
};

const onScanSuccess = async (decodedText) => {
    stopScan();
    const parts = decodedText.split('|');
    if (parts.length >= 6) {
        employee.cccd = parts[0];
        employee.tenNhanVien = parts[2];
        employee.gioiTinh = parts[4].trim() === 'Nam';
        
        const rawDate = parts[3];
        if (rawDate && rawDate.length === 8) {
            employee.ngaySinh = `${rawDate.substring(4,8)}-${rawDate.substring(2,4)}-${rawDate.substring(0,2)}`;
        }
        
        const fullAddress = parts[5]; 
        if (fullAddress) {
            const addrParts = fullAddress.split(',').map(p => p.trim());
            
            if (addrParts.length >= 3) {
                const pName = addrParts[addrParts.length - 1]; 
                const dName = addrParts[addrParts.length - 2]; 
                const wName = addrParts[addrParts.length - 3]; 
                
                const pCode = findLocationCode(pName, locationData.provinces);
                if (pCode) {
                    address.provinceId = pCode;
                    await onProvinceChange(); 
                    
                    const dCode = findLocationCode(dName, locationData.districts);
                    if (dCode) {
                        address.districtId = dCode;
                        await onDistrictChange(); 
                        
                        const wCode = findLocationCode(wName, locationData.wards);
                        if (wCode) {
                            address.wardCode = wCode;
                        }
                    }
                }
                const detailParts = addrParts.slice(0, addrParts.length - 3);
                employee.diaChiCuThe = detailParts.join(', ');
            } else {
                employee.diaChiCuThe = fullAddress;
            }
        }
        toastSuccess(`Đã quét xong! Xin chào: ${employee.tenNhanVien}`);
    } else {
        toastError('Mã QR không đúng định dạng CCCD!');
    }
};

// LOAD DATA EDIT
const loadEmployeeData = async () => {
    if (!isEditMode.value) return;
    try {
        const res = await request.get(`/nhan-vien/${route.params.id}`);
        const data = res.data;
        Object.assign(employee, data);
        if (data.anhDaiDien) previewImage.value = `http://localhost:8080/api/v1/nhan-vien/images/${data.anhDaiDien}`;
    } catch (e) { console.error(e); }
};

// SUBMIT
const handleSubmit = async () => {
    if (!validateForm()) return;

    try {
        const fd = new FormData();
        fd.append("tenNhanVien", employee.tenNhanVien); 
        fd.append("cccd", employee.cccd);
        fd.append("email", employee.email); 
        fd.append("soDienThoai", employee.soDienThoai);
        fd.append("gioiTinh", employee.gioiTinh); 
        fd.append("ngaySinh", employee.ngaySinh);
        fd.append("chucVu", employee.chucVu); 

        const p = getNameFromId(address.provinceId, locationData.provinces);
        const d = getNameFromId(address.districtId, locationData.districts);
        const w = getNameFromId(address.wardCode, locationData.wards);
        
        const fullAddr = [employee.diaChiCuThe, w, d, p].filter(Boolean).join(", ");
        fd.append("diaChi", fullAddr);

        if (selectedFile.value) fd.append("avatarFile", selectedFile.value);

        if (isEditMode.value) {
            await request.put(`/nhan-vien/${route.params.id}`, fd);
            toastSuccess('Cập nhật nhân viên thành công!');
        } else {
            await request.post("/nhan-vien", fd);
            toastSuccess('Thêm nhân viên mới thành công!');
        }
        router.push({ name: "admin-employee-list" });
    } catch (e) {
        console.error(e);
        toastError(e.response?.data?.message || 'Có lỗi xảy ra!');
    }
};

const goBack = () => router.push({ name: 'admin-employee-list' });

onMounted(async () => { await fetchProvinces(); if (isEditMode.value) await loadEmployeeData(); });
</script>

<style scoped>
.page-title { color: #2b4360; font-weight: 700; font-size: 24px; margin-bottom: 20px; }
.create-employee-page { font-family: 'Segoe UI', sans-serif; background-color: #f8fafc; min-height: 100vh; padding: 20px; }
.header-section { margin-bottom: 20px; }

/* === UPDATE CSS: Card Styling to match EmployeeList === */
.card { 
    background: #fff; 
    border-radius: 16px; /* Bo góc tròn hơn */
    box-shadow: 0 4px 12px rgba(0,0,0,0.05); 
    padding: 30px; 
    border: 1px solid #bfdbfe; /* Viền màu xanh dương nhạt giống EmployeeList */
}

.layout-grid { display: grid; grid-template-columns: 300px 1fr; gap: 40px; }

.left-col { border-right: 1px solid #f1f5f9; padding-right: 30px; }
.section-title { font-size: 16px; font-weight: 700; color: #0f172a; margin-bottom: 20px; text-transform: uppercase; }

/* Avatar */
.avatar-upload-container { display: flex; justify-content: center; margin-bottom: 25px; }
.avatar-preview { width: 180px; height: 180px; border-radius: 50%; border: 2px dashed #cbd5e1; display: flex; align-items: center; justify-content: center; cursor: pointer; background: #f8fafc; overflow: hidden; position: relative; flex-direction: column; }
.avatar-img { width: 100%; height: 100%; object-fit: cover; }
.upload-placeholder { display: flex; flex-direction: column; align-items: center; color: #64748b; }
.hidden-input { display: none; }

/* Form */
.form-row { display: flex; gap: 20px; margin-bottom: 15px; }
.form-group { margin-bottom: 15px; } .form-group.half { flex: 1; } .form-group.third { flex: 1; }
label { display: block; margin-bottom: 8px; font-weight: 600; font-size: 13px; color: #334155; }
.required::after { content: " *"; color: #ef4444; }
.form-control { width: 100%; padding: 10px 12px; border: 1px solid #e2e8f0; border-radius: 6px; font-size: 14px; outline: none; transition: 0.2s; }
.form-control:focus { border-color: #3b82f6; box-shadow: 0 0 0 3px rgba(59,130,246,0.1); }

.radio-group { display: flex; gap: 20px; align-items: center; height: 42px; }
.radio-item { display: flex; align-items: center; gap: 6px; cursor: pointer; }

/* Buttons */
.section-header-row { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.form-actions { display: flex; justify-content: flex-end; gap: 10px; margin-top: 30px; border-top: 1px solid #f1f5f9; padding-top: 20px; }

.btn { padding: 10px 24px; border-radius: 6px; font-weight: 600; cursor: pointer; font-size: 14px; border: 1px solid transparent; }
.btn-outline { background: #fff; border-color: #cbd5e1; color: #475569; }
.btn-sm { padding: 6px 12px; font-size: 13px; }
.btn-danger { background: #ef4444; color: #fff; }

/* Button Gradient */
.btn-gradient { 
    background: linear-gradient(135deg, #1e3a8a 0%, #0f172a 100%); 
    color: #fff; 
    box-shadow: 0 4px 10px rgba(15, 23, 42, 0.3); 
    transition: 0.2s; 
}
.btn-gradient:hover { 
    transform: translateY(-1px); 
    box-shadow: 0 6px 15px rgba(15, 23, 42, 0.4); 
}

/* Modal QR */
.qr-overlay { position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.7); display: flex; justify-content: center; align-items: center; z-index: 9999; }
.qr-modal { background: #fff; padding: 20px; border-radius: 12px; width: 400px; text-align: center; }
.btn-orange { 
    background: #f97316; /* Màu cam đậm */
    color: #ffffff; 
    border: 1px solid #f97316;
}
.btn-orange:hover { 
    background: #ea580c; /* Cam đậm hơn khi hover */
    box-shadow: 0 4px 10px rgba(234, 88, 12, 0.3);
}
.form-control::placeholder {

    color: #000000 !important;  /* Màu đen */
    opacity: 0.5 !important;      /* Chống mờ */
    font-weight: 500;           /* Đậm lên tí cho dễ đọc (tùy chọn) */

}
</style>
