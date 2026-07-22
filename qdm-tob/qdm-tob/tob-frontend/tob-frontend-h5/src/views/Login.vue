<template>
  <div class="cas-page">
    <div class="cas-card">
      <!-- Logo -->
      <div class="logo-area">
        <div class="logo-icon">Q</div>
        <h1 class="brand-name">钱鲜达</h1>
        <p class="brand-tagline">生鲜供应链智能管理平台</p>
      </div>

      <!-- 错误状态 -->
      <template v-if="isTicketError">
        <div class="error-icon">!</div>
        <p class="error-text">统一登录验证失败</p>
        <p class="error-desc">请检查网络连接后重试，或联系管理员</p>
        <button class="retry-btn" @click="goToCas">重新登录</button>
      </template>

      <template v-else-if="errorType === 'no_account'">
        <div class="error-icon">!</div>
        <p class="error-text">暂无系统权限</p>
        <p class="error-desc">
          CAS 登录成功，但您的账号尚未在系统中注册。<br />请联系管理员创建账号后再试。
        </p>
      </template>

      <!-- 正常跳转 -->
      <template v-else>
        <div class="loading-dots">
          <span class="dot" />
          <span class="dot" />
          <span class="dot" />
        </div>
        <p class="loading-text">正在跳转至统一登录...</p>
        <button class="manual-link" @click="goToCas">如无反应，点击此处手动跳转</button>
      </template>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '@/store/user'
import { loginByTicket } from '@/api/auth'
import { getCasLoginUrl } from '@/config/env'

const route = useRoute()
const userStore = useUserStore()
const isTicketError = ref(false)
const errorType = ref<'ticket_failed' | 'no_account' | null>(null)

function goToCas(): void {
  const redirect = (route.query.redirect as string) || undefined
  window.location.replace(getCasLoginUrl(redirect))
}

onMounted(() => {
  // 无账号错误
  if (route.query.error === 'no_account') {
    errorType.value = 'no_account'
    return
  }

  // ticket 换 token 失败
  if (route.query.error === 'ticket_failed') {
    errorType.value = 'ticket_failed'
    isTicketError.value = true
    return
  }

  // 自动跳转 CAS
  goToCas()
})
</script>

<style lang="less" scoped>
.cas-page {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  background: linear-gradient(160deg, #f0fdf4 0%, #f8fafc 50%, #f0f4ff 100%);
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC', sans-serif;
}

.cas-card {
  text-align: center;
  padding: 48px 36px;
  background: #fff;
  border-radius: 14px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.05), 0 1px 4px rgba(0, 0, 0, 0.03);
  max-width: 340px;
  width: 100%;
}

.logo-area {
  margin-bottom: 32px;
}

.logo-icon {
  width: 56px;
  height: 56px;
  line-height: 56px;
  border-radius: 50%;
  background: linear-gradient(135deg, #10b981, #059669);
  color: #fff;
  font-size: 28px;
  font-weight: 700;
  margin: 0 auto 16px;
}

.brand-name {
  font-size: 24px;
  font-weight: 600;
  color: #1e293b;
  margin: 0 0 4px;
}
.brand-tagline {
  font-size: 13px;
  color: #94a3b8;
  margin: 0;
}

.loading-dots {
  display: flex;
  justify-content: center;
  gap: 8px;
  margin-bottom: 20px;
}
.dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #10b981;
  animation: dotPulse 1.4s ease-in-out infinite;
  &:nth-child(2) { animation-delay: 0.2s; }
  &:nth-child(3) { animation-delay: 0.4s; }
}

@keyframes dotPulse {
  0%, 60%, 100% { opacity: 0.3; transform: scale(0.8); }
  30% { opacity: 1; transform: scale(1); }
}

.loading-text {
  font-size: 15px;
  color: #64748b;
  margin: 0 0 24px;
}

.manual-link {
  font-size: 14px;
  color: #6366f1;
  background: none;
  border: none;
  cursor: pointer;
  text-decoration: underline;
  text-underline-offset: 3px;
}

.error-icon {
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
.error-text {
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
  margin: 0 0 8px;
}
.error-desc {
  font-size: 14px;
  color: #94a3b8;
  margin: 0 0 28px;
  line-height: 1.6;
}
.retry-btn {
  font-size: 15px;
  font-weight: 600;
  color: #fff;
  background: #10b981;
  border: none;
  border-radius: 8px;
  padding: 12px 36px;
  cursor: pointer;
}
</style>
