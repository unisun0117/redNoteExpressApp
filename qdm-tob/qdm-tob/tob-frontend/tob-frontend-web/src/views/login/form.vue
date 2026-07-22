<template>
  <!-- ====== 本地开发：手动登录表单（仅 ?devForm=1 时显示） ====== -->
  <div v-if="showDevForm" class="login-page">
    <a href="#login-form" class="skip-link">跳转到登录表单</a>

    <!-- 左侧品牌面板 -->
    <aside class="brand-panel" aria-label="品牌信息">
      <div class="glow-orb glow-orb--1" aria-hidden="true" />
      <div class="glow-orb glow-orb--2" aria-hidden="true" />

      <div class="brand-card">
        <div class="logo-badge" aria-hidden="true">
          <svg viewBox="0 0 44 44" fill="none" xmlns="http://www.w3.org/2000/svg">
            <circle cx="22" cy="22" r="20" fill="#F0FDF4" stroke="#10B981" stroke-width="2" />
            <path d="M14 22c0 4.4 3.6 8 8 8s8-3.6 8-8-3.6-8-8-8-8 3.6-8 8Z" fill="#10B981" />
            <circle cx="22" cy="20" r="2.5" fill="#FFFFFF" />
            <path
              d="M18 27c0-2.2 1.8-4 4-4s4 1.8 4 4"
              stroke="#FFFFFF"
              stroke-width="1.5"
              stroke-linecap="round"
            />
          </svg>
        </div>

        <h1 class="brand-name">钱鲜达</h1>
        <p class="brand-tagline">生鲜供应链智能管理平台</p>

        <ul class="feature-stack" aria-label="核心功能">
          <li class="feature-chip">冷链全程监控</li>
          <li class="feature-chip">库存智能预警</li>
          <li class="feature-chip">品质一码溯源</li>
        </ul>
      </div>
    </aside>

    <!-- 右侧表单 -->
    <main id="login-form" class="form-panel" aria-labelledby="form-heading">
      <div class="form-card">
        <div class="ai-status" aria-hidden="true">
          <span class="ai-dot" />
          <span class="ai-dot" />
          <span class="ai-dot" />
          <span class="ai-label">本地开发</span>
        </div>

        <header class="form-header">
          <h2 id="form-heading" class="form-title">登录钱鲜达</h2>
          <p class="form-desc">本地开发环境，请输入账号密码</p>
        </header>

        <el-form
          ref="formRef"
          :model="loginForm"
          :rules="formRules"
          class="login-form"
          size="large"
          @submit.prevent="handleLogin"
        >
          <div class="field-group">
            <label for="username-input" class="field-label">用户名 / 手机号</label>
            <el-input
              id="username-input"
              v-model="loginForm.username"
              placeholder="请输入用户名或手机号"
              :prefix-icon="User"
              clearable
              autocomplete="username"
              aria-label="用户名或手机号输入框"
            />
          </div>

          <div class="field-group">
            <label for="password-input" class="field-label">密码</label>
            <el-input
              id="password-input"
              v-model="loginForm.password"
              type="password"
              placeholder="请输入密码"
              :prefix-icon="Lock"
              show-password
              autocomplete="current-password"
              aria-label="密码输入框"
              @keyup.enter="handleLogin"
            />
          </div>

          <div class="form-extra">
            <label class="remember-label">
              <input v-model="loginForm.remember" type="checkbox" class="remember-check" />
              <span>记住登录状态</span>
            </label>
          </div>

          <button type="submit" class="submit-btn" :disabled="loading" :aria-busy="loading">
            <template v-if="loading">
              <span class="btn-dot" />
              <span class="btn-dot" />
              <span class="btn-dot" />
            </template>
            <template v-else>登 录</template>
          </button>
        </el-form>
      </div>
    </main>
  </div>

  <!-- ====== 测试/生产：CAS 跳转页 ====== -->
  <div v-else class="cas-page">
    <div class="cas-card">
      <div class="logo-badge" aria-hidden="true">
        <svg viewBox="0 0 44 44" fill="none" xmlns="http://www.w3.org/2000/svg">
          <circle cx="22" cy="22" r="20" fill="#F0FDF4" stroke="#10B981" stroke-width="2" />
          <path d="M14 22c0 4.4 3.6 8 8 8s8-3.6 8-8-3.6-8-8-8-8 3.6-8 8Z" fill="#10B981" />
          <circle cx="22" cy="20" r="2.5" fill="#FFFFFF" />
          <path
            d="M18 27c0-2.2 1.8-4 4-4s4 1.8 4 4"
            stroke="#FFFFFF"
            stroke-width="1.5"
            stroke-linecap="round"
          />
        </svg>
      </div>

      <h1 class="cas-brand">钱鲜达</h1>
      <p class="cas-tagline">生鲜供应链智能管理平台</p>

      <!-- 错误展示 -->
      <template v-if="errorMessage">
        <div class="cas-error-icon">!</div>
        <p class="cas-error-text">登录失败</p>
        <p class="cas-error-desc">{{ errorMessage }}</p>
      </template>

      <!-- 正常跳转 -->
      <template v-else>
        <div class="cas-loading">
          <span class="cas-dot" />
          <span class="cas-dot" />
          <span class="cas-dot" />
        </div>
        <p class="cas-text">正在跳转至统一登录...</p>
        <button class="cas-link" @click="goToCas">如无反应，点击此处手动跳转</button>
      </template>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { setToken, setRealName, setMobile } from '@/utils/auth'
import { isLocalDev, getCasLoginUrl } from '@/config/env'
import { loginByPassword } from '@/api/auth'

const router = useRouter()
const route = useRoute()

// 本地开发默认走 CAS；URL 带 ?devForm=1 时显示手动表单（逃生口）
const showDevForm = isLocalDev && route.query.devForm === '1'
const formRef = ref<FormInstance>()
const loading = ref(false)
const errorMessage = ref('')

// ===== 手动登录表单 =====
const loginForm = reactive({
  username: '',
  password: '',
  remember: false,
})

const formRules: FormRules = {
  username: [
    { required: true, message: '请输入用户名或手机号', trigger: 'blur' },
    { min: 2, message: '用户名至少 2 个字符', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少 6 个字符', trigger: 'blur' },
  ],
}

function handleLoginSuccess(data: {
  access_token: string
  real_name: string
  mobile: string
}): void {
  setToken(data.access_token)
  if (data.real_name) setRealName(data.real_name)
  if (data.mobile) setMobile(data.mobile)

  ElMessage.success('登录成功')
  router.replace('/')
}

async function handleLogin(): Promise<void> {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    loading.value = true
    try {
      const data = await loginByPassword(loginForm.username, loginForm.password)
      handleLoginSuccess(data)
    } catch {
      ElMessage.error('登录失败，请检查用户名和密码')
    } finally {
      loading.value = false
    }
  })
}

// ===== CAS 跳转 =====
function goToCas(): void {
  const redirect = (route.query.redirect as string) || undefined
  window.location.replace(getCasLoginUrl(redirect))
}

onMounted(() => {
  // 从 sessionStorage 读取错误信息
  const stored = sessionStorage.getItem('login_error')
  if (stored) {
    errorMessage.value = stored
    sessionStorage.removeItem('login_error')
    return
  }

  // 非手动表单模式：自动跳转 CAS（本地开发也走统一登录）
  if (!showDevForm) {
    goToCas()
  }
})
</script>

<style scoped>
/* ===== 跳过导航 ===== */
.skip-link {
  position: absolute;
  top: -100px;
  left: 16px;
  padding: 12px 20px;
  background: #1e293b;
  color: #fff;
  font-size: 16px;
  border-radius: 10px;
  z-index: 1000;
  text-decoration: none;
}
.skip-link:focus {
  top: 16px;
  outline: 3px solid #10b981;
  outline-offset: 2px;
}

/* ===== 手动登录页 ===== */
.login-page {
  display: flex;
  min-height: 100vh;
  font-family: 'Nunito Sans', sans-serif;
  background: #f8fafc;
  color: #1e293b;
}

.brand-panel {
  position: relative;
  flex: 0 0 460px;
  background: linear-gradient(160deg, #f0fdf4 0%, #f8fafc 60%, #f0f4ff 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.glow-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  pointer-events: none;
}
.glow-orb--1 {
  width: 320px;
  height: 320px;
  background: #10b981;
  top: -60px;
  right: -60px;
  opacity: 0.35;
}
.glow-orb--2 {
  width: 240px;
  height: 240px;
  background: #6366f1;
  bottom: -40px;
  left: -40px;
  opacity: 0.2;
}

.brand-card {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 28px;
  padding: 56px 48px;
  text-align: center;
}

.logo-badge {
  display: flex;
  justify-content: center;
}
.logo-badge svg {
  width: 64px;
  height: 64px;
  filter: drop-shadow(0 2px 8px rgba(16, 185, 129, 0.15));
}

.brand-name {
  font-family: 'Varela Round', sans-serif;
  font-size: 34px;
  font-weight: 600;
  color: #1e293b;
  margin: 0;
  letter-spacing: 3px;
}

.brand-tagline {
  font-size: 14px;
  color: #64748b;
  margin: -12px 0 0;
}

.feature-stack {
  list-style: none;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.feature-chip {
  font-size: 14px;
  color: #475569;
  background: rgba(255, 255, 255, 0.75);
  padding: 10px 28px;
  border-radius: 20px;
  box-shadow:
    0 2px 8px rgba(0, 0, 0, 0.04),
    0 1px 2px rgba(0, 0, 0, 0.03);
}

.form-panel {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 48px;
}

.form-card {
  width: 100%;
  max-width: 420px;
  background: #fff;
  padding: 48px 44px;
  border-radius: 14px;
  box-shadow:
    0 4px 24px rgba(0, 0, 0, 0.05),
    0 1px 4px rgba(0, 0, 0, 0.03);
}

.ai-status {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 32px;
  padding: 8px 16px;
  background: #f1f5f9;
  border-radius: 20px;
  width: fit-content;
}

.ai-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #10b981;
  animation: softPulse 2s ease-in-out infinite;
}
.ai-dot:nth-child(2) {
  animation-delay: 0.3s;
}
.ai-dot:nth-child(3) {
  animation-delay: 0.6s;
}

@keyframes softPulse {
  0%,
  100% {
    opacity: 0.35;
    transform: scale(0.8);
  }
  50% {
    opacity: 1;
    transform: scale(1);
  }
}

.ai-label {
  font-size: 12px;
  color: #64748b;
  margin-left: 6px;
}

.form-header {
  margin-bottom: 36px;
}
.form-title {
  font-family: 'Varela Round', sans-serif;
  font-size: 28px;
  font-weight: 600;
  color: #1e293b;
  margin: 0 0 6px;
}
.form-desc {
  font-size: 16px;
  color: #64748b;
  margin: 0;
}

.field-group {
  margin-bottom: 22px;
}
.field-label {
  display: block;
  font-size: 15px;
  font-weight: 600;
  color: #334155;
  margin-bottom: 8px;
}

.field-group :deep(.el-input__wrapper) {
  border-radius: 10px;
  padding: 6px 16px;
  background: #f8fafc;
  border: 1.5px solid #e2e8f0;
  box-shadow: inset 0 1px 3px rgba(0, 0, 0, 0.03);
  transition: all 200ms ease;
}
.field-group :deep(.el-input__wrapper:hover) {
  border-color: #cbd5e1;
  background: #fff;
}
.field-group :deep(.el-input__wrapper.is-focus) {
  border-color: #10b981;
  background: #fff;
  box-shadow:
    0 0 0 3px rgba(16, 185, 129, 0.1),
    inset 0 1px 3px rgba(0, 0, 0, 0.02);
}
.field-group :deep(.el-input__inner) {
  font-size: 16px;
  color: #1e293b;
}
.field-group :deep(.el-input__inner::placeholder) {
  color: #94a3b8;
}
.field-group :deep(.el-input__prefix) {
  color: #94a3b8;
  font-size: 18px;
}
.field-group :deep(.el-form-item__error) {
  font-size: 13px;
  color: #ef4444;
  padding-top: 6px;
}
.field-group :deep(.el-form-item__error)::before {
  content: '⚠ ';
  font-size: 12px;
}

.form-extra {
  display: flex;
  align-items: center;
  margin: 6px 0 28px;
}
.remember-label {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 15px;
  color: #475569;
  cursor: pointer;
  min-height: 44px;
}
.remember-check {
  width: 18px;
  height: 18px;
  accent-color: #10b981;
  border-radius: 4px;
}

.submit-btn {
  width: 100%;
  min-height: 50px;
  font-family: 'Varela Round', sans-serif;
  font-size: 17px;
  font-weight: 600;
  letter-spacing: 3px;
  color: #fff;
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  border: none;
  border-radius: 12px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  box-shadow:
    0 4px 16px rgba(16, 185, 129, 0.25),
    0 2px 4px rgba(16, 185, 129, 0.15),
    0 1px 2px rgba(0, 0, 0, 0.06);
  transition: all 250ms ease;
}
@media (prefers-reduced-motion: no-preference) {
  .submit-btn:hover {
    transform: translateY(-1px);
    box-shadow:
      0 6px 24px rgba(16, 185, 129, 0.3),
      0 3px 8px rgba(16, 185, 129, 0.2),
      0 1px 2px rgba(0, 0, 0, 0.06);
  }
  .submit-btn:active {
    transform: translateY(1px);
    box-shadow:
      0 2px 8px rgba(16, 185, 129, 0.2),
      0 1px 2px rgba(0, 0, 0, 0.06);
  }
}
.submit-btn:focus-visible {
  outline: 3px solid #10b981;
  outline-offset: 3px;
}
.submit-btn:disabled {
  background: #cbd5e1;
  color: #94a3b8;
  cursor: not-allowed;
  box-shadow: none;
}

.btn-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: #fff;
  animation: btnBounce 0.9s ease-in-out infinite;
}
.btn-dot:nth-child(2) {
  animation-delay: 0.15s;
}
.btn-dot:nth-child(3) {
  animation-delay: 0.3s;
}

@keyframes btnBounce {
  0%,
  80%,
  100% {
    transform: translateY(0);
  }
  40% {
    transform: translateY(-8px);
  }
}

.form-footer {
  text-align: center;
  font-size: 13px;
  color: #cbd5e1;
  margin-top: 28px;
}

/* ===== CAS 跳转页 ===== */
.cas-page {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  background: linear-gradient(160deg, #f0fdf4 0%, #f8fafc 50%, #f0f4ff 100%);
  font-family: 'Nunito Sans', sans-serif;
}

.cas-card {
  text-align: center;
  padding: 56px 48px;
  background: #fff;
  border-radius: 14px;
  box-shadow:
    0 4px 24px rgba(0, 0, 0, 0.05),
    0 1px 4px rgba(0, 0, 0, 0.03);
  max-width: 380px;
  width: 100%;
}

.cas-brand {
  font-family: 'Varela Round', sans-serif;
  font-size: 28px;
  font-weight: 600;
  color: #1e293b;
  margin: 20px 0 4px;
}
.cas-tagline {
  font-size: 13px;
  color: #94a3b8;
  margin: 0 0 36px;
}

.cas-loading {
  display: flex;
  justify-content: center;
  gap: 8px;
  margin-bottom: 20px;
}
.cas-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #10b981;
  animation: dotPulse 1.4s ease-in-out infinite;
}
.cas-dot:nth-child(2) {
  animation-delay: 0.2s;
}
.cas-dot:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes dotPulse {
  0%,
  60%,
  100% {
    opacity: 0.3;
    transform: scale(0.8);
  }
  30% {
    opacity: 1;
    transform: scale(1);
  }
}

.cas-text {
  font-size: 15px;
  color: #64748b;
  margin: 0 0 24px;
}

.cas-error-icon {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: #fef2f2;
  border: 2px solid #ef4444;
  color: #ef4444;
  font-size: 22px;
  font-weight: 700;
  line-height: 40px;
  margin: 0 auto 16px;
}
.cas-error-text {
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
  margin: 0 0 8px;
}
.cas-error-desc {
  font-size: 14px;
  color: #94a3b8;
  margin: 0 0 28px;
}
.cas-retry-btn {
  font-size: 15px;
  font-weight: 600;
  color: #fff;
  background: #10b981;
  border: none;
  border-radius: 8px;
  padding: 12px 36px;
  cursor: pointer;
}
.cas-retry-btn:hover {
  background: #059669;
}

.cas-link {
  font-size: 14px;
  color: #6366f1;
  background: none;
  border: none;
  cursor: pointer;
  text-decoration: underline;
  text-underline-offset: 3px;
}

/* ===== 响应式 ===== */
@media (max-width: 840px) {
  .brand-panel {
    display: none;
  }
  .form-panel {
    padding: 24px;
  }
  .form-card {
    padding: 36px 28px;
    box-shadow: none;
    border-radius: 0;
  }
}
</style>
