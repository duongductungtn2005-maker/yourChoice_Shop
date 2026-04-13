import axios from "axios"

const ghnRequest = axios.create({
  baseURL: "http://localhost:8080",
  timeout: 10000,
})

export const ghnApi = {

  getProvinces() {
    return ghnRequest.get("/api/admin/ghn/tinh-thanh")
  },

  getDistricts(provinceId) {
    return ghnRequest.get(`/api/admin/ghn/quan-huyen/${provinceId}`)
  },

  getWards(districtId) {
    return ghnRequest.get(`/api/admin/ghn/phuong-xa/${districtId}`)
  },

  getShopAddress() {
    return ghnRequest.get("/api/admin/ghn/dia-chi-shop")
  },

  calculateShippingFee(data) {
    return ghnRequest.post("/api/admin/ghn/tinh-phi-van-chuyen", data)
  }

}