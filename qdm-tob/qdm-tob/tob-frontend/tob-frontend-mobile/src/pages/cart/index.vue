<template>
  <view class="cart-page">

    <view class="cart-nav" :style="{ paddingTop: statusBarH + 'px' }">
      <view class="flex items-center px-4 pt-3 pb-2 text-base" @click="handleAddressClick">
        <text class="text-gray-500 h-5 mr-1"></text>
      </view>

      <view class="flex items-center justify-between px-4 pt-2 pb-3">
        <view class="flex items-baseline gap-5">
          <text class="text-xl font-extrabold text-gray-900">购物车</text>
          <text class="text-lg text-gray-500">我常买</text>
        </view>
        <view class="flex items-center gap-4">
          <view class="flex items-center gap-1" @click="goCoupon">
            <text class="text-base text-gray-500">优惠券({{ couponCount }})</text>
          </view>
          <text
            class="text-base font-medium"
            :class="isEditing ? 'text-red-500' : 'text-gray-500'"
            @click="toggleEditMode"
          >{{ isEditing ? '完成' : '管理' }}</text>
        </view>
      </view>
    </view>

    <!-- 加载中 -->
    <view v-if="loading" class="pt-22 flex justify-center">
      <text class="text-gray-400">加载中...</text>
    </view>

    <!-- 列表 -->
    <view v-else-if="items.length > 0" :style="{ paddingTop: navTotalH + 10 + 'px' }">
      <!-- 有效商品 -->
      <view class="px-3 mb-2">
        <view class="bg-white rounded-xl overflow-hidden">
          <CartItemRow
            v-for="(item, idx) in validItems"
            :key="item.id"
            :item="item"
            :is-last="idx === validItems.length - 1 && invalidItems.length === 0"
            @toggle="(id) => cartStore.toggleItem(Number(id))"
            @update-quantity="(id, qty) => cartStore.updateQuantity(Number(id), qty)"
          />
        </view>
      </view>

      <!-- 失效商品分隔 -->
      <view v-if="invalidItems.length > 0" class="px-3 mb-2">
        <view class="flex items-center justify-between px-1 py-2">
          <text class="text-xs text-gray-400">已失效商品</text>
          <view class="clear-invalid-btn" @click="handleClearInvalid">
            <text class="text-xs text-gray-500">一键清除</text>
          </view>
        </view>
        <view class="bg-white rounded-xl overflow-hidden invalid-section">
          <CartItemRow
            v-for="(item, idx) in invalidItems"
            :key="item.id"
            :item="item"
            :disabled="true"
            :is-last="idx === invalidItems.length - 1"
            @toggle="() => {}"
            @update-quantity="() => {}"
          />
        </view>
      </view>
    </view>

    <!-- 空状态 -->
    <view v-else class="empty-wrap" :style="{ paddingTop: navTotalH + 80 + 'px' }">
      <text class="text-7xl mb-5">🛒</text>
      <text class="text-gray-400 text-base font-medium mb-1.5">购物车还是空的</text>
      <text class="text-gray-300 text-sm mb-8">快去逛逛吧～</text>
      <view class="empty-btn" @click="goHome">
        <text class="text-white text-base font-bold">去逛逛</text>
      </view>
    </view>

    <!-- 凑单助手 -->
    <view  v-if="shortageForFreeShipping > 0" class="addon-bar" @click="goAddOn">
      <view class="flex items-center gap-1.5 flex-1 min-w-0">
        <text class="text-sm font-extrabold text-red-600 shrink-0">凑单助手</text>
        <text class="addon-text">
          整单再买<text class="addon-highlight">{{ formatPrice(shortageForFreeShipping) }}</text>元，可享满{{ FREE_SHIPPING_THRESHOLD }}元免基础配送费
        </text>
      </view>
      <text class="addon-btn">去凑单</text>
    </view>
    <view v-if="shortageForFreeShipping <= 0 && selectedCount > 0" class="free-ship-bar">
      <text class="text-sm text-green-700 font-medium">
        ✓ 已满{{ FREE_SHIPPING_THRESHOLD }}元，可享免基础配送费
      </text>
    </view>

    <view v-if="items.length > 0" class="settle-bar" :style="{ paddingBottom: safeAreaBottom + 10 + 'px' }">
      <view class="flex items-center gap-2 mr-4" @click="toggleAll">
        <view class="check-dot" :class="isAllSelected ? 'dot--on' : 'dot--off'">
          <u-icon v-if="isAllSelected" name="checkmark" size="11" color="#FFFFFF"></u-icon>
        </view>
        <text class="text-sm text-gray-800 font-medium">全选</text>
      </view>

      <view class="flex-1 min-w-0 text-right" @click="showDetail = !showDetail">
        <view class="flex items-baseline justify-end gap-1">
          <text class="text-xs text-gray-500">券后合计¥</text>
          <text class="settle-price">{{ formatPrice(totalPayable) }}</text>
          <u-icon name="arrow-up" size="10" color="#DC2626"></u-icon>
        </view>
        <text class="settle-sub justify-end">
          含配送费¥{{ shippingFee }}，已减<text class="text-red-500 font-medium">¥{{ formatPrice(totalSaved) }}</text>元
        </text>
      </view>

      <view
        v-if="isEditing"
        class="settle-btn settle-btn--delete"
        @click="handleDeleteSelected"
      >
        <text class="text-white text-[15px] font-bold">删除({{ selectedCount }})</text>
      </view>
      <view
        v-else
        class="settle-btn settle-btn--checkout"
        @click="goSettle"
      >
        <text class="text-white text-[15px] font-bold">去结算({{ selectedCount }})</text>
      </view>
    </view>
  </view>
</template>

<script lang="ts">
import { ref, computed, defineComponent } from 'vue'
import { storeToRefs } from 'pinia'
import { useCartStore, type CartItem } from '@/store/modules/cart'
import type { CartItemVO } from '@/api/modules/cart'
import CartItemRow from './CartItemRow.vue'

export default defineComponent({
  name: 'CartPage',
  components: { CartItemRow },
  setup() {
    const cartStore = useCartStore()

    const {
      items,
      loading,
      selectedCount,
      isAllSelected,
    } = storeToRefs(cartStore)

    const {
      toggleAll,
      removeSelected,
    } = cartStore

    const FREE_SHIPPING_THRESHOLD = 35
    const sysInfo = uni.getSystemInfoSync()
    const statusBarH = ref(sysInfo.statusBarHeight ?? 20)
    // #ifdef MP-WEIXIN
    const safeAreaBottom = ref(0)
    // #endif
    // #ifdef H5
    const safeAreaBottom = ref(50)
    // #endif
    const navTotalH = computed(() => statusBarH.value + 88)
    const couponCount = ref(1)
    const showDetail = ref(false)
    const isEditing = ref(false)
    const shippingFee = ref(0)

    /** 编辑模式切换 */
    function toggleEditMode() {
      isEditing.value = !isEditing.value
    }

    /** CartItemVO → CartItem 映射 */
    function mapToCartItem(vo: CartItemVO): CartItem {
      return {
        id: vo.id,
        name: vo.goodsName,
        image: vo.productImage || '📦',
        price: vo.unitPrice ?? 0,
        quantity: vo.quantity,
        selected: vo.selected === 1,
        productStatus: vo.productStatus,
        spec: vo.unit,
        valid: vo.valid !== false,
      }
    }

    /** 映射后的购物车列表 */
    const mappedItems = computed(() => items.value.map(mapToCartItem))

    /** 有效商品 */
    const validItems = computed(() => mappedItems.value.filter(item => item.valid))

    /** 失效商品 */
    const invalidItems = computed(() => mappedItems.value.filter(item => !item.valid))

    /** 选中商品总金额 */
    const totalPayable = computed(() => {
      return cartStore.selectedItems.reduce((sum, item) => sum + item.unitPrice * item.quantity, 0)
    })

    /** 已优惠金额（暂以原价与现价差额估算） */
    const totalSaved = computed(() => 0)

    /** 距免配送费还差多少 */
    const shortageForFreeShipping = computed(() => {
      return Math.max(0, FREE_SHIPPING_THRESHOLD - totalPayable.value)
    })

    function formatPrice(unitPrice: number): string {
      if (Number.isInteger(unitPrice)) return String(unitPrice)
      return unitPrice.toFixed(2)
    }

    /** 一键清除失效商品 */
    function handleClearInvalid() {
      if (invalidItems.value.length === 0) return
      const ids = invalidItems.value.map(item => item.id)
      uni.showModal({
        title: '清除失效商品',
        content: `确定要清除 ${invalidItems.value.length} 件失效商品吗？`,
        success: (res: any) => {
          if (res.confirm) {
            cartStore.removeBatch(ids)
            uni.showToast({ title: '已清除', icon: 'success', duration: 1200 })
          }
        },
      })
    }

    function handleAddressClick() { /* 选址切换 */ }
    function goCoupon() { uni.navigateTo({ url: '/sub-pages/coupon/pages/list/index' }) }
    function goAddOn() { uni.switchTab({ url: '/pages/index/index' }) }
    function goHome() { uni.switchTab({ url: '/pages/index/index' }) }

    function goSettle() {
      if (selectedCount.value === 0) {
        uni.showToast({ title: '请选择商品', icon: 'none', duration: 1500 })
        return
      }
      uni.navigateTo({ url: '/sub-pages/order/checkout/index' })
    }

    function handleDeleteSelected() {
      if (selectedCount.value === 0) {
        uni.showToast({ title: '请选择商品', icon: 'none', duration: 1500 })
        return
      }
      uni.showModal({
        title: '确认删除',
        content: `确定要删除选中的 ${selectedCount.value} 件商品吗？`,
        success: (res: any) => {
          if (res.confirm) {
            removeSelected()
            uni.showToast({ title: '已删除', icon: 'success', duration: 1200 })
          }
        },
      })
    }

    return {
      cartStore,
      items,
      loading,
      selectedCount,
      isAllSelected,
      toggleAll,
      removeSelected,
      FREE_SHIPPING_THRESHOLD,
      statusBarH,
      safeAreaBottom,
      navTotalH,
      couponCount,
      showDetail,
      isEditing,
      shippingFee,
      toggleEditMode,
      mappedItems,
      validItems,
      invalidItems,
      totalPayable,
      totalSaved,
      shortageForFreeShipping,
      formatPrice,
      handleClearInvalid,
      handleAddressClick,
      goCoupon,
      goAddOn,
      goHome,
      goSettle,
      handleDeleteSelected,
    }
  },
})
</script>

<style lang="scss" scoped>
.cart-page { min-height: 100vh; background: #f5f5f5; }
.cart-nav { position: fixed; top: 0; left: 0; right: 0; z-index: 50; background: #f5f5f5; }
.settle-bar { position: fixed; bottom: 50px; left: 0; right: 0; z-index: 50; background: #fff; border-top: 1px solid #f3f4f6; display: flex; align-items: center; padding: 10px 14px; }
.check-dot { width: 20px; height: 20px; border-radius: 50%; display: flex; align-items: center; justify-content: center; flex-shrink: 0; transition: all 0.2s ease; }
.dot--on { background: #DC2626; border: 2px solid #DC2626; }
.dot--off { background: #fff; border: 2px solid #d1d5db; }
.settle-btn { border-radius: 24px; padding: 10px 22px; flex-shrink: 0; margin-left: 8px; transition: all 0.2s ease; }
.settle-btn--checkout { background: linear-gradient(135deg, #DC2626, #e5a708); }
.settle-btn--delete { background: linear-gradient(135deg, #DC2626, #ef4444); }
.empty-wrap { display: flex; flex-direction: column; align-items: center; }
.empty-btn { background: linear-gradient(135deg, #DC2626, #ef4444); border-radius: 24px; padding: 10px 40px; box-shadow: 0 4px 12px rgba(220, 38, 38, 0.25); }
.cart-page {
  min-height: 100vh;
  background: #f5f5f5;
}

.clear-invalid-btn {
  background: #f3f4f6;
  border-radius: 12px;
  padding: 4px 12px;
}

.invalid-section {
  opacity: 0.7;
}
</style>
