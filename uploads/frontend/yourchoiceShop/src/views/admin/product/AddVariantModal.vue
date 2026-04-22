<template>
  <div v-if="isOpen" class="modal-overlay">
    <div class="modal-content">
      <div class="modal-header">
        <h3>Thêm biến thể mới</h3>
        <button class="close-btn" @click="$emit('close')">&times;</button>
      </div>

      <div class="modal-body">
        <div class="form-grid">
          <div class="form-group">
            <label>Màu sắc <span class="required">*</span></label>
            <select v-model="form.idMauSac" class="form-select" :class="{ invalid: errors.idMauSac }">
              <option :value="null">Chọn màu sắc</option>
              <option v-for="item in options.mauSac" :key="item.id" :value="item.id">{{ item.tenMauSac }}</option>
            </select>
            <span v-if="errors.idMauSac" class="error-msg">{{ errors.idMauSac }}</span>
          </div>

          <div class="form-group">
            <label>Kích thước <span class="required">*</span></label>
            <select v-model="form.idKichThuoc" class="form-select" :class="{ invalid: errors.idKichThuoc }">
              <option :value="null">Chọn kích thước</option>
              <option v-for="item in options.kichThuoc" :key="item.id" :value="item.id">{{ item.tenKichThuoc }}</option>
            </select>
            <span v-if="errors.idKichThuoc" class="error-msg">{{ errors.idKichThuoc }}</span>
          </div>

          <div class="form-group">
            <label>Giá nhập</label>
            <input type="number" min="0" v-model.number="form.giaNhap" class="form-input" />
          </div>

          <div class="form-group">
            <label>Giá bán <span class="required">*</span></label>
            <input
              type="number"
              min="0"
              v-model.number="form.giaBan"
              class="form-input"
              :class="{ invalid: errors.giaBan }"
            />
            <span v-if="errors.giaBan" class="error-msg">{{ errors.giaBan }}</span>
          </div>

          <div class="form-group">
            <label>Số lượng <span class="required">*</span></label>
            <input
              type="number"
              min="0"
              v-model.number="form.soLuong"
              class="form-input"
              :class="{ invalid: errors.soLuong }"
            />
            <span v-if="errors.soLuong" class="error-msg">{{ errors.soLuong }}</span>
          </div>
        </div>

        <div class="form-group mt-12">
          <label>Hình ảnh biến thể</label>
          <div class="media-row">
            <div v-for="(imgUrl, idx) in form.listAnh" :key="`${imgUrl}-${idx}`" class="img-box">
              <img :src="normalizeImageUrl(imgUrl)" alt="Variant image" @error="$event.target.style.display='none'" />
              <button class="remove-img" @click="removeImage(idx)">x</button>
            </div>

            <button class="add-img" @click="triggerFileInput" :disabled="uploading">
              <span v-if="uploading">Đang tải...</span>
              <span v-else>+ Ảnh</span>
            </button>

            <input ref="fileInput" type="file" hidden accept="image/*" @change="handleUploadImage" />
          </div>
        </div>
      </div>

      <div class="modal-footer">
        <button class="btn btn-cancel" @click="$emit('close')">
          <i class="fas fa-times icon-cancel"></i>
          Hủy
        </button>
        <button class="btn btn-save" @click="saveVariant" :disabled="saving">
          <i class="fas fa-plus"></i>
          {{ saving ? 'Đang lưu...' : 'Thêm biến thể' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, watch } from 'vue';
import axios from 'axios';
import { toastError, toastSuccess } from '@/utils/toast';

const props = defineProps({
  isOpen: { type: Boolean, default: false },
  productId: { type: [String, Number], required: true },
  options: {
    type: Object,
    default: () => ({
      mauSac: [],
      kichThuoc: []
    })
  }
});

const emit = defineEmits(['close', 'saved']);

const API_URL = 'http://localhost:8080/api/v1';
const IMAGE_BASE_URL = 'http://localhost:8080/images/';

const saving = ref(false);
const uploading = ref(false);
const fileInput = ref(null);

const initialForm = () => ({
  idMauSac: null,
  idKichThuoc: null,
  giaNhap: 0,
  giaBan: 0,
  soLuong: 0,
  listAnh: []
});

const form = reactive(initialForm());
const errors = reactive({
  idMauSac: null,
  idKichThuoc: null,
  giaBan: null,
  soLuong: null
});

watch(
  () => props.isOpen,
  (val) => {
    if (val) {
      Object.assign(form, initialForm());
      errors.idMauSac = null;
      errors.idKichThuoc = null;
      errors.giaBan = null;
      errors.soLuong = null;
    }
  }
);

const normalizeImageUrl = (url) => {
  if (!url) return '';
  if (url.startsWith('http://') || url.startsWith('https://')) return url;
  return `${IMAGE_BASE_URL}${url.replace(/^\//, '')}`;
};

const validateForm = () => {
  errors.idMauSac = form.idMauSac ? null : 'Vui lòng chọn màu sắc';
  errors.idKichThuoc = form.idKichThuoc ? null : 'Vui lòng chọn kích thước';
  errors.giaBan = form.giaBan == null || Number(form.giaBan) < 0 ? 'Giá bán không hợp lệ' : null;
  errors.soLuong = form.soLuong == null || Number(form.soLuong) < 0 ? 'Số lượng không hợp lệ' : null;

  return !errors.idMauSac && !errors.idKichThuoc && !errors.giaBan && !errors.soLuong;
};

const triggerFileInput = () => {
  if (!uploading.value) fileInput.value?.click();
};

const handleUploadImage = async (event) => {
  const file = event.target.files?.[0];
  if (!file) return;

  uploading.value = true;
  try {
    const formData = new FormData();
    formData.append('file', file);
    const res = await axios.post(`${API_URL}/upload`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    });
    const fileName = res.data?.url;
    if (fileName) {
      form.listAnh.push(fileName);
    }
  } catch (e) {
    toastError('Không thể tải ảnh lên');
  } finally {
    uploading.value = false;
    event.target.value = '';
  }
};

const removeImage = (index) => {
  form.listAnh.splice(index, 1);
};

const saveVariant = async () => {
  if (!validateForm()) {
    toastError('Vui lòng kiểm tra lại dữ liệu biến thể');
    return;
  }

  saving.value = true;
  try {
    const payload = {
      idMauSac: form.idMauSac,
      idKichThuoc: form.idKichThuoc,
      giaNhap: Number(form.giaNhap || 0),
      giaBan: Number(form.giaBan || 0),
      soLuong: Number(form.soLuong || 0),
      listAnh: [...form.listAnh]
    };

    const res = await axios.post(`${API_URL}/products/${props.productId}/variants`, payload);
    toastSuccess('Thêm biến thể thành công');
    emit('saved', res.data);
    emit('close');
  } catch (e) {
    const message = e?.response?.data?.message || e?.response?.data || 'Không thể thêm biến thể';
    toastError(String(message));
  } finally {
    saving.value = false;
  }
};
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.55);
  z-index: 1000;
  display: flex;
  justify-content: center;
  align-items: center;
}

.modal-content {
  width: 760px;
  max-height: 90vh;
  overflow-y: auto;
  background: #fff;
  border-radius: 10px;
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.18);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #e2e8f0;
}

.modal-header h3 {
  margin: 0;
  color: #0f172a;
  font-size: 20px;
}

.close-btn {
  border: none;
  background: transparent;
  font-size: 24px;
  cursor: pointer;
  color: #64748b;
}

.modal-body {
  padding: 20px;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.form-group label {
  color: #0f172a;
  font-size: 13px;
  font-weight: 600;
}

.required {
  color: #dc2626;
}

.form-input,
.form-select {
  height: 38px;
  border: 1px solid #cbd5e1;
  border-radius: 6px;
  padding: 0 10px;
  outline: none;
  color: #334155;
}

.form-input:focus,
.form-select:focus {
  border-color: #2563eb;
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.12);
}

.invalid {
  border-color: #dc2626;
}

.error-msg {
  color: #dc2626;
  font-size: 12px;
}

.mt-12 {
  margin-top: 12px;
}

.media-row {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.img-box {
  width: 82px;
  height: 82px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  overflow: hidden;
  position: relative;
  background: #f8fafc;
}

.img-box img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.remove-img {
  position: absolute;
  top: 0;
  right: 0;
  width: 20px;
  height: 20px;
  border: none;
  background: rgba(0, 0, 0, 0.6);
  color: #fff;
  cursor: pointer;
}

.add-img {
  width: 82px;
  height: 82px;
  border: 1px dashed #94a3b8;
  border-radius: 8px;
  background: #fff;
  color: #0f172a;
  font-size: 13px;
  cursor: pointer;
}

.add-img:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.modal-footer {
  display: flex;
  justify-content: center;
  gap: 10px;
  padding: 16px 20px;
  border-top: 1px solid #e2e8f0;
}

.btn {
  min-width: 120px;
  height: 38px;
  border-radius: 6px;
  border: none;
  font-weight: 600;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.btn-cancel {
  background: #fff;
  border: 1px solid #94a3b8;
  color: #334155;
}

.icon-cancel {
  color: #dc2626;
}

.btn-save {
  background: linear-gradient(135deg, #1e3a8a 0%, #0f172a 100%); 
  color: #fff;
}

.btn-save:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}
</style>
