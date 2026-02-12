<template>
  <div class="page-container">
    <div class="page-header">
       <h1>KHO VOUCHER</h1>
       <p>Săn ngay mã giảm giá hấp dẫn dành cho bạn</p>
    </div>

    <div v-if="loading" class="loading-state">
        <div class="spinner"></div>
    </div>

    <div v-else class="coupon-grid">
       <div v-for="vc in vouchers" :key="vc.id" class="coupon-ticket">
          
          <div class="coupon-left">
             <div class="c-logo">YC</div>
             <div class="c-type">VOUCHER</div>
          </div>

          <div class="coupon-right">
             <h3 class="discount-text">{{ vc.tieuDe }}</h3>
             <p class="condition">Đơn tối thiểu {{ formatMoney(vc.donToiThieu) }}</p>
             
             <div class="c-code-row">
                <span>Mã:</span>
                <strong class="code-text">{{ vc.maVoucher }}</strong>
             </div>
             
             <div class="c-expiry">HSD: {{ formatDate(vc.ngayKetThuc) }}</div>
             
             <button class="btn-save" @click="copyCode(vc.maVoucher)">
                <i class="fas fa-copy"></i> Lưu mã
             </button>
          </div>

          <div class="circle-top"></div>
          <div class="circle-bottom"></div>
          <div class="dashed-line"></div>
       </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import Swal from 'sweetalert2';

const loading = ref(false);
const vouchers = ref([]);

// --- MOCK DATA (Thay bằng API call nếu có) ---
const fetchVouchers = async () => {
    loading.value = true;
    try {
        // Giả lập API trả về
        setTimeout(() => {
            vouchers.value = [
                { id: 1, maVoucher: 'WELCOME2026', tieuDe: 'Giảm 50K', donToiThieu: 0, ngayKetThuc: '2026-12-31' },
                { id: 2, maVoucher: 'FREESHIP', tieuDe: 'Miễn Phí Vận Chuyển', donToiThieu: 300000, ngayKetThuc: '2026-06-30' },
                { id: 3, maVoucher: 'SALE20', tieuDe: 'Giảm 20%', donToiThieu: 500000, ngayKetThuc: '2026-05-01' },
                { id: 4, maVoucher: 'VIPMEMBER', tieuDe: 'Giảm 100K', donToiThieu: 1000000, ngayKetThuc: '2026-12-31' },
                { id: 5, maVoucher: 'TET2026', tieuDe: 'Lì Xì 200K', donToiThieu: 2000000, ngayKetThuc: '2026-02-28' },
                { id: 6, maVoucher: 'HOANXU', tieuDe: 'Hoàn 10% Xu', donToiThieu: 150000, ngayKetThuc: '2026-03-15' }
            ];
            loading.value = false;
        }, 500);
    } catch (e) {
        loading.value = false;
    }
};

// --- UTILS ---
const formatMoney = (val) => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val);
const formatDate = (dateStr) => {
    const d = new Date(dateStr);
    return d.toLocaleDateString('vi-VN');
};

const copyCode = (code) => {
    navigator.clipboard.writeText(code);
    Swal.fire({
        icon: 'success',
        title: 'Đã sao chép mã!',
        text: code,
        showConfirmButton: false,
        timer: 1000
    });
};

onMounted(() => {
    fetchVouchers();
});
</script>

<style scoped>
/* GLOBAL FONT */
.page-container { 
    max-width: 1280px; margin: 0 auto; padding: 60px 20px; 
    font-family: Arial, sans-serif; color: #333;
}

/* HEADER */
.page-header { text-align: center; margin-bottom: 50px; }
.page-header h1 { font-size: 36px; color: #0f172a; margin-bottom: 10px; font-weight: 800; }
.page-header p { font-size: 16px; color: #64748b; }

/* LOADING */
.loading-state { display: flex; justify-content: center; height: 300px; align-items: center; }
.spinner { border: 4px solid #f3f3f3; border-top: 4px solid #1e3a8a; border-radius: 50%; width: 40px; height: 40px; animation: spin 1s linear infinite; }
@keyframes spin { 0% { transform: rotate(0deg); } 100% { transform: rotate(360deg); } }

/* GRID LAYOUT */
.coupon-grid { 
    display: grid; grid-template-columns: repeat(3, 1fr); gap: 30px; 
}

/* COUPON TICKET STYLING */
.coupon-ticket { 
    display: flex; background: white; border-radius: 12px; 
    position: relative; overflow: hidden; 
    box-shadow: 0 4px 15px rgba(0,0,0,0.05); height: 160px;
    border: 1px solid #e2e8f0; transition: 0.3s;
}
.coupon-ticket:hover { transform: translateY(-5px); box-shadow: 0 10px 25px rgba(0,0,0,0.1); }

/* Left Side (Blue Gradient) */
.coupon-left { 
    width: 110px; 
    background: linear-gradient(135deg, #1e3a8a 0%, #0f172a 100%); 
    color: white; display: flex; flex-direction: column; align-items: center; justify-content: center;
    position: relative; z-index: 1;
}
.c-logo { font-size: 28px; font-weight: 800; letter-spacing: 2px; margin-bottom: 5px; }
.c-type { font-size: 10px; letter-spacing: 1px; opacity: 0.8; }

/* Right Side (Info) */
.coupon-right { flex: 1; padding: 20px 20px 20px 30px; position: relative; display: flex; flex-direction: column; justify-content: center; }
.discount-text { font-size: 22px; color: #ef4444; margin: 0 0 5px; font-weight: 700; }
.condition { font-size: 13px; color: #64748b; margin-bottom: 10px; }

.c-code-row { font-size: 14px; margin-bottom: 5px; color: #334155; }
.code-text { background: #f1f5f9; padding: 2px 8px; border-radius: 4px; font-family: monospace; color: #0f172a; }

.c-expiry { font-size: 12px; color: #94a3b8; }

/* Button Save */
.btn-save { 
    position: absolute; bottom: 20px; right: 20px; 
    background: #0f172a; color: white; border: none; 
    padding: 8px 16px; border-radius: 6px; cursor: pointer;
    font-size: 12px; font-weight: 600; transition: 0.2s;
    display: flex; align-items: center; gap: 6px;
}
.btn-save:hover { background: #1e3a8a; }

/* DECORATION (Vết cắt & Nét đứt) */
.circle-top, .circle-bottom { 
    width: 24px; height: 24px; background: #fff; border-radius: 50%; 
    position: absolute; left: 98px; z-index: 10;
    border: 1px solid #e2e8f0;
}
.circle-top { top: -14px; border-bottom-color: transparent; }
.circle-bottom { bottom: -14px; border-top-color: transparent; }

.dashed-line {
    position: absolute; left: 109px; top: 15px; bottom: 15px;
    border-left: 2px dashed #e2e8f0; z-index: 5;
}

/* RESPONSIVE */
@media (max-width: 992px) {
    .coupon-grid { grid-template-columns: repeat(2, 1fr); }
}
@media (max-width: 600px) {
    .coupon-grid { grid-template-columns: 1fr; }
    .page-header h1 { font-size: 28px; }
}
</style>