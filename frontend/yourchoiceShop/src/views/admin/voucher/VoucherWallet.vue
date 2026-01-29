<template>
  <div class="voucher-container">
    <div class="header-section">
      <h2 class="title">🎁 Kho Voucher của bạn</h2>
      <p class="subtitle">Lưu lại mã để sử dụng khi thanh toán nhé!</p>
    </div>

    <div class="voucher-list">
      <div 
        v-for="v in vouchers" 
        :key="v.id" 
        class="voucher-ticket"
        :class="{ 'expired': v.trangThai === 0 }"
      >
        <div class="ticket-left">
          <div class="discount-val">
            <span v-if="v.loaiPhieu === 'PhanTram'">{{ v.giaTriGiam }}%</span>
            <span v-else>{{ formatCurrency(v.giaTriGiam) }}</span>
          </div>
          <div class="discount-label">GIẢM GIÁ</div>
          <div class="badge-type" :class="v.kieu === 'CaNhan' ? 'badge-private' : 'badge-public'">
             {{ v.kieu === 'CaNhan' ? 'Riêng tư' : 'Công khai' }}
          </div>
        </div>

        <div class="ticket-sawtooth"></div>

        <div class="ticket-right">
          <div class="ticket-info">
            <h4 class="code-name">{{ v.tenPhieuGiamGia }}</h4>
            <div class="code-apply">Đơn tối thiểu: {{ formatCurrency(v.donHangToiThieu) }}</div>
            <div class="code-date">HSD: {{ formatDate(v.ngayKetThuc) }}</div>
          </div>
          
          <div class="ticket-action">
    <div class="code-text">{{ v.maPhieuGiamGia }}</div>
    
    <div class="action-buttons">
        <button 
            @click="sendToEmail(v)" 
            class="btn-icon" 
            title="Gửi về Email"
            :disabled="v.trangThai === 0"
        >
            <i class="fas fa-envelope"></i>
        </button>

        <button @click="copyCode(v.maPhieuGiamGia)" class="btn-copy" :disabled="v.trangThai === 0">
            {{ v.trangThai === 0 ? 'Hết hạn' : 'Sao chép' }}
        </button>
    </div>
</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';

// Mock Data (Sau này gọi API lấy về)
const vouchers = ref([
  {
    id: 1,
    maPhieuGiamGia: 'SALE50',
    tenPhieuGiamGia: 'Siêu sale giữa tháng',
    loaiPhieu: 'PhanTram',
    giaTriGiam: 50,
    donHangToiThieu: 200000,
    ngayKetThuc: '2025-12-31T00:00:00',
    kieu: 'CongKhai',
    trangThai: 1
  },
  {
    id: 2,
    maPhieuGiamGia: 'VIPMEMBER',
    tenPhieuGiamGia: 'Tri ân khách hàng thân thiết',
    loaiPhieu: 'TienMat',
    giaTriGiam: 100000,
    donHangToiThieu: 500000,
    ngayKetThuc: '2025-10-20T00:00:00',
    kieu: 'CaNhan',
    trangThai: 1
  },
  {
    id: 3,
    maPhieuGiamGia: 'EXPIRED22',
    tenPhieuGiamGia: 'Voucher tết 2024',
    loaiPhieu: 'PhanTram',
    giaTriGiam: 20,
    donHangToiThieu: 0,
    ngayKetThuc: '2024-01-01T00:00:00',
    kieu: 'CongKhai',
    trangThai: 0 // Hết hạn
  }
]);

// Hàm format tiền
const formatCurrency = (value) => {
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value);
};

// Hàm format ngày
const formatDate = (dateString) => {
  const date = new Date(dateString);
  return date.toLocaleDateString('vi-VN') + ' ' + date.toLocaleTimeString('vi-VN', {hour: '2-digit', minute:'2-digit'});
};

// Hàm copy
const copyCode = (code) => {
  navigator.clipboard.writeText(code);
  alert(`Đã sao chép mã: ${code}`);
};

const sendToEmail = async (voucher) => {
    // 1. Hỏi email người nhận (Nếu app có đăng nhập rồi thì lấy từ store, ở đây t dùng prompt cho nhanh)
    const email = prompt("Nhập email bạn muốn nhận mã:", "chi.example@gmail.com");
    
    if (!email) return; // Nếu ko nhập hoặc hủy thì thôi

    try {
        // 2. Gọi API Backend (Controller mày đã viết ở bước trước)
        // API: POST /api/v1/coupon/send-personal?email=...&code=...
        
        // Tạo FormData hoặc params tùy cách mày viết API, 
        // theo code Controller trước thì dùng params:
        await request.post('/coupon/send-personal', null, {
            params: {
                email: email,
                code: voucher.maPhieuGiamGia
            }
        });

        alert(`Đã gửi mã ${voucher.maPhieuGiamGia} tới ${email}. Check inbox (cả spam) nhé!`);

    } catch (error) {
        console.error(error);
        alert("Lỗi không gửi được mail: " + (error.response?.data || error.message));
    }
};
</script>

<style scoped>
/* Container chính */
.voucher-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 40px 20px;
  font-family: 'Segoe UI', sans-serif;
  background-color: #f8fafc;
  min-height: 100vh;
}

.header-section { margin-bottom: 30px; text-align: center; }
.title { color: #2b4360; font-weight: 800; margin-bottom: 5px; }
.subtitle { color: #64748b; font-size: 14px; }

/* Grid Layout */
.voucher-list {
  display: grid;
  grid-template-columns: 1fr;
  gap: 20px;
}

/* --- TICKET STYLING --- */
.voucher-ticket {
  display: flex;
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 15px rgba(0,0,0,0.05);
  transition: transform 0.2s;
  position: relative;
  height: 140px;
}
.voucher-ticket:hover { transform: translateY(-3px); box-shadow: 0 8px 25px rgba(0,0,0,0.1); }

/* Left Side (Màu xanh) */
.ticket-left {
  width: 140px;
  background: linear-gradient(135deg, #2b4360, #1e2f45); /* Màu Brand */
  color: #fff;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  position: relative;
  padding: 10px;
  text-align: center;
}
.discount-val { font-size: 28px; font-weight: 800; color: #eddcc3; /* Màu Vàng be */ }
.discount-label { font-size: 12px; letter-spacing: 1px; opacity: 0.9; margin-bottom: 8px; }

/* Badge (Công khai/Cá nhân) */
.badge-type { font-size: 10px; padding: 3px 8px; border-radius: 4px; font-weight: 600; text-transform: uppercase; }
.badge-public { background: rgba(255,255,255,0.2); color: #fff; }
.badge-private { background: #eddcc3; color: #2b4360; }

/* Đường cắt răng cưa (Dùng CSS Border) */
.ticket-sawtooth {
  width: 20px;
  background-image: radial-gradient(circle at 0 10px, transparent 6px, white 7px);
  background-size: 100% 20px; /* Chiều cao mỗi răng cưa */
  background-position: -10px 0;
  background-repeat: repeat-y;
  position: relative;
  z-index: 1;
  margin-left: -10px; /* Kéo đè lên phần xanh */
}
/* Hoặc đơn giản là Border Dashed */
.ticket-sawtooth {
    width: 0;
    border-left: 2px dashed #cbd5e1;
    margin: 10px 0;
}

/* Right Side (Thông tin) */
.ticket-right {
  flex: 1;
  padding: 20px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.code-name { margin: 0; color: #334155; font-size: 16px; font-weight: 700; }
.code-apply { font-size: 13px; color: #64748b; margin-top: 5px; }
.code-date { font-size: 12px; color: #94a3b8; margin-top: auto; }

/* Action Area */
.ticket-action {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 15px;
  background: #f1f5f9;
  padding: 8px 12px;
  border-radius: 8px;
}

.code-text { 
    font-weight: 700; 
    color: #2b4360; 
    font-family: monospace; 
    font-size: 15px; 
    letter-spacing: 1px; 
}

.action-buttons {
    display: flex;
    gap: 8px; /* Khoảng cách giữa nút mail và nút copy */
}

/* Style cho nút Icon Email */
.btn-icon {
    background: white;
    border: 1px solid #cbd5e1;
    color: #2b4360;
    width: 32px;
    height: 30px;
    border-radius: 6px;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: 0.2s;
}
.btn-icon:hover:not(:disabled) {
    background: #e0f2fe;
    color: #0284c7;
    border-color: #0284c7;
}

.btn-copy {
  background: #2b4360;
  color: white;
  border: none;
  padding: 6px 12px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 12px;
  font-weight: 600;
  transition: 0.2s;
  height: 30px; /* Cố định chiều cao cho bằng nút icon */
}
.btn-copy:hover { background: #1e2f45; }
.btn-copy:disabled, .btn-icon:disabled { background: #cbd5e1; cursor: not-allowed; color: white; border: none; }
/* Trạng thái hết hạn */
.voucher-ticket.expired { opacity: 0.6; filter: grayscale(1); }
.voucher-ticket.expired .ticket-left { background: #64748b; }

/* Responsive Mobile */
@media (max-width: 600px) {
    .voucher-ticket { flex-direction: column; height: auto; }
    .ticket-left { width: 100%; height: 80px; flex-direction: row; justify-content: space-between; padding: 0 20px; }
    .ticket-sawtooth { width: 100%; height: 0; border-left: none; border-top: 2px dashed #cbd5e1; margin: 0; }
    .discount-val { font-size: 24px; }
    .ticket-info { margin-bottom: 15px; }
}
</style>