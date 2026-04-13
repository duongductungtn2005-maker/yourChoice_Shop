<template>
  <div v-if="isOpen" class="modal-overlay">
    <div class="modal-content">
      <div class="modal-header">
        <h3>Quản lý ảnh cho màu: <span class="text-primary">{{ colorName }}</span></h3>
        <button class="close-btn" @click="$emit('close')">×</button>
      </div>

      <div class="modal-body">
        <div class="section-title">
           Ảnh đã chọn ({{ tempSelected.length }}/5)
           <span v-if="tempSelected.length === 0" class="text-hint">Chưa chọn ảnh nào</span>
        </div>
        <div class="selected-strip">
              <div v-for="(img, idx) in tempSelected" :key="idx" class="selected-item">
                  <img :src="normalizeImageUrl(img)">
              <button class="remove-mini" @click="toggleSelect(img)">×</button>
           </div>
        </div>

        <hr class="divider">

        <div class="section-header">
           <div class="section-title">Thư viện ảnh</div>
           <label class="btn-upload">
              <font-awesome-icon v-if="uploading" :icon="['fas', 'spinner']" spin />
              <font-awesome-icon v-else :icon="['fas', 'cloud-arrow-up']" /> 
              {{ uploading ? 'Đang tải...' : 'Tải ảnh mới' }}
              <input type="file" hidden multiple accept="image/*" @change="handleFileUpload" :disabled="uploading">
           </label>
        </div>

        <div class="gallery-grid">
           <div 
              v-for="(img, idx) in galleryImages" 
              :key="idx" 
              class="gallery-item"
              :class="{ 'active': tempSelected.includes(img) }"
              @click="toggleSelect(img)"
           >
                  <img :src="normalizeImageUrl(img)">
              <div class="check-overlay"><font-awesome-icon :icon="['fas', 'check']" /></div>
           </div>
        </div>
      </div>

      <div class="modal-footer">
        <button class="btn btn-outline" @click="$emit('close')">Hủy</button>
        <button class="btn btn-primary" @click="confirm">Xác nhận</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue';
import axios from 'axios';

const props = defineProps(['isOpen', 'colorName', 'currentImages']);
const emit = defineEmits(['close', 'save']);
const API_URL = 'http://localhost:8080/api/v1';
const IMAGE_BASE_URL = 'http://localhost:8080/images/';

const galleryImages = ref([]); // Tất cả ảnh có trên server (hoặc vừa up)
const tempSelected = ref([]);  // Ảnh đang tick chọn
const uploading = ref(false);

const normalizeImageUrl = (img) => {
    if (!img || typeof img !== 'string') return '';
    if (img.startsWith('http://') || img.startsWith('https://') || img.startsWith('data:') || img.startsWith('blob:')) {
        return img;
    }
    return `${IMAGE_BASE_URL}${img.replace(/^\/+/, '')}`;
};

// Khi mở modal -> Load dữ liệu
watch(() => props.isOpen, (newVal) => {
    if (newVal) {
        tempSelected.value = [...props.currentImages];
        // Trong thực tế, bạn nên gọi API lấy tất cả ảnh đã upload trước đó
        // Ở đây mình giả lập giữ lại các ảnh vừa up trong phiên làm việc
        if (galleryImages.value.length === 0) {
            galleryImages.value = [...props.currentImages]; 
        }
    }
});

// Chọn/Bỏ chọn ảnh
const toggleSelect = (img) => {
    if (tempSelected.value.includes(img)) {
        tempSelected.value = tempSelected.value.filter(i => i !== img);
    } else {
        if (tempSelected.value.length >= 5) return alert("Chỉ được chọn tối đa 5 ảnh!");
        tempSelected.value.push(img);
    }
};

// Upload ảnh lên Server
const handleFileUpload = async (event) => {
    const files = event.target.files;
    if (!files || files.length === 0) return;

    uploading.value = true;
    const formData = new FormData();
    for (let i = 0; i < files.length; i++) {
        formData.append('file', files[i]); // Backend cần hỗ trợ nhận file (xem lại controller)
    }

    try {
        // Gọi API Upload (Bạn cần đảm bảo UploadController đã chạy)
        // Nếu API chỉ nhận 1 file, cần loop gọi nhiều lần. 
        // Giả sử API trả về { url: "..." }
        const res = await axios.post(`${API_URL}/upload`, formData, {
            headers: { 'Content-Type': 'multipart/form-data' }
        });
        
        // Giả sử backend trả về url
        const newUrl = res.data.url; 
        galleryImages.value.unshift(newUrl); // Thêm vào đầu thư viện
        toggleSelect(newUrl); // Tự động chọn luôn

    } catch (e) {
        console.error(e);
        // Fallback: Nếu chưa có API, dùng FileReader để demo trên giao diện
        for (let i = 0; i < files.length; i++) {
             const reader = new FileReader();
             reader.onload = (e) => {
                 galleryImages.value.unshift(e.target.result);
                 toggleSelect(e.target.result);
             };
             reader.readAsDataURL(files[i]);
        }
    } finally {
        uploading.value = false;
        event.target.value = ''; // Reset input
    }
};

const confirm = () => {
    emit('save', tempSelected.value);
    emit('close');
};
</script>

<style scoped>
.modal-overlay { position: fixed; top: 0; left: 0; width: 100vw; height: 100vh; background: rgba(0,0,0,0.6); z-index: 2000; display: flex; justify-content: center; align-items: center; }
.modal-content { background: #fff; width: 700px; max-height: 85vh; border-radius: 8px; display: flex; flex-direction: column; overflow: hidden; animation: slideDown 0.3s; }
.modal-header { padding: 15px 20px; border-bottom: 1px solid #eee; display: flex; justify-content: space-between; align-items: center; background: #f8f9fa; }
.modal-header h3 { margin: 0; font-size: 16px; font-weight: 700; }
.close-btn { background: none; border: none; font-size: 24px; cursor: pointer; color: #666; }
.modal-body { padding: 20px; overflow-y: auto; flex: 1; }

.section-title { font-weight: 700; font-size: 14px; margin-bottom: 10px; color: #333; display: flex; justify-content: space-between; }
.text-hint { font-weight: 400; color: #999; font-style: italic; }

/* Selected Strip */
.selected-strip { display: flex; gap: 10px; overflow-x: auto; padding-bottom: 5px; min-height: 80px; }
.selected-item { position: relative; width: 70px; height: 70px; border-radius: 4px; overflow: hidden; border: 1px solid #ddd; flex-shrink: 0; }
.selected-item img { width: 100%; height: 100%; object-fit: cover; }
.remove-mini { position: absolute; top: 0; right: 0; background: rgba(0,0,0,0.5); color: #fff; border: none; width: 20px; height: 20px; cursor: pointer; display: flex; align-items: center; justify-content: center; font-size: 12px; }

.divider { border: 0; border-top: 1px solid #eee; margin: 20px 0; }

.section-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 15px; }
.btn-upload { background: #e3f2fd; color: #1976d2; padding: 6px 12px; border-radius: 4px; font-weight: 600; font-size: 13px; cursor: pointer; transition: 0.2s; }
.btn-upload:hover { background: #bbdefb; }

/* Grid Gallery */
.gallery-grid { display: grid; grid-template-columns: repeat(5, 1fr); gap: 10px; }
.gallery-item { position: relative; width: 100%; padding-top: 100%; border-radius: 6px; overflow: hidden; border: 2px solid transparent; cursor: pointer; background: #f1f1f1; }
.gallery-item img { position: absolute; top: 0; left: 0; width: 100%; height: 100%; object-fit: cover; transition: 0.2s; }
.gallery-item:hover { border-color: #ccc; }
.gallery-item.active { border-color: #1976d2; }
.gallery-item.active img { transform: scale(0.9); }
.check-overlay { position: absolute; top: 5px; right: 5px; background: #1976d2; color: #fff; width: 18px; height: 18px; border-radius: 50%; font-size: 10px; display: none; align-items: center; justify-content: center; }
.gallery-item.active .check-overlay { display: flex; }

.modal-footer { padding: 15px 20px; border-top: 1px solid #eee; display: flex; justify-content: flex-end; gap: 10px; background: #f8f9fa; }
.btn { padding: 8px 20px; border-radius: 4px; font-weight: 600; cursor: pointer; border: 1px solid transparent; }
.btn-outline { background: #fff; border-color: #ddd; color: #333; }
.btn-primary { background: #1976d2; color: #fff; }

@keyframes slideDown { from { opacity: 0; transform: translateY(-20px); } to { opacity: 1; transform: translateY(0); } }
</style>