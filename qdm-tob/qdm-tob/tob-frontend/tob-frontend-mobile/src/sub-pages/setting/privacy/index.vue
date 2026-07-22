<template>
  <view style="min-height:100vh;background:linear-gradient(180deg,#FEF2F2 0%,#FFFFFF 40%);">

    <!-- ============================================================
         个人信息
         ============================================================ -->
    <view style="padding:12px 12px 0;">
      <view style="border-radius:12px;overflow:hidden;background:#FFFFFF;box-shadow:0 2px 16px rgba(220,38,38,0.06);">
        <view style="display:flex;align-items:center;justify-content:space-between;padding:14px 16px;" @click="goPersonalInfo">
          <view style="display:flex;align-items:center;gap:10px;">
            <text style="font-size:18px;">👤</text>
            <text style="font-size:15px;font-weight:500;color:#1F2937;">个人信息</text>
          </view>
          <text style="font-size:16px;color:#D1D5DB;">›</text>
        </view>
      </view>
    </view>

    <!-- ============================================================
         相机/相册授权
         ============================================================ -->
    <view style="padding:12px 12px 0;">
      <view style="border-radius:12px;overflow:hidden;background:#FFFFFF;box-shadow:0 2px 16px rgba(220,38,38,0.06);">
        <view style="padding:12px 16px 6px;">
          <text style="font-size:13px;color:#9CA3AF;">📷 相机/相册授权</text>
        </view>
        <view style="display:flex;align-items:center;justify-content:space-between;padding:10px 16px;border-bottom:1px solid #F8FAFC;">
          <text style="font-size:15px;color:#374151;">相机</text>
          <switch :checked="authCamera" color="#DC2626" style="transform:scale(0.84);" @change="onAuthCameraChange" />
        </view>
        <view style="display:flex;align-items:center;justify-content:space-between;padding:10px 16px;">
          <text style="font-size:15px;color:#374151;">相册</text>
          <switch :checked="authAlbum" color="#DC2626" style="transform:scale(0.84);" @change="onAuthAlbumChange" />
        </view>
      </view>
    </view>

    <!-- ============================================================
         隐私说明（从后端拉取已发布文档）
         ============================================================ -->
    <view style="padding:12px 12px 0;">
      <view style="border-radius:12px;overflow:hidden;background:#FFFFFF;box-shadow:0 2px 16px rgba(220,38,38,0.06);">
        <view style="padding:12px 16px 6px;">
          <text style="font-size:13px;color:#9CA3AF;">📄 隐私说明</text>
        </view>

        <!-- 加载中 -->
        <view v-if="loading" style="padding:8px 16px 16px;">
          <view v-for="i in 3" :key="'sk-'+i" style="display:flex;align-items:center;padding:10px 0;gap:12px;" :style="{borderBottom:i<3?'1px solid #F8FAFC':'none'}">
            <view style="width:40px;height:40px;border-radius:50%;background:#F3F4F6;flex-shrink:0;" />
            <view style="flex:1;">
              <view style="height:16px;width:55%;background:#F3F4F6;border-radius:4px;margin-bottom:6px;" />
              <view style="height:12px;width:35%;background:#F3F4F6;border-radius:4px;" />
            </view>
          </view>
        </view>

        <!-- 文档列表 -->
        <view v-else>
          <view
            v-for="(doc, idx) in docList"
            :key="doc.id"
            style="display:flex;align-items:center;justify-content:space-between;padding:12px 16px;"
            :style="{ borderBottom: idx < docList.length - 1 ? '1px solid #F8FAFC' : 'none' }"
            @click="openDoc(doc)"
          >
            <text style="font-size:15px;color:#374151;">{{ docLabel(doc.docType) }}</text>
            <view style="display:flex;align-items:center;gap:6px;">
              <text style="font-size:12px;color:#9CA3AF;">v{{ doc.version }}</text>
              <text style="font-size:16px;color:#D1D5DB;">›</text>
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- 底部安全距离 -->
    <view style="height:24px;" />

    <!-- ============================================================
         视图二：文档详情（含版本选择器 — 仅隐私政策）
         ============================================================ -->
    <view v-if="currentView === 'detail'" style="position:fixed;inset:0;z-index:100;background:#FFFFFF;display:flex;flex-direction:column;">
      <view style="display:flex;align-items:center;padding:12px;border-bottom:1px solid #F3F4F6;gap:8px;">
        <view style="width:32px;height:32px;display:flex;align-items:center;justify-content:center;flex-shrink:0;" @click="goBack">
          <text style="font-size:18px;color:#450A0A;">←</text>
        </view>
        <text style="font-size:16px;font-weight:600;flex:1;text-align:center;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;color:#450A0A;">{{ detailTitle }}</text>
        <view style="width:32px;height:32px;flex-shrink:0;" />
      </view>

      <view v-if="isPrivacyPolicy" style="display:flex;align-items:center;justify-content:space-between;padding:8px 12px;gap:8px;">
        <view style="display:flex;align-items:center;gap:4px;background:#F3F4F6;padding:6px 12px;border-radius:8px;" @click="showVersionPicker = true">
          <text style="font-size:14px;font-weight:500;color:#1F2937;">{{ selectedVersionDate || selectedVersion }}</text>
          <text style="font-size:10px;color:#9CA3AF;">▼</text>
        </view>
        <view style="display:flex;gap:6px;">
          <button style="background:transparent;border:1px solid #DC2626;border-radius:8px;padding:6px 14px;font-size:13px;color:#DC2626;line-height:1.4;" size="mini" @click="copyDocUrl">复制地址</button>
          <button style="background:transparent;border:1px solid #D1D5DB;border-radius:8px;padding:6px 14px;font-size:13px;color:#374151;line-height:1.4;" size="mini" @click="downloadDoc">下载</button>
        </view>
      </view>

      <view v-if="detailLoading" style="display:flex;justify-content:center;padding-top:128px;">
        <text style="color:#9CA3AF;">加载中...</text>
      </view>
      <scroll-view v-else-if="detailContent" scroll-y style="flex:1;padding:0 16px;">
        <rich-text :nodes="detailContent" style="font-size:15px;color:#450A0A;line-height:1.8;" />
      </scroll-view>
      <view v-else style="display:flex;flex-direction:column;align-items:center;justify-content:center;padding-top:128px;">
        <view style="width:96px;height:96px;border-radius:50%;display:flex;align-items:center;justify-content:center;margin-bottom:20px;background:#FEF2F2;">
          <text style="font-size:40px;">📄</text>
        </view>
        <text style="font-size:18px;font-weight:600;color:#450A0A;">暂无内容</text>
      </view>

      <!-- 版本选择弹窗 -->
      <view v-if="showVersionPicker" style="position:fixed;inset:0;z-index:200;display:flex;align-items:flex-end;justify-content:center;">
        <view style="position:absolute;inset:0;background:rgba(0,0,0,0.4);" @click="showVersionPicker = false" />
        <view style="position:relative;background:#FFFFFF;border-radius:12px 12px 0 0;width:100%;padding-bottom:env(safe-area-inset-bottom);">
          <view style="display:flex;align-items:center;justify-content:space-between;padding:14px 16px;border-bottom:1px solid #F3F4F6;">
            <button style="background:transparent;border:none;font-size:15px;color:#9CA3AF;padding:0;" @click="showVersionPicker = false">取消</button>
            <text style="font-size:16px;font-weight:600;color:#450A0A;">选择版本日期</text>
            <button style="background:transparent;border:none;font-size:15px;color:#DC2626;padding:0;font-weight:600;" @click="onVersionConfirm">确定</button>
          </view>
          <picker-view :value="pickerIndex" style="width:100%;height:220px;" indicator-style="height:40px;" @change="onPickerChange">
            <picker-view-column>
              <view v-for="v in versionList" :key="v.id" style="display:flex;align-items:center;justify-content:center;height:40px;">
                <text style="font-size:16px;color:#1F2937;">{{ toDateLabel(v.updatedAt) }}</text>
              </view>
            </picker-view-column>
          </picker-view>
        </view>
      </view>
    </view>

    <!-- ============================================================
         视图三：个人信息详情
         ============================================================ -->
    <view v-if="currentView === 'personal-info'" style="position:fixed;inset:0;z-index:100;background:#FFFFFF;display:flex;flex-direction:column;">
      <view style="display:flex;align-items:center;padding:12px;border-bottom:1px solid #F3F4F6;gap:8px;">
        <view style="width:32px;height:32px;display:flex;align-items:center;justify-content:center;flex-shrink:0;" @click="goBack">
          <text style="font-size:18px;color:#450A0A;">←</text>
        </view>
        <text style="font-size:16px;font-weight:600;flex:1;text-align:center;color:#450A0A;">个人信息</text>
        <view style="width:32px;height:32px;flex-shrink:0;" />
      </view>

      <view style="padding:12px;">
        <view style="border-radius:12px;background:#FFFFFF;box-shadow:0 2px 16px rgba(220,38,38,0.06);padding:16px;margin-bottom:12px;">
          <text style="font-size:13px;color:#9CA3AF;display:block;margin-bottom:12px;">下载内容</text>
          <view style="display:flex;align-items:center;justify-content:space-between;padding:10px 0;border-bottom:1px solid #F8FAFC;">
            <text style="font-size:14px;color:#6B7280;">用户名</text>
            <text style="font-size:14px;color:#1F2937;">{{ userName }}</text>
          </view>
          <view style="display:flex;align-items:center;justify-content:space-between;padding:10px 0;border-bottom:1px solid #F8FAFC;">
            <text style="font-size:14px;color:#6B7280;">手机号码</text>
            <text style="font-size:14px;color:#1F2937;">{{ maskPhone(userPhone) }}</text>
          </view>
          <view style="display:flex;align-items:center;justify-content:space-between;padding:10px 0;">
            <text style="font-size:14px;color:#6B7280;">车牌号</text>
            <text style="font-size:14px;color:#1F2937;">{{ userPlateNo }}</text>
          </view>
        </view>

        <view style="border-radius:12px;background:#FFFFFF;box-shadow:0 2px 16px rgba(220,38,38,0.06);padding:16px;">
          <text style="font-size:13px;color:#9CA3AF;display:block;margin-bottom:12px;">下载设置</text>
          <view style="margin-bottom:12px;">
            <text style="font-size:14px;color:#374151;display:block;margin-bottom:6px;">邮箱地址</text>
            <input
              :value="emailInput"
              style="border:1px solid #E5E7EB;border-radius:8px;padding:14px 12px;font-size:14px;width:100%;box-sizing:border-box;background:#F9FAFB;min-height:48px;line-height:20px;"
              placeholder="请输入您的邮箱地址"
              type="text"
              @input="onEmailInput"
            />
          </view>
          <view style="background:#FEF3C7;border-radius:8px;padding:10px 14px;margin-bottom:16px;">
            <text style="font-size:13px;color:#92400E;line-height:1.6;">我们将在5个工作日内将文件以Excel格式发送至您的邮箱。</text>
          </view>
          <view style="display:flex;justify-content:center;">
            <button style="background:#DC2626;color:#FFFFFF;border:none;border-radius:999px;padding:12px 0;font-size:16px;font-weight:600;width:100%;" @click="submitDownload">提交申请</button>
          </view>
        </view>
      </view>
    </view>

  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { privacyApi } from '@/api'
import type { PrivacyDocSummary, DocType } from '@/api/modules/privacy'
import { useUserStore } from '@/store/modules/user'
import { getNameFromToken } from '@/utils/jwt'
import { env } from '@/utils/env'

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
// 个人信息（从 Pinia Store + JWT 获取）
// ------------------------------------------------------------------
const userStore = useUserStore()
const userName = computed(() => {
  const nm = userStore.userName || getNameFromToken(uni.getStorageSync('token') || '')
  return nm || '客户'
})
const userPhone = computed(() => userStore.userInfo?.phone || '')
const userPlateNo = ref('')

function maskPhone(phone: string): string {
  if (!phone || phone.length < 7) return phone
  return phone.slice(0, 3) + '****' + phone.slice(7)
}

function goPersonalInfo(): void {
  pushView('personal-info')
}

const emailInput = ref('')
function onEmailInput(e: { detail: { value: string } }): void {
  emailInput.value = e.detail.value
}
function submitDownload(): void {
  if (!emailInput.value.trim()) {
    uni.showToast({ title: '请输入邮箱地址', icon: 'none', duration: 2000 })
    return
  }
  const emailReg = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!emailReg.test(emailInput.value.trim())) {
    uni.showToast({ title: '请输入正确的邮箱地址', icon: 'none', duration: 2000 })
    return
  }
  // 提交下载申请（后端尚未实现，仅前端交互）
  privacyApi.submitAuthRecord('INFO_DOWNLOAD').catch(() => {})
  uni.showToast({ title: '申请已提交，我们将在5个工作日内将文件以Excel格式发送至您的邮箱', icon: 'none', duration: 3000 })
  emailInput.value = ''
}

// ------------------------------------------------------------------
// 相机/相册授权
// ------------------------------------------------------------------
const authCamera = ref(true)
const authAlbum = ref(true)

function onAuthCameraChange(e: { detail: { value: boolean } }): void {
  authCamera.value = e.detail.value
  if (e.detail.value) {
    privacyApi.submitAuthRecord('CAMERA').catch(() => {})
  }
  uni.showToast({ title: e.detail.value ? '相机授权已开启' : '相机授权已关闭', icon: 'none', duration: 1500 })
}
function onAuthAlbumChange(e: { detail: { value: boolean } }): void {
  authAlbum.value = e.detail.value
  if (e.detail.value) {
    privacyApi.submitAuthRecord('ALBUM').catch(() => {})
  }
  uni.showToast({ title: e.detail.value ? '相册授权已开启' : '相册授权已关闭', icon: 'none', duration: 1500 })
}

// ------------------------------------------------------------------
// 隐私文档列表 — 固定顺序，每种类型只取最新一条
// ------------------------------------------------------------------
const loading = ref(true)
const docList = ref<PrivacyDocSummary[]>([])

/** 固定展示顺序：隐私政策 → 摘要 → 用户规则 → 用户协议 */
const docOrder: { type: string; label: string }[] = [
  { type: 'PRIVACY_POLICY', label: '隐私政策' },
  { type: 'PRIVACY_SUMMARY', label: '隐私政策摘要' },
  { type: 'USER_RULES', label: '用户管理规则及公约' },
  { type: 'USER_AGREEMENT', label: '用户协议' },
]

function docLabel(type: string): string {
  return docOrder.find((d) => d.type === type)?.label ?? type
}

async function fetchDocList(): Promise<void> {
  loading.value = true
  try {
    const res = await privacyApi.getPublishedDocs()
    if (res.code === 0 || res.code === 200) {
      const all = res.data ?? []
      // 按固定顺序，每种类型只取最新一条（后端已按 createdAt 倒序）
      const ordered: PrivacyDocSummary[] = []
      for (const item of docOrder) {
        const match = all.find((d) => d.docType === item.type)
        if (match) ordered.push(match)
      }
      docList.value = ordered
    }
  } catch {
    // 拦截器已 toast
  } finally {
    loading.value = false
  }
}
fetchDocList()

// ------------------------------------------------------------------
// 文档详情
// ------------------------------------------------------------------
const detailTitle = ref('')
const detailContent = ref('')
const detailLoading = ref(false)
const isPrivacyPolicy = ref(false)
const versionList = ref<PrivacyDocSummary[]>([])
const selectedVersion = ref('')
const selectedVersionDate = ref('')
const showVersionPicker = ref(false)
const pickerIndex = ref([0])

/** 格式化日期用于版本选择器展示 */
function toDateLabel(dateStr: string): string {
  if (!dateStr) return ''
  return dateStr.slice(0, 10)
}

/** H5 doc types — these now open in web-view instead of in-app detail */
const H5_DOC_TYPES: string[] = ['PRIVACY_POLICY', 'PRIVACY_SUMMARY', 'USER_RULES', 'USER_AGREEMENT']

/** Map doc type → H5 static HTML file */
const H5_ROUTES: Record<string, string> = {
  PRIVACY_POLICY: '/privacy-policy.html',
  PRIVACY_SUMMARY: '/privacy-summary.html',
  USER_RULES: '/user-rules.html',
  USER_AGREEMENT: '/user-agreement.html',
}

function getH5Url(doc: PrivacyDocSummary): string {
  const base = env.h5BaseUrl || ''
  const route = H5_ROUTES[doc.docType] || ''
  const url = `${base}${route}`
  if (doc.docType === 'PRIVACY_POLICY' && doc.version) {
    return `${url}?version=${encodeURIComponent(doc.version)}`
  }
  return url
}

async function openDoc(doc: PrivacyDocSummary): Promise<void> {
  // 隐私说明下4个文档 → 跳转 web-view 打开 H5 页面
  if (H5_DOC_TYPES.includes(doc.docType)) {
    const url = getH5Url(doc)
    uni.navigateTo({
      url: `/sub-pages/setting/privacy/webview?url=${encodeURIComponent(url)}`,
    })
    return
  }

  // 个人信息收集清单 / 第三方共享个人信息清单 → 保持原有逻辑
  detailTitle.value = docLabel(doc.docType)
  isPrivacyPolicy.value = false
  detailLoading.value = true
  pushView('detail')

  try {
    const res = await privacyApi.getDocByType(doc.docType as DocType)
    if (res.code === 0 || res.code === 200) {
      detailContent.value = res.data?.richContent ?? ''
    } else {
      detailContent.value = ''
    }
  } catch {
    detailContent.value = ''
  } finally {
    detailLoading.value = false
  }
}

function onPickerChange(e: { detail: { value: number[] } }): void {
  pickerIndex.value = e.detail.value
}
async function onVersionConfirm(): Promise<void> {
  const idx = pickerIndex.value[0]
  if (idx === undefined || idx < 0 || idx >= versionList.value.length) {
    showVersionPicker.value = false
    return
  }
  const item = versionList.value[idx]
  const ver = item.version
  selectedVersion.value = ver
  selectedVersionDate.value = toDateLabel(item.updatedAt)
  showVersionPicker.value = false
  detailLoading.value = true
  try {
    const res = await privacyApi.getPrivacyByVersion(ver)
    if (res.code === 0 || res.code === 200) {
      detailContent.value = res.data?.richContent ?? ''
    }
  } catch {
    // ignore
  } finally {
    detailLoading.value = false
  }
}

function copyDocUrl(): void {
  uni.setClipboardData({
    data: `https://h5.qdama.cn/privacy/doc/${encodeURIComponent(selectedVersion.value)}`,
    success() { uni.showToast({ title: '复制成功', icon: 'none', duration: 1500 }) },
    fail() { uni.showToast({ title: '复制失败', icon: 'none', duration: 1500 }) },
  })
}
function downloadDoc(): void {
  uni.showToast({ title: '请复制链接到浏览器进行下载', icon: 'none', duration: 2000 })
}
</script>

<style lang="scss" scoped>
/* 全部使用内联样式 */
</style>
