<template>
  <div class="vnpay-return-page">
    <div class="container">
      <div class="result-card" v-if="!loading">
        <div v-if="success" class="result-success">
          <div class="icon-circle success-circle">
            <i class="fas fa-check"></i>
          </div>
          <h2>Thanh toán thành công!</h2>
          <p class="desc">Cảm ơn bạn đã mua hàng tại YourChoice Shop</p>
          <div class="info-box">
            <div class="info-row">
              <span class="label">Mã đơn hàng:</span>
              <span class="value">{{ maHoaDon }}</span>
            </div>
            <div class="info-row" v-if="transactionNo">
              <span class="label">Mã giao dịch:</span>
              <span class="value">{{ transactionNo }}</span>
            </div>
            <div class="info-row" v-if="amount">
              <span class="label">Số tiền:</span>
              <span class="value">{{ formatMoney(amount) }}</span>
            </div>
          </div>
          <div class="actions">
            <button class="btn-primary" @click="$router.push('/orders')" v-if="isAuth">
              <i class="fas fa-list"></i> Xem đơn hàng
            </button>
            <button class="btn-secondary" @click="$router.push('/')">
              <i class="fas fa-home"></i> Về trang chủ
            </button>
          </div>
        </div>

        <div v-else class="result-fail">
          <div class="icon-circle fail-circle">
            <i class="fas fa-times"></i>
          </div>
          <h2>Thanh toán thất bại</h2>
          <p class="desc">{{ message }}</p>
          <div class="info-box" v-if="maHoaDon">
            <div class="info-row">
              <span class="label">Mã đơn hàng:</span>
              <span class="value">{{ maHoaDon }}</span>
            </div>
          </div>
          <div class="actions">
            <button class="btn-primary" @click="$router.push('/checkout')">
              <i class="fas fa-redo"></i> Thử lại
            </button>
            <button class="btn-secondary" @click="$router.push('/')">
              <i class="fas fa-home"></i> Về trang chủ
            </button>
          </div>
        </div>
      </div>

      <div class="loading-card" v-else>
        <i class="fas fa-spinner fa-spin"></i>
        <p>Đang xác nhận thanh toán...</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useCartStore } from '@/stores/cart'
import { isAuthenticated } from '@/services/auth'
import axios from 'axios'

const route = useRoute()
const cartStore = useCartStore()

const loading = ref(true)
const success = ref(false)
const message = ref('')
const maHoaDon = ref('')
const transactionNo = ref('')
const amount = ref(0)
const isAuth = isAuthenticated()

const formatMoney = (val) => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val || 0)

onMounted(async () => {
  try {
    // Lấy toàn bộ query params từ VNPay redirect
    const params = { ...route.query }

    const { data } = await axios.get('http://localhost:8080/api/v1/vnpay/payment-result', { params })

    success.value = data.success
    message.value = data.message
    maHoaDon.value = data.maHoaDon || ''
    transactionNo.value = data.transactionNo || ''

    if (params.vnp_Amount) {
      amount.value = parseInt(params.vnp_Amount) / 100
    }

    if (data.success) {
      cartStore.clearCart()
    }
  } catch (e) {
    console.error('Lỗi xác nhận thanh toán:', e)
    success.value = false
    message.value = 'Không thể xác nhận kết quả thanh toán. Vui lòng liên hệ hỗ trợ.'
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.vnpay-return-page {
  min-height: 60vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  background: #f8fafc;
}

.container { max-width: 560px; width: 100%; }

.result-card, .loading-card {
  background: #fff;
  border-radius: 16px;
  padding: 40px;
  text-align: center;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.08);
}

.loading-card i { font-size: 36px; color: #1e3a8a; margin-bottom: 16px; }
.loading-card p { font-size: 16px; color: #64748b; }

.icon-circle {
  width: 80px; height: 80px;
  border-radius: 50%;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 20px;
}
.icon-circle i { font-size: 36px; color: #fff; }
.success-circle { background: #22c55e; }
.fail-circle { background: #ef4444; }

h2 { font-size: 24px; color: #0f172a; margin-bottom: 8px; }
.desc { font-size: 15px; color: #64748b; margin-bottom: 24px; }

.info-box {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  padding: 16px 20px;
  margin-bottom: 28px;
  text-align: left;
}
.info-row {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  font-size: 14px;
}
.info-row + .info-row { border-top: 1px solid #e2e8f0; }
.info-row .label { color: #64748b; }
.info-row .value { font-weight: 600; color: #0f172a; }

.actions {
  display: flex;
  gap: 12px;
  justify-content: center;
}
.btn-primary, .btn-secondary {
  padding: 12px 24px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  border: none;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  transition: 0.2s;
}
.btn-primary { background: #1e3a8a; color: #fff; }
.btn-primary:hover { background: #1e40af; }
.btn-secondary { background: #f1f5f9; color: #334155; }
.btn-secondary:hover { background: #e2e8f0; }
</style>
