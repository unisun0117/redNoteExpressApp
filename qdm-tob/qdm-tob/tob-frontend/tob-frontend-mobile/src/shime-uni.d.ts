/// <reference types='@dcloudio/types' />
import 'vue'
// 🌟 1. 引入刚才导出的路由工具类型
import type { ToolsType } from '@/utils/router' 

declare module '@vue/runtime-core' {
  type Hooks = App.AppInstance & Page.PageInstance
  interface ComponentCustomOptions extends Hooks {}

  // 🌟 2. 扩充全局属性，让 Vue 实例和 template 认识 $tools 的结构
  interface ComponentCustomProperties {
    $tools: ToolsType
  }
}