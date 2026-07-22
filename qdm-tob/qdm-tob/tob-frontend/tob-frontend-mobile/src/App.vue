<script setup lang="ts">
import { onLaunch, onShow, onHide } from '@dcloudio/uni-app'

onLaunch(() => {
  console.log('[App] 小程序启动')
})

onShow(() => {
  console.log('[App] 小程序显示')
})

onHide(() => {
  console.log('[App] 小程序隐藏')
})
</script>

<style lang="scss">
/* 引入 Tailwind CSS */
@import '@/styles/tailwind.css';
/* 引入 uview-plus 基础样式 */
@import 'uview-plus/index.scss';

.u-navbar__content__title{
  font-weight: bold;
}
</style>
