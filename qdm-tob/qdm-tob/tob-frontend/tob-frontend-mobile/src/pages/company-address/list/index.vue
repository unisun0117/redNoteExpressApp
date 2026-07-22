<template>
  <view class="page">
    <!-- 顶部导航栏 -->
    <view class="nav-bar fixed top-0 left-0 right-0 bg-white z-50 flex items-center"
      :style="{ paddingTop: statusBarH + 'px', height: statusBarH + 44 + 'px' }">
      <view class="nav-back" @click="goBack">
        <text class="text-2xl">‹</text>
      </view>
      <view class="nav-title">收货地址</view>
    </view>

    <!-- 页面内容区 -->
    <scroll-view scroll-y class="content" :style="{ height: contentH + 'px', marginTop: (statusBarH + 44) + 'px' }"
      @scrolltolower="loadMore" refresher-enabled :refresher-triggered="refreshing"
      @refresherrefresh="onRefresh">
      <!-- 空数据状态 -->
      <view v-if="!loading && list.length === 0" class="empty-state">
        <view class="empty-icon">📍</view>
        <view class="empty-title">暂无收货地址</view>
        <view class="empty-btn" @click="goAdd">新增地址</view>
      </view>

      <!-- 地址列表 -->
      <view v-else class="address-list">
        <view v-for="item in list" :key="item.id" class="address-card"
          @click="handleCardClick(item)">
          <view class="company-name">{{ item.companyName }}</view>
          <view class="contact-row">
            <text class="contact-name">{{ item.contactName }}</text>
            <text class="contact-phone">{{ item.contactPhone }}</text>
          </view>
          <view class="address-row">
            <text class="address-text">{{ item.fullAddress }}</text>
          </view>
          <view class="status-row">
            <view :class="['status-tag', statusClass(item.auditStatus)]">
              {{ statusText(item.auditStatus) }}
            </view>
          </view>
          <!-- 驳回原因 -->
          <view v-if="item.auditStatus === 'REJECTED' && item.auditRejectReason"
            class="reject-reason">
            {{ item.auditRejectReason }}
          </view>
        </view>
      </view>

      <!-- 加载更多提示 -->
      <view v-if="hasMore" class="load-more">加载中...</view>
      <view v-else-if="list.length > 0 && pageNum > 1" class="load-more">没有更多了</view>
    </scroll-view>

    <!-- 底部新增按钮 -->
    <view class="bottom-bar">
      <view class="btn-primary" @click="goAdd">新增公司地址</view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { companyAddressApi, type CompanyAddressItem } from '@/api'
import { useCustomerStore } from '@/store/modules/customer'

// ---------------------------------------------------------------------------
// 状态
// ---------------------------------------------------------------------------

const sysInfo = uni.getSystemInfoSync()
const statusBarH = ref(sysInfo.statusBarHeight ?? 20)
const contentH = ref(sysInfo.windowHeight ?? 600)

const list = ref<CompanyAddressItem[]>([])
const loading = ref(false)
const refreshing = ref(false)
const hasMore = ref(true)
const pageNum = ref(1)
const pageSize = ref(20)

// ---------------------------------------------------------------------------
// 工具方法
// ---------------------------------------------------------------------------

function maskPhone(phone: string): string {
  if (!phone || phone.length < 11) return phone
  return phone.slice(0, 3) + '****' + phone.slice(-4)
}

function statusText(status: string): string {
  const map: Record<string, string> = {
    'PENDING': '待审核',
    'APPROVED': '已通过',
    'REJECTED': '已驳回',
    'DISABLED': '已禁用',
  }
  return map[status] || status
}

function statusClass(status: string): string {
  const map: Record<string, string> = {
    'PENDING': 'tag-pending',
    'APPROVED': 'tag-pass',
    'REJECTED': 'tag-reject',
    'DISABLED': 'tag-disabled',
  }
  return map[status] || ''
}

// ---------------------------------------------------------------------------
// 数据加载
// ---------------------------------------------------------------------------

async function loadData(isRefresh = false) {
  if (loading.value) return
  if (!isRefresh && !hasMore.value) return

  loading.value = true

  try {
    const res = await companyAddressApi.getAddressList({
      pageNum: isRefresh ? 1 : pageNum.value,
      pageSize: pageSize.value,
    })

    if (res.code === 0 || res.code === 200) {
      const newList = res.data?.records || []
      if (isRefresh) {
        list.value = newList
        pageNum.value = 1
      } else {
        list.value = [...list.value, ...newList]
      }

      hasMore.value = newList.length >= pageSize.value
      if (!isRefresh && newList.length > 0) {
        pageNum.value++
      }
    }
  } catch (e) {
    console.error('加载地址列表失败', e)
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

function onRefresh() {
  loadData(true)
}

function loadMore() {
  loadData(false)
}

// ---------------------------------------------------------------------------
// 交互
// ---------------------------------------------------------------------------

function handleCardClick(item: CompanyAddressItem) {
  if (item.auditStatus === 'APPROVED') {
    // 选中地址并返回上一页
    const customerStore = useCustomerStore()
    customerStore.saveCustomer({
      archiveId: Number(item.id),
      customerName: item.companyName,
      contactName: item.contactName,
      contactPhone: item.contactPhone,
      address: item.fullAddress || item.address,
    })
    uni.navigateBack()
    return
  }
  if (item.auditStatus === 'PENDING') {
    // 待审核 → 只读详情页
    uni.navigateTo({ url: '/pages/company-address/detail/index?id=' + item.id })
    return
  }
  if (item.auditStatus === 'REJECTED') {
    // 已驳回跳编辑
    uni.navigateTo({ url: '/pages/company-address/add/index?id=' + item.id })
    return
  }
}

function goBack() {
  uni.navigateBack()
}

function goAdd() {
  uni.navigateTo({ url: '/pages/company-address/add/index' })
}

// ---------------------------------------------------------------------------
// 生命周期
// ---------------------------------------------------------------------------

onShow(() => {
  contentH.value = (sysInfo.windowHeight ?? 600) - (statusBarH.value + 44) - 80
  loadData(true)
})
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background: #F5F6F7;
  position: relative;
}

.nav-bar {
  padding-left: 8px;
}

.nav-back {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #000000;
  flex-shrink: 0;
}

.nav-title {
  flex: 1;
  text-align: center;
  font-size: 17px;
  font-weight: 600;
  color: #000000;
  margin-right: 36px;
}

.content {
  padding: 12px;
  box-sizing: border-box;
}

/* ===== 空数据状态 ===== */
.empty-state {
  padding: 120px 0 80px;
  text-align: center;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 16px;
}

.empty-title {
  font-size: 15px;
  color: #666666;
  margin-bottom: 24px;
}

.empty-btn {
  display: inline-block;
  padding: 10px 32px;
  background: linear-gradient(135deg, #FF5722 0%, #FF4D2D 100%);
  border-radius: 24px;
  color: #FFFFFF;
  font-size: 15px;
  font-weight: 500;
}

/* ===== 地址列表 ===== */
.address-list {
  padding-bottom: 16px;
}

.address-card {
  background: #FFFFFF;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 12px;
}

.company-name {
  font-size: 16px;
  font-weight: 600;
  color: #000000;
  margin-bottom: 8px;
}

.contact-row {
  font-size: 14px;
  color: #666666;
  margin-bottom: 4px;
}

.contact-name {
  margin-right: 16px;
}

.contact-phone {
  color: #666666;
}

.address-row {
  margin-bottom: 12px;
}

.address-text {
  font-size: 13px;
  color: #666666;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  line-height: 1.4;
}

.status-row {
  display: flex;
}

.status-tag {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 2px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.tag-pending {
  background: #FEF3C7;
  color: #92400E;
}

.tag-pass {
  background: #D1FAE5;
  color: #065F46;
}

.tag-reject {
  background: #FEE2E2;
  color: #DC2626;
}

.tag-disabled {
  background: #F3F4F6;
  color: #6B7280;
}

.reject-reason {
  margin-top: 12px;
  padding: 10px 12px;
  background: #FEF2F2;
  border-radius: 6px;
  font-size: 12px;
  color: #DC2626;
  line-height: 1.5;
}

/* ===== 底部按钮 ===== */
.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: #FFFFFF;
  padding: 12px 16px;
  padding-bottom: calc(12px + env(safe-area-inset-bottom));
  border-top: 1px solid #EEEEEE;
  z-index: 50;
}

.btn-primary {
  width: 100%;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #FF5722 0%, #FF4D2D 100%);
  border-radius: 24px;
  color: #FFFFFF;
  font-size: 16px;
  font-weight: 600;
}

/* ===== 加载更多 ===== */
.load-more {
  text-align: center;
  padding: 20px 0;
  font-size: 13px;
  color: #999999;
}
</style>
