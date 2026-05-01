<template>
  <div v-if="isOpen" class="modal-overlay">
    <div class="modal-content">
      <h2 class="modal-title">Cập nhật chi tiết sản phẩm</h2>

      <div class="modal-body">
        <div class="form-grid">
          <div class="form-col">
             <div class="form-group">
                <label>Mã <span class="text-red">*</span></label>
                <input type="text" v-model="form.maCtsp" class="form-input" disabled>
             </div>
             
             <div class="form-group">
                <label>Cổ áo</label>
                <select v-model="form.idCoAo" class="form-select">
                   <option v-for="i in options.coAo" :value="i.id" :key="i.id">{{ i.tenCoAo }}</option>
                </select>
             </div>
             
             <div class="form-group">
                <label>Tay áo</label>
                <select v-model="form.idTayAo" class="form-select">
                   <option v-for="i in options.tayAo" :value="i.id" :key="i.id">{{ i.tenTayAo }}</option>
                </select>
             </div>
             
             <div class="form-group">
                <label>Xuất xứ</label>
                <select v-model="form.idXuatXu" class="form-select">
                   <option v-for="i in options.xuatXu" :value="i.id" :key="i.id">{{ i.tenXuatXu }}</option>
                </select>
             </div>

             <div class="form-group">
                <label>Giá bán <span class="text-red">*</span></label>
                <input 
                   type="number" 
                   v-model="form.giaBan" 
                   class="form-input"
                   :class="{ 'is-invalid': errors.giaBan }"
                   @blur="validateField('giaBan')"
                   @input="validateField('giaBan')"
                >
                <span v-if="errors.giaBan" class="error-msg">{{ errors.giaBan }}</span>
             </div>
          </div>

          <div class="form-col">
             <div class="form-group">
                <label>Thương hiệu</label>
                <select v-model="form.idThuongHieu" class="form-select">
                   <option v-for="i in options.thuongHieu" :value="i.id" :key="i.id">{{ i.tenThuongHieu }}</option>
                </select>
             </div>
             <div class="form-group">
                <label>Chất liệu</label>
                <select v-model="form.idChatLieu" class="form-select">
                   <option v-for="i in options.chatLieu" :value="i.id" :key="i.id">{{ i.tenChatLieu }}</option>
                </select>
             </div>
             <div class="form-group">
                <label>Kích thước</label>
                <select v-model="form.idKichThuoc" class="form-select">
                   <option v-for="i in options.kichThuoc" :value="i.id" :key="i.id">{{ i.tenKichThuoc }}</option>
                </select>
             </div>
             <div class="form-group">
                <label>Màu sắc</label>
                <select v-model="form.idMauSac" class="form-select">
                   <option v-for="i in options.mauSac" :value="i.id" :key="i.id">{{ i.tenMauSac }}</option>
                </select>
             </div>

             <div class="form-group">
                <label>Số lượng tồn <span class="text-red">*</span></label>
                <input 
                   type="number" 
                   v-model="form.soLuong" 
                   class="form-input"
                   :class="{ 'is-invalid': errors.soLuong }"
                   @blur="validateField('soLuong')"
                   @input="validateField('soLuong')"
                >
                <span v-if="errors.soLuong" class="error-msg">{{ errors.soLuong }}</span>
             </div>
          </div>
        </div>

        <div class="form-group mt-3">
           <label>Mô tả sản phẩm</label>
           <textarea v-model="form.moTa" class="form-area" placeholder="Nhập mô tả sản phẩm"></textarea>
        </div>

        <div class="media-section">
            <label class="label-img">Hình ảnh & QR biến thể:</label>
            <div class="media-row">
                <div class="box-qr">
                    <QrcodeVue 
                        v-if="form.maCtsp" 
                        :value="form.maCtsp" 
                        :size="70" 
                        level="H" 
                    />
                </div>

                <div v-for="(imgUrl, index) in form.listAnh" :key="index" class="box-img">
                    <img :src="normalizeImageUrl(imgUrl)" alt="Product Img" @error="$event.target.src='https://placehold.co/80x80?text=Error'">
                    <span class="remove-img" @click="removeImage(index)">×</span>
                </div>

                <div class="box-add" title="Thêm ảnh mới" @click="triggerFileInput">
                    <font-awesome-icon v-if="uploading" :icon="['fas', 'spinner']" spin />
                    <font-awesome-icon v-else :icon="['fas', 'plus']" />
                </div>
                
                <input type="file" ref="fileInput" hidden accept="image/*" @change="handleUploadImage">
            </div>
            <div v-if="form.listAnh.length === 0" class="no-img-text">Chưa có ảnh (Bấm dấu + để thêm)</div>
        </div>

      </div>

      <div class="modal-footer">
         <button class="btn btn-close" @click="$emit('close')">Đóng</button>
         <button class="btn btn-save" @click="save" :disabled="loading">
            <font-awesome-icon v-if="loading" :icon="['fas', 'spinner']" spin />
            {{ loading ? 'Đang lưu...' : 'Lưu thay đổi' }}
         </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, watch, ref } from 'vue';
import axios from 'axios';
import QrcodeVue from 'qrcode.vue';
import Swal from 'sweetalert2';
// 1. Import Toast Utils
import { validate } from '@/utils/validate';
import { toastSuccess, toastError, Toast } from '@/utils/toast';

const props = defineProps(['isOpen', 'variantData', 'parentData', 'options']);
const emit = defineEmits(['close', 'save']); // Event 'save' dùng để reload lại bảng ở cha
const API_URL = 'http://localhost:8080/api/v1';
const IMAGE_BASE_URL = 'http://localhost:8080/images/';

const fileInput = ref(null);
const uploading = ref(false);
const loading = ref(false); // State loading khi lưu

const form = reactive({
    id: null, maCtsp: '', 
    idKichThuoc: null, idMauSac: null,
    idThuongHieu: null, idChatLieu: null, idCoAo: null, idTayAo: null, idXuatXu: null,
    giaBan: 0, giaNhap: 0, soLuong: 0, trangThai: 1, moTa: '',
    listAnh: [] 
});

// State lỗi
const errors = reactive({
    giaBan: null,
    soLuong: null
});

// Logic Validate
const validateField = (field) => {
    if (field === 'giaBan') {
        errors.giaBan = validate.isRequired(form.giaBan) || validate.isPositiveNumber(form.giaBan);
    }
    if (field === 'soLuong') {
        errors.soLuong = validate.isRequired(form.soLuong) || (Number(form.soLuong) < 0 ? 'Số lượng không được âm' : null);
    }
};

const isFormValid = () => {
    validateField('giaBan');
    validateField('soLuong');
    return !errors.giaBan && !errors.soLuong;
};

// Map dữ liệu khi mở Modal
watch(() => props.isOpen, (newVal) => {
    if (newVal && props.variantData) {
        // Reset lỗi
        errors.giaBan = null;
        errors.soLuong = null;
        loading.value = false;

        const val = props.variantData;
        const parent = props.parentData || {}; 

        form.id = val.id;
        form.maCtsp = val.maCtsp;
        form.giaBan = val.giaBan;
        form.giaNhap = val.giaNhap;
        form.soLuong = val.soLuong;
        form.trangThai = val.trangThai;
        form.idMauSac = val.mauSac?.id;
        form.idKichThuoc = val.kichThuoc?.id;
        
        // Lưu tên file gốc (không normalize), normalize chỉ khi hiển thị
        form.listAnh = val.listAnh ? [...val.listAnh] : [];

        // Map các thuộc tính cha
        const th = props.options.thuongHieu?.find(o => o.tenThuongHieu === parent.tenThuongHieu);
        form.idThuongHieu = th ? th.id : null;
        const cl = props.options.chatLieu?.find(o => o.tenChatLieu === parent.tenChatLieu);
        form.idChatLieu = cl ? cl.id : null;
        const xx = props.options.xuatXu?.find(o => o.tenXuatXu === parent.tenXuatXu);
        form.idXuatXu = xx ? xx.id : null;
        const ca = props.options.coAo?.find(o => o.tenCoAo === parent.tenCoAo);
        form.idCoAo = ca ? ca.id : null;
        const ta = props.options.tayAo?.find(o => o.tenTayAo === parent.tenTayAo);
        form.idTayAo = ta ? ta.id : null;
        form.moTa = parent.moTa || '';
    }
});

// Helper: Chuẩn hóa URL ảnh
const normalizeImageUrl = (url) => {
    if (!url) return '';
    // Nếu đã là URL đầy đủ (http/https), trả về nguyên
    if (url.startsWith('http://') || url.startsWith('https://')) return url;
    // Nếu chỉ là tên file hoặc path tương đối, thêm base URL
    return `${IMAGE_BASE_URL}${url.replace(/^\//, '')}`; // Remove leading slash if any
};

// Upload Ảnh
const triggerFileInput = () => { fileInput.value.click(); };

const handleUploadImage = async (event) => {
    const file = event.target.files[0];
    if (!file) return;

    uploading.value = true;
    const formData = new FormData();
    formData.append('file', file);

    try {
        const res = await axios.post(`${API_URL}/upload`, formData, {
            headers: { 'Content-Type': 'multipart/form-data' }
        });
        // Backend trả về chỉ tên file, không phải full URL
        const fileName = res.data.url;
        form.listAnh.push(fileName);
    } catch (e) {
        console.error("Lỗi upload:", e);
        toastError("Lỗi upload ảnh! Vui lòng thử lại.");
    } finally {
        uploading.value = false;
        event.target.value = '';
    }
};

const removeImage = (index) => { form.listAnh.splice(index, 1); };

// --- HÀM LƯU (CẬP NHẬT) ---
// File: ProductDetailEditModal.vue

const save = async () => { 
    // 1. Check Validate
    if (!isFormValid()) {
        Toast.fire({
            icon: 'warning',
            title: 'Dữ liệu không hợp lệ',
            text: 'Vui lòng kiểm tra lại giá bán và số lượng.'
        });
        return; 
    }

    const confirmResult = await Swal.fire({
        title: 'Xác nhận lưu thay đổi?',
        text: `Bạn có chắc muốn cập nhật "${form.maCtsp}"?`,
        icon: 'question',
        showCancelButton: true,
        confirmButtonText: 'Đồng ý',
        cancelButtonText: 'Hủy',
        customClass: {
            popup: 'swal-rounded',
            confirmButton: 'swal-btn-solid',
            cancelButton: 'swal-btn-outline'
        },
        buttonsStyling: false
    });

    if (!confirmResult.isConfirmed) return;

    loading.value = true;
    try {
        // 2. Gọi API Update - SỬA ENDPOINT ĐỂ XỬ LÝ ẢNH
        await axios.put(`${API_URL}/products/variants/${form.id}`, form);

        // 3. Thông báo thành công
        toastSuccess('Cập nhật thành công!');

        // --- SỬA DÒNG NÀY ---
        // Cũ: emit('save');  <-- Sai: Không gửi dữ liệu về cha
        // Mới: Gửi biến 'form' về để cha biết dòng nào vừa sửa xong
        emit('save', form); 
        
        // Đóng modal
        emit('close');

    } catch (e) {
        console.error(e);
        toastError(e.response?.data?.message || 'Lỗi cập nhật sản phẩm');
    } finally {
        loading.value = false;
    }
};
</script>

<style scoped>
/* CSS */
.modal-overlay { position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.5); z-index: 999; display: flex; justify-content: center; align-items: center; }
.modal-content { background: #fff; width: 850px; padding: 30px; border-radius: 8px; box-shadow: 0 5px 15px rgba(0,0,0,0.3); animation: fadeIn 0.2s; max-height: 90vh; overflow-y: auto; }
.modal-title { text-align: center; font-size: 22px; font-weight: 700; color: #1e293b; margin-bottom: 25px; }
.form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 30px; }
.form-group { margin-bottom: 12px; display: flex; flex-direction: column; }
.form-group label { font-weight: 600; font-size: 13px; margin-bottom: 5px; color: #334155; }
.text-red { color: red; }
.form-input, .form-select { padding: 9px; border: 1px solid #cbd5e1; border-radius: 4px; outline: none; font-size: 14px; width: 100%; transition: 0.2s; }
.form-input:focus, .form-select:focus { border-color: #0f172a; }
.form-input:disabled { background: #f1f5f9; color: #64748b; }
.form-area { padding: 10px; border: 1px solid #cbd5e1; border-radius: 6px; height: 70px; resize: none; width: 100%; outline: none; }
.mt-3 { margin-top: 15px; }
.modal-footer { display: flex; justify-content: center; gap: 15px; margin-top: 25px; border-top: 1px solid #f1f5f9; padding-top: 20px; }
.btn { padding: 9px 25px; border-radius: 6px; font-weight: 600; cursor: pointer; border: none; font-size: 14px; display: inline-flex; align-items: center; gap: 8px; }
.btn-close { background: #fff; border: 1px solid #334155; color: #334155; }
.btn-close:hover { background: #f8fafc; }
.btn-save { background: #0f172a; color: #fff; }
.btn-save:hover { background: #1e293b; }
.btn-save:disabled { background: #94a3b8; cursor: not-allowed; }

/* CSS Media */
.media-section { margin-top: 20px; }
.label-img { font-weight: 600; font-size: 13px; color: #334155; margin-bottom: 8px; display: block; }
.media-row { display: flex; gap: 12px; flex-wrap: wrap; align-items: center; }

/* CSS BOX QR */
.box-qr, .box-img, .box-add { 
    width: 80px; height: 80px; 
    border: 1px dashed #cbd5e1; 
    border-radius: 6px; 
    display: flex; align-items: center; justify-content: center; 
    background: #f8fafc; 
    overflow: hidden; 
    position: relative;
    padding: 2px;
}
.box-qr { font-size: 11px; color: #64748b; text-align: center; border-style: solid; background: #fff; }

.box-img { border-style: solid; border-color: #e2e8f0; }
.box-img img { width: 100%; height: 100%; object-fit: cover; }
.remove-img { position: absolute; top: 0; right: 0; background: rgba(0,0,0,0.6); color: #fff; width: 20px; height: 20px; display: flex; justify-content: center; align-items: center; cursor: pointer; font-size: 12px; }
.remove-img:hover { background: red; }

.box-add { cursor: pointer; color: #94a3b8; font-size: 18px; transition: all 0.2s; }
.box-add:hover { border-color: #2563eb; color: #2563eb; background: #fff; }
.no-img-text { font-size: 13px; color: #94a3b8; font-style: italic; margin-left: 10px; margin-top: 5px; }

/* CSS Validate */
.is-invalid { border-color: #ef4444 !important; background-color: #fef2f2; }
.error-msg { color: #ef4444; font-size: 12px; margin-top: 4px; text-align: left; }

@keyframes fadeIn { from { opacity: 0; transform: translateY(-10px); } to { opacity: 1; transform: translateY(0); } }
</style>