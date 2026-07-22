<script setup lang="ts">
import { ref, reactive, computed, nextTick } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { createMenu, updateMenu } from '@/api/modules/menu'
import type { MenuTreeNode, MenuSaveDTO } from '@/types/menu'

interface Props {
  /** 弹窗类型：MENU 菜单 / PAGE 页面 */
  type: 'MENU' | 'PAGE'
  /** 弹窗显示状态 */
  visible: boolean
  /** 所属分组 */
  group: 'WEB' | 'WECOM'
  /** 父菜单名称（仅页面弹窗需要展示） */
  parentMenuName?: string
}

const props = withDefaults(defineProps<Props>(), {
  parentMenuName: '',
})

const emit = defineEmits<{
  'update:visible': [value: boolean]
  /** 提交成功后触发，通知父组件刷新数据 */
  success: []
}>()

const formRef = ref<FormInstance>()
const submitting = ref(false)
const isEdit = ref(false)
const editId = ref<number | null>(null)
const form = reactive<MenuSaveDTO>({
  parentId: 0,
  code: '',
  name: '',
  type: props.type,
  group: props.group,
  path: '',
  component: '',
  icon: '',
  sort: 0,
  status: 'ACTIVE',
})

const typeLabel = computed(() => (props.type === 'MENU' ? '菜单' : '页面'))
const dialogTitle = computed(() =>
  isEdit.value ? `编辑${typeLabel.value}` : `新增${typeLabel.value}`,
)

// 校验规则（页面弹窗 path 必填，菜单弹窗不要求）
const codePattern = /^[A-Za-z0-9_]+$/
const formRules = computed<FormRules>(() => ({
  code: [
    { required: true, message: '编码不能为空', trigger: 'blur' },
    { pattern: codePattern, message: '仅允许字母、数字、下划线', trigger: 'blur' },
  ],
  name: [{ required: true, message: '名称不能为空', trigger: 'blur' }],
  ...(props.type === 'PAGE'
    ? { path: [{ required: true, message: '路由路径不能为空', trigger: 'blur' }] }
    : {}),
}))

/** 打开弹窗（供父组件通过 ref 调用） */
function open(data?: MenuTreeNode, parentId?: number) {
  isEdit.value = !!data
  editId.value = data?.id ?? null

  if (data) {
    Object.assign(form, {
      parentId: data.parentId,
      code: data.code,
      name: data.name,
      sort: data.sort,
      status: data.status,
      path: data.path,
      component: data.component,
      icon: data.icon,
      type: props.type,
      group: props.group,
    })
  } else {
    Object.assign(form, {
      parentId: parentId ?? 0,
      code: '',
      name: '',
      type: props.type,
      group: props.group,
      path: '',
      component: '',
      icon: '',
      sort: 0,
      status: 'ACTIVE',
    })
  }
  emit('update:visible', true)
  nextTick(() => formRef.value?.clearValidate())
}

async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate()
  submitting.value = true
  try {
    if (isEdit.value && editId.value) {
      await updateMenu({
        id: editId.value,
        name: form.name,
        path: form.path,
        component: form.component,
        icon: form.icon,
        sort: form.sort,
        status: form.status,
      })
      ElMessage.success(`${typeLabel.value}更新成功`)
    } else {
      await createMenu(form)
      ElMessage.success(`${typeLabel.value}创建成功`)
    }
    emit('update:visible', false)
    emit('success')
  } catch {
    /* 由拦截器处理 */
  } finally {
    submitting.value = false
  }
}

function handleClose() {
  emit('update:visible', false)
}

defineExpose({ open })
</script>

<template>
  <el-dialog
    :model-value="visible"
    :title="dialogTitle"
    width="540px"
    :close-on-click-modal="false"
    @update:model-value="emit('update:visible', $event)"
  >
    <!-- 页面弹窗展示父菜单信息 -->
    <div v-if="type === 'PAGE' && parentMenuName" class="tw-text-sm tw-text-gray-500 tw-mb-3">
      所属菜单：<el-tag size="small">{{ parentMenuName }}</el-tag>
    </div>

    <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px">
      <el-form-item :label="`${typeLabel}编码`" prop="code">
        <el-input
          v-model="form.code"
          :placeholder="`如 ${type === 'MENU' ? 'order_manage' : 'order_list'}`"
          maxlength="64"
        />
      </el-form-item>
      <el-form-item :label="`${typeLabel}名称`" prop="name">
        <el-input
          v-model="form.name"
          :placeholder="`如 ${type === 'MENU' ? '订单管理' : '订单列表'}`"
          maxlength="50"
        />
      </el-form-item>
      <el-form-item label="路由路径" prop="path">
        <el-input
          v-model="form.path"
          :placeholder="`如 ${type === 'MENU' ? '/order' : '/order/list'}`"
          maxlength="200"
        />
      </el-form-item>
      <el-form-item label="组件路径" prop="component">
        <el-input v-model="form.component" placeholder="如 system/order/index" maxlength="200" />
      </el-form-item>
      <el-form-item label="图标" prop="icon">
        <el-input v-model="form.icon" placeholder="图标类名" maxlength="64" />
      </el-form-item>
      <el-form-item label="排序" prop="sort">
        <div class="tw-flex tw-items-center tw-gap-0">
          <el-button
            size="small"
            style="min-width: 28px; padding: 2px 6px"
            @click="form.sort = Math.max(0, (form.sort || 0) - 1)"
            >-</el-button
          >
          <el-input
            v-model.number="form.sort"
            size="small"
            style="width: 60px; text-align: center"
            input-style="text-align:center"
          />
          <el-button
            size="small"
            style="min-width: 28px; padding: 2px 6px"
            @click="form.sort = (form.sort || 0) + 1"
            >+</el-button
          >
        </div>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="form.status" style="width: 120px">
          <el-option label="启用" value="ACTIVE" />
          <el-option label="停用" value="INACTIVE" />
        </el-select>
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="handleSubmit">
        {{ isEdit ? '保存修改' : '确认创建' }}
      </el-button>
    </template>
  </el-dialog>
</template>
