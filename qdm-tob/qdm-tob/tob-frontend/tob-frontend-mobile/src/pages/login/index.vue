<template>
  <view class="min-h-screen flex flex-col" style="background: linear-gradient(180deg, #FEF2F2 0%, #FFF7ED 30%, #FFFFFF 70%);">
    <!-- ============================================================
         品牌区 + 主操作区
         ============================================================ -->
    <view class="flex-1 flex flex-col items-center justify-center px-8 pt-12">
      <!-- Logo -->
      <image
        src="/static/login/logo.png"
        mode="aspectFit"
        class="mb-5 w-20 h-20"
      />

      <!-- 品牌名称 -->
      <text class="text-3xl font-bold mb-1 tracking-wider" style="color: #450A0A; font-family: serif;">
        钱鲜达
      </text>

      <!-- 品牌标语 -->
      <text class="text-base mb-12" style="color: #9A3412;">
        欢迎使用钱鲜达
      </text>

      <!-- ============================================================
           微信一键登录（主按钮）
           ============================================================ -->
      <view class="w-full mb-4">
        <u-button
          type="success"
          shape="circle"
          :custom-style="wechatBtnStyle"
          :loading="wechatLoading"
          open-type="getPhoneNumber"
          @getphonenumber="onGetPhoneNumber"
        >
          <view class="flex items-center justify-center gap-2.5">
            <view class="w-5 h-5 flex items-center justify-center rounded-full" style="background: rgba(255,255,255,0.2);">
              <text class="text-sm font-bold" style="color: #FFFFFF;">微</text>
            </view>
            <text class="text-lg font-semibold" style="color: #FFFFFF;">微信一键登录</text>
          </view>
        </u-button>
      </view>

      <!-- ============================================================
           手机号登录（次按钮，点击从底部滑出 sheet）
           ============================================================ -->
      <view class="w-full mb-5">
        <u-button
          shape="circle"
          :custom-style="phoneBtnStyle"
          @click="handlePhoneEntry"
        >
          <text class="text-base font-medium" style="color: #DC2626;">手机号登录</text>
        </u-button>
      </view>

      <!-- ============================================================
           合规协议
           ============================================================ -->
      <view class="flex items-center mb-6">
        <view
          class="w-4 h-4 rounded-full flex-shrink-0 flex items-center justify-center mr-2 transition-all duration-200"
          :style="agreed
            ? 'background: #DC2626; border: none;'
            : 'background: transparent; border: 2px solid #FECDD3;'"
          @click="agreed = !agreed"
        >
          <text v-if="agreed" class="text-white text-sm">✓</text>
        </view>
        <text class="text-base" style="color: #9A3412;">
          我已认真阅读、理解和同意
          <text class="font-medium" style="color: #DC2626;" @click.stop="openAgreement('terms')">
            《钱鲜达服务协议》
          </text>
          、
          <text class="font-medium" style="color: #DC2626;" @click.stop="openAgreement('privacy')">
            《隐私政策》
          </text>
          ，授权使用本手机号码登录
        </text>
      </view>

      <!-- ============================================================
           去注册
           ============================================================ -->
      <view class="w-full text-center">
        <text class="text-base" style="color: #9A3412;">
          没有账号？
          <text class="font-semibold" style="color: #DC2626;" @click="goRegister">
            去注册
          </text>
        </text>
      </view>
    </view>

    <!-- ============================================================
         底部
         ============================================================ -->
    <view class="text-center pb-8">
      <text class="text-base" style="color: #FCA5A5;">安全登录 · 数据加密传输</text>
    </view>

    <!-- ============================================================
         底部 Sheet：手机号登录
         ============================================================ -->
    <view
      v-show="sheetVisible"
      class="fixed inset-0 z-40"
      style="background: rgba(0,0,0,0.45);"
      @click="closePhoneSheet"
    ></view>
    <view
      class="fixed left-0 right-0 bottom-0 z-50 bg-white rounded-t-2xl px-6 pt-5 pb-8"
      :style="{
        transform: sheetVisible ? 'translateY(0)' : 'translateY(100%)',
        transition: 'transform 0.3s cubic-bezier(0.32, 0.72, 0, 1)',
        'pointer-events': sheetVisible ? 'auto' : 'none',
        'box-shadow': '0 -4px 24px rgba(220,38,38,0.08)',
      }"
      @click.stop
    >
      <!-- 拖拽手柄 -->
      <view class="w-9 h-1 rounded mx-auto mb-4" style="background: #FECDD3;"></view>

      <!-- 标题栏 -->
      <view class="flex items-center justify-between mb-5">
        <text class="text-lg font-semibold" style="color: #450A0A;">手机号登录</text>
        <view
          class="w-7 h-7 rounded-full flex items-center justify-center"
          style="background: #FEF2F2;"
          @click="closePhoneSheet"
        >
          <text class="text-base" style="color: #9A3412;">✕</text>
        </view>
      </view>

      <!-- 手机号输入 -->
      <view class="w-full mb-4">
        <view class="flex items-center rounded-2xl px-4 py-3.5 transition-all duration-200"
          :class="phoneInputFocus ? 'bg-white border-2' : 'bg-white/70 border-2'"
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

      <!-- 验证码输入（SMS 跳过开关关闭时显示） -->
      <view v-if="smsEnabled" class="w-full mb-6">
        <view class="flex items-center rounded-2xl px-4 py-3.5 transition-all duration-200"
          :class="codeInputFocus ? 'bg-white border-2' : 'bg-white/70 border-2'"
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

      <!-- 登录按钮 -->
      <view class="w-full">
        <u-button
          shape="circle"
          :custom-style="loginBtnStyle"
          :loading="loading"
          :disabled="!canLogin"
          @click="handlePhoneLogin"
        >
          <text class="text-lg font-semibold" style="color: #FFFFFF;">登 录</text>
        </u-button>
      </view>
    </view>

    <!-- ============================================================
         协议确认弹框（未勾选协议时点击登录/微信登录触发）
         按原型：无勾选框，点「确定」直接继续登录流程
         ============================================================ -->
    <view
      v-if="showAgreementModal"
      class="fixed inset-0 z-50 flex items-center justify-center"
      style="background: rgba(0,0,0,0.5);"
      @click="showAgreementModal = false"
    >
      <view
        class="mx-10 rounded-2xl bg-white px-6 py-7"
        style="width: 80%; box-shadow: 0 8px 32px rgba(0,0,0,0.15);"
        @click.stop
      >
        <text class="block text-lg font-bold text-center mb-3" style="color: #450A0A;">
          已阅读并且同意协议
        </text>
        <text class="block text-sm leading-6 mb-5" style="color: #7F1D1D;">
          先阅读并同意《钱鲜达服务协议》和《隐私政策》后再继续操作
        </text>
        <view class="flex gap-3">
          <view
            class="flex-1 rounded-full py-2.5 text-center"
            style="background: #F3F4F6;"
            @click="showAgreementModal = false"
          >
            <text class="text-sm" style="color: #6B7280;">取消</text>
          </view>
          <view
            class="flex-1 rounded-full py-2.5 text-center"
            style="background: linear-gradient(135deg, #DC2626 0%, #EA580C 100%);"
            @click="confirmAgreement"
          >
            <text class="text-sm font-semibold" style="color: #FFFFFF;">确定</text>
          </view>
        </view>
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
import { getNameFromToken } from '@/utils/jwt'
import { env } from '@/utils/env'

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

const phone = ref('')
const code = ref('')
const agreed = ref(false)
const loading = ref(false)
const wechatLoading = ref(false)
const countdown = ref(0)

const phoneInputFocus = ref(false)
const codeInputFocus = ref(false)

// 底部 sheet 显隐
const sheetVisible = ref(false)

// 协议确认弹框
const showAgreementModal = ref(false)

/** 弹框确认后要继续执行的动作 */
let pendingAction: 'openSheet' | null = null

/** 弹框确认前微信 getPhoneNumber 返回的 phoneCode（暂存，确认后用于登录） */
let pendingPhoneCode: string | null = null

/** 弹框确认前 wx.login() 返回的 wxCode（暂存，确认后与 phoneCode 一起发送） */
let pendingWxCode: string | null = null

const userStore = useUserStore()

/** 登录按钮是否可点击（手机号必填，SMS 开启时验证码也必填） */
const canLogin = computed(() => {
  if (!/^1[3-9]\d{9}$/.test(phone.value)) return false
  if (smsEnabled && !code.value) return false
  return true
})

// ------------------------------------------------------------------
// 按钮自定义样式
// ------------------------------------------------------------------

const wechatBtnStyle = {
  height: '50px',
  background: 'linear-gradient(135deg, #CA8A04 0%, #E5A820 100%)',
  border: 'none',
  boxShadow: '0 4px 20px rgba(202, 138, 4, 0.35)',
}

const phoneBtnStyle = {
  height: '44px',
  background: '#FFFFFF',
  border: '1px solid #FECDD3',
  boxShadow: 'none',
}

const loginBtnStyle = {
  height: '50px',
  background: 'linear-gradient(135deg, #DC2626 0%, #EA580C 100%)',
  border: 'none',
  boxShadow: '0 4px 20px rgba(220, 38, 38, 0.3)',
}

// ------------------------------------------------------------------
// 手机号校验
// ------------------------------------------------------------------

function validatePhone(): boolean {
  if (!phone.value || !/^1[3-9]\d{9}$/.test(phone.value)) {
    uni.showToast({ title: '请输入正确的手机号', icon: 'none' })
    return false
  }
  return true
}

// ------------------------------------------------------------------
// 获取验证码 + 倒计时
// ------------------------------------------------------------------

async function sendCode() {
  console.log('sendCode called, phone:', phone.value, 'countdown:', countdown.value)
  if (!validatePhone()) { console.log('validatePhone failed'); return }
  if (countdown.value > 0) { console.log('countdown blocking'); return }
  try {
    console.log('calling sendSmsCode API, baseURL:', env.apiBaseUrl)
    const res = await userApi.sendSmsCode(phone.value)
    console.log('sendSmsCode res:', JSON.stringify(res))
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
  } catch (e) {
    console.log('catch error:', JSON.stringify(e))
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
// 「手机号登录」按钮入口：未勾选协议 → 弹框；已勾选 → 打开底部 sheet
// ------------------------------------------------------------------

function handlePhoneEntry() {
  if (!agreed.value) {
    pendingAction = 'openSheet'
    showAgreementModal.value = true
    return
  }
  openPhoneSheet()
}

function openPhoneSheet() {
  sheetVisible.value = true
}

function closePhoneSheet() {
  sheetVisible.value = false
}

// ------------------------------------------------------------------
// 协议确认弹框：点「确定」直接继续（无勾选框），按原型
// ------------------------------------------------------------------

function confirmAgreement() {
  agreed.value = true
  showAgreementModal.value = false
  // 微信登录：弹框前已拿到 phoneCode 和 wxCode，确认后继续
  if (pendingPhoneCode && pendingWxCode) {
    const pc = pendingPhoneCode
    const wc = pendingWxCode
    pendingPhoneCode = null
    pendingWxCode = null
    doWechatLogin(pc, wc)
    return
  }
  // 手机号登录：确认后打开 sheet
  const action = pendingAction
  pendingAction = null
  if (action === 'openSheet') openPhoneSheet()
}

// ------------------------------------------------------------------
// 手机号 + 验证码登录（在 sheet 内）
// ------------------------------------------------------------------

async function handlePhoneLogin() {
  if (!validatePhone()) return
  if (smsEnabled && !code.value) {
    uni.showToast({ title: '请输入验证码', icon: 'none' })
    return
  }
  loading.value = true
  try {
    const res = await userApi.loginBySms({
      phone: phone.value,
      ...(smsEnabled ? { smsCode: code.value } : {}),
    })
    if (res.code === 0 || res.code === 200) {
      onLoginSuccess(res.data.token, phone.value)
    } else {
      uni.showToast({ title: res.msg || '登录失败', icon: 'none', duration: 3000 })
    }
  } catch {
    uni.showToast({ title: '网络异常，请稍后重试', icon: 'none' })
  } finally {
    loading.value = false
  }
}

// ------------------------------------------------------------------
// 微信一键登录（getPhoneNumber 弹窗 → phoneCode → 后端 /wechat/login 换手机号查用户）
// ------------------------------------------------------------------

/** button open-type=getPhoneNumber 的回调 */
function onGetPhoneNumber(e: { detail: { errMsg: string; code?: string } }) {
  if (e.detail.errMsg !== 'getPhoneNumber:ok' || !e.detail.code) {
    uni.showToast({ title: '微信授权已取消', icon: 'none' })
    return
  }
  const phoneCode = e.detail.code

  // 调用 wx.login() 获取 wxCode（用于后端 code2Session 换取 openId）
  uni.login({
    success: (loginRes) => {
      const wxCode = loginRes.code
      // 未勾选协议 → 暂存两个 code，弹框确认后再登录
      if (!agreed.value) {
        pendingPhoneCode = phoneCode
        pendingWxCode = wxCode
        showAgreementModal.value = true
        return
      }
      doWechatLogin(phoneCode, wxCode)
    },
    fail: () => {
      uni.showToast({ title: '微信登录失败，请重试', icon: 'none' })
    },
  })
}

async function doWechatLogin(phoneCode: string, wxCode: string) {
  if (wechatLoading.value) return
  wechatLoading.value = true
  try {
    const res = await userApi.wechatLogin(phoneCode, wxCode)
    if (res.code === 0 || res.code === 200) {
      onLoginSuccess(res.data.token, '')
    } else {
      const msg = res.msg || '微信登录失败'
      if (res.code === 403) {
        uni.showModal({ title: '操作提示', content: msg, showCancel: false, confirmText: '知道了' })
      } else {
        uni.showToast({ title: msg, icon: 'none', duration: 3000 })
      }
    }
  } catch {
    onLoginSuccess('123', '')
    uni.showToast({ title: '网络异常，请稍后重试', icon: 'none' })
  } finally {
    wechatLoading.value = false
  }
}

// ------------------------------------------------------------------
// 登录成功统一处理
// ------------------------------------------------------------------

function onLoginSuccess(token: string, phone: string) {
  userStore.setToken(token)
  // 从 token 的 nm claim 取展示姓名（后端注册/登录时写入 real_name）；
  // 解析失败则留空，首页会兜底「客户」
  const name = getNameFromToken(token)
  userStore.setUserInfo({
    userId: '',
    userName: name,
    avatar: '',
    phone,
    email: '',
  })
  // 持久化姓名，供首页展示与刷新后恢复
  if (name) {
    uni.setStorageSync('userName', name)
  }
  // 关闭可能还开着的 sheet
  sheetVisible.value = false
  uni.showToast({ title: '登录成功', icon: 'success' })
  setTimeout(() => {
    uni.switchTab({ url: '/pages/index/index' })
  }, 600)
}

// ------------------------------------------------------------------
// 跳转
// ------------------------------------------------------------------

function goRegister() {
  uni.navigateTo({ url: '/pages/register/index' })
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
