<template>
  <view class="page">
    <!-- 顶部导航栏 -->
    <view class="nav-bar fixed top-0 left-0 right-0 bg-white z-50 flex items-center"
      :style="{ paddingTop: statusBarH + 'px', height: statusBarH + 44 + 'px' }">
      <view class="nav-back" @click="goBack">
        <text class="text-2xl">‹</text>
      </view>
      <view class="nav-title">地址详情</view>
    </view>

    <!-- 页面内容区 -->
    <scroll-view scroll-y class="content" :style="{ height: contentH + 'px', marginTop: (statusBarH + 44) + 'px' }"
      v-if="detail">
      <!-- 地址卡片 -->
      <view class="address-card">
        <view class="company-name">{{ detail.companyName }}</view>
        <view class="contact-row">
          <text class="contact-name">{{ detail.contactName }}</text>
          <text class="contact-phone">{{ detail.contactPhone }}</text>
        </view>
        <view class="address-row">
          <text class="address-text">{{ detail.fullAddress }}</text>
        </view>
        <view class="status-row">
          <view :class="['status-tag', statusTagClass(detail.auditStatus)]">
            {{ statusText(detail.auditStatus) }}
          </view>
        </view>
      </view>

      <!-- 待审核提示 -->
      <view v-if="detail.auditStatus === 'PENDING'" class="pending-info">
        <text>审核中，请耐心等待。</text>
        <text v-if="detail.auditorName" class="block mt-1">审核人：{{ detail.auditorName }}</text>
        <text class="block mt-2 text-sm" style="color:#999;">审核通过后可用于下单</text>
      </view>

      <!-- 以下详情仅在非待审核状态显示 -->
      <template v-if="detail.auditStatus !== 'PENDING'">
      <!-- 收货配置 -->
      <view class="info-card">
        <view class="info-row">
          <view class="info-label">可收货时段</view>
          <view class="info-value">{{ detail.receiveTimeStart }} - {{ detail.receiveTimeEnd }}</view>
        </view>
        <view v-if="detail.receiveRequirement" class="info-row">
          <view class="info-label">收货要求</view>
          <view class="info-value">{{ detail.receiveRequirement }}</view>
        </view>
        <view v-if="detail.storagePhotos && detail.storagePhotos.length > 0" class="info-row">
          <view class="info-label">收货位置照片</view>
          <view class="photo-list">
            <view v-for="(url, idx) in detail.storagePhotos" :key="idx"
              class="photo-item" @click="previewPhoto(url)">
              <image :src="url" mode="aspectFill" class="photo-img" />
            </view>
          </view>
        </view>
      </view>

      <!-- 资质照片 -->
      <view class="info-card">
        <view class="info-row">
          <view class="info-label">营业执照编号</view>
          <view class="info-value">{{ detail.licenseNo }}</view>
        </view>
        <view class="info-row">
          <view class="info-label">门头照片</view>
          <view class="photo-list">
            <view class="photo-item" @click="previewPhoto(detail.doorPhoto)">
              <image :src="detail.doorPhoto" mode="aspectFill" class="photo-img" />
            </view>
          </view>
        </view>
        <view class="info-row">
          <view class="info-label">营业执照照片</view>
          <view class="photo-list">
            <view class="photo-item" @click="previewPhoto(detail.licensePhoto)">
              <image :src="detail.licensePhoto" mode="aspectFill" class="photo-img" />
            </view>
          </view>
        </view>
      </view>

      <!-- 业务属性（仅显示） -->
      <view v-if="hasBusinessAttrs" class="info-card">
        <view v-if="detail.priceGroup" class="info-row">
          <view class="info-label">价格组</view>
          <view class="info-value">{{ detail.priceGroup }}</view>
        </view>
        <view v-if="detail.settleCompany" class="info-row">
          <view class="info-label">结算公司</view>
          <view class="info-value">{{ detail.settleCompany }}</view>
        </view>
        <view v-if="detail.businessType" class="info-row">
          <view class="info-label">经营类型</view>
          <view class="info-value">{{ detail.businessType }}</view>
        </view>
        <view v-if="detail.settleType" class="info-row">
          <view class="info-label">结算类型</view>
          <view class="info-value">{{ detail.settleType }}</view>
        </view>
        <view v-if="detail.sapCustomerCode" class="info-row">
          <view class="info-label">SAP客户编码</view>
          <view class="info-value">{{ detail.sapCustomerCode }}</view>
        </view>
      </view>

      <!-- 审核信息 -->
      <view v-if="detail.auditorName" class="info-card">
        <view class="info-row">
          <view class="info-label">审核人</view>
          <view class="info-value">{{ detail.auditorName }}</view>
        </view>
      </view>
      </template>
    </scroll-view>

    <!-- 底部按钮 -->
    <view v-if="detail && detail.auditStatus === 'APPROVED' && isAddressAdmin" class="bottom-bar">
      <view class="btn-outline" @click="goMembers">
        管理成员
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { companyAddressApi, type CompanyAddressDetail } from '@/api'

// ---------------------------------------------------------------------------
// 状态
// ---------------------------------------------------------------------------

let addressId = ''

const sysInfo = uni.getSystemInfoSync()
const statusBarH = ref(sysInfo.statusBarHeight ?? 20)
const contentH = ref((sysInfo.windowHeight ?? 600) - (statusBarH.value + 44) - 80)

const detail = ref<CompanyAddressDetail | null>(null)
const loading = ref(false)
const isAddressAdmin = ref(false)

// ---------------------------------------------------------------------------
// 数据加载
// ---------------------------------------------------------------------------

async function loadData() {
  if (!addressId) return
  loading.value = true

  try {
    const res = await companyAddressApi.getAddressDetail(addressId)
    if ((res.code === 0 || res.code === 200) && res.data) {
      detail.value = res.data as CompanyAddressDetail
      isAddressAdmin.value = (res.data as any).isAdmin ?? false
    }
  } catch (e) {
    console.error('加载地址详情失败', e)
  } finally {
    loading.value = false
  }
}

// ---------------------------------------------------------------------------
// 计算属性
// ---------------------------------------------------------------------------

const hasBusinessAttrs = computed(() => {
  if (!detail.value) return false
  return (
    detail.value.priceGroup ||
    detail.value.settleCompany ||
    detail.value.businessType ||
    detail.value.settleType ||
    detail.value.sapCustomerCode
  )
})

// ---------------------------------------------------------------------------
// 图片预览
// ---------------------------------------------------------------------------

function previewPhoto(url: string) {
  uni.previewImage({ urls: [url], current: 0 })
}

// ---------------------------------------------------------------------------
// 导航
// ---------------------------------------------------------------------------

function goBack() {
  uni.navigateBack()
}

function statusText(status: string): string {
  const map: Record<string, string> = {
    'PENDING': '待审核','APPROVED': '已通过','REJECTED': '已驳回','DISABLED': '已禁用',
  }
  return map[status] || status
}

function statusTagClass(status: string): string {
  const map: Record<string, string> = {
    'PENDING': 'tag-pending','APPROVED': 'tag-pass','REJECTED': 'tag-reject','DISABLED': 'tag-disabled',
  }
  return map[status] || ''
}

function goMembers() {
  if (!addressId) return
  uni.navigateTo({ url: '/pages/company-address/members/index?id=' + addressId })
}

// ---------------------------------------------------------------------------
// 生命周期
// ---------------------------------------------------------------------------

onLoad((options: any) => {
  addressId = options?.id || ''
})

onMounted(() => {
  loadData()
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
  padding: 12px 16px 16px;
  box-sizing: border-box;
}

/* ===== 地址卡片 ===== */
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
  line-height: 1.5;
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

.tag-pending { background: #FEF3C7; color: #92400E; }
.tag-pass { background: #D1FAE5; color: #065F46; }
.tag-reject { background: #FEE2E2; color: #DC2626; }
.tag-disabled { background: #F3F4F6; color: #6B7280; }

.pending-info {
  text-align: center;
  padding: 24px 16px;
  font-size: 14px;
  color: #666;
}

.tag-pass {
  background: #D1FAE5;
  color: #065F46;
}

/* ===== 信息卡片 ===== */
.info-card {
  background: #FFFFFF;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 12px;
}

.info-row {
  display: flex;
  padding: 10px 0;
  border-bottom: 1px solid #F5F6F7;
}

.info-row:last-child {
  border-bottom: none;
}

.info-label {
  width: 110px;
  flex-shrink: 0;
  font-size: 14px;
  color: #666666;
}

.info-value {
  flex: 1;
  font-size: 14px;
  color: #000000;
  word-break: break-all;
}

/* ===== 照片列表 ===== */
.photo-list {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.photo-item {
  width: 88px;
  height: 88px;
  border-radius: 8px;
  overflow: hidden;
}

.photo-img {
  width: 100%;
  height: 100%;
  display: block;
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

.btn-outline {
  width: 100%;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid #FF5722;
  border-radius: 24px;
  color: #FF5722;
  font-size: 16px;
  font-weight: 600;
  background: #FFFFFF;
}
</style>
