<template>
  <view class="min-h-screen flex flex-col" style="background: linear-gradient(180deg, #FEF2F2 0%, #FFFFFF 40%);">
    <!-- ============================================================
         搜索栏
         ============================================================ -->
    <view class="flex items-center px-3 pt-3 pb-3" style="gap: 10px;">
      <view
        class="flex-1 flex items-center rounded-full py-2 transition-all duration-200"
        style="padding-left: 14px; padding-right: 14px;"
        :style="inputFocused
          ? 'background: #FFFFFF; border: 1.5px solid #DC2626; box-shadow: 0 0 0 3px rgba(220,38,38,0.08);'
          : 'background: #F3F4F6; border: 1.5px solid transparent;'"
      >
        <text class="text-base mr-2 flex-shrink-0">🔍</text>
        <input
          :value="keyword"
          :focus="true"
          type="text"
          placeholder="搜索商品/品类"
          maxlength="50"
          class="flex-1 text-base bg-transparent"
          style="color: #450A0A; min-height: 22px;"
          placeholder-style="color: #9CA3AF;"
          confirm-type="search"
          @input="onKeywordInput"
          @focus="inputFocused = true"
          @blur="inputFocused = false"
          @confirm="onSearchInput"
        />
        <!-- 清除按钮 -->
        <view
          v-if="keyword"
          class="w-5 h-5 rounded-full flex items-center justify-center flex-shrink-0 ml-2"
          style="background: #D1D5DB;"
          @click="clearSearch"
        >
          <text class="text-xs" style="color: #FFFFFF;">✕</text>
        </view>
      </view>
      <!-- 取消按钮 -->
      <text
        class="text-base flex-shrink-0 font-medium"
        style="color: #6B7280;"
        @click="goBack"
      >
        取消
      </text>
    </view>

    <!-- ============================================================
         搜索中占位
         ============================================================ -->
    <view v-if="searching" class="flex-1 px-3">
      <view class="flex flex-wrap" style="margin: -6px;">
        <view
          v-for="i in 4"
          :key="'skel-' + i"
          class="mb-3"
          style="width: 50%; padding: 0 6px;"
        >
          <view class="rounded-xl overflow-hidden" style="background: #F3F4F6;">
            <view style="height: 168px; background: #E5E7EB;" />
            <view style="padding: 10px;">
              <view class="mb-2 rounded" style="height: 14px; width: 70%; background: #E5E7EB;" />
              <view class="rounded" style="height: 16px; width: 40%; background: #E5E7EB;" />
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- ============================================================
         搜索结果区
         ============================================================ -->
    <scroll-view
      v-else-if="keyword && !searching"
      scroll-y
      style="flex: 1; height: 0;"
    >
      <!-- 有结果：双列商品卡片网格 -->
      <view v-if="results.length > 0" class="px-3 pt-1 pb-6">
        <view class="flex flex-wrap" style="margin: 0 -6px;">
          <view
            v-for="item in results"
            :key="item.id"
            class="mb-3"
            style="width: 50%; padding: 0 6px;"
          >
            <!-- 商品卡片 -->
            <view
              class="rounded-xl overflow-hidden relative"
              style="background: #FFFFFF; box-shadow: 0 2px 12px rgba(0,0,0,0.06);"
              @click="goDetail(item.id)"
            >
              <!-- 商品图片区 — 使用固定高度（小程序不支持 aspect-ratio） -->
              <view class="relative w-full overflow-hidden" style="height: 168px;">
                <image
                  v-if="item.image"
                  :src="item.image"
                  mode="aspectFill"
                  class="w-full h-full"
                  style="border-radius: 12px 12px 0 0;"
                  @error="onImageError(item)"
                />
                <!-- 占位图（无图片或加载失败时显示） -->
                <view
                  v-if="!item.image || item.imageFailed"
                  class="w-full h-full flex flex-col items-center justify-center"
                  style="background: #FEF2F2; border-radius: 12px 12px 0 0;"
                >
                  <text class="text-2xl mb-1">📦</text>
                  <text class="text-xs" style="color: #FCA5A5;">暂无图片</text>
                </view>

                <!-- 「惠」活动标签 -->
                <view
                  v-if="item.hasActivity"
                  class="absolute top-2 left-2 rounded-full flex items-center justify-center"
                  style="
                    background: linear-gradient(135deg, #DC2626 0%, #EF4444 100%);
                    padding: 2px 10px;
                    box-shadow: 0 2px 8px rgba(220,38,38,0.3);
                  "
                >
                  <text class="text-xs font-bold" style="color: #FFFFFF;">惠</text>
                </view>
              </view>

              <!-- 商品信息区 -->
              <view style="padding: 10px;">
                <!-- 商品名称（最多2行截断） -->
                <text
                  class="text-sm font-medium block leading-tight"
                  style="
                    color: #450A0A;
                    display: -webkit-box;
                    -webkit-line-clamp: 2;
                    -webkit-box-orient: vertical;
                    overflow: hidden;
                    margin-bottom: 6px;
                  "
                >
                  {{ item.name }}
                </text>

                <!-- 价格 + 加购按钮 -->
                <view class="flex items-center justify-between">
                  <view class="flex items-baseline">
                    <text class="text-xs font-semibold" style="color: #DC2626;">¥</text>
                    <text class="text-lg font-bold leading-none" style="color: #DC2626;">
                      {{ formatPrice(item.price) }}
                    </text>
                  </view>

                  <!-- 加购按钮 -->
                  <view
                    class="w-7 h-7 rounded-full flex items-center justify-center flex-shrink-0"
                    style="
                      background: linear-gradient(135deg, #DC2626 0%, #EA580C 100%);
                      box-shadow: 0 2px 8px rgba(220,38,38,0.25);
                    "
                    @click.stop="addToCart(item)"
                  >
                    <text class="text-base font-bold" style="color: #FFFFFF; margin-top: -1px;">+</text>
                  </view>
                </view>
              </view>
            </view>
          </view>
        </view>
      </view>

      <!-- 无结果：空状态 -->
      <view v-else class="flex flex-col items-center justify-center px-8" style="padding-top: 30%;">
        <view class="w-24 h-24 rounded-full flex items-center justify-center mb-5" style="background: #FEF2F2;">
          <text class="text-4xl">🔍</text>
        </view>
        <text class="text-lg font-semibold mb-2" style="color: #450A0A;">
          未找到相关商品
        </text>
        <text class="text-sm text-center" style="color: #9A3412; line-height: 20px;">
          换个关键词试试
        </text>
      </view>
    </scroll-view>

    <!-- ============================================================
         初始状态（无关键词时）
         ============================================================ -->
    <view
      v-if="!keyword && !searching"
      class="flex-1 flex flex-col items-center justify-center px-8"
      style="padding-top: 25%;"
    >
      <view class="w-20 h-20 rounded-full flex items-center justify-center mb-4" style="background: #FEF2F2;">
        <text class="text-3xl">🛒</text>
      </view>
      <text class="text-base font-medium" style="color: #9A3412;">
        输入关键词搜索商品
      </text>
      <text class="text-xs" style="color: #FCA5A5; margin-top: 6px;">
        支持按商品名称、品类名称搜索
      </text>
    </view>
  </view>
</template>

<!-- ==================================================================
     Script
     ================================================================== -->
<script setup lang="ts">
import { ref } from 'vue'

// ------------------------------------------------------------------
// Mock 商品数据（按品类分组）
// ------------------------------------------------------------------

interface Product {
  id: string
  name: string
  image: string
  price: number
  hasActivity: boolean
  categoryName: string
  imageFailed?: boolean
}

const mockProducts: Product[] = [
  // 猪肉类
  { id: 'p1', name: '五花肉（精品切块）', image: '', price: 32.50, hasActivity: true, categoryName: '猪肉' },
  { id: 'p2', name: '排骨（肋排段）', image: '', price: 45.00, hasActivity: false, categoryName: '猪肉' },
  { id: 'p3', name: '猪蹄（对半切开）', image: '', price: 28.80, hasActivity: true, categoryName: '猪肉' },
  { id: 'p4', name: '梅花肉片（火锅专用）', image: '', price: 38.00, hasActivity: false, categoryName: '猪肉' },
  { id: 'p5', name: '瘦肉馅（肥瘦比例3:7）', image: '', price: 22.50, hasActivity: true, categoryName: '猪肉' },
  { id: 'p6', name: '猪肚（清洗干净）', image: '', price: 55.00, hasActivity: false, categoryName: '猪肉' },

  // 蔬菜类
  { id: 'v1', name: '有机菠菜（新鲜采摘）', image: '', price: 8.90, hasActivity: false, categoryName: '蔬菜' },
  { id: 'v2', name: '西红柿（沙瓤多汁）', image: '', price: 6.50, hasActivity: true, categoryName: '蔬菜' },
  { id: 'v3', name: '娃娃菜（精品装）', image: '', price: 5.80, hasActivity: false, categoryName: '蔬菜' },
  { id: 'v4', name: '青椒（薄皮微辣）', image: '', price: 7.20, hasActivity: false, categoryName: '蔬菜' },
  { id: 'v5', name: '土豆（黄心大个）', image: '', price: 3.50, hasActivity: false, categoryName: '蔬菜' },

  // 海鲜类
  { id: 's1', name: '基围虾（鲜活大号）', image: '', price: 68.00, hasActivity: true, categoryName: '海鲜' },
  { id: 's2', name: '三文鱼（冰鲜切块）', image: '', price: 98.00, hasActivity: true, categoryName: '海鲜' },
  { id: 's3', name: '鲈鱼（现杀去鳞）', image: '', price: 42.00, hasActivity: false, categoryName: '海鲜' },
  { id: 's4', name: '花蛤（吐沙干净）', image: '', price: 18.00, hasActivity: false, categoryName: '海鲜' },
  { id: 's5', name: '带鱼段（东海冰鲜）', image: '', price: 35.00, hasActivity: true, categoryName: '海鲜' },

  // 冻品类
  { id: 'f1', name: '鸡胸肉（去皮冷冻）', image: '', price: 15.80, hasActivity: false, categoryName: '冻品' },
  { id: 'f2', name: '鸡翅中（腌制入味）', image: '', price: 28.00, hasActivity: true, categoryName: '冻品' },
  { id: 'f3', name: '速冻水饺（猪肉白菜）', image: '', price: 12.50, hasActivity: false, categoryName: '冻品' },
  { id: 'f4', name: '肥牛卷（火锅专用）', image: '', price: 42.00, hasActivity: true, categoryName: '冻品' },

  // 粮油类
  { id: 'g1', name: '金龙鱼调和油 5L', image: '', price: 79.90, hasActivity: false, categoryName: '粮油' },
  { id: 'g2', name: '东北大米 10kg', image: '', price: 65.00, hasActivity: false, categoryName: '粮油' },
  { id: 'g3', name: '面粉 5kg（中筋）', image: '', price: 28.00, hasActivity: false, categoryName: '粮油' },
]

// ------------------------------------------------------------------
// 响应式数据
// ------------------------------------------------------------------

const keyword = ref('')
const results = ref<Product[]>([])
const searching = ref(false)
const inputFocused = ref(false)

let searchTimer: ReturnType<typeof setTimeout> | null = null

// ------------------------------------------------------------------
// 搜索逻辑（防抖 300ms，支持按商品名称和品类名称匹配）
// ------------------------------------------------------------------

function onKeywordInput(e: { detail: { value: string } }) {
  keyword.value = e.detail.value
  if (searchTimer) clearTimeout(searchTimer)
  if (!keyword.value.trim()) {
    results.value = []
    searching.value = false
    return
  }
  searching.value = true
  searchTimer = setTimeout(() => {
    doSearch(keyword.value.trim())
    searching.value = false
    searchTimer = null
  }, 300)
}

function onSearchInput() {
  if (searchTimer) clearTimeout(searchTimer)
  if (!keyword.value.trim()) return
  searching.value = true
  searchTimer = setTimeout(() => {
    doSearch(keyword.value.trim())
    searching.value = false
    searchTimer = null
  }, 300)
}

function doSearch(kw: string) {
  const lower = kw.toLowerCase()
  results.value = mockProducts.filter(
    (p) =>
      p.name.toLowerCase().includes(lower) ||
      p.categoryName.toLowerCase().includes(lower),
  )
}

function clearSearch() {
  keyword.value = ''
  results.value = []
  searching.value = false
  if (searchTimer) {
    clearTimeout(searchTimer)
    searchTimer = null
  }
}

// ------------------------------------------------------------------
// 加购（Mock 行为）
// ------------------------------------------------------------------

function addToCart(_item: Product) {
  uni.showToast({ title: '已加购', icon: 'success', duration: 1200 })
}

// ------------------------------------------------------------------
// 图片加载失败处理
// ------------------------------------------------------------------

function onImageError(item: Product) {
  item.imageFailed = true
}

// ------------------------------------------------------------------
// 价格格式化
// ------------------------------------------------------------------

function formatPrice(price: number): string {
  return price.toFixed(2)
}

// ------------------------------------------------------------------
// 跳转
// ------------------------------------------------------------------

function goDetail(_id: string) {
  uni.showToast({ title: '商品详情页待开发', icon: 'none', duration: 1500 })
}

function goBack() {
  uni.switchTab({ url: '/pages/index/index' })
}
</script>

<!-- ==================================================================
     Style
     ================================================================== -->
<style lang="scss" scoped>
/* 全部样式通过内联 + Tailwind 实现 */
</style>
