<template>
  <div v-if="isOpen" class="modal-backdrop" @click.self="close">
    <div class="modal-content">
      <div class="modal-header">
        <h3>Chọn {{ title }}</h3>
        <button class="close-btn" @click="close">×</button>
      </div>
      
      <div class="modal-body">
        <div class="tags-container">
          <label 
            v-for="item in options" 
            :key="item.id" 
            class="tag-item" 
            :class="{ active: isSelected(item) }"
          >
            <input 
              type="checkbox" 
              :value="item" 
              :checked="isSelected(item)"
              @change="toggleItem(item)"
              hidden
            >
            {{ getItemName(item) }}
            <span v-if="isSelected(item)" class="check-mark">✓</span>
          </label>
        </div>
      </div>

      <div class="modal-footer">
        <button class="btn btn-outline" @click="close">Đóng</button>
        <button class="btn btn-primary" @click="confirm">Xác nhận</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue';

const props = defineProps({
  isOpen: Boolean,
  title: String,
  options: Array,
  selected: Array, // Current selected items from parent
  type: String // 'color' or 'size' to know which property to display
});

const emit = defineEmits(['close', 'confirm']);

const tempSelected = ref([]);

// Sync props to local state when modal opens
watch(() => props.isOpen, (newVal) => {
  if (newVal) {
    tempSelected.value = [...props.selected];
  }
});

const getItemName = (item) => {
  return props.type === 'color' ? item.tenMauSac : item.tenKichThuoc;
};

const isSelected = (item) => {
  return tempSelected.value.some(i => i.id === item.id);
};

const toggleItem = (item) => {
  const index = tempSelected.value.findIndex(i => i.id === item.id);
  if (index === -1) {
    tempSelected.value.push(item);
  } else {
    tempSelected.value.splice(index, 1);
  }
};

const close = () => emit('close');
const confirm = () => emit('confirm', tempSelected.value);
</script>

<style scoped>
.modal-backdrop {
  position: fixed; top: 0; left: 0; width: 100%; height: 100%;
  background: rgba(0,0,0,0.5); z-index: 1000;
  display: flex; justify-content: center; align-items: center;
}
.modal-content {
  background: white; width: 500px; border-radius: 12px;
  box-shadow: 0 10px 25px rgba(0,0,0,0.2); overflow: hidden;
}
.modal-header {
  padding: 15px 20px; border-bottom: 1px solid #eee;
  display: flex; justify-content: space-between; align-items: center;
}
.modal-header h3 { margin: 0; font-size: 18px; color: #333; }
.close-btn { background: none; border: none; font-size: 24px; cursor: pointer; color: #999; }

.modal-body { padding: 20px; max-height: 400px; overflow-y: auto; }

.tags-container { display: flex; flex-wrap: wrap; gap: 10px; }
.tag-item {
  padding: 8px 16px; border: 1px solid #ddd; border-radius: 20px;
  cursor: pointer; font-size: 14px; user-select: none; transition: 0.2s;
  display: flex; align-items: center; gap: 5px; background: #f8f9fa;
}
.tag-item:hover { border-color: #aaa; }
.tag-item.active {
  background-color: #0f172a; color: white; border-color: #0f172a;
}
.check-mark { font-size: 12px; font-weight: bold; }

.modal-footer {
  padding: 15px 20px; border-top: 1px solid #eee;
  display: flex; justify-content: flex-end; gap: 10px;
}
.btn { padding: 8px 20px; border-radius: 6px; cursor: pointer; font-weight: 600; border: none; }
.btn-outline { background: white; border: 1px solid #ddd; color: #333; }
.btn-primary { background: #0f172a; color: white; }
</style>