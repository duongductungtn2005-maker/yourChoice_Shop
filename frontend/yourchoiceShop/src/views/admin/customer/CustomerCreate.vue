<template>
  <div class="create-customer-page">
    <div class="header-section">
      <!-- <div class="breadcrumb">
        <span class="text-gray cursor-pointer" @click="$router.push('/admin/khach-hang')">Khách hàng</span>
        <span class="divider">/</span>
        <span class="active">Thêm khách hàng</span>
      </div> -->
      <h1 class="page-title">Quản lý khách hàng / Thêm Khách hàng</h1>
    </div>

    <div class="card">
      <div class="card-body">
        <form @submit.prevent="submitForm">
          <div class="layout-grid">
            
            <div class="left-col">
              <h3 class="section-title">Thông tin khách hàng</h3>
              
              <div class="avatar-upload-container">
                <div class="avatar-preview" @click="triggerFileInput">
                  <img v-if="previewImage" :src="previewImage" alt="Avatar Preview">
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
                  @change="handleFileUpload"
                >
              </div>

              <div class="form-group">
                <label class="required">Họ và tên</label>
                <input 
                  type="text" 
                  v-model="form.tenKhachHang" 
                  class="form-control" 
                  placeholder="Nhập họ tên..."
                >
              </div>
            </div>

            <div class="right-col">
              <h3 class="section-title">Thông tin chi tiết</h3>

              <div class="form-row">
                <div class="form-group half">
                  <label>Mã khách hàng (Tự sinh nếu để trống)</label>
                  <input 
                    type="text" 
                    v-model="form.maKhachHang" 
                    class="form-control" 
                    placeholder="VD: KH001"
                  >
                </div>
                <div class="form-group half">
                  <label class="required">Giới tính</label>
                  <div class="radio-group">
                    <label class="radio-item">
                      <input type="radio" :value="true" v-model="form.gioiTinh"> Nam
                    </label>
                    <label class="radio-item">
                      <input type="radio" :value="false" v-model="form.gioiTinh"> Nữ
                    </label>
                  </div>
                </div>
              </div>

              <div class="form-row">
                <div class="form-group half">
                  <label class="required">Tên tài khoản</label>
                  <input
                    type="text"
                    v-model="form.username"
                    class="form-control"
                    placeholder="Nhập tên tài khoản..."
                  >
                </div>
                <div class="form-group half">
                  <label class="required">Mật khẩu</label>
                  <input
                    type="password"
                    v-model="form.password"
                    class="form-control"
                    placeholder="Nhập mật khẩu..."
                  >
                </div>
              </div>

              <div class="form-row">
                <div class="form-group half">
                  <label class="required">Ngày sinh</label>
                  <input 
                    type="date" 
                    v-model="form.ngaySinh" 
                    class="form-control"
                  >
                </div>
                <div class="form-group half">
                  <label class="required">Email</label>
                  <input 
                    type="email" 
                    v-model="form.email" 
                    class="form-control" 
                    placeholder="example@gmail.com"
                  >
                </div>
              </div>

              <div class="form-row">
                <div class="form-group half">
                  <label class="required">Số điện thoại</label>
                  <input 
                    type="text" 
                    v-model="form.soDienThoai" 
                    class="form-control" 
                    placeholder="Nhập SĐT..."
                  >
                </div>
                <div class="form-group half">
                   <label>Trạng thái</label>
                   <select v-model="form.trangThai" class="form-control">
                      <option :value="1">Hoạt động</option>
                      <option :value="0">Ngừng hoạt động</option>
                   </select>
                </div>
              </div>

              <div class="address-header">
                <h3 class="section-title" style="margin-top:10px;">Địa chỉ nhận hàng</h3>
                <button type="button" class="btn btn-outline btn-add-address" @click="addNewAddress">+ Thêm địa chỉ mới</button>
              </div>

              <div>
                <div v-for="(addr, idx) in form.addresses" :key="idx" class="address-box" style="margin-bottom:14px;">
                    <div class="form-row address-row">
                    <div class="form-group quarter">
                      <select v-model="addr.tinhId" @change="() => onProvinceChange(idx)" class="form-control">
                        <option value="">-- Tỉnh/TP --</option>
                        <option v-for="t in provinces" :key="t.code" :value="t.code">{{ t.name }}</option>
                      </select>
                    </div>
                    <div class="form-group quarter">
                      <select v-model="addr.huyenId" @change="() => onDistrictChange(idx)" class="form-control" :disabled="!addr.tinhId">
                        <option value="">-- Quận/Huyện --</option>
                        <option v-for="d in districtsList[idx]" :key="d.code" :value="d.code">{{ d.name }}</option>
                      </select>
                    </div>
                    <div class="form-group quarter">
                      <select v-model="addr.xaId" class="form-control" :disabled="!addr.huyenId">
                        <option value="">-- Phường/Xã --</option>
                        <option v-for="w in wardsList[idx]" :key="w.code" :value="w.code">{{ w.name }}</option>
                      </select>
                    </div>
                    <div class="form-group quarter">
                      <input type="text" v-model="addr.diaChiNhanHang" class="form-control" placeholder="Số nhà, đường...">
                    </div>
                  </div>

                  <div style="margin-top:12px; display:flex; gap:8px; align-items:center;">
                    <button type="button" :class="['btn', 'btn-default-address', addr.diaChiMacDinh ? 'active' : '']" @click="() => toggleDefaultAddress(idx)">
                      <span v-if="addr.diaChiMacDinh">✓</span> {{ addr.diaChiMacDinh ? 'Mặc định' : 'Đặt làm mặc định' }}
                    </button>
                    <button v-if="form.addresses.length > 1" type="button" class="btn btn-outline" @click="() => removeAddress(idx)">Xóa</button>
                  </div>
                </div>
              </div>

              <div class="form-actions">
                 <button type="button" class="btn btn-outline" @click="$router.go(-1)">Hủy</button>
                 <button type="submit" class="btn btn-primary" :disabled="loading">
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
import axios from 'axios';
import request from '@/services/request';
import Swal from 'sweetalert2';
import { useRouter } from 'vue-router';

const router = useRouter();
const loading = ref(false);
const fileInput = ref(null);
const previewImage = ref(null);
const avatarFile = ref(null);

const form = reactive({
  maKhachHang: '',
  tenKhachHang: '',
  email: '',
  soDienThoai: '',
  username: '',
  password: '',
  gioiTinh: true, // Mặc định Nam
  ngaySinh: '',
  trangThai: 1,
  // Địa chỉ nhận hàng: hỗ trợ nhiều địa chỉ
  addresses: [
    { tinhId: '', huyenId: '', xaId: '', diaChiNhanHang: '', diaChiMacDinh: false }
  ]
});

// Xử lý chọn ảnh
const triggerFileInput = () => {
  fileInput.value.click();
};

const handleFileUpload = (event) => {
  const file = event.target.files[0];
  if (file) {
    avatarFile.value = file;
    // Tạo URL preview ảnh
    previewImage.value = URL.createObjectURL(file);
  }
};

// Address helper data (populate from API if available)
const provinces = ref([]);
// For multiple address boxes we keep per-box lists
const districtsList = ref([[]]);
const wardsList = ref([[]]);

const toggleDefaultAddress = (index) => {
  // Only one address should be default
  form.addresses.forEach((a, i) => a.diaChiMacDinh = (i === index ? !a.diaChiMacDinh : false));
};

const addNewAddress = () => {
  form.addresses.push({ tinhId: '', huyenId: '', xaId: '', diaChiNhanHang: '', diaChiMacDinh: false });
  districtsList.value.push([]);
  wardsList.value.push([]);
};

const removeAddress = (index) => {
  if (form.addresses.length <= 1) return; // keep at least one
  form.addresses.splice(index, 1);
  districtsList.value.splice(index, 1);
  wardsList.value.splice(index, 1);
};

// Load provinces on mount and handle dependent selects
onMounted(async () => {
  try {
    const res = await axios.get('https://provinces.open-api.vn/api/?depth=1');
    provinces.value = res.data;
  } catch (e) {
    console.error('Không thể load tỉnh thành', e);
  }
});

const onProvinceChange = async (index) => {
  // index is the address box index
  form.addresses[index].huyenId = '';
  form.addresses[index].xaId = '';
  districtsList.value[index] = [];
  wardsList.value[index] = [];
  if (!form.addresses[index].tinhId) return;
  try {
    const res = await axios.get(`https://provinces.open-api.vn/api/p/${form.addresses[index].tinhId}?depth=2`);
    districtsList.value[index] = res.data.districts || [];
  } catch (e) {
    console.error('Không thể load quận/huyện', e);
  }
};

const onDistrictChange = async (index) => {
  form.addresses[index].xaId = '';
  wardsList.value[index] = [];
  if (!form.addresses[index].huyenId) return;
  try {
    const res = await axios.get(`https://provinces.open-api.vn/api/d/${form.addresses[index].huyenId}?depth=2`);
    wardsList.value[index] = res.data.wards || [];
  } catch (e) {
    console.error('Không thể load phường/xã', e);
  }
};

// Gửi form
const submitForm = async () => {
  // Validate cơ bản
  if (!form.tenKhachHang || !form.email || !form.soDienThoai) {
    Swal.fire({
        icon: 'warning',
        title: 'Thiếu thông tin',
        text: 'Vui lòng nhập Họ tên, Email và Số điện thoại!'
    });
    return;
  }

  loading.value = true;

  try {
    const formData = new FormData();
    formData.append('tenKhachHang', form.tenKhachHang);
    formData.append('email', form.email);
    formData.append('soDienThoai', form.soDienThoai);
    formData.append('gioiTinh', form.gioiTinh);
    formData.append('trangThai', form.trangThai);
    if (form.username) formData.append('username', form.username);
    if (form.password) formData.append('password', form.password);
    
    if (form.maKhachHang) formData.append('maKhachHang', form.maKhachHang);
    if (form.ngaySinh) formData.append('ngaySinh', form.ngaySinh);

    // Địa chỉ nhận hàng (nhiều địa chỉ)
    if (form.addresses && form.addresses.length) {
      formData.append('addresses', JSON.stringify(form.addresses));
    }

    if (avatarFile.value) formData.append('avatarFile', avatarFile.value);

    // Debug: log payload contents for easier troubleshooting
    try {
      const debugPayload = {};
      formData.forEach((value, key) => {
        // For binary data (avatarFile), just note the filename
        if (value instanceof File) debugPayload[key] = value.name;
        else debugPayload[key] = value;
      });
      console.debug('Submitting /khach-hang', debugPayload);
    } catch (e) { console.debug('Could not serialize FormData for debug', e); }

    await request.post('/khach-hang', formData);

    await Swal.fire({
        icon: 'success',
        title: 'Thành công',
        text: 'Đã thêm khách hàng mới!',
        timer: 1500,
        showConfirmButton: false
    });

    router.push({ name: 'admin-customer-list' });

  } catch (error) {
    console.error('Error creating customer:', error);
    const serverMessage = error.response?.data?.message || error.response?.data || error.message || 'Không thể thêm khách hàng.';
    Swal.fire({
        icon: 'error',
        title: 'Lỗi',
        html: `<div style="text-align:left">${String(serverMessage).replace(/\n/g, '<br>')}</div>`
    });
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
.page-title { color: #2b4360; font-weight: 700; font-size: 24px; margin-bottom: 20px; }

.create-customer-page { font-family: 'Segoe UI', sans-serif; background-color: #f8fafc; min-height: 100vh; padding: 20px; }
.header-section { margin-bottom: 20px; }
.breadcrumb { font-size: 14px; color: #64748b; }
.breadcrumb .active { font-weight: 600; color: #0f172a; margin-left: 5px; }
.divider { margin: 0 5px; color: #cbd5e1; }
.text-gray { color: #64748b; }
.cursor-pointer { cursor: pointer; }

.card { background: #fff; border-radius: 8px; box-shadow: 0 1px 3px rgba(0,0,0,0.05); }
.card-body { padding: 30px; }

/* GRID LAYOUT: Chia 2 cột 30% - 70% */
.layout-grid { display: grid; grid-template-columns: 300px 1fr; gap: 40px; }

/* LEFT COLUMN STYLE */
.left-col { border-right: 1px solid #f1f5f9; padding-right: 30px; }
.section-title { font-size: 16px; font-weight: 700; color: #0f172a; margin-bottom: 20px; text-transform: uppercase; }

/* Avatar Upload Circle */
.avatar-upload-container { display: flex; justify-content: center; margin-bottom: 25px; }
.avatar-preview { 
    width: 180px; height: 180px; 
    border-radius: 50%; 
    border: 2px dashed #cbd5e1; 
    display: flex; align-items: center; justify-content: center; 
    cursor: pointer; overflow: hidden; position: relative; background: #f8fafc;
    transition: 0.2s;
}
.avatar-preview:hover { border-color: #3b82f6; background: #eff6ff; }
.avatar-preview img { width: 100%; height: 100%; object-fit: cover; }
.upload-placeholder { display: flex; flex-direction: column; align-items: center; color: #64748b; }
.upload-placeholder i { font-size: 24px; margin-bottom: 5px; }
.hidden-input { display: none; }

/* RIGHT COLUMN STYLE */
.form-row { display: flex; gap: 20px; margin-bottom: 15px; }
.form-group { margin-bottom: 15px; }
.form-group.half { flex: 1; }

label { display: block; margin-bottom: 8px; font-weight: 600; font-size: 14px; color: #334155; }
.required::after { content: " *"; color: #ef4444; }

.form-control { 
    width: 100%; padding: 10px 12px; 
    border: 1px solid #e2e8f0; border-radius: 6px; 
    outline: none; font-size: 14px; transition: 0.2s;
}
.form-control:focus { border-color: #3b82f6; box-shadow: 0 0 0 3px rgba(59,130,246,0.1); }

/* Radio Group */
.radio-group { display: flex; gap: 20px; align-items: center; height: 42px; }
.radio-item { display: flex; align-items: center; gap: 6px; cursor: pointer; font-weight: 400; }
.radio-item input { width: 16px; height: 16px; accent-color: #0f172a; }

/* Actions */
.form-actions { display: flex; justify-content: flex-end; gap: 10px; margin-top: 30px; padding-top: 20px; border-top: 1px solid #f1f5f9; }
.btn { padding: 10px 24px; border-radius: 6px; font-weight: 600; cursor: pointer; border: 1px solid transparent; font-size: 14px; }
.btn-primary { background: #0f172a; color: #fff; }
.btn-primary:hover { background: #1e293b; }
.btn-outline { background: #fff; border-color: #cbd5e1; color: #475569; }
.btn-outline:hover { background: #f8fafc; }
.btn:disabled { opacity: 0.7; cursor: not-allowed; }

/* Address styles */
.address-header { display:flex; justify-content:space-between; align-items:center; margin-top:10px; }
.btn-add-address { background: transparent; border:2px dashed #cbd5e1; color:#475569; padding:8px 12px; border-radius:6px; cursor:pointer; }
.btn-add-address:hover, .btn-add-address:focus { border-color: #3b82f6; color: #3b82f6; }
.address-box { position:relative; background:#fafafa; border:1px solid #eceff2; padding:16px; border-radius:6px; margin-bottom:16px; }
.address-row .quarter { flex: 1; min-width: 150px; }
.btn-default-address { background:#f3f4f6; border:1px solid #475569; color:#5b6b76; padding:8px 14px; border-radius:30px; cursor:pointer; }
.btn-default-address:hover { border-color:#475569; }
.btn-default-address.active { background:#475569; color:#fff; border-color:#475569; }
</style>