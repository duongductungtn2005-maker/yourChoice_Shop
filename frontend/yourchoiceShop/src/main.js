import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router' // Import router của bạn

// 1. IMPORT FONTAWESOME
import { library } from '@fortawesome/fontawesome-svg-core'
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome'
import { fas } from '@fortawesome/free-solid-svg-icons'
import { far } from '@fortawesome/free-regular-svg-icons'

// 2. IMPORT CSS (Nếu có file css chung)
import './assets/main.css' // Ví dụ file css

// 3. THÊM ICON VÀO THƯ VIỆN
library.add(fas, far)

// --- KHỞI TẠO APP (QUAN TRỌNG: Dòng này phải nằm trước khi dùng biến 'app') ---
const app = createApp(App)

// 4. ĐĂNG KÝ COMPONENT & PLUGIN SAU KHI ĐÃ CÓ 'app'
app.component('font-awesome-icon', FontAwesomeIcon) // <--- Lỗi của bạn nằm ở đây (do đặt trước dòng const app)

app.use(router)
app.use(createPinia())

// 5. MOUNT (Cuối cùng)
app.mount('#app')