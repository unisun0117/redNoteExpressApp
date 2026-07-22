<template>
  <view class="result-page">
    <view v-if="orderNo" class="result-content">
      <text class="text-6xl mb-4">{{ statusIcon }}</text>
      <text class="text-xl font-extrabold text-gray-900 mb-2">{{ statusTitle }}</text>
      <text class="text-sm text-gray-500 mb-6">{{ statusSubtitle }}</text>

      <view class="info-card">
        <view class="flex justify-between mb-3">
          <text class="text-sm text-gray-500">订单编号</text>
          <text class="text-sm font-bold text-gray-900">{{ orderNo }}</text>
        </view>
        <view class="flex justify-between">
          <text class="text-sm text-gray-500">实付金额</text>
          <text class="text-base font-extrabold text-red-600">¥{{ paidAmount }}</text>
        </view>
      </view>

      <view class="flex gap-3 mt-8 w-full px-6">
        <view class="flex-1 py-3 rounded-2xl border border-gray-200 text-center" @click="goOrders">
          <text class="text-sm text-gray-700 font-medium">查看订单</text>
        </view>
        <view class="flex-1 py-3 rounded-2xl bg-red-600 text-center" @click="goHome">
          <text class="text-sm text-white font-bold">返回首页</text>
        </view>
      </view>
    </view>

    <view v-else class="result-content">
      <text class="text-6xl mb-4">❌</text>
      <text class="text-xl font-extrabold text-gray-900 mb-2">下单失败</text>
      <text class="text-sm text-gray-500 mb-8">请返回重试</text>
      <view class="py-3 px-10 rounded-2xl bg-red-600" @click="goBack">
        <text class="text-sm text-white font-bold">返回重试</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'

const orderNo = ref('')
const paidAmount = ref('')
const payStatus = ref('')

onMounted(() => {
  const pages = getCurrentPages()
  const page = pages[pages.length - 1] as any
  const options = page.$page?.options || page.options || {}
  orderNo.value = options.orderNo || ''
  paidAmount.value = options.paidAmount || '0'
  payStatus.value = options.payStatus || ''
})

const statusIcon = computed(() => {
  switch (payStatus.value) {
    case 'success': return '✅'
    case 'cancel': return '⏳'
    case 'fail': return '⚠️'
    default: return '✅'
  }
})

const statusTitle = computed(() => {
  switch (payStatus.value) {
    case 'success': return '支付成功'
    case 'cancel': return '下单成功，待支付'
    case 'fail': return '下单成功，支付失败'
    default: return '下单成功'
  }
})

const statusSubtitle = computed(() => {
  switch (payStatus.value) {
    case 'fail': return '可在订单列表重新支付'
    default: return '订单已提交，请耐心等待'
  }
})

function goHome() { uni.switchTab({ url: '/pages/index/index' }) }
function goOrders() { uni.redirectTo({ url: '/sub-pages/order/pages/index/index' }) }
function goBack() { uni.navigateBack() }
</script>

<style lang="scss" scoped>
.result-page { min-height: 100vh; background: #f5f5f5; display: flex; align-items: center; justify-content: center; }
.result-content { display: flex; flex-direction: column; align-items: center; padding: 32px; }
.info-card { background: #fff; border-radius: 12px; padding: 16px; width: 100%; box-shadow: 0 1px 4px rgba(0,0,0,0.04); }
</style>
