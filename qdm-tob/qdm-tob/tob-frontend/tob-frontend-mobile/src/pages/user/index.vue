<template>
  <view class="page">
    <view
      class="mp-nav"
      :style="{
        opacity: navOpacity,
      }"
    >
      <up-navbar title="我的" :bgColor="'#FFFFFF'" leftIcon=""></up-navbar>
    </view>
    <view class="header" :style="{ paddingTop: statusBarH + 48 + 'px' }">
      <view class="flex items-center justify-between px-4">
        <!-- 左侧：头像 + 信息 -->
        <view class="flex items-center gap-3">
          <view class="avatar mt-3">
            <text class="text-lg leading-none">🏫</text>
          </view>
          <view class="nolist">
            <view class="text-xl font-bold text-gray-900">三苗文体食堂</view>
            <view class="tag-outline mt-1">
              <text class="fs-[24rpx] text-gray-400">食堂</text>
            </view>
          </view>
        </view>
        <!-- 右侧：设置 + 消息 -->
        <view class="flex items-center gap-4">
          <text class="text-base text-gray-900" @click="goSettings">设置</text>
          <text class="text-base text-gray-900" @click="goMessages">消息</text>
        </view>
      </view>
    </view>

    <!-- 卡片一：公司收货地址 -->
    <view class="card card-row" @click="goCompanyAddress">
      <u-icon name="map" size="18" color="#333333"></u-icon>
      <view class="address-info">
        <text class="address-label">公司收货地址</text>
        <text class="address-desc">管理用于下单的公司收货地址</text>
      </view>
      <text class="address-change">更改 ›</text>
    </view>

    <!--  卡片二：我的钱包 -->
    <view class="card !pb-5">
      <view class="card-header">
        <text class="card-title text-lg">我的钱包</text>
        <text class="card-link" @click="goWallet">进入钱包 ›</text>
      </view>
      <view class="wallet-grid">
        <view class="wallet-item" @click="goBalance">
          <text class="wallet-num fs-40rpx">0.00<text class="fw-[400] fs-24rpx ml-1">元</text></text>
          <text class="wallet-label fs-26rpx">余额</text>
        </view>
        <view class="wallet-item" @click="goOweDetail">
          <text class="wallet-num fs-40rpx">0.00<text class="fw-[400] fs-24rpx ml-1">元</text></text>
          <text class="wallet-label fs-26rpx">需补金额</text>
        </view>
        <view class="wallet-item" @click="goCoupon">
          <text class="wallet-num fs-40rpx">0<text class="fw-[400] fs-24rpx ml-1">张</text></text>
          <text class="wallet-label fs-26rpx">优惠券</text>
        </view>
      </view>
    </view>

    <!-- 订单状态 常用服务 帮助中心 -->
    <view class="card" v-for="(item,index) in quickItems" :key="index">
      <view class="card-header">
        <text class="card-title block text-lg">{{item.label}}</text>
        <text v-if="item.tapLabe" class="card-link" @click="goWallet">{{item.tapLabe}} ›</text>
      </view>
      <view class="list-grid" v-if="item.list">
        <view v-for="item2 in item.list" :key="item2.label" class="list-item" @click="$tools.router.push(`${item2.page}`)">
          <up-image :src="item2.image" v-if="item2.image" width="62rpx" height="62rpx"></up-image>
          <u-icon :name="item2.icon" size="30" color="#000000" v-else></u-icon>
          <text class="fs-26rpx">{{ item2.label }}</text>
        </view>
      </view>
    </view>

    <view class="h-5"></view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { onPageScroll } from '@dcloudio/uni-app'
const sysInfo = uni.getSystemInfoSync()
const statusBarH = ref(sysInfo.statusBarHeight ?? 20)

/** 滚动时导航栏透明度：0 隐蔽，1 显示 */
const scrollThreshold = 100
const navOpacity = computed(() => Math.min(scrollTop.value / scrollThreshold, 1))
const scrollTop = ref(0)
onPageScroll((e: { scrollTop: number }) => {
  scrollTop.value = e.scrollTop
})


export interface quickItem {
  // 名称
  label: string
  // 图片地址或者名称
  icon?: string,
  image?: string,
  // 跳转页面地址
  page?: string
  // 副名称
  tapLabe?: string,
  // 子内容
  list?: quickItem[]
}

// ---------------------------------------------------------------------------
// 订单状态 常用服务 帮助中心
// ---------------------------------------------------------------------------
const quickItems:quickItem[] = [
  {
    label: '我的订单', tapLabe: '全部订单',list: [
      { label: '待支付', image: '/static/user/zf.png' },
      { label: '待出库', image: '/static/user/ck.png' },
      { label: '出库中', image: '/static/user/fc.png' },
      { label: '已完成', image: '/static/user/wc.png' },
      { label: '退款/售后', image: '/static/user/th.png' },
    ]
  },
  {
    label: '常用服务', list: [
      { label: '开发票', image: '/static/user/fp.png' },
      { label: '在线客服', image: '/static/user/zx.png' },
      { label: '销售经理', image: '/static/user/jl.png' },
      { label: '服务热线', image: '/static/user/rx.png' },
      { label: '售后说明', image: '/static/user/sm.png' },
      { label: '隐私政策', image: '/static/user/ys.png', page: '/sub-pages/setting/privacy/index'},
    ]
  },
  // {
  //   label: '帮助中心', list: [
  //     { label: '联系客服', icon: '🎧' },
  //     { label: '销售助理', icon: '💬' },
  //     { label: '销售经理', icon: '👤' },
  //     { label: '常见问题', icon: '❓' },
  //     { label: '售后规则', icon: '📋' },
  //     { label: '隐私政策', icon: '📋', page: '/sub-pages/setting/pages/privacy/index'},
  //   ]
  // },
  
]

const toast = (name: string) => uni.showToast({ title: name, icon: 'none', duration: 1500 })

function goSettings() {
  uni.navigateTo({ url: '/sub-pages/setting/pages/other-settings/index' })
}
function goMessages() { toast('消息中心') }
function goCompanyAddress() {
  uni.navigateTo({ url: '/pages/company-address/list/index' })
}
function goAddress() { toast('配送地址') }
function goWallet() { uni.navigateTo({ url: '/sub-pages/pay/pages/index/index' }) }
function goCoupon() { toast('优惠券') }
function goBalance() { toast('余额') }
function goOweDetail() { toast('需补金额') }
function goOrder(label: string) { toast(label) }
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background: #F5F6F7;
}

/* 小程序吸顶导航栏 */
.mp-nav {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
  background: #FFFFFF;
  display: flex;
  align-items: center;
  justify-content: center;
  padding-bottom: 12px;
}

.mp-nav-title {
  font-size: 17px;
  font-weight: 700;
  color: #000000;
}

/* ===== 头部 ===== */
.header {
  background: linear-gradient(180deg, #FFFFFF 0%, #F5F6F7 100%);
  padding-bottom: 16px;
}

.avatar {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background: #FFFFFF;
  display: flex;
  align-items: center;
  justify-content: center;
}

.tag-outline {
  display: inline-flex;
  border: 1px solid #e4e4e4;
  border-radius: 4px;
  padding: 1px 6px;
}

/* ===== 通用卡片 ===== */
.card {
  background: #FFFFFF;
  border-radius: 8px;
  margin: 10px 12px 0 12px;
  padding: 14px;
}

/* ===== 公司地址卡片 ===== */
.card-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.address-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 0;
}

.address-label {
  font-size: 15px;
  font-weight: 500;
  color: #000000;
}

.address-desc {
  font-size: 12px;
  color: #999999;
}

.address-change {
  font-size: 13px;
  color: #888888;
  flex-shrink: 0;
}

/* ===== 卡片标题行 ===== */
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 14px;
}

.card-title {
  font-weight: 700;
  color: #000000;
}

.card-link {
  font-size: 13px;
  color: #999999;
}

/* ===== 钱包三栏 ===== */
.wallet-grid {
  display: flex;
}

.wallet-item {
  flex: 1;
  text-align: center;
}

.wallet-num {
  font-weight: 700;
  color: #000000;
  line-height: 1.2;
}

.wallet-label {
  display: block;
  color: #666666;
  margin-top: 8px;
}

/* ===== 订单五栏 ===== */
.list-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
}

.list-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  padding: 6px 0;
}
</style>
