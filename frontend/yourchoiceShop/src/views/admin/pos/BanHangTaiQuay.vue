<template>
  <div class="pos-container">
    <div class="left-panel">
      <div class="header-pos">
        <input 
        
          v-model="searchKeyword" 
          placeholder="Tìm kiếm sản phẩm (Tên, Mã)..." 
          class="search-input"
        />
        <button class="btn-scan"><i class="fas fa-qrcode"></i></button>
      </div>

      <div class="product-grid">
        <div 
          v-for="i in 8" :key="i" 
          class="product-card" 
          @click="addToCart({ id: i, name: 'Giày Sneaker Mẫu ' + i, price: 250000, img: '' })"
        >
          <div class="p-img">
            <img src="https://placehold.co/100x100?text=Giay" alt="sp">
          </div>
          <div class="p-info">
            <div class="p-name">Giày Sneaker Mẫu {{ i }}</div>
            <div class="p-price">250.000 ₫</div>
          </div>
        </div>
      </div>
    </div>

    <div class="right-panel">
      <div class="cart-header">
        <h3>Hóa đơn mới</h3>
        <span class="badge-date">{{ new Date().toLocaleString() }}</span>
      </div>

      <div class="cart-items">
        <div v-if="cart.length === 0" class="empty-cart">
          Chưa có sản phẩm nào
        </div>
        <div v-else v-for="(item, index) in cart" :key="index" class="cart-item">
          <div class="item-name">{{ item.name }}</div>
          <div class="item-control">
            <button @click="item.qty--">-</button>
            <span>{{ item.qty }}</span>
            <button @click="item.qty++">+</button>
          </div>
          <div class="item-price">{{ formatMoney(item.price * item.qty) }}</div>
          <button class="btn-remove" @click="cart.splice(index, 1)">×</button>
        </div>
      </div>

      <div class="payment-info">
        <div class="row">
          <span>Khách hàng:</span>
          <div class="customer-select">
            <i class="fas fa-user"></i> Khách lẻ
          </div>
        </div>
        <div class="divider"></div>
        <div class="row total-row">
          <span>Tổng tiền:</span>
          <span class="total-price">{{ formatMoney(totalPrice) }}</span>
        </div>
        
        <button class="btn-pay" @click="handlePayment">
          THANH TOÁN ({{ formatMoney(totalPrice) }})
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { createOrder } from '@/api/HoaDonApi'
const router = useRouter()
const searchKeyword = ref('')
const cart = ref([])

// Thêm vào giỏ
const addToCart = (product) => {
  const exist = cart.value.find(c => c.id === product.id)
  if (exist) {
    exist.qty++
  } else {
    cart.value.push({ ...product, qty: 1 })
  }
}

// Tính tổng tiền
const totalPrice = computed(() => {
  return cart.value.reduce((sum, item) => sum + (item.price * item.qty), 0)
})

const handlePayment = async () => {
  if (cart.value.length === 0) return alert("Giỏ hàng đang trống!");

  if (!confirm(`Xác nhận thanh toán ${formatMoney(totalPrice.value)}?`)) return;

  try {
    // Chuẩn bị dữ liệu gửi lên Server
    const payload = {
      tenKhachHang: "Khách lẻ", // Hoặc lấy từ biến customer đã chọn
      items: cart.value.map(item => ({
        idSanPham: item.id, // ID sản phẩm
        soLuong: item.qty,
        donGia: item.price
      }))
    };

    // Gọi API
    await createOrder(payload);

    alert("Thanh toán thành công! Đang chuyển về danh sách...");
    
    // Reset giỏ
    cart.value = [];
    
    // Chuyển hướng về trang quản lý đơn hàng
    router.push({ name: 'admin-order-list' }); // Đảm bảo tên route này đúng trong router/index.js

  } catch (error) {
    console.error(error);
    alert("Lỗi thanh toán: " + (error.response?.data || "Lỗi hệ thống"));
  }
}

const formatMoney = (val) => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val);
</script>

<style scoped>
.pos-container { display: flex; height: 100vh; background: #f3f4f6; }

/* PANEL TRÁI */
.left-panel { flex: 1; padding: 20px; overflow-y: auto; }
.header-pos { display: flex; gap: 10px; margin-bottom: 20px; }
.search-input { flex: 1; padding: 12px; border: 1px solid #ddd; border-radius: 8px; }
.search-input::placeholder{
    color: #000000 !important;  /* Màu đen */
    opacity: 1 !important;      /* Chống mờ */
    font-weight: 500;           /* Đậm lên tí cho dễ đọc (tùy chọn) */

}
.btn-scan { padding: 0 20px; background: white; border: 1px solid #ddd; border-radius: 8px; cursor: pointer; }

.product-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(180px, 1fr)); gap: 15px; }
.product-card { background: white; border-radius: 8px; padding: 10px; cursor: pointer; transition: 0.2s; box-shadow: 0 2px 4px rgba(0,0,0,0.05); }
.product-card:hover { transform: translateY(-3px); border: 1px solid #2563eb; }
.p-img img { width: 100%; border-radius: 6px; }
.p-name { font-size: 14px; font-weight: 600; margin: 8px 0 4px; }
.p-price { color: #dc2626; font-weight: bold; }

/* PANEL PHẢI */
.right-panel { width: 400px; background: white; display: flex; flex-direction: column; border-left: 1px solid #ddd; }
.cart-header { padding: 20px; border-bottom: 1px solid #eee; display: flex; justify-content: space-between; align-items: center; }
.cart-items { flex: 1; overflow-y: auto; padding: 15px; }

.cart-item { display: flex; justify-content: space-between; align-items: center; margin-bottom: 15px; padding-bottom: 10px; border-bottom: 1px dashed #eee; }
.item-name { flex: 1; font-weight: 500; font-size: 14px; }
.item-control { display: flex; align-items: center; gap: 8px; margin: 0 10px; }
.item-control button { width: 24px; height: 24px; border: 1px solid #ddd; background: white; border-radius: 4px; cursor: pointer; }
.item-price { font-weight: bold; font-size: 14px; color: #333; }
.btn-remove { border: none; background: none; color: #999; cursor: pointer; font-size: 18px; margin-left: 10px; }

.empty-cart { text-align: center; color: #999; margin-top: 50px; }

/* THANH TOÁN */
.payment-info { padding: 20px; background: #f8fafc; border-top: 1px solid #eee; }
.row { display: flex; justify-content: space-between; margin-bottom: 10px; font-size: 14px; }
.customer-select { color: #2563eb; font-weight: 600; cursor: pointer; }
.divider { height: 1px; background: #ddd; margin: 10px 0; }
.total-row { font-size: 18px; font-weight: bold; color: #1e293b; }
.total-price { color: #dc2626; }

.btn-pay { width: 100%; padding: 15px; background: #2563eb; color: white; border: none; border-radius: 8px; font-size: 16px; font-weight: bold; cursor: pointer; margin-top: 10px; }
.btn-pay:hover { background: #1d4ed8; }
</style>