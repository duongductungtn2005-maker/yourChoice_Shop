<template>
  <div class="create-customer-page">
    <div class="header-section">
      <h1 class="page-title">Quản lý khách hàng / Thêm Khách hàng</h1>
    </div>

    <div class="card">
      <div class="card-body">
        <form @submit.prevent="submitForm">
          <div class="layout-grid">
            
            <div class="left-col">
              <h3 class="section-title">Thông tin chung</h3>
              <div class="avatar-upload-container">
                <div class="avatar-preview" @click="triggerFileInput">
                  <img v-if="previewImage" :src="previewImage" alt="Avatar Preview">
                  <div v-else class="upload-placeholder">
                    <i class="fas fa-camera"></i>
                    <span>Chọn ảnh</span>
                  </div>
                </div>
                <input type="file" ref="fileInput" class="hidden-input" accept="image/*" @change="handleFileUpload">
              </div>

              <div class="form-group">
                <label class="required">Họ và tên</label>
                <input type="text" v-model="form.tenKhachHang" class="form-control" placeholder="Nhập họ tên...">
              </div>
                <div class="form-group">
                  <label class="required">Tên tài khoản</label>
                  <input type="text" v-model="form.username" class="form-control" placeholder="Nhập tên tài khoản...">
                </div>
              <div class="form-group">
                 <label class="required">Email</label>
                 <input type="email" v-model="form.email" class="form-control" placeholder="example@gmail.com">
              </div>
            </div>

            <div class="right-col">
              <h3 class="section-title">Thông tin chi tiết</h3>

              <div class="form-row">
                <div class="form-group half">
                  <label>Mã khách hàng</label>
                  <input type="text" v-model="form.maKhachHang" class="form-control" placeholder="Tự sinh nếu trống">
                </div>
                <div class="form-group half">
                   <label class="required">Số điện thoại</label>
                   <input type="text" v-model="form.soDienThoai" class="form-control" placeholder="Nhập SĐT...">
                </div>
              </div>

              <div class="form-row">
                <div class="form-group half">
                  <label class="required">Ngày sinh</label>
                  <input type="date" v-model="form.ngaySinh" class="form-control">
                </div>
                <div class="form-group half">
                  <label class="required">Giới tính</label>
                  <div class="radio-group">
                    <label class="radio-item"><input type="radio" :value="true" v-model="form.gioiTinh"> Nam</label>
                    <label class="radio-item"><input type="radio" :value="false" v-model="form.gioiTinh"> Nữ</label>
                  </div>
                </div>
              </div>

              <div class="address-section">
                 <div class="address-header">
                    <h3 class="section-title mb-0">Địa chỉ nhận hàng</h3>
                    <button type="button" class="btn btn-gradient" @click="addNewAddress">
                       <i class="fas fa-plus"></i> Thêm địa chỉ
                    </button>
                 </div>

                 <div v-for="(addr, idx) in form.addresses" :key="idx" class="address-card">
                    <div class="card-top-bar">
                       <label class="radio-default">
                          <input type="radio" name="defaultAddr" :checked="addr.diaChiMacDinh" @change="toggleDefaultAddress(idx)">
                          <span class="ml-2">Đặt làm mặc định</span>
                       </label>
                       <button v-if="form.addresses.length > 1" type="button" class="btn-delete-addr" @click="removeAddress(idx)">
                          <i class="far fa-trash-alt"></i> Xóa
                       </button>
                    </div>

                    <div class="form-row">
                       <div class="form-group half">
                          <label class="required">Tên địa chỉ</label>
                          <input type="text" v-model="addr.tenDiaChi" class="form-control" placeholder="Ví dụ: Nhà riêng / Công ty">
                       </div>
                       <div class="form-group half">
                          <label>Số nhà / Đường</label>
                          <input type="text" v-model="addr.diaChiNhanHang" class="form-control" placeholder="VD: 12A Nguyễn Trãi..." @input="updatePreviewAddress(idx)">
                       </div>
                    </div>

                    <div class="form-row">
                        <div class="form-group third">
                            <label>Tỉnh/Thành</label>
                            <select v-model="addr.tinhId" @change="() => onProvinceChange(idx)" class="form-control">
                                <option value="">-- Chọn --</option>
                                <option v-for="t in provinces" :key="t.code" :value="t.code">{{ t.name }}</option>
                            </select>
                        </div>
                        <div class="form-group third">
                            <label>Quận/Huyện</label>
                            <select v-model="addr.huyenId" @change="() => onDistrictChange(idx)" class="form-control" :disabled="!addr.tinhId">
                                <option value="">-- Chọn --</option>
                                <option v-for="d in districtsList[idx]" :key="d.code" :value="d.code">{{ d.name }}</option>
                            </select>
                        </div>
                        <div class="form-group third">
                            <label>Phường/Xã</label>
                            <select v-model="addr.xaId" class="form-control" :disabled="!addr.huyenId" @change="updatePreviewAddress(idx)">
                                <option value="">-- Chọn --</option>
                                <option v-for="w in wardsList[idx]" :key="w.code" :value="w.code">{{ w.name }}</option>
                            </select>
                        </div>
                    </div>

                    <div class="form-group">
                       <label>Hiển thị</label>
                       <input type="text" v-model="addr.previewText" class="form-control bg-gray" readonly placeholder="...">
                    </div>
                 </div>
              </div>

              <div class="form-actions">
                 <button type="button" class="btn btn-outline" @click="$router.go(-1)">Hủy</button>
                 <button type="submit" class="btn btn-gradient" :disabled="loading">
                    {{ loading ? 'Đang xử lý...' : 'Thêm khách hàng' }}
                 </button>
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
import request from '@/services/request';
import { useRouter } from 'vue-router';
import { toastSuccess, toastError, Toast } from '@/utils/toast';
import axios from 'axios';
import Swal from 'sweetalert2';
import { fetchProvinces as apiFetchProvinces, fetchDistricts as apiFetchDistricts, fetchWards as apiFetchWards } from '@/api/locationApi';

const router = useRouter();
const role = (sessionStorage.getItem('userRole') || 'ADMIN').toUpperCase();
const customerListRouteName = role === 'STAFF' ? 'staff-customer-list' : 'admin-customer-list';
const loading = ref(false);
const fileInput = ref(null);
const previewImage = ref(null);
const avatarFile = ref(null);

const form = reactive({
  maKhachHang: '', tenKhachHang: '', username: '', email: '', soDienThoai: '',
  gioiTinh: true, ngaySinh: '',
  addresses: [
    { 
      tinhId: '', huyenId: '', xaId: '', diaChiNhanHang: '', 
      tenDiaChi: '',
      diaChiMacDinh: true, previewText: '' 
    }
  ]
});

const provinces = ref([]);
const districtsList = ref([[]]); 
const wardsList = ref([[]]);

// Xử lý ảnh
const triggerFileInput = () => fileInput.value.click();
const handleFileUpload = (event) => {
  const file = event.target.files[0];
  if (file) {
    avatarFile.value = file;
    previewImage.value = URL.createObjectURL(file);
  }
};

// Logic Địa chỉ
const addNewAddress = () => {
  form.addresses.push({ 
      tinhId: '', huyenId: '', xaId: '', diaChiNhanHang: '', 
      tenDiaChi: '',
      diaChiMacDinh: false, previewText: ''
  });
  districtsList.value.push([]);
  wardsList.value.push([]);
};

const removeAddress = (index) => {
  if (form.addresses.length <= 1) return;
  form.addresses.splice(index, 1);
  districtsList.value.splice(index, 1);
  wardsList.value.splice(index, 1);
};

const toggleDefaultAddress = (index) => {
  form.addresses.forEach((a, i) => a.diaChiMacDinh = (i === index));
};

// --- AUTO PREVIEW ---
const updatePreviewAddress = (index) => {
    const addr = form.addresses[index];
    const pObj = provinces.value.find(p => p.code == addr.tinhId);
    const dObj = districtsList.value[index].find(d => d.code == addr.huyenId);
    const wObj = wardsList.value[index].find(w => w.code == addr.xaId);

    const parts = [
        addr.diaChiNhanHang,
        wObj ? wObj.name : '',
        dObj ? dObj.name : '',
        pObj ? pObj.name : ''
    ].filter(p => p && p.trim() !== '');

    addr.previewText = parts.join(', ');
};

// API Location
onMounted(async () => {
  try {
    provinces.value = await apiFetchProvinces();
  } catch (e) { console.error('Lỗi load tỉnh', e); }
});

const onProvinceChange = async (index) => {
  form.addresses[index].huyenId = '';
  form.addresses[index].xaId = '';
  districtsList.value[index] = [];
  wardsList.value[index] = [];
  updatePreviewAddress(index);

  if (!form.addresses[index].tinhId) return;
  try {
    districtsList.value[index] = await apiFetchDistricts(form.addresses[index].tinhId);
  } catch (e) {}
};

const onDistrictChange = async (index) => {
  form.addresses[index].xaId = '';
  wardsList.value[index] = [];
  updatePreviewAddress(index);

  if (!form.addresses[index].huyenId) return;
  try {
    wardsList.value[index] = await apiFetchWards(form.addresses[index].huyenId);
  } catch (e) {}
};

// --- SUBMIT (MAPPING QUAN TRỌNG) ---
const submitForm = async () => {
  if (!form.tenKhachHang.trim()) return Toast.fire({ icon: 'warning', title: 'Thiếu tên khách hàng' });
  if (!form.username || !form.username.trim()) return Toast.fire({ icon: 'warning', title: 'Thiếu tên tài khoản' });
  if (!form.soDienThoai) return Toast.fire({ icon: 'warning', title: 'Thiếu số điện thoại' });
  // Kiểm tra SĐT - đúng 10 chữ số
  if (form.soDienThoai.replace(/[^0-9]/g, '').length !== 10) {
    return Toast.fire({ icon: 'warning', title: 'Số điện thoại phải đúng 10 chữ số' });
  }
  // Kiểm tra Ngày sinh - không tương lai và tuổi >= 18
  if (form.ngaySinh) {
    const birthDate = new Date(form.ngaySinh);
    const today = new Date();
    if (birthDate > today) {
      return Toast.fire({ icon: 'warning', title: 'Ngày sinh không thể là ngày tương lai' });
    }
    let age = today.getFullYear() - birthDate.getFullYear();
    const monthDiff = today.getMonth() - birthDate.getMonth();
    if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birthDate.getDate())) {
      age--;
    }
    if (age < 18) {
      return Toast.fire({ icon: 'warning', title: 'Khách hàng phải từ đủ 18 tuổi trở lên' });
    }
  }

  // Kiểm tra trùng tên tài khoản
  try {
    const resUsername = await request.get(`/khach-hang/exists-username?username=${form.username.trim()}`);
    if (resUsername.data.exists) {
      return Toast.fire({ icon: 'warning', title: 'Tên tài khoản đã tồn tại!' });
    }
  } catch (e) {
    console.error('Lỗi check trùng tài khoản:', e);
    return Toast.fire({ icon: 'error', title: 'Lỗi kết nối khi kiểm tra tài khoản!' });
  }

  // Kiểm tra trùng số điện thoại
  try {
    const resSDT = await request.get(`/khach-hang/exists-sdt?soDienThoai=${form.soDienThoai.trim()}`);
    if (resSDT.data.exists) {
      return Toast.fire({ icon: 'warning', title: 'Số điện thoại đã tồn tại!' });
    }
  } catch (e) {
    console.error('Lỗi check trùng SĐT:', e);
    return Toast.fire({ icon: 'error', title: 'Lỗi kết nối khi kiểm tra số điện thoại!' });
  }

  // Dialog xác nhận thêm khách hàng
  const result = await Swal.fire({
    title: 'Xác nhận thêm khách hàng',
    text: 'Hệ thống sẽ tự động gửi email thông báo cho khách hàng.',
    icon: 'question',
    showCancelButton: true,
    confirmButtonText: '<i class="fas fa-check"></i> Xác nhận',
    cancelButtonText: 'Hủy',
    customClass: {
      popup: 'swal-rounded',
      confirmButton: 'swal-btn-solid',
      cancelButton: 'swal-btn-outline'
    },
    buttonsStyling: false
  });

  if (!result.isConfirmed) return;

  // Luôn gửi email khi thêm khách hàng
  const sendEmail = true;

  loading.value = true;
  try {
    const formData = new FormData();
    formData.append('tenKhachHang', form.tenKhachHang);
    formData.append('username', form.username.trim()); 
    formData.append('password', form.password || '123456');
    formData.append('email', form.email);
    formData.append('soDienThoai', form.soDienThoai);
    formData.append('gioiTinh', form.gioiTinh);
    if (form.ngaySinh) formData.append('ngaySinh', form.ngaySinh);
    formData.append('trangThai', 1);
    if (form.maKhachHang) formData.append('maKhachHang', form.maKhachHang);
    formData.append('sendEmail', sendEmail ? 'true' : 'false');

    if (form.addresses && form.addresses.length > 0) {
      const mappedAddresses = form.addresses.map((addr, index) => {
        const pObj = provinces.value.find(p => p.code == addr.tinhId);
        const dObj = districtsList.value[index].find(d => d.code == addr.huyenId);
        const wObj = wardsList.value[index].find(w => w.code == addr.xaId);

        return {
          thanhPho: pObj ? pObj.name : '',       
          quan: dObj ? dObj.name : '',           
          phuong: wObj ? wObj.name : '',         
          diaChiCuThe: addr.diaChiNhanHang, 
          tenNguoiNhan: addr.tenNguoiNhan || form.tenKhachHang, 
          soDienThoai: addr.sdtNguoiNhan || form.soDienThoai,
          macDinh: addr.diaChiMacDinh,      
          trangThai: 1
        };
      });
      formData.append('addresses', JSON.stringify(mappedAddresses));
    }

    if (avatarFile.value) formData.append('avatarFile', avatarFile.value);

    await request.post('/khach-hang', formData);
    toastSuccess('Thêm khách hàng thành công!');
    router.push({ name: customerListRouteName });
  } catch (error) {
    console.error(error);
    const backendMessage = typeof error.response?.data === 'string'
      ? error.response.data
      : error.response?.data?.message;
    toastError(backendMessage || 'Có lỗi xảy ra');
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
.page-title { color: #2b4360; font-weight: 700; font-size: 24px; margin-bottom: 20px; }
.create-customer-page { font-family: 'Segoe UI', sans-serif; background: #ebecee; min-height: 100vh; padding: 20px; }
.header-section { margin-bottom: 20px; }

/* === UPDATE CSS: Card Styling (Viền xanh) === */
.card { 
    background: #fff; 
    border-radius: 16px; /* Bo góc tròn hơn */
    box-shadow: 0 4px 12px rgba(0,0,0,0.05); 
    border: 1px solid #bfdbfe !important; /* Viền màu xanh dương nhạt */
}
.card-body { padding: 30px; }

.layout-grid { display: grid; grid-template-columns: 320px 1fr; gap: 40px; }
.left-col { border-right: 1px solid #f1f5f9; padding-right: 30px; }
.section-title { font-size: 16px; font-weight: 700; color: #0f172a; margin-bottom: 20px; text-transform: uppercase; }

/* Avatar */
.avatar-upload-container { display: flex; justify-content: center; margin-bottom: 25px; }
.avatar-preview { width: 180px; height: 180px; border-radius: 50%; border: 2px dashed #cbd5e1; display: flex; align-items: center; justify-content: center; cursor: pointer; background: #f8fafc; overflow: hidden; }
.avatar-preview img { width: 100%; height: 100%; object-fit: cover; }
.upload-placeholder { display: flex; flex-direction: column; align-items: center; color: #64748b; }
.hidden-input { display: none; }

/* Form Elements */
.form-row { display: flex; gap: 20px; margin-bottom: 15px; }
.form-group { margin-bottom: 15px; } .form-group.half { flex: 1; } .form-group.third { flex: 1; }
label { display: block; margin-bottom: 6px; font-weight: 600; font-size: 13px; color: #334155; }
.required::after { content: " *"; color: #ef4444; }
.form-control { width: 100%; padding: 10px 12px; border: 1px solid #e2e8f0; border-radius: 6px; outline: none; font-size: 14px; transition: 0.2s; }
.form-control:focus { border-color: #3b82f6; box-shadow: 0 0 0 3px rgba(59,130,246,0.1); }
.bg-gray { background-color: #f8fafc; color: #64748b; }

/* Radio */
.radio-group { display: flex; gap: 20px; align-items: center; height: 42px; }
.radio-item { display: flex; align-items: center; gap: 6px; cursor: pointer; }

/* Address UI */
.address-section { margin-top: 30px; padding-top: 20px; border-top: 1px dashed #e2e8f0; }
.address-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 15px; }

/* === UPDATE CSS: Address Card (Viền xanh) === */
.address-card { 
    background: #fff; 
    border: 1px solid #bfdbfe; /* Viền xanh cho card địa chỉ con */
    border-radius: 8px; 
    padding: 20px; 
    margin-bottom: 15px; 
    position: relative; 
    box-shadow: 0 2px 4px rgba(0,0,0,0.03); 
}

.card-top-bar { display: flex; justify-content: space-between; margin-bottom: 15px; border-bottom: 1px solid #f1f5f9; padding-bottom: 10px; }
.btn-delete-addr { border: none; background: none; color: #ef4444; font-size: 13px; cursor: pointer; font-weight: 500; }
.radio-default { font-size: 13px; font-weight: 600; color: #0f172a; cursor: pointer; display: flex; align-items: center; }
.ml-2 { margin-left: 6px; }

/* Buttons */
.form-actions { display: flex; justify-content: flex-end; gap: 10px; margin-top: 30px; border-top: 1px solid #f1f5f9; padding-top: 20px; }
.btn { padding: 10px 24px; border-radius: 6px; font-weight: 600; cursor: pointer; border: 1px solid transparent; font-size: 14px; }
.btn-outline { background: #fff; border-color: #cbd5e1; color: #475569; }
.btn-gradient { background: linear-gradient(135deg, #1e3a8a 0%, #0f172a 100%); color: #fff; box-shadow: 0 4px 10px rgba(15, 23, 42, 0.3); transition: 0.2s; }
.btn-gradient:hover { transform: translateY(-1px); box-shadow: 0 6px 15px rgba(15, 23, 42, 0.4); }
.btn:disabled { opacity: 0.7; cursor: not-allowed; }
.btn-sm { padding: 6px 12px; font-size: 13px; }
.form-control::placeholder {

    color: #000000 !important;  /* Màu đen */
    opacity: 0.5 !important;      /* Chống mờ */
    font-weight: 500;           /* Đậm lên tí cho dễ đọc (tùy chọn) */

}
</style>