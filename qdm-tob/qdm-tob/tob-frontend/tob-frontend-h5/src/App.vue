<template>
  <router-view />
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { initWxConfig } from '@/utils/wechat'
import { setAppVersion } from '@/utils/sls'

onMounted(async () => {
  // 注入应用版本号到 SLS 模块
  try {
    const pkg = await import('../package.json')
    setAppVersion(pkg.version)
  } catch {
    // 无法获取 package.json 版本，使用默认值
  }

  // 全局初始化企业微信 JS-SDK 鉴权
  // 仅在企业微信客户端内执行，非企微环境自动静默跳过
  await initWxConfig()
})
</script>
