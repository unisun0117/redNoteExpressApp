import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'

// ===== 样式导入 =====
// Vant 4 组件样式（按需引入或者全局导入）
import 'vant/lib/index.css'
// Tailwind CSS
import './styles/tailwind.css'

// ===== 企业微信 JS-SDK 类型（全局声明生效） =====
// 类型声明在 src/types/wechat.d.ts 中，此处无需额外导入

const app = createApp(App)

app.use(router)
app.use(store)

app.mount('#app')
