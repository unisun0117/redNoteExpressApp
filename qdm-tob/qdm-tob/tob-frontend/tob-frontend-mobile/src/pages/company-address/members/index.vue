<template>
  <view class="page">
    <!-- 顶部导航栏 -->
    <view class="nav-bar fixed top-0 left-0 right-0 bg-white z-50 flex items-center"
      :style="{ paddingTop: statusBarH + 'px', height: statusBarH + 44 + 'px' }">
      <view class="nav-back" @click="goBack">
        <text class="text-2xl">‹</text>
      </view>
      <view class="nav-title">成员管理</view>
    </view>

    <!-- 页面内容区 -->
    <scroll-view scroll-y class="content" :style="{ height: contentH + 'px', marginTop: (statusBarH + 44) + 'px' }">
      <!-- 地址信息摘要 -->
      <view class="summary-card">
        <view class="summary-company">{{ companyName }}</view>
        <view class="summary-count">已绑定 {{ members.length }} 人</view>
      </view>

      <!-- 成员列表 -->
      <view class="member-list">
        <view v-for="item in members" :key="item.id" class="member-item">
          <view class="member-avatar">
            <text v-if="item.avatar" class="avatar-img">{{ item.avatar }}</text>
            <text v-else class="avatar-text">{{ (item.nickname || 'U')[0] }}</text>
          </view>
          <view class="member-info">
            <view class="member-name">
              {{ item.nickname }}
              <text v-if="item.isAdmin" class="admin-tag">管理员</text>
            </view>
            <view class="member-time">绑定时间：{{ item.bindTime }}</view>
          </view>
          <view v-if="!item.isAdmin" class="unbind-btn" @click="onUnbind(item)">
            解绑
          </view>
          <view v-else class="unbind-disabled">解绑</view>
        </view>
      </view>

      <!-- 空状态 -->
      <view v-if="members.length === 0" class="empty-tip">
        <text>暂无其他成员，点击下方按钮邀请</text>
      </view>
    </scroll-view>

    <!-- 底部邀请按钮 -->
    <view class="bottom-bar">
      <view class="btn-primary" @click="onInvite" :class="{ disabled: inviting }">
        {{ inviting ? '生成中...' : '邀请成员' }}
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { companyAddressApi, type AddressMember } from '@/api'

// ---------------------------------------------------------------------------
// 页面参数
// ---------------------------------------------------------------------------

const pages = getCurrentPages()
const currPage = pages[pages.length - 1]
const options = currPage.options as Record<string, string | undefined>
const addressId = options?.id

// ---------------------------------------------------------------------------
// 状态
// ---------------------------------------------------------------------------

const sysInfo = uni.getSystemInfoSync()
const statusBarH = ref(sysInfo.statusBarHeight ?? 20)
const contentH = ref((sysInfo.windowHeight ?? 600) - (statusBarH.value + 44) - 80)

const companyName = ref('')
const members = ref<AddressMember[]>([])
const inviting = ref(false)

// ---------------------------------------------------------------------------
// 数据加载
// ---------------------------------------------------------------------------

async function loadData() {
  if (!addressId) return

  try {
    const res = await companyAddressApi.getMemberList(addressId)
    if (res.code === 0 || res.code === 200) {
      members.value = res.data || []
    }
    // 同时获取地址详情用于显示公司名称
    try {
      const addrRes = await companyAddressApi.getAddressDetail(addressId)
      if (addrRes.code === 0 || addrRes.code === 200) {
        companyName.value = addrRes.data.companyName
      }
    } catch (e) {
      console.error('加载地址详情失败', e)
    }
  } catch (e) {
    console.error('加载成员列表失败', e)
  }
}

// ---------------------------------------------------------------------------
// 邀请成员
// ---------------------------------------------------------------------------

async function onInvite() {
  if (!addressId || inviting.value) return

  inviting.value = true

  try {
    const res = await companyAddressApi.generateInviteCode(addressId)
    if (res.code === 0 || res.code === 200 && res.data) {
      const inviteCode = res.data.inviteCode
      // 显示邀请码
      uni.showModal({
        title: '邀请码已生成',
        content: `邀请码：${inviteCode}\n（24小时内有效）`,
        confirmText: '分享',
        success: (modalRes) => {
          if (modalRes.confirm) {
            // 分享到微信
            uni.showShareMenu({
              withShareTicket: true,
              menus: ['shareAppMessage'],
            })
            // 或复制邀请码
            uni.setClipboardData({
              data: inviteCode,
              success: () => {
                uni.showToast({ title: '邀请码已复制', icon: 'success' })
              },
            })
          }
        },
      })
    }
  } catch (e) {
    console.error('生成邀请码失败', e)
  } finally {
    inviting.value = false
  }
}

// ---------------------------------------------------------------------------
// 解绑成员
// ---------------------------------------------------------------------------

function onUnbind(item: AddressMember) {
  uni.showModal({
    title: '确认解绑',
    content: `确认解绑成员「${item.nickname}」吗？解绑后该成员将无法使用此地址下单。`,
    success: async (modalRes) => {
      if (modalRes.confirm && addressId) {
        try {
          await companyAddressApi.unbindMember(addressId, item.userId)
          uni.showToast({ title: '解绑成功', icon: 'success' })
          loadData()
        } catch (e) {
          console.error('解绑失败', e)
        }
      }
    },
  })
}

// ---------------------------------------------------------------------------
// 导航
// ---------------------------------------------------------------------------

function goBack() {
  uni.navigateBack()
}

// ---------------------------------------------------------------------------
// 生命周期
// ---------------------------------------------------------------------------

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

/* ===== 摘要卡片 ===== */
.summary-card {
  background: #FFFFFF;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 12px;
}

.summary-company {
  font-size: 16px;
  font-weight: 600;
  color: #000000;
  margin-bottom: 4px;
}

.summary-count {
  font-size: 13px;
  color: #666666;
}

/* ===== 成员列表 ===== */
.member-list {
  background: #FFFFFF;
  border-radius: 12px;
  overflow: hidden;
}

.member-item {
  display: flex;
  align-items: center;
  padding: 14px 16px;
  border-bottom: 1px solid #F5F6F7;
}

.member-item:last-child {
  border-bottom: none;
}

.member-avatar {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: linear-gradient(135deg, #FF5722 0%, #FF4D2D 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 12px;
  flex-shrink: 0;
}

.avatar-text {
  font-size: 18px;
  font-weight: 600;
  color: #FFFFFF;
  text-transform: uppercase;
}

.member-info {
  flex: 1;
  min-width: 0;
}

.member-name {
  font-size: 15px;
  font-weight: 500;
  color: #000000;
  margin-bottom: 4px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.admin-tag {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 2px 8px;
  background: #FFF7ED;
  border-radius: 10px;
  font-size: 11px;
  color: #FF5722;
  font-weight: 500;
}

.member-time {
  font-size: 12px;
  color: #999999;
}

.unbind-btn {
  padding: 8px 14px;
  font-size: 14px;
  color: #DC2626;
  flex-shrink: 0;
}

.unbind-disabled {
  padding: 8px 14px;
  font-size: 14px;
  color: #CCCCCC;
  flex-shrink: 0;
}

/* ===== 空状态提示 ===== */
.empty-tip {
  text-align: center;
  padding: 40px 0;
  font-size: 13px;
  color: #999999;
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
</style>
