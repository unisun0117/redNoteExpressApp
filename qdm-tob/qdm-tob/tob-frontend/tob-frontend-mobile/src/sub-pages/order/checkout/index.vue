<template>
  <view class="checkout-page">
    <!-- 收货地址 -->
    <view class="address-card" @click="handleAddressClick">
      <view class="flex items-center gap-3">
        <u-icon name="map" size="20" color="#DC2626"></u-icon>
        <view v-if="customerStore.current" class="flex-1">
          <view class="flex items-center gap-2 mb-1">
            <text class="font-bold text-base text-gray-900">{{ customerStore.current.contactName }}</text>
            <text class="text-sm text-gray-500">{{ customerStore.current.contactPhone }}</text>
          </view>
          <text class="text-sm text-gray-500">{{ customerStore.current.address }}</text>
        </view>
        <view v-else class="flex-1">
          <text class="text-sm text-gray-400">请选择收货地址</text>
        </view>
        <u-icon name="arrow-right" size="14" color="#9CA3AF"></u-icon>
      </view>
    </view>

    <!-- 商品明细 -->
    <view class="section-card" v-if="preview">
      <text class="section-title">商品明细</text>
      <view v-for="item in preview.items" :key="item.barcode" class="goods-row">
        <view class="flex-1 min-w-0">
          <text class="text-sm text-gray-900 line-clamp-1">{{ item.goodsName }}</text>
        </view>
        <text class="text-xs text-gray-400 mx-2">×{{ item.quantity }}</text>
        <text class="text-sm font-bold text-gray-900">¥{{ fmt(item.lineTotal) }}</text>
      </view>
    </view>

    <!-- 支付方式 -->
    <view class="section-card">
      <text class="section-title">支付方式</text>
      <view class="flex gap-3">
        <view
          v-for="pm in payMethods"
          :key="pm.value"
          class="pay-option"
          :class="payMethod === pm.value ? 'pay-option--active' : ''"
          @click="payMethod = pm.value"
        >
          <text>{{ pm.label }}</text>
        </view>
      </view>
    </view>

    <!-- 到货日期 + 备注 -->
    <view class="section-card">
      <view class="flex items-center justify-between mb-3">
        <text class="text-sm text-gray-900">到货日期</text>
        <text class="text-sm text-gray-500" @click="selectDate">
          {{ arrivalDate || '请选择' }}
        </text>
      </view>
      <input
        v-model="remark"
        class="remark-input"
        placeholder="备注（选填）"
        maxlength="200"
      />
    </view>

    <!-- 金额汇总 -->
    <view class="section-card" v-if="preview">
      <view class="flex justify-between mb-2">
        <text class="text-sm text-gray-500">商品总额</text>
        <text class="text-sm text-gray-900">¥{{ fmt(preview.goodsAmount) }}</text>
      </view>
      <view class="flex justify-between mb-2">
        <text class="text-sm text-gray-500">运费</text>
        <text class="text-sm text-gray-900">¥{{ fmt(preview.freightAmount) }}</text>
      </view>
      <view class="flex justify-between pt-2 border-t border-gray-100">
        <text class="text-base font-bold text-gray-900">实付</text>
        <text class="text-lg font-extrabold text-red-600">¥{{ fmt(preview.paidAmount) }}</text>
      </view>
    </view>

    <!-- 底部按钮 -->
    <view class="submit-bar" :style="{ paddingBottom: safeBottom + 10 + 'px' }">
      <view class="flex items-baseline gap-1 mr-4">
        <text class="text-xs text-gray-500">合计</text>
        <text class="text-xl font-extrabold text-red-600">¥{{ preview ? fmt(preview.paidAmount) : '--' }}</text>
      </view>
      <view class="submit-btn" :class="submitting ? 'submit-btn--loading' : ''" @click="handleSubmit">
        <text class="text-white text-base font-bold">{{ submitting ? '提交中...' : '提交订单' }}</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { previewOrder, submitOrder } from '@/api/modules/order'
import { getAddressList } from '@/api/modules/companyAddress'
import { useCustomerStore } from '@/store/modules/customer'
import type { OrderPreviewResult } from '@/api/modules/order'

const customerStore = useCustomerStore()
const preview = ref<OrderPreviewResult | null>(null)
const payMethod = ref('WECHAT')
const arrivalDate = ref('')
const remark = ref('')
const submitting = ref(false)

const sysInfo = uni.getSystemInfoSync()
const safeBottom = ref(sysInfo.safeAreaInsets?.bottom ?? 0)

const payMethods = [
  { label: '微信', value: 'WECHAT' },
  { label: '账期', value: 'CREDIT' },
  { label: '余额', value: 'BALANCE' },
]

const addressId = computed(() => customerStore.addressId())

/** 加载收货地址 — 与首页 header 逻辑一致 */
async function loadAddress() {
  const addrRes = await getAddressList({ pageNum: 1, pageSize: 50 })
  if (addrRes.code === 0 && addrRes.data?.records?.length) {
    const approved = addrRes.data.records.filter(a => a.auditStatus === 'APPROVED')
    const selected = customerStore.current
    if (selected && approved.some(a => String(a.id) === String(selected.archiveId))) {
      // 当前选中地址仍在有效列表中，保持不变
    } else if (approved.length > 0) {
      // 选第一个已通过地址
      const first = approved[0]
      customerStore.saveCustomer({
        archiveId: Number(first.id),
        customerName: first.companyName,
        contactName: first.contactName,
        contactPhone: first.contactPhone,
        address: first.fullAddress || first.address,
      })
    }
  }
}

/** 跳转地址列表页选择地址 */
function handleAddressClick() {
  uni.navigateTo({ url: '/pages/company-address/list/index' })
}

onMounted(async () => {
  await loadAddress()
  if (!addressId.value) {
    uni.showToast({ title: '请先选择收货地址', icon: 'none', duration: 1500 })
    return
  }
  loadPreview()
})

onShow(async () => {
  // 从地址选择页返回时刷新地址显示
  await loadAddress()
  if (addressId.value && !preview.value) {
    loadPreview()
  }
})

watch(payMethod, () => loadPreview())

async function loadPreview() {
  if (!addressId.value) return
  try {
    const res = await previewOrder({
      addressId: addressId.value,
      payMethod: payMethod.value,
      arrivalDate: arrivalDate.value || undefined,
    })
    if (res.code === 0) preview.value = res.data
  } catch (_) {
    uni.showToast({ title: '加载失败', icon: 'none' })
  }
}

function selectDate() {
  const d = new Date()
  d.setDate(d.getDate() + 1)
  arrivalDate.value = d.toISOString().split('T')[0]
  loadPreview()
}

async function handleSubmit() {
  if (submitting.value || !addressId.value) return
  submitting.value = true
  try {
    const res = await submitOrder({
      addressId: addressId.value,
      payMethod: payMethod.value,
      arrivalDate: arrivalDate.value || undefined,
      deliveryRemark: remark.value || undefined,
      idempotentKey: `${Date.now()}`,
    })
    if (res.code === 0) {
      const orderNo = String(res.data.orderNo || '').trim()
      const paidAmount = res.data.paidAmount ?? 0
      const payParams = res.data.payParams
      let payStatus = ''

      // 有支付参数 → 调起微信支付
      if (payParams) {
        try {
          await new Promise<void>((resolve) => {
            wx.requestPayment({
              appId: payParams.appId,
              timeStamp: String(payParams.timeStamp),
              nonceStr: payParams.nonceStr,
              package: payParams.packageStr,
              signType: payParams.signType || 'RSA',
              paySign: payParams.paySign,
              success: () => {
                payStatus = 'success'
                resolve()
              },
              fail: (err: { errMsg?: string }) => {
                payStatus = err?.errMsg?.includes('cancel') ? 'cancel' : 'fail'
                resolve() // 取消或失败不 reject，继续跳结果页
              },
            })
          })
        } catch (_) {
          payStatus = 'fail'
        }
      }

      const query = `orderNo=${encodeURIComponent(orderNo)}&paidAmount=${encodeURIComponent(paidAmount)}${payStatus ? `&payStatus=${payStatus}` : ''}`
      uni.redirectTo({
        url: `/sub-pages/order/result/index?${query}`,
      })
    }
  } catch (_) {
    // error handled by request interceptor
  } finally {
    submitting.value = false
  }
}

function fmt(n: number): string {
  return Number.isInteger(n) ? String(n) : n.toFixed(2)
}
</script>

<style lang="scss" scoped>
.checkout-page { min-height: 100vh; background: #f5f5f5; padding-bottom: 100px; }
.address-card { background: #fff; margin: 12px; padding: 16px; border-radius: 12px; box-shadow: 0 1px 4px rgba(0,0,0,0.04); }
.section-card { background: #fff; margin: 0 12px 12px; padding: 16px; border-radius: 12px; box-shadow: 0 1px 4px rgba(0,0,0,0.04); }
.section-title { font-size: 14px; font-weight: 700; color: #333; margin-bottom: 12px; }
.goods-row { display: flex; align-items: center; padding: 8px 0; border-bottom: 1px solid #f5f5f5; }
.goods-row:last-child { border-bottom: none; }
.line-clamp-1 { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.pay-option { padding: 8px 16px; border-radius: 8px; border: 1px solid #e5e7eb; font-size: 13px; color: #666; transition: all 0.2s; }
.pay-option--active { border-color: #DC2626; color: #DC2626; background: #FEF2F2; }
.remark-input { width: 100%; padding: 8px 12px; border: 1px solid #e5e7eb; border-radius: 8px; font-size: 13px; }
.submit-bar { position: fixed; bottom: 0; left: 0; right: 0; background: #fff; border-top: 1px solid #f3f4f6; display: flex; align-items: center; justify-content: flex-end; padding: 10px 14px; z-index: 50; }
.submit-btn { background: linear-gradient(135deg, #DC2626, #CA8A04); border-radius: 24px; padding: 10px 32px; transition: all 0.2s; }
.submit-btn--loading { opacity: 0.6; }
</style>
