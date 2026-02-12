<template>
  <div class="page-container">
    
    <div class="header-section">
      <div class="header-left">
        <h1 class="page-title">Chi tiết đơn hàng</h1>
        <div class="sub-info" v-if="!loading">
          <span>Mã đơn hàng: <strong class="text-primary">{{ order.maHoaDon }}</strong></span>
          <span class="divider">|</span>
          <span class="text-gray">Ngày tạo: {{ formatDate(order.ngayTao) }}</span>
        </div>
      </div>
      <div class="header-actions">
         <button class="btn btn-outline" @click="$router.push('/admin/orders')">
            <font-awesome-icon :icon="['fas', 'arrow-left']" /> Quay lại danh sách
         </button>
      </div>
    </div>

    <div v-if="loading" class="loading-state">
        <font-awesome-icon :icon="['fas', 'spinner']" spin size="2x" />
        <p>Đang tải thông tin đơn hàng...</p>
    </div>

    <div v-else class="detail-grid">
      
      <div class="col-main">
        
        <div class="card status-card">
           <div class="card-header-icon">
              <i class="fas fa-truck-fast"></i> <span>Trạng thái đơn hàng</span>
           </div>
           <div class="timeline-wrapper">
              <div class="progress-track">
                 <div class="progress-bar" :style="{ width: getProgressWidth(order.trangThai) }"></div>
              </div>
              <div class="steps-container">
                 <div 
                    v-for="(step, index) in steps" 
                    :key="index"
                    class="step-item"
                    :class="{ 'active': index <= getCurrentStepIndex(order.trangThai) }"
                 >
                    <div class="step-icon">
                       <i :class="step.icon"></i>
                    </div>
                    <div class="step-label">{{ step.label }}</div>
                 </div>
              </div>
           </div>
        </div>

        <div class="info-row">
           <div class="card info-card">
              <div class="card-header-icon">
                 <i class="fas fa-user"></i> <span>Thông tin khách hàng</span>
              </div>
              <div class="info-body">
                 <div class="info-line">
                    <span class="label">Tên khách hàng</span>
                    <span class="value">{{ order.tenKhachHang || 'Khách lẻ' }}</span>
                 </div>
                 <div class="info-line">
                    <span class="label">Số điện thoại</span>
                    <span class="value">{{ order.sdtKhachHang || '-' }}</span>
                 </div>
                 <div class="info-line">
                    <span class="label">Email</span>
                    <span class="value">{{ order.emailKhachHang || '-' }}</span>
                 </div>
              </div>
           </div>

           <div class="card info-card">
              <div class="card-header-icon">
                 <i class="fas fa-map-marker-alt"></i> <span>Thông tin giao hàng</span>
              </div>
              <div class="info-body">
                 <div class="info-line">
                    <span class="label">Địa chỉ</span>
                    <span class="value truncate-2">{{ order.diaChiGiaoHang || 'Tại quầy' }}</span>
                 </div>
                 <div class="info-line">
                    <span class="label">Loại đơn</span>
                    <span class="value fw-bold text-primary">{{ order.loaiHoaDon }}</span>
                 </div>
                 <div class="info-line">
                    <span class="label">Ghi chú</span>
                    <span class="value text-gray f-italic">{{ order.ghiChu || 'Không có ghi chú' }}</span>
                 </div>
              </div>
           </div>
        </div>

        <div class="card product-card">
           <div class="card-header-icon">
              <i class="fas fa-box-open"></i> <span>Danh sách sản phẩm ({{ order.chiTietHoaDonList?.length || 0 }})</span>
           </div>
           <div class="table-responsive">
              <table class="custom-table">
                 <thead>
                    <tr>
                       <th width="5%" class="text-center">STT</th>
                       <th>Tên sản phẩm</th>
                       <th class="text-center">Kích cỡ</th>
                       <th class="text-center">Màu sắc</th>
                       <th width="10%" class="text-center">Số lượng</th>
                       <th width="15%" class="text-right">Đơn giá</th>
                       <th width="15%" class="text-right">Thành tiền</th>
                    </tr>
                 </thead>
                 <tbody>
                    <tr v-for="(item, index) in order.chiTietHoaDonList" :key="index">
                       <td class="text-center">{{ index + 1 }}</td>
                       <td class="fw-bold text-navy">{{ item.tenSanPham }}</td>
                       <td class="text-center"><span class="badge-attr">{{ item.tenKichThuoc }}</span></td>
                       <td class="text-center"><span class="badge-attr">{{ item.tenMauSac }}</span></td>
                       <td class="text-center fw-bold">{{ item.soLuong }}</td>
                       <td class="text-right">{{ formatMoney(item.donGia) }}</td>
                       <td class="text-right text-price">{{ formatMoney(item.donGia * item.soLuong) }}</td>
                    </tr>
                 </tbody>
              </table>
           </div>
        </div>

      </div>

      <div class="col-sidebar">
         
         <div class="card summary-card">
            <div class="card-header-icon">
               <i class="fas fa-file-invoice-dollar"></i> <span>Tổng kết thanh toán</span>
            </div>
            <div class="summary-body">
               <div class="summary-row">
                  <span>Tổng tiền hàng</span>
                  <span>{{ formatMoney(order.tongTienHang) }}</span>
               </div>
               <div class="summary-row">
                  <span>Giảm giá</span>
                  <span class="text-green">- {{ formatMoney(order.tienGiam) }}</span>
               </div>
               <div class="summary-row">
                  <span>Phí vận chuyển</span>
                  <span>+ {{ formatMoney(order.phiVanChuyen) }}</span>
               </div>
               <div class="summary-divider"></div>
               <div class="summary-row total-row">
                  <span>TỔNG TIỀN</span>
                  <span class="total-price">{{ formatMoney(order.tongTienSauGiam) }}</span>
               </div>
            </div>
         </div>

         <div class="card history-card">
            <div class="card-header-icon">
               <i class="fas fa-history"></i> <span>Lịch sử thanh toán</span>
            </div>
            <div class="history-body">
               <div v-if="!order.lichSuThanhToanList || order.lichSuThanhToanList.length === 0" class="empty-history">
                  Chưa có lịch sử thanh toán
               </div>
               <div v-else class="history-list">
                  <div v-for="(hist, hIdx) in order.lichSuThanhToanList" :key="hIdx" class="history-item">
                     <span class="dot"></span>
                     <div class="h-info">
                        <span class="h-date">{{ formatDate(hist.ngayThanhToan) }}</span>
                        <span class="h-desc">{{ hist.ghiChu || 'Thanh toán' }}</span>
                     </div>
                     <span class="h-amount">{{ formatMoney(hist.soTien) }}</span>
                  </div>
               </div>
            </div>
         </div>

         <div class="action-buttons-col">
            <button class="btn btn-blue-block" @click="printOrder">
               <i class="fas fa-print"></i> In hóa đơn
            </button>
            <button class="btn btn-orange-block" @click="openEditOrder">
               <i class="fas fa-edit"></i> Chỉnh sửa đơn hàng
            </button>
            
            <button v-if="order.trangThai === 1 || order.trangThai === 2" class="btn btn-red-outline-block" @click="cancelOrder">
                Hủy đơn hàng
            </button>
         </div>

      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import Swal from 'sweetalert2';
import request from '@/services/request'; // Import request service
import { toastSuccess, toastError } from '@/utils/toast';

const route = useRoute();
const router = useRouter();
const orderId = route.params.id; // Lấy ID từ URL (VD: /orders/HD0001 -> orderId = HD0001)

const loading = ref(true);
const order = ref({});

// --- API FETCH DATA ---
const fetchOrderDetail = async () => {
    loading.value = true;
    try {
        // Gọi API lấy chi tiết (Thay đổi endpoint nếu BE của bạn khác)
        // Ví dụ: GET /api/v1/hoa-don/detail/HD0001 hoặc /api/v1/hoa-don/HD0001
        const res = await request.get(`/hoa-don/detail/${orderId}`);
        order.value = res.data;
    } catch (error) {
        console.error("Lỗi tải đơn hàng:", error);
        toastError("Không tìm thấy thông tin đơn hàng!");
        router.push('/admin/orders'); // Quay về danh sách nếu lỗi
    } finally {
        loading.value = false;
    }
};

// --- TIMELINE SETUP ---
const steps = [
    { label: 'Chờ xác nhận', icon: 'fas fa-clipboard-list' },
    { label: 'Chờ giao hàng', icon: 'fas fa-box' },
    { label: 'Đang giao', icon: 'fas fa-shipping-fast' },
    { label: 'Hoàn thành', icon: 'fas fa-check-circle' }
];

const getCurrentStepIndex = (status) => {
    if (status === 0 || status === 6) return -1; // Đã hủy / Hoàn trả
    if (status >= 1 && status <= 4) return status - 1;
    if (status === 5) return 3; // Đã thanh toán -> coi như hoàn thành quy trình
    return 0; 
};

const getProgressWidth = (status) => {
    const idx = getCurrentStepIndex(status);
    if(idx === -1) return '0%';
    const percent = (idx / (steps.length - 1)) * 100;
    return `${percent}%`;
};

// --- UTILS ---
const formatMoney = (val) => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val || 0);
const formatDate = (val) => {
    if (!val) return '';
    return new Date(val).toLocaleString('vi-VN', { hour: '2-digit', minute:'2-digit', day: '2-digit', month: '2-digit', year: 'numeric' });
};

// --- ACTIONS ---
const printOrder = () => {
    // Gọi API xuất PDF hóa đơn
    window.open(`http://localhost:8080/api/v1/hoa-don/print/${orderId}`, '_blank');
};

const openEditOrder = () => {
    // Chuyển sang trang chỉnh sửa hoặc mở modal
    Swal.fire('Chức năng đang phát triển', 'Bạn sẽ sớm có thể sửa đơn hàng tại đây', 'info');
};

const cancelOrder = async () => {
    const res = await Swal.fire({
        title: 'Hủy đơn hàng?',
        text: 'Bạn có chắc chắn muốn hủy đơn hàng này không? Hành động này không thể hoàn tác.',
        icon: 'warning',
        showCancelButton: true,
        confirmButtonText: 'Đồng ý hủy',
        cancelButtonText: 'Quay lại',
        confirmButtonColor: '#ef4444'
    });

    if (res.isConfirmed) {
        try {
            await request.put(`/hoa-don/cancel/${orderId}`, null, {
                params: { lyDo: 'Admin hủy đơn' }
            });
            toastSuccess('Đã hủy đơn hàng thành công');
            fetchOrderDetail(); // Tải lại dữ liệu mới nhất
        } catch (e) {
            toastError(e.response?.data?.message || 'Lỗi khi hủy đơn hàng');
        }
    }
}

onMounted(() => {
    if (orderId) {
        fetchOrderDetail();
    } else {
        toastError("Mã đơn hàng không hợp lệ");
        router.push('/admin/orders');
    }
});
</script>

<style scoped>
/* GENERAL */
.page-container { padding: 20px; font-family: 'Segoe UI', sans-serif; background: #f8fafc; min-height: 100vh; color: #334155; }

/* LOADING */
.loading-state { 
    display: flex; flex-direction: column; align-items: center; justify-content: center; 
    height: 400px; color: #64748b; gap: 15px; 
}

/* HEADER */
.header-section { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 24px; }
.page-title { margin: 0 0 5px 0; font-size: 24px; font-weight: 700; color: #1e293b; }
.sub-info { font-size: 14px; color: #64748b; display: flex; align-items: center; gap: 8px; }
.divider { color: #cbd5e1; }
.text-primary { color: #2563eb; }

/* BUTTONS HEADER */
.btn { height: 38px; padding: 0 16px; border-radius: 6px; font-weight: 600; cursor: pointer; border: 1px solid transparent; display: inline-flex; align-items: center; gap: 8px; font-size: 13px; transition: 0.2s; }
.btn-outline { background: #fff; border-color: #cbd5e1; color: #475569; }
.btn-outline:hover { background: #f1f5f9; border-color: #94a3b8; color: #0f172a; }

/* LAYOUT GRID */
.detail-grid { display: grid; grid-template-columns: 2.5fr 1fr; gap: 20px; }

/* CARD GLOBAL STYLE */
.card { 
    background: #fff; border: 1px solid #bfdbfe; border-radius: 12px; 
    box-shadow: 0 4px 12px rgba(0,0,0,0.03); margin-bottom: 20px; overflow: hidden;
}
.card-header-icon { 
    background: #f8fafc; padding: 12px 16px; border-bottom: 1px solid #e2e8f0; 
    font-weight: 700; color: #334155; display: flex; align-items: center; gap: 8px; 
}
.card-header-icon i { color: #64748b; }

/* --- CỘT TRÁI --- */

/* 1. TIMELINE */
.status-card { padding-bottom: 20px; }
.timeline-wrapper { padding: 30px 40px 10px 40px; position: relative; }
.progress-track { 
    position: absolute; top: 45px; left: 10%; right: 10%; height: 4px; background: #e2e8f0; z-index: 1; border-radius: 4px;
}
.progress-bar { height: 100%; background: #10b981; transition: width 0.5s ease; border-radius: 4px; }

.steps-container { display: flex; justify-content: space-between; position: relative; z-index: 2; }
.step-item { display: flex; flex-direction: column; align-items: center; gap: 8px; color: #94a3b8; width: 80px; }
.step-icon { 
    width: 36px; height: 36px; border-radius: 50%; background: #fff; border: 2px solid #e2e8f0; 
    display: flex; align-items: center; justify-content: center; font-size: 14px; transition: 0.3s; 
}
.step-label { font-size: 12px; font-weight: 600; text-align: center; }

/* Active Step */
.step-item.active .step-icon { border-color: #10b981; background: #10b981; color: #fff; box-shadow: 0 0 0 4px rgba(16, 185, 129, 0.1); }
.step-item.active .step-label { color: #10b981; }

/* 2. INFO CARDS (CUSTOMER & SHIPPING) */
.info-row { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; margin-bottom: 20px; }
.info-body { padding: 16px; display: flex; flex-direction: column; gap: 12px; }
.info-line { display: flex; justify-content: space-between; font-size: 13px; }
.info-line .label { color: #64748b; width: 100px; flex-shrink: 0; }
.info-line .value { color: #1e293b; font-weight: 500; text-align: right; }
.truncate-2 { display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.f-italic { font-style: italic; }

/* 3. PRODUCT TABLE */
.table-responsive { width: 100%; overflow-x: auto; }
.custom-table { width: 100%; border-collapse: collapse; }
.custom-table th { background: #eff6ff; padding: 12px; font-size: 12px; color: #1e40af; border-bottom: 1px solid #bfdbfe; font-weight: 700; white-space: nowrap; }
.custom-table td { padding: 12px; border-bottom: 1px solid #f1f5f9; font-size: 13px; color: #334155; vertical-align: middle; }
.text-navy { color: #1e293b; }
.text-price { color: #ef4444; font-weight: 600; }
.badge-attr { background: #f1f5f9; padding: 4px 8px; border-radius: 4px; font-size: 11px; border: 1px solid #e2e8f0; color: #475569; }

/* --- CỘT PHẢI --- */

/* SUMMARY */
.summary-body { padding: 16px; }
.summary-row { display: flex; justify-content: space-between; margin-bottom: 10px; font-size: 13px; color: #475569; }
.summary-divider { height: 1px; background: #e2e8f0; margin: 15px 0; }
.total-row { color: #0f172a; font-weight: 700; font-size: 15px; margin-bottom: 0; align-items: center; }
.total-price { color: #ef4444; font-size: 18px; }
.text-green { color: #10b981; }

/* HISTORY */
.history-body { padding: 16px; min-height: 100px; }
.empty-history { color: #94a3b8; font-style: italic; text-align: center; font-size: 13px; margin-top: 20px; }
.history-item { display: flex; align-items: center; gap: 10px; margin-bottom: 12px; font-size: 13px; }
.history-item .dot { width: 8px; height: 8px; background: #10b981; border-radius: 50%; }
.h-info { flex: 1; display: flex; flex-direction: column; }
.h-date { color: #64748b; font-size: 11px; }
.h-desc { font-weight: 500; }
.h-amount { font-weight: 700; color: #0f172a; }

/* ACTION BUTTONS COLUMN */
.action-buttons-col { display: flex; flex-direction: column; gap: 12px; }
.btn-blue-block { 
    width: 100%; padding: 10px; background: #3b82f6; color: white; border: none; border-radius: 8px; 
    font-weight: 600; cursor: pointer; transition: 0.2s; display: flex; align-items: center; justify-content: center; gap: 8px;
}
.btn-blue-block:hover { background: #2563eb; }

.btn-orange-block { 
    width: 100%; padding: 10px; background: #f97316; color: white; border: none; border-radius: 8px; 
    font-weight: 600; cursor: pointer; transition: 0.2s; display: flex; align-items: center; justify-content: center; gap: 8px;
}
.btn-orange-block:hover { background: #ea580c; }

.btn-red-outline-block {
    width: 100%; padding: 8px; background: #fff; color: #ef4444; border: 1px solid #ef4444; border-radius: 8px;
    font-weight: 600; cursor: pointer; transition: 0.2s;
}
.btn-red-outline-block:hover { background: #fef2f2; }

/* UTILS */
.text-center { text-align: center; }
.text-right { text-align: right; }
.fw-bold { font-weight: 700; }
</style>