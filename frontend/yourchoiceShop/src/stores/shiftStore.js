import { defineStore } from 'pinia'
import request from '@/services/request'
import { getCurrentUser } from '@/services/auth'

export const useShiftStore = defineStore('shift', {
  state: () => ({
    activeShift: null,
    hasActiveShift: false
  }),

  actions: {
    async fetchShift() {
      const user = getCurrentUser()
      if (!user || !user.tenTaiKhoan) return

      try {
        const res = await request.get(`/giao-ca/hien-tai?username=${user.tenTaiKhoan}`)
        
        this.activeShift = res.data || null
        this.hasActiveShift = !!(res.data && res.data.id)

      } catch (e) {
        this.activeShift = null
        this.hasActiveShift = false
      }
    },

    setActiveShift(shift) {
      this.activeShift = shift
      this.hasActiveShift = !!shift
    },

    clearShift() {
      this.activeShift = null
      this.hasActiveShift = false
    }
  }
})