<!-- <template>
  <div class="customer-page">
    <div class="header-section">
      <div class="breadcrumb">
        <span>Quản lý khách hàng</span> 
        <span class="divider">/</span> 
        <span class="active">Danh sách khách hàng</span>
      </div>
    </div>

    <div class="card">
      <div class="card-header">
         <div class="search-wrap">
            <span class="search-icon">🔍</span>
            <input 
              type="text" 
              v-model="filter.keyword" 
              placeholder="Tìm theo tên, SĐT, email..." 
              @keyup.enter="fetchData"
            >
         </div>
         <div class="action-group">
             <button class="btn btn-outline" @click="exportExcel">
                <i class="fas fa-file-excel"></i> Xuất Excel
             </button>
             <button class="btn btn-primary" @click="$router.push('/admin/khach-hang/create')">
                <i class="fas fa-plus"></i> Tạo khách hàng
             </button>
         </div>
      </div>

      <div class="filter-bar">
         <div class="filter-item">
            <label>Giới tính:</label>
            <select v-model="filter.gioiTinh" @change="handleFilterChange">
               <option :value="null">Tất cả</option>
               <option :value="true">Nam</option>
               <option :value="false">Nữ</option>
            </select>
         </div>
         <div class="filter-item">
            <label>Trạng thái:</label>
            <select v-model="filter.trangThai" @change="handleFilterChange">
               <option :value="null">Tất cả</option>
               <option :value="1">Hoạt động</option>
               <option :value="0">Ngừng hoạt động</option>
            </select>
         </div>
      </div>

      <div class="table-responsive">
        <table>
          <thead>
            <tr>
              <th class="text-center" width="5%">STT</th>
              <th width="10%">Mã KH</th>
              <th width="20%">Họ tên</th>
              <th width="15%">Email</th>
              <th width="10%">SĐT</th>
              <th class="text-center" width="10%">Giới tính</th>
              <th class="text-center" width="10%">Ngày sinh</th>
              <th class="text-center" width="10%">Trạng thái</th>
              <th class="text-center" width="10%">Thao tác</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="loading"><td colspan="9" class="text-center py-4">Đang tải dữ liệu...</td></tr>
            <tr v-else-if="items.length === 0"><td colspan="9" class="text-center py-4">Không tìm thấy khách hàng nào.</td></tr>
            
            <tr v-else v-for="(item, index) in items" :key="item.id">
              <td class="text-center">{{ (page - 1) * pageSize + index + 1 }}</td>
              
              <td class="font-bold text-code">{{ item.maKhachHang }}</td>
              
              <td class="font-medium text-primary cursor-pointer" @click="viewDetail(item)" title="Xem chi tiết">
                  {{ item.tenKhachHang }}
              </td>
              
              <td>{{ item.email }}</td>
              <td>{{ item.soDienThoai }}</td>
              
              <td class="text-center">
                  {{ item.gioiTinh === true ? 'Nam' : (item.gioiTinh === false ? 'Nữ' : '-') }}
              </td>
              
              <td class="text-center">{{ item.ngaySinh || '-' }}</td>
              
              <td class="text-center">
                 <span :class="['badge', item.trangThai === 1 ? 'badge-success' : 'badge-secondary']">
                    {{ item.trangThai === 1 ? 'Hoạt động' : 'Ngừng' }}
                 </span>
              </td>
              
              <td class="text-center">
                 <div class="action-container">
                    <button class="btn-icon" @click="viewDetail(item)" title="Xem chi tiết">
                        <i class="far fa-eye"></i>
                    </button>

                    <div class="status-wrapper" @click="toggleStatus(item)" title="Bật/Tắt trạng thái">
                       <i 
                         class="fas toggle-icon"
                         :class="item.trangThai === 1 ? 'fa-toggle-on active' : 'fa-toggle-off inactive'"
                       ></i>
                    </div>
                 </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <div class="pagination-footer">
         <div class="page-info">
            Hiển thị 
            <select v-model="pageSize" @change="handlePageSizeChange">
               <option :value="5">5</option>
               <option :value="10">10</option>
               <option :value="20">20</option>
            </select> 
            kết quả
         </div>
         <div class="page-controls">
            <button :disabled="page === 1" @click="changePage(page - 1)">‹</button>
            <span class="page-number">Trang {{ page }} / {{ totalPages }}</span>
            <button :disabled="page === totalPages" @click="changePage(page + 1)">›</button>
         </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import request from '@/services/request'; // Import request chung của bạn
import Swal from 'sweetalert2';
import { useRouter } from 'vue-router';

const router = useRouter();

// --- STATE ---
const items = ref([]);
const loading = ref(false);
const page = ref(1);
const pageSize = ref(10);
const totalPages = ref(1);

const filter = reactive({ 
    keyword: '', 
    gioiTinh: null, 
    trangThai: null 
});

// API URL (Khớp với Controller Backend)
const API_URL = '/khach-hang'; 

// --- FETCH DATA ---
const fetchData = async () => {
    loading.value = true;
    try {
        const res = await request.get(API_URL, {
            params: {
                page: page.value - 1,
                size: pageSize.value,
                keyword: filter.keyword,
                gioiTinh: filter.gioiTinh,
                trangThai: filter.trangThai
            }
        });
        // API trả về Page<KhachHang>, cần map đúng
        items.value = res.data.content;
        totalPages.value = res.data.totalPages;
    } catch (e) {
        console.error("Lỗi tải dữ liệu:", e);
    } finally {
        loading.value = false;
    }
};

// --- LOGIC TOGGLE STATUS (OPTIMISTIC UI - KHÔNG RELOAD BẢNG) ---
const toggleStatus = async (item) => {
    const oldStatus = item.trangThai;
    const newStatus = oldStatus === 1 ? 0 : 1;

    // 1. Cập nhật giao diện ngay lập tức
    item.trangThai = newStatus;

    try {
        // 2. Gọi API cập nhật ngầm
        await request.put(`${API_URL}/${item.id}/trang-thai`, null, {
            params: { trangThai: newStatus }
        });
        
        // Thông báo nhỏ
        const Toast = Swal.mixin({ toast: true, position: 'top-end', showConfirmButton: false, timer: 1500 });
        Toast.fire({ 
            icon: 'success', 
            title: `Đã cập nhật trạng thái: ${newStatus === 1 ? 'Hoạt động' : 'Ngừng'}` 
        });

    } catch (e) {
        // 3. Nếu lỗi thì hoàn tác
        item.trangThai = oldStatus;
        Swal.fire('Lỗi', 'Không thể cập nhật trạng thái', 'error');
        console.error(e);
    }
};

// --- CÁC ACTIONS KHÁC ---
const exportExcel = async () => {
    try {
        const response = await request.get(`${API_URL}/export`, {
            params: {
                keyword: filter.keyword,
                gioiTinh: filter.gioiTinh,
                trangThai: filter.trangThai
            },
            responseType: 'blob'
        });
        
        const url = window.URL.createObjectURL(new Blob([response.data]));
        const link = document.createElement('a');
        link.href = url;
        const dateStr = new Date().toISOString().slice(0,10);
        link.setAttribute('download', `DS_KhachHang_${dateStr}.xlsx`);
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        window.URL.revokeObjectURL(url);
    } catch (e) {
        Swal.fire('Lỗi', 'Không thể xuất file Excel', 'error');
    }
};

const viewDetail = (item) => {
    // Chuyển sang trang chi tiết (Update sau)
    router.push(`/admin/khach-hang/detail/${item.id}`);
};

const changePage = (p) => { 
    if (p >= 1 && p <= totalPages.value) { 
        page.value = p; 
        fetchData(); 
    } 
};

const handlePageSizeChange = () => { 
    page.value = 1; 
    fetchData(); 
};

const handleFilterChange = () => {
    page.value = 1;
    fetchData();
};

onMounted(() => {
    fetchData();
});
</script>

<style scoped>
/* GENERAL LAYOUT */
.customer-page { font-family: 'Segoe UI', sans-serif; background-color: #f8fafc; min-height: 100vh; padding: 20px; }
.header-section { margin-bottom: 20px; }
.breadcrumb { font-size: 14px; color: #64748b; } 
.breadcrumb .active { font-weight: 600; color: #0f172a; }

.card { background: #fff; border-radius: 8px; box-shadow: 0 1px 3px rgba(0,0,0,0.05); padding: 20px; }

/* TOOLBAR */
.card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.search-wrap { position: relative; width: 350px; }
.search-wrap input { width: 100%; padding: 8px 12px 8px 36px; border: 1px solid #e2e8f0; border-radius: 4px; outline: none; font-size: 14px; }
.search-wrap input:focus { border-color: #3b82f6; box-shadow: 0 0 0 3px rgba(59,130,246,0.1); }
.search-icon { position: absolute; left: 10px; top: 50%; transform: translateY(-50%); color: #94a3b8; }

.action-group { display: flex; gap: 10px; }
.btn { padding: 8px 16px; border-radius: 4px; font-weight: 600; cursor: pointer; border: 1px solid transparent; display: flex; align-items: center; gap: 5px; font-size: 14px; }
.btn-primary { background: #0f172a; color: #fff; }
.btn-outline { background: #fff; border-color: #cbd5e1; color: #475569; }
.btn-outline:hover { background-color: #f8fafc; border-color: #94a3b8; }

/* FILTER BAR */
.filter-bar { display: flex; gap: 30px; margin-bottom: 20px; padding-bottom: 15px; border-bottom: 1px solid #f1f5f9; }
.filter-item { display: flex; align-items: center; gap: 10px; font-size: 14px; font-weight: 600; color: #334155; }
.filter-item select { padding: 4px 8px; border: 1px solid #e2e8f0; border-radius: 4px; cursor: pointer; outline: none; color: #475569; }
.filter-item select:focus { border-color: #3b82f6; }

/* TABLE */
.table-responsive { overflow-x: auto; border: 1px solid #e2e8f0; border-radius: 4px; }
table { width: 100%; border-collapse: collapse; }
th { background: #f8fafc; padding: 12px; font-weight: 600; color: #475569; border-bottom: 1px solid #e2e8f0; font-size: 13px; text-transform: uppercase; text-align: left; }
td { padding: 12px; border-bottom: 1px solid #f1f5f9; font-size: 14px; vertical-align: middle; color: #334155; }

.text-center { text-align: center; }
.font-bold { font-weight: 600; }
.font-medium { font-weight: 500; }
.text-primary { color: #0f172a; }
.text-code { color: #64748b; font-family: monospace; }
.text-gray { color: #94a3b8; }
.cursor-pointer { cursor: pointer; }

/* BADGE */
.badge { padding: 4px 10px; border-radius: 20px; font-size: 12px; font-weight: 600; }
.badge-success { background: #dcfce7; color: #166534; border: 1px solid #bbf7d0; }
.badge-secondary { background: #f1f5f9; color: #64748b; border: 1px solid #e2e8f0; }
.badge-danger { background: #fee2e2; color: #991b1b; }

/* ACTIONS & TOGGLE */
.action-container { display: flex; align-items: center; justify-content: center; gap: 15px; }
.btn-icon { background: none; border: none; cursor: pointer; font-size: 16px; color: #3b82f6; transition: 0.2s; }
.btn-icon:hover { transform: scale(1.1); color: #2563eb; }

.status-wrapper { display: flex; align-items: center; justify-content: center; cursor: pointer; transition: opacity 0.2s; }
.status-wrapper:hover { opacity: 0.8; }
.toggle-icon { font-size: 24px; transition: color 0.3s ease; }
.toggle-icon.active { color: #10b981; } /* Xanh lá */
.toggle-icon.inactive { color: #cbd5e1; } /* Xám */

/* PAGINATION */
.pagination-footer { display: flex; justify-content: space-between; align-items: center; margin-top: 20px; padding-top: 15px; }
.page-info { font-size: 14px; color: #64748b; }
.page-info select { border: 1px solid #e2e8f0; border-radius: 4px; padding: 2px 6px; margin: 0 5px; outline: none; }
.page-controls button { width: 32px; height: 32px; border: 1px solid #e2e8f0; background: #fff; border-radius: 4px; margin: 0 5px; cursor: pointer; }
.page-controls button:disabled { background: #f8fafc; color: #cbd5e1; cursor: not-allowed; }
.page-number { font-size: 14px; font-weight: 600; margin: 0 10px; }
</style> -->