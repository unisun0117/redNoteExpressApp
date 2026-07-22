<script setup lang="ts">
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import {
  getAdminTree,
  getButtons,
  getPermissionOptions,
  createMenu,
  updateMenu,
  deleteMenu,
} from '@/api/modules/menu'
import type { MenuTreeNode, PermissionOption } from '@/types/menu'
import MenuPageDialog from './components/MenuPageDialog.vue'

// 状态
const activeGroup = ref<'WEB' | 'WECOM'>('WEB')
const treeData = ref<MenuTreeNode[]>([])
const selectedPage = ref<MenuTreeNode | null>(null)
const buttonList = ref<MenuTreeNode[]>([])
const allPermissionOptions = ref<PermissionOption[]>([])

// 菜单/页面弹窗（统一组件）
const menuDialogRef = ref<InstanceType<typeof MenuPageDialog>>()
const pageDialogRef = ref<InstanceType<typeof MenuPageDialog>>()
const menuDialogVisible = ref(false)
const pageDialogVisible = ref(false)
const pageParentMenuName = ref('')

// 按钮弹窗
const btnDialogVisible = ref(false)
const btnDialogIsEdit = ref(false)
const btnEditId = ref<number | null>(null)
const btnFormRef = ref<FormInstance>()
const btnSubmitting = ref(false)
const btnForm = reactive({
  code: '',
  name: '',
  status: 'ACTIVE' as 'ACTIVE' | 'INACTIVE',
  permissionCodes: [] as string[],
})

/** 当前 hover 的树节点 ID，控制操作按钮显隐 */
const hoveredNodeId = ref<number | null>(null)

// 删除弹窗
const deleteDialogVisible = ref(false)
const deleteTarget = ref<MenuTreeNode | null>(null)
const deleteSubmitting = ref(false)

// 按钮弹窗校验规则
const codePattern = /^[A-Za-z0-9_]+$/
const btnFormRules: FormRules = {
  code: [
    { required: true, message: '编码不能为空', trigger: 'blur' },
    { pattern: codePattern, message: '仅允许字母、数字、下划线', trigger: 'blur' },
  ],
  name: [{ required: true, message: '名称不能为空', trigger: 'blur' }],
}

// 父菜单名
const parentMenuName = computed(() => {
  if (!selectedPage.value) return ''
  return findParentName(treeData.value, selectedPage.value.id)
})

function findParentName(nodes: MenuTreeNode[], targetId: number): string {
  for (const n of nodes) {
    if (n.children?.some((c) => c.id === targetId)) return n.name
    if (n.children) {
      const r = findParentName(n.children, targetId)
      if (r) return r
    }
  }
  return ''
}

function getPermName(code: string): string {
  return allPermissionOptions.value.find((p) => p.authority === code)?.authorityName ?? code
}

// 加载数据
onMounted(async () => {
  await loadTree()
  const res = await getPermissionOptions()
  allPermissionOptions.value = res
})

async function loadTree() {
  treeData.value = await getAdminTree(activeGroup.value)
  selectedPage.value = null
}

function onNodeClick(data: MenuTreeNode) {
  if (data.type === 'PAGE') {
    selectedPage.value = data
    loadButtons(data.id)
  }
}

async function loadButtons(pageId: number) {
  buttonList.value = await getButtons(pageId)
}

// 菜单弹窗
function openMenuDialog(data?: MenuTreeNode) {
  menuDialogRef.value?.open(data)
}

// 页面弹窗
function openPageDialog(data?: MenuTreeNode, parentMenu?: MenuTreeNode) {
  if (data) {
    pageParentMenuName.value = findParentName(treeData.value, data.id)
    pageDialogRef.value?.open(data)
  } else if (parentMenu) {
    pageParentMenuName.value = parentMenu.name
    pageDialogRef.value?.open(undefined, parentMenu.id)
  }
}

// 弹窗提交成功后刷新树
async function handleDialogSuccess() {
  await loadTree()
}

// 按钮弹窗
function openButtonDialog(data?: MenuTreeNode) {
  btnDialogIsEdit.value = !!data
  btnEditId.value = data?.id ?? null
  if (data) {
    btnForm.code = data.code
    btnForm.name = data.name
    btnForm.status = data.status
    btnForm.permissionCodes = [...(data.permissionCodes ?? [])]
  } else {
    btnForm.code = ''
    btnForm.name = ''
    btnForm.status = 'ACTIVE'
    btnForm.permissionCodes = []
  }
  btnDialogVisible.value = true
  nextTick(() => btnFormRef.value?.clearValidate())
}

async function submitBtn() {
  if (!btnFormRef.value) return
  await btnFormRef.value.validate()
  btnSubmitting.value = true
  try {
    if (btnDialogIsEdit.value && btnEditId.value) {
      await updateMenu({
        id: btnEditId.value,
        name: btnForm.name,
        status: btnForm.status,
        permissionCodes: btnForm.permissionCodes,
      })
      ElMessage.success('按钮更新成功')
    } else if (selectedPage.value) {
      await createMenu({
        parentId: selectedPage.value.id,
        code: btnForm.code,
        name: btnForm.name,
        type: 'BUTTON',
        group: activeGroup.value,
        sort: 0,
        status: btnForm.status,
        permissionCodes: btnForm.permissionCodes,
      })
      ElMessage.success('按钮创建成功')
    }
    btnDialogVisible.value = false
    if (selectedPage.value) await loadButtons(selectedPage.value.id)
  } catch {
    /* 由拦截器处理 */
  } finally {
    btnSubmitting.value = false
  }
}

// 删除
function confirmDelete(data: MenuTreeNode) {
  deleteTarget.value = data
  deleteDialogVisible.value = true
}

async function doDelete() {
  if (!deleteTarget.value) return
  deleteSubmitting.value = true
  try {
    await deleteMenu(deleteTarget.value.id)
    ElMessage.success('按钮删除成功')
    deleteDialogVisible.value = false
    if (selectedPage.value) await loadButtons(selectedPage.value.id)
  } catch {
    /* 由拦截器处理 */
  } finally {
    deleteSubmitting.value = false
  }
}
</script>

<template>
  <div class="tw-flex tw-gap-3 tw-h-full">
    <!-- 左侧菜单树 -->
    <div class="tw-w-[340px] tw-shrink-0 tw-bg-white tw-rounded-lg tw-p-3 tw-flex tw-flex-col">
      <div class="tw-flex tw-items-center tw-justify-between tw-mb-2 tw-gap-2">
        <el-select v-model="activeGroup" style="width: 130px" @change="loadTree">
          <el-option label="后台菜单" value="WEB" />
          <el-option label="企微菜单" value="WECOM" />
        </el-select>
        <el-button type="primary" size="small" @click="openMenuDialog()">新增菜单</el-button>
      </div>
      <div
        class="tw-flex-1 tw-overflow-y-auto tw-border tw-border-gray-200 tw-rounded tw-p-2 tw-bg-gray-50"
      >
        <div
          v-if="treeData.length === 0"
          class="tw-flex tw-flex-col tw-items-center tw-justify-center tw-h-full tw-text-gray-400"
        >
          <div style="font-size: 48px; margin-bottom: 8px">📁</div>
          <div>暂无菜单数据</div>
        </div>
        <el-tree
          v-else
          :data="treeData"
          node-key="id"
          default-expand-all
          :expand-on-click-node="false"
          highlight-current
          @node-click="onNodeClick"
        >
          <template #default="{ data }">
            <span
              class="tw-flex tw-items-center tw-w-full"
              @mouseenter="hoveredNodeId = data.id"
              @mouseleave="hoveredNodeId = null"
            >
              <span class="tw-mr-1">{{ data.type === 'MENU' ? '📁' : '📄' }}</span>
              <span
                class="tw-flex-1 tw-text-sm"
                :style="{ color: data.status === 'INACTIVE' ? '#c0c4cc' : '#303133' }"
                >{{ data.name }}</span
              >
              <el-tag
                :type="data.status === 'ACTIVE' ? 'success' : 'info'"
                size="small"
                class="tw-mr-2"
              >
                {{ data.status === 'ACTIVE' ? '启用' : '停用' }}
              </el-tag>
              <span v-show="hoveredNodeId === data.id" class="tw-inline-flex tw-items-center">
                <el-button
                  v-if="data.type === 'MENU'"
                  type="primary"
                  link
                  size="small"
                  @click.stop="openPageDialog(undefined, data)"
                  >+页面</el-button
                >
                <el-button
                  type="warning"
                  link
                  size="small"
                  @click.stop="data.type === 'MENU' ? openMenuDialog(data) : openPageDialog(data)"
                  >编辑</el-button
                >
              </span>
            </span>
          </template>
        </el-tree>
      </div>
    </div>

    <!-- 右侧按钮管理区 -->
    <div class="tw-flex-1 tw-bg-white tw-rounded-lg tw-p-3 tw-overflow-y-auto">
      <template v-if="selectedPage">
        <div class="tw-text-base tw-font-semibold tw-mb-3 tw-pb-2 tw-border-b">
          <span>{{ selectedPage.name }} — 按钮列表</span>
          <span class="tw-text-xs tw-text-gray-400 tw-font-normal tw-ml-2">
            所属菜单：{{ parentMenuName }}
          </span>
        </div>
        <div class="tw-mb-3">
          <el-button type="success" size="small" @click="openButtonDialog()">+ 新增按钮</el-button>
        </div>
        <el-table :data="buttonList" stripe style="width: 100%">
          <el-table-column prop="code" label="按钮编码" width="140" />
          <el-table-column prop="name" label="按钮名称" width="120" />
          <el-table-column label="权限组" min-width="180">
            <template #default="{ row }">
              <div class="tw-flex tw-flex-wrap tw-gap-1">
                <el-tag v-for="pg in row.permissionCodes" :key="pg" size="small">{{
                  getPermName(pg)
                }}</el-tag>
                <span v-if="!row.permissionCodes?.length" class="tw-text-gray-400">-</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="80" align="center">
            <template #default="{ row }">
              <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'info'" size="small">
                {{ row.status === 'ACTIVE' ? '启用' : '禁用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="130" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" link size="small" @click="openButtonDialog(row)"
                >编辑</el-button
              >
              <el-button type="danger" link size="small" @click="confirmDelete(row)"
                >删除</el-button
              >
            </template>
          </el-table-column>
        </el-table>
        <div v-if="buttonList.length === 0" class="tw-text-center tw-py-10 tw-text-gray-400">
          <div style="font-size: 32px; margin-bottom: 8px">🔘</div>
          <div>暂无按钮，点击"新增按钮"添加</div>
        </div>
      </template>
      <div
        v-else
        class="tw-flex tw-flex-col tw-items-center tw-justify-center tw-h-full tw-text-gray-400"
      >
        <div style="font-size: 56px; margin-bottom: 12px">📋</div>
        <div>请在左侧菜单树中选择一个页面</div>
      </div>
    </div>

    <!-- ===== 菜单弹窗（新增/编辑菜单） ===== -->
    <MenuPageDialog
      ref="menuDialogRef"
      v-model:visible="menuDialogVisible"
      type="MENU"
      :group="activeGroup"
      @success="handleDialogSuccess"
    />

    <!-- ===== 页面弹窗（新增/编辑页面） ===== -->
    <MenuPageDialog
      ref="pageDialogRef"
      v-model:visible="pageDialogVisible"
      type="PAGE"
      :group="activeGroup"
      :parent-menu-name="pageParentMenuName"
      @success="handleDialogSuccess"
    />

    <!-- ===== 按钮弹窗（新增/编辑按钮） ===== -->
    <el-dialog
      v-model="btnDialogVisible"
      :title="btnDialogIsEdit ? '编辑按钮' : '新增按钮'"
      width="480px"
      :close-on-click-modal="false"
    >
      <el-form ref="btnFormRef" :model="btnForm" :rules="btnFormRules" label-width="100px">
        <el-form-item label="按钮编码" prop="code">
          <el-input v-model="btnForm.code" placeholder="如 create" maxlength="64" />
        </el-form-item>
        <el-form-item label="按钮名称" prop="name">
          <el-input v-model="btnForm.name" placeholder="如 新增" maxlength="50" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="btnForm.status" style="width: 120px">
            <el-option label="启用" value="ACTIVE" />
            <el-option label="停用" value="INACTIVE" />
          </el-select>
        </el-form-item>
        <el-form-item label="权限组" prop="permissionCodes">
          <el-select
            v-model="btnForm.permissionCodes"
            multiple
            collapse-tags
            collapse-tags-tooltip
            placeholder="关联权限（可选）"
            style="width: 100%"
          >
            <el-option
              v-for="p in allPermissionOptions"
              :key="p.authority"
              :label="`${p.authorityName} (${p.authority})`"
              :value="p.authority"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="btnDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="btnSubmitting" @click="submitBtn">
          {{ btnDialogIsEdit ? '保存修改' : '确认创建' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- ===== 删除确认弹窗 ===== -->
    <el-dialog
      v-model="deleteDialogVisible"
      title="删除确认"
      width="400px"
      :close-on-click-modal="false"
    >
      <div class="tw-py-4 tw-text-center">
        <div style="font-size: 48px; margin-bottom: 12px">⚠️</div>
        <div class="tw-text-base tw-font-medium">
          确定要删除按钮「{{ deleteTarget?.name }}」吗？
        </div>
        <div class="tw-text-sm tw-text-gray-500 tw-mt-2">删除后不可恢复，请谨慎操作</div>
      </div>
      <template #footer>
        <el-button @click="deleteDialogVisible = false">取消</el-button>
        <el-button type="danger" :loading="deleteSubmitting" @click="doDelete">确认删除</el-button>
      </template>
    </el-dialog>
  </div>
</template>
