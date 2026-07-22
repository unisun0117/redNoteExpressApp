<template>
  <div
    class="tw-flex tw-flex-col tw-items-center tw-justify-center tw-min-h-screen tw-bg-[--bg-color]"
  >
    <h1 class="tw-text-8xl tw-font-bold tw-text-[--color-primary] tw-mb-4">404</h1>
    <p class="tw-text-lg tw-text-gray-500 tw-mb-8">抱歉，您访问的页面不存在</p>
    <el-button type="primary" @click="$router.replace('/')">返回首页</el-button>
  </div>
</template>
