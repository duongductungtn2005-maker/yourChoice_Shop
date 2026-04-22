<template>
  <div class="cart-page">
    <div class="page-header-small">
      <div class="header-overlay"></div>
      <div class="container header-content">
        <h1>GIỎ HÀNG</h1>
        <div class="breadcrumb">
          <span @click="$router.push('/')">Trang chủ</span> / <span>Giỏ hàng</span>
        </div>
      </div>
    </div>

    <div class="container cart-content">
      <!-- Giỏ trống -->
      <div v-if="cartStore.items.length === 0" class="empty-cart">
        <i class="fas fa-shopping-bag"></i>
        <h3>Giỏ hàng trống</h3>
        <p>Bạn chưa thêm sản phẩm nào vào giỏ hàng.</p>
        <button class="btn-continue" @click="$router.push('/products')">TIẾP TỤC MUA SẮM</button>
      </div>

      <!-- Có sản phẩm -->
      <template v-else>
        <div class="cart-grid">
          <!-- Bảng giỏ hàng -->
          <div class="cart-table-wrap">
            <div class="cart-header-row">
              <span class="col-product">Sản phẩm</span>
              <span class="col-price">Đơn giá</span>
              <span class="col-qty">Số lượng</span>
              <span class="col-total">Thành tiền</span>
              <span class="col-action"></span>
            </div>

            <div class="cart-item" v-for="item in cartStore.items" :key="item.variantId">
              <div class="col-product">
                <img :src="item.anh" alt="" @error="handleImgError" class="item-img" />
                <div class="item-info">
                  <h4 @click="$router.push(`/product/${item.productId}`)">{{ item.tenSanPham }}</h4>
                  <span class="variant-label">{{ item.mauSac }} / {{ item.kichThuoc }}</span>
                </div>
              </div>
              <div class="col-price">
                <span>{{ formatMoney(item.donGia) }}</span>
                <span v-if="item.giaGoc" style="text-decoration: line-through; color: #999; font-size: 12px; display: block;">{{ formatMoney(item.giaGoc) }}</span>
                <span v-if="item.phanTramGiam" style="color: #d32f2f; font-size: 12px; font-weight: 600;">-{{ item.phanTramGiam }}%</span>
              </div>
              <div class="col-qty">
                <div class="qty-control">
                  <button @click="changeQty(item.variantId, item.soLuong - 1)">-</button>
                  <input type="number" :value="item.soLuong" min="1" @change="onQtyInput($event, item.variantId)" />
                  <button @click="changeQty(item.variantId, item.soLuong + 1)">+</button>
                </div>
              </div>
              <div class="col-total">{{ formatMoney(item.donGia * item.soLuong) }}</div>
              <div class="col-action">
                <button class="btn-remove" @click="removeItem(item.variantId)" title="Xóa">
                  <i class="fas fa-trash-alt"></i>
                </button>
              </div>
            </div>

            <div class="cart-actions">
              <button class="btn-clear" @click="clearAll"><i class="fas fa-trash"></i> Xóa tất cả</button>
              <button class="btn-continue-shopping" @click="$router.push('/products')"><i class="fas fa-arrow-left"></i> Tiếp tục mua sắm</button>
            </div>
          </div>

          <!-- Tổng kết -->
          <div class="cart-summary">
            <h3>TÓM TẮT ĐƠN HÀNG</h3>
            <div class="summary-row">
              <span>Tạm tính ({{ cartStore.totalItems }} sản phẩm)</span>
              <span>{{ formatMoney(cartStore.totalMoney) }}</span>
            </div>
            <div class="summary-row">
              <span>Phí vận chuyển</span>
              <span class="shipping-note">Tính khi thanh toán</span>
            </div>
            <div class="summary-divider"></div>
            <div class="summary-row total-row">
              <span>TỔNG CỘNG</span>
              <span class="total-price">{{ formatMoney(cartStore.totalMoney) }}</span>
            </div>
            <button class="btn-checkout" @click="goCheckout">
              TIẾN HÀNH THANH TOÁN
            </button>
            <div class="payment-methods">
              <span>Chấp nhận thanh toán:</span>
              <div class="methods-icons">
                <span title="COD"><i class="fas fa-money-bill-wave"></i></span>
                <span title="Banking"><i class="fas fa-university"></i></span>
                <span title="MoMo"><i class="fas fa-wallet"></i></span>
              </div>
            </div>
          </div>
        </div>
      </template>
    </div>
  </div>
</template>

<script setup>
import { useCartStore } from '@/stores/cart'
import { useRouter } from 'vue-router'
import Swal from 'sweetalert2'

const cartStore = useCartStore()
const router = useRouter()

const formatMoney = (val) => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val || 0)

const changeQty = (variantId, qty) => {
  if (qty < 1) return
  cartStore.updateQuantity(variantId, qty)
}

const onQtyInput = (e, variantId) => {
  const val = parseInt(e.target.value)
  if (val > 0) cartStore.updateQuantity(variantId, val)
}

const removeItem = (variantId) => {
  Swal.fire({
    title: 'Xóa sản phẩm?',
    text: 'Sản phẩm sẽ bị xóa khỏi giỏ hàng',
    icon: 'warning',
    showCancelButton: true,
    confirmButtonColor: '#ef4444',
    confirmButtonText: 'Xóa',
    cancelButtonText: 'Hủy',
  }).then((result) => {
    if (result.isConfirmed) cartStore.removeItem(variantId)
  })
}

const clearAll = () => {
  Swal.fire({
    title: 'Xóa tất cả?',
    text: 'Toàn bộ giỏ hàng sẽ bị xóa',
    icon: 'warning',
    showCancelButton: true,
    confirmButtonColor: '#ef4444',
    confirmButtonText: 'Xóa tất cả',
    cancelButtonText: 'Hủy',
  }).then((result) => {
    if (result.isConfirmed) cartStore.clearCart()
  })
}

const goCheckout = () => {
  if (cartStore.items.length === 0) return
  router.push('/checkout')
}

const handleImgError = (e) => { e.target.src = 'https://placehold.co/100x120?text=No+Img' }
</script>

<style scoped>
.cart-page { font-family: Arial, sans-serif; color: #333; }
.container { max-width: 1280px; margin: 0 auto; padding: 0 20px; box-sizing: border-box; }

.page-header-small {
  background-image: url('https://images.unsplash.com/photo-1441984904996-e0b6ba687e04?w=1600&q=80');
  background-size: cover; background-position: center; height: 180px; position: relative;
  display: flex; align-items: center; justify-content: center; margin-bottom: 40px;
}
.header-overlay { position: absolute; inset: 0; background: rgba(0,0,0,0.5); }
.header-content { position: relative; z-index: 1; text-align: center; color: #fff; }
.header-content h1 { font-size: 32px; font-weight: 700; margin-bottom: 8px; }
.breadcrumb { font-size: 14px; color: #e2e8f0; }
.breadcrumb span { cursor: pointer; }
.breadcrumb span:first-child:hover { text-decoration: underline; }

/* Empty cart */
.empty-cart { text-align: center; padding: 80px 20px; }
.empty-cart i { font-size: 60px; color: #cbd5e1; margin-bottom: 20px; }
.empty-cart h3 { font-size: 24px; color: #0f172a; margin-bottom: 10px; }
.empty-cart p { color: #64748b; margin-bottom: 30px; }
.btn-continue { padding: 14px 40px; background: #0f172a; color: #fff; border: none; font-weight: 700; font-size: 15px; border-radius: 6px; cursor: pointer; transition: 0.2s; }
.btn-continue:hover { background: #1e3a8a; }

/* Cart grid */
.cart-content { margin-bottom: 60px; }
.cart-grid { display: grid; grid-template-columns: 1fr 360px; gap: 40px; }

/* Cart table */
.cart-table-wrap { min-width: 0; }
.cart-header-row { display: grid; grid-template-columns: 2fr 1fr 1fr 1fr 50px; gap: 15px; padding: 12px 0; border-bottom: 2px solid #0f172a; font-weight: 700; font-size: 13px; text-transform: uppercase; color: #64748b; }
.cart-item { display: grid; grid-template-columns: 2fr 1fr 1fr 1fr 50px; gap: 15px; padding: 20px 0; border-bottom: 1px solid #f1f5f9; align-items: center; }
.col-product { display: flex; gap: 15px; align-items: center; min-width: 0; }
.item-img { width: 80px; height: 100px; object-fit: cover; border-radius: 6px; border: 1px solid #f1f5f9; flex-shrink: 0; }
.item-info { min-width: 0; }
.item-info h4 { font-size: 15px; font-weight: 600; color: #334155; margin: 0 0 5px; cursor: pointer; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.item-info h4:hover { color: #1e3a8a; }
.variant-label { font-size: 13px; color: #94a3b8; }
.col-price { font-weight: 600; font-size: 15px; color: #334155; }
.col-total { font-weight: 700; font-size: 15px; color: #0f172a; }
.col-action { text-align: center; }

.qty-control { display: flex; align-items: center; border: 1px solid #e2e8f0; border-radius: 6px; width: fit-content; }
.qty-control button { width: 32px; height: 32px; border: none; background: #f8fafc; cursor: pointer; font-size: 14px; font-weight: 600; }
.qty-control button:hover { background: #e2e8f0; }
.qty-control input { width: 40px; height: 32px; text-align: center; border: none; border-left: 1px solid #e2e8f0; border-right: 1px solid #e2e8f0; outline: none; font-weight: 600; font-size: 14px; -moz-appearance: textfield; }
.qty-control input::-webkit-outer-spin-button,
.qty-control input::-webkit-inner-spin-button { -webkit-appearance: none; margin: 0; }

.btn-remove { background: none; border: none; color: #94a3b8; cursor: pointer; font-size: 16px; transition: 0.2s; padding: 5px; }
.btn-remove:hover { color: #ef4444; }

.cart-actions { display: flex; gap: 15px; margin-top: 20px; }
.btn-clear { padding: 10px 20px; border: 1px solid #ef4444; color: #ef4444; background: #fff; font-weight: 600; font-size: 13px; cursor: pointer; border-radius: 6px; transition: 0.2s; }
.btn-clear:hover { background: #fef2f2; }
.btn-continue-shopping { padding: 10px 20px; border: 1px solid #cbd5e1; color: #64748b; background: #fff; font-weight: 600; font-size: 13px; cursor: pointer; border-radius: 6px; transition: 0.2s; }
.btn-continue-shopping:hover { border-color: #1e3a8a; color: #1e3a8a; }

/* Summary */
.cart-summary { background: #f8fafc; border-radius: 12px; padding: 30px; height: fit-content; position: sticky; top: 100px; }
.cart-summary h3 { font-size: 18px; color: #0f172a; margin-bottom: 25px; font-weight: 700; }
.summary-row { display: flex; justify-content: space-between; margin-bottom: 14px; font-size: 15px; color: #475569; }
.shipping-note { color: #94a3b8; font-style: italic; font-size: 13px; }
.summary-divider { border-top: 1px solid #e2e8f0; margin: 15px 0; }
.total-row { font-weight: 700; font-size: 18px; color: #0f172a; }
.total-price { color: #dc2626; }
.btn-checkout { width: 100%; padding: 15px; background: linear-gradient(135deg, #1e3a8a 0%, #0f172a 100%); color: #fff; border: none; font-weight: 700; font-size: 16px; border-radius: 8px; cursor: pointer; margin-top: 20px; transition: 0.2s; }
.btn-checkout:hover { opacity: 0.9; box-shadow: 0 4px 15px rgba(15, 23, 42, 0.3); }
.payment-methods { text-align: center; margin-top: 20px; font-size: 13px; color: #94a3b8; }
.methods-icons { display: flex; justify-content: center; gap: 15px; margin-top: 10px; }
.methods-icons span { font-size: 20px; color: #1e3a8a; }

@media (max-width: 992px) {
  .cart-grid { grid-template-columns: 1fr; }
  .cart-summary { position: static; }
}
@media (max-width: 768px) {
  .cart-header-row { display: none; }
  .cart-item { grid-template-columns: 1fr; gap: 10px; }
  .col-product { flex-direction: column; align-items: flex-start; }
}
</style>
