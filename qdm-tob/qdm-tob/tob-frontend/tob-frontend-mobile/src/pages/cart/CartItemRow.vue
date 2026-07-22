<template>
  <view class="swipe-container">
    <!-- 删除按钮（在背后右侧） -->
    <view class="swipe-delete" @click.stop="handleDelete">
      <text class="text-white text-sm font-bold">删除</text>
    </view>

    <!-- 可滑动的主内容 -->
    <view
      class="swipe-content"
      :class="{ 'swipe-content--open': isSwiped }"
      :style="{ transform: 'translateX(' + translateX + 'px)' }"
      @touchstart="onTouchStart"
      @touchmove="onTouchMove"
      @touchend="onTouchEnd"
    >
      <view class="cart-row" :class="{ 'cart-row--last': isLast }">
        <!-- 选择 -->
        <view class="select-box" :class="{ 'select-box--disabled': disabled }" @click.stop="!disabled && $emit('toggle', item.id)">
          <view class="check-dot" :class="disabled ? 'dot--disabled' : (item.selected ? 'dot--on' : 'dot--off')">
            <u-icon v-if="item.selected && !disabled" name="checkmark" size="11" color="#FFFFFF"></u-icon>
          </view>
        </view>

        <!-- 图片 -->
        <view class="row-img">
          <text class="row-img-text">{{ item.image }}</text>
        </view>

        <!-- 信息 -->
        <view class="row-info">
          <view class="flex items-center gap-1.5">
            <text v-if="item.brand" class="brand-tag">{{ item.brand }}</text>
            <text class="row-name fs-30rpx">{{ item.name }}</text>
          </view>

          <text v-if="item.priceDrop" class="drop-tip">比加入时降¥{{ item.priceDrop }}</text>

          <view v-if="item.spec" class="flex items-center gap-1 bg-gray-100 rounded px-1.5 py-px self-start">
            <text class="fs-24rpx text-gray-500">{{ item.spec }}</text>
            <u-icon name="arrow-down" size="8" color="#9CA3AF"></u-icon>
          </view>

          <view class="flex items-center justify-between gap-1.5">
            <text class="price-current">
              <text class="price-yen">¥</text>{{ formatPrice(item.price) }}
              <text v-if="item.originalPrice" class="price-line-through">¥{{ formatPrice(item.originalPrice) }}</text>
            </text>

            <u-number-box
              button-size="24"
              :modelValue="item.quantity"
              :min="0"
              size="mini"
              :max="item.stock"
              integer
              @change="onQuantityChange"
            />
          </view>

          <text v-if="item.estimatedPrice" class="est-tag fs-20rpx">
            预估到手价 ¥{{ formatPrice(item.estimatedPrice) }}
          </text>

          <view v-if="item.promotionNote" class="flex items-start gap-1">
            <up-icon class="promo-icon" name="info-circle" size="10"></up-icon>
            <text class="promo-text">{{ item.promotionNote }}</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import type { CartItem } from '@/store/modules/cart'

const props = defineProps<{
  item: CartItem
  isLast?: boolean
  disabled?: boolean
}>()

const emit = defineEmits<{
  toggle: [id: number]
  updateQuantity: [id: number, quantity: number]
  delete: [id: number]
}>()

/** 左滑删除 */
const DELETE_BTN_WIDTH = 70 // px，与 CSS 中 .swipe-delete 宽度一致
const SWIPE_THRESHOLD = 30 // 触发打开的滑动距离

const isSwiped = ref(false)
const translateX = ref(0)
let startX = 0
let startY = 0
let isHorizontal: boolean | null = null

function onTouchStart(e: any) {
  const touch = e.touches[0]
  startX = touch.clientX
  startY = touch.clientY
  isHorizontal = null
}

function onTouchMove(e: any) {
  const touch = e.touches[0]
  const deltaX = touch.clientX - startX
  const deltaY = touch.clientY - startY

  // 首次移动时判断方向
  if (isHorizontal === null) {
    isHorizontal = Math.abs(deltaX) > Math.abs(deltaY)
  }

  if (!isHorizontal) return

  // 只允许左滑（deltaX < 0），限制最大滑动距离
  if (isSwiped.value) {
    // 已打开状态：从 -DELETE_BTN_WIDTH 开始滑动
    translateX.value = Math.max(-DELETE_BTN_WIDTH, Math.min(0, -DELETE_BTN_WIDTH + deltaX))
  } else {
    translateX.value = Math.max(-DELETE_BTN_WIDTH, Math.min(0, deltaX))
  }
}

function onTouchEnd() {
  if (isSwiped.value) {
    // 已打开：右滑超过阈值则关闭
    if (translateX.value > -SWIPE_THRESHOLD) {
      closeSwipe()
    } else {
      translateX.value = -DELETE_BTN_WIDTH
    }
  } else {
    // 已关闭：左滑超过阈值则打开
    if (translateX.value < -SWIPE_THRESHOLD) {
      translateX.value = -DELETE_BTN_WIDTH
      isSwiped.value = true
    } else {
      translateX.value = 0
    }
  }
}

function closeSwipe() {
  translateX.value = 0
  isSwiped.value = false
}

function handleDelete() {
  closeSwipe()
  emit('delete', props.item.id)
}

function onQuantityChange(e: { value: number }) {
  emit('updateQuantity', props.item.id, e.value)
}

function formatPrice(price: number | null | undefined): string {
  if (price == null) return '--'
  if (Number.isInteger(price)) return String(price)
  return price.toFixed(2)
}
</script>

<style lang="scss" scoped>
/* ==================================================================
   左滑删除容器
   ================================================================== */
.swipe-container {
  position: relative;
  overflow: hidden;
  background: #DC2626;
}

/* 删除按钮（背后） */
.swipe-delete {
  position: absolute;
  right: 0;
  top: 0;
  bottom: 0;
  width: 70px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #DC2626;
}

/* 可滑动的内容 */
.swipe-content {
  position: relative;
  background: #fff;
  transition: transform 0.25s ease;
  z-index: 1;
}

.swipe-content--open {
  border-radius: 0;
}

/* ==================================================================
   购物车商品行 — 完整精修样式
   ================================================================== */

/* ---- 行 ---- */
.cart-row {
  display: flex;
  align-items: center;
  padding: 14px 14px;
  border-bottom: 1px solid #f8f8f8;
}

.cart-row--last {
  border-bottom: none;
}

/* ---- 选中 ---- */
.select-box {
  padding: 4px 10px 4px 0;
  flex-shrink: 0;
}

.check-dot {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
}

.dot--on {
  background: #DC2626;
  border: 2px solid #DC2626;
}

.dot--off {
  background: #fff;
  border: 2px solid #d1d5db;
}

.dot--disabled {
  background: #f3f4f6;
  border: 2px solid #e5e7eb;
}

.select-box--disabled {
  opacity: 0.5;
}

/* ---- 图片 ---- */
.row-img {
  width: 72px;
  height: 72px;
  background: #fafafa;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.row-img-text {
  font-size: 36px;
  line-height: 1;
}

/* ---- 信息区 ---- */
.row-info {
  flex: 1;
  min-width: 0;
  margin-left: 10px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.brand-tag {
  font-size: 10px;
  font-weight: 700;
  color: #fff;
  background: #EA580C;
  padding: 1px 5px;
  border-radius: 3px;
  flex-shrink: 0;
  line-height: 1.5;
}

.row-name {
  font-weight: 700;
  color: #1f2937;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  line-height: 1.4;
}

/* ---- 降价 ---- */
.drop-tip {
  font-size: 11px;
  color: #DC2626;
  font-weight: 500;
}

/* ---- 价格 ---- */
.price-current {
  font-size: 34rpx;
  font-weight: 900;
  color: #DC2626;
  line-height: 1.2;
}

.price-yen {
  font-size: 24rpx;
  font-weight: 700;
}

.price-line-through {
  font-size: 22rpx;
  color: #b0b0b0;
  text-decoration: line-through;
}

/* ---- 预估到手价 ---- */
.est-tag {
  display: inline-block;
  color: #db2777;
  background: #fdf2f8;
  border-radius: 3px;
  padding: 1px 6px;
  align-self: flex-start;
  font-weight: 500;
}

/* ---- 促销提示 ---- */
.promo-text {
  font-size: 10px;
  color: #9ca3af;
  line-height: 1.4;
}

.promo-icon {
  width: 14px;
  height: 14px;
  border: 1px solid #e5e7eb;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 8px;
  color: #b0b0b0;
  flex-shrink: 0;
  margin-top: 1px;
}
</style>
