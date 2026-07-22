<template>
  <el-upload
    ref="uploadRef"
    list-type="picture-card"
    :file-list="innerFileList"
    :auto-upload="false"
    :limit="limit"
    :accept="accept"
    :disabled="disabled"
    :on-change="handleChange"
    :on-remove="handleRemove"
  >
    <el-icon><Plus /></el-icon>
  </el-upload>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { ossUploadImage } from '@/utils/oss'
import type { UploadFile, UploadFiles, UploadInstance, UploadRawFile } from 'element-plus'

// ===== 压缩选项 =====

export interface CompressOptions {
  /** 是否启用压缩，默认 false */
  enabled?: boolean
  /** 最大宽度（px），超出等比缩放 */
  maxWidth?: number
  /** 最大高度（px），超出等比缩放 */
  maxHeight?: number
  /** JPEG 质量 0-1，默认 0.8 */
  quality?: number
}

// ===== Props =====

const props = withDefaults(
  defineProps<{
    /** 双向绑定：单选为 string，多选为 string[] */
    modelValue?: string | string[]
    /** 最大上传数量，默认 1（单选） */
    limit?: number
    /** 接受的文件类型 */
    accept?: string
    /** 最大文件大小（MB），0 表示不限制 */
    maxSize?: number
    /** 禁用上传 */
    disabled?: boolean
    /** 上传子目录 */
    dir?: string
    /** 压缩配置 */
    compress?: CompressOptions
    /** STS 上传策略，默认 CUSTOMER_ARCHIVE */
    policy?: 'AVATARS' | 'PRODUCTS' | 'CUSTOMER_ARCHIVE'
  }>(),
  {
    modelValue: '',
    limit: 1,
    accept: 'image/jpeg,image/png',
    maxSize: 5,
    disabled: false,
    dir: 'customer-archive',
    compress: () => ({ enabled: false, maxWidth: 1920, maxHeight: 1920, quality: 0.8 }),
    policy: 'CUSTOMER_ARCHIVE',
  },
)

// ===== Emits =====

const emit = defineEmits<{
  (e: 'update:modelValue', url: string | string[]): void
}>()

// ===== 状态 =====

const uploadRef = ref<UploadInstance>()
const innerFileList = ref<UploadFile[]>([])
let _syncPending = true

/** 外部 URL → 回显为缩略图 */
function syncFromModel() {
  const val = props.modelValue
  if (!val) {
    innerFileList.value = []
    return
  }
  const list = Array.isArray(val) ? val : [val]
  const valid = list.filter(Boolean)
  if (valid.length === 0) {
    innerFileList.value = []
    return
  }
  innerFileList.value = valid.map(
    (url) =>
      ({
        name: url.split('/').pop() || 'image',
        url,
        status: 'success',
      }) as UploadFile,
  )
}

watch(
  () => props.modelValue,
  () => {
    if (_syncPending) {
      syncFromModel()
      _syncPending = false
    }
  },
  { immediate: true },
)

// ===== 图片压缩（Canvas） =====

function compressImage(file: File, opts: CompressOptions): Promise<File> {
  const { maxWidth = 1920, maxHeight = 1920, quality = 0.8 } = opts
  return new Promise((resolve) => {
    // 非图片文件不压缩
    if (!file.type.startsWith('image/')) {
      resolve(file)
      return
    }

    const img = new Image()
    const url = URL.createObjectURL(file)
    img.onload = () => {
      URL.revokeObjectURL(url)
      let { width, height } = img
      // 等比缩放
      if (width > maxWidth) {
        height = Math.round((height * maxWidth) / width)
        width = maxWidth
      }
      if (height > maxHeight) {
        width = Math.round((width * maxHeight) / height)
        height = maxHeight
      }
      // 无需压缩则直接返回原文件
      if (width >= img.width && height >= img.height) {
        resolve(file)
        return
      }
      const canvas = document.createElement('canvas')
      canvas.width = width
      canvas.height = height
      const ctx = canvas.getContext('2d')
      if (!ctx) {
        resolve(file)
        return
      }
      ctx.drawImage(img, 0, 0, width, height)
      canvas.toBlob(
        (blob) => {
          if (!blob) {
            resolve(file)
            return
          }
          resolve(new File([blob], file.name, { type: file.type, lastModified: Date.now() }))
        },
        file.type,
        quality,
      )
    }
    img.onerror = () => {
      URL.revokeObjectURL(url)
      resolve(file) // 压缩失败，降级用原文件
    }
    img.src = url
  })
}

// ===== 选择文件时校验（auto-upload=false 下不再触发 before-upload） =====

function validateRaw(raw: UploadRawFile): boolean {
  // 类型校验
  if (props.accept) {
    const allowed = props.accept.split(',').map((s) => s.trim())
    const match = allowed.some((a) => {
      if (a.endsWith('/*')) return raw.type.startsWith(a.replace('/*', '/'))
      return raw.type === a
    })
    if (!match) {
      ElMessage.warning(`仅支持 ${props.accept} 格式`)
      return false
    }
  }
  // 大小校验
  if (props.maxSize > 0 && raw.size / 1024 / 1024 > props.maxSize) {
    ElMessage.warning(`文件大小不能超过 ${props.maxSize}MB`)
    return false
  }
  return true
}

/**
 * 选择文件回调：仅做本地校验 + 本地预览，暂不上传 OSS。
 * el-upload 会自动为图片生成 blob 预览 url（file.url），并挂载原始文件 file.raw。
 */
function handleChange(file: UploadFile, files: UploadFiles) {
  // 同步 el-upload 内部列表到本地
  innerFileList.value = files
  // 仅处理新加入（ready）的文件
  if (file.status === 'ready' && file.raw && !validateRaw(file.raw)) {
    // 校验不通过则移除该文件（触发 handleRemove 重新同步 + emit）
    uploadRef.value?.handleRemove(file)
    return
  }
  // emit 本地预览 url，使表单 required(trigger: change) 校验通过并显示缩略图
  emitValue()
}

// ===== 提交时统一上传 OSS（由父组件在提交前 await 调用） =====

async function submit(): Promise<string | string[]> {
  for (const f of innerFileList.value) {
    // 已是 OSS url（回显或已传）跳过；仅上传本地暂存的 raw 文件
    if (!f.raw || f.status === 'success') continue
    f.status = 'uploading'
    let file: File = f.raw
    if (props.compress?.enabled) {
      file = await compressImage(f.raw, props.compress)
    }
    try {
      const result = await ossUploadImage(file, {
        dir: props.dir,
        policy: props.policy,
        onProgress: (p: number) => {
          f.percentage = p
        },
      })
      f.url = result.url
      f.status = 'success'
      f.raw = undefined
    } catch (err) {
      f.status = 'fail'
      ElMessage.error(`${f.name} 上传失败，请重试`)
      throw err
    }
  }
  return emitValue()
}

// ===== 删除 =====

function handleRemove(_file: UploadFile, files: UploadFiles) {
  innerFileList.value = files
  emitValue()
}

// ===== emit 当前 URL 列表（本地预览 blob 或 OSS url） =====

function emitValue(): string | string[] {
  const urls = innerFileList.value.filter((f) => f.url).map((f) => f.url as string)
  const value: string | string[] = props.limit === 1 ? urls[0] || '' : urls
  emit('update:modelValue', value)
  return value
}

/** 外部调用：syncFromModel 手动回显；submit 提交时上传 */
defineExpose({ syncFromModel, submit })
</script>
