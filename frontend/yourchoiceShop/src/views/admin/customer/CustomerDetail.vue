<template>
  <div class="customer-detail-page">
    <div class="header-section">
      <!-- <div class="breadcrumb">
        <span class="text-gray cursor-pointer" @click="$router.push({ name: 'admin-customer-list' })">Khách hàng</span>
        <span class="divider">/</span>
        <span class="active">Chi tiết khách hàng</span>
      </div> -->
      <h1 class="page-title">Quản lý khách hàng / Chi tiết khách hàng</h1>
    </div>

    <div class="main-container">
      <div class="left-panel card">
        <h3 class="panel-title">Thông tin khách hàng</h3>
        <form @submit.prevent="updateCustomer">
          <div class="avatar-section">
            <div class="avatar-wrapper" @click="triggerFileInput">
              <img v-if="previewImage" :src="previewImage" class="avatar-img" />
              <div v-else class="avatar-placeholder">
                <i class="fas fa-camera"></i>
                <span>Chọn ảnh</span>
              </div>
            </div>
            <input type="file" ref="fileInput" class="hidden-input" accept="image/*" @change="handleFileUpload">
          </div>
          <div class="form-body">
            <div class="form-group"><label>Mã KH</label><input type="text" v-model="form.maKhachHang" class="form-control" disabled></div>
            <div class="form-group"><label class="required">Tên khách hàng</label><input type="text" v-model="form.tenKhachHang" class="form-control"></div>
            <div class="form-group"><label class="required">Tên tài khoản</label><input type="text" v-model="form.username" class="form-control"></div>
            <div class="form-group"><label class="required">Email</label><input type="email" v-model="form.email" class="form-control"></div>
            <div class="form-group"><label class="required">Số điện thoại</label><input type="text" v-model="form.soDienThoai" class="form-control"></div>
            <div class="form-group"><label>Ngày sinh</label><input type="date" v-model="form.ngaySinh" class="form-control"></div>
            <div class="form-group">
              <label class="required">Giới tính</label>
              <div class="radio-group">
                <label class="radio-item"><input type="radio" :value="true" v-model="form.gioiTinh"> Nam</label>
                <label class="radio-item"><input type="radio" :value="false" v-model="form.gioiTinh"> Nữ</label>
              </div>
            </div>
            <div class="form-group mt-3">
               <button type="submit" class="btn btn-update w-100" :disabled="loading">Cập nhật thông tin</button>
            </div>
          </div>
        </form>
      </div>

      <div class="right-panel card">
        <h3 class="panel-title">Danh sách địa chỉ</h3>

        <div class="address-list">
          <div v-if="addresses.length === 0" class="empty-text">Chưa có địa chỉ nào.</div>
          
          <div class="address-item" v-for="(addr, index) in addresses" :key="addr.id">
             <div class="address-header">
                <strong>Địa chỉ {{ index + 1 }} <span v-if="addr.macDinh" class="text-default">(Mặc định)</span></strong>
                <div class="address-actions">
                   <i class="fas fa-star star-icon" :class="{ active: addr.macDinh }" @click="setDefaultAddress(addr)" title="Đặt mặc định"></i>
                   <i class="fas fa-pen edit-icon" @click="openAddressModal(addr)" title="Sửa"></i>
                   <i class="fas fa-trash delete-icon" @click="deleteAddress(addr.id)" title="Xóa"></i>
                </div>
             </div>
             
             <div class="row-inputs">
                <div class="col-half">
                    <label>Tên người nhận</label>
                    <input type="text" class="form-control" :value="addr.tenNguoiNhan" readonly>
                </div>
                <div class="col-half">
                    <label>SĐT</label>
                    <input type="text" class="form-control" :value="addr.soDienThoai" readonly>
                </div>
             </div>
             <div class="form-group mt-2">
                <label>Địa chỉ</label>
                <div class="address-text">
                    {{ addr.diaChiCuThe }}, {{ addr.phuong }}, {{ addr.quan }}, {{ addr.thanhPho }}
                </div>
             </div>
             <hr v-if="index < addresses.length - 1">
          </div>
        </div>

        <div class="panel-footer">
           <button class="btn btn-outline-primary" @click="openAddressModal(null)">+ Thêm địa chỉ</button>
        </div>
      </div>
    </div>

    <div v-if="showModal" class="modal-overlay">
       <div class="modal-content">
          <h3>{{ isEditMode ? 'Cập nhật địa chỉ' : 'Thêm địa chỉ mới' }}</h3>
          <form @submit.prevent="submitAddress">
             <div class="form-row">
                <div class="form-group half"><label>Tên người nhận</label><input type="text" v-model="addrForm.tenNguoiNhan" class="form-control" required></div>
                <div class="form-group half"><label>SĐT</label><input type="text" v-model="addrForm.sdt" class="form-control" required></div>
             </div>
             <div class="form-row">
                <div class="form-group third">
                   <label>Tỉnh/Thành</label>
                   <select v-model="selectedCity" @change="onCityChange" class="form-control">
                      <option :value="null">-- Chọn Tỉnh --</option>
                      <option v-for="c in locationData.cities" :key="c.code" :value="c">{{ c.name }}</option>
                   </select>
                </div>
                <div class="form-group third">
                   <label>Quận/Huyện</label>
                   <select v-model="selectedDistrict" @change="onDistrictChange" class="form-control">
                      <option :value="null">-- Chọn Huyện --</option>
                      <option v-for="d in locationData.districts" :key="d.code" :value="d">{{ d.name }}</option>
                   </select>
                </div>
                <div class="form-group third">
                   <label>Phường/Xã</label>
                   <select v-model="selectedWard" class="form-control">
                      <option :value="null">-- Chọn Xã --</option>
                      <option v-for="w in locationData.wards" :key="w.code" :value="w">{{ w.name }}</option>
                   </select>
                </div>
             </div>
             <div class="form-group">
                <label>Số nhà, đường</label>
                <input type="text" v-model="addrForm.duong" class="form-control" required placeholder="Số 1, Ngõ 2...">
             </div>
             <div class="form-group">
                <label class="checkbox-label">
                   <input type="checkbox" v-model="addrForm.macDinh"> Đặt làm địa chỉ mặc định
                </label>
             </div>
             <div class="modal-actions">
                <button type="button" class="btn btn-outline" @click="showModal = false">Hủy</button>
                <button type="submit" class="btn btn-primary">Lưu</button>
             </div>
          </form>
       </div>
    </div>

  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import request from '@/services/request';
import Swal from 'sweetalert2';
import axios from 'axios';

const route = useRoute();
const router = useRouter();
const id = route.params.id; // ID Khách hàng
const role = (localStorage.getItem('userRole') || 'ADMIN').toUpperCase();
const customerListRouteName = role === 'STAFF' ? 'staff-customer-list' : 'admin-customer-list';

// --- STATE KHÁCH HÀNG ---
const loading = ref(false);
const fileInput = ref(null);
const previewImage = ref(null);
const avatarFile = ref(null);
const form = reactive({ tenKhachHang: '', username: '', email: '', soDienThoai: '', ngaySinh: '', gioiTinh: true, maKhachHang: '', trangThai: 1, avatar: '' });

// --- STATE ĐỊA CHỈ ---
const addresses = ref([]);
const showModal = ref(false);
const isEditMode = ref(false);
const currentAddrId = ref(null);
const addrForm = reactive({ tenNguoiNhan: '', sdt: '', duong: '', macDinh: false });

// --- STATE LOCATION ---
const locationData = reactive({ cities: [], districts: [], wards: [] });
const selectedCity = ref(null);
const selectedDistrict = ref(null);
const selectedWard = ref(null);

// 1. LẤY THÔNG TIN KHÁCH HÀNG
const fetchCustomer = async () => {
  try {
    const res = await request.get(`/khach-hang/${id}`);
    const data = res.data;
    Object.assign(form, data);
    form.username = data.tenTaiKhoan || data.username || '';
    if (data.ngaySinh && Array.isArray(data.ngaySinh)) {
        form.ngaySinh = `${data.ngaySinh[0]}-${String(data.ngaySinh[1]).padStart(2,'0')}-${String(data.ngaySinh[2]).padStart(2,'0')}`;
    }
    if (data.avatar) previewImage.value = `http://localhost:8080/api/v1/khach-hang/images/${data.avatar}`;
  } catch (e) { console.error(e); }
};

// 2. LẤY DANH SÁCH ĐỊA CHỈ
const fetchAddresses = async () => {
    try {
        const res = await request.get(`/dia-chi`, { params: { khachHangId: id } });
        addresses.value = res.data;
    } catch (e) { console.error("Lỗi lấy địa chỉ"); }
};

// 3. API HÀNH CHÍNH CÔNG
const fetchCities = async () => {
    const res = await axios.get('https://provinces.open-api.vn/api/?depth=1');
    locationData.cities = res.data;
};
const onCityChange = async () => {
    selectedDistrict.value = null; selectedWard.value = null; locationData.districts = []; locationData.wards = [];
    if (selectedCity.value) {
        const res = await axios.get(`https://provinces.open-api.vn/api/p/${selectedCity.value.code}?depth=2`);
        locationData.districts = res.data.districts;
    }
};
const onDistrictChange = async () => {
    selectedWard.value = null; locationData.wards = [];
    if (selectedDistrict.value) {
        const res = await axios.get(`https://provinces.open-api.vn/api/d/${selectedDistrict.value.code}?depth=2`);
        locationData.wards = res.data.wards;
    }
};

// 4. XỬ LÝ MODAL ĐỊA CHỈ
const openAddressModal = (addr) => {
    if (addr) {
        isEditMode.value = true;
        currentAddrId.value = addr.id;
        addrForm.tenNguoiNhan = addr.tenNguoiNhan;
        // SỬA 3: Map dữ liệu khi sửa
        addrForm.sdt = addr.soDienThoai;
        addrForm.duong = addr.diaChiCuThe;
        addrForm.macDinh = addr.macDinh;
        
        selectedCity.value = null; selectedDistrict.value = null; selectedWard.value = null;
    } else {
        isEditMode.value = false;
        Object.assign(addrForm, { tenNguoiNhan: form.tenKhachHang, sdt: form.soDienThoai, duong: '', macDinh: false });
        selectedCity.value = null; selectedDistrict.value = null; selectedWard.value = null;
    }
    showModal.value = true;
};

const submitAddress = async () => {
    if (!selectedCity.value || !selectedDistrict.value || !selectedWard.value) {
        Swal.fire('Cảnh báo', 'Vui lòng chọn đầy đủ địa chỉ hành chính', 'warning'); return;
    }
    
    // SỬA 4: Chuẩn hóa payload gửi lên Server khớp với DTO Backend
    const payload = {
        idKhachHang: id,
        tenNguoiNhan: addrForm.tenNguoiNhan,
        soDienThoai: addrForm.sdt,
        thanhPho: selectedCity.value.name,
        quan: selectedDistrict.value.name,
        phuong: selectedWard.value.name,
        diaChiCuThe: addrForm.duong,
        macDinh: addrForm.macDinh,
        trangThai: 1
    };

    try {
        if (isEditMode.value) {
            await request.put(`/dia-chi/${currentAddrId.value}`, payload);
        } else {
            await request.post(`/dia-chi`, payload);
        }
        showModal.value = false;
        fetchAddresses(); // Load lại danh sách sau khi thêm/sửa
        Swal.fire('Thành công', 'Đã lưu địa chỉ', 'success');
    } catch (e) { 
        console.error(e);
        Swal.fire('Lỗi', 'Không thể lưu địa chỉ', 'error'); 
    }
};

const deleteAddress = async (addrId) => {
    const result = await Swal.fire({ title: 'Xóa địa chỉ này?', icon: 'warning', showCancelButton: true, confirmButtonText: 'Xóa' });
    if (result.isConfirmed) {
        await request.delete(`/dia-chi/${addrId}`);
        fetchAddresses();
    }
};

const setDefaultAddress = async (addr) => {
    if (addr.macDinh) return;
    await request.put(`/dia-chi/${addr.id}/set-default`);
    fetchAddresses();
    const Toast = Swal.mixin({ toast: true, position: 'top-end', showConfirmButton: false, timer: 1500 });
    Toast.fire({ icon: 'success', title: 'Đã đặt làm mặc định' });
};

// CÁC HÀM CŨ (Update Customer, Upload ảnh)
const triggerFileInput = () => fileInput.value.click();
const handleFileUpload = (e) => { const f = e.target.files[0]; if(f){ avatarFile.value=f; previewImage.value=URL.createObjectURL(f); }};
const updateCustomer = async () => {
  if (!form.username || !form.username.trim()) {
    return Swal.fire('Cảnh báo', 'Tên tài khoản không được để trống', 'warning');
  }

    loading.value = true;
    try {
        const fd = new FormData();
        fd.append('tenKhachHang', form.tenKhachHang); fd.append('email', form.email);
    fd.append('username', form.username.trim());
        fd.append('soDienThoai', form.soDienThoai); fd.append('gioiTinh', form.gioiTinh);
        if(form.ngaySinh) fd.append('ngaySinh', form.ngaySinh);
        if(avatarFile.value) fd.append('avatarFile', avatarFile.value);
        await request.put(`/khach-hang/${id}`, fd);
        await Swal.fire({
          icon: 'success',
          title: 'Thành công',
          text: 'Cập nhật thông tin khách hàng thành công',
          timer: 1500,
          showConfirmButton: false
        });
        router.push({ name: customerListRouteName });
  } catch(e) {
    console.error(e);
    const backendMessage = typeof e.response?.data === 'string'
      ? e.response.data
      : (e.response?.data?.message || 'Không thể cập nhật thông tin');
    Swal.fire('Lỗi', backendMessage, 'error');
  } finally { loading.value = false; }
};

onMounted(() => {
    fetchCustomer();
    fetchAddresses();
    fetchCities();
});
</script>

<style scoped>
/* CSS Tương tự các màn trước */
.page-title { color: #2b4360; font-weight: 700; font-size: 24px; margin-bottom: 20px; }

.customer-detail-page { background: #ebecee; min-height: 100vh; padding: 20px; font-family: 'Segoe UI', sans-serif; }
.header-section { margin-bottom: 20px; } .breadcrumb { font-size: 14px; color: #64748b; }
.active { font-weight: 600; color: #0f172a; margin-left: 5px; } .cursor-pointer { cursor: pointer; }
.main-container { display: grid; grid-template-columns: 350px 1fr; gap: 20px; align-items: start; }
.card { background: #fff; border-radius: 8px; box-shadow: 0 1px 3px rgba(0,0,0,0.05); padding: 20px; }
.panel-title { font-size: 16px; font-weight: 700; color: #1e293b; margin-bottom: 20px; border-bottom: 1px solid #f1f5f9; padding-bottom: 10px; }

/* Left Panel */
.avatar-section { display: flex; justify-content: center; margin-bottom: 20px; }
.avatar-wrapper { width: 120px; height: 120px; border-radius: 50%; border: 2px dashed #cbd5e1; display: flex; align-items: center; justify-content: center; cursor: pointer; overflow: hidden; background: #f8fafc; }
.avatar-img { width: 100%; height: 100%; object-fit: cover; } .hidden-input { display: none; }
.form-group { margin-bottom: 15px; } label { display: block; font-weight: 600; font-size: 13px; color: #475569; margin-bottom: 5px; }
.form-control { width: 100%; padding: 8px 12px; border: 1px solid #e2e8f0; border-radius: 6px; outline: none; }
.radio-group { display: flex; gap: 20px; } .btn-update { background: #1e293b; color: #fff; padding: 10px; border-radius: 6px; border: none; cursor: pointer; }

/* Right Panel (Address) */
.address-item { padding: 15px; border: 1px solid #e2e8f0; border-radius: 8px; margin-bottom: 15px; background: #fff; }
.address-header { display: flex; justify-content: space-between; margin-bottom: 10px; font-size: 14px; }
.text-default { color: #10b981; font-size: 12px; margin-left: 5px; }
.address-actions i { margin-left: 10px; cursor: pointer; color: #94a3b8; transition: 0.2s; }
.address-actions i:hover { transform: scale(1.1); }
.star-icon.active { color: #eab308; } .edit-icon:hover { color: #3b82f6; } .delete-icon:hover { color: #ef4444; }
.row-inputs { display: flex; gap: 10px; margin-bottom: 10px; } .col-half { flex: 1; }
.address-text { font-size: 14px; color: #334155; }
.btn-outline-primary { background: #fff; border: 1px solid #1e293b; color: #1e293b; padding: 8px 16px; border-radius: 6px; cursor: pointer; }

/* Modal */
.modal-overlay { position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.5); display: flex; justify-content: center; align-items: center; z-index: 1000; }
.modal-content { background: #fff; padding: 25px; border-radius: 8px; width: 500px; }
.modal-content h3 { margin-top: 0; margin-bottom: 20px; color: #1e293b; }
.form-row { display: flex; gap: 10px; } .half { flex: 1; } .third { flex: 1; }
.modal-actions { display: flex; justify-content: flex-end; gap: 10px; margin-top: 20px; }
.btn-primary { background: #1e293b; color: #fff; border: none; padding: 8px 20px; border-radius: 6px; cursor: pointer; }
.btn-outline { background: #fff; border: 1px solid #ccc; padding: 8px 20px; border-radius: 6px; cursor: pointer; }
</style>