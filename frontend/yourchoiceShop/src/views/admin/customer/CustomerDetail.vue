<template>
  <div class="container-fluid p-4 bg-light">
    <div class="row g-4">
      
      <div class="col-lg-4">
        <div class="card border-0 shadow-sm h-100">
          <div class="card-header bg-white border-0 pt-4 pb-0">
            <h5 class="fw-bold text-primary">Thông tin khách hàng</h5>
            <hr>
          </div>
          <div class="card-body">
            <div class="d-flex justify-content-center mb-4">
              <div class="rounded-circle border d-flex align-items-center justify-content-center text-muted bg-light" 
                   style="width: 120px; height: 120px; cursor: pointer;">
                <span>Chọn ảnh</span>
              </div>
            </div>

            <div class="mb-3">
              <label class="form-label text-danger fw-bold">*Tên khách hàng</label>
              <input type="text" class="form-control" v-model="customer.name">
            </div>
            <div class="mb-3">
              <label class="form-label text-danger fw-bold">*Email</label>
              <input type="email" class="form-control" v-model="customer.email">
            </div>
            <div class="mb-3">
              <label class="form-label text-danger fw-bold">*Số điện thoại</label>
              <input type="text" class="form-control" v-model="customer.phone">
            </div>
            <div class="mb-3">
              <label class="form-label text-danger fw-bold">*Ngày sinh</label>
              <input type="date" class="form-control" v-model="customer.dob">
            </div>
            <div class="mb-4">
              <label class="form-label text-danger fw-bold">*Giới tính</label>
              <div class="d-flex gap-4">
                <div class="form-check">
                  <input class="form-check-input" type="radio" value="Nam" v-model="customer.gender">
                  <label class="form-check-label">Nam</label>
                </div>
                <div class="form-check">
                  <input class="form-check-input" type="radio" value="Nữ" v-model="customer.gender">
                  <label class="form-check-label">Nữ</label>
                </div>
              </div>
            </div>

            <button class="btn btn-outline-primary w-100 fw-bold" @click="updateCustomer">
              Cập nhật khách hàng
            </button>
          </div>
        </div>
      </div>

      <div class="col-lg-8">
        <div class="card border-0 shadow-sm h-100">
          <div class="card-header bg-white border-0 pt-4 pb-0">
            <h5 class="fw-bold text-primary">Danh sách địa chỉ</h5>
            <hr>
          </div>
          <div class="card-body overflow-auto" style="max-height: 80vh;">
            
            <div v-for="(addr, index) in addresses" :key="index" class="address-item mb-4 pb-3 border-bottom">
              <div class="d-flex justify-content-between mb-2">
                 <h6 class="fw-bold text-secondary">Địa chỉ {{ index + 1 }}</h6>
                 <div class="actions">
                    </div>
              </div>
              
              <div class="row g-3">
                <div class="col-md-6">
                  <label class="form-label text-danger small">*Tên</label>
                  <input type="text" class="form-control" v-model="addr.receiverName">
                </div>
                <div class="col-md-6">
                   <label class="form-label text-danger small">*Số điện thoại</label>
                  <input type="text" class="form-control" v-model="addr.receiverPhone">
                </div>
                
                <div class="col-md-4">
                  <label class="form-label text-danger small">*Tỉnh/thành phố</label>
                  <select class="form-select" v-model="addr.city">
                    <option>Thanh Hóa</option>
                  </select>
                </div>
                <div class="col-md-4">
                  <label class="form-label text-danger small">*Quận/Huyện</label>
                  <select class="form-select" v-model="addr.district">
                    <option>Huyện 36</option>
                  </select>
                </div>
                <div class="col-md-4">
                  <label class="form-label text-danger small">*Phường/Xã</label>
                  <select class="form-select" v-model="addr.ward">
                    <option>36 trấn</option>
                  </select>
                </div>
                
                <div class="col-12">
                   <label class="form-label text-danger small">*Địa chỉ cụ thể</label>
                   <input type="text" class="form-control" v-model="addr.detail">
                </div>

                <div class="col-12 d-flex justify-content-between align-items-center mt-2">
                    <i class="bi bi-star text-warning" v-if="addr.isDefault"></i>
                    <i class="bi bi-star text-secondary" v-else style="cursor:pointer"></i>
                    
                    <div class="d-flex gap-3">
                      <i class="bi bi-cloud-arrow-up-fill text-dark" style="cursor:pointer"></i>
                      <i class="bi bi-trash text-dark" style="cursor:pointer"></i>
                    </div>
                </div>
              </div>
            </div>

            <div class="text-center mt-3">
               <button class="btn btn-outline-primary px-4 fw-bold" @click="addAddress">
                 Thêm địa chỉ
               </button>
            </div>
            
            <div class="d-flex justify-content-end mt-3">
               <nav>
                 <ul class="pagination pagination-sm">
                   <li class="page-item"><a class="page-link rounded-circle mx-1" href="#"><</a></li>
                   <li class="page-item active"><a class="page-link rounded-circle mx-1" href="#">1</a></li>
                   <li class="page-item"><a class="page-link rounded-circle mx-1" href="#">></a></li>
                 </ul>
               </nav>
            </div>

          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'CustomerDetail',
  data() {
    return {
      customer: {
        name: '',
        email: '',
        phone: '',
        dob: '',
        gender: 'Nam'
      },
      addresses: [
        {
          receiverName: 'Nguyễn Văn A',
          receiverPhone: 'Nguyễn Văn A', // Dựa theo ảnh placeholder
          city: 'Thanh Hóa',
          district: 'Huyện 36',
          ward: '36 trấn',
          detail: '',
          isDefault: true
        },
        {
          receiverName: 'Nguyễn Văn A',
          receiverPhone: 'Nguyễn Văn A',
          city: 'Thanh Hóa',
          district: 'Huyện 36',
          ward: '36 trấn',
          detail: '',
          isDefault: false
        }
      ]
    }
  },
  methods: {
    updateCustomer() {
      alert('Đã cập nhật thông tin!');
    },
    addAddress() {
      this.addresses.push({
          receiverName: '',
          receiverPhone: '',
          city: '',
          district: '',
          ward: '',
          detail: '',
          isDefault: false
      });
    }
  }
}
</script>

<style scoped>
.form-label {
  margin-bottom: 0.2rem;
}
/* Style cho pagination tròn */
.page-link {
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50% !important;
}
</style>