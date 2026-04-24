<template>
  <div class="page-container">
    <div class="header-row">
       <div class="header-title">
          <h3 style="color: #1e293b">Phiếu giảm giá / Chỉnh sửa phiếu giảm giá</h3>
       </div>
       <div style="display: flex; gap: 10px;">
           <button type="button" @click="$router.go(-1)" class="btn btn-back">
              <i class="fas fa-arrow-left"></i> Quay lại
           </button>
       </div>
    </div>

    <div class="main-content">
      
      <div class="card left-panel">
        
        <div class="form-group">
          <label>Mã phiếu giảm giá</label>
          <input 
            v-model="form.maPhieuGiamGia" 
            class="form-control" 
            disabled 
            style="background-color: #f8fafc; color: #94a3b8;" 
          />
        </div>
        
        <div class="form-group">
          <label>Tên giảm giá <span class="required">*</span></label>
          <input 
            v-model="form.tenPhieuGiamGia" 
            placeholder="Ví dụ: Siêu sale 2025" 
            class="form-control" 
          />
        </div>

        <div class="row-2">
          <div class="form-group">
            <label>Hình thức giảm</label>
            <div class="radio-group mt-2">
              <label class="radio-item">
                <input type="radio" value="TienMat" v-model="form.loaiPhieu" /> VNĐ
              </label>
              <label class="radio-item">
                <input type="radio" value="PhanTram" v-model="form.loaiPhieu" /> %
              </label>
              <label class="radio-item">
                <input type="radio" value="FreeShip" v-model="form.loaiPhieu" /> Free Ship
              </label>
            </div>
          </div>
          
          <div class="form-group" v-if="form.loaiPhieu !== 'FreeShip'">
            <label>Giá trị giảm ({{ form.loaiPhieu === 'PhanTram' ? '%' : 'VNĐ' }}) <span class="required">*</span></label>
            <input 
                v-model.number="form.giaTriGiam" 
                type="number" 
                class="form-control" 
                placeholder="0" 
                min="0"
            />
          </div>
          <div class="form-group" v-else>
            <label>Giá trị giảm</label>
            <input type="text" class="form-control disabled-input" value="100% Phí vận chuyển" disabled />
          </div>
        </div>

        <div class="row-2">
          <div class="form-group">
            <label>
              Giảm tối đa (VNĐ)
              <span v-if="form.loaiPhieu === 'TienMat'" style="font-weight:normal; color:#64748b; font-size:12px;">(Không áp dụng cho VNĐ)</span>
            </label>
            <input 
                v-model.number="form.giaTriGiamToiDa" 
                type="number" 
                class="form-control" 
                :disabled="form.loaiPhieu === 'TienMat'"
                :class="{'disabled-input': form.loaiPhieu === 'TienMat'}"
                placeholder="0 (Để trống = Không giới hạn)" 
                min="0" 
            />
          </div>
          <div class="form-group">
            <label>Giới hạn dùng / 1 Khách</label>
            <input 
                v-model.number="form.gioiHanMoiKhach" 
                type="number" 
                class="form-control" 
                placeholder="Ví dụ: 1" 
                min="1" 
            />
          </div>
        </div>

        <div class="row-2">
          <div class="form-group">
            <div class="label-row">
                <label class="mb-0">Số lượng sử dụng (Tổng)</label>
                <div class="toggle-wrapper" v-if="form.kieu === 'CongKhai'">
                    <span class="toggle-text">Vô hạn</span>
                    <label class="switch">
                        <input type="checkbox" v-model="isUnlimited">
                        <span class="slider round"></span>
                    </label>
                </div>
            </div>
            
            <div v-if="form.kieu === 'CongKhai'">
                <input 
                  v-if="isUnlimited"
                  type="text" 
                  class="form-control" 
                  value="Vô hạn"
                  disabled
                  style="background-color: #f8fafc; font-style: italic; color: #64748b;"
                />
                <input 
                  v-else
                  v-model.number="form.soLuong" 
                  type="number" 
                  class="form-control" 
                  placeholder="Nhập số lượng..."
                  min="0"
                  @input="validateSoLuong"
                />
            </div>
            <div v-else>
                <input 
                  type="text" 
                  class="form-control disabled-input" 
                  value="Số lượng theo khách hàng chọn" 
                  disabled 
                />
            </div>
          </div>

          <div class="form-group">
            <label>Hóa đơn tối thiểu (VNĐ)</label>
            <input 
                v-model="donHangToiThieuDisplay"
                type="text"
                inputmode="numeric"
                autocomplete="off"
                class="form-control" 
                placeholder="0" 
            />
          </div>
        </div>

        <div class="row-2">
          <div class="form-group">
            <label>Ngày bắt đầu</label>
            <input v-model="form.ngayBatDau" type="datetime-local" class="form-control" />
          </div>
          <div class="form-group">
            <label>Ngày kết thúc</label>
            <input v-model="form.ngayKetThuc" type="datetime-local" class="form-control" />
          </div>
        </div>

        <div class="form-group">
          <label>Loại phiếu</label>
          <div class="radio-group mt-2">
            <label class="radio-item">
              <input type="radio" value="CongKhai" v-model="form.kieu" /> Công khai
            </label>
            <label class="radio-item">
              <input type="radio" value="CaNhan" v-model="form.kieu" /> Cá nhân
            </label>
          </div>
        </div>

        <div class="form-group">
          <label>Mô tả phiếu giảm giá</label>
          <textarea 
            v-model="form.moTa" 
            class="form-control" 
            rows="3" 
            placeholder="Nhập ghi chú hoặc điều kiện áp dụng phiếu..."
          ></textarea>
        </div>

        <button @click="submitForm" class="btn-submit">Lưu thay đổi</button>
      </div>

      <div class="card right-panel mt-4" v-if="form.kieu === 'CaNhan'">
        <div class="panel-header">
          <div class="selected-text mb-2">Đã chọn: {{ selectedCustomerIds.length }} khách hàng</div>
          
          <div class="segment-actions mb-3">
            <span style="font-weight: 600; font-size: 13px; color: #334155;">⚡ Hạng thành viên:</span>
            <button class="btn-segment btn-all" @click="selectBySegment('ALL')" title="Chọn tất cả khách">Tất cả</button>
            <button class="btn-segment btn-newbie" @click="selectBySegment('NEWBIE')" title="Chưa mua hàng">Newbie (0đ)</button>
            <button class="btn-segment btn-bronze" @click="selectBySegment('BRONZE')" title="Dưới 2 Triệu">Đồng</button>
            <button class="btn-segment btn-silver" @click="selectBySegment('SILVER')" title="2tr - 5 Triệu">Bạc</button>
            <button class="btn-segment btn-gold" @click="selectBySegment('GOLD')" title="5tr - 10 Triệu">Vàng</button>
            <button class="btn-segment btn-diamond" @click="selectBySegment('DIAMOND')" title="Trên 10 Triệu">Kim Cương 💎</button>
            <button class="btn-segment btn-clear" @click="selectedCustomerIds = []">Bỏ chọn hết</button>
          </div>

          <div class="filter-container">
            <button class="btn-filter-action btn-blue" @click="fetchCustomers">Làm mới</button>
            
            <button class="btn-filter-action btn-outline-mail" @click="confirmSendMailDirect">
              <i class="far fa-envelope"></i> Gửi Mail
            </button>

            <input 
              v-model="custFilter.ten" 
              @keyup.enter="searchCustomers"
              placeholder="Tìm theo tên..." 
              class="form-control filter-input" 
            />
            <input 
              v-model="custFilter.sdt" 
              @keyup.enter="searchCustomers"
              placeholder="Tìm theo SĐT..." 
              class="form-control filter-input" 
            />
            <select v-model="custFilter.trangThai" class="form-control filter-input" @change="searchCustomers">
              <option value="">Tất cả trạng thái</option>
              <option value="1">Đang hoạt động</option>
              <option value="0">Ngừng hoạt động</option>
            </select>
            <button class="btn-filter-action btn-gray" @click="clearFilters">Xóa lọc</button>
            
            <div class="page-size-wrapper">
              <span>Hiển thị:</span>
              <select v-model="custPageSize" class="form-control size-select" @change="searchCustomers">
                <option :value="5">5</option>
                <option :value="10">10</option>
                <option :value="20">20</option>
                <option :value="50">50</option>
              </select>
            </div>
          </div>
        </div>
        
        <div class="customer-list">
          <table class="mini-table">
            <thead>
              <tr>
                <th width="40" class="text-center">
                    <input type="checkbox" :checked="isAllSelected" @change="toggleSelectAll">
                </th>
                <th class="text-center">Tên</th>
                <th class="text-center">SĐT</th>
                <th class="text-center">Email</th>
                <th class="text-center">Ngày sinh</th>
                <th class="text-center">Tổng chi tiêu</th>
                <th class="text-center">Số đơn hàng</th>
                <th class="text-center">Đơn hàng gần nhất</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="loadingCust"><td colspan="8" class="text-center">Đang tải...</td></tr>
              <tr v-else-if="customers.length === 0"><td colspan="8" class="text-center">Không tìm thấy khách hàng</td></tr>
              
              <tr v-else v-for="cust in customers" :key="cust.id" :class="{ 'active-row': selectedCustomerIds.includes(cust.id) }">
                <td class="text-center">
                    <input 
                        type="checkbox" 
                        :value="cust.id" 
                        v-model="selectedCustomerIds"
                    >
                </td>
                <td class="text-center"><span class="cust-name">{{ cust.tenKhachHang }}</span></td>
                <td class="text-center"><span class="cust-info">{{ cust.soDienThoai }}</span></td>
                <td class="text-center"><span class="cust-info">{{ cust.email }}</span></td>
                <td class="text-center"><span class="cust-info">{{ formatDate(cust.ngaySinh) }}</span></td>
                <td class="text-center"><span class="cust-info">{{ formatCurrency(cust.tongChiTieu) }}</span></td>
                <td class="text-center"><span class="cust-info">{{ cust.soDonHang || 0 }}</span></td>
                <td class="text-center"><span class="cust-info">{{ cust.donHangGanNhat ? formatDate(cust.donHangGanNhat) : 'Chưa có' }}</span></td>
              </tr>
            </tbody>
          </table>
        </div>

        <div class="panel-footer mt-3">
            <span class="cust-info">Hiển thị {{ displayStart }} - {{ displayEnd }} trong {{ custTotalElements }} khách hàng</span>
            
            <div class="mini-pagination">
                <button class="mini-btn" :disabled="custPage === 0" @click="changeCustPage(custPage - 1)">‹</button>
                <span>Trang {{ custPage + 1 }}</span>
                <button class="mini-btn" :disabled="custPage >= custTotalPages - 1" @click="changeCustPage(custPage + 1)">›</button>
            </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, watch } from 'vue';
import request from '@/services/request';
import { useRouter, useRoute } from 'vue-router';
import Swal from 'sweetalert2';

const router = useRouter();
const route = useRoute();
const voucherId = route.params.id; 

const Toast = Swal.mixin({
  toast: true,
  position: 'top-end',
  showConfirmButton: false,
  timer: 3000,
  timerProgressBar: true,
  didOpen: (toast) => {
    toast.addEventListener('mouseenter', Swal.stopTimer)
    toast.addEventListener('mouseleave', Swal.resumeTimer)
  }
});

const isUnlimited = ref(false); 

const form = ref({
  id: '',
  maPhieuGiamGia: '',
  tenPhieuGiamGia: '',
  loaiPhieu: 'PhanTram',
  giaTriGiam: 0,
  giaTriGiamToiDa: null,
  donHangToiThieu: 0,
  soLuong: 0, 
  gioiHanMoiKhach: 1,
  ngayBatDau: '',
  ngayKetThuc: '',
  trangThai: 1,
  kieu: 'CongKhai',
  moTa: ''
});

const customers = ref([]);
const selectedCustomerIds = ref([]);
const loadingCust = ref(false);
const custPage = ref(0);
const custPageSize = ref(10);
const custTotalPages = ref(0);
const custTotalElements = ref(0); 

const custFilter = reactive({ 
    ten: '',
    sdt: '',
    trangThai: ''
});

const formatCurrency = (value) => {
    if (!value && value !== 0) return '0 đ';
    return new Intl.NumberFormat('vi-VN').format(value) + ' đ';
};

const formatDate = (dateStr) => {
    if (!dateStr) return 'Chưa có';
    const date = new Date(dateStr);
    const day = String(date.getDate()).padStart(2, '0');
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const year = date.getFullYear();
    return `${day}/${month}/${year}`;
};

const formatDateTimeForInput = (dateStr) => {
    if (!dateStr) return '';
    const date = new Date(dateStr);
    const yyyy = date.getFullYear();
    const mm = String(date.getMonth() + 1).padStart(2, '0');
    const dd = String(date.getDate()).padStart(2, '0');
    const hh = String(date.getHours()).padStart(2, '0');
    const min = String(date.getMinutes()).padStart(2, '0');
    return `${yyyy}-${mm}-${dd}T${hh}:${min}`;
};

const validateSoLuong = () => {
    if (form.value.soLuong < 0) {
        form.value.soLuong = 0;
    }
};

const donHangToiThieuDisplay = computed({
  get: () => {
    const value = Number(form.value.donHangToiThieu || 0);
    return value > 0 ? new Intl.NumberFormat('vi-VN').format(value) : '';
  },
  set: (value) => {
    const digitsOnly = String(value ?? '').replace(/\D/g, '');
    form.value.donHangToiThieu = digitsOnly ? Number(digitsOnly) : 0;
  }
});

const searchCustomers = () => {
    custPage.value = 0;
    fetchCustomers();
};

const fetchVoucherDetail = async () => {
    try {
        const res = await request.get(`/phieu-giam-gia/${voucherId}`);
        const data = res.data.data || res.data; 
        
        if (data) {
            form.value.id = data.id;
            form.value.maPhieuGiamGia = data.maPhieuGiamGia;
            form.value.tenPhieuGiamGia = data.tenPhieuGiamGia;
            form.value.loaiPhieu = data.loaiPhieu;
            form.value.giaTriGiam = data.giaTriGiam;
            form.value.giaTriGiamToiDa = data.giaTriGiamToiDa || null;
            form.value.donHangToiThieu = data.donHangToiThieu;
            form.value.soLuong = data.soLuong;
            form.value.gioiHanMoiKhach = data.gioiHanMoiKhach || 1;
            form.value.kieu = data.kieu;
            form.value.moTa = data.moTa;
            form.value.trangThai = data.trangThai;

            if (data.ngayBatDau) form.value.ngayBatDau = formatDateTimeForInput(data.ngayBatDau);
            if (data.ngayKetThuc) form.value.ngayKetThuc = formatDateTimeForInput(data.ngayKetThuc);
            
            if (data.kieu === 'CongKhai' && (data.soLuong === null || data.soLuong === 0)) {
                isUnlimited.value = true;
            }

            if ((data.kieu === 'CaNhan' || data.kieu === '1') && data.customerIds) {
                selectedCustomerIds.value = data.customerIds;
            }
        }
    } catch (e) {
        Toast.fire({ icon: 'error', title: 'Không thể tải thông tin phiếu giảm giá' });
        console.error("Lỗi API chi tiết:", e);
    }
};

const fetchCustomers = async () => {
    loadingCust.value = true;
    try {
        const searchKeyword = custFilter.ten.trim() !== '' ? custFilter.ten : custFilter.sdt.trim();

        // 1. Chỉ tạo params với những giá trị bắt buộc
        const queryParams = {
            page: custPage.value,
            size: custPageSize.value,
            keyword: searchKeyword
        };

        // 2. NẾU CÓ CHỌN TRẠNG THÁI THÌ MỚI GỬI LÊN (Bỏ qua chuỗi rỗng)
        if (custFilter.trangThai !== null && custFilter.trangThai !== '') {
            queryParams.trangThai = parseInt(custFilter.trangThai);
        }

        // 3. Gửi request sạch
        const res = await request.get('/khach-hang/thong-ke', {
            params: queryParams
        });
        
        customers.value = res.data.content;
        custTotalPages.value = res.data.totalPages;
        custTotalElements.value = res.data.totalElements || res.data.content.length; 
    } catch (e) {
        console.error("Lỗi 400 chi tiết:", e);
        Toast.fire({ icon: 'error', title: 'Lỗi tải danh sách khách hàng' });
    } finally {
        loadingCust.value = false;
    }
};
const changeCustPage = (p) => {
    if (p >= 0 && p < custTotalPages.value) {
        custPage.value = p;
        fetchCustomers();
    }
};

const changePageSize = () => {
    custPage.value = 0;
    fetchCustomers();
};

const clearFilters = () => {
    custFilter.ten = '';
    custFilter.sdt = '';
    custFilter.trangThai = '';
    custPage.value = 0;
    fetchCustomers();
};

const selectBySegment = async (type) => {
    loadingCust.value = true;
    try {
        const res = await request.get('/khach-hang/segmentation', { params: { type } });
        const ids = res.data;

        if (ids && ids.length > 0) {
            selectedCustomerIds.value = ids; 
            Toast.fire({ icon: 'success', title: `Đã tự động chọn ${ids.length} khách hàng!` });
        } else {
            Toast.fire({ icon: 'info', title: `Không có khách hàng nào thuộc nhóm này.` });
        }
    } catch (e) {
        console.error(e);
        Toast.fire({ icon: 'error', title: 'Lỗi khi lấy danh sách khách hàng theo nhóm' });
    } finally {
        loadingCust.value = false;
    }
};

const displayStart = computed(() => {
    if (custTotalElements.value === 0) return 0;
    return (custPage.value * custPageSize.value) + 1;
});

const displayEnd = computed(() => {
    const end = (custPage.value + 1) * custPageSize.value;
    return end > custTotalElements.value ? custTotalElements.value : end;
});

const isAllSelected = computed(() => {
    if (customers.value.length === 0) return false;
    return customers.value.every(c => selectedCustomerIds.value.includes(c.id));
});

const toggleSelectAll = (e) => {
    if (e.target.checked) {
        const currentIds = customers.value.map(c => c.id);
        selectedCustomerIds.value = [...new Set([...selectedCustomerIds.value, ...currentIds])];
    } else {
        const currentIds = customers.value.map(c => c.id);
        selectedCustomerIds.value = selectedCustomerIds.value.filter(id => !currentIds.includes(id));
    }
};

watch(() => form.value.kieu, (newVal) => {
    if (newVal === 'CongKhai') {
        selectedCustomerIds.value = [];
    } else {
        isUnlimited.value = false;
    }
});

const submitForm = async () => {
  try {
    if (!form.value.tenPhieuGiamGia || form.value.tenPhieuGiamGia.trim() === '') {
      return Toast.fire({ icon: 'warning', title: 'Vui lòng nhập tên phiếu giảm giá' });
    }
    
    if (form.value.loaiPhieu !== 'FreeShip' && (!form.value.giaTriGiam || Number(form.value.giaTriGiam) <= 0)) {
      return Toast.fire({ icon: 'warning', title: 'Giá trị giảm phải lớn hơn 0' });
    }
    if (!form.value.gioiHanMoiKhach || Number(form.value.gioiHanMoiKhach) < 1) {
      return Toast.fire({ icon: 'warning', title: 'Giới hạn dùng mỗi khách phải từ 1 trở lên' });
    }

    if (form.value.kieu === 'CongKhai' && !isUnlimited.value) {
        if (form.value.soLuong === null || form.value.soLuong === '' || Number(form.value.soLuong) <= 0) {
            return Toast.fire({ icon: 'warning', title: 'Số lượng phải lớn hơn 0' });
        }
    }

    if (!form.value.ngayBatDau || !form.value.ngayKetThuc) {
      return Toast.fire({ icon: 'warning', title: 'Vui lòng chọn đầy đủ thời gian' });
    }
    if (new Date(form.value.ngayKetThuc) <= new Date(form.value.ngayBatDau)) {
      return Toast.fire({ icon: 'warning', title: 'Ngày kết thúc phải sau ngày bắt đầu' });
    }
    
    if (form.value.kieu === 'CaNhan' && selectedCustomerIds.value.length === 0) {
        return Toast.fire({ icon: 'warning', title: 'Vui lòng chọn ít nhất 1 khách hàng' });
    }

    const payload = { ...form.value };
    
    if (!payload.maPhieuGiamGia || payload.maPhieuGiamGia.trim() === '') {
        payload.maPhieuGiamGia = null; 
    }

    if (payload.kieu === 'CongKhai' && isUnlimited.value) {
        payload.soLuong = null; 
    }

    if (payload.loaiPhieu === 'FreeShip') payload.giaTriGiam = 0;
    if (payload.loaiPhieu === 'TienMat') payload.giaTriGiamToiDa = null;

    if (payload.kieu === 'CaNhan') {
        payload.customerIds = selectedCustomerIds.value;
        payload.soLuong = selectedCustomerIds.value.length;
        payload.sendEmail = false; 
    }

    await request.put(`/phieu-giam-gia/${voucherId}`, payload);
    
    localStorage.setItem('voucherSuccessMessage', 'Cập nhật phiếu giảm giá thành công!');
    router.push({ name: 'admin-voucher-list' }); 
  } catch (error) {
    const msg = error.response?.data?.message || error.message || 'Có lỗi xảy ra';
    Toast.fire({ icon: 'error', title: msg });
  }
};

const confirmSendMailDirect = async () => {
    if (selectedCustomerIds.value.length === 0) {
        return Toast.fire({ icon: 'warning', title: 'Vui lòng chọn ít nhất 1 khách hàng để gửi mail' });
    }

    const result = await Swal.fire({
        title: 'Xác nhận gửi mail',
        text: 'Bạn có muốn gửi gmail cho các khách hàng đã chọn không?',
        icon: 'question',
        showCancelButton: true,
        confirmButtonText: 'Có',
        cancelButtonText: 'Hủy',
        confirmButtonColor: '#2563eb',
        cancelButtonColor: '#f59e0b',
    });

    if (!result.isConfirmed) return;

    try {
        const res = await request.get(`/phieu-giam-gia/${voucherId}/customers`).catch(() => ({data: []}));
        const savedCustomers = res.data || [];

        const emailMap = {};
        savedCustomers.forEach(c => emailMap[c.id] = c.email);
        customers.value.forEach(c => emailMap[c.id] = c.email);

        const emailsToSend = selectedCustomerIds.value
            .map(id => emailMap[id])
            .filter(email => email);

        if (emailsToSend.length === 0) {
            return Toast.fire({ icon: 'warning', title: 'Vui lòng Lưu thay đổi (để cập nhật ID mới) trước khi gửi mail nhé!' });
        }

        await request.post(`/phieu-giam-gia/${voucherId}/send-mail`, { emails: emailsToSend });
        Toast.fire({ icon: 'success', title: 'Đã gửi mail thành công!' }); 
    } catch(e) { 
        Toast.fire({ icon: 'error', title: 'Có lỗi khi gửi mail' }); 
    }
};

onMounted(() => {
    fetchVoucherDetail();
    fetchCustomers();
});
</script>

<style scoped>
.page-container { padding: 20px; font-family: 'Segoe UI', sans-serif; background-color: #ebecee; min-height: 100vh; }

.header-row { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.header-title h3 { font-weight: 700; color: #2b4360; font-size: 24px; margin: 0; }
.btn-back { background: linear-gradient(135deg, #1e3a8a 0%, #0f172a 100%);  color: #ffffff; padding: 8px 16px; border-radius: 6px; cursor: pointer; font-weight: 600; font-size: 14px; transition: 0.2s; border: none;}
.btn-back:hover { background: linear-gradient(135deg, #1e3a8a 0%, #0f172a 100%); color: #ffffff; }

.btn-outline-mail { background: #fff; border: 1px solid #cbd5e1; color: #475569; padding: 8px 16px; border-radius: 6px; cursor: pointer; font-weight: 600; font-size: 14px; transition: 0.2s; display: inline-flex; align-items: center; gap: 8px; }
.btn-outline-mail:hover { background: #f8fafc; border-color: #3b82f6; color: #3b82f6; }

.main-content {
    display: flex;
    flex-direction: column;
    gap: 20px;
}

.card { 
    background: #fff; 
    padding: 24px; 
    border-radius: 16px; 
    border: 1px solid #bfdbfe;
    box-shadow: 0 4px 12px rgba(0,0,0,0.05); 
    width: 100%;
}

.mt-4 { margin-top: 1.5rem; }
.mt-3 { margin-top: 1rem; }
.mt-2 { margin-top: 0.5rem; }
.mb-2 { margin-bottom: 0.5rem; }
.mb-0 { margin-bottom: 0 !important; }

.form-group { margin-bottom: 16px; }
.form-group label { display: block; margin-bottom: 6px; font-weight: 600; color: #334155; font-size: 13px; }
.required { color: #ef4444; }

.form-control { 
    width: 100%; 
    padding: 10px; 
    border: 1px solid #e2e8f0; 
    border-radius: 6px; 
    font-size: 14px; 
    outline: none; 
}
.form-control:focus { border-color: #3b82f6; box-shadow: 0 0 0 3px rgba(59,130,246,0.1); }
.form-control:disabled, .disabled-input { background-color: #f1f5f9; color: #94a3b8; cursor: not-allowed; }

.row-2 { display: flex; gap: 15px; }
.row-2 > * { flex: 1; }

.radio-group { display: flex; gap: 20px; margin-top: 8px; }
.radio-item { display: flex; align-items: center; cursor: pointer; font-size: 14px; }
.radio-item input { margin-right: 8px; accent-color: #0f172a; width: 16px; height: 16px; }

.label-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 6px;
}
.toggle-wrapper {
    display: flex;
    align-items: center;
    gap: 8px;
}
.toggle-text {
    font-size: 13px;
    font-weight: 600;
    color: #64748b;
}

.switch {
  position: relative;
  display: inline-block;
  width: 36px;
  height: 20px;
}
.switch input { 
  opacity: 0;
  width: 0;
  height: 0;
}
.slider {
  position: absolute;
  cursor: pointer;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: #ccc;
  transition: .4s;
  border-radius: 34px;
}
.slider:before {
  position: absolute;
  content: "";
  height: 14px;
  width: 14px;
  left: 3px;
  bottom: 3px;
  background-color: white;
  transition: .4s;
  border-radius: 50%;
}
input:checked + .slider {
  background-color: #2563eb;
}
input:checked + .slider:before {
  transform: translateX(16px);
}

.btn-submit { 
    width: 100%; 
    background: linear-gradient(135deg, #1e3a8a 0%, #0f172a 100%); 
    color: #fff; 
    padding: 12px; 
    border: none; 
    border-radius: 6px; 
    font-weight: 600; 
    cursor: pointer; 
    margin-top: 15px; 
    font-size: 15px; 
    box-shadow: 0 4px 10px rgba(15, 23, 42, 0.3);
    transition: 0.2s;
}
.btn-submit:hover { transform: translateY(-1px); box-shadow: 0 6px 15px rgba(15, 23, 42, 0.4); }

.panel-header { margin-bottom: 15px; border-bottom: 1px solid #f1f5f9; padding-bottom: 15px; }
.selected-text { color: #64748b; font-size: 14px; }

.filter-container {
    display: flex;
    gap: 10px;
    align-items: center;
    flex-wrap: wrap;
}
.filter-input {
    width: auto;
    flex: 1;
    min-width: 150px;
}
.btn-filter-action {
    padding: 10px 16px;
    border-radius: 6px;
    font-size: 14px;
    font-weight: 500;
    cursor: pointer;
    border: none;
    transition: 0.2s;
}
.btn-blue { background-color: #2563eb; color: #fff; }
.btn-blue:hover { background-color: #1d4ed8; }
.btn-gray { background-color: #64748b; color: #fff; }
.btn-gray:hover { background-color: #475569; }

.page-size-wrapper {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 14px;
    color: #334155;
}
.size-select {
    width: 70px;
    padding: 8px;
}

.customer-list { 
    max-height: 500px; 
    overflow-x: auto; 
    overflow-y: auto; 
    border: 1px solid #e2e8f0; 
    border-radius: 6px; 
    margin-bottom: 15px; 
}

.mini-table { 
    width: 100%; 
    border-collapse: collapse; 
    font-size: 13px; 
    min-width: 800px; 
}

.mini-table th, .mini-table td { 
    text-align: left; 
    padding: 12px 10px; 
    vertical-align: middle;
}

.mini-table th.text-center, .mini-table td.text-center { text-align: center; }

.mini-table th { 
    background: #f8fafc; 
    color: #334155; 
    border-bottom: 1px solid #e2e8f0; 
    position: sticky; 
    top: 0; 
    font-weight: 600; 
    z-index: 10;
}
.mini-table td { border-bottom: 1px solid #f1f5f9; }
.active-row { background-color: #eff6ff; }

.cust-name { font-weight: 500; color: #0f172a; display: block; }
.cust-info { font-size: 13px; color: #475569; }
.text-primary { color: #2563eb; }

.panel-footer { display: flex; justify-content: space-between; align-items: center; font-size: 13px; }
.mini-pagination { display: flex; align-items: center; gap: 10px; }
.mini-btn { width: 28px; height: 28px; border: 1px solid #e2e8f0; background: #fff; border-radius: 4px; cursor: pointer; }
.mini-btn:disabled { color: #ccc; cursor: not-allowed; background: #f9fafb; }
.mini-btn:hover:not(:disabled) { border-color: #2b4360; color: #2b4360; }

.segment-actions {
    display: flex;
    align-items: center;
    flex-wrap: wrap;
    gap: 10px;
    padding: 12px;
    background-color: #f8fafc;
    border-radius: 8px;
    border: 1px dashed #cbd5e1;
}
.btn-segment {
    padding: 6px 14px;
    border-radius: 20px;
    font-size: 12px;
    font-weight: 600;
    cursor: pointer;
    border: 1px solid #3b82f6;
    background-color: #eff6ff;
    color: #1d4ed8;
    transition: all 0.2s;
}
.btn-segment:hover {
    background-color: #3b82f6;
    color: #ffffff;
    box-shadow: 0 2px 6px rgba(59, 130, 246, 0.3);
}
.btn-clear {
    border-color: #ef4444;
    background-color: #fef2f2;
    color: #b91c1c;
    margin-left: auto; 
}
.btn-clear:hover {
    background-color: #ef4444;
    color: #ffffff;
    box-shadow: 0 2px 6px rgba(239, 68, 68, 0.3);
}
</style>