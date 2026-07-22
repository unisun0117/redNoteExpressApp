<template>
  <div class="tw-space-y-3">
    <div class="tw-bg-white tw-rounded-[--border-radius-card] tw-px-5 tw-py-3 tw-shadow-[--shadow-card] tw-flex tw-items-center tw-gap-3">
      <el-button text @click="goBack">← 返回</el-button>
      <span class="tw-text-base tw-font-semibold tw-text-[--text-color]">{{ pageTitle }}</span>
    </div>
    <div class="tw-bg-white tw-rounded-[--border-radius-card] tw-px-6 tw-py-6 tw-shadow-[--shadow-card]">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" :disabled="isReadonly" style="max-width: 900px">
        <el-form-item label="文档类型" prop="docType">
          <el-select v-model="form.docType" placeholder="请选择文档类型" :disabled="isEdit" style="width: 100%">
            <el-option v-for="opt in DOC_TYPE_OPTIONS" :key="opt.code" :label="opt.label" :value="opt.code" />
          </el-select>
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="版本号" prop="version">
              <el-input v-model="form.version" placeholder="如 v1.0.0" maxlength="50" :disabled="isEdit" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label-width="120px">
              <template #label>
                <span>
                  <span v-if="fileRequired" class="tw-text-red-500 tw-mr-0.5">*</span>附件(PDF)
                </span>
              </template>
              <div class="tw-w-full">
                <el-upload ref="uploadRef" :auto-upload="false" :limit="1" :file-list="fileList" accept=".pdf" @change="onFileChange" @remove="onFileRemove">
                  <el-button size="small" type="primary" plain :disabled="isReadonly">选择文件</el-button>
                </el-upload>
                <span class="tw-text-xs tw-text-[--text-secondary]">仅限PDF格式，≤10MB</span>
              </div>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="URL地址" prop="h5Url">
          <el-input v-model="form.h5Url" placeholder="请输入 H5 地址，以 http:// 或 https:// 开头" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="选填，最多200字" maxlength="200" show-word-limit />
        </el-form-item>
        <el-form-item label="富文本内容" prop="richContent">
          <div class="tw-w-full tw-border tw-border-gray-300 tw-rounded">
            <div v-if="editor" class="tw-flex tw-gap-px tw-p-2 tw-bg-gray-50 tw-border-b tw-border-gray-200 tw-flex-wrap">
              <el-button size="small" @click="editor.chain().focus().toggleBold().run()" :type="editor.isActive('bold') ? 'primary' : 'default'"><b>B</b></el-button>
              <el-button size="small" @click="editor.chain().focus().toggleItalic().run()" :type="editor.isActive('italic') ? 'primary' : 'default'"><i>I</i></el-button>
              <el-button size="small" @click="editor.chain().focus().toggleUnderline().run()" :type="editor.isActive('underline') ? 'primary' : 'default'"><u>U</u></el-button>
              <el-divider direction="vertical" />
              <el-button size="small" @click="editor.chain().focus().toggleHeading({ level: 1 }).run()" :type="editor.isActive('heading', { level: 1 }) ? 'primary' : 'default'">H1</el-button>
              <el-button size="small" @click="editor.chain().focus().toggleHeading({ level: 2 }).run()" :type="editor.isActive('heading', { level: 2 }) ? 'primary' : 'default'">H2</el-button>
              <el-button size="small" @click="editor.chain().focus().toggleHeading({ level: 3 }).run()" :type="editor.isActive('heading', { level: 3 }) ? 'primary' : 'default'">H3</el-button>
              <el-divider direction="vertical" />
              <el-button size="small" @click="editor.chain().focus().toggleBulletList().run()" :type="editor.isActive('bulletList') ? 'primary' : 'default'">•</el-button>
              <el-button size="small" @click="editor.chain().focus().toggleOrderedList().run()" :type="editor.isActive('orderedList') ? 'primary' : 'default'">1.</el-button>
              <el-divider direction="vertical" />
              <el-button size="small" @click="setLink">链接</el-button>
              <el-button size="small" @click="editor.chain().focus().toggleBlockquote().run()" :type="editor.isActive('blockquote') ? 'primary' : 'default'">引用</el-button>
              <el-button size="small" @click="editor.chain().focus().clearNodes().unsetAllMarks().run()">清除格式</el-button>
            </div>
            <editor-content :editor="editor" class="tw-min-h-[400px] tw-p-4 tw-text-sm tw-leading-relaxed" />
          </div>
        </el-form-item>
      </el-form>
      <div class="tw-flex tw-gap-3 tw-justify-end tw-mt-6" style="max-width: 900px">
        <el-button @click="goBack">取消</el-button>
        <el-button v-if="!isReadonly" type="primary" :loading="submitting" @click="submitForm">确定</el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useEditor, EditorContent } from '@tiptap/vue-3'
import StarterKit from '@tiptap/starter-kit'
import Underline from '@tiptap/extension-underline'
import Link from '@tiptap/extension-link'
import Placeholder from '@tiptap/extension-placeholder'
import type { FormInstance, FormRules, UploadFile, UploadRawFile } from 'element-plus'
import {
  getPrivacyDoc,
  createPrivacyDoc,
  updatePrivacyDoc,
  DOC_TYPE_OPTIONS,
} from '@/api/modules/privacy-policy'
import type { DocTypeCode, PrivacyDocCreationDTO, PrivacyDocEditDTO } from '@/types/privacy-policy'

const router = useRouter()
const route = useRoute()

const editId = route.params.id ? Number(route.params.id) : null
const isEdit = !!editId
const isReadonly = ref(false)
const pageTitle = computed(() => {
  if (isReadonly.value) return '查看文档'
  return isEdit ? '编辑文档' : '新增文档'
})

const submitting = ref(false)
const formRef = ref<FormInstance | null>(null)
const fileList = ref<UploadFile[]>([])
const uploadedFile = ref<File | null>(null)

const form = reactive<{
  docType: DocTypeCode | ''
  version: string
  h5Url: string
  remark: string
  richContent: string
}>({
  docType: '',
  version: '',
  h5Url: '',
  remark: '',
  richContent: '',
})

const fileRequired = computed(() => form.docType === 'PRIVACY_POLICY')

const rules: FormRules = {
  docType: [{ required: true, message: '请选择文档类型', trigger: 'change' }],
  version: [{ required: true, message: '请输入版本号', trigger: 'blur' }],
  h5Url: [
    { required: true, message: '请输入 URL 地址', trigger: 'blur' },
    { pattern: /^https?:\/\/.+/, message: '请输入合法的 http/https 链接', trigger: 'blur' },
  ],
  richContent: [{ required: true, message: '请输入富文本内容', trigger: 'blur' }],
}

const editor = useEditor({
  extensions: [
    StarterKit.configure({ link: false }),
    Underline,
    Link.configure({ openOnClick: false }),
    Placeholder.configure({ placeholder: '请输入富文本内容...' }),
  ],
  content: '',
  editable: true,
  onUpdate: ({ editor: ed }) => {
    form.richContent = ed.getHTML()
  },
})

function onFileChange(file: UploadFile) {
  const raw = file.raw as UploadRawFile
  if (!raw) return
  if (raw.type !== 'application/pdf') {
    ElMessage.warning('仅支持 PDF 格式')
    fileList.value = []
    return
  }
  if (raw.size > 10 * 1024 * 1024) {
    ElMessage.warning('文件大小不能超过 10MB')
    fileList.value = []
    return
  }
  uploadedFile.value = raw
  fileList.value = [file]
}

function onFileRemove() {
  uploadedFile.value = null
  fileList.value = []
}

function setLink() {
  if (!editor.value) return
  const previousUrl = editor.value.getAttributes('link').href
  const url = window.prompt('请输入链接地址：', previousUrl || 'https://')
  if (url === null) return
  if (url === '') {
    editor.value.chain().focus().extendMarkRange('link').unsetLink().run()
  } else {
    editor.value.chain().focus().extendMarkRange('link').setLink({ href: url }).run()
  }
}

onMounted(async () => {
  if (!isEdit || !editId) return
  try {
    const res = await getPrivacyDoc(editId)
    if (res.code !== 0 || !res.data) {
      ElMessage.error('加载文档失败')
      goBack()
      return
    }
    const doc = res.data
    form.docType = doc.docType
    form.version = doc.version
    form.h5Url = doc.h5Url || ''
    form.remark = doc.remark || ''
    form.richContent = doc.richContent || ''
    if (doc.fileUrl) {
      fileList.value = [{ name: `${doc.docTypeName}_${doc.version}.pdf`, url: doc.fileUrl } as UploadFile]
    }
    if (doc.status !== 'UNPUBLISHED') {
      isReadonly.value = true
      editor.value?.setEditable(false)
    }
    nextTick(() => {
      editor.value?.commands.setContent(doc.richContent || '')
    })
  } catch {
    ElMessage.error('加载文档失败')
    goBack()
  }
})

onBeforeUnmount(() => {
  editor.value?.destroy()
})

async function submitForm() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    if (fileRequired.value && !uploadedFile.value && !(isEdit && fileList.value.length > 0)) {
      ElMessage.warning('隐私政策类型必须上传 PDF 文件')
      return
    }
    submitting.value = true
    try {
      if (isEdit && editId) {
        const data: PrivacyDocEditDTO = {
          version: form.version,
          h5Url: form.h5Url,
          remark: form.remark,
          richContent: form.richContent,
          fileUrl: fileList.value.length > 0 ? fileList.value[0]?.url : undefined,
        }
        const res = await updatePrivacyDoc(editId, data, uploadedFile.value || undefined)
        if (res.code === 0) ElMessage.success('保存成功')
      } else {
        const data: PrivacyDocCreationDTO = {
          docType: form.docType as DocTypeCode,
          version: form.version,
          h5Url: form.h5Url,
          remark: form.remark,
          richContent: form.richContent,
        }
        const res = await createPrivacyDoc(data, uploadedFile.value || undefined)
        if (res.code === 0) ElMessage.success('新增成功')
      }
      goBack()
    } catch {
      // 拦截器已统一提示
    } finally {
      submitting.value = false
    }
  })
}

function goBack() {
  router.push({ name: 'PrivacyPolicy' })
}
</script>
