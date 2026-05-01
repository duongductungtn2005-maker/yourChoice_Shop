<template>
  <div class="create-product-page">
   
    <div class="header-section">
      <h1 class="page-title">Quản lý sản phẩm / Danh sách sản phẩm</h1>
      
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
             <QuickAddSelect
               v-model="product.idThuongHieu"
               :options="attributes.thuongHieu"
               label-key="tenThuongHieu"
               label="Thương hiệu"
               placeholder="Tìm hoặc chọn thương hiệu..."
               :api-url="`${API_URL}/thuong-hieu`"
               @item-added="(item) => attributes.thuongHieu.push(item)"
             />
           </div>
           <div class="form-group">
             <label class="required">Chất liệu</label>
             <QuickAddSelect
               v-model="product.idChatLieu"
               :options="attributes.chatLieu"
               label-key="tenChatLieu"
               label="Chất liệu"
               placeholder="Tìm hoặc chọn chất liệu..."
               :api-url="`${API_URL}/chat-lieu`"
               @item-added="(item) => attributes.chatLieu.push(item)"
             />
           </div>
           <div class="form-group">
              <label class="required">Cổ áo</label>
              <QuickAddSelect
                v-model="product.idCoAo"
                :options="attributes.coAo"
                label-key="tenCoAo"
                label="Cổ áo"
                placeholder="Tìm hoặc chọn cổ áo..."
                :api-url="`${API_URL}/co-ao`"
                @item-added="(item) => attributes.coAo.push(item)"
              />
            </div>
            <div class="form-group">
               <label class="required">Tay áo</label>
               <QuickAddSelect
                 v-model="product.idTayAo"
                 :options="attributes.tayAo"
                 label-key="tenTayAo"
                 label="Tay áo"
                 placeholder="Tìm hoặc chọn tay áo..."
                 :api-url="`${API_URL}/tay-ao`"
                 @item-added="(item) => attributes.tayAo.push(item)"
               />
            </div>
           <div class="form-group">
             <label class="required">Xuất xứ</label>
             <QuickAddSelect
               v-model="product.idXuatXu"
               :options="attributes.xuatXu"
               label-key="tenXuatXu"
               label="Xuất xứ"
               placeholder="Tìm hoặc chọn xuất xứ..."
               :api-url="`${API_URL}/xuat-xu`"
               @item-added="(item) => attributes.xuatXu.push(item)"
             />
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
           
           <div class="attr-row-clean">
             <div class="attr-label">Màu sắc: <span class="required">*</span></div>
             <div class="selected-list">
               <div v-for="(color, index) in selectedColors" :key="color.id" class="selected-tag" :style="{ backgroundColor: getColorCode(color.tenMauSac) }">
                  <span class="tag-text" :class="{ 'light-text': isDarkColor(getColorCode(color.tenMauSac)) }">{{ color.tenMauSac }}</span>
                  <span class="remove-x" @click="removeColor(index)">×</span>
               </div>
               <button class="btn-add-mini" @click="openAttrModal('color')">+</button>
             </div>
           </div>

           <div class="attr-row-clean">
             <div class="attr-label">Kích cỡ: <span class="required">*</span></div>
             <div class="selected-list">
               <div v-for="(size, index) in selectedSizes" :key="size.id" class="selected-tag size-tag">
                  {{ size.tenKichThuoc }}
                  <span class="remove-x" @click="removeSize(index)">×</span>
               </div>
               <button class="btn-add-mini" @click="openAttrModal('size')">+</button>
             </div>
           </div>

         </div>
       </div>

       <div v-if="generatedVariants.length > 0" class="variants-section">
         
         <div v-for="color in selectedColors" :key="color.id" class="variant-group-card">
             
             <div class="group-header">
                <div class="group-title">
                   <span class="color-indicator" :style="{ backgroundColor: getColorCode(color.tenMauSac) }"></span>
                   Danh sách sản phẩm màu: <strong>{{ color.tenMauSac }}</strong>
                </div>
                <div class="group-actions">
                   <button class="btn-restore" @click="resetVariantGroup(color.id)" title="Khôi phục mặc định">
                      <font-awesome-icon :icon="['fas', 'rotate-left']" /> Khôi phục
                   </button>

                   <button 
                      class="btn-bulk-edit-large" 
                      :class="{ 'btn-active': countSelectedInGroup(color.id) > 0 }"
                      :disabled="countSelectedInGroup(color.id) === 0"
                      @click="openBulkEditModal(color)"
                   >
                      <font-awesome-icon :icon="['fas', 'pen-to-square']" /> 
                      Sửa nhanh ({{ countSelectedInGroup(color.id) }})
                   </button>
                </div>
             </div>

             <div class="group-body-flex">
                
                <div class="col-left-table">
                    <table class="custom-table">
                       <thead>
                          <tr>
                             <th width="40" class="text-center">
                                <input 
                                    type="checkbox" 
                                    :checked="isGroupAllSelected(color.id)"
                                    @change="toggleGroupSelection(color.id, $event)"
                                >
                             </th>
                             <th>Kích cỡ</th>
                             <th width="110">Số lượng</th>
                             <th width="140">Giá nhập (VNĐ)</th>
                             <th width="140">Giá bán (VNĐ)</th>
                             <th width="60" class="text-center">Xóa</th> </tr>
                       </thead>
                       <tbody>
                          <tr v-for="variant in getVariantsByColor(color.id)" :key="variant.key">
                             <td class="text-center"><input type="checkbox" v-model="variant.isSelected"></td>
                             <td>
                                <span class="size-badge">{{ variant.tenKichThuoc }}</span>
                             </td>
                             <td>
                                <input type="number" v-model="variant.soLuong" class="form-control-sm text-center">
                             </td>
                             <td>
                                <input type="number" v-model="variant.giaNhap" class="form-control-sm text-right">
                             </td>
                             <td>
                                <input type="number" v-model="variant.giaBan" class="form-control-sm text-right text-price">
                             </td>
                             <td class="text-center">
                                <button class="btn-icon-trash" @click="removeVariantByKey(variant.key)" title="Xóa dòng này">
                                    <font-awesome-icon :icon="['fas', 'trash']" />
                                </button>
                             </td>
                          </tr>
                       </tbody>
                    </table>
                </div>

                <div class="col-right-images">
                    <div class="img-header-row">
                        <span>Ảnh sản phẩm</span>
                        <span class="img-count-badge">{{ (groupImages[color.id] || []).length }} ảnh</span>
                    </div>

                    <div class="image-content-wrap">
                        <div class="image-upload-area">
                            <div v-if="!groupImages[color.id] || groupImages[color.id].length === 0" 
                                 class="upload-placeholder" 
                                 @click="openGalleryModal(color)">
                                <div class="icon-box-large">
                                    <font-awesome-icon :icon="['fas', 'cloud-arrow-up']" />
                                </div>
                                <span class="upload-text">Chọn ảnh</span>
                            </div>

                            <div v-else class="image-gallery-container">
                                <div class="gallery-grid">
                                    <div v-for="(img, idx) in groupImages[color.id]" :key="idx" class="img-thumbnail">
                                        <img :src="getPreviewUrl(img)" alt="Product Image" @error="handlePreviewError">
                                    </div>
                                    <div class="add-image-tile" @click="openGalleryModal(color)">
                                        <div class="tile-icon">
                                            <font-awesome-icon :icon="['fas', 'images']" />
                                        </div>
                                        <span class="tile-text">Quản lý</span>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

             </div> 
          </div> 
       </div>

    </div>

    <div class="bottom-action-bar">
       <button class="btn btn-outline" @click="$router.go(-1)">Hủy bỏ</button>
       <button class="btn btn-primary" @click="submitProduct" :disabled="loading">
          <font-awesome-icon v-if="loading" :icon="['fas', 'spinner']" spin /> 
          {{ loading ? ' Đang xử lý...' : 'Hoàn tất' }}
       </button>
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
                     <div class="form-col"><label>Giá nhập</label><input type="text" inputmode="numeric" v-model="bulkForm.giaNhap" class="form-control" @input="bulkForm.giaNhap = formatMoneyInput(bulkForm.giaNhap)"></div>
                     <div class="form-col"><label>Giá bán</label><input type="text" inputmode="numeric" v-model="bulkForm.giaBan" class="form-control" @input="bulkForm.giaBan = formatMoneyInput(bulkForm.giaBan)"></div>
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
        :color-id="galleryColorId" 
        @close="showGalleryModal = false"
        @save="handleGallerySave"
    />

    <AttributeSelectionModal 
      :is-open="showAttrModal"
      :title="attrModalType === 'color' ? 'Màu sắc' : 'Kích cỡ'"
      :options="attrModalType === 'color' ? attributes.mauSac : attributes.kichThuoc"
      :selected="attrModalType === 'color' ? selectedColors : selectedSizes"
      :type="attrModalType"
      @close="showAttrModal = false"
      @confirm="handleAttrConfirm"
    />

  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import axios from 'axios';
import { useRouter } from 'vue-router';
import Swal from 'sweetalert2';
import ImageGalleryModal from './ImageGalleryModal.vue';
import AttributeSelectionModal from './AttributeSelectionModal.vue';
import QuickAddSelect from './QuickAddSelect.vue';
import { toastSuccess, toastError, Toast } from '@/utils/toast';

const router = useRouter();
const loading = ref(false);
const API_URL = 'http://localhost:8080/api/v1';
const IMAGE_BASE_URL = 'http://localhost:8080/images/';

// STATE
const product = reactive({ tenSanPham: '', moTa: '', idThuongHieu: null, idChatLieu: null, idXuatXu: null, idCoAo: null, idTayAo: null });
const attributes = reactive({ thuongHieu: [], chatLieu: [], xuatXu: [], coAo: [], tayAo: [], mauSac: [], kichThuoc: [] });
const selectedColors = ref([]);
const selectedSizes = ref([]);
const generatedVariants = ref([]);
const deletedVariants = ref([]); 

// State cho Ảnh
const groupImages = reactive({}); 

// Modal States
const showGalleryModal = ref(false);
const galleryColorId = ref(null);
const galleryColorName = ref('');
const currentEditingImages = ref([]);

const showBulkModal = ref(false);
const editingColor = ref(null);
const bulkForm = reactive({ soLuong: null, giaNhap: '', giaBan: '' });

const showAttrModal = ref(false);
const attrModalType = ref('color');

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
                key: key, 
                isSelected: false, 
                tempId: `${prefix}-${color.maMauSac}-${size.maKichThuoc}`,
                idMauSac: color.id, tenMauSac: color.tenMauSac, idKichThuoc: size.id, tenKichThuoc: size.tenKichThuoc,
                soLuong: 10, giaNhap: 100000, giaBan: 200000, trangThai: 1
            });
        });
    });
    generatedVariants.value = newVariants;
};

// Checkbox logic
const isGroupAllSelected = (colorId) => {
    const group = getVariantsByColor(colorId);
    return group.length > 0 && group.every(v => v.isSelected);
};

const toggleGroupSelection = (colorId, event) => {
    const isChecked = event.target.checked;
    generatedVariants.value.forEach(v => {
        if (v.idMauSac === colorId) {
            v.isSelected = isChecked;
        }
    });
};

// ATTRIBUTE MODAL
const openAttrModal = (type) => {
  attrModalType.value = type;
  showAttrModal.value = true;
};
const handleAttrConfirm = (selectedItems) => {
  if (attrModalType.value === 'color') selectedColors.value = selectedItems;
  else selectedSizes.value = selectedItems;
  showAttrModal.value = false;
  generateVariants();
};
const removeColor = (index) => { selectedColors.value.splice(index, 1); generateVariants(); };
const removeSize = (index) => { selectedSizes.value.splice(index, 1); generateVariants(); };

// HELPER COLORS
const getColorCode = (name) => {
    const map = { 'Đỏ': '#ef4444', 'Xanh': '#3b82f6', 'Vàng': '#eab308', 'Đen': '#000', 'Trắng': '#ffffff', 'Tím': '#a855f7', 'Xám': '#6b7280', 'Cam': '#f97316', 'Hồng': '#ec4899' };
    for(let k in map) if(name && name.includes(k)) return map[k];
    return '#e2e8f0';
};
const isDarkColor = (hex) => hex === '#000' || (hex !== '#eab308' && hex !== '#ffffff');

// VARIANT METHODS
const getVariantsByColor = (colorId) => generatedVariants.value.filter(v => v.idMauSac === colorId);

const removeVariantByKey = (key) => { 
    const idx = generatedVariants.value.findIndex(v => v.key === key); 
    if (idx !== -1) {
        const deletedItem = generatedVariants.value[idx];
        deletedVariants.value.push(deletedItem); 
        generatedVariants.value.splice(idx, 1); 
    }
};

const resetVariantGroup = async (colorId) => {
    const result = await Swal.fire({
        title: 'Khôi phục mặc định?',
        text: "Hành động này sẽ khôi phục lại các dòng đã xóa và đặt lại giá/số lượng.",
        icon: 'warning',
        showCancelButton: true,
        confirmButtonText: 'Đồng ý',
        cancelButtonText: 'Hủy'
    });

    if (!result.isConfirmed) return;

    const color = selectedColors.value.find(c => c.id === colorId);
    if (!color) return;

    generatedVariants.value = generatedVariants.value.filter(v => v.idMauSac !== colorId);
    deletedVariants.value = deletedVariants.value.filter(v => v.idMauSac !== colorId);

    const prefix = 'SP' + Math.floor(Date.now() / 1000).toString().slice(-4); 
    const newItems = selectedSizes.value.map(size => ({
        key: `${color.id}-${size.id}`, 
        isSelected: false, 
        tempId: `${prefix}-${color.maMauSac}-${size.maKichThuoc}`,
        idMauSac: color.id, tenMauSac: color.tenMauSac, 
        idKichThuoc: size.id, tenKichThuoc: size.tenKichThuoc,
        soLuong: 10, giaNhap: 100000, giaBan: 200000, trangThai: 1
    }));

    generatedVariants.value.push(...newItems);
    generatedVariants.value.sort((a, b) => {
        if (a.idMauSac !== b.idMauSac) return a.idMauSac - b.idMauSac;
        return a.idKichThuoc - b.idKichThuoc;
    });

    toastSuccess('Đã khôi phục dữ liệu nhóm!');
};

// IMAGE GALLERY LOGIC
const openGalleryModal = (color) => {
    galleryColorId.value = color.id;
    galleryColorName.value = color.tenMauSac;
    currentEditingImages.value = groupImages[color.id] ? [...groupImages[color.id]] : [];
    showGalleryModal.value = true;
};

const handleGallerySave = (selectedImages) => {
    groupImages[galleryColorId.value] = [...selectedImages]; 
    showGalleryModal.value = false;
};

const getPreviewUrl = (imgObject) => {
    if (typeof imgObject === 'string') {
        if (!imgObject) return '';
        if (imgObject.startsWith('http://') || imgObject.startsWith('https://') || imgObject.startsWith('data:') || imgObject.startsWith('blob:')) {
            return imgObject;
        }
        return `${IMAGE_BASE_URL}${imgObject.replace(/^\/+/, '')}`;
    }
    if (imgObject && imgObject.dataURL) return imgObject.dataURL;
    if (imgObject && imgObject.url) {
        if (imgObject.url.startsWith('http://') || imgObject.url.startsWith('https://') || imgObject.url.startsWith('data:') || imgObject.url.startsWith('blob:')) {
            return imgObject.url;
        }
        return `${IMAGE_BASE_URL}${imgObject.url.replace(/^\/+/, '')}`;
    }
    return '';
};

const handlePreviewError = (event) => {
    // Use a clean inline fallback so broken URL never renders as a browser error icon.
    event.target.src = "data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='280' height='280' viewBox='0 0 280 280'%3E%3Crect width='280' height='280' fill='%23eef2ff'/%3E%3Cg fill='none' stroke='%2364748b' stroke-width='10'%3E%3Crect x='52' y='66' width='176' height='148' rx='14'/%3E%3Cpath d='M80 182l34-36 30 28 28-24 28 32'/%3E%3Ccircle cx='182' cy='110' r='12'/%3E%3C/g%3E%3Ctext x='140' y='244' text-anchor='middle' fill='%2364748b' font-family='Arial' font-size='18'%3EImage unavailable%3C/text%3E%3C/svg%3E";
};

const formatMoneyInput = (value) => {
    const digits = String(value ?? '').replace(/\D/g, '');
    if (!digits) return '';
    return digits.replace(/\B(?=(\d{3})+(?!\d))/g, '.');
};

const parseMoneyInput = (value) => {
    const digits = String(value ?? '').replace(/\D/g, '');
    return digits ? Number(digits) : null;
};

// BULK EDIT
const countSelectedInGroup = (colorId) => generatedVariants.value.filter(v => v.idMauSac === colorId && v.isSelected).length;
const openBulkEditModal = (color) => {
    if (countSelectedInGroup(color.id) === 0) return Toast.fire({ icon: 'info', title: 'Vui lòng chọn ít nhất 1 dòng để sửa' });
    editingColor.value = color;
    bulkForm.soLuong = null; bulkForm.giaNhap = ''; bulkForm.giaBan = '';
    showBulkModal.value = true;
};
const applyBulkEdit = () => {
    if (!editingColor.value) return;
    const giaNhap = parseMoneyInput(bulkForm.giaNhap);
    const giaBan = parseMoneyInput(bulkForm.giaBan);
    generatedVariants.value.forEach(v => {
        if (v.idMauSac === editingColor.value.id && v.isSelected) {
            if (bulkForm.soLuong !== null && bulkForm.soLuong !== '') v.soLuong = Number(bulkForm.soLuong);
            if (giaNhap !== null) v.giaNhap = giaNhap;
            if (giaBan !== null) v.giaBan = giaBan;
        }
    });
    showBulkModal.value = false;
    toastSuccess('Đã cập nhật hàng loạt!');
};

// SUBMIT LOGIC
const submitProduct = async () => {
    if (!product.tenSanPham || !product.idThuongHieu) {
        return Toast.fire({ icon: 'warning', title: 'Thiếu thông tin', text: 'Vui lòng nhập tên sản phẩm và chọn thương hiệu.' });
    }
    if (selectedColors.value.length === 0 || selectedSizes.value.length === 0) {
        return Toast.fire({ icon: 'warning', title: 'Thiếu thuộc tính', text: 'Vui lòng chọn ít nhất 1 màu và 1 kích cỡ.' });
    }
    if (!generatedVariants.value || generatedVariants.value.length === 0) {
        return Toast.fire({ icon: 'warning', title: 'Chưa có biến thể', text: 'Danh sách phân loại hàng đang trống.' });
    }

    const result = await Swal.fire({ 
        title: 'Xác nhận tạo sản phẩm?', 
        icon: 'question', 
        showCancelButton: true, 
        confirmButtonText: 'Đồng ý',
        confirmButtonColor: '#0f172a' 
    });
    if (!result.isConfirmed) return;

    loading.value = true;
    try {
        const payload = {
            ...product,
            maSanPham: null,
            variants: generatedVariants.value.map(v => ({
                idMauSac: v.idMauSac, idKichThuoc: v.idKichThuoc,
                soLuong: v.soLuong, giaNhap: v.giaNhap, giaBan: v.giaBan, trangThai: v.trangThai,
                listAnh: groupImages[v.idMauSac] || [] 
            }))
        };
        await axios.post(`${API_URL}/products`, payload);
        toastSuccess('Thêm sản phẩm thành công');
        router.push('/admin/products');
    } catch (e) {
        console.error(e);
        toastError(e.response?.data?.message || 'Có lỗi xảy ra khi tạo sản phẩm');
    } finally {
        loading.value = false;
    }
};

onMounted(() => fetchAttributes());
</script>

<style scoped>
/* GENERAL */
.create-product-page { 
    font-family: 'Segoe UI', sans-serif; 
    color: #334155; 
    padding-bottom: 100px; /* Chừa chỗ cho thanh action cố định */ 
    background-color: #ebecee; 
    min-height: 100vh; 
    padding: 20px; 
}

.header-section { 
    display: flex; 
    justify-content: space-between; 
    align-items: center; 
    margin-bottom: 20px; 
}

.header-section 
.page-title { margin: 0; font-size: 24px; font-weight: 700; color: #1e293b; }
.text-gray { color: #64748b; }
.text-bold { font-weight: 700; color: #0f172a; } 
.separator { margin: 0 8px; color: #cbd5e1; }
.cursor-pointer { cursor: pointer; }

/* === CARD STYLE === */
.card { 
    background: #fff; 
    border-radius: 16px; 
    border: 1px solid #bfdbfe !important; 
    box-shadow: 0 4px 12px rgba(0,0,0,0.05); 
    padding: 24px; 
    margin-bottom: 24px; 
    overflow: visible;
}

.card-header h3 { 
    font-size: 16px; 
    font-weight: 700; 
    color: #0f172a; 
    margin-bottom: 20px; 
    text-transform: uppercase; 
    border-bottom: 1px solid #f1f5f9;
    padding-bottom: 10px;
}

/* FORM ELEMENTS */
.form-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 20px; overflow: visible; } 
.full-width { grid-column: span 2; }

.form-group label { 
    display: block; 
    margin-bottom: 8px; 
    font-weight: 600; 
    font-size: 13px; 
    color: #334155; 
}

.required::after { content: " *"; color: #ef4444; }

.form-control { 
    width: 100%; 
    padding: 10px 12px; 
    border: 1px solid #e2e8f0; 
    border-radius: 6px; 
    outline: none; 
    transition: all 0.2s; 
    font-size: 14px;
}

.form-control:focus { 
    border-color: #3b82f6; 
    box-shadow: 0 0 0 3px rgba(59,130,246,0.1); 
}
.form-control::placeholder {
    color: #000000 !important;
    opacity: 0.5 !important;
    font-weight: 500;
}

/* ATTRIBUTE SECTION */
.attribute-selector { margin-top: 10px; }
.attr-row-clean { display: flex; align-items: flex-start; margin-bottom: 20px; }
.attr-label { width: 100px; font-weight: 600; color: #334155; padding-top: 6px; font-size: 13px; }

.selected-list { flex: 1; display: flex; flex-wrap: wrap; gap: 8px; align-items: center; }

.btn-add-mini { 
    width: 32px; height: 32px; 
    border: 1px dashed #94a3b8; 
    color: #64748b; 
    background: white; 
    border-radius: 6px; 
    font-size: 18px; 
    cursor: pointer; 
    display: flex; justify-content: center; align-items: center; 
    transition: 0.2s; 
}
.btn-add-mini:hover { border-color: #0f172a; color: #0f172a; background: #f1f5f9; }

.selected-tag { 
    display: inline-flex; align-items: center; 
    padding: 6px 12px; 
    border-radius: 6px; 
    border: 1px solid rgba(0,0,0,0.1); 
    font-size: 13px; font-weight: 600; color: #333; 
    box-shadow: 0 1px 2px rgba(0,0,0,0.05);
}
.tag-text { margin-right: 8px; } 
.light-text { color: white; }
.size-tag { background-color: #f8fafc; border: 1px solid #cbd5e1; color: #334155; }

.remove-x { cursor: pointer; opacity: 0.6; font-weight: bold; margin-left: 6px; font-size: 16px; line-height: 1; } 
.remove-x:hover { opacity: 1; color: #ef4444; }

/* VARIANT GROUPS */
.variants-section { margin-top: 10px; display: flex; flex-direction: column; gap: 20px; }

.variant-group-card { 
    border: 1px solid #bfdbfe !important; 
    border-radius: 12px; 
    background: #fff; 
    overflow: hidden; 
    box-shadow: 0 4px 12px rgba(0,0,0,0.03); 
    transition: all 0.2s ease; 
    margin-bottom: 20px; 
}
.variant-group-card:hover { 
    box-shadow: 0 8px 20px rgba(0,0,0,0.06); 
    transform: translateY(-2px); 
    border-color: #3b82f6 !important; 
}

.group-header { 
    background: #eff6ff; 
    padding: 14px 20px; 
    border-bottom: 1px solid #bfdbfe !important; 
    display: flex; 
    justify-content: space-between; 
    align-items: center; 
}

.group-title { font-size: 14px; color: #0f172a; display: flex; align-items: center; font-weight: 700; }
.color-indicator { width: 16px; height: 16px; border-radius: 50%; margin-right: 10px; border: 1px solid #cbd5e1; display: inline-block; }

.group-actions { display: flex; gap: 10px; }

/* BUTTONS IN GROUP HEADER */
.btn-restore { 
    background-color: #fff; 
    color: #3b82f6; 
    border: 1px solid #bfdbfe; 
    padding: 8px 16px; 
    font-size: 13px; 
    font-weight: 600; 
    border-radius: 6px; 
    cursor: pointer;
    transition: all 0.2s;
    display: inline-flex; align-items: center; gap: 6px;
}
.btn-restore:hover { background-color: #eff6ff; border-color: #2563eb; color: #1d4ed8; }

.btn-bulk-edit-large { 
    background-color: #f8fafc; color: #94a3b8; border: 1px solid #e2e8f0;
    padding: 8px 16px; font-size: 13px; font-weight: 600; border-radius: 6px;
    cursor: not-allowed; display: inline-flex; align-items: center; gap: 6px;
}
.btn-bulk-edit-large.btn-active { 
    background: linear-gradient(135deg, #1e3a8a 0%, #0f172a 100%); 
    color: #fff; border: none; cursor: pointer; box-shadow: 0 4px 10px rgba(15, 23, 42, 0.2); 
}
.btn-bulk-edit-large.btn-active:hover { transform: translateY(-1px); box-shadow: 0 6px 15px rgba(15, 23, 42, 0.3); }

/* LAYOUT 2 COLUMNS (TABLE + IMAGE) */
.group-body-flex { display: flex; align-items: flex-start; gap: 14px; padding: 14px; background: #f8fbff; }
.col-left-table { 
    flex: 1; 
    min-width: 0;
    border: 1px solid #dbeafe;
    border-radius: 10px;
    background: #fff;
    padding: 0; 
}
.col-right-images {
    flex: 0 0 300px;
    min-width: 300px;
    max-width: 300px;
    background: #fff;
    border: 1px solid #dbeafe;
    border-radius: 10px;
    box-shadow: 0 4px 10px rgba(59, 130, 246, 0.08);
    display: flex;
    flex-direction: column;
}

/* TABLE VARIANT */
.custom-table { width: 100%; border-collapse: collapse; }
.custom-table th { 
    text-align: left; 
    font-size: 12px; 
    color: #1e40af; /* Chữ xanh đậm */
    font-weight: 700; 
    padding: 12px 16px; 
    border-bottom: 1px solid #bfdbfe !important; 
    background: #f8fafc; 
    white-space: nowrap; 
}
.custom-table td { 
    padding: 10px 16px; 
    vertical-align: middle; 
    border-bottom: 1px solid #f1f5f9; 
}

.form-control-sm { 
    width: 100%; padding: 8px 10px; border: 1px solid #e2e8f0; border-radius: 4px; 
    font-size: 13px; outline: none; transition: 0.2s; 
}
.form-control-sm:focus { border-color: #3b82f6; }

.size-badge { 
    background: #f1f5f9; padding: 4px 10px; border-radius: 4px; 
    font-weight: 600; font-size: 12px; color: #475569; border: 1px solid #e2e8f0; 
}

/* UPDATE: Nút xóa (Thùng rác đỏ) */
.btn-icon-trash { 
    color: #ef4444; 
    background: #fee2e2; 
    border: 1px solid #fecaca; 
    border-radius: 6px; 
    width: 32px; 
    height: 32px; 
    display: flex; 
    align-items: center; 
    justify-content: center; 
    cursor: pointer; 
    transition: 0.2s; 
    margin: 0 auto;
}
.btn-icon-trash:hover { 
    background: #ef4444; 
    color: white; 
    transform: scale(1.1); 
    box-shadow: 0 2px 5px rgba(239, 68, 68, 0.3);
}

/* IMAGE UPLOAD AREA */
.img-header-row { 
    width: 100%; 
    padding: 10px 12px; 
    border-bottom: 1px solid #dbeafe; 
    font-size: 12px; 
    font-weight: 700; 
    color: #1e40af; 
    display: flex;
    align-items: center;
    justify-content: space-between;
    background: #f8fbff; 
    text-transform: uppercase; 
}

.img-count-badge {
    font-size: 11px;
    font-weight: 700;
    color: #1d4ed8;
    background: #dbeafe;
    border-radius: 999px;
    padding: 3px 8px;
    text-transform: none;
}

.image-content-wrap { padding: 12px; display: flex; justify-content: center; }

.image-upload-area {
    width: 100%;
}

.upload-placeholder { 
    width: 100%; min-height: 120px; border: 2px dashed #cbd5e1; border-radius: 10px; 
    display: flex; flex-direction: column; align-items: center; justify-content: center; 
    cursor: pointer; color: #64748b; transition: all 0.2s; background: #f8fafc;
    gap: 8px;
}
.upload-placeholder:hover { border-color: #3b82f6; color: #3b82f6; background: #eff6ff; }

.image-gallery-container { width: 100%; }
.gallery-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 8px; }

.img-thumbnail { 
    width: 100%; aspect-ratio: 1; border-radius: 10px; overflow: hidden; 
    border: 1px solid #dbeafe; position: relative;
    background: #f8fafc;
    box-shadow: inset 0 0 0 1px rgba(255, 255, 255, 0.45);
}
.img-thumbnail img { width: 100%; height: 100%; object-fit: cover; }

.add-image-tile { 
    width: 100%; aspect-ratio: 1; border: 2px dashed #cbd5e1; border-radius: 10px; 
    display: flex; flex-direction: column; align-items: center; justify-content: center; 
    cursor: pointer; color: #64748b; transition: 0.2s;
    background: #f8fafc;
}
.add-image-tile:hover { border-color: #3b82f6; color: #3b82f6; background: #eff6ff; }

/* FIXED BOTTOM BAR */
.bottom-action-bar {
    position: fixed;
    left: calc(260px + 24px); 
    right: 24px;
    bottom: 0;
    background: rgba(253, 253, 253, 0.95);
    backdrop-filter: blur(10px);
    padding: 12px 24px;
    border-top: 1px solid #e2e8f0;
    display: flex; justify-content: flex-end; gap: 12px;
    box-shadow: 0 -4px 20px rgba(0,0,0,0.05);
    z-index: 100;
    border-radius: 12px 12px 0 0;
}

/* BUTTONS */
.btn { 
    padding: 10px 24px; border-radius: 6px; font-weight: 600; cursor: pointer; 
    transition: 0.2s; border: 1px solid transparent; font-size: 14px; 
}
.btn-primary { 
    background: linear-gradient(135deg, #1e3a8a 0%, #0f172a 100%); 
    color: #fff; box-shadow: 0 4px 10px rgba(15, 23, 42, 0.2); 
}
.btn-primary:hover { transform: translateY(-1px); box-shadow: 0 6px 15px rgba(15, 23, 42, 0.3); }
.btn-primary:disabled { background: #cbd5e1; cursor: not-allowed; box-shadow: none; transform: none; }

.btn-outline { background: #fff; border-color: #cbd5e1; color: #475569; }
.btn-outline:hover { background: #f1f5f9; border-color: #94a3b8; color: #0f172a; }

/* MODAL */
.modal-backdrop { position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(15, 23, 42, 0.6); z-index: 999; display: flex; justify-content: center; align-items: center; backdrop-filter: blur(2px); }
.modal-content { background: #fff; width: 500px; border-radius: 12px; overflow: hidden; box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1); animation: slideIn 0.2s ease-out; }
@keyframes slideIn { from { transform: translateY(20px); opacity: 0; } to { transform: translateY(0); opacity: 1; } }

.modal-header { padding: 16px 24px; border-bottom: 1px solid #e2e8f0; display: flex; justify-content: space-between; background: #f8fafc; }
.modal-body { padding: 24px; }
.modal-footer { padding: 16px 24px; border-top: 1px solid #e2e8f0; display: flex; justify-content: flex-end; gap: 12px; background: #f8fafc; }

.text-center { text-align: center; }
.text-right { text-align: right; }

@media (max-width: 1360px) {
    .group-body-flex {
        flex-direction: column;
    }

    .col-right-images {
        width: 100%;
        min-width: 100%;
        max-width: 100%;
    }

    .gallery-grid {
        grid-template-columns: repeat(4, minmax(0, 1fr));
    }
}
</style>