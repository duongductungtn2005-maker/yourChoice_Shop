import { createApp } from 'vue'
import { createPinia } from 'pinia'

// 1. Import App gốc
import App from './App.vue'

// 2. Import Router (File router/index.js mà tôi đã đưa ở các bước trước)
import router from './router'

// 3. Import CSS Global (File reset CSS tôi đã đưa ở bước trước)
import './assets/main.css'

const app = createApp(App)

// 4. Sử dụng các Plugins
app.use(createPinia()) // Quản lý State
app.use(router)        // Quản lý Điều hướng

// 5. Mount vào DOM
app.mount('#app')