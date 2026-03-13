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
              <h3 class="section-title" style="text-align: center;">Ảnh đại diện</h3>
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
              <div class="section-header-row" style="justify-content: center;">
                 <button type="button" class="btn btn-teal" @click="startScan">
                   <i class="fas fa-qrcode"></i> Quét QR
                </button>
              </div>
            </div>

            <div class="right-col">
              <h3 class="section-title">Thông tin nhân viên</h3>
              
              <div class="form-row">
                 <div class="form-group third">
                    <label class="required">Số điện thoại</label>
                    <input type="text" v-model="employee.soDienThoai" class="form-control">
                 </div>
                 <div class="form-group third">
                    <label class="required">Email</label>
                    <input type="email" v-model="employee.email" class="form-control">
                 </div>
              </div>
              
              <div class="form-row">
                 <div class="form-group third">
                    <label class="required">Ngày sinh</label>
                    <input type="date" v-model="employee.ngaySinh" class="form-control">
                 </div>
                 <div class="form-group third">
                    <label class="required">Giới tính</label>
                    <div class="radio-group">
                      <label class="radio-item"><input type="radio" :value="true" v-model="employee.gioiTinh"> Nam</label>
                      <label class="radio-item"><input type="radio" :value="false" v-model="employee.gioiTinh"> Nữ</label>
                    </div>
                 </div>
              </div>
              <div class="form-row">
                <div class="form-group third">
                    <label>Tên tài khoản (tự sinh theo tên)</label>
                                        <input type="text" :value="generatedUsername" class="form-control" readonly>
                </div>
                 <div class="form-group third">
                   <label class="required">Quyền hạn</label>
                   <select v-model="employee.chucVu" class="form-control">
                      <option value="STAFF">Nhân viên</option>
                      <option value="ADMIN">Quản lý (Admin)</option>
                   </select>
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
                <div class="form-group" style="width: 100%;">
                   <label class="required">Địa chỉ cụ thể</label>
                   <input type="text" v-model="employee.diaChiCuThe" class="form-control" placeholder="Số nhà, đường...">
                </div>
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
import Swal from 'sweetalert2'; // Import thư viện

const router = useRouter();
const route = useRoute(); 
const isEditMode = computed(() => !!route.params.id);
const generatedUsername = computed(() => buildUsernameFromName(employee.tenNhanVien));

// --- STATE ---
const employee = reactive({ 
    tenNhanVien: '', cccd: '', gioiTinh: true, ngaySinh: '', 
    email: '', soDienThoai: '', diaChiCuThe: '', 
    chucVu: 'STAFF' 
});
const address = reactive({ provinceId: '', districtId: '', wardCode: '' });

// --- FILE/IMAGE ---
const fileInput = ref(null);
const selectedFile = ref(null);
const previewImage = ref(null); 

// --- QR & LOCATION DATA ---
const showScanner = ref(false);
let html5QrcodeScanner = null;
const locationData = reactive({ provinces: [], districts: [], wards: [] });

// ============================================================
// 1. CÁC HÀM HỖ TRỢ XỬ LÝ CHUỖI (Đưa lên đầu để tránh lỗi)
// ============================================================

const getNameFromId = (id, list) => { const item = list.find(x => x.code == id); return item ? item.name : ""; };

// Hàm bỏ dấu tiếng Việt (để so sánh chính xác hơn)
const removeAccents = (str) => {
    return str.normalize("NFD").replace(/[\u0300-\u036f]/g, "").replace(/đ/g, "d").replace(/Đ/g, "D");
};

// Hàm chuẩn hóa tên (Bỏ Tiền tố + Lowercase + Bỏ dấu)
const normalizeName = (str) => {
    if (!str) return "";
    let clean = str.toLowerCase().trim();
    // Bỏ các từ hành chính thông dụng
    clean = clean.replace(/(tỉnh|thành phố|tp\.?|quận|huyện|thị xã|tx\.?|xã|phường|thị trấn|tt\.?)\s*/g, '');
    return removeAccents(clean).trim();
};

const buildUsernameFromName = (name) => {
    if (!name || !name.trim()) return "";
    return removeAccents(name)
        .toLowerCase()
        .trim()
        .replace(/[^a-z0-9]+/g, '');
};

// Hàm tìm Code trong danh sách
const findLocationCode = (inputName, listData) => {
    if (!inputName || !listData || listData.length === 0) return "";
    
    const target = normalizeName(inputName);

    // Tìm trong list, chuẩn hóa cả tên trong list để so sánh
    const found = listData.find(item => normalizeName(item.name) === target);
    
    // Nếu vẫn không thấy, thử tìm kiểu "chứa trong" (dành cho trường hợp tên dài/ngắn)
    if (!found) {
        return listData.find(item => normalizeName(item.name).includes(target))?.code || "";
    }

    return found ? found.code : "";
};

// ============================================================
// 2. LOGIC API LOCATION
// ============================================================
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

// ============================================================
// 3. LOGIC QR CODE
// ============================================================
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
    if (parts.length < 6) return toastError('QR không đúng định dạng CCCD!');

    try {
        // Điền thông tin cơ bản
        employee.cccd = parts[0]; 
        employee.tenNhanVien = parts[2]; 
        
        // Ngày sinh
        const rawDate = parts[3];
        if (rawDate?.length === 8) {
            employee.ngaySinh = `${rawDate.substring(4,8)}-${rawDate.substring(2,4)}-${rawDate.substring(0,2)}`;
        }
        employee.gioiTinh = parts[4].trim() === 'Nam';

        // Xử lý địa chỉ
        const fullAddress = parts[5];
        if (fullAddress) {
            const addrParts = fullAddress.split(',').map(p => p.trim());
            
            if (addrParts.length >= 3) {
                // Lấy 3 cấp từ cuối lên
                const pName = addrParts[addrParts.length - 1];
                const dName = addrParts[addrParts.length - 2];
                const wName = addrParts[addrParts.length - 3];
                
                // Phần còn lại là chi tiết
                employee.diaChiCuThe = addrParts.slice(0, addrParts.length - 3).join(', ');

                // Tự động chọn Dropdown (Await để chạy tuần tự)
                const pCode = findLocationCode(pName, locationData.provinces);
                if (pCode) {
                    address.provinceId = pCode;
                    await onProvinceChange(); // Chờ load huyện

                    const dCode = findLocationCode(dName, locationData.districts);
                    if (dCode) {
                        address.districtId = dCode;
                        await onDistrictChange(); // Chờ load xã

                        const wCode = findLocationCode(wName, locationData.wards);
                        if (wCode) address.wardCode = wCode;
                    }
                }
            } else {
                employee.diaChiCuThe = fullAddress;
            }
        }
        toastSuccess(`Đã quét xong: ${employee.tenNhanVien}`);
    } catch (error) {
        console.error(error);
        toastError('Lỗi xử lý QR');
    }
};

// ============================================================
// 4. CÁC HÀM XỬ LÝ FORM & ẢNH
// ============================================================
const triggerFileInput = () => fileInput.value.click();
const handleFileChange = (event) => {
  const file = event.target.files[0];
  if (file) { selectedFile.value = file; previewImage.value = URL.createObjectURL(file); }
};

const validateForm = async () => {
    // 1. Kiểm tra Họ và tên
    if (!employee.tenNhanVien || !employee.tenNhanVien.trim()) {
        Toast.fire({ icon: 'warning', title: 'Vui lòng nhập họ và tên' });
        return false;
    }

    // 2. Kiểm tra Ngày sinh và Tuổi (>= 18)
    if (!employee.ngaySinh) {
        Toast.fire({ icon: 'warning', title: 'Vui lòng chọn ngày sinh' });
        return false;
    } else {
        const today = new Date();
        const birthDate = new Date(employee.ngaySinh);
        
        // Tính tuổi
        let age = today.getFullYear() - birthDate.getFullYear();
        const monthDiff = today.getMonth() - birthDate.getMonth();
        
        // Nếu chưa tới tháng sinh nhật, hoặc cùng tháng nhưng chưa tới ngày sinh nhật -> trừ đi 1 tuổi
        if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birthDate.getDate())) {
            age--;
        }
        
        if (age < 18) {
            Toast.fire({ icon: 'warning', title: 'Nhân viên phải từ đủ 18 tuổi trở lên' });
            return false;
        }
    }

    // 3. Kiểm tra Số điện thoại (Regex số điện thoại VN: 10 số, bắt đầu 03, 05, 07, 08, 09)
    const phoneRegex = /(84|0[3|5|7|8|9])+([0-9]{8})\b/;
    if (!employee.soDienThoai || !employee.soDienThoai.trim()) {
        Toast.fire({ icon: 'warning', title: 'Vui lòng nhập số điện thoại' });
        return false;
    } else if (!phoneRegex.test(employee.soDienThoai)) {
        Toast.fire({ icon: 'warning', title: 'Số điện thoại không hợp lệ' });
        return false;
    } else if (employee.soDienThoai.replace(/[^0-9]/g, '').length !== 10) {
        Toast.fire({ icon: 'warning', title: 'Số điện thoại phải đúng 10 chữ số' });
        return false;
    } else {
        // Kiểm tra trùng số điện thoại
        try {
            const idParam = isEditMode.value ? `&id=${route.params.id}` : '';
            const res = await request.get(`/nhan-vien/xac-thuc/sdt?soDienThoai=${employee.soDienThoai}${idParam}`);
            if (res.data === true) {
                Toast.fire({ icon: 'warning', title: 'Số điện thoại này đã có người sử dụng!' });
                return false;
            }
        } catch (error) {
            console.error('Lỗi khi check trùng SĐT:', error);
            Toast.fire({ icon: 'error', title: 'Lỗi kết nối khi kiểm tra số điện thoại!' });
            return false;
        }
    }

    // 4. Kiểm tra Email (Regex định dạng email cơ bản)
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!employee.email || !employee.email.trim()) {
        Toast.fire({ icon: 'warning', title: 'Vui lòng nhập Email' });
        return false;
    } else if (!emailRegex.test(employee.email)) {
        Toast.fire({ icon: 'warning', title: 'Email không đúng định dạng' });
        return false;
    }

    // 5. Tên tài khoản tự sinh từ họ tên
    if (!generatedUsername.value) {
        Toast.fire({ icon: 'warning', title: 'Vui lòng nhập họ tên để tạo tài khoản tự động' });
        return false;
    }

    // 6. Kiểm tra Quyền hạn (Chức vụ)
    if (!employee.chucVu) {
        Toast.fire({ icon: 'warning', title: 'Vui lòng chọn quyền hạn' });
        return false;
    }

    // 7. Kiểm tra Địa chỉ 4 cấp
    if (!address.provinceId) {
        Toast.fire({ icon: 'warning', title: 'Vui lòng chọn Tỉnh/Thành' });
        return false;
    }
    if (!address.districtId) {
        Toast.fire({ icon: 'warning', title: 'Vui lòng chọn Quận/Huyện' });
        return false;
    }
    if (!address.wardCode) {
        Toast.fire({ icon: 'warning', title: 'Vui lòng chọn Xã/Phường' });
        return false;
    }
    if (!employee.diaChiCuThe || !employee.diaChiCuThe.trim()) {
        Toast.fire({ icon: 'warning', title: 'Vui lòng nhập địa chỉ cụ thể' });
        return false;
    }

    return true;
};

const loadEmployeeData = async () => {
    if (!isEditMode.value) return;
    try {
        const res = await request.get(`/nhan-vien/${route.params.id}`);
        const data = res.data;
        Object.assign(employee, data); // Copy dữ liệu vào form
        
        // Nếu muốn map ngược địa chỉ từ DB vào Dropdown, cần logic parseAddressString ở đây
        // Nhưng nếu chỉ cần hiển thị thì giữ nguyên logic cũ của bạn
        
        if (data.anhDaiDien) previewImage.value = `http://localhost:8080/api/v1/nhan-vien/images/${data.anhDaiDien}`;
    } catch (e) { console.error(e); }
};

const handleSubmit = async () => {
    // 1. Validate Form (Thêm await ở đây 👇)
    if (!(await validateForm())) return;

    // 2. Hiển thị Popup xác nhận
    const result = await Swal.fire({
        title: 'Xác nhận thêm nhân viên',
        text: 'Hệ thống sẽ tự động gửi email thông báo cho nhân viên.',
        icon: 'question',
        showCancelButton: true,
        confirmButtonText: '<i class="fas fa-check"></i> Xác nhận',
        confirmButtonColor: '#3085d6',
        cancelButtonText: 'Hủy',
        cancelButtonColor: '#6b7280',
    });

    if (!result.isConfirmed) return;

    // Luôn gửi email khi thêm nhân viên
    const isSendEmail = true;

    // 3. Hiển thị Loading
    Swal.fire({
        title: 'Đang xử lý...',
        text: 'Đang thêm nhân viên và gửi email kích hoạt...',
        allowOutsideClick: false,
        didOpen: () => { Swal.showLoading(); }
    });

    try {
        const fd = new FormData();
        fd.append("tenNhanVien", employee.tenNhanVien); 
        fd.append("cccd", employee.cccd);
        fd.append("email", employee.email); 
        fd.append("soDienThoai", employee.soDienThoai);
        fd.append("gioiTinh", employee.gioiTinh); 
        fd.append("ngaySinh", employee.ngaySinh);
        fd.append("chucVu", employee.chucVu);

        // Gửi cờ (flag) lên server
        fd.append("isSendEmail", isSendEmail); 

        // Xử lý địa chỉ
        const p = getNameFromId(address.provinceId, locationData.provinces);
        const d = getNameFromId(address.districtId, locationData.districts);
        const w = getNameFromId(address.wardCode, locationData.wards);
        const fullAddr = [employee.diaChiCuThe, w, d, p].filter(Boolean).join(", ");
        fd.append("diaChi", fullAddr);

        if (selectedFile.value) fd.append("avatarFile", selectedFile.value);

        // Gọi API
        await request.post('/nhan-vien', fd);

        // 4. Thông báo thành công
        Swal.close(); 
        await Swal.fire({
            icon: 'success',
            title: 'Thành công!',
            text: 'Đã thêm nhân viên và gửi email kích hoạt.',
            timer: 2000,
            showConfirmButton: false
        });

        router.push({ name: "admin-employee-list" });

    } catch (e) {
        Swal.close();
        console.error(e);
        Swal.fire({
            icon: 'error',
            title: 'Thất bại',
            text: e.response?.data?.message || 'Có lỗi xảy ra!',
        });
    }
};

const goBack = () => router.push({ name: 'admin-employee-list' });

onMounted(async () => { await fetchProvinces(); if (isEditMode.value) await loadEmployeeData(); });
</script>

<style scoped>
.page-title { color: #2b4360; font-weight: 700; font-size: 24px; margin-bottom: 20px; }
.create-employee-page { font-family: 'Segoe UI', sans-serif; background-color:#ebecee; /* Gray-100: Màu xám chuẩn */; min-height: 100vh; padding: 20px; }
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
.btn-soft-blue {
    background-color: #e0f2fe; /* Xanh dương pastel */
    color: #1e3a8a; /* Chữ xanh đen đậm */
    border: 1px solid #bae6fd;
    font-weight: 600;
    transition: all 0.2s ease;
}
.btn-soft-blue:hover {
    background-color: #bae6fd;
    transform: translateY(-1px);
}
.btn-outline-blue {
    background-color: #ffffff;
    color: #1e3a8a;
    border: 2px solid #1e3a8a;
    font-weight: 600;
    transition: all 0.2s ease;
}
.btn-outline-blue:hover {
    background-color: #f8fafc; /* Xám rất nhẹ khi hover */
    box-shadow: 0 4px 6px rgba(30, 58, 138, 0.1);
}
.btn-teal {
    background-color: #10b981; /* Xanh ngọc / Xanh lá */
    color: #ffffff;
    border: none;
    font-weight: 600;
    transition: all 0.2s ease;
}
.btn-teal:hover {
    background-color: #059669; /* Đậm hơn khi hover */
    box-shadow: 0 4px 10px rgba(16, 185, 129, 0.3);
    transform: translateY(-1px);
}
</style>
