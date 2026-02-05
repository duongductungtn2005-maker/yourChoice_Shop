<template>
  <div class="create-product-page">
   
    <div class="header-section">
      <h1 class="page-title">
        <span class="text-gray cursor-pointer" @click="$router.push('/admin/products')">Quản lý sản phẩm</span>
        <span class="separator">/</span>
        <span class="text-bold">Tạo sản phẩm</span>
      </h1>
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
                   <button 
                      class="btn-restore" 
                      @click="resetVariantGroup(color.id)"
                      title="Khôi phục dòng đã xóa & Đặt lại giá trị mặc định"
                   >
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
                             <th width="40">
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
                             <th width="40"></th>
                          </tr>
                       </thead>
                       <tbody>
                          <tr v-for="variant in getVariantsByColor(color.id)" :key="variant.key">
                             <td><input type="checkbox" v-model="variant.isSelected"></td>
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
                    <div class="img-header-row">Ảnh</div>

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
                                        <img :src="getPreviewUrl(img)" alt="Product Image">
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

const router = useRouter();
const loading = ref(false);
const API_URL = 'http://localhost:8080/api/v1';

// STATE
const product = reactive({ tenSanPham: '', moTa: '', idThuongHieu: null, idChatLieu: null, idXuatXu: null, idCoAo: null, idTayAo: null });
const attributes = reactive({ thuongHieu: [], chatLieu: [], xuatXu: [], coAo: [], tayAo: [], mauSac: [], kichThuoc: [] });
const selectedColors = ref([]);
const selectedSizes = ref([]);
const generatedVariants = ref([]);
const deletedVariants = ref([]); // LƯU CÁC DÒNG ĐÃ XÓA

// State cho Ảnh
const groupImages = reactive({}); 

// Modal States
const showGalleryModal = ref(false);
const galleryColorId = ref(null);
const galleryColorName = ref('');
const currentEditingImages = ref([]);

const showBulkModal = ref(false);
const editingColor = ref(null);
const bulkForm = reactive({ soLuong: null, giaNhap: null, giaBan: null });

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

// VARIANT METHODS (Xóa & Khôi phục)
const getVariantsByColor = (colorId) => generatedVariants.value.filter(v => v.idMauSac === colorId);

// SỬA: Xóa dòng -> Đẩy vào thùng rác (deletedVariants)
const removeVariantByKey = (key) => { 
    const idx = generatedVariants.value.findIndex(v => v.key === key); 
    if (idx !== -1) {
        const deletedItem = generatedVariants.value[idx];
        deletedVariants.value.push(deletedItem); // Lưu lại để khôi phục
        generatedVariants.value.splice(idx, 1); 
    }
};

// MỚI: Khôi phục dòng đã xóa
const restoreVariants = (colorId) => {
    // Tìm các dòng đã xóa của màu này
    const variantsToRestore = deletedVariants.value.filter(v => v.idMauSac === colorId);
    
    if (variantsToRestore.length === 0) return;

    // Lọc ra những dòng chưa tồn tại trong bảng (tránh trùng)
    const currentKeys = new Set(generatedVariants.value.map(v => v.key));
    const validRestores = variantsToRestore.filter(v => !currentKeys.has(v.key));

    // Đẩy lại vào bảng
    generatedVariants.value.push(...validRestores);

    // Xóa khỏi thùng rác
    deletedVariants.value = deletedVariants.value.filter(v => v.idMauSac !== colorId);

    // Sắp xếp lại bảng cho đẹp (Size nhỏ lên trước)
    generatedVariants.value.sort((a, b) => {
        if (a.idMauSac !== b.idMauSac) return a.idMauSac - b.idMauSac;
        return a.idKichThuoc - b.idKichThuoc;
    });
};

const hasDeletedVariants = (colorId) => {
    return deletedVariants.value.some(v => v.idMauSac === colorId);
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
    if (typeof imgObject === 'string') return imgObject;
    if (imgObject && imgObject.dataURL) return imgObject.dataURL;
    if (imgObject && imgObject.url) return imgObject.url;
    return '';
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

// SUBMIT
const submitProduct = async () => {
    // Validate cơ bản
    if (!product.tenSanPham || !product.idThuongHieu) {
        return Swal.fire({ icon: 'warning', title: 'Thiếu thông tin', text: 'Vui lòng nhập tên sản phẩm và chọn thương hiệu.' });
    }
    if (selectedColors.value.length === 0 || selectedSizes.value.length === 0) {
        return Swal.fire({ icon: 'warning', title: 'Thiếu thuộc tính', text: 'Vui lòng chọn ít nhất 1 màu và 1 kích cỡ.' });
    }
    if (!generatedVariants.value || generatedVariants.value.length === 0) {
        return Swal.fire({ icon: 'warning', title: 'Thiếu biến thể', text: 'Vui lòng tạo ít nhất 1 biến thể sản phẩm.' });
    }
    // Kiểm tra từng biến thể
    for (const v of generatedVariants.value) {
        if (!v.giaBan || Number(v.giaBan) <= 0) {
            return Swal.fire({ icon: 'warning', title: 'Giá bán không hợp lệ', text: `Vui lòng nhập giá bán (>0) cho phân loại ${v.tenMauSac || ''} ${v.tenKichThuoc || ''}` });
        }
        if (v.soLuong == null || Number(v.soLuong) < 0) {
            return Swal.fire({ icon: 'warning', title: 'Số lượng không hợp lệ', text: `Vui lòng nhập số lượng >= 0 cho phân loại ${v.tenMauSac || ''} ${v.tenKichThuoc || ''}` });
        }
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
// ... (Các import và state giữ nguyên)

// --- LOGIC MỚI: LÀM MỚI (RESET) NHÓM BIẾN THỂ ---
const resetVariantGroup = async (colorId) => {
    // 1. Hỏi người dùng trước khi reset (vì sẽ mất dữ liệu vừa sửa)
    const result = await Swal.fire({
        title: 'Khôi phục mặc định?',
        text: "Hành động này sẽ khôi phục lại các dòng đã xóa và đặt lại giá/số lượng về mặc định.",
        icon: 'warning',
        showCancelButton: true,
        confirmButtonText: 'Đồng ý',
        cancelButtonText: 'Hủy'
    });

    if (!result.isConfirmed) return;

    // 2. Tìm thông tin màu hiện tại
    const color = selectedColors.value.find(c => c.id === colorId);
    if (!color) return;

    // 3. Xóa sạch các biến thể hiện tại của màu này khỏi danh sách chính
    generatedVariants.value = generatedVariants.value.filter(v => v.idMauSac !== colorId);

    // 4. Xóa sạch trong thùng rác (deletedVariants) của màu này
    deletedVariants.value = deletedVariants.value.filter(v => v.idMauSac !== colorId);

    // 5. Tạo lại mới tinh theo danh sách Size đang chọn
    const prefix = 'SP' + Math.floor(Date.now() / 1000).toString().slice(-4); 
    
    const newItems = selectedSizes.value.map(size => ({
        key: `${color.id}-${size.id}`, 
        isSelected: false, 
        tempId: `${prefix}-${color.maMauSac}-${size.maKichThuoc}`,
        idMauSac: color.id, tenMauSac: color.tenMauSac, 
        idKichThuoc: size.id, tenKichThuoc: size.tenKichThuoc,
        soLuong: 10, giaNhap: 100000, giaBan: 200000, trangThai: 1
    }));

    // 6. Thêm lại vào danh sách chính
    generatedVariants.value.push(...newItems);

    // 7. Sắp xếp lại danh sách (để màu và size không bị lộn xộn)
    generatedVariants.value.sort((a, b) => {
        if (a.idMauSac !== b.idMauSac) return a.idMauSac - b.idMauSac;
        return a.idKichThuoc - b.idKichThuoc;
    });

    Swal.fire({ toast: true, position: 'top-end', icon: 'success', title: 'Đã khôi phục mặc định', showConfirmButton: false, timer: 1000 });
};
onMounted(() => fetchAttributes());
</script>

<style scoped>
/* GENERAL */
.create-product-page { font-family: 'Segoe UI', sans-serif; color: #334155; padding-bottom: 100px; /* chừa chỗ cho thanh action cố định */ background-color: #f8fafc; min-height: 100vh; padding: 20px; }
.header-section { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.header-section .page-title { margin: 0; } /* dùng lại style tiêu đề chung nhưng bỏ margin dưới để gọn */
.text-bold { font-weight: 700; color: #0f172a; } .separator { margin: 0 8px; color: #cbd5e1; }
.btn { padding: 9px 20px; border-radius: 6px; font-weight: 500; cursor: pointer; transition: 0.2s; border: none; display: inline-flex; align-items: center; gap: 8px; }
.btn-primary { background: #0f172a; color: #fff; } .btn-outline { background: #fff; border: 1px solid #cbd5e1; margin-right: 10px; color: #334155; }
.card { background: #fff; border-radius: 8px; box-shadow: 0 1px 2px rgba(0,0,0,0.05); padding: 24px; margin-bottom: 24px; border: 1px solid #e2e8f0; }
.card-header h3 { font-size: 16px; font-weight: 700; color: #0f172a; margin-bottom: 16px; text-transform: uppercase; }
.form-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 20px; } .full-width { grid-column: span 2; }
.form-group label { display: block; margin-bottom: 8px; font-weight: 500; font-size: 13px; color: #475569; }
.required::after { content: " *"; color: #ef4444; }
.form-control { width: 100%; padding: 10px 12px; border: 1px solid #cbd5e1; border-radius: 6px; outline: none; transition: border-color 0.2s; }
.form-control:focus { border-color: #0f172a; }

/* ATTRIBUTE */
.attr-row-clean { display: flex; align-items: center; margin-bottom: 15px; }
.attr-label { width: 100px; font-weight: 500; color: #334155; }
.selected-list { display: flex; flex-wrap: wrap; gap: 8px; align-items: center; }
.btn-add-mini { width: 30px; height: 30px; border: 1px dashed #94a3b8; color: #64748b; background: white; border-radius: 4px; font-size: 18px; cursor: pointer; display: flex; justify-content: center; align-items: center; transition: 0.2s; }
.btn-add-mini:hover { border-color: #0f172a; color: #0f172a; background: #f1f5f9; }
.selected-tag { display: inline-flex; align-items: center; padding: 4px 8px 4px 12px; border-radius: 4px; border: 1px solid rgba(0,0,0,0.05); font-size: 13px; font-weight: 600; color: #333; }
.tag-text { margin-right: 8px; } .light-text { color: white; }
.size-tag { background-color: #f1f5f9; border: 1px solid #cbd5e1; color: #334155; }
.remove-x { cursor: pointer; opacity: 0.6; font-weight: bold; margin-left: 4px; } .remove-x:hover { opacity: 1; }

/* VARIANT LAYOUT */
.variants-section { margin-top: 10px; display: flex; flex-direction: column; gap: 16px; }
.variant-group-card { border: 1px solid #e2e8f0; border-radius: 10px; background: #fff; overflow: hidden; box-shadow: 0 2px 6px rgba(15,23,42,0.05); transition: box-shadow 0.2s ease, transform 0.2s ease; }
.variant-group-card:hover { box-shadow: 0 6px 18px rgba(15,23,42,0.10); transform: translateY(-1px); }
.group-header { background: linear-gradient(90deg, #eff6ff, #f8fafc); padding: 12px 20px; border-bottom: 1px solid #e2e8f0; display: flex; justify-content: space-between; align-items: center; }
.group-title { font-size: 14px; color: #0f172a; display: flex; align-items: center; font-weight: 600; }
.color-indicator { width: 14px; height: 14px; border-radius: 50%; margin-right: 10px; border: 1px solid #cbd5e1; display: inline-block; }
.btn-sm { padding: 6px 12px; font-size: 12px; }
.group-actions { display: flex; gap: 10px; }

/* 2 COLS */
.group-body-flex { display: flex; align-items: stretch; gap: 0; }
.col-left-table { flex: 2; border-right: 1px solid #e2e8f0; padding: 8px 0 8px 0; }
.col-right-images { flex: 1; min-width: 340px ; background-color: #fff; display: flex; flex-direction: column; padding: 8px 0 8px 0; }

/* TABLE */
.custom-table { width: 100%; border-collapse: collapse; table-layout: fixed; }
.custom-table th { text-align: left; font-size: 12px; color: #1E3A8A; font-weight: 700; padding: 10px 14px; border-bottom: 1px solid #e2e8f0; background: #E9F1FB; white-space: nowrap; }
.custom-table td { padding: 10px 14px; vertical-align: middle; border-bottom: 1px solid #f1f5f9; background-color: #fff; }
.form-control-sm { width: 100%; padding: 8px 10px; border: 1px solid #cbd5e1; border-radius: 4px; font-size: 13px; outline: none; transition: border-color 0.2s; }
.form-control-sm:focus { border-color: #0f172a; }
.text-price { color: #0f172a; font-weight: 400; }
.size-badge { background: #f1f5f9; padding: 4px 10px; border-radius: 4px; font-weight: 400; font-size: 12px; color: #475569; border: 1px solid #e2e8f0; }
.btn-icon-trash { color: #94a3b8; background: none; border: none; cursor: pointer; font-size: 14px; transition: color 0.2s; }
.btn-icon-trash:hover { color: #ef4444; }
.text-center { text-align: center; } .text-right { text-align: right; } .font-bold { font-weight: 400; }

/* IMAGE AREA */
.img-header-row { width: 100%; padding: 12px 15px; border-bottom: 1px solid #e2e8f0; font-size: 12px; font-weight: 600; color: #475569; text-align: center; background: #f9fafb; text-transform: uppercase; letter-spacing: 0.03em; }
.image-content-wrap { flex: 1; display: flex; align-items: center; justify-content: center; padding: 16px 20px; }
.image-upload-area { width: 100%; min-height: 120px; display: flex; justify-content: center; align-items: flex-start; flex-direction: column; }
.upload-placeholder { width: 100%; min-height: 120px; border: 2px dashed #cbd5e1; border-radius: 10px; display: flex; flex-direction: column; align-items: center; justify-content: center; cursor: pointer; color: #64748b; transition: all 0.2s; background: #f8fafc; }
.upload-placeholder:hover { border-color: #0f172a; color: #0f172a; background: #f1f5f9; }
.icon-box-large { font-size: 28px; margin-bottom: 8px; color: #94a3b8; }
.upload-text { font-weight: 500; font-size: 14px; margin-bottom: 4px; }
.image-gallery-container { width: 100% }
.gallery-grid { display: flex; flex-wrap: wrap; justify-content: flex-start; gap: 12px; width: 100%; }
.img-thumbnail { width: 96px; height: 96px; border-radius: 8px; overflow: hidden; border: 1px solid #e2e8f0; background: #fff; box-shadow: 0 2px 6px rgba(0,0,0,0.06); position: relative; transition: transform 0.2s, box-shadow 0.2s; }
.img-thumbnail:hover { transform: translateY(-2px); box-shadow: 0 4px 8px rgba(0,0,0,0.1); }
.img-thumbnail img { width: 100%; height: 100%; object-fit: cover; }
.add-image-tile { width: 96px; height: 96px; border: 2px dashed #cbd5e1; border-radius: 8px; display: flex; flex-direction: column; align-items: center; justify-content: center; cursor: pointer; background-color: #fff; color: #64748b; transition: all 0.2s; }
.add-image-tile:hover { border-color: #3b82f6; color: #3b82f6; background-color: #eff6ff; }
.tile-icon { font-size: 24px; margin-bottom: 5px; }
.tile-text { font-size: 12px; font-weight: 400; }

/* BULK & RESTORE BUTTONS */
.btn-bulk-edit-large, .btn-restore { padding: 8px 20px; font-size: 13px; font-weight: 500; border-radius: 6px; border: 1px solid #e2e8f0; transition: all 0.2s ease; display: inline-flex; align-items: center; gap: 8px; cursor: pointer; }

/* Button Sửa nhanh */
.btn-bulk-edit-large { background-color: #f8fafc; color: #94a3b8; cursor: not-allowed; }
.btn-bulk-edit-large.btn-active { background-color: #0f172a; color: #fff; border-color: #0f172a; cursor: pointer; box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1); }
.btn-bulk-edit-large.btn-active:hover { background-color: #1e293b; transform: translateY(-1px); }

/* Button Khôi phục */
/* Button Khôi phục */
.btn-restore { 
    background-color: #fff; 
    color: #3b82f6; /* Màu xanh dương */
    border: 1px solid #bfdbfe; 
    padding: 8px 16px; 
    font-size: 13px; 
    font-weight: 600; 
    border-radius: 6px; 
    cursor: pointer;
    transition: all 0.2s;
    display: inline-flex;
    align-items: center;
    gap: 6px;
    margin-right: 8px;
}

.btn-restore:hover { 
    background-color: #eff6ff; 
    border-color: #2563eb; 
    color: #1d4ed8;
}

/* --- FIXED BOTTOM ACTION BAR --- */
.bottom-action-bar {
    position: fixed;
    left: calc(260px + 24px); /* rộng sidebar + padding content-body */
    right: 24px;
    bottom: 0;
    background: #ffffff;
    padding: 12px 20px;
    border-top: 1px solid #e2e8f0;
    display: flex;
    justify-content: flex-end;
    gap: 12px;
    box-shadow: 0 -4px 12px rgba(15,23,42,0.08);
    z-index: 110;
    border-radius: 10px 10px 0 0;
}

/* Modal */
.modal-backdrop { position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(15, 23, 42, 0.5); z-index: 999; display: flex; justify-content: center; align-items: center; }
.modal-content { background: #fff; width: 500px; border-radius: 12px; overflow: hidden; box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1); }
.modal-header { padding: 15px 20px; border-bottom: 1px solid #e2e8f0; display: flex; justify-content: space-between; background: #f8fafc; }
.modal-header h3 { margin: 0; font-size: 16px; font-weight: 700; color: #0f172a; }
.close-icon { font-size: 20px; color: #94a3b8; cursor: pointer; } .close-icon:hover { color: #ef4444; }
.modal-body { padding: 20px; }
.modal-footer { padding: 15px 20px; border-top: 1px solid #e2e8f0; display: flex; justify-content: flex-end; gap: 10px; background: #f8fafc; }
.alert-info { background: #eff6ff; color: #1e40af; padding: 10px 15px; border-radius: 6px; font-size: 13px; margin-bottom: 20px; border: 1px solid #dbeafe; }
.form-row { display: flex; gap: 15px; margin-bottom: 15px; } .form-col { flex: 1; }
</style>