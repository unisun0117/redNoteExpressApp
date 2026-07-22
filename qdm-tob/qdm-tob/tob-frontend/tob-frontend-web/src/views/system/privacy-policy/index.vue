<template>
  <div class="tw-space-y-3">
    <el-tabs v-model="activeTab" class="privacy-tabs">
      <!-- ============================================================ -->
      <!-- Tab 1: 小程序隐私文档维护 -->
      <!-- ============================================================ -->
      <el-tab-pane label="小程序隐私文档维护" name="docs">
        <!-- 查询区 -->
        <div
          class="tw-bg-white tw-rounded-[--border-radius-card] tw-px-5 tw-pt-4 tw-pb-1 tw-shadow-[--shadow-card]"
        >
          <div class="tw-flex tw-flex-wrap tw-gap-3 tw-items-center">
            <el-select
              v-model="docSearch.docType"
              placeholder="全部类型"
              clearable
              style="width: 220px"
            >
              <el-option label="全部" value="" />
              <el-option
                v-for="opt in DOC_TYPE_OPTIONS"
                :key="opt.code"
                :label="opt.label"
                :value="opt.code"
              />
            </el-select>
            <el-input
              v-model="docSearch.version"
              placeholder="请输入版本号"
              clearable
              style="width: 200px"
            />
            <el-button type="primary" @click="doDocSearch">查询</el-button>
            <el-button type="primary" plain @click="goCreate">新增文档</el-button>
            <el-button @click="handleExportDocs">导出</el-button>
          </div>
        </div>

        <!-- 数据区 -->
        <div
          class="tw-mt-3 tw-bg-white tw-rounded-[--border-radius-card] tw-px-5 tw-pt-4 tw-pb-5 tw-shadow-[--shadow-card]"
        >
          <el-table
            v-loading="docLoading"
            :data="docTableData"
            stripe
            style="width: 100%"
            :header-cell-style="{
              background: '#F1F5F9',
              color: '#475569',
              fontWeight: 600,
              fontSize: '13px',
            }"
          >
            <el-table-column label="序号" min-width="60" align="center">
              <template #default="{ $index }">
                {{ (docPageNum - 1) * docPageSize + $index + 1 }}
              </template>
            </el-table-column>
            <el-table-column label="状态" min-width="90" align="center">
              <template #default="{ row }">
                <el-tag :type="statusTagType(row.status)" size="small" effect="plain">
                  {{ row.statusName }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="文档类型" min-width="180">
              <template #default="{ row }">
                {{ row.docTypeName }}
              </template>
            </el-table-column>
            <el-table-column
              prop="h5Url"
              label="H5地址"
              min-width="200"
              show-overflow-tooltip
            />
            <el-table-column prop="version" label="版本号" min-width="110" />
            <el-table-column
              prop="remark"
              label="备注"
              min-width="160"
              show-overflow-tooltip
            >
              <template #default="{ row }">
                {{ row.remark || '-' }}
              </template>
            </el-table-column>
            <el-table-column prop="createdBy" label="创建人" min-width="110" />
            <el-table-column prop="createdAt" label="创建时间" min-width="160" />
            <el-table-column prop="updatedBy" label="最后更新人" min-width="110" />
            <el-table-column prop="updatedAt" label="最后更新时间" min-width="160" />
            <el-table-column label="操作" min-width="240" fixed="right">
              <template #default="{ row }">
                <div class="tw-flex tw-gap-1 tw-flex-wrap">
                  <el-button type="primary" link size="small" @click="copyUrl(row.h5Url)">
                    复制地址
                  </el-button>
                  <el-button type="primary" link size="small" @click="goDetail(row.id)">
                    详情
                  </el-button>
                  <el-button
                    v-if="row.status === 'UNPUBLISHED'"
                    type="success"
                    link
                    size="small"
                    @click="handlePublish(row)"
                  >
                    发布
                  </el-button>
                  <el-button
                    v-if="row.status === 'UNPUBLISHED' || row.status === 'PUBLISHED'"
                    type="warning"
                    link
                    size="small"
                    @click="handleWithdraw(row)"
                  >
                    下架
                  </el-button>
                  <el-button
                    v-if="
                      row.status === 'PUBLISHED' &&
                      row.docType === 'PRIVACY_POLICY' &&
                      !!row.fileUrl
                    "
                    type="primary"
                    link
                    size="small"
                    @click="handleDownloadFile(row)"
                  >
                    下载文件
                  </el-button>
                </div>
              </template>
            </el-table-column>
            <template #empty>
              <div class="tw-py-16 tw-text-center tw-text-[--text-secondary]">
                <div class="tw-text-5xl tw-mb-3">📄</div>
                <div>暂无文档数据</div>
              </div>
            </template>
          </el-table>

          <div class="tw-mt-4 tw-flex">
            <el-pagination
              v-model:current-page="docPageNum"
              v-model:page-size="docPageSize"
              :page-sizes="[10, 20, 50]"
              :total="docTotal"
              layout="total, sizes, prev, pager, next"
              background
            />
          </div>
        </div>
      </el-tab-pane>

      <!-- ============================================================ -->
      <!-- Tab 2: 用户授权记录 -->
      <!-- ============================================================ -->
      <el-tab-pane label="用户授权记录" name="records">
        <!-- 查询区 -->
        <div
          class="tw-bg-white tw-rounded-[--border-radius-card] tw-px-5 tw-pt-4 tw-pb-1 tw-shadow-[--shadow-card]"
        >
          <div class="tw-flex tw-flex-wrap tw-gap-3 tw-items-center">
            <el-input
              v-model="recordSearch.openid"
              placeholder="请输入 openid"
              clearable
              style="width: 220px"
            />
            <el-input
              v-model="recordSearch.phone"
              placeholder="请输入手机号码"
              clearable
              style="width: 200px"
            />
            <el-select
              v-model="recordSearch.authType"
              placeholder="全部类型"
              clearable
              style="width: 200px"
            >
              <el-option label="全部" value="" />
              <el-option
                v-for="opt in AUTH_TYPE_OPTIONS"
                :key="opt.code"
                :label="opt.label"
                :value="opt.code"
              />
            </el-select>
            <el-button type="primary" @click="doRecordSearch">查询</el-button>
            <el-button @click="handleExportRecords">导出</el-button>
          </div>
        </div>

        <!-- 数据区 -->
        <div
          class="tw-mt-3 tw-bg-white tw-rounded-[--border-radius-card] tw-px-5 tw-pt-4 tw-pb-5 tw-shadow-[--shadow-card]"
        >
          <el-table
            v-loading="recordLoading"
            :data="recordTableData"
            stripe
            style="width: 100%"
            :header-cell-style="{
              background: '#F1F5F9',
              color: '#475569',
              fontWeight: 600,
              fontSize: '13px',
            }"
          >
            <el-table-column prop="openid" label="openid" min-width="220" show-overflow-tooltip />
            <el-table-column prop="phone" label="手机号码" min-width="140" />
            <el-table-column label="类型" min-width="160">
              <template #default="{ row }">
                {{ row.authTypeName }}
              </template>
            </el-table-column>
            <el-table-column prop="version" label="版本" min-width="120" />
            <el-table-column prop="authTime" label="授权时间" min-width="170" />
            <template #empty>
              <div class="tw-py-16 tw-text-center tw-text-[--text-secondary]">
                <div class="tw-text-5xl tw-mb-3">📋</div>
                <div>暂无授权记录</div>
              </div>
            </template>
          </el-table>

          <div class="tw-mt-4 tw-flex">
            <el-pagination
              v-model:current-page="recordPageNum"
              v-model:page-size="recordPageSize"
              :page-sizes="[10, 20, 50]"
              :total="recordTotal"
              layout="total, sizes, prev, pager, next"
              background
            />
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getPrivacyDocPage,
  publishPrivacyDoc,
  withdrawPrivacyDoc,
  exportDocs,
  downloadFile,
  getAuthRecordPage,
  exportAuthRecords,
  DOC_TYPE_OPTIONS,
  AUTH_TYPE_OPTIONS,
  DOC_STATUS_TAG,
} from '@/api/modules/privacy-policy'
import type {
  PrivacyDocSummaryVO,
  AuthRecordVO,
  DocStatusCode,
  DocTypeCode,
  AuthTypeCode,
} from '@/types/privacy-policy'

const router = useRouter()
const activeTab = ref<'docs' | 'records'>('docs')

function statusTagType(status: DocStatusCode): 'success' | 'warning' | 'info' {
  return DOC_STATUS_TAG[status] || 'info'
}

// ================================================================
// 通用：下载 Blob
// ================================================================
function saveBlob(res: unknown, filename: string) {
  const blob = new Blob([res as BlobPart])
  const url = window.URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = filename
  a.click()
  window.URL.revokeObjectURL(url)
}

// ================================================================
// Tab 1: 隐私文档维护
// ================================================================
const docLoading = ref(false)
const docTableData = ref<PrivacyDocSummaryVO[]>([])
const docTotal = ref(0)
const docPageNum = ref(1)
const docPageSize = ref(20)

const docSearch = reactive<{ docType: DocTypeCode | ''; version: string }>({
  docType: '',
  version: '',
})

async function loadDocs() {
  docLoading.value = true
  try {
    const res = await getPrivacyDocPage({
      docType: docSearch.docType || undefined,
      version: docSearch.version || undefined,
      pageNum: docPageNum.value,
      pageSize: docPageSize.value,
    })
    if (res.code === 0) {
      docTableData.value = res.data.records || []
      docTotal.value = res.data.total || 0
    }
  } catch {
    // 拦截器已统一提示
  } finally {
    docLoading.value = false
  }
}

function doDocSearch() {
  docPageNum.value = 1
  loadDocs()
}

watch([docPageNum, docPageSize], () => loadDocs())

function goCreate() {
  router.push({ name: 'PrivacyPolicyEdit' })
}

function goDetail(id: number) {
  router.push({ name: 'PrivacyPolicyDetail', params: { id } })
}

async function copyUrl(url: string) {
  if (!url) {
    ElMessage.warning('该文档暂无 H5 地址')
    return
  }
  try {
    await navigator.clipboard.writeText(url)
    ElMessage.success('地址已复制到剪贴板')
  } catch {
    ElMessage.error('复制失败，请手动复制')
  }
}

async function handlePublish(row: PrivacyDocSummaryVO) {
  try {
    await ElMessageBox.confirm(
      `确定发布【${row.docTypeName}】（版本 ${row.version}）吗？发布后该文档将对小程序用户生效。`,
      '发布确认',
      { type: 'warning', confirmButtonText: '确定发布', cancelButtonText: '取消' },
    )
  } catch {
    return
  }
  try {
    const res = await publishPrivacyDoc(row.id)
    if (res.code === 0) {
      ElMessage.success('发布成功')
      loadDocs()
    }
  } catch {
    // 拦截器已统一提示
  }
}

async function handleWithdraw(row: PrivacyDocSummaryVO) {
  try {
    await ElMessageBox.confirm(
      `确定下架【${row.docTypeName}】（版本 ${row.version}）吗？下架后该文档将不再对小程序用户展示。`,
      '下架确认',
      { type: 'warning', confirmButtonText: '确定下架', cancelButtonText: '取消' },
    )
  } catch {
    return
  }
  try {
    const res = await withdrawPrivacyDoc(row.id)
    if (res.code === 0) {
      ElMessage.success('下架成功')
      loadDocs()
    }
  } catch {
    // 拦截器已统一提示
  }
}

async function handleExportDocs() {
  try {
    const res = await exportDocs({
      docType: docSearch.docType || undefined,
      version: docSearch.version || undefined,
    })
    saveBlob(res, '小程序隐私文档.xlsx')
    ElMessage.success('导出成功')
  } catch {
    ElMessage.error('导出失败')
  }
}

async function handleDownloadFile(row: PrivacyDocSummaryVO) {
  try {
    const res = await downloadFile(row.id)
    saveBlob(res, `${row.docTypeName}_${row.version}.pdf`)
  } catch {
    ElMessage.error('文件下载失败')
  }
}

// ================================================================
// Tab 2: 用户授权记录
// ================================================================
const recordLoading = ref(false)
const recordTableData = ref<AuthRecordVO[]>([])
const recordTotal = ref(0)
const recordPageNum = ref(1)
const recordPageSize = ref(20)
const recordLoaded = ref(false)

const recordSearch = reactive<{ openid: string; phone: string; authType: AuthTypeCode | '' }>({
  openid: '',
  phone: '',
  authType: '',
})

async function loadRecords() {
  recordLoading.value = true
  try {
    const res = await getAuthRecordPage({
      openid: recordSearch.openid || undefined,
      phone: recordSearch.phone || undefined,
      authType: recordSearch.authType || undefined,
      pageNum: recordPageNum.value,
      pageSize: recordPageSize.value,
    })
    if (res.code === 0) {
      recordTableData.value = res.data.records || []
      recordTotal.value = res.data.total || 0
    }
  } catch {
    // 拦截器已统一提示
  } finally {
    recordLoading.value = false
  }
}

function doRecordSearch() {
  recordPageNum.value = 1
  loadRecords()
}

watch([recordPageNum, recordPageSize], () => {
  if (recordLoaded.value) loadRecords()
})

async function handleExportRecords() {
  try {
    const res = await exportAuthRecords({
      openid: recordSearch.openid || undefined,
      phone: recordSearch.phone || undefined,
      authType: recordSearch.authType || undefined,
    })
    saveBlob(res, '用户授权记录.xlsx')
    ElMessage.success('导出成功')
  } catch {
    ElMessage.error('导出失败')
  }
}

// 切换到授权记录 Tab 时懒加载
watch(activeTab, (tab) => {
  if (tab === 'records' && !recordLoaded.value) {
    recordLoaded.value = true
    loadRecords()
  }
})

// ================================================================
// 初始化
// ================================================================
loadDocs()
</script>

<style scoped>
.privacy-tabs :deep(.el-tabs__item) {
  font-size: 15px;
  font-weight: 600;
}
</style>
