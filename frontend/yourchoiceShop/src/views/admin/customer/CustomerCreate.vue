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
                  <label>Ngày sinh</label>
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
import { ref, reactive } from 'vue';
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
  gioiTinh: true, // Mặc định Nam
  ngaySinh: '',
  trangThai: 1
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
    
    if (form.maKhachHang) formData.append('maKhachHang', form.maKhachHang);
    if (form.ngaySinh) formData.append('ngaySinh', form.ngaySinh);
    if (avatarFile.value) formData.append('avatarFile', avatarFile.value);

    // --- BẠN ĐANG THIẾU DÒNG NÀY (HÃY THÊM VÀO) ---
    await request.post('/khach-hang', formData);
    // -----------------------------------------------

    await Swal.fire({
        icon: 'success',
        title: 'Thành công',
        text: 'Đã thêm khách hàng mới!',
        timer: 1500,
        showConfirmButton: false
    });

    router.push({ name: 'admin-customer-list' });

  } catch (error) {
    console.error(error);
    Swal.fire({
        icon: 'error',
        title: 'Lỗi',
        text: error.response?.data?.message || 'Không thể thêm khách hàng.'
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
</style>