<template>
  <div class="login-page">
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

    <div class="cas-page form-panel" aria-labelledby="form-heading">
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

        <h3 class="cas-brand mb-2">登录钱鲜达</h3>

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
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, type FormInstance } from 'element-plus'
import { setToken, setRealName, setMobile } from '@/utils/auth'
import { getCasLoginUrl, envConfig } from '@/config/env'
import { loginByCasTicket } from '@/api/auth'

const router = useRouter()
const route = useRoute()

const { query } = parseUrlParams(route?.query?.redirect as string) || ''

const ticket = query.ticket

const errorMessage = ref('')

async function loginFn() {
  const redirect = (route?.query?.redirect as string) || '/home'
  if (ticket) {
    const redirect = (query.redirect as string) || '/home'
    const { code, data } = await loginByCasTicket(ticket, window.location.origin + redirect)
    if (code == 0) {
      setToken(data.token)
      setRealName('a')
      setMobile('b')
      window.location.replace(window.location.origin + redirect)
    } else {
      ElMessage.error('登录失败')
    }
  } else {
    goToCas()
  }
}

// ===== CAS 跳转 =====
function goToCas(): void {
  console.log(window.location)
  const redirect = (route.query.redirect as string) || '/home'
  window.location.replace(getCasLoginUrl(removeTicketParam(redirect) || '/home'))
}

function removeTicketParam(url: string) {
  const [path, queryString] = url.split('?')
  if (!queryString) return url
  const params = new URLSearchParams(decodeURIComponent(queryString))
  params.delete('ticket')

  // 重新拼接
  const newQuery = params.toString()
  return newQuery ? `${path}?${newQuery}` : path
}

function parseUrlParams(url: string) {
  try {
    const result = {
      path: '',
      query: {},
      fullPath: url,
    }

    // 处理可能存在的 # 号
    const hashIndex = url.indexOf('#')
    const cleanUrl = hashIndex !== -1 ? url.substring(0, hashIndex) : url

    const questionIndex = cleanUrl.indexOf('?')

    // 提取 path
    result.path = questionIndex !== -1 ? cleanUrl.substring(0, questionIndex) : cleanUrl

    // 提取 query
    if (questionIndex !== -1) {
      const queryString = cleanUrl.substring(questionIndex + 1)
      // 解码（处理 %26 等编码）
      const decoded = decodeURIComponent(queryString)

      decoded.split('&').forEach((param) => {
        if (param) {
          const [key, value] = param.split('=')
          if (key) {
            result.query[key] = value ? decodeURIComponent(value) : ''
          }
        }
      })
    }

    return result
  } catch (error) {
    console.error('解析URL失败:', error)
    return { path: url, query: {}, fullPath: url }
  }
}

onMounted(() => {
  const stored = sessionStorage.getItem('login_error')
  if (stored) {
    errorMessage.value = stored
    sessionStorage.removeItem('login_error')
    return
  }
  loginFn()
})
</script>

<style scoped>
/* 原有样式保持不变 */
/* .login-page {
  display: flex;
  min-height: 100vh;
  font-family: 'Nunito Sans', sans-serif;
  background: #f8fafc;
  color: #1e293b;
}
.brand-panel {
  position: relative;
  width: 40%;
  min-width: 420px;
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
  padding: 0;
}
.feature-chip {
  font-size: 14px;
  color: #475569;
  background: rgba(255, 255, 255, 0.75);
  padding: 10px 28px;
  border-radius: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}
.form-panel {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 48px;
}
.cas-page {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  background: linear-gradient(160deg, #f0fdf4 0%, #f8fafc 50%, #f0f4ff 100%);
}
.cas-card {
  text-align: center;
  padding: 56px 48px;
  background: #fff;
  border-radius: 14px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.05);
  max-width: 380px;
  width: 100%;
}
.cas-brand {
  font-family: 'Varela Round', sans-serif;
  font-size: 24px;
  font-weight: 600;
  color: #1e293b;
  margin: 20px 0 4px;
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
} */
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
/* .cas-text {
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
.cas-link {
  font-size: 14px;
  color: #6366f1;
  background: none;
  border: none;
  cursor: pointer;
  text-decoration: underline;
  text-underline-offset: 3px;
} */
@media (max-width: 840px) {
  .brand-panel {
    display: none;
  }
  .form-panel {
    padding: 24px;
  }
}
</style>
