<template>
  <div class="base-page-container">
    <div class="page-header">
      <slot name="header">
        <h2>Tiêu đề trang</h2>
      </slot>
    </div>

    <div class="main-card">
      <div class="toolbar" v-if="$slots.toolbar">
        <slot name="toolbar"></slot>
      </div>

      <div class="filter-bar" v-if="$slots.filter">
        <slot name="filter"></slot>
      </div>

      <div class="content-body">
        <slot></slot> </div>

      <div class="pagination-footer" v-if="$slots.footer">
        <slot name="footer"></slot>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* Copy toàn bộ CSS bố cục chung vào đây */
.base-page-container {
  padding: 20px;
  background-color: #f5f5f5;
  min-height: 100vh;
}
.page-header { margin-bottom: 20px; }
.main-card {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.05);
}
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.filter-bar {
  display: flex;
  gap: 30px;
  margin-bottom: 20px;
  border-bottom: 1px solid #eee;
  padding-bottom: 15px;
}
.pagination-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 10px;
  border-top: 1px solid #eee;
  margin-top: 20px;
}
</style>