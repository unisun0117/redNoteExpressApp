<template>
  <view class="category-page">
    <view class="header-bar fixed top-0 left-0 right-0 z-50" :style="{ paddingTop: statusBarH + 'px' }">
      <!-- 地址 + 搜索 -->
        <view class="flex items-center justify-between px-3 pt-2 pb-2.5 gap-2.5">
          <view class="flex items-center shrink-0" @click="handleLocation">
            <u-icon name="map" size="16" color="#6B7280"></u-icon>
            <text class="text-lg font-bold text-gray-900 truncate max-w-[140px] px-1">{{ addressText }}</text>
            <u-icon name="arrow-down" size="12" color="#6B7280"></u-icon>
          </view>
          <view class="flex-1 h-7 max-w-[120px] bg-red-50 justify-between rounded-2xl flex items-center px-3 mr-16" @click="goSearch">
            <text class="fs-24rpx text-primary font-medium truncate">⏰ 22:30 截单</text>
          </view>
        </view>
      <!-- 截单提示 -->
      <view class="flex items-center mx-3 mb-2 bg-[#fff] rounded-3xl px-1 py-1 pl-2.5">
        <u-icon name="search" size="24" color="#9CA3AF"></u-icon>
        <view class="flex flex-1 items-center justify-between">
          <text class="text-base text-gray-400 ml-1.5">搜索商品</text>
          <view class="fs-28rpx w-14 h-7 text-white p-btn text-w rounded-full flex items-center justify-center shrink-0">查询</view>
        </view>
      </view>
    </view>

    <view class="px-3 bg-[#fff] banner-card-wap py-1 pt-3 overflow-hidden" :style="{ marginTop: fixedTotalH + 'px' }">
      <u-swiper
              :list="[  
                  '/static/123.png',
                  '/static/123.png',
              ]"
              :height="140"
              indicator
              indicatorMode="line"
              circular
      ></u-swiper>
    </view>
    <!-- 公告栏 -->
    <view class="bg-white px-3 py-2 pb-3">
      <view class="bg-red-50 rounded-lg py-2 px-3 pr-2 flex items-center gap-2" @click="goAnnouncement">
        <u-icon class="text-base shrink-0" name="volume-fill" color="#dc2626"></u-icon>
        <text class="flex-1 text-primary text-lg text-sm font-bold truncate">钱鲜达不卖隔夜肉</text>
        <u-icon class="" size="14" color="#CCC" name="arrow-right"></u-icon>
      </view>
    </view>

    <u-sticky offset-top="20">
      <view class="tab-sticky" >
        <scroll-view scroll-x enable-flex :show-scrollbar="false" class="tab-scroll">
          <view
            v-for="cat in categories"
            :key="cat.id"
            class="tab-item"
            :class="{ 'tab-active': activeTab === cat.id }"
            @click="switchTab(cat.id)"
          >
            <view class="tab-icon-box" :class="{ 'tab-icon-active': activeTab === cat.id }">
              <text class="text-xl leading-none">{{ cat.icon }}</text>
            </view>
            <text class="tab-name fs-26rpx py-1" :class="{ 'tab-name-active': activeTab === cat.id }">{{ cat.name }}</text>
          </view>
        </scroll-view>
      </view>
    </u-sticky>

    <scroll-view
      scroll-y
      :show-scrollbar="false"
      class="main-scroll"
      :style="{ paddingBottom: tabBarH + 'px' }"
    >
      <!-- 商品标题栏 -->
      <view class="flex items-center justify-between px-3.5 pt-3 pb-2.5">
        <view class="flex items-center gap-1.5">
          <view class="w-1 h-3.5 bg-primary rounded-sm"></view>
          <text class="text-[15px] font-bold text-primary">{{ currentTitle }}</text>
        </view>
        <text class="fs-24rpx text-gray-400" @click="goMore">查看更多 &gt;</text>
      </view>

      <!-- 商品列表 -->
      <view class="mx-3 px-2 pt-3 rounded-lg bg-[#fff]">
        <view v-if="loading" class="text-center py-10">
          <u-loading-icon mode="circle" size="20" color="#DC2626"></u-loading-icon>
          <text class="text-sm text-gray-400 ml-2">加载中...</text>
        </view>
        <view v-else-if="currentGoods.length === 0" class="text-center py-10">
          <text class="text-sm text-gray-400">暂无商品</text>
        </view>
        <view v-else>
          <GoodsCard
            v-for="(item, idx) in currentGoods"
            :key="idx"
            :item="item"
            @add-to-cart="addToCart"
            @choose-spec="chooseSpec"
          />
        </view>
      </view>

      <view class="h-4" />
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import GoodsCard from '@/components/GoodsCard.vue'
import { getProductList, getCategories, type CategoryVO } from '@/api/modules/product'
import { getAddressList } from '@/api/modules/companyAddress'
import { useCartStore } from '@/store/modules/cart'
import { useCustomerStore } from '@/store/modules/customer'
import type { GoodsItem } from '@/types/goods'
import { enrichGoodsList } from '@/utils/enrichGoods'

const cartStore = useCartStore()
const customerStore = useCustomerStore()

// ---------------------------------------------------------------------------
// 系统
// ---------------------------------------------------------------------------
const sysInfo = uni.getSystemInfoSync()
const statusBarH = ref(sysInfo.statusBarHeight ?? 20)
const tabBarH = ref(52)
const addressText = ref('请选择收货地址')

/** 固定区总高：渐变区(~200) + notice(42) + tabs(58) */
const fixedTotalH = computed(() => {
  return statusBarH.value + 80;
});

function goAnnouncement() {
  uni.navigateTo({ url: "/sub-pages/announcement/pages/list/index" });
}

// ---------------------------------------------------------------------------
// 商品数据（从 API 获取）
// ---------------------------------------------------------------------------
const products = ref<GoodsItem[]>([])
const loading = ref(false)

// 分类从 API 加载
const categories = ref<{ id: string; name: string; icon: string }[]>([
  { id: 'all', name: '全部', icon: '✨' },
])

// ---------------------------------------------------------------------------
// 状态
// ---------------------------------------------------------------------------
const activeTab = ref('all')

const currentTitle = computed(() =>
  categories.value.find((c) => c.id === activeTab.value)?.name ?? '全部商品'
)

const currentGoods = computed((): GoodsItem[] => products.value)

// ---------------------------------------------------------------------------
// 生命周期
// ---------------------------------------------------------------------------
onMounted(async () => {
  loading.value = true
  try {
    await loadAddress()
    // 加载分类
    const catRes = await getCategories()
    if (catRes.code === 0 && catRes.data) {
      categories.value = [
        { id: 'all', name: '全部', icon: '✨' },
        ...catRes.data.map((c: CategoryVO) => ({
          id: c.id,
          name: c.alias || c.name,
          icon: '📦',
        })),
      ]
    }
    // 同步购物车 + 商品列表
    await loadProductsAndCart()
  } catch (_) {
    // 错误由拦截器统一处理
  } finally {
    loading.value = false
  }
})

onShow(async () => {
  // 从地址选择页返回时刷新显示
  const prevAddrId = customerStore.addressId()
  await loadAddress()
  if (customerStore.addressId() !== prevAddrId) {
    await loadProductsAndCart()
  }
})

// ---------------------------------------------------------------------------
// 数据加载
// ---------------------------------------------------------------------------

async function loadAddress() {
  const addrRes = await getAddressList({ pageNum: 1, pageSize: 50 })
  if (addrRes.code === 0 && addrRes.data?.records?.length) {
    const approved = addrRes.data.records.filter(a => a.auditStatus === 'APPROVED')
    const selected = customerStore.current
    if (selected && approved.some(a => String(a.id) === String(selected.archiveId))) {
      // 当前选中地址仍在有效列表中
      addressText.value = selected.customerName || selected.address
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
      addressText.value = first.companyName
    }
  }
}

async function loadProductsAndCart() {
  const addrId = customerStore.addressId() || 1
  cartStore.fetchCart(addrId)
  const res = await getProductList(addrId)
  if (res.code === 0 && res.data) {
    products.value = enrichGoodsList(res.data.records ?? [])
  }
}

// ---------------------------------------------------------------------------
// 交互
// ---------------------------------------------------------------------------
function switchTab(id: string) {
  activeTab.value = id
}

async function addToCart(item: GoodsItem) {
  if (!item.productBarcode) {
    uni.showToast({ title: '商品信息异常', icon: 'none' })
    return
  }
  try {
    await cartStore.addItem(item.productBarcode, 1)
    uni.showToast({ title: `已加购：${item.miniappName || item.productName}`, icon: 'success', duration: 1200 })
  } catch (_) {
    uni.showToast({ title: '加购失败', icon: 'none' })
  }
}

function chooseSpec(item: GoodsItem) {
  uni.showToast({ title: `选择规格：${item.miniappName || item.productName}`, icon: 'none', duration: 1500 })
}

function handleLocation() {
  uni.navigateTo({ url: '/pages/company-address/list/index' })
}

function goSearch() {
  uni.navigateTo({ url: '/pages/search/index' })
}

function goMore() {
  uni.showToast({ title: '更多商品', icon: 'none', duration: 1500 })
}
</script>

<style lang="scss" scoped>
/* ==================================================================
   分类页面样式
   ================================================================== */

.category-page {
  min-height: 100vh;
  background: #F9FAFB;
  display: flex;
  flex-direction: column;
}

/* ---- 顶部 ---- */
.header-bar {
  // background: #fdb2b2;
  background: linear-gradient(135deg, #ffe6e6, #ffe6e6);
  flex-shrink: 0;
}
.banner-card-wap{
  background: linear-gradient(180deg, #ffe6e6 5%, #ffe6e6 30%, #ffffff 100%);
}

/* ---- 标签滚动 ---- */
.tab-sticky {
  background: #fff;
  border-bottom: 1px solid #f3f4f6;
  flex-shrink: 0;
}

.tab-scroll {
  display: flex;
  white-space: nowrap;
  padding: 8px 4px;
  height: 160rpx;
}

.tab-item {
  display: inline-flex;
  flex-direction: column;
  align-items: center;
  padding: 2px 11px;
  position: relative;
  min-width: 140rpx;
}

.tab-icon-box {
  width: 36px;
  height: 36px;
  background: #FFF5F5;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 4px;
  transition: transform 0.2s;
}

.tab-icon-active {
  background: #FEE2E2;
  transform: scale(1.05);
}

.tab-name {
  color: #6B7280;
  font-weight: 500;
}

.tab-name-active {
  color: #DC2626;
  font-weight: 700;
}

.tab-active::after {
  // content: '';
  // position: absolute;
  // bottom: -5px;
  // left: 25%;
  // width: 50%;
  // height: 3px;
  // background: #DC2626;
  // border-radius: 2px;
}

/* ---- 主滚动区 ---- */
.main-scroll {
  flex: 1;
  overflow-y: auto;
}

/* ---- Banner ---- */
.banner {
  width: 100%;
  height: 90px;
  /* 渐变 → 模板 bg-gradient-to-br from-secondary to-primary */
  border-radius: 12px;
  position: relative;
  overflow: hidden;
  display: flex;
  align-items: center;
  padding: 0 16px;
  box-shadow: 0 4px 10px rgba(220, 38, 38, 0.15);
}

.banner-text {
  position: relative;
  z-index: 1;
}

.banner-badge {
  display: inline-block;
  /* 底色 → 模板 bg-cta */
  color: #fff;
  font-size: 10px;
  padding: 2px 6px;
  border-radius: 4px;
  margin-top: 4px;
}

.banner-emoji {
  position: absolute;
  right: 12px;
  font-size: 56px;
  opacity: 0.85;
  transform: rotate(15deg);
}

</style>
