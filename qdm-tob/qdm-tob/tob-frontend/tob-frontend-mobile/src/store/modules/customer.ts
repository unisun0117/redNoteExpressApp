/**
 * 当前选中客户 Store — 持久化到本地存储
 */
import { defineStore } from 'pinia'
import { ref } from 'vue'

export interface CustomerInfo {
  archiveId: number
  customerName: string
  contactName: string
  contactPhone: string
  address: string
}

export const useCustomerStore = defineStore('customer', () => {
  const current = ref<CustomerInfo | null>(loadCustomer())

  function loadCustomer(): CustomerInfo | null {
    try {
      const raw = uni.getStorageSync('current_customer')
      return raw ? JSON.parse(raw) : null
    } catch {
      return null
    }
  }

  function saveCustomer(c: CustomerInfo) {
    current.value = c
    uni.setStorageSync('current_customer', JSON.stringify(c))
  }

  function clearCustomer() {
    current.value = null
    uni.removeStorageSync('current_customer')
  }

  const addressId = () => current.value?.archiveId

  return { current, addressId, saveCustomer, clearCustomer }
})
