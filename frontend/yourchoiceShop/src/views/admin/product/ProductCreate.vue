<template>
  <div class="create-product-page">
    
    <div class="header-section">
      <div class="breadcrumb">
        <span class="text-gray cursor-pointer" @click="$router.push('/admin/products')">Quản lý sản phẩm</span>
        <span class="separator">/</span>
        <span class="text-bold">Tạo sản phẩm</span>
      </div>
      <div class="header-actions">
         <button class="btn btn-outline" @click="$router.go(-1)">Hủy bỏ</button>
         <button class="btn btn-primary" @click="submitProduct" :disabled="loading">
            <font-awesome-icon v-if="loading" :icon="['fas', 'spinner']" spin /> 
            {{ loading ? ' Đang xử lý...' : 'Hoàn tất' }}
         </button>
      </div>
    </div>

    <div class="content-container">
       <div class="card section-info">
         <div class="card-header"><h3>Thông tin chung</h3></div>
         <div class="form-grid">
           <div class="form-group full-width">
             <label class="required">Tên sản phẩm</label>
             <input type="text" v-model="product.tenSanPham" class="form-control" placeholder="Nhập tên sản phẩm...">
           </div>
           <div class="form-group">
             <label class="required">Thương hiệu</label>
             <select v-model="product.idThuongHieu" class="form-control">
               <option :value="null">-- Chọn thương hiệu --</option>
               <option v-for="item in attributes.thuongHieu" :key="item.id" :value="item.id">{{ item.tenThuongHieu }}</option>
             </select>
           </div>
           <div class="form-group">
             <label class="required">Chất liệu</label>
             <select v-model="product.idChatLieu" class="form-control">
               <option :value="null">-- Chọn chất liệu --</option>
               <option v-for="item in attributes.chatLieu" :key="item.id" :value="item.id">{{ item.tenChatLieu }}</option>
             </select>
           </div>
           <div class="form-group">
              <label class="required">Cổ áo</label>
              <select v-model="product.idCoAo" class="form-control">
                <option :value="null">-- Chọn cổ áo --</option>
                <option v-for="item in attributes.coAo" :key="item.id" :value="item.id">{{ item.tenCoAo }}</option>
              </select>
            </div>
            <div class="form-group">
                <label class="required">Tay áo</label>
                <select v-model="product.idTayAo" class="form-control">
                  <option :value="null">-- Chọn tay áo --</option>
                  <option v-for="item in attributes.tayAo" :key="item.id" :value="item.id">{{ item.tenTayAo }}</option>
                </select>
            </div>
           <div class="form-group">
             <label class="required">Xuất xứ</label>
             <select v-model="product.idXuatXu" class="form-control">
               <option :value="null">-- Chọn xuất xứ --</option>
               <option v-for="item in attributes.xuatXu" :key="item.id" :value="item.id">{{ item.tenXuatXu }}</option>
             </select>
           </div>
           <div class="form-group full-width">
             <label>Mô tả sản phẩm</label>
             <textarea v-model="product.moTa" class="form-control" rows="3" placeholder="Nhập mô tả chi tiết..."></textarea>
           </div>
         </div>
       </div>

       <div class="card section-attributes">
         <div class="card-header"><h3>Màu sắc & Kích cỡ</h3></div>
         <div class="attribute-selector">
           <div class="attr-row">
             <div class="attr-header">
                <label class="required">Màu sắc:</label>
                <button class="btn-text-add" @click="$router.push('/admin/mau-sac')">+ Quản lý màu</button>
             </div>
             <div class="attr-options">
               <label v-for="color in attributes.mauSac" :key="color.id" :class="['option-tag', { active: selectedColors.some(c => c.id === color.id) }]">
                   <input type="checkbox" :value="color" v-model="selectedColors" @change="generateVariants" hidden>
                   {{ color.tenMauSac }}
               </label>
             </div>
           </div>
           <div class="attr-row">
              <div class="attr-header">
                 <label class="required">Kích cỡ:</label>
                 <button class="btn-text-add" @click="$router.push('/admin/kich-thuoc')">+ Quản lý size</button>
              </div>
               <div class="attr-options">
                 <label v-for="size in attributes.kichThuoc" :key="size.id" :class="['option-tag', { active: selectedSizes.some(s => s.id === size.id) }]">
                     <input type="checkbox" :value="size" v-model="selectedSizes" @change="generateVariants" hidden>
                     {{ size.tenKichThuoc }}
                 </label>
               </div>
             </div>
         </div>
       </div>

       <div v-if="generatedVariants.length > 0" class="variants-container">
          
          <div v-for="color in selectedColors" :key="color.id" class="variant-group-card">
             <div class="group-header">
                <div class="group-title">
                   <span class="color-indicator" :style="{ backgroundColor: getColorCode(color.tenMauSac) }"></span>
                   Màu: <strong>{{ color.tenMauSac }}</strong>
                </div>
                <div class="group-actions">
                   <div class="upload-group-box" @click="openGalleryModal(color)">
                      <div v-if="!groupImages[color.id] || groupImages[color.id].length === 0" class="upload-placeholder">
                          <font-awesome-icon :icon="['fas', 'images']" /> 
                          <span>Chọn ảnh</span>
                      </div>
                      <div v-else class="mini-gallery-preview">
                          <div v-for="(img, i) in groupImages[color.id]" :key="i" class="mini-thumb">
                              <img :src="img">
                          </div>
                          <div class="mini-edit-badge"><font-awesome-icon :icon="['fas', 'pen']" /></div>
                      </div>
                   </div>

                   <button class="btn-bulk-edit" @click="openBulkEditModal(color)">
                      <font-awesome-icon :icon="['fas', 'pen-to-square']" /> Sửa nhanh
                   </button>
                </div>
             </div>

             <div class="table-responsive">
                <table class="group-table">
                   <thead>
                      <tr>
                         <th width="40"><input type="checkbox" checked disabled></th>
                         <th>Size</th>
                         <th width="120">Số lượng</th>
                         <th width="150">Giá nhập</th>
                         <th width="150">Giá bán</th>
                         <th width="50" class="text-center">Xóa</th>
                      </tr>
                   </thead>
                   <tbody>
                      <tr v-for="variant in getVariantsByColor(color.id)" :key="variant.key">
                         <td><input type="checkbox" v-model="variant.isSelected"></td>
                         <td>
                            <span class="size-badge">{{ variant.tenKichThuoc }}</span>
                            <div class="sku-text">{{ variant.tempId }}</div>
                         </td>
                         <td><input type="number" v-model="variant.soLuong" class="form-control-sm text-center"></td>
                         <td><input type="number" v-model="variant.giaNhap" class="form-control-sm text-right"></td>
                         <td><input type="number" v-model="variant.giaBan" class="form-control-sm text-right font-bold text-orange"></td>
                         <td class="text-center"><button class="btn-icon-trash" @click="removeVariantByKey(variant.key)">×</button></td>
                      </tr>
                   </tbody>
                </table>
             </div>
          </div>
       </div>

    </div>

    <div v-if="showBulkModal" class="modal-backdrop">
       <div class="modal-content">
          <div class="modal-header">
             <h3>Cập nhật nhanh: Màu {{ editingColor?.tenMauSac }}</h3>
             <span class="close-icon" @click="showBulkModal = false">×</span>
          </div>
          <div class="modal-body">
             <div class="alert-info">Đang áp dụng cho <strong>{{ countSelectedInGroup(editingColor?.id) }}</strong> phân loại.</div>
             <div class="form-row">
                <div class="form-col"><label>Số lượng</label><input type="number" v-model="bulkForm.soLuong" class="form-control"></div>
             </div>
             <div class="form-row">
                <div class="form-col"><label>Giá nhập</label><input type="number" v-model="bulkForm.giaNhap" class="form-control"></div>
                <div class="form-col"><label>Giá bán</label><input type="number" v-model="bulkForm.giaBan" class="form-control"></div>
             </div>
          </div>
          <div class="modal-footer">
             <button class="btn btn-outline" @click="showBulkModal = false">Đóng</button>
             <button class="btn btn-primary" @click="applyBulkEdit">Áp dụng</button>
          </div>
       </div>
    </div>

    <ImageGalleryModal 
        :is-open="showGalleryModal"
        :color-name="galleryColorName"
        :current-images="currentEditingImages"
        @close="showGalleryModal = false"
        @save="handleGallerySave"
    />

  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import axios from 'axios';
import { useRouter } from 'vue-router';
import Swal from 'sweetalert2';
// FIX IMPORT: Import đúng file Modal
import ImageGalleryModal from './ImageGalleryModal.vue';

const router = useRouter();
const loading = ref(false);
const API_URL = 'http://localhost:8080/api/v1';

// STATE
const product = reactive({ tenSanPham: '', moTa: '', idThuongHieu: null, idChatLieu: null, idXuatXu: null, idCoAo: null, idTayAo: null });
const attributes = reactive({ thuongHieu: [], chatLieu: [], xuatXu: [], coAo: [], tayAo: [], mauSac: [], kichThuoc: [] });
const selectedColors = ref([]);
const selectedSizes = ref([]);
const generatedVariants = ref([]);

// State cho Ảnh
const groupImages = reactive({}); // Lưu dạng { colorId: ['url1', 'url2'] }

// State cho Modal Gallery
const showGalleryModal = ref(false);
const galleryColorId = ref(null);
const galleryColorName = ref('');
const currentEditingImages = ref([]);

// State cho Modal Bulk Edit
const showBulkModal = ref(false);
const editingColor = ref(null);
const bulkForm = reactive({ soLuong: null, giaNhap: null, giaBan: null });

// FETCH DATA
const fetchAttributes = async () => {
    try {
        const config = { params: { status: 1, size: 100 } };
        const [th, cl, xx, ca, ta, ms, kt] = await Promise.all([
            axios.get(`${API_URL}/thuong-hieu`, config), axios.get(`${API_URL}/chat-lieu`, config),
            axios.get(`${API_URL}/xuat-xu`, config), axios.get(`${API_URL}/co-ao`, config),
            axios.get(`${API_URL}/tay-ao`, config), axios.get(`${API_URL}/mau-sac`, config),
            axios.get(`${API_URL}/kich-thuoc`, config)
        ]);
        attributes.thuongHieu = th.data.content || th.data;
        attributes.chatLieu = cl.data.content || cl.data;
        attributes.xuatXu = xx.data.content || xx.data;
        attributes.coAo = ca.data.content || ca.data;
        attributes.tayAo = ta.data.content || ta.data;
        attributes.mauSac = ms.data.content || ms.data;
        attributes.kichThuoc = kt.data.content || kt.data;
    } catch (e) { console.error(e); }
};

// CORE LOGIC: BIẾN THỂ
const generateVariants = () => {
    const newVariants = [];
    const prefix = 'SP' + Math.floor(Date.now() / 1000).toString().slice(-4); 
    selectedColors.value.forEach(color => {
        selectedSizes.value.forEach(size => {
            const key = `${color.id}-${size.id}`;
            const existing = generatedVariants.value.find(v => v.key === key);
            if (existing) newVariants.push(existing);
            else newVariants.push({
                key: key, isSelected: true, tempId: `${prefix}-${color.maMauSac}-${size.maKichThuoc}`,
                idMauSac: color.id, tenMauSac: color.tenMauSac, idKichThuoc: size.id, tenKichThuoc: size.tenKichThuoc,
                soLuong: 10, giaNhap: 100000, giaBan: 200000, trangThai: 1
            });
        });
    });
    generatedVariants.value = newVariants;
};
const getVariantsByColor = (colorId) => generatedVariants.value.filter(v => v.idMauSac === colorId);
const removeVariantByKey = (key) => { const idx = generatedVariants.value.findIndex(v => v.key === key); if (idx !== -1) generatedVariants.value.splice(idx, 1); };

// --- LOGIC MỚI: MỞ MODAL GALLERY ---
const openGalleryModal = (color) => {
    galleryColorId.value = color.id;
    galleryColorName.value = color.tenMauSac;
    // Lấy ảnh hiện tại của màu này (nếu có) truyền vào modal
    currentEditingImages.value = groupImages[color.id] || [];
    showGalleryModal.value = true;
};

const handleGallerySave = (selectedImages) => {
    // Lưu lại danh sách ảnh người dùng đã chọn vào state
    groupImages[galleryColorId.value] = selectedImages;
};

// BULK EDIT
const countSelectedInGroup = (colorId) => generatedVariants.value.filter(v => v.idMauSac === colorId && v.isSelected).length;
const openBulkEditModal = (color) => {
    if (countSelectedInGroup(color.id) === 0) return Swal.fire('Chú ý', 'Vui lòng chọn ít nhất 1 dòng', 'info');
    editingColor.value = color;
    bulkForm.soLuong = null; bulkForm.giaNhap = null; bulkForm.giaBan = null;
    showBulkModal.value = true;
};
const applyBulkEdit = () => {
    if (!editingColor.value) return;
    generatedVariants.value.forEach(v => {
        if (v.idMauSac === editingColor.value.id && v.isSelected) {
            if (bulkForm.soLuong) v.soLuong = bulkForm.soLuong;
            if (bulkForm.giaNhap) v.giaNhap = bulkForm.giaNhap;
            if (bulkForm.giaBan) v.giaBan = bulkForm.giaBan;
        }
    });
    showBulkModal.value = false;
    Swal.fire({ toast: true, position: 'top-end', icon: 'success', title: 'Đã cập nhật!', showConfirmButton: false, timer: 1000 });
};

const getColorCode = (name) => {
    const map = { 'Đỏ': '#ef4444', 'Xanh': '#3b82f6', 'Vàng': '#eab308', 'Đen': '#000', 'Trắng': '#e5e7eb', 'Tím': '#a855f7', 'Xám': '#6b7280' };
    for(let k in map) if(name && name.includes(k)) return map[k];
    return '#94a3b8';
};

// SUBMIT
const submitProduct = async () => {
    if (!product.tenSanPham || !product.idThuongHieu || generatedVariants.value.length === 0) {
        return Swal.fire({ toast: true, position: 'top-end', icon: 'warning', title: 'Thiếu thông tin bắt buộc!', showConfirmButton: false, timer: 2000 });
    }
    const result = await Swal.fire({ title: 'Xác nhận?', icon: 'question', showCancelButton: true, confirmButtonText: 'Đồng ý' });
    if (!result.isConfirmed) return;

    loading.value = true;
    try {
        const payload = {
            ...product,
            maSanPham: null,
            variants: generatedVariants.value.map(v => ({
                idMauSac: v.idMauSac, idKichThuoc: v.idKichThuoc,
                soLuong: v.soLuong, giaNhap: v.giaNhap, giaBan: v.giaBan, trangThai: v.trangThai,
                // GỬI KÈM LIST ẢNH
                listAnh: groupImages[v.idMauSac] || [] 
            }))
        };
        await axios.post(`${API_URL}/products`, payload);
        await Swal.fire({ icon: 'success', title: 'Thành công!', confirmButtonColor: '#0f172a' });
        router.push('/admin/products');
    } catch (e) {
        Swal.fire('Lỗi', e.response?.data?.message || 'Có lỗi xảy ra', 'error');
    } finally {
        loading.value = false;
    }
};

onMounted(() => fetchAttributes());
</script>

<style scoped>
/* CSS STYLE GIỮ NGUYÊN NHƯ CŨ, CHỈ THÊM PHẦN ẢNH MỚI */
.create-product-page { font-family: 'Segoe UI', sans-serif; color: #334155; padding-bottom: 60px; }
.header-section { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; }
.text-bold { font-weight: 700; color: #0f172a; } .separator { margin: 0 8px; color: #cbd5e1; }
.btn { padding: 9px 20px; border-radius: 6px; font-weight: 600; cursor: pointer; transition: 0.2s; border: none; }
.btn-primary { background: #0f172a; color: #fff; } .btn-outline { background: #fff; border: 1px solid #cbd5e1; margin-right: 10px; }
.card { background: #fff; border-radius: 8px; box-shadow: 0 1px 3px rgba(0,0,0,0.05); padding: 24px; margin-bottom: 24px; border: 1px solid #e2e8f0; }
.card-header h3 { font-size: 16px; font-weight: 700; color: #0f172a; margin-bottom: 16px; text-transform: uppercase; }
.form-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 20px; } .full-width { grid-column: span 2; }
.form-group label { display: block; margin-bottom: 8px; font-weight: 600; font-size: 13px; color: #475569; }
.required::after { content: " *"; color: #ef4444; }
.form-control { width: 100%; padding: 10px 12px; border: 1px solid #cbd5e1; border-radius: 6px; outline: none; }
.attr-header { display: flex; justify-content: space-between; margin-bottom: 10px; }
.btn-text-add { background: none; border: none; color: #3b82f6; font-weight: 600; cursor: pointer; }
.attr-options { display: flex; flex-wrap: wrap; gap: 10px; }
.option-tag { padding: 8px 16px; border: 1px solid #cbd5e1; border-radius: 6px; cursor: pointer; background: #f8fafc; font-weight: 500; font-size: 13px; }
.option-tag.active { background: #0f172a; color: #fff; border-color: #0f172a; }

/* STYLE CHO KHUNG UPLOAD MỚI */
.upload-group-box { border: 1px dashed #cbd5e1; background: #fff; padding: 5px; border-radius: 6px; cursor: pointer; min-width: 120px; display: flex; align-items: center; justify-content: center; height: 40px; transition: 0.2s; }
.upload-group-box:hover { border-color: #3b82f6; background: #f0f9ff; }
.upload-placeholder { font-size: 13px; color: #64748b; display: flex; gap: 6px; align-items: center; }
.mini-gallery-preview { display: flex; align-items: center; gap: 4px; position: relative; }
.mini-thumb { width: 30px; height: 30px; border-radius: 4px; overflow: hidden; border: 1px solid #eee; }
.mini-thumb img { width: 100%; height: 100%; object-fit: cover; }
.mini-edit-badge { position: absolute; right: -10px; top: -10px; background: #fff; border: 1px solid #ddd; border-radius: 50%; width: 18px; height: 18px; font-size: 10px; display: flex; align-items: center; justify-content: center; color: #333; }

.variant-group-card { border: 1px solid #cbd5e1; border-radius: 8px; margin-bottom: 24px; overflow: hidden; }
.group-header { background: #f1f5f9; padding: 12px 20px; border-bottom: 1px solid #cbd5e1; display: flex; justify-content: space-between; align-items: center; }
.group-title { display: flex; align-items: center; font-weight: 600; color: #334155; }
.color-indicator { width: 12px; height: 12px; border-radius: 50%; margin-right: 8px; border: 1px solid #cbd5e1; }
.group-actions { display: flex; gap: 10px; align-items: center; }
.btn-bulk-edit { background: #fff; border: 1px solid #cbd5e1; color: #0f172a; padding: 6px 12px; border-radius: 6px; font-weight: 600; font-size: 13px; cursor: pointer; }
.table-responsive { width: 100%; overflow-x: auto; }
.group-table { width: 100%; border-collapse: collapse; }
.group-table th { background: #fff; text-align: left; padding: 12px 15px; font-size: 13px; font-weight: 600; color: #64748b; border-bottom: 1px solid #e2e8f0; }
.group-table td { padding: 10px 15px; border-bottom: 1px solid #f1f5f9; vertical-align: middle; }
.form-control-sm { width: 100%; padding: 8px; border: 1px solid #e2e8f0; border-radius: 4px; font-size: 14px; outline: none; }
.size-badge { font-weight: 700; color: #0f172a; font-size: 14px; }
.sku-text { font-size: 11px; color: #94a3b8; }
.text-right { text-align: right; } .text-center { text-align: center; } .text-orange { color: #f97316; }
.btn-icon-trash { background: none; border: none; color: #ef4444; font-size: 20px; cursor: pointer; }

/* MODAL */
.modal-backdrop { position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.5); z-index: 999; display: flex; justify-content: center; align-items: center; }
.modal-content { background: #fff; width: 500px; border-radius: 12px; padding-bottom: 20px; }
.modal-header { padding: 15px 20px; border-bottom: 1px solid #eee; display: flex; justify-content: space-between; }
.modal-body { padding: 20px; }
.form-row { display: flex; gap: 15px; margin-bottom: 15px; } .form-col { flex: 1; }
.modal-footer { padding: 0 20px; display: flex; justify-content: flex-end; gap: 10px; }
</style>