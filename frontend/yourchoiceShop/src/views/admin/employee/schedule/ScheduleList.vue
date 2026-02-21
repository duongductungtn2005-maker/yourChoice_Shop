<template>
  <div class="page-container">
    <h1 class="page-title">Lịch làm việc</h1>

    <div class="card">
      <div class="top-bar">
        <div class="search-box">
          <i class="fas fa-search"></i>
          <input type="text" placeholder="Tìm kiếm nhân viên..." />
        </div>
        <button class="btn-add" @click="router.push({ name: 'admin-schedule-create' })">
          <i class="fas fa-plus"></i> Thêm lịch làm việc
        </button>
      </div>

      <div class="filter-row">
        <div class="filter-group">
          <label>Nhân viên</label>
          <input type="text" placeholder="Tên nhân viên" />
        </div>
        <div class="filter-group">
          <label>Ca làm việc</label>
          <select>
            <option value="">Chọn ca</option>
          </select>
        </div>
        <div class="filter-group">
          <label>Trạng thái</label>
          <select>
            <option value="">Chọn trạng thái</option>
          </select>
        </div>
        <div class="filter-group">
          <label>Ngày làm việc</label>
          <input type="date" />
        </div>
      </div>

      <div class="table-container">
        <table>
          <thead>
            <tr>
              <th class="text-center" style="width: 50px;"><input type="checkbox" /></th>
              <th>STT</th>
              <th>Nhân viên</th>
              <th>Ca làm việc</th>
              <th>Giờ bắt đầu</th>
              <th>Giờ kết thúc</th>
              <th>Ngày làm việc</th>
              <th>Trạng thái</th>
              <th class="text-center">Hành động</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="i in 4" :key="i">
              <td class="text-center"><input type="checkbox" /></td>
              <td>{{ i }}</td>
              <td class="font-medium">Vũ Thành Hải Phong</td>
              <td>Ca {{ i }}</td>
              <td>08:00</td>
              <td>12:00</td>
              <td>15/10/2023</td>
              <td><span class="badge badge-success">Hoạt động</span></td>
              <td class="text-center">
                <button class="btn-icon"><i class="fas fa-eye"></i></button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router';
const router = useRouter();
</script>

<style scoped>
/* CSS giống hệt file ShiftList.vue để đồng bộ giao diện */
* { box-sizing: border-box; }
.page-container { padding: 24px; background-color: #f8f9fa; min-height: 100vh; font-family: Arial, sans-serif; }
.page-title { font-size: 24px; font-weight: bold; color: #1e293b; margin-top: 0; margin-bottom: 24px; }
.card { background: #ffffff; border-radius: 12px; box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1); padding: 24px; border: 1px solid #f1f5f9; }
.top-bar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; }
.search-box { position: relative; width: 350px; }
.search-box input { width: 100%; padding: 10px 16px 10px 40px; border: 1px solid #e2e8f0; border-radius: 8px; outline: none; font-size: 14px; }
.search-box input:focus { border-color: #3b82f6; }
.search-box i { position: absolute; left: 14px; top: 50%; transform: translateY(-50%); color: #94a3b8; }
.btn-add { padding: 10px 20px; border: 2px solid #1e293b; background: #ffffff; color: #1e293b; border-radius: 8px; cursor: pointer; font-weight: 600; display: flex; align-items: center; gap: 8px; font-size: 14px; }
.btn-add:hover { background: #f8fafc; }
.filter-row { display: grid; grid-template-columns: repeat(4, 1fr); gap: 24px; margin-bottom: 24px; }
.filter-group { display: flex; flex-direction: column; gap: 8px; }
.filter-group label { font-size: 14px; font-weight: 500; color: #475569; }
.filter-group input, .filter-group select { padding: 10px 12px; border: 1px solid #e2e8f0; border-radius: 8px; outline: none; font-size: 14px; background-color: #fff; }
.table-container { overflow-x: auto; }
table { width: 100%; border-collapse: collapse; text-align: left; }
th, td { padding: 16px; border-bottom: 1px solid #f1f5f9; font-size: 14px; }
th { background-color: #f8fafc; font-weight: 600; color: #475569; text-transform: uppercase; font-size: 13px; border-top: 1px solid #f1f5f9; }
tr:hover td { background-color: #f8fafc; }
.text-center { text-align: center; }
.font-medium { font-weight: 500; color: #1e293b; }
.badge { padding: 4px 12px; border-radius: 9999px; font-size: 12px; font-weight: 600; display: inline-block; }
.badge-success { background: #dcfce7; color: #166534; border: 1px solid #bbf7d0; }
.btn-icon { background: none; border: none; color: #64748b; cursor: pointer; padding: 6px; border-radius: 6px; }
.btn-icon:hover { background-color: #eff6ff; color: #3b82f6; }
</style>