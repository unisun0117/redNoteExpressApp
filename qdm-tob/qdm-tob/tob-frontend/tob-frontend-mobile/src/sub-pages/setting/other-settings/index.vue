<template>
  <view style="min-height:100vh;background:linear-gradient(180deg,#FEF2F2 0%,#FFFFFF 40%);display:flex;flex-direction:column;">

    <!-- ============================================================
         个人信息收集清单 + 第三方共享（从后端拉取）
         ============================================================ -->
    <view style="padding:12px 12px 0;">
      <view style="border-radius:12px;overflow:hidden;background:#FFFFFF;box-shadow:0 2px 16px rgba(220,38,38,0.06);">
        <view
          v-for="(item, idx) in docEntries"
          :key="item.key"
          style="display:flex;align-items:center;justify-content:space-between;padding:14px 16px;"
          :style="{ borderBottom: idx < docEntries.length - 1 ? '1px solid #F3F4F6' : 'none' }"
          @click="goToDoc(item.key, item.label)"
        >
          <view style="display:flex;align-items:center;gap:10px;">
            <text style="font-size:18px;">{{ item.icon }}</text>
            <text style="font-size:15px;font-weight:500;color:#1F2937;">{{ item.label }}</text>
          </view>
          <text style="font-size:16px;color:#D1D5DB;">›</text>
        </view>
      </view>
    </view>

    <!-- ============================================================
         展开式入口：投诉举报 / 用户注销 / 客服热线
         ============================================================ -->
    <view style="padding:12px 12px 0;display:flex;flex-direction:column;gap:8px;">
      <view
        v-for="item in fixedEntries"
        :key="item.key"
        style="border-radius:12px;overflow:hidden;background:#FFFFFF;box-shadow:0 2px 16px rgba(220,38,38,0.06);"
      >
        <view
          style="display:flex;align-items:center;justify-content:space-between;padding:14px 16px;"
          :style="{ borderBottom: expandedItem === item.key ? '1px solid #F3F4F6' : 'none' }"
          @click="toggleExpand(item.key)"
        >
          <view style="display:flex;align-items:center;gap:10px;">
            <text style="font-size:18px;">{{ item.icon }}</text>
            <text style="font-size:15px;font-weight:500;color:#1F2937;">{{ item.label }}</text>
          </view>
          <text style="font-size:16px;color:#D1D5DB;">{{ expandedItem === item.key ? '▼' : '›' }}</text>
        </view>
        <view v-if="expandedItem === item.key" style="padding:14px 16px;font-size:14px;color:#4B5563;line-height:1.8;background:#FAFAFA;">
          <text v-if="item.key === 'complaint'">{{ complaintText }}</text>
          <text v-else-if="item.key === 'logout'">{{ logoutText }}</text>
          <text v-else style="font-size:18px;font-weight:700;color:#1F2937;">{{ hotlineNumber }}</text>
        </view>
      </view>
    </view>

    <!-- 底部安全距离 -->
    <view style="height:24px;" />

    <!-- ============================================================
         视图二：只读文档详情
         ============================================================ -->
    <view v-if="currentView === 'readonly-doc'" style="position:fixed;inset:0;z-index:100;background:#FFFFFF;display:flex;flex-direction:column;">
      <view style="display:flex;align-items:center;padding:12px;border-bottom:1px solid #F3F4F6;gap:8px;">
        <view style="width:32px;height:32px;display:flex;align-items:center;justify-content:center;flex-shrink:0;" @click="goBack">
          <text style="font-size:18px;color:#450A0A;">←</text>
        </view>
        <text style="font-size:16px;font-weight:600;flex:1;text-align:center;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;color:#450A0A;">{{ docTitle }}</text>
        <view style="width:32px;height:32px;flex-shrink:0;" />
      </view>

      <view v-if="docLoading" style="display:flex;justify-content:center;padding-top:128px;">
        <text style="color:#9CA3AF;">加载中...</text>
      </view>
      <scroll-view v-else-if="readonlyContent" scroll-y style="flex:1;padding:0 16px;">
        <rich-text :nodes="readonlyContent" style="font-size:15px;color:#450A0A;line-height:1.8;" />
      </scroll-view>
      <view v-else style="display:flex;flex-direction:column;align-items:center;justify-content:center;padding-top:128px;">
        <view style="width:96px;height:96px;border-radius:50%;display:flex;align-items:center;justify-content:center;margin-bottom:20px;background:#FEF2F2;">
          <text style="font-size:40px;">📄</text>
        </view>
        <text style="font-size:18px;font-weight:600;color:#450A0A;">暂无内容</text>
      </view>
    </view>

  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { privacyApi } from '@/api'
import type { DocType } from '@/api/modules/privacy'

// ------------------------------------------------------------------
// 视图
// ------------------------------------------------------------------
const currentView = ref('main')
const viewStack = ref<string[]>(['main'])

function pushView(view: string): void {
  viewStack.value.push(view)
  currentView.value = view
}
function goBack(): void {
  if (viewStack.value.length > 1) {
    viewStack.value.pop()
    currentView.value = viewStack.value[viewStack.value.length - 1]
  } else {
    uni.navigateBack()
  }
}

// ------------------------------------------------------------------
// 文档入口（从后端获取）
// ------------------------------------------------------------------
const docEntries = [
  { key: 'INFO_COLLECTION', icon: '🗂️', label: '个人信息收集清单' },
  { key: 'THIRD_PARTY_SHARING', icon: '📤', label: '第三方共享个人信息清单' },
]

// ------------------------------------------------------------------
// 固定入口
// ------------------------------------------------------------------
const fixedEntries = [
  { key: 'complaint', icon: '📝', label: '投诉与举报' },
  { key: 'logout', icon: '🚫', label: '用户注销' },
  { key: 'hotline', icon: '📞', label: '客服热线' },
]

const complaintText =
  '如您有任何关于网络安全、数据安全、个人信息保护；帐号信息管理；网络信息内容；与钱大妈提供的移动互联网应用程序信息服务、推荐服务、互联网弹窗信息推送服务等有关的或其他任何方面的投诉、举报信息，均可以联系在线客服进行反馈或者拨打客服热线（400-061-2888）进行反馈。'
const logoutText =
  '如您希望注销账号的，您可以通过企业微信联系【供应链IT客服】进行申请，我们会将您的申请进行审核，符合条件的，我们会及时为您注销账号，注销后，您将无法继续登录使用本产品，请谨慎申请。'
const hotlineNumber = '400-0612-888'

// ------------------------------------------------------------------
// 展开/收起
// ------------------------------------------------------------------
const expandedItem = ref('')

function toggleExpand(key: string): void {
  expandedItem.value = expandedItem.value === key ? '' : key
}

// ------------------------------------------------------------------
// 文档详情
// ------------------------------------------------------------------
const docTitle = ref('')
const readonlyContent = ref('')
const docLoading = ref(false)

async function goToDoc(key: string, label: string): Promise<void> {
  docTitle.value = label
  docLoading.value = true
  pushView('readonly-doc')

  try {
    const res = await privacyApi.getDocByType(key as DocType)
    if (res.code === 0 || res.code === 200) {
      readonlyContent.value = res.data?.richContent ?? ''
    } else {
      readonlyContent.value = ''
    }
  } catch {
    readonlyContent.value = ''
  } finally {
    docLoading.value = false
  }
}
</script>

<style lang="scss" scoped>
/* 全部使用内联样式 */
</style>
