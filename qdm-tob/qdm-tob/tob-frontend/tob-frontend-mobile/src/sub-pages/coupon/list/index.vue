<template>
  <view style="min-height: 100vh; background: linear-gradient(180deg, #FEF2F2 0%, #FFFFFF 40%);">
    <!-- ============================================================
         Tab 页签
         ============================================================ -->
    <view style="display: flex; background: #FFFFFF; border-bottom: 1px solid #F3F4F6;">
      <view
        v-for="tab in tabs"
        :key="tab.key"
        style="flex: 1; display: flex; align-items: center; justify-content: center; padding: 12px 0; position: relative;"
        @click="activeTab = tab.key"
      >
        <text
          style="font-size: 16px; transition: color 0.2s;"
          :style="{
            color: activeTab === tab.key ? '#DC2626' : '#9CA3AF',
            fontWeight: activeTab === tab.key ? '700' : '400',
          }"
        >
          {{ tab.label }}
        </text>
        <view
          v-if="activeTab === tab.key"
          style="position: absolute; bottom: 0; left: 50%; transform: translateX(-50%); width: 24px; height: 3px; border-radius: 999px; background: #DC2626;"
        />
      </view>
    </view>

    <!-- ============================================================
         未使用页签
         ============================================================ -->
    <view v-if="activeTab === 'unused'" style="padding: 12px 12px 24px; display: flex; flex-direction: column; gap: 12px;">
      <!-- 空状态 -->
      <view v-if="activeCoupons.length === 0 && expiredCoupons.length === 0" style="display: flex; flex-direction: column; align-items: center; justify-content: center; padding-top: 30%;">
        <view style="width: 96px; height: 96px; border-radius: 50%; display: flex; align-items: center; justify-content: center; margin-bottom: 20px; background: #FEF2F2;">
          <text style="font-size: 40px;">🎫</text>
        </view>
        <text style="font-size: 18px; font-weight: 600; margin-bottom: 8px; color: #450A0A;">暂无未使用的优惠券</text>
        <text style="font-size: 14px; text-align: center; color: #9A3412;">去下单页面看看吧</text>
      </view>

      <template v-else>
        <!-- 有效券 -->
        <view
          v-for="item in activeCoupons"
          :key="item.distributionId"
          style="border-radius: 12px; padding: 16px; display: flex; flex-direction: column; background: #FFFFFF; box-shadow: 0 2px 16px rgba(220,38,38,0.08);"
          :style="item.status === 'LOCKED' ? { border: '1px solid #FBBF24' } : {}"
        >
          <view v-if="item.status === 'LOCKED'" style="align-self: flex-start; border-radius: 999px; padding: 2px 10px; margin-bottom: 8px; background: #FEF3C7;">
            <text style="font-size: 12px; font-weight: 500; color: #D97706;">处理中</text>
          </view>
          <view style="display: flex; align-items: flex-start; justify-content: space-between;">
            <view style="display: flex; align-items: center; flex: 1; min-width: 0; gap: 8px;">
              <text style="font-size: 18px; flex-shrink: 0;">🏷</text>
              <text style="font-size: 16px; font-weight: 600; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; color: #450A0A;">{{ item.couponName }}</text>
            </view>
          </view>
          <text style="font-size: 20px; font-weight: 700; margin-top: 8px; color: #DC2626;">{{ formatRule(item.thresholdAmount, item.discountAmount) }}</text>
          <view style="display: flex; flex-wrap: wrap; margin-top: 12px; gap: 8px;">
            <view v-for="cat in item.categories" :key="cat" style="border-radius: 999px; padding: 2px 10px; background: #F3F4F6;">
              <text style="font-size: 12px; color: #6B7280;">{{ cat }}</text>
            </view>
          </view>
          <text style="font-size: 14px; margin-top: 12px; color: #9CA3AF;">📅 {{ item.startDate }} ~ {{ item.endDate }}</text>
        </view>

        <!-- 已过期分隔 -->
        <view v-if="expiredCoupons.length > 0 && activeCoupons.length > 0" style="display: flex; align-items: center; gap: 8px;">
          <view style="flex: 1; height: 1px; background: #F3F4F6;" />
          <text style="font-size: 12px; flex-shrink: 0; color: #D1D5DB;">以下已过期</text>
          <view style="flex: 1; height: 1px; background: #F3F4F6;" />
        </view>

        <!-- 已过期券 -->
        <view
          v-for="item in expiredCoupons"
          :key="item.distributionId"
          style="border-radius: 12px; padding: 16px; display: flex; flex-direction: column; background: #FFFFFF; box-shadow: 0 2px 16px rgba(220,38,38,0.08); opacity: 0.45;"
        >
          <view style="display: flex; align-items: flex-start; justify-content: space-between;">
            <view style="display: flex; align-items: center; flex: 1; min-width: 0; gap: 8px;">
              <text style="font-size: 18px; flex-shrink: 0;">🏷</text>
              <text style="font-size: 16px; font-weight: 600; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; color: #450A0A;">{{ item.couponName }}</text>
            </view>
            <view style="border-radius: 999px; padding: 2px 10px; flex-shrink: 0; margin-left: 8px; background: #FEF2F2;">
              <text style="font-size: 12px; font-weight: 500; color: #DC2626;">已过期</text>
            </view>
          </view>
          <text style="font-size: 20px; font-weight: 700; margin-top: 8px; color: #DC2626;">{{ formatRule(item.thresholdAmount, item.discountAmount) }}</text>
          <view style="display: flex; flex-wrap: wrap; margin-top: 12px; gap: 8px;">
            <view v-for="cat in item.categories" :key="cat" style="border-radius: 999px; padding: 2px 10px; background: #F3F4F6;">
              <text style="font-size: 12px; color: #6B7280;">{{ cat }}</text>
            </view>
          </view>
          <text style="font-size: 14px; margin-top: 12px; color: #9CA3AF;">📅 {{ item.startDate }} ~ {{ item.endDate }}</text>
        </view>
      </template>
    </view>

    <!-- ============================================================
         已使用页签
         ============================================================ -->
    <view v-if="activeTab === 'used'" style="padding: 12px 12px 24px; display: flex; flex-direction: column; gap: 12px;">
      <view v-if="usedCoupons.length === 0" style="display: flex; flex-direction: column; align-items: center; justify-content: center; padding-top: 30%;">
        <view style="width: 96px; height: 96px; border-radius: 50%; display: flex; align-items: center; justify-content: center; margin-bottom: 20px; background: #FEF2F2;">
          <text style="font-size: 40px;">🎫</text>
        </view>
        <text style="font-size: 18px; font-weight: 600; margin-bottom: 8px; color: #450A0A;">暂无已使用的优惠券</text>
        <text style="font-size: 14px; text-align: center; color: #9A3412;">使用优惠券下单后在此查看</text>
      </view>

      <view
        v-for="item in usedCoupons"
        :key="item.distributionId"
        style="border-radius: 12px; padding: 16px; display: flex; flex-direction: column; background: #FFFFFF; box-shadow: 0 2px 16px rgba(220,38,38,0.08); opacity: 0.45;"
      >
        <view style="display: flex; align-items: center; gap: 8px;">
          <text style="font-size: 18px; flex-shrink: 0;">🏷</text>
          <text style="font-size: 16px; font-weight: 600; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; color: #450A0A;">{{ item.couponName }}</text>
        </view>
        <text style="font-size: 20px; font-weight: 700; margin-top: 8px; color: #DC2626;">{{ formatRule(item.thresholdAmount, item.discountAmount) }}</text>
        <view style="display: flex; flex-wrap: wrap; margin-top: 12px; gap: 8px;">
          <view v-for="cat in item.categories" :key="cat" style="border-radius: 999px; padding: 2px 10px; background: #F3F4F6;">
            <text style="font-size: 12px; color: #6B7280;">{{ cat }}</text>
          </view>
        </view>
        <text style="font-size: 14px; margin-top: 12px; color: #9CA3AF;">📅 {{ item.startDate }} ~ {{ item.endDate }}</text>
        <view style="margin-top: 12px; padding-top: 12px; border-top: 1px dashed #E5E7EB;">
          <text style="font-size: 14px; display: block; color: #9CA3AF;">✅ 使用时间：{{ item.useTime }}</text>
          <text style="font-size: 14px; display: block; margin-top: 4px; color: #9CA3AF;">📋 订单号：{{ item.orderNo }}</text>
          <text style="font-size: 14px; display: block; margin-top: 4px; color: #9CA3AF;">💰 订单金额：{{ formatOrderAmount(item.orderAmount) }}</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'

type CouponStatus = 'UNUSED' | 'LOCKED' | 'USED' | 'EXPIRED'

interface CouponItem {
  distributionId: string
  couponId: string
  couponName: string
  thresholdAmount: number
  discountAmount: number
  categories: string[]
  startDate: string
  endDate: string
  receiveTime: string
  status: CouponStatus
  useTime: string | null
  orderId: string | null
  orderNo: string | null
  orderAmount: number | null
}

// ------------------------------------------------------------------
// Tab 配置
// ------------------------------------------------------------------

const tabs = [
  { key: 'unused' as const, label: '未使用' },
  { key: 'used' as const, label: '已使用' },
]

// ------------------------------------------------------------------
// Mock 数据（8 张券，覆盖全部 4 种状态）
// ------------------------------------------------------------------

const mockCoupons: CouponItem[] = [
  {
    distributionId: 'd1', couponId: 'c1', couponName: '新春促销海鲜券',
    thresholdAmount: 100, discountAmount: 20,
    categories: ['海鲜类', '冻品类'],
    startDate: '2026-07-01', endDate: '2026-07-31',
    receiveTime: '2026-07-01 10:30:00', status: 'UNUSED',
    useTime: null, orderId: null, orderNo: null, orderAmount: null,
  },
  {
    distributionId: 'd2', couponId: 'c2', couponName: '蔬菜季优惠券',
    thresholdAmount: 50, discountAmount: 10,
    categories: ['蔬菜类', '水果类'],
    startDate: '2026-07-05', endDate: '2026-08-05',
    receiveTime: '2026-07-05 14:00:00', status: 'UNUSED',
    useTime: null, orderId: null, orderNo: null, orderAmount: null,
  },
  {
    distributionId: 'd3', couponId: 'c3', couponName: '粮油特惠券',
    thresholdAmount: 200, discountAmount: 30,
    categories: ['粮油类'],
    startDate: '2026-07-01', endDate: '2026-07-10',
    receiveTime: '2026-07-03 09:15:00', status: 'UNUSED',
    useTime: null, orderId: null, orderNo: null, orderAmount: null,
  },
  {
    distributionId: 'd4', couponId: 'c4', couponName: '猪肉专享券',
    thresholdAmount: 80, discountAmount: 15,
    categories: ['猪肉类'],
    startDate: '2026-07-01', endDate: '2026-07-31',
    receiveTime: '2026-07-02 11:00:00', status: 'LOCKED',
    useTime: null, orderId: null, orderNo: null, orderAmount: null,
  },
  {
    distributionId: 'd5', couponId: 'c5', couponName: '端午粽子券',
    thresholdAmount: 150, discountAmount: 25,
    categories: ['冻品类'],
    startDate: '2026-06-01', endDate: '2026-06-30',
    receiveTime: '2026-06-01 08:00:00', status: 'EXPIRED',
    useTime: null, orderId: null, orderNo: null, orderAmount: null,
  },
  {
    distributionId: 'd6', couponId: 'c6', couponName: '春季蔬菜券',
    thresholdAmount: 30, discountAmount: 5,
    categories: ['蔬菜类'],
    startDate: '2026-05-01', endDate: '2026-06-25',
    receiveTime: '2026-05-10 16:30:00', status: 'EXPIRED',
    useTime: null, orderId: null, orderNo: null, orderAmount: null,
  },
  {
    distributionId: 'd7', couponId: 'c7', couponName: '618海鲜大促券',
    thresholdAmount: 200, discountAmount: 50,
    categories: ['海鲜类'],
    startDate: '2026-06-01', endDate: '2026-06-18',
    receiveTime: '2026-06-01 00:00:00', status: 'USED',
    useTime: '2026-06-15 09:30:00', orderId: 'o1', orderNo: 'OD20260615001', orderAmount: 356.00,
  },
  {
    distributionId: 'd8', couponId: 'c8', couponName: '新客专享券',
    thresholdAmount: 20, discountAmount: 8,
    categories: ['蔬菜类', '水果类', '粮油类'],
    startDate: '2026-06-01', endDate: '2026-07-01',
    receiveTime: '2026-06-10 12:00:00', status: 'USED',
    useTime: '2026-06-20 18:45:00', orderId: 'o2', orderNo: 'OD20260620003', orderAmount: 89.50,
  },
]

// ------------------------------------------------------------------
// 响应式
// ------------------------------------------------------------------

const activeTab = ref<'unused' | 'used'>('unused')

const activeCoupons = computed(() =>
  mockCoupons
    .filter(c => c.status === 'UNUSED' || c.status === 'LOCKED')
    .sort((a, b) => a.endDate.localeCompare(b.endDate) || b.receiveTime.localeCompare(a.receiveTime)),
)

const expiredCoupons = computed(() =>
  mockCoupons
    .filter(c => c.status === 'EXPIRED')
    .sort((a, b) => b.endDate.localeCompare(a.endDate) || b.receiveTime.localeCompare(a.receiveTime)),
)

const usedCoupons = computed(() =>
  mockCoupons
    .filter(c => c.status === 'USED')
    .sort((a, b) => (b.useTime || '').localeCompare(a.useTime || '')),
)

// ------------------------------------------------------------------
// 格式化
// ------------------------------------------------------------------

function formatRule(threshold: number, discount: number): string {
  const t = Number.isInteger(threshold) ? threshold : threshold.toFixed(2)
  const d = Number.isInteger(discount) ? discount : discount.toFixed(2)
  return `满 ${t} 减 ${d}`
}

function formatOrderAmount(amount: number | null): string {
  if (amount === null || amount === undefined) return '-'
  const parts = amount.toFixed(2).split('.')
  parts[0] = parts[0].replace(/\B(?=(\d{3})+(?!\d))/g, ',')
  return '¥' + parts.join('.')
}
</script>

<style lang="scss" scoped>
/* 全部样式通过内联实现 */
</style>
