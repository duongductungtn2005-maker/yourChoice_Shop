<template>
  <div class="container-fluid p-4 bg-light">
    <div class="d-flex justify-content-between align-items-center mb-4">
      <h4 class="fw-bold">Khách hàng</h4>
      <div>
        <button class="btn btn-outline-secondary me-2">
          <i class="bi bi-file-earmark-excel"></i> Xuất Excel
        </button>
        <button class="btn btn-primary" @click="goToCreate">
          <i class="bi bi-plus-lg"></i> Tạo khách hàng
        </button>
      </div>
    </div>

    <div class="card border-0 shadow-sm mb-4">
      <div class="card-body">
        <div class="row g-3">
          <div class="col-md-6">
            <div class="input-group">
              <span class="input-group-text bg-white"><i class="bi bi-search"></i></span>
              <input type="text" class="form-control" placeholder="Tìm khách hàng (tên, email, sđt)..." v-model="filters.keyword">
            </div>
          </div>
          <div class="col-md-3">
             <select class="form-select" v-model="filters.gender">
               <option value="">Giới tính: Tất cả</option>
               <option value="Nam">Nam</option>
               <option value="Nữ">Nữ</option>
             </select>
          </div>
          <div class="col-md-3">
            <select class="form-select" v-model="filters.status">
               <option value="">Trạng thái: Tất cả</option>
               <option value="active">Hoạt động</option>
               <option value="inactive">Ngừng hoạt động</option>
             </select>
          </div>
        </div>
      </div>
    </div>

    <div class="card border-0 shadow-sm">
      <div class="card-body p-0">
        <table class="table table-hover align-middle mb-0">
          <thead class="bg-light text-secondary">
            <tr>
              <th scope="col" class="ps-4">STT</th>
              <th scope="col">Code</th>
              <th scope="col">Email</th>
              <th scope="col">Họ tên</th>
              <th scope="col">Ngày sinh</th>
              <th scope="col">Số điện thoại</th>
              <th scope="col">Giới tính</th>
              <th scope="col">Trạng thái</th>
              <th scope="col" class="text-center">Thao tác</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(customer, index) in customers" :key="customer.id">
              <td class="ps-4">{{ index + 1 }}</td>
              <td>{{ customer.code }}</td>
              <td>{{ customer.email }}</td>
              <td class="fw-bold">{{ customer.name }}</td>
              <td>{{ customer.dob }}</td>
              <td>{{ customer.phone }}</td>
              <td>{{ customer.gender }}</td>
              <td>
                <span :class="['badge rounded-pill', customer.status === 'active' ? 'bg-success-subtle text-success border border-success' : 'bg-danger-subtle text-danger border border-danger']">
                  {{ customer.status === 'active' ? 'Hoạt động' : 'Ngừng hoạt động' }}
                </span>
              </td>
              <td class="text-center">
                <button class="btn btn-sm btn-link text-secondary" @click="viewDetail(customer.id)">
                  <i class="bi bi-eye"></i>
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <div class="card-footer bg-white d-flex justify-content-between align-items-center py-3">
         <span class="text-muted small">Xem 5 khách hàng</span>
         <nav>
           <ul class="pagination pagination-sm mb-0">
             <li class="page-item disabled"><a class="page-link" href="#"><</a></li>
             <li class="page-item active"><a class="page-link" href="#">1</a></li>
             <li class="page-item"><a class="page-link" href="#">...</a></li>
             <li class="page-item"><a class="page-link" href="#">10</a></li>
             <li class="page-item"><a class="page-link" href="#">></a></li>
           </ul>
         </nav>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'CustomerList',
  data() {
    return {
      filters: {
        keyword: '',
        gender: '',
        status: ''
      },
      // Mock Data giống hình ảnh
      customers: [
        { id: 1, code: 'KH1', email: 'abc@gmail.com', name: 'ABC', dob: '1/1/2000', phone: '0987654321', gender: 'Nam', status: 'active' },
        { id: 2, code: 'KH2', email: 'abc@gmail.com', name: 'ABC', dob: '2/2/2001', phone: '0987654321', gender: 'Nữ', status: 'active' },
        { id: 3, code: 'KH3', email: 'abc@gmail.com', name: 'ABC', dob: '3/3/2002', phone: '0987654321', gender: 'Nam', status: 'active' },
        { id: 4, code: 'KH4', email: 'abc@gmail.com', name: 'ABC', dob: '4/4/2003', phone: '0987654321', gender: 'Nữ', status: 'active' },
        { id: 5, code: 'KH5', email: 'abc@gmail.com', name: 'ABC', dob: '5/5/2004', phone: '0987654321', gender: 'Nam', status: 'inactive' },
      ]
    }
  },
  methods: {
    viewDetail(id) {
      console.log('Xem chi tiết khách hàng:', id);
      // this.$router.push(`/customer/${id}`);
    },
    goToCreate() {
      console.log('Chuyển trang tạo mới');
    }
  }
}
</script>

<style scoped>
/* Tùy chỉnh thêm nếu cần */
.badge {
  font-weight: 500;
  padding: 6px 12px;
}
</style>