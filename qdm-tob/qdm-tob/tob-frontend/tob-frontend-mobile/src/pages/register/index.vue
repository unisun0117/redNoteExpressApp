<template>
  <view class="min-h-screen flex flex-col" style="background: linear-gradient(180deg, #FEF2F2 0%, #FFF7ED 30%, #FFFFFF 70%);">
    <!-- ============================================================
         品牌区 + 表单区
         ============================================================ -->
    <view class="flex-1 flex flex-col items-center justify-center px-8 pt-10">
      <!-- 标题 -->
      <text class="text-2xl font-bold mb-1" style="color: #450A0A; font-family: serif;">
        注册账号
      </text>
      <text class="text-sm mb-10" style="color: #9A3412;">
        创建您的钱鲜达账号
      </text>

      <!-- ============================================================
           姓名输入
           ============================================================ -->
      <view class="w-full mb-4">
        <view class="flex items-center rounded-2xl px-4 py-3.5 transition-all duration-200"
          :class="nameInputFocus
            ? 'bg-white border-2'
            : 'bg-white/70 border-2'"
          :style="nameInputFocus
            ? 'border-color: #DC2626; box-shadow: 0 0 0 4px rgba(220,38,38,0.08);'
            : 'border-color: #FECDD3;'">
          <text class="mr-3 text-lg">👤</text>
          <input
            v-model="realName"
            type="text"
            placeholder="请输入姓名"
            maxlength="50"
            class="flex-1 text-base bg-transparent outline-none"
            style="color: #450A0A; min-height: 22px;"
            placeholder-style="color: #FCA5A5;"
            @focus="nameInputFocus = true"
            @blur="nameInputFocus = false"
          />
        </view>
      </view>

      <!-- ============================================================
           手机号输入
           ============================================================ -->
      <view class="w-full mb-4">
        <view class="flex items-center rounded-2xl px-4 py-3.5 transition-all duration-200"
          :class="phoneInputFocus
            ? 'bg-white border-2'
            : 'bg-white/70 border-2'"
          :style="phoneInputFocus
            ? 'border-color: #DC2626; box-shadow: 0 0 0 4px rgba(220,38,38,0.08);'
            : 'border-color: #FECDD3;'">
          <text class="mr-3 text-lg">📱</text>
          <input
            v-model="phone"
            type="number"
            placeholder="请输入手机号"
            maxlength="11"
            class="flex-1 text-base bg-transparent outline-none"
            style="color: #450A0A; min-height: 22px;"
            placeholder-style="color: #FCA5A5;"
            @focus="phoneInputFocus = true"
            @blur="phoneInputFocus = false"
          />
        </view>
      </view>

      <!-- ============================================================
           验证码输入（SMS 跳过开关关闭时显示）
           ============================================================ -->
      <view v-if="smsEnabled" class="w-full mb-6">
        <view class="flex items-center rounded-2xl px-4 py-3.5 transition-all duration-200"
          :class="codeInputFocus
            ? 'bg-white border-2'
            : 'bg-white/70 border-2'"
          :style="codeInputFocus
            ? 'border-color: #DC2626; box-shadow: 0 0 0 4px rgba(220,38,38,0.08);'
            : 'border-color: #FECDD3;'">
          <text class="mr-3 text-lg">🔒</text>
          <input
            v-model="code"
            type="number"
            placeholder="请输入验证码"
            maxlength="6"
            class="flex-1 text-base bg-transparent outline-none"
            style="color: #450A0A; min-height: 22px;"
            placeholder-style="color: #FCA5A5;"
            @focus="codeInputFocus = true"
            @blur="codeInputFocus = false"
          />
          <view class="flex-shrink-0 ml-3">
            <text
              v-if="!countdown"
              class="text-base font-semibold"
              style="color: #DC2626;"
              @click="sendCode"
            >
              获取验证码
            </text>
            <text v-else class="text-base" style="color: #FCA5A5;">
              {{ countdown }}s 后重发
            </text>
          </view>
        </view>
      </view>

      <!-- ============================================================
           注册按钮
           ============================================================ -->
      <view class="w-full mb-5">
        <u-button
          shape="circle"
          :custom-style="registerBtnStyle"
          :loading="loading"
          :disabled="!canSubmit"
          @click="handleRegister"
        >
          <text class="text-lg font-semibold" style="color: #FFFFFF;">注 册</text>
        </u-button>
      </view>

      <!-- ============================================================
           合规协议
           ============================================================ -->
      <view class="flex items-center mb-6" @click="agreed = !agreed">
        <view
          class="w-4 h-4 rounded-full flex-shrink-0 flex items-center justify-center mr-2 transition-all duration-200"
          :style="agreed
            ? 'background: #DC2626; border: none;'
            : 'background: transparent; border: 2px solid #FECDD3;'"
        >
          <text v-if="agreed" class="text-white text-sm">✓</text>
        </view>
        <text class="text-sm leading-5 flex-1" style="color: #9A3412;">
          我已认真阅读、理解和同意
          <text class="font-medium" style="color: #DC2626;" @click.stop="openAgreement('terms')">
            《钱鲜达服务协议》
          </text>
          、
          <text class="font-medium" style="color: #DC2626;" @click.stop="openAgreement('privacy')">
            《隐私政策》
          </text>
          ，授权使用本手机号码注册
        </text>
      </view>

      <!-- ============================================================
           去登录
           ============================================================ -->
      <view class="w-full text-center">
        <text class="text-base" style="color: #9A3412;">
          已有账号？
          <text class="font-semibold" style="color: #DC2626;" @click="goLogin">
            去登录
          </text>
        </text>
      </view>
    </view>
  </view>
</template>

<!-- ==================================================================
     Script
     ================================================================== -->
<script setup lang="ts">
import { ref, computed } from 'vue'
import { userApi } from '@/api'
import { useUserStore } from '@/store/modules/user'

// ------------------------------------------------------------------
// 短信验证码开关（通过环境变量 VITE_SMS_BYPASS_ENABLED 控制）
// 生产环境始终走真实短信验证码；本地联调时可设 VITE_SMS_BYPASS_ENABLED=true 跳过。
// ------------------------------------------------------------------
const smsEnabled = true

/** 手机号脱敏：138****1234 */
function maskMobile(m: string): string {
  if (!m || m.length < 11) return m
  return `${m.slice(0, 3)}****${m.slice(7)}`
}

// ------------------------------------------------------------------
// 响应式数据
// ------------------------------------------------------------------

const realName = ref('')
const phone = ref('')
const code = ref('')
const agreed = ref(false)
const loading = ref(false)
const countdown = ref(0)

const nameInputFocus = ref(false)
const phoneInputFocus = ref(false)
const codeInputFocus = ref(false)

const userStore = useUserStore()

// ------------------------------------------------------------------
// 计算属性：注册按钮是否可点
// ------------------------------------------------------------------

const canSubmit = computed(() => {
  // 原型约定：字段填齐 + 勾选协议后才可点击（未满足置灰），不再走弹框
  const base = realName.value.trim().length > 0 && /^1[3-9]\d{9}$/.test(phone.value)
  return base && (!smsEnabled || code.value.length > 0) && agreed.value
})

// ------------------------------------------------------------------
// 按钮样式
// ------------------------------------------------------------------

const registerBtnStyle = {
  height: '50px',
  background: 'linear-gradient(135deg, #DC2626 0%, #EA580C 100%)',
  border: 'none',
  boxShadow: '0 4px 20px rgba(220, 38, 38, 0.3)',
}

// ------------------------------------------------------------------
// 校验
// ------------------------------------------------------------------

function validateName(): boolean {
  const n = realName.value.trim()
  if (!n) {
    uni.showToast({ title: '请输入姓名', icon: 'none' })
    return false
  }
  if (n.length > 50) {
    uni.showToast({ title: '姓名不能超过50字符', icon: 'none' })
    return false
  }
  return true
}

function validatePhone(): boolean {
  if (!/^1[3-9]\d{9}$/.test(phone.value)) {
    uni.showToast({ title: '请输入正确的手机号', icon: 'none' })
    return false
  }
  return true
}

// ------------------------------------------------------------------
// 获取验证码 + 倒计时
// ------------------------------------------------------------------

async function sendCode() {
  if (!validatePhone()) return
  if (countdown.value > 0) return
  try {
    const res = await userApi.sendSmsCode(phone.value)
    if (res.code === 0 || res.code === 200) {
      uni.showToast({ title: `验证码已发送至 ${maskMobile(phone.value)}`, icon: 'none' })
      startCountdown()
    } else {
      const msg = res.msg || '验证码发送失败'
      if (res.code === 403) {
        uni.showModal({ title: '操作提示', content: msg, showCancel: false, confirmText: '知道了' })
      } else {
        uni.showToast({ title: msg, icon: 'none', duration: 3000 })
      }
    }
  } catch {
    uni.showToast({ title: '网络异常，请稍后重试', icon: 'none' })
  }
}

function startCountdown() {
  countdown.value = 60
  const timer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      clearInterval(timer)
    }
  }, 1000)
}

// ------------------------------------------------------------------
// 注册
// ------------------------------------------------------------------

function handleRegister() {
  if (!validateName() || !validatePhone()) return
  if (smsEnabled && !code.value) {
    uni.showToast({ title: '请输入验证码', icon: 'none' })
    return
  }
  // 协议已通过 canSubmit 置灰保证勾选，此处无需再弹框
  doRegister()
}

async function doRegister() {
  loading.value = true
  try {
    const res = await userApi.register({
      realName: realName.value.trim(),
      phone: phone.value,
      // SMS 跳过时后端不校验验证码；开启时按用户输入提交
      ...(smsEnabled ? { smsCode: code.value } : {}),
    })
    // 注册成功即登录
    userStore.setToken(res.data.token)
    userStore.setUserInfo({
      userId: '',
      userName: realName.value.trim(),
      avatar: '',
      phone: phone.value,
      email: '',
    })
    // 持久化姓名，供首页展示（注册场景后端 token 里也带 nm）
    uni.setStorageSync('userName', realName.value.trim())
    uni.showToast({ title: '注册成功', icon: 'success' })
    setTimeout(() => {
      uni.switchTab({ url: '/pages/index/index' })
    }, 600)
  } catch {
    // 拦截器已 toast（如「该手机号已注册」「验证码错误」）
  } finally {
    loading.value = false
  }
}

// ------------------------------------------------------------------
// 跳转
// ------------------------------------------------------------------

function goLogin() {
  uni.navigateBack({ fail: () => uni.redirectTo({ url: '/pages/login/index' }) })
}

function openAgreement(_type: 'privacy' | 'terms') {
  uni.showToast({ title: '协议全文页待开发', icon: 'none' })
}
</script>

<!-- ==================================================================
     Style
     ================================================================== -->
<style lang="scss" scoped>
/* 全部样式通过内联 + Tailwind 实现 */
</style>
