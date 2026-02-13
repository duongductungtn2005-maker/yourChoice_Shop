<template>
  <div class="edit-employee-page">
    <div class="header-section">
       <h1 class="page-title">Quản lý nhân viên / Cập nhật thông tin</h1>
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
                  <input type="number" v-model="employee.cccd" class="form-control">
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
                 <button type="submit" class="btn btn-gradient">Lưu thay đổi</button>
              </div>
            </div>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'; 
import { useRouter, useRoute } from 'vue-router'; 
import request from '@/services/request'; 
import axios from 'axios'; 
import { toastSuccess, toastError, Toast } from '@/utils/toast';

const router = useRouter();
const route = useRoute();
const id = route.params.id; 

const employee = reactive({ 
    tenNhanVien: '', cccd: '', gioiTinh: true, ngaySinh: '', 
    email: '', soDienThoai: '', diaChiCuThe: '', 
    chucVu: 'STAFF' 
});
const address = reactive({ provinceId: '', districtId: '', wardCode: '' });
const locationData = reactive({ provinces: [], districts: [], wards: [] });

const fileInput = ref(null);
const selectedFile = ref(null);
const previewImage = ref(null); 

const loadData = async () => {
    try {
        const resP = await axios.get('https://provinces.open-api.vn/api/?depth=1');
        locationData.provinces = resP.data;

        const res = await request.get(`/nhan-vien/${id}`); 
        const data = res.data; 

        employee.tenNhanVien = data.tenNhanVien;
        employee.cccd = data.cccd;
        employee.email = data.email;
        employee.soDienThoai = data.soDienThoai;
        employee.gioiTinh = data.gioiTinh;
        employee.ngaySinh = data.ngaySinh;
        employee.chucVu = (data.quyenHan && data.quyenHan.id === 1) ? 'ADMIN' : 'STAFF';

        if (data.anhDaiDien) {
            previewImage.value = `http://localhost:8080/api/v1/nhan-vien/images/${data.anhDaiDien}`;
        }

        // 3. Xử lý địa chỉ (QUAN TRỌNG: Cần await)
        if (data.diaChi && data.diaChi !== 'Chưa cập nhật') {
            await parseAddressString(data.diaChi);
        } else {
            employee.diaChiCuThe = data.diaChi;
        }

    } catch (e) {
        console.error("Lỗi load dữ liệu:", e);
    }
};

// Hàm chuẩn hóa chuỗi: Bỏ dấu, bỏ viết hoa, bỏ tiền tố hành chính
const normalizeName = (str) => {
    if (!str) return '';
    return str.toLowerCase()
        // Bỏ các từ: Tỉnh, Thành phố, TP., Quận, Huyện, Xã, Phường... (kể cả có hoặc không có dấu chấm/cách)
        .replace(/(tỉnh|thành phố|tp\.?|quận|huyện|thị xã|tx\.?|xã|phường|thị trấn|tt\.?)\s*/g, '')
        .trim();
};

const findLocationCode = (inputName, listData) => {
    if (!inputName || !listData || listData.length === 0) return "";
    
    const searchKey = normalizeName(inputName);
    
    // Thử tìm chính xác
    let found = listData.find(item => normalizeName(item.name) === searchKey);
    
    // Nếu chưa thấy, thử tìm "chứa trong"
    if (!found) {
        found = listData.find(item => normalizeName(item.name).includes(searchKey));
    }
    
    // LỖI CŨ CỦA BẠN NẰM Ở ĐÂY: return found ? item.code : "";
    // SỬA THÀNH:
    return found ? found.code : ""; 
};

// Hàm tách chuỗi địa chỉ và tự động fill Dropdown
const parseAddressString = async (fullAddr) => {
    if (!fullAddr) return;

    // Tách chuỗi bằng dấu phẩy
    const parts = fullAddr.split(',').map(s => s.trim());
    
    // Nếu địa chỉ quá ngắn (không đủ 3 cấp: Xã, Huyện, Tỉnh), chỉ điền vào ô chi tiết
    if (parts.length < 3) {
        employee.diaChiCuThe = fullAddr;
        return;
    }

    // Lấy ngược từ cuối lên: Tỉnh (cuối) -> Huyện (kế cuối) -> Xã (kế nữa)
    const pName = parts[parts.length - 1]; 
    const dName = parts[parts.length - 2]; 
    const wName = parts[parts.length - 3]; 

    // --- BƯỚC 1: XỬ LÝ TỈNH ---
    // Tìm ID Tỉnh trong danh sách đã load sẵn (locationData.provinces)
    const pCode = findLocationCode(pName, locationData.provinces);
    
    if (pCode) {
        address.provinceId = pCode;
        
        try {
            // --- BƯỚC 2: XỬ LÝ HUYỆN ---
            // Gọi API lấy danh sách Huyện của Tỉnh này
            const resD = await axios.get(`https://provinces.open-api.vn/api/p/${pCode}?depth=2`);
            locationData.districts = resD.data.districts;
            
            // Tìm ID Huyện trong danh sách vừa tải về
            const dCode = findLocationCode(dName, locationData.districts);
            
            if (dCode) {
                address.districtId = dCode;
                
                // --- BƯỚC 3: XỬ LÝ XÃ ---
                // Gọi API lấy danh sách Xã của Huyện này
                const resW = await axios.get(`https://provinces.open-api.vn/api/d/${dCode}?depth=2`);
                locationData.wards = resW.data.wards;

                // Tìm ID Xã trong danh sách vừa tải về
                const wCode = findLocationCode(wName, locationData.wards);
                if (wCode) {
                    address.wardCode = wCode;
                }
            }
        } catch (e) {
            console.error("Lỗi khi load Huyện/Xã:", e);
        }
    }

    // Phần còn lại ở đầu chuỗi là địa chỉ cụ thể (Số nhà, đường...)
    // VD: [Số 1, Ngõ A, Phường B, Quận C, Tỉnh D] -> Lấy "Số 1, Ngõ A"
    // Slice từ đầu đến phần tử thứ (length - 3)
    if (parts.length > 3) {
        employee.diaChiCuThe = parts.slice(0, parts.length - 3).join(', ');
    } else {
        employee.diaChiCuThe = ""; // Trường hợp chỉ có đúng Xã, Huyện, Tỉnh thì ô chi tiết để trống
    }
};

const getNameFromId = (id, list) => { const item = list.find(x => x.code == id); return item ? item.name : ""; };
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

const triggerFileInput = () => fileInput.value.click();
const handleFileChange = (event) => {
  const file = event.target.files[0];
  if (file) { selectedFile.value = file; previewImage.value = URL.createObjectURL(file); }
};

const validateForm = () => {
    if (!employee.tenNhanVien.trim()) return Toast.fire({ icon: 'warning', title: 'Thiếu tên nhân viên' });
    return true;
};

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

        await request.put(`/nhan-vien/${id}`, fd);
        toastSuccess('Cập nhật nhân viên thành công!');
        router.push({ name: "admin-employee-list" });
    } catch (e) {
        console.error(e);
        toastError('Lỗi cập nhật!');
    }
};

const goBack = () => router.push({ name: 'admin-employee-list' });

onMounted(() => { loadData(); });
</script>

<style scoped>
.page-title { color: #2b4360; font-weight: 700; font-size: 24px; margin-bottom: 20px; }
.edit-employee-page { font-family: 'Segoe UI', sans-serif; background-color: #f8fafc; min-height: 100vh; padding: 20px; }
.header-section { margin-bottom: 20px; }

/* === UPDATE CSS: Card Styling === */
.card { 
    background: #fff; 
    border-radius: 16px; 
    box-shadow: 0 4px 12px rgba(0,0,0,0.05); 
    padding: 30px; 
    /* Dùng !important để ghi đè style của thư viện (ví dụ Bootstrap) */
    border: 1px solid #bfdbfe !important; 
}

.layout-grid { display: grid; grid-template-columns: 300px 1fr; gap: 40px; }
.left-col { border-right: 1px solid #f1f5f9; padding-right: 30px; }
.section-title { font-size: 16px; font-weight: 700; color: #0f172a; margin-bottom: 20px; text-transform: uppercase; }
.avatar-upload-container { display: flex; justify-content: center; margin-bottom: 25px; }
.avatar-preview { width: 180px; height: 180px; border-radius: 50%; border: 2px dashed #cbd5e1; display: flex; align-items: center; justify-content: center; cursor: pointer; background: #f8fafc; overflow: hidden; position: relative; flex-direction: column; }
.avatar-img { width: 100%; height: 100%; object-fit: cover; }
.upload-placeholder { display: flex; flex-direction: column; align-items: center; color: #64748b; }
.hidden-input { display: none; }
.form-row { display: flex; gap: 20px; margin-bottom: 15px; }
.form-group { margin-bottom: 15px; } .form-group.half { flex: 1; } .form-group.third { flex: 1; }
label { display: block; margin-bottom: 8px; font-weight: 600; font-size: 13px; color: #334155; }
.required::after { content: " *"; color: #ef4444; }
.form-control { width: 100%; padding: 10px 12px; border: 1px solid #e2e8f0; border-radius: 6px; font-size: 14px; outline: none; transition: 0.2s; }
.form-control:focus { border-color: #3b82f6; box-shadow: 0 0 0 3px rgba(59,130,246,0.1); }
.radio-group { display: flex; gap: 20px; align-items: center; height: 42px; }
.radio-item { display: flex; align-items: center; gap: 6px; cursor: pointer; }
.form-actions { display: flex; justify-content: flex-end; gap: 10px; margin-top: 30px; border-top: 1px solid #f1f5f9; padding-top: 20px; }
.btn { padding: 10px 24px; border-radius: 6px; font-weight: 600; cursor: pointer; font-size: 14px; border: 1px solid transparent; }
.btn-outline { background: #fff; border-color: #cbd5e1; color: #475569; }
.btn-gradient { background: linear-gradient(135deg, #1e3a8a 0%, #0f172a 100%); color: #fff; box-shadow: 0 4px 10px rgba(15, 23, 42, 0.3); transition: 0.2s; }
.btn-gradient:hover { transform: translateY(-1px); box-shadow: 0 6px 15px rgba(15, 23, 42, 0.4); }
</style>
