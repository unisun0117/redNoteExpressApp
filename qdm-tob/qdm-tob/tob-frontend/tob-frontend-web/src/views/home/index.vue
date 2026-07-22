<template>
  <div class="tw-p-8">
    <h2 class="tw-text-2xl tw-font-semibold tw-mb-4">欢迎使用管理后台</h2>
    <p class="tw-text-gray-500 tw-mb-6">此页面为首页占位，后续可按需替换为实际 Dashboard 内容。</p>
    <el-card class="tw-max-w-md">
      <template #header>
        <span>快速入门</span>
      </template>
      <ul class="tw-list-disc tw-list-inside tw-text-sm tw-text-gray-600 tw-space-y-2">
        <li>在 src/router/modules/ 中添加业务模块路由</li>
        <li>在 src/api/modules/ 中添加 API 接口定义</li>
        <li>在 src/views/ 中创建业务页面组件</li>
        <li>在 src/stores/ 中管理业务状态</li>
      </ul>
    </el-card>
  </div>
</template>
