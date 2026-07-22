<template>
  <web-view :src="h5Url" @error="onError" />
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { onLoad } from '@dcloudio/uni-app'

const h5Url = ref('')

onLoad((query?: { url?: string }) => {
  if (query?.url) {
    h5Url.value = decodeURIComponent(query.url)
  } else {
    uni.showToast({ title: '页面地址缺失', icon: 'none', duration: 2000 })
    setTimeout(() => uni.navigateBack(), 2000)
  }
})

function onError(): void {
  uni.showToast({ title: '页面加载失败', icon: 'none', duration: 2000 })
}
</script>
