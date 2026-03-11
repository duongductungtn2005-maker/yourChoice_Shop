import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useCartStore = defineStore('cart', () => {
  const STORAGE_KEY = 'yc_cart_items'

  // Khởi tạo từ localStorage
  const items = ref(loadFromStorage())

  function loadFromStorage() {
    try {
      return JSON.parse(localStorage.getItem(STORAGE_KEY)) || []
    } catch { return [] }
  }

  function saveToStorage() {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(items.value))
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
  }
})
