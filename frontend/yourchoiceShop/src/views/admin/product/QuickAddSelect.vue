<template>
  <div class="quick-add-select" :class="{ 'is-open': showDropdown }" ref="wrapper">
    <div class="select-input-wrap">
      <input
        ref="inputEl"
        type="text"
        v-model="searchText"
        :placeholder="placeholder"
        class="form-control"
        @focus="openDropdown"
        @blur="onBlur"
        @input="onSearch"
        @keydown.escape="closeDropdown"
        autocomplete="off"
      >
      <span class="dropdown-arrow" :class="{ open: showDropdown }" @mousedown.prevent="toggleDropdown">
        <svg width="12" height="12" viewBox="0 0 12 12"><path d="M2 4l4 4 4-4" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/></svg>
      </span>
    </div>

    <div v-if="showDropdown" class="dropdown-list" @mousedown.prevent>
      <div class="dropdown-scroll">
        <div
          v-if="showAddNewOption"
          class="dropdown-item add-new-item"
          @click="addNew"
        >
          <span>Thêm mới "<strong>{{ searchText.trim() }}</strong>"</span>
          <span v-if="saving" class="spinner-mini"></span>
          <span v-else class="add-label">{{ label }}</span>
        </div>
        <div
          v-for="item in filteredOptions"
          :key="item.id"
          class="dropdown-item"
          :class="{ selected: modelValue === item.id }"
          @click="selectItem(item)"
        >
          {{ item[labelKey] }}
          <span v-if="modelValue === item.id" class="check-icon">✓</span>
        </div>
        <div v-if="filteredOptions.length === 0 && !searchText" class="dropdown-empty">
          Không có dữ liệu
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onBeforeUnmount, nextTick } from 'vue'
import axios from 'axios'
import { toastSuccess, toastError } from '@/utils/toast'

const props = defineProps({
  modelValue: { type: Number, default: null },
  options: { type: Array, default: () => [] },
  labelKey: { type: String, required: true },
  label: { type: String, default: 'mục' },
  placeholder: { type: String, default: '-- Tìm hoặc chọn --' },
  apiUrl: { type: String, required: true }
})

const emit = defineEmits(['update:modelValue', 'item-added'])

const wrapper = ref(null)
const inputEl = ref(null)

const showDropdown = ref(false)
const searchText = ref('')
const saving = ref(false)
let blurTimer = null

// Sync display text with the selected value
watch(() => props.modelValue, (val) => {
  if (val != null) {
    const found = props.options.find(o => o.id === val)
    if (found) searchText.value = found[props.labelKey]
  } else {
    searchText.value = ''
  }
}, { immediate: true })

watch(() => props.options, () => {
  if (props.modelValue != null) {
    const found = props.options.find(o => o.id === props.modelValue)
    if (found) searchText.value = found[props.labelKey]
  }
})

const filteredOptions = computed(() => {
  if (!searchText.value) return props.options
  const keyword = searchText.value.toLowerCase().trim()
  return props.options.filter(item =>
    item[props.labelKey]?.toLowerCase().includes(keyword)
  )
})

// Show "Thêm mới" only when typed text has no exact match
const showAddNewOption = computed(() => {
  const text = searchText.value.trim()
  if (!text) return false
  return !props.options.some(item =>
    item[props.labelKey]?.toLowerCase() === text.toLowerCase()
  )
})

const openDropdown = () => {
  clearTimeout(blurTimer)
  showDropdown.value = true
}

const closeDropdown = () => {
  showDropdown.value = false
  // Restore display text if something is selected
  if (props.modelValue != null) {
    const found = props.options.find(o => o.id === props.modelValue)
    if (found) searchText.value = found[props.labelKey]
  } else {
    searchText.value = ''
  }
}

const toggleDropdown = () => {
  if (showDropdown.value) {
    closeDropdown()
    inputEl.value?.blur()
  } else {
    inputEl.value?.focus()
  }
}

const onBlur = () => {
  // Delay closing so clicks inside dropdown can process first
  blurTimer = setTimeout(() => {
    closeDropdown()
  }, 200)
}

const onSearch = () => {
  showDropdown.value = true
  if (!searchText.value.trim()) {
    emit('update:modelValue', null)
  }
}

const selectItem = (item) => {
  clearTimeout(blurTimer)
  emit('update:modelValue', item.id)
  searchText.value = item[props.labelKey]
  showDropdown.value = false
  inputEl.value?.blur()
}

const addNew = async () => {
  clearTimeout(blurTimer)
  const name = searchText.value.trim()
  if (!name || saving.value) return
  saving.value = true
  try {
    const res = await axios.post(props.apiUrl, { ten: name, trangThai: 1 })
    const created = res.data
    emit('item-added', created)
    nextTick(() => {
      emit('update:modelValue', created.id)
      searchText.value = created[props.labelKey] || name
    })
    toastSuccess(`Đã thêm: ${name}`)
    showDropdown.value = false
  } catch (e) {
    const msg = e.response?.data?.message || 'Không thể thêm mới'
    toastError(msg)
  } finally {
    saving.value = false
  }
}

// Close dropdown on click outside
const onClickOutside = (e) => {
  if (wrapper.value && !wrapper.value.contains(e.target)) {
    closeDropdown()
  }
}

onMounted(() => document.addEventListener('mousedown', onClickOutside))
onBeforeUnmount(() => {
  document.removeEventListener('mousedown', onClickOutside)
  clearTimeout(blurTimer)
})
</script>

<style scoped>
.quick-add-select {
  position: relative;
  width: 100%;
}

.quick-add-select.is-open {
  z-index: 999;
}

.select-input-wrap {
  position: relative;
}

.select-input-wrap .form-control {
  width: 100%;
  padding: 10px 32px 10px 12px;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  outline: none;
  transition: all 0.2s;
  font-size: 14px;
  cursor: text;
  background: #fff;
}

.select-input-wrap .form-control:focus {
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.select-input-wrap .form-control::placeholder {
  color: #000 !important;
  opacity: 0.5 !important;
  font-weight: 500;
}

.dropdown-arrow {
  position: absolute;
  right: 10px;
  top: 50%;
  transform: translateY(-50%);
  color: #94a3b8;
  cursor: pointer;
  pointer-events: auto;
  transition: transform 0.2s;
  padding: 4px;
}

.dropdown-arrow.open {
  transform: translateY(-50%) rotate(180deg);
}

/* DROPDOWN LIST */
.dropdown-list {
  position: absolute;
  top: calc(100% + 4px);
  left: 0;
  right: 0;
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.15);
  z-index: 9999;
  overflow: visible;
  animation: fadeSlideIn 0.15s ease-out;
}

@keyframes fadeSlideIn {
  from { opacity: 0; transform: translateY(-4px); }
  to { opacity: 1; transform: translateY(0); }
}

.dropdown-scroll {
  max-height: 200px;
  overflow-y: auto;
}

.dropdown-item {
  padding: 9px 14px;
  cursor: pointer;
  font-size: 14px;
  color: #334155;
  display: flex;
  justify-content: space-between;
  align-items: center;
  transition: background 0.1s;
}

.dropdown-item:hover {
  background: #f1f5f9;
}

.dropdown-item.selected {
  background: #eff6ff;
  color: #1e40af;
  font-weight: 600;
}

.check-icon {
  color: #3b82f6;
  font-size: 13px;
  font-weight: bold;
}

.dropdown-empty {
  padding: 12px 14px;
  color: #94a3b8;
  font-size: 13px;
  text-align: center;
}

/* ADD NEW ITEM */
.add-new-item {
  color: #3b82f6;
  font-weight: 600;
  border-bottom: 1px solid #f1f5f9;
  background: #f8fafc;
}

.add-new-item:hover {
  background: #eff6ff !important;
}

.add-label {
  font-size: 11px;
  font-weight: 500;
  color: #94a3b8;
  text-transform: uppercase;
}

.spinner-mini {
  width: 14px;
  height: 14px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.5s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}
</style>
