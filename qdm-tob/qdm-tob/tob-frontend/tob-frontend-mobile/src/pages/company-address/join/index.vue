<template>
  <view class="page">
    <!-- 顶部导航栏 -->
    <view class="nav-bar fixed top-0 left-0 right-0 bg-white z-50 flex items-center"
      :style="{ paddingTop: statusBarH + 'px', height: statusBarH + 44 + 'px' }">
      <view class="nav-back" @click="goBack">
        <text class="text-2xl">‹</text>
      </view>
      <view class="nav-title">加入地址</view>
    </view>

    <!-- 页面内容区 -->
    <view class="content" :style="{ marginTop: (statusBarH + 44) + 'px' }">
      <!-- 已过期状态 -->
      <view v-if="inviteExpired" class="expired-state">
        <view class="expired-icon">⏰</view>
        <view class="expired-title">邀请已过期</view>
        <view class="expired-desc">该邀请码已失效，请联系地址管理员重新生成</view>
      </view>

      <!-- 正常状态 -->
      <view v-else class="address-card">
        <view class="company-name">{{ addressInfo.companyName || '公司地址' }}</view>
        <view v-if="addressInfo.fullAddress" class="address-row">
          <text class="address-text">{{ addressInfo.fullAddress }}</text>
        </view>
        <view v-if="addressInfo.receiverName || addressInfo.receiverPhone" class="contact-row">
          <text v-if="addressInfo.receiverName" class="contact-name">{{ addressInfo.receiverName }}</text>
          <text v-if="addressInfo.receiverPhone" class="contact-phone">{{ addressInfo.receiverPhone }}</text>
        </view>
      </view>

      <!-- 提示文案 -->
      <view v-if="!inviteExpired" class="hint-text">
        <text>加入后，你可在下单时使用此收货地址</text>
      </view>
    </view>

    <!-- 底部按钮 -->
    <view v-if="!inviteExpired" class="bottom-bar">
      <view class="btn-primary" @click="onJoin" :class="{ disabled: joining }">
        {{ joining ? '加入中...' : '确认加入' }}
      </view>
    </view>
    <view v-else class="bottom-bar">
      <view class="btn-outline" @click="goBack">
        返回
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { companyAddressApi } from '@/api'

// ---------------------------------------------------------------------------
// 页面参数
// ---------------------------------------------------------------------------

const pages = getCurrentPages()
const currPage = pages[pages.length - 1]
const options = currPage.options as Record<string, string | undefined>
const inviteCode = options?.inviteCode

// ---------------------------------------------------------------------------
// 状态
// ---------------------------------------------------------------------------

const sysInfo = uni.getSystemInfoSync()
const statusBarH = ref(sysInfo.statusBarHeight ?? 20)

const addressInfo = ref({
  companyName: '',
  fullAddress: '',
  receiverName: '',
  receiverPhone: '',
})
const inviteExpired = ref(false)
const joining = ref(false)

// ---------------------------------------------------------------------------
// 验证邀请码并获取地址信息
// ---------------------------------------------------------------------------

async function verifyInvite() {
  if (!inviteCode) {
    inviteExpired.value = true
    return
  }

  try {
    const res = await companyAddressApi.verifyInviteCode(inviteCode)
    if (res.code === 0 || res.code === 200) {
      if (res.data?.valid && res.data.address) {
        addressInfo.value = res.data.address
        inviteExpired.value = false
      } else {
        inviteExpired.value = true
      }
    } else {
      inviteExpired.value = true
    }
  } catch (e) {
    inviteExpired.value = true
  }
}

// ---------------------------------------------------------------------------
// 确认加入
// ---------------------------------------------------------------------------

async function onJoin() {
  if (!inviteCode || joining.value) return

  joining.value = true

  try {
    const res = await companyAddressApi.joinAddress(inviteCode)
    if (res.code === 0 || res.code === 200) {
      uni.showToast({ title: '加入成功', icon: 'success', duration: 1500 })
      setTimeout(() => {
        // 返回列表或首页
        uni.reLaunch({ url: '/pages/user/index' })
      }, 1500)
    }
  } catch (e) {
    console.error('加入失败', e)
  } finally {
    joining.value = false
  }
}

// ---------------------------------------------------------------------------
// 导航
// ---------------------------------------------------------------------------

function goBack() {
  const pages = getCurrentPages()
  if (pages.length > 1) {
    uni.navigateBack()
  } else {
    uni.switchTab({ url: '/pages/user/index' })
  }
}

// ---------------------------------------------------------------------------
// 生命周期
// ---------------------------------------------------------------------------

onMounted(() => {
  verifyInvite()
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
  padding: 20px 16px;
}

/* ===== 已过期状态 ===== */
.expired-state {
  text-align: center;
  padding: 80px 0;
}

.expired-icon {
  font-size: 64px;
  margin-bottom: 16px;
}

.expired-title {
  font-size: 18px;
  font-weight: 600;
  color: #000000;
  margin-bottom: 8px;
}

.expired-desc {
  font-size: 14px;
  color: #666666;
  line-height: 1.5;
}

/* ===== 地址卡片 ===== */
.address-card {
  background: #FFFFFF;
  border-radius: 12px;
  padding: 20px 16px;
  margin-bottom: 16px;
}

.company-name {
  font-size: 17px;
  font-weight: 600;
  color: #000000;
  margin-bottom: 12px;
}

.address-row {
  margin-bottom: 8px;
}

.address-text {
  font-size: 14px;
  color: #666666;
  line-height: 1.5;
}

.contact-row {
  font-size: 14px;
  color: #666666;
}

.contact-name {
  margin-right: 16px;
}

.contact-phone {
  color: #666666;
}

/* ===== 提示文案 ===== */
.hint-text {
  text-align: center;
  padding: 12px 16px;
  font-size: 14px;
  color: #666666;
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

.btn-primary.disabled {
  background: #CCCCCC;
}

.btn-outline {
  width: 100%;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid #DDDDDD;
  border-radius: 24px;
  color: #333333;
  font-size: 16px;
  font-weight: 600;
  background: #FFFFFF;
}
</style>
