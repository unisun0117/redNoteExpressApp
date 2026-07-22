<template>
  <view class="page">
    <!-- 顶部搜索栏 -->
    <view class="search-bar-wrap" :style="{ paddingTop: statusBarH + 6 + 'px' }">
      <view class="search-bar" @click="goSearch">
        <u-icon name="search" size="16" color="#666666"></u-icon>
        <text class="search-placeholder">搜索商品</text>
      </view>
    </view>

    <!-- 主体：左侧分类 + 右侧商品 -->
    <view class="main-body">
      <!-- 左侧分类 -->
      <scroll-view scroll-y :show-scrollbar="false" class="side-bar">
        <view
          class="side-item"
          :class="{ 'side-item--active': activePrimary === 'all' }"
          @click="switchPrimary('all')"
        >
          <text class="side-item-text" :class="{ 'side-item-text--active': activePrimary === 'all' }">
            📋 全部
          </text>
        </view>
        <view
          v-for="cat in primaryCategories"
          :key="cat.id"
          class="side-item"
          :class="{ 'side-item--active': activePrimary === cat.id }"
          @click="switchPrimary(cat.id)"
        >
          <text class="side-item-text" :class="{ 'side-item-text--active': activePrimary === cat.id }">
            {{ cat.name }}
          </text>
        </view>
      </scroll-view>

      <!-- 右侧商品列表 -->
      <scroll-view scroll-y :show-scrollbar="false" class="goods-list">
        <view v-if="loading" class="empty-tip">
          <text class="text-sm text-gray-400">加载中...</text>
        </view>
        <view v-else-if="currentGoods.length === 0" class="empty-tip">
          <text class="text-sm text-gray-400">暂无商品</text>
        </view>
        <GoodsCard
          v-for="(item, idx) in currentGoods"
          :key="idx"
          :item="item"
          @add-to-cart="addToCart"
          @choose-spec="chooseSpec"
        />
      </scroll-view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import GoodsCard from '@/components/GoodsCard.vue'
import { getProductList, getCategories, type CategoryVO } from '@/api/modules/product'
import { useCartStore } from '@/store/modules/cart'
import { useCustomerStore } from '@/store/modules/customer'
import type { GoodsItem } from '@/types/goods'
import { enrichGoodsList } from '@/utils/enrichGoods'
import { usePageGuard } from '@/router/index'
// usePageGuard()

const cartStore = useCartStore()
const customerStore = useCustomerStore()

const sysInfo = uni.getSystemInfoSync()
const statusBarH = ref(sysInfo.statusBarHeight ?? 20)

// --- 分类 ---
const primaryCategories = ref<{ id: string; name: string }[]>([])
const activePrimary = ref('all')

// --- 商品 ---
const products = ref<GoodsItem[]>([])
const loading = ref(false)

const currentGoods = computed((): GoodsItem[] => products.value)

onMounted(async () => {
  loading.value = true
  try {
    // 加载分类
    const catRes = await getCategories()
    if (catRes.code === 0 && catRes.data) {
      primaryCategories.value = catRes.data.map((c: CategoryVO) => ({
        id: c.id,
        name: c.alias || c.name,
      }))
    }
    // 加载商品
    const addrId = customerStore.addressId() || 1
    cartStore.fetchCart(addrId)
    const res = await getProductList(addrId)
    if (res.code === 0 && res.data) {
      products.value = enrichGoodsList(res.data.records ?? [])
    }
  } catch (_) {
    // 拦截器统一处理
  } finally {
    loading.value = false
  }
})

function switchPrimary(_id: string) {
  activePrimary.value = _id
}

function goSearch() {
  uni.navigateTo({ url: '/pages/search/index' })
}

function chooseSpec(_item: GoodsItem) {
  // 暂未实现规格选择
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
</script>

<style lang="scss" scoped>
.page {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #FFFFFF;
}

.search-bar-wrap {
  background: #FFFFFF;
  padding: 0 12px 8px 12px;
  flex-shrink: 0;
}

.search-bar {
  height: 36px;
  background: #F5F5F5;
  border-radius: 18px;
  display: flex;
  align-items: center;
  padding: 0 12px;
  width: calc(100% - 100px);
}

.search-placeholder {
  font-size: 14px;
  color: #888888;
  margin-left: 8px;
}

.main-body {
  flex: 1;
  display: flex;
  overflow: hidden;
}

.side-bar {
  width: 80px;
  background: #F7F8FA;
  flex-shrink: 0;
}

.side-item {
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 4px;
  position: relative;
}

.side-item--active {
  background: #FFFFFF;
}

.side-item--active::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 3px;
  height: 18px;
  background: #FF4D2D;
  border-radius: 2px;
}

.side-item-text {
  font-size: 12px;
  color: #333333;
  text-align: center;
}

.side-item-text--active {
  color: #111111;
  font-weight: 700;
}

.goods-list {
  flex: 1;
  background: #FFFFFF;
  padding: 0 12rpx;
}

.empty-tip {
  text-align: center;
  padding: 60px 0;
}
</style>
