<template>
  <view class="flex pb-3 border-b border-gray-100 last:border-b-0">
    <!-- 左侧商品图 -->
    <view
      class="w-[72px] h-[72px] bg-gray-100 rounded-lg flex items-center justify-center shrink-0 relative overflow-hidden"
    >
      <image
        v-if="item.mainImage"
        :src="item.mainImage"
        mode="aspectFill"
        class="w-full h-full"
      />
      <text v-else class="text-[48px] leading-none">📦</text>
      <text
        v-if="item.watermark"
        class="absolute bottom-1 right-1 text-[9px] text-green-500 opacity-50 font-bold"
      >
        {{ item.watermark }}
      </text>
    </view>

    <!-- 右侧信息 -->
    <view class="flex-1 min-w-0 ml-2.5 pr-2 flex flex-col">
      <!-- 商品名 -->
      <text class="fs-30rpx font-bold text-gray-900 leading-relaxed mb-1">{{ item.miniappName }}</text>

      <!-- 属性行 -->
      <text v-if="item.attrs" class="fs-24rpx text-gray-400 leading-relaxed mb-1">
        {{ item.attrs }}
      </text>

      <!-- 品质标签 -->
      <view
        v-if="item.qualityTag"
        class="inline-flex items-center bg-amber-50 text-amber-700 text-[10px] px-1.5 py-0.5 rounded-sm self-start mb-1"
      >
        <text>{{ item.qualityTag }}</text>
      </view>

      <!-- 促销红字 -->
      <text v-if="item.promoText" class="fs-24rpx text-primary mb-1">{{ item.promoText }}</text>

      <!-- 价格行 -->
      <view class="flex items-center justify-between mt-0.5">
        <view class="flex items-baseline flex-wrap">
          <text v-if="item.minWeight" class="text-[13px] font-bold text-gray-900 mr-0.5">
            {{ item.minWeight }}
          </text>
          <text class="fs-24rpx font-bold text-primary">¥</text>
          <text class="text-xl font-bold text-primary leading-none">{{ item.price }}</text>
          <text v-if="item.priceUnit" class="fs-22rpx text-gray-500 ml-1">/{{ item.priceUnit }}</text>
          <text v-if="item.totalPrice" class="fs-22rpx text-gray-700 ml-2">
            {{ item.totalPrice }}
          </text>
        </view>

        <!-- 选规格 -->
        <!-- <view
          v-if="item.hasSpec"
          class="p-btn rounded-lg px-3 py-1.5 shrink-0 fs-24rpx flex"
          @click.stop="$emit('choose-spec', item)"
        >
          <text class="fs-24rpx text-white font-medium">选规格</text>
        </view> -->
        <!-- 加购 / 数量步进器 -->
        <u-number-box
          v-if="quantity > 0"
          :modelValue="quantity"
          :min="0"
          :max="99"
          integer
          button-size="24"
          size="mini"
          @change="handleQuantityChange"
        />
        <view
          v-else
          class="w-6 h-6 p-btn rounded-full flex items-center justify-center shrink-0"
          @click.stop="handleAdd"
        >
          <u-icon name="plus" size="14" color="#FFFFFF"></u-icon>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { GoodsItem } from '@/types/goods'
import { useCartStore } from '@/store/modules/cart'

const cartStore = useCartStore()

const props = defineProps<{
  item: GoodsItem
}>()

const emit = defineEmits<{
  'add-to-cart': [item: GoodsItem]
  'choose-spec': [item: GoodsItem]
}>()

/** 购物车中对应商品条目 */
const cartEntry = computed(() =>
  cartStore.items.find(i => i.barcode === props.item.productBarcode)
)

/** 当前商品已加购数量，0 时显示 "+" 按钮，>0 时显示步进器 */
const quantity = computed(() => cartEntry.value?.quantity ?? 0)

/** 点击 "+" 加购 */
async function handleAdd() {
  if (!props.item.productBarcode) return
  try {
    await cartStore.addItem(props.item.productBarcode, 1)
    uni.showToast({ title: '已加购', icon: 'success', duration: 1000 })
  } catch (_) {
    uni.showToast({ title: '加购失败', icon: 'none' })
  }
}

/** 步进器数量变化 — 同步到后端购物车 */
async function handleQuantityChange(e: { value: number }) {
  if (!props.item.productBarcode) return
  try {
    const entry = cartEntry.value
    if (e.value <= 0 && entry) {
      await cartStore.removeItem(entry.id)
    } else if (entry) {
      await cartStore.updateQuantity(entry.id, e.value)
    } else {
      await cartStore.addItem(props.item.productBarcode, e.value)
    }
  } catch (_) {
    uni.showToast({ title: '操作失败', icon: 'none' })
  }
}
</script>
