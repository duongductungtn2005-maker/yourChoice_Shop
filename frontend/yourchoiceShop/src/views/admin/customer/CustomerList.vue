<template>
  <div class="page-container">
    <h1 class="page-title">Quản lý tài khoản / Quản lý khách hàng</h1>

    <div class="control-panel">
      <div class="controls-row">
        <div class="filter-group">
          <div class="search-box">
            <i class="fas fa-magnifying-glass search-icon"></i>
            <input 
            class="input-den"
              type="text" 
              v-model="filter.keyword" 
              placeholder="Tìm tên, SĐT, email..." 
              @keyup.enter="fetchData"
            >
          </div>

          <select v-model="filter.gioiTinh" @change="fetchData" class="form-select">
            <option :value="null">-- Giới tính --</option>
            <option :value="true">Nam</option>
            <option :value="false">Nữ</option>
          </select>

          <select v-model="filter.trangThai" @change="fetchData" class="form-select">
            <option :value="null">-- Trạng thái --</option>
            <option :value="1">Hoạt động</option>
            <option :value="0">Ngừng hoạt động</option>
          </select>
        </div>

        <div class="action-group">
          <button class="btn btn-navy" @click="resetFilter">
            <font-awesome-icon :icon="['fas', 'sync-alt']" /> Đặt lại
          </button>

          <button class="btn btn-outline" @click="exportExcel">
            <font-awesome-icon :icon="['fas', 'file-excel']" /> Xuất Excel
          </button>
          
          <button class="btn btn-gradient" @click="$router.push({ name: customerCreateRouteName })">
            <font-awesome-icon :icon="['fas', 'plus']" /> Thêm mới
          </button>
        </div>

      </div>
    </div>

    <div class="table-container">
      <table class="custom-table">
        <thead>
          <tr>
            <th class="text-center" width="5%">STT</th>
            <th width="10%">Mã KH</th>
            <th width="15%">Họ tên</th>
            <th width="12%">Tên tài khoản</th>
            <th width="10%">SĐT</th>
            <th width="15%">Email</th>
            <th width="20%">Địa chỉ</th>
            <th class="text-center" width="8%">Giới tính</th>
            <th class="text-center" width="10%">Trạng thái</th>
            <th class="text-center" width="7%">Thao tác</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading">
            <td colspan="10" class="text-center empty-state">Đang tải dữ liệu...</td>
          </tr>
          <tr v-else-if="items.length === 0">
            <td colspan="10" class="text-center empty-state">Không tìm thấy khách hàng nào.</td>
          </tr>

          <tr v-else v-for="(item, index) in items" :key="item.id">
            <td class="text-center">{{ (page - 1) * pageSize + index + 1 }}</td>
            <td class="code-text">{{ item.maKhachHang }}</td>
            <td class="name-text">{{ item.tenKhachHang }}</td>
            <td>{{ item.tenTaiKhoan || '-' }}</td>
            <td>{{ item.soDienThoai }}</td>
            <td class="text-gray">{{ item.email }}</td>
            
            <td class="text-address">
              <span class="truncate-text" :title="getAddressString(item)">
                {{ getAddressString(item) }}
              </span>
            </td>

            <td class="text-center">{{ item.gioiTinh === true ? 'Nam' : 'Nữ' }}</td>

            <td class="text-center">
              <span class="badge" :class="item.trangThai === 1 ? 'badge-active' : 'badge-stopped'">
                {{ item.trangThai === 1 ? 'Hoạt động' : 'Ngừng' }}
              </span>
            </td>

            <td class="text-center action-col">
              <div class="action-wrapper">
                <button class="icon-btn" @click="viewDetail(item)" title="Xem chi tiết">
                  <i class="far fa-eye"></i>
                </button>

                <button class="icon-btn" @click="openQuickAddressModal(item)" title="Thêm địa chỉ nhanh">
                  <i class="fas fa-map-marker-alt"></i>
                </button>

                <label class="switch" title="Bật/Tắt trạng thái">
                  <input 
                    type="checkbox" 
                    :checked="item.trangThai === 1" 
                    @click="toggleStatus(item, $event)"
                  >
                  <span class="slider round"></span>
                </label>
              </div>
            </td>
          </tr>
        </tbody>
      </table>

      <div class="pagination-footer">
        <div class="page-info">
          Hiển thị 
          <select v-model="pageSize" @change="handlePageSizeChange">
            <option :value="5">5</option>
            <option :value="10">10</option>
            <option :value="20">20</option>
          </select> 
          khách hàng / trang
        </div>
        <div class="page-controls">
          <button :disabled="page === 1" @click="changePage(page - 1)">‹</button>
          <button 
            v-for="p in visiblePages" 
            :key="p" 
            :class="{ active: p === page }" 
            @click="changePage(p)"
          >
            {{ p }}
          </button>
          <button :disabled="page === totalPages" @click="changePage(page + 1)">›</button>
        </div>
      </div>
    </div>

    <div v-if="showAddressModal" class="quick-modal-overlay" @click.self="closeQuickAddressModal">
      <div class="quick-modal-card">
        <div class="quick-modal-header">
          <div class="quick-title-wrap">
            <i class="fas fa-map-marker-alt quick-title-icon"></i>
            <div>
              <h3 class="quick-title">Sổ địa chỉ khách hàng</h3>
              <p class="quick-subtitle">
                {{ selectedCustomer?.tenKhachHang || '-' }} · {{ selectedCustomer?.maKhachHang || '-' }}
              </p>
            </div>
          </div>
          <div class="quick-head-actions">
            <button class="quick-head-btn" @click="refreshAddresses" title="Làm mới danh sách">
              <i class="fas fa-sync-alt"></i>
            </button>
            <button class="quick-head-btn" @click="closeQuickAddressModal" title="Đóng">
              <i class="fas fa-times"></i>
            </button>
          </div>
        </div>

        <div class="quick-modal-body">
          <div class="quick-list-panel">
            <div class="quick-panel-title">
              <i class="fas fa-list"></i> Danh sách địa chỉ
            </div>

            <table class="quick-table">
              <thead>
                <tr>
                  <th width="10%">STT</th>
                  <th>Địa chỉ</th>
                  <th width="20%">Mặc định</th>
                </tr>
              </thead>
              <tbody>
                <tr v-if="addressLoading">
                  <td colspan="3" class="quick-empty">Đang tải địa chỉ...</td>
                </tr>
                <tr v-else-if="customerAddresses.length === 0">
                  <td colspan="3" class="quick-empty">Chưa có địa chỉ nào.</td>
                </tr>
                <tr v-else v-for="(addr, idx) in customerAddresses" :key="addr.id || idx">
                  <td>{{ idx + 1 }}</td>
                  <td>
                    <div class="quick-addr-line">{{ formatAddress(addr) }}</div>
                    <div class="quick-addr-sub">Người nhận: {{ addr.tenNguoiNhan || '---' }} · SĐT: {{ addr.soDienThoai || '---' }}</div>
                  </td>
                  <td>
                    <span v-if="addr.macDinh" class="quick-default-badge">Mặc định</span>
                    <span v-else>--</span>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>

          <div class="quick-form-panel">
            <div class="quick-panel-title">
              <i class="fas fa-location-arrow"></i> Thêm nhanh địa chỉ
            </div>

            <div class="quick-form-grid two-cols">
              <div class="quick-field">
                <label>Họ tên người nhận</label>
                <input v-model="quickAddressForm.tenNguoiNhan" class="form-control" type="text" placeholder="vd: Nguyễn Văn A">
              </div>
              <div class="quick-field">
                <label>Số điện thoại</label>
                <input v-model="quickAddressForm.soDienThoai" class="form-control" type="text" placeholder="vd: 09xxxxxxxx">
              </div>
            </div>

            <div class="quick-form-grid two-cols">
              <div class="quick-field">
                <label>Thành phố/Tỉnh</label>
                <div class="combo-box" @click.stop>
                  <input
                    v-model="quickAddressSearch.tinh"
                    class="form-control combo-input"
                    type="text"
                    placeholder="Chọn hoặc nhập tỉnh/thành"
                    @focus="openQuickDropdown('tinh')"
                    @input="handleProvinceInput"
                    @blur="closeQuickDropdown('tinh')"
                  >
                  <button type="button" class="combo-toggle" @mousedown.prevent @click="toggleQuickDropdown('tinh')">
                    <i class="fas fa-chevron-down"></i>
                  </button>
                  <ul v-if="quickDropdownOpen.tinh" class="combo-menu">
                    <li
                      v-for="p in filteredProvinces"
                      :key="p.code"
                      class="combo-item"
                      @mousedown.prevent="selectProvince(p)"
                    >
                      {{ p.name }}
                    </li>
                    <li v-if="filteredProvinces.length === 0" class="combo-item combo-empty">Không có kết quả</li>
                  </ul>
                </div>
              </div>
              <div class="quick-field">
                <label>Quận/Huyện</label>
                <div class="combo-box" @click.stop>
                  <input
                    v-model="quickAddressSearch.huyen"
                    class="form-control combo-input"
                    type="text"
                    placeholder="Chọn hoặc nhập quận/huyện"
                    :disabled="!quickAddressForm.tinhId"
                    @focus="openQuickDropdown('huyen')"
                    @input="handleDistrictInput"
                    @blur="closeQuickDropdown('huyen')"
                  >
                  <button type="button" class="combo-toggle" :disabled="!quickAddressForm.tinhId" @mousedown.prevent @click="toggleQuickDropdown('huyen')">
                    <i class="fas fa-chevron-down"></i>
                  </button>
                  <ul v-if="quickDropdownOpen.huyen" class="combo-menu">
                    <li
                      v-for="d in filteredDistricts"
                      :key="d.code"
                      class="combo-item"
                      @mousedown.prevent="selectDistrict(d)"
                    >
                      {{ d.name }}
                    </li>
                    <li v-if="filteredDistricts.length === 0" class="combo-item combo-empty">Không có kết quả</li>
                  </ul>
                </div>
              </div>
            </div>

            <div class="quick-form-grid two-cols">
              <div class="quick-field">
                <label>Phường/Xã</label>
                <div class="combo-box" @click.stop>
                  <input
                    v-model="quickAddressSearch.xa"
                    class="form-control combo-input"
                    type="text"
                    placeholder="Chọn hoặc nhập phường/xã"
                    :disabled="!quickAddressForm.huyenId"
                    @focus="openQuickDropdown('xa')"
                    @input="handleWardInput"
                    @blur="closeQuickDropdown('xa')"
                  >
                  <button type="button" class="combo-toggle" :disabled="!quickAddressForm.huyenId" @mousedown.prevent @click="toggleQuickDropdown('xa')">
                    <i class="fas fa-chevron-down"></i>
                  </button>
                  <ul v-if="quickDropdownOpen.xa" class="combo-menu combo-menu-up">
                    <li
                      v-for="w in filteredWards"
                      :key="w.code"
                      class="combo-item"
                      @mousedown.prevent="selectWard(w)"
                    >
                      {{ w.name }}
                    </li>
                    <li v-if="filteredWards.length === 0" class="combo-item combo-empty">Không có kết quả</li>
                  </ul>
                </div>
              </div>
              <div class="quick-field">
                <label>Địa chỉ cụ thể</label>
                <input v-model="quickAddressForm.diaChiCuThe" class="form-control" type="text" placeholder="Số nhà, đường...">
              </div>
            </div>

            <label class="quick-checkbox">
              <input type="checkbox" v-model="quickAddressForm.macDinh"> Đặt làm địa chỉ mặc định
            </label>

            <div class="quick-submit-wrap">
              <button class="btn btn-gradient" :disabled="addingAddress" @click="addQuickAddress">
                {{ addingAddress ? 'Đang thêm...' : 'Thêm nhanh' }}
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch, computed } from 'vue';
import request from '@/services/request'; 
import Swal from 'sweetalert2';
import axios from 'axios';
import { useRouter } from 'vue-router';
import { toastSuccess, toastError } from '@/utils/toast';
import { fetchProvinces as apiFetchProvinces, fetchDistricts as apiFetchDistricts, fetchWards as apiFetchWards } from '@/api/locationApi';

const router = useRouter();
const items = ref([]);
const loading = ref(false);
const page = ref(1);
const pageSize = ref(10);
const totalPages = ref(1);
const filter = reactive({ keyword: '', gioiTinh: null, trangThai: null });
const API_URL = '/khach-hang'; 
const role = (sessionStorage.getItem('userRole') || 'ADMIN').toUpperCase();
const customerCreateRouteName = computed(() => (role === 'STAFF' ? 'staff-customer-create' : 'admin-customer-create'));
const customerDetailRouteName = computed(() => (role === 'STAFF' ? 'staff-customer-detail' : 'admin-customer-detail'));
const showAddressModal = ref(false);
const addressLoading = ref(false);
const addingAddress = ref(false);
const selectedCustomer = ref(null);
const customerAddresses = ref([]);
const provinces = ref([]);
const quickDistricts = ref([]);
const quickWards = ref([]);
const quickAddressForm = reactive({
  tenNguoiNhan: '',
  soDienThoai: '',
  tinhId: '',
  huyenId: '',
  xaId: '',
  diaChiCuThe: '',
  macDinh: false
});
const quickAddressSearch = reactive({ tinh: '', huyen: '', xa: '' });
const quickDropdownOpen = reactive({ tinh: false, huyen: false, xa: false });

const normalizeText = (value) => String(value || '')
  .toLowerCase()
  .normalize('NFD')
  .replace(/[\u0300-\u036f]/g, '')
  .trim();

const filterOptions = (options, keyword) => {
  const normalizedKeyword = normalizeText(keyword);
  if (!normalizedKeyword) return options;
  return options.filter(item => normalizeText(item.name).includes(normalizedKeyword));
};

const filteredProvinces = computed(() => filterOptions(provinces.value, quickAddressSearch.tinh));
const filteredDistricts = computed(() => filterOptions(quickDistricts.value, quickAddressSearch.huyen));
const filteredWards = computed(() => filterOptions(quickWards.value, quickAddressSearch.xa));

const findExactMatchByName = (options, keyword) => options.find(item => normalizeText(item.name) === normalizeText(keyword));

const openQuickDropdown = (type) => {
  quickDropdownOpen[type] = true;
};

const closeQuickDropdown = (type) => {
  setTimeout(() => {
    quickDropdownOpen[type] = false;
  }, 120);
};

const toggleQuickDropdown = (type) => {
  quickDropdownOpen[type] = !quickDropdownOpen[type];
};

const resetDistrictAndWard = () => {
  quickAddressForm.huyenId = '';
  quickAddressForm.xaId = '';
  quickAddressSearch.huyen = '';
  quickAddressSearch.xa = '';
  quickDistricts.value = [];
  quickWards.value = [];
};

const resetWard = () => {
  quickAddressForm.xaId = '';
  quickAddressSearch.xa = '';
  quickWards.value = [];
};

const selectProvince = async (province) => {
  quickAddressForm.tinhId = province.code;
  quickAddressSearch.tinh = province.name;
  quickDropdownOpen.tinh = false;
  await onQuickProvinceChange();
};

const selectDistrict = async (district) => {
  quickAddressForm.huyenId = district.code;
  quickAddressSearch.huyen = district.name;
  quickDropdownOpen.huyen = false;
  await onQuickDistrictChange();
};

const selectWard = (ward) => {
  quickAddressForm.xaId = ward.code;
  quickAddressSearch.xa = ward.name;
  quickDropdownOpen.xa = false;
};

const handleProvinceInput = async () => {
  quickDropdownOpen.tinh = true;
  const matched = findExactMatchByName(provinces.value, quickAddressSearch.tinh);
  if (matched) {
    if (String(quickAddressForm.tinhId) !== String(matched.code)) {
      await selectProvince(matched);
    }
    return;
  }
  if (quickAddressForm.tinhId) {
    quickAddressForm.tinhId = '';
    resetDistrictAndWard();
  }
};

const handleDistrictInput = async () => {
  quickDropdownOpen.huyen = true;
  const matched = findExactMatchByName(quickDistricts.value, quickAddressSearch.huyen);
  if (matched) {
    if (String(quickAddressForm.huyenId) !== String(matched.code)) {
      await selectDistrict(matched);
    }
    return;
  }
  if (quickAddressForm.huyenId) {
    quickAddressForm.huyenId = '';
    resetWard();
  }
};

const handleWardInput = () => {
  quickDropdownOpen.xa = true;
  const matched = findExactMatchByName(quickWards.value, quickAddressSearch.xa);
  quickAddressForm.xaId = matched ? matched.code : '';
};

// FETCH DATA
const fetchData = async () => {
    loading.value = true;
    try {
        const res = await request.get(API_URL, {
            params: {
                page: page.value - 1, size: pageSize.value,
                keyword: filter.keyword, gioiTinh: filter.gioiTinh, trangThai: filter.trangThai
            }
        });
        items.value = res.data.content;
        totalPages.value = res.data.totalPages;
    } catch (e) { console.error(e); } finally { loading.value = false; }
};

const getAddressString = (item) => {
    let list = item.listDiaChi || item.addresses || item.diaChiKhachHangList;
    if (typeof list === 'string') {
        try { list = JSON.parse(list); } catch (e) { return '-'; }
    }
    if (Array.isArray(list) && list.length > 0) {
        let addr = list.find(a => a.macDinh === true || a.macDinh === 1 || a.macDinh === 'true');
        if (!addr) addr = list.find(a => a.trangThai === 1) || list[0];
        if (!addr) return 'Chưa có địa chỉ';

        const street = addr.diaChiCuThe || addr.diaChiNhanHang || '';
        const ward = addr.phuong || addr.tenXa || '';
        const district = addr.quan || addr.tenHuyen || '';
        const city = addr.thanhPho || addr.tenTinh || '';

        const parts = [street, ward, district, city].filter(p => p && String(p).trim() !== '');
        return parts.length > 0 ? parts.join(', ') : 'Địa chỉ trống';
    }
    return '-'; 
};

const resetFilter = () => {
    filter.keyword = ''; filter.gioiTinh = null; filter.trangThai = null;
    page.value = 1; fetchData();
};

const toggleStatus = async (item, event) => {
    event.preventDefault(); 
    const newStatus = item.trangThai === 1 ? 0 : 1;
    const actionText = newStatus === 1 ? 'Kích hoạt' : 'Ngừng hoạt động';

    const result = await Swal.fire({
        title: `Xác nhận ${actionText}?`,
        text: `Bạn có muốn ${actionText.toLowerCase()} khách hàng "${item.tenKhachHang}"?`,
        icon: 'question', showCancelButton: true,
        confirmButtonText: 'Đồng ý', cancelButtonText: 'Hủy'
    });

    if (result.isConfirmed) {
        try {
            await request.put(`${API_URL}/${item.id}/trang-thai`, null, { params: { trangThai: newStatus } });
            item.trangThai = newStatus;
            toastSuccess('Cập nhật trạng thái thành công!');
        } catch (e) { toastError('Lỗi cập nhật trạng thái'); }
    }
};

const exportExcel = async () => {
    const result = await Swal.fire({
        title: 'Xác nhận', text: 'Tải xuống danh sách khách hàng?',
        icon: 'question', showCancelButton: true, confirmButtonText: 'Có', cancelButtonText: 'Hủy'
    });
    if (!result.isConfirmed) return;

    try {
        const response = await request.get(`${API_URL}/export`, {
            params: { keyword: filter.keyword, gioiTinh: filter.gioiTinh, trangThai: filter.trangThai },
            responseType: 'blob'
        });
        const url = window.URL.createObjectURL(new Blob([response.data]));
        const link = document.createElement('a'); link.href = url;
        const dateStr = new Date().toISOString().slice(0,10);
        link.setAttribute('download', `DS_KhachHang_${dateStr}.xlsx`);
        document.body.appendChild(link); link.click(); document.body.removeChild(link);
        toastSuccess('Xuất Excel thành công!');
    } catch (e) { toastError('Lỗi xuất file Excel'); }
};

const changePage = (p) => { if (p >= 1 && p <= totalPages.value) { page.value = p; fetchData(); } };
const handlePageSizeChange = () => { page.value = 1; fetchData(); };
const viewDetail = (item) => { router.push({ name: customerDetailRouteName.value, params: { id: item.id } }); };

const formatAddress = (addr) => {
  const street = addr.diaChiCuThe || addr.diaChiNhanHang || '';
  const ward = addr.phuong || addr.tenXa || '';
  const district = addr.quan || addr.tenHuyen || '';
  const city = addr.thanhPho || addr.tenTinh || '';
  const parts = [street, ward, district, city].filter(part => part && String(part).trim() !== '');
  return parts.length > 0 ? parts.join(', ') : 'Địa chỉ trống';
};

const fetchProvinces = async () => {
  if (provinces.value.length > 0) return;
  try {
    const res = await apiFetchProvinces();
    provinces.value = res || [];
  } catch (e) {
    toastError('Không tải được danh mục tỉnh/thành');
  }
};

const fetchCustomerAddresses = async () => {
  if (!selectedCustomer.value?.id) return;
  addressLoading.value = true;
  try {
    const res = await request.get('/dia-chi', { params: { khachHangId: selectedCustomer.value.id } });
    customerAddresses.value = Array.isArray(res.data) ? res.data : [];
  } catch (e) {
    customerAddresses.value = [];
    toastError('Không tải được sổ địa chỉ');
  } finally {
    addressLoading.value = false;
  }
};

const resetQuickAddressForm = () => {
  quickAddressForm.tenNguoiNhan = selectedCustomer.value?.tenKhachHang || '';
  quickAddressForm.soDienThoai = selectedCustomer.value?.soDienThoai || '';
  quickAddressForm.tinhId = '';
  quickAddressForm.huyenId = '';
  quickAddressForm.xaId = '';
  quickAddressForm.diaChiCuThe = '';
  quickAddressForm.macDinh = false;
  quickAddressSearch.tinh = '';
  quickAddressSearch.huyen = '';
  quickAddressSearch.xa = '';
  quickDropdownOpen.tinh = false;
  quickDropdownOpen.huyen = false;
  quickDropdownOpen.xa = false;
  quickDistricts.value = [];
  quickWards.value = [];
};

const openQuickAddressModal = async (customer) => {
  selectedCustomer.value = customer;
  showAddressModal.value = true;
  resetQuickAddressForm();
  await Promise.all([fetchProvinces(), fetchCustomerAddresses()]);
};

const closeQuickAddressModal = () => {
  showAddressModal.value = false;
  selectedCustomer.value = null;
  customerAddresses.value = [];
};

const refreshAddresses = async () => {
  await fetchCustomerAddresses();
};

const onQuickProvinceChange = async () => {
  resetDistrictAndWard();

  if (!quickAddressForm.tinhId) return;
  try {
    quickDistricts.value = await apiFetchDistricts(quickAddressForm.tinhId);
  } catch (e) {
    toastError('Không tải được quận/huyện');
  }
};

const onQuickDistrictChange = async () => {
  resetWard();

  if (!quickAddressForm.huyenId) return;
  try {
    quickWards.value = await apiFetchWards(quickAddressForm.huyenId);
  } catch (e) {
    toastError('Không tải được phường/xã');
  }
};

const addQuickAddress = async () => {
  if (!selectedCustomer.value?.id) return;
  if (!quickAddressForm.tenNguoiNhan.trim()) return toastError('Thiếu họ tên người nhận');
  if (!quickAddressForm.soDienThoai.trim()) return toastError('Thiếu số điện thoại');
  if (!quickAddressForm.tinhId || !quickAddressForm.huyenId || !quickAddressForm.xaId) {
    return toastError('Vui lòng chọn đầy đủ tỉnh/huyện/xã');
  }

  const selectedProvince = provinces.value.find(p => String(p.code) === String(quickAddressForm.tinhId));
  const selectedDistrict = quickDistricts.value.find(d => String(d.code) === String(quickAddressForm.huyenId));
  const selectedWard = quickWards.value.find(w => String(w.code) === String(quickAddressForm.xaId));

  const payload = {
    idKhachHang: selectedCustomer.value.id,
    tenNguoiNhan: quickAddressForm.tenNguoiNhan.trim(),
    soDienThoai: quickAddressForm.soDienThoai.trim(),
    thanhPho: selectedProvince?.name || '',
    quan: selectedDistrict?.name || '',
    phuong: selectedWard?.name || '',
    diaChiCuThe: quickAddressForm.diaChiCuThe.trim(),
    macDinh: quickAddressForm.macDinh,
    trangThai: 1
  };

  addingAddress.value = true;
  try {
    await request.post('/dia-chi', payload);
    toastSuccess('Thêm địa chỉ thành công!');
    await fetchCustomerAddresses();
    await fetchData();
    resetQuickAddressForm();
  } catch (e) {
    toastError(e.response?.data?.message || 'Không thể thêm địa chỉ');
  } finally {
    addingAddress.value = false;
  }
};

let searchTimer = null;
watch(() => filter.keyword, () => {
    clearTimeout(searchTimer);
    searchTimer = setTimeout(() => { page.value = 1; fetchData(); }, 400);
});

const visiblePages = computed(() => {
    let p = [];
    for (let i = 1; i <= totalPages.value; i++) {
        if (i === 1 || i === totalPages.value || (i >= page.value - 1 && i <= page.value + 1)) p.push(i);
    }
    return p;
});

onMounted(() => { fetchData(); });
</script>

<style scoped>
/* === CSS CHUẨN ĐỒNG BỘ === */
.page-container { padding: 20px; font-family: 'Segoe UI', sans-serif; background: #ebecee; min-height: 100vh; color: #333; font-size: 14px; }
.page-title { color: #2b4360; font-weight: 700; font-size: 24px; margin-bottom: 20px; }

/* CARD STYLING */
.control-panel, .table-container { 
    background: white; border-radius: 16px; border: 1px solid #bfdbfe !important; 
    box-shadow: 0 4px 12px rgba(0,0,0,0.05); margin-bottom: 20px; padding: 24px; 
}
.table-container { padding: 0; overflow: hidden; }

/* FLEX LAYOUT */
.controls-row { display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 15px; }
.filter-group { display: flex; gap: 12px; align-items: center; flex-wrap: wrap; }
.action-group { display: flex; gap: 10px; }

/* INPUTS & SEARCH */
.search-box { position: relative; width: 250px; }
.search-icon { position: absolute; left: 12px; top: 11px; color: #94a3b8; }
.search-box input { width: 100%; padding: 8px 10px 8px 36px; border: 1px solid #e2e8f0; border-radius: 6px; outline: none; height: 40px; }
.search-box input:focus { border-color: #3b82f6; box-shadow: 0 0 0 3px rgba(59,130,246,0.1); }

/* SELECT STYLES */
.form-select { height: 40px; padding: 0 10px; border: 1px solid #e2e8f0; border-radius: 6px; outline: none; color: #334155; min-width: 150px; cursor: pointer; }
.form-select:focus { border-color: #3b82f6; box-shadow: 0 0 0 3px rgba(59,130,246,0.1); }

/* BUTTONS */
.btn { 
    height: 40px; padding: 0 20px; border-radius: 6px; font-weight: 600; cursor: pointer; 
    font-size: 13px; border: 1px solid transparent; transition: 0.2s; display: inline-flex; 
    align-items: center; gap: 8px; text-decoration: none;
}
.btn-outline { background: #fff; border: 1px solid #e2e8f0; color: #475569; }
.btn-outline:hover { background: #f8fafc; border-color: #cbd5e1; }
.btn-gradient { 
    background: linear-gradient(135deg, #1e3a8a 0%, #0f172a 100%); color: #fff; 
    box-shadow: 0 4px 10px rgba(15, 23, 42, 0.2); 
}
.btn-gradient:hover { transform: translateY(-1px); box-shadow: 0 6px 15px rgba(15, 23, 42, 0.3); }

/* TABLE STYLES */
.custom-table { width: 100%; border-collapse: collapse; }
.custom-table th {
    background: #f5f5f5 !important; color: #000000; padding: 16px; text-align: left;
     font-weight: 700; text-transform: uppercase; border-bottom: 1px solid #e0eaf5 !important; white-space: nowrap;
}
.custom-table th.text-center { text-align: center; }
.custom-table td { padding: 14px 16px; border-bottom: 1px solid #f1f5f9; vertical-align: middle; font-size: 14px; }

.text-center { text-align: center; }
.code-text { color: #000000; font-weight: 600; font-family: monospace; font-size: 13px; }
.name-text { font-weight: 600; color: #000000; }
.text-gray { color: #000000; }
.text-address { max-width: 250px; color: #000000; font-size: 13px; line-height: 1.4; }
.truncate-text { display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; text-overflow: ellipsis; }
.empty-state { padding: 40px; color: #64748b; font-style: italic; }

/* BADGES */
.badge { padding: 4px 12px; border-radius: 20px; font-size: 11px; font-weight: 600; white-space: nowrap; border: 1px solid transparent; }
.badge-active { background: #dcfce7; color: #166534; border-color: #bbf7d0; }
.badge-stopped { background: #fee2e2; color: #991b1b; border-color: #fecaca; }

/* ACTIONS */
.action-wrapper { display: flex; align-items: center; justify-content: center; gap: 10px; }
.icon-btn { 
    width: 34px; height: 34px; display: flex; align-items: center; justify-content: center; 
    background: white; border: 1px solid #e2e8f0; border-radius: 6px; cursor: pointer; color: #64748b; 
}
.icon-btn:hover { background: #f1f5f9; color: #0f172a; border-color: #cbd5e1; }

/* TOGGLE SWITCH */
.switch { position: relative; display: inline-block; width: 36px; height: 20px; }
.switch input { opacity: 0; width: 0; height: 0; }
.slider { position: absolute; cursor: pointer; top: 0; left: 0; right: 0; bottom: 0; background-color: #cbd5e1; transition: .4s; border-radius: 34px; }
.slider:before { position: absolute; content: ""; height: 14px; width: 14px; left: 3px; bottom: 3px; background-color: white; transition: .4s; border-radius: 50%; }
input:checked + .slider { background-color: #10b981; }
input:checked + .slider:before { transform: translateX(16px); }

/* PAGINATION */
.pagination-footer { display: flex; justify-content: space-between; align-items: center; padding: 15px 24px; border-top: 1px solid #f1f5f9; }
.page-info select { border: 1px solid #e2e8f0; border-radius: 4px; padding: 2px 5px; margin: 0 5px; }
.page-controls button { width: 32px; height: 32px; border: 1px solid #e2e8f0; background: #fff; border-radius: 4px; margin-left: 5px; cursor: pointer; }
.page-controls button.active { background: #0f172a; color: #fff; border-color: #0f172a; }
.btn-navy {
    background-color: #0f172a; /* Xanh than đậm */
    color: #ffffff;
    box-shadow: 0 4px 6px rgba(15, 23, 42, 0.2);
}
.btn-navy:hover {
    background-color: #1e293b;
    transform: translateY(-1px);
}
/* Màu chữ placeholder đen xì, rõ nét */
.input-den::placeholder {
    color: #000000 !important;  /* Màu đen */
    opacity: 1 !important;      /* Chống mờ */
    font-weight: 500;           /* Đậm lên tí cho dễ đọc (tùy chọn) */
}

.quick-modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.35);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1200;
}

.quick-modal-card {
  width: min(1060px, 92vw);
  background: #fff;
  border-radius: 16px;
  border: 1px solid #e2e8f0;
  box-shadow: 0 18px 40px rgba(15, 23, 42, 0.2);
  overflow: hidden;
}

.quick-modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 18px;
  border-bottom: 1px solid #f1f5f9;
}

.quick-title-wrap {
  display: flex;
  align-items: flex-start;
  gap: 10px;
}

.quick-title-icon {
  color: #f97373;
  margin-top: 2px;
}

.quick-title {
  margin: 0;
  font-size: 22px;
  color: #334155;
  font-weight: 600;
}

.quick-subtitle {
  margin: 4px 0 0;
  color: #94a3b8;
  font-size: 13px;
}

.quick-head-actions {
  display: flex;
  gap: 8px;
}

.quick-head-btn {
  width: 34px;
  height: 34px;
  border-radius: 10px;
  border: 1px solid #e2e8f0;
  background: #fff;
  color: #475569;
  cursor: pointer;
}

.quick-head-btn:hover {
  background: #f8fafc;
}

.quick-modal-body {
  display: grid;
  grid-template-columns: 1.2fr 0.9fr;
  gap: 14px;
  padding: 14px;
}

.quick-list-panel,
.quick-form-panel {
  border: 1px solid #e2e8f0;
  border-radius: 14px;
  background: #fff;
}

.quick-panel-title {
  padding: 12px 14px;
  border-bottom: 1px solid #f1f5f9;
  font-weight: 600;
  color: #475569;
  display: flex;
  align-items: center;
  gap: 8px;
}

.quick-table {
  width: 100%;
  border-collapse: collapse;
}

.quick-table th,
.quick-table td {
  padding: 10px 12px;
  border-bottom: 1px solid #f8fafc;
  font-size: 13px;
  color: #334155;
  vertical-align: top;
}

.quick-table th {
  background: #f8fafc;
  font-weight: 600;
}

.quick-empty {
  text-align: center;
  color: #64748b;
  padding: 20px;
}

.quick-addr-line {
  color: #334155;
  margin-bottom: 4px;
}

.quick-addr-sub {
  font-size: 12px;
  color: #94a3b8;
}

.quick-default-badge {
  display: inline-block;
  background: #fee2e2;
  border: 1px solid #fecaca;
  color: #b91c1c;
  border-radius: 999px;
  padding: 3px 10px;
  font-size: 12px;
}

.quick-form-panel {
  padding-bottom: 12px;
}

.quick-form-grid {
  display: grid;
  gap: 10px;
  padding: 10px 12px 0;
}

.quick-form-grid.two-cols {
  grid-template-columns: 1fr 1fr;
}

.quick-field label {
  display: block;
  font-size: 13px;
  font-weight: 600;
  color: #475569;
  margin-bottom: 6px;
}

.combo-box {
  position: relative;
}

.combo-input {
  padding-right: 34px;
}

.combo-toggle {
  position: absolute;
  right: 8px;
  top: 50%;
  transform: translateY(-50%);
  border: none;
  background: transparent;
  color: #64748b;
  cursor: pointer;
  width: 20px;
  height: 20px;
}

.combo-toggle:disabled {
  cursor: not-allowed;
  opacity: 0.4;
}

.combo-menu {
  position: absolute;
  left: 0;
  right: 0;
  top: calc(100% + 4px);
  max-height: 180px;
  overflow-y: auto;
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  box-shadow: 0 8px 18px rgba(15, 23, 42, 0.12);
  z-index: 25;
  margin: 0;
  padding: 4px 0;
  list-style: none;
}

.combo-menu-up {
  top: auto;
  bottom: calc(100% + 4px);
}

.combo-item {
  padding: 8px 12px;
  font-size: 13px;
  color: #334155;
  cursor: pointer;
}

.combo-item:hover {
  background: #f1f5f9;
}

.combo-empty {
  color: #94a3b8;
  cursor: default;
}

.quick-checkbox {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px;
  color: #64748b;
  font-size: 13px;
}

.quick-submit-wrap {
  display: flex;
  justify-content: flex-end;
  padding: 0 12px;
}

@media (max-width: 980px) {
  .quick-modal-body {
    grid-template-columns: 1fr;
  }

  .quick-form-grid.two-cols {
    grid-template-columns: 1fr;
  }
}
</style>