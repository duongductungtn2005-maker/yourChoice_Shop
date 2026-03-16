import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

function getCurrentUserId() {
  try {
    const user = JSON.parse(sessionStorage.getItem('user') || 'null')
    return user?.id || null
  } catch { return null }
}

function getStorageKey() {
  const userId = getCurrentUserId()
  return userId ? `yc_cart_items_${userId}` : 'yc_cart_items_guest'
}

export const useCartStore = defineStore('cart', () => {
  // Khởi tạo từ localStorage theo user hiện tại
  const items = ref(loadFromStorage())

  function loadFromStorage() {
    try {
      return JSON.parse(localStorage.getItem(getStorageKey())) || []
    } catch { return [] }
  }

  function saveToStorage() {
    localStorage.setItem(getStorageKey(), JSON.stringify(items.value))
  }

  /** Gọi khi đăng nhập/đăng xuất để nạp lại giỏ hàng đúng user */
  function reloadCart() {
    items.value = loadFromStorage()
  }

  // Computed
  const totalItems = computed(() => items.value.reduce((sum, i) => sum + i.soLuong, 0))
  const totalMoney = computed(() => items.value.reduce((sum, i) => sum + i.donGia * i.soLuong, 0))

  // Actions
  function addItem(item) {
    const existing = items.value.find(x => x.variantId === item.variantId)
    if (existing) {
      existing.soLuong += item.soLuong
      if (item.maxStock && existing.soLuong > item.maxStock) {
        existing.soLuong = item.maxStock
      }
    } else {
      items.value.push({ ...item })
    }
    saveToStorage()
  }

  function updateQuantity(variantId, qty) {
    const item = items.value.find(x => x.variantId === variantId)
    if (item && qty > 0) {
      item.soLuong = qty
      saveToStorage()
    }
  }

  function removeItem(variantId) {
    items.value = items.value.filter(x => x.variantId !== variantId)
    saveToStorage()
  }

  function clearCart() {
    items.value = []
    saveToStorage()
  }

  return {
    items,
    totalItems,
    totalMoney,
    addItem,
    updateQuantity,
    removeItem,
    clearCart,
    reloadCart,
  }
})
