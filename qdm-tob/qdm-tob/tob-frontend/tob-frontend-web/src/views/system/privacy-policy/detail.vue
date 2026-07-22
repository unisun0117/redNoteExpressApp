<template>
  <div class="tw-space-y-3">
    <div class="tw-bg-white tw-rounded-[--border-radius-card] tw-px-5 tw-py-3 tw-shadow-[--shadow-card] tw-flex tw-items-center tw-gap-3">
      <el-button text @click="goBack">← 返回</el-button>
      <span class="tw-text-base tw-font-semibold tw-text-[--text-color]">{{ doc?.docTypeName || '隐私政策详情' }}</span>
      <div class="tw-ml-auto">
        <el-button v-if="doc?.status === 'UNPUBLISHED'" type="primary" size="small" @click="goEdit">编辑</el-button>
      </div>
    </div>
    <div class="tw-bg-white tw-rounded-[--border-radius-card] tw-px-6 tw-py-6 tw-shadow-[--shadow-card]" style="max-width: 900px">
      <el-form label-width="100px" :disabled="true" v-if="doc">
        <el-form-item label="文档类型">
          <el-input :model-value="doc.docTypeName" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="版本号">
              <el-input :model-value="doc.version" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="附件">
              <el-tag v-if="doc.fileUrl" type="success" size="small">已上传 (PDF)</el-tag>
              <el-tag v-else type="info" size="small">未上传</el-tag>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="URL地址">
          <div class="tw-flex tw-gap-2 tw-w-full tw-items-center">
            <el-input :model-value="doc.h5Url" style="flex: 1" />
            <el-button size="small" @click="copyUrl">复制地址</el-button>
          </div>
        </el-form-item>
        <el-form-item label="备注">
          <el-input :model-value="doc.remark || '-'" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="状态">
          <el-tag :type="statusTagType(doc.status)" size="small" effect="plain">{{ doc.statusName }}</el-tag>
        </el-form-item>
        <el-form-item label="创建信息">
          <span class="tw-text-sm tw-text-[--text-secondary]">{{ doc.createdBy }} 创建于 {{ doc.createdAt }}</span>
        </el-form-item>
        <el-form-item label="更新信息">
          <span class="tw-text-sm tw-text-[--text-secondary]">{{ doc.updatedBy }} 最后更新于 {{ doc.updatedAt }}</span>
        </el-form-item>
        <el-form-item label="富文本内容">
          <div class="tw-w-full tw-border tw-border-gray-200 tw-rounded tw-p-4 tw-bg-gray-50 tw-min-h-[320px]">
            <div class="tw-text-sm tw-leading-relaxed tw-rich-content" v-html="doc.richContent || '<span class=\'tw-text-gray-400\'>暂无内容</span>'" />
          </div>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getPrivacyDoc, DOC_STATUS_TAG } from '@/api/modules/privacy-policy'
import type { PrivacyDocViewVO, DocStatusCode } from '@/types/privacy-policy'

const router = useRouter()
const route = useRoute()
const docId = Number(route.params.id)
const doc = ref<PrivacyDocViewVO | null>(null)

function statusTagType(status: DocStatusCode): 'success' | 'warning' | 'info' {
  return DOC_STATUS_TAG[status] || 'info'
}

onMounted(async () => {
  try {
    const res = await getPrivacyDoc(docId)
    if (res.code === 0 && res.data) {
      doc.value = res.data
    } else {
      ElMessage.error('加载文档失败')
      goBack()
    }
  } catch {
    ElMessage.error('加载文档失败')
    goBack()
  }
})

function copyUrl() {
  if (!doc.value?.h5Url) {
    ElMessage.warning('该文档暂无 H5 地址')
    return
  }
  navigator.clipboard.writeText(doc.value.h5Url).then(() => {
    ElMessage.success('地址已复制到剪贴板')
  }).catch(() => {
    ElMessage.error('复制失败，请手动复制')
  })
}

function goEdit() {
  router.push({ name: 'PrivacyPolicyEdit', params: { id: docId } })
}

function goBack() {
  router.push({ name: 'PrivacyPolicy' })
}
</script>

<style scoped>
.tw-rich-content h1, .tw-rich-content h2, .tw-rich-content h3 {
  margin: 16px 0 8px;
}
.tw-rich-content p {
  margin: 8px 0;
}
.tw-rich-content ul, .tw-rich-content ol {
  padding-left: 20px;
  margin: 8px 0;
}
</style>
