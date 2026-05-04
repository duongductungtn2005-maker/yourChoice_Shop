<template>
  <div class="base-page-container">
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
  width: 100%;
  max-width: 100%;
  overflow: hidden;
}
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 20px;
}
.filter-bar {
  display: flex;
  gap: 30px;
  flex-wrap: wrap;
  margin-bottom: 20px;
  border-bottom: 1px solid #eee;
  padding-bottom: 15px;
}
.content-body {
  width: 100%;
  max-width: 100%;
  overflow-x: auto;
}
.pagination-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
  padding-top: 10px;
  border-top: 1px solid #eee;
  margin-top: 20px;
}

@media (max-width: 768px) {
  .base-page-container {
    padding: 12px;
  }
  .main-card {
    padding: 16px;
  }
  .toolbar,
  .filter-bar,
  .pagination-footer {
    flex-direction: column;
    align-items: stretch;
    gap: 12px;
  }
}
</style>