<template>
  <view class="min-h-screen bg-gray-50 flex flex-col">
    <!-- ================================================================
         页面标题
         ================================================================ -->
    <view class="bg-white px-5 py-4 border-b border-gray-100">
      <text class="text-lg font-bold" style="color: #450A0A;">微信支付测试</text>
      <text class="text-sm block mt-1" style="color: #9CA3AF;">输入金额后点击支付，调起微信收银台</text>
    </view>

    <!-- ================================================================
         金额输入区
         ================================================================ -->
    <view class="flex-1 px-5 pt-8">
      <view class="bg-white rounded-2xl px-5 py-6" style="box-shadow: 0 2px 12px rgba(0,0,0,0.04);">
        <!-- 金额标签 -->
        <text class="text-sm font-medium mb-2 block" style="color: #6B7280;">支付金额</text>

        <!-- 金额输入行 -->
        <view class="flex items-baseline border-b-2 pb-2 mb-1 transition-all duration-200"
          :style="amountFocus
            ? 'border-color: #DC2626;'
            : 'border-color: #E5E7EB;'">
          <text class="text-3xl font-bold mr-1" style="color: #450A0A;">¥</text>
          <input
            v-model="amountText"
            type="digit"
            placeholder="0.00"
            class="flex-1 text-3xl font-bold bg-transparent outline-none"
            style="color: #450A0A; min-height: 44px;"
            placeholder-style="color: #D1D5DB;"
            :focus="autoFocus"
            @focus="amountFocus = true"
            @blur="amountFocus = false"
          />
        </view>

        <!-- 金额提示 -->
        <text class="text-xs" style="color: #9CA3AF;">
          实际扣款 {{ amountYuan > 0 ? amountYuan.toFixed(2) : '0.00' }} 元
        </text>
      </view>

      <!-- ================================================================
           快捷金额
           ================================================================ -->
      <view class="mt-5">
        <text class="text-sm font-medium mb-3 block" style="color: #6B7280;">快捷金额</text>
        <view class="flex flex-wrap gap-3">
          <view
            v-for="item in quickAmounts"
            :key="item.label"
            class="rounded-full px-5 py-2.5 transition-all duration-200"
            :style="quickSelected === item.value
              ? 'background: #DC2626; border: 1px solid #DC2626;'
              : 'background: #FFFFFF; border: 1px solid #E5E7EB;'"
            @click="selectQuick(item.value)"
          >
            <text
              class="text-sm font-medium"
              :style="quickSelected === item.value ? 'color: #FFFFFF;' : 'color: #374151;'"
            >
              {{ item.label }}
            </text>
          </view>
        </view>
      </view>

      <!-- ================================================================
           订单描述
           ================================================================ -->
      <view class="mt-5 bg-white rounded-2xl px-5 py-4" style="box-shadow: 0 2px 12px rgba(0,0,0,0.04);">
        <text class="text-sm font-medium mb-2 block" style="color: #6B7280;">订单描述（选填）</text>
        <input
          v-model="description"
          placeholder="商品描述，如：测试商品"
          class="w-full text-base bg-transparent outline-none"
          style="color: #450A0A; min-height: 36px;"
          placeholder-style="color: #D1D5DB;"
        />
      </view>
    </view>

    <!-- ================================================================
         支付按钮
         ================================================================ -->
    <view class="px-5 pt-6 pb-8">
      <view
        class="w-full rounded-full py-3.5 flex items-center justify-center transition-all duration-200"
        :style="canPay
          ? 'background: linear-gradient(135deg, #DC2626 0%, #EA580C 100%); box-shadow: 0 4px 20px rgba(220, 38, 38, 0.3);'
          : 'background: #D1D5DB;'"
        @click="handlePay"
      >
        <!-- loading 状态 -->
        <template v-if="paying">
          <u-loading-icon mode="circle" color="#ffffff" size="20" />
          <text class="text-lg font-semibold ml-2" style="color: #FFFFFF;">支付中...</text>
        </template>
        <template v-else>
          <text class="text-lg font-semibold" style="color: #FFFFFF;">
            微信支付 ¥{{ amountYuan > 0 ? amountYuan.toFixed(2) : '0.00' }}
          </text>
        </template>
      </view>
    </view>

    <!-- ================================================================
         支付结果弹窗
         ================================================================ -->
    <view
      v-if="showResult"
      class="fixed inset-0 z-50 flex items-center justify-center"
      style="background: rgba(0,0,0,0.5);"
      @click="showResult = false"
    >
      <view
        class="mx-10 rounded-2xl bg-white px-6 py-7 text-center"
        style="width: 75%; box-shadow: 0 8px 32px rgba(0,0,0,0.15);"
        @click.stop
      >
        <text class="text-5xl block mb-4">{{ resultSuccess ? '✅' : '❌' }}</text>
        <text class="text-lg font-bold block mb-2" :style="resultSuccess ? 'color: #059669;' : 'color: #DC2626;'">
          {{ resultSuccess ? '支付成功' : '支付失败' }}
        </text>
        <text class="text-sm block mb-5" style="color: #6B7280;">{{ resultMsg }}</text>
        <view
          class="rounded-full py-2.5 text-center"
          style="background: linear-gradient(135deg, #DC2626 0%, #EA580C 100%);"
          @click="showResult = false"
        >
          <text class="text-sm font-semibold" style="color: #FFFFFF;">确定</text>
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
import { payApi } from '@/api'

// ------------------------------------------------------------------
// 状态
// ------------------------------------------------------------------

const amountText = ref('0.01')
const description = ref('测试支付')
const paying = ref(false)
const amountFocus = ref(false)
const autoFocus = ref(true)

// 弹窗
const showResult = ref(false)
const resultSuccess = ref(false)
const resultMsg = ref('')

// ------------------------------------------------------------------
// 快捷金额
// ------------------------------------------------------------------

const quickAmounts = [
  { label: '0.01 元', value: 0.01 },
  { label: '0.10 元', value: 0.10 },
  { label: '1.00 元', value: 1.00 },
  { label: '10.00 元', value: 10.00 },
]

const quickSelected = ref(0.01)

/** 金额文本 → 元 */
const amountYuan = computed(() => {
  const n = parseFloat(amountText.value)
  if (isNaN(n) || n <= 0) return 0
  return Math.round(n * 100) / 100
})

/** 是否可以支付 */
const canPay = computed(() => amountYuan.value > 0 && !paying.value)

// ------------------------------------------------------------------
// 快捷金额选中
// ------------------------------------------------------------------

function selectQuick(yuan: number) {
  quickSelected.value = yuan
  amountText.value = yuan.toFixed(2)
}

// ------------------------------------------------------------------
// 生成唯一商户订单号：TEST + 时间戳 + 4位随机数
// ------------------------------------------------------------------

function generateOutTradeNo(): string {
  const now = new Date()
  const pad = (n: number) => String(n).padStart(2, '0')
  const datePart =
    now.getFullYear().toString() +
    pad(now.getMonth() + 1) +
    pad(now.getDate()) +
    pad(now.getHours()) +
    pad(now.getMinutes()) +
    pad(now.getSeconds())
  const random = Math.floor(Math.random() * 10000)
    .toString()
    .padStart(4, '0')
  return `TEST${datePart}${random}`
}

// ------------------------------------------------------------------
// 调起支付
// ------------------------------------------------------------------

async function handlePay() {
  if (!canPay.value) return

  paying.value = true
  const outTradeNo = generateOutTradeNo()

  try {
    // ① 调后端下单接口，获取 prepay_id 及签名参数
    const res = await payApi.createOrder({
      description: description.value || '测试支付',
      outTradeNo,
      amountYuan: amountYuan.value,
    })

    if (res.code !== 0 && res.code !== 200) {
      showPayResult(false, res.msg || '下单失败')
      return
    }

    const payParams = res.data

    // ② 调起微信支付
    wx.requestPayment({
      appId: payParams.appId,
      timeStamp: payParams.timeStamp,
      nonceStr: payParams.nonceStr,
      package: payParams.packageStr,
      signType: payParams.signType || 'RSA',
      paySign: payParams.paySign,
      success: () => {
        showPayResult(true, `订单号 ${outTradeNo}\n请关注回调通知确认支付状态`)
      },
      fail: (err: any) => {
        // 用户取消支付
        if (err?.errMsg?.includes('cancel')) {
          showPayResult(false, '用户取消支付')
        } else {
          showPayResult(false, err?.errMsg || '支付调起失败')
        }
      },
    })
  } catch (e: any) {
    const msg = e?.data?.msg || e?.msg || '网络异常，请重试'
    showPayResult(false, msg)
  } finally {
    paying.value = false
  }
}

// ------------------------------------------------------------------
// 结果弹窗
// ------------------------------------------------------------------

function showPayResult(success: boolean, msg: string) {
  resultSuccess.value = success
  resultMsg.value = msg
  showResult.value = true
}
</script>

<!-- ==================================================================
     Style
     ================================================================== -->
<style lang="scss" scoped>
/* 全部样式通过内联 + Tailwind 实现 */
</style>
