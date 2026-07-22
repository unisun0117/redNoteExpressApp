/**
 * 购物车状态管理 Store（API 驱动）
 */

import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import {
  getCartList,
  addToCart,
  updateCart,
  removeCartItem,
  removeCartBatch,
  type CartItemVO,
} from '@/api/modules/cart'

/** 购物车行组件使用的数据接口 */
export interface CartItem {
  id: number
  name: string
  image: string
  price: number
  originalPrice?: number
  quantity: number
  selected: boolean
  brand?: string
  spec?: string
  priceDrop?: string
  estimatedPrice?: number
  promotionNote?: string
  stock?: number
  productStatus: string
  valid: boolean
}

export const useCartStore = defineStore('cart', () => {
  const items = ref<CartItemVO[]>([])
  const loading = ref(false)
  let _addressId = 0

  const selectedItems = computed(() => items.value.filter((i) => i.selected === 1 && i.valid !== false))
  const selectedCount = computed(() => selectedItems.value.reduce((s, i) => s + i.quantity, 0))

  const validItems = computed(() => items.value.filter((i) => i.valid !== false))
  const isAllSelected = computed(
    () => validItems.value.length > 0 && validItems.value.every((i) => i.selected === 1)
  )

  async function _fetch() {
    if (!_addressId) return
    loading.value = true
    try {
      const res = await getCartList(_addressId)
      if (res.code === 0) items.value = res.data ?? []
    } finally {
      loading.value = false
    }
  }

  function setAddressId(id: number) { _addressId = id }

  async function fetchCart(addressId: number) {
    _addressId = addressId
    await _fetch()
  }

  async function addItem(barcode: string, quantity: number) {
    await addToCart(barcode, quantity)
    await _fetch()
  }

  async function toggleItem(id: number) {
    const item = items.value.find((i) => i.id === id)
    if (!item) return
    const newSelected = item.selected === 1 ? 0 : 1
    await updateCart(id, { selected: newSelected })
    await _fetch()
  }

  async function updateQuantity(id: number, quantity: number) {
    if (quantity < 1) {
      await removeItem(id)
      return
    }
    await updateCart(id, { quantity })
    await _fetch()
  }

  async function removeItem(id: number) {
    await removeCartItem(id)
    await _fetch()
  }

  async function removeSelected() {
    const ids = selectedItems.value.map((i) => i.id)
    for (const id of ids) {
      await removeCartItem(id)
    }
    await _fetch()
  }

  async function removeBatch(ids: number[]) {
    if (ids.length === 0) return
    await removeCartBatch(ids)
    await _fetch()
  }

  async function toggleAll() {
    const newVal = isAllSelected.value ? 0 : 1
    // 仅切换有效商品，失效商品不可选中
    const targetItems = validItems.value
    const changedIds = targetItems
      .filter((i) => i.selected !== newVal)
      .map((i) => i.id)
    // 乐观更新：先切本地状态，即时 UI 反馈
    for (const item of targetItems) {
      item.selected = newVal
    }
    for (const id of changedIds) {
      await updateCart(id, { selected: newVal })
    }
    await _fetch()
  }

  return {
    items,
    loading,
    selectedItems,
    selectedCount,
    isAllSelected,
    setAddressId,
    fetchCart,
    addItem,
    toggleItem,
    updateQuantity,
    removeItem,
    removeSelected,
    removeBatch,
    toggleAll,
  }
})
