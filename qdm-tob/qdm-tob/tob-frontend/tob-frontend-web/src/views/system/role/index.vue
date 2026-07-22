<script setup lang="ts">
import { ref, reactive, computed, onMounted, nextTick, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { getRoleList, getRoleMenus, createRole, updateRole, deleteRole } from '@/api/modules/role'
import { getAdminTree } from '@/api/modules/menu'
import type { RoleViewVO, RoleCreationVO, RoleEditVO, PageResult } from '@/types/role'
import type { MenuTreeNode } from '@/types/menu'

// ========================= 页面模式 =========================
type PageMode = 'list' | 'create' | 'edit'
const mode = ref<PageMode>('list')
const editingRoleId = ref<number | null>(null)
const submitting = ref(false)

// ========================= 搜索 & 分页 =========================
const search = reactive({ code: '', name: '' })
const pageNum = ref(1)
const pageSize = ref(20)
const loading = ref(false)
const tableData = ref<RoleViewVO[]>([])
const total = ref(0)

const hasSearch = computed(() => !!(search.code || search.name))

async function loadList() {
  loading.value = true
  try {
    const result: PageResult<RoleViewVO> = await getRoleList({
      code: search.code || undefined,
      name: search.name || undefined,
      page: pageNum.value,
      size: pageSize.value,
    })
    tableData.value = result.records
    total.value = result.total
  } finally {
    loading.value = false
  }
}

function doSearch() {
  pageNum.value = 1
  loadList()
}

function resetSearch() {
  search.code = ''
  search.name = ''
  pageNum.value = 1
  loadList()
}

watch([() => search.code, () => search.name], () => {
  pageNum.value = 1
})

watch([pageNum, pageSize], () => {
  loadList()
})

onMounted(() => {
  loadList()
})

// ========================= 菜单树 =========================
const webMenuTree = ref<MenuTreeNode[]>([])
const wecomMenuTree = ref<MenuTreeNode[]>([])

async function loadMenuTrees() {
  const [web, wecom] = await Promise.all([
    getAdminTree('WEB', true),
    getAdminTree('WECOM', true),
  ])
  webMenuTree.value = web
  wecomMenuTree.value = wecom
}

// ========================= 表单 =========================
const formRef = ref<FormInstance>()
const webTreeRef = ref()
const wecomTreeRef = ref()
const form = reactive({
  code: '',
  name: '',
  description: '',
  status: 'ACTIVE' as 'ACTIVE' | 'INACTIVE',
  menuIds: [] as number[],
})

const formRules: FormRules = {
  code: [
    { required: true, message: '请输入角色编码', trigger: 'blur' },
    { pattern: /^[A-Za-z0-9_]+$/, message: '仅允许字母、数字、下划线', trigger: 'blur' },
  ],
  name: [
    { required: true, message: '请输入角色名称', trigger: 'blur' },
    { max: 60, message: '最长60字符', trigger: 'blur' },
  ],
}

const editFormRules: FormRules = {
  name: [
    { required: true, message: '请输入角色名称', trigger: 'blur' },
    { max: 60, message: '最长60字符', trigger: 'blur' },
  ],
}

// ========================= 页面跳转 =========================
function goList() {
  mode.value = 'list'
  editingRoleId.value = null
  submitting.value = false
  loadList()
}

function goCreate() {
  mode.value = 'create'
  editingRoleId.value = null
  submitting.value = false
  form.code = ''
  form.name = ''
  form.description = ''
  form.status = 'ACTIVE'
  form.menuIds = []
  loadMenuTrees()
  nextTick(() => {
    formRef.value?.clearValidate()
    webTreeRef.value?.setCheckedKeys([])
    wecomTreeRef.value?.setCheckedKeys([])
  })
}

async function goEdit(row: RoleViewVO) {
  mode.value = 'edit'
  editingRoleId.value = row.id
  submitting.value = false
  form.code = row.code
  form.name = row.name
  form.description = row.description || ''
  form.status = row.status

  await loadMenuTrees()
  const menuIds = await getRoleMenus(row.id)
  form.menuIds = menuIds
  nextTick(() => {
    formRef.value?.clearValidate()
    webTreeRef.value?.setCheckedKeys(menuIds)
    wecomTreeRef.value?.setCheckedKeys(menuIds)
  })
}

// ========================= 菜单树勾选 =========================
function onWebTreeCheck(_node: unknown, checkedInfo: { checkedKeys: number[] }) {
  form.menuIds = checkedInfo.checkedKeys
}
function onWecomTreeCheck(_node: unknown, checkedInfo: { checkedKeys: number[] }) {
  form.menuIds = checkedInfo.checkedKeys
}

function getTypeIcon(type: string) {
  if (type === 'MENU') return '📁'
  if (type === 'PAGE') return '📄'
  return '🔘'
}

function getNodeStyle(type: string) {
  return {
    fontSize: type === 'BUTTON' ? '12px' : '14px',
    color: type === 'BUTTON' ? '#909399' : '#303133',
  }
}

// ========================= 提交 =========================
async function submitCreate() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    const data: RoleCreationVO = {
      code: form.code,
      name: form.name,
      description: form.description || undefined,
      status: form.status,
      menuIds: form.menuIds,
    }
    await createRole(data)
    ElMessage.success('角色创建成功')
    goList()
  } catch {
    // 拦截器已处理 toast
  } finally {
    submitting.value = false
  }
}

async function submitEdit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    const data: RoleEditVO = {
      id: editingRoleId.value!,
      name: form.name,
      description: form.description || undefined,
      status: form.status,
      menuIds: form.menuIds,
    }
    await updateRole(data)
    ElMessage.success('角色更新成功')
    goList()
  } catch {
    // 拦截器已处理 toast
  } finally {
    submitting.value = false
  }
}

async function handleDelete(row: RoleViewVO) {
  try {
    await ElMessageBox.confirm(
      `确认删除角色「${row.name}」吗？删除后已绑定用户将失去该角色权限。`,
      '确认删除',
      { confirmButtonText: '删除', cancelButtonText: '取消', type: 'warning' },
    )
  } catch {
    return
  }
  try {
    await deleteRole(row.id)
    ElMessage.success('角色已删除')
    loadList()
  } catch {
    // 拦截器已处理 toast
  }
}
</script>

<template>
  <div class="tw-space-y-3">
    <!-- ==================== 列表视图 ==================== -->
    <template v-if="mode === 'list'">
      <div class="tw-bg-white tw-rounded-[--border-radius-card] tw-px-5 tw-pt-4 tw-pb-1 tw-shadow-[--shadow-card]">
        <div class="tw-flex tw-flex-wrap tw-gap-3 tw-items-center">
          <el-input v-model="search.code" placeholder="角色编码" clearable style="width:160px" @keyup.enter="doSearch" />
          <el-input v-model="search.name" placeholder="角色名称" clearable style="width:160px" @keyup.enter="doSearch" />
          <el-button type="primary" @click="doSearch">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
          <span class="tw-text-sm tw-text-[--text-secondary] tw-ml-auto">共 <b class="tw-text-[--text-color]">{{ total }}</b> 条记录</span>
          <el-button type="primary" @click="goCreate">+ 新增角色</el-button>
        </div>
      </div>

      <div class="tw-bg-white tw-rounded-[--border-radius-card] tw-px-5 tw-pb-5 tw-shadow-[--shadow-card]">

        <el-table
          :data="tableData"
          stripe
          v-loading="loading"
          style="width:100%"
          :header-cell-style="{ background: '#f5f7fa', color: '#606266', fontWeight: 500 }"
        >
          <el-table-column label="序号" width="70" align="center">
            <template #default="scope">
              {{ (pageNum - 1) * pageSize + scope.$index + 1 }}
            </template>
          </el-table-column>
          <el-table-column prop="code" label="角色编码" min-width="120">
            <template #default="scope">
              <b>{{ scope.row.code }}</b>
            </template>
          </el-table-column>
          <el-table-column prop="name" label="角色名称" min-width="120" />
          <el-table-column label="状态" width="100" align="center">
            <template #default="scope">
              <el-tag
                :type="scope.row.status === 'ACTIVE' ? 'success' : 'info'"
                size="small"
                disable-transitions
              >
                {{ scope.row.status === 'ACTIVE' ? '启用' : '禁用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createdAt" label="创建时间" min-width="160" />
          <el-table-column prop="createdBy" label="创建人" min-width="80" />
          <el-table-column prop="updatedAt" label="更新时间" min-width="160" />
          <el-table-column prop="updatedBy" label="更新人" min-width="80" />
          <el-table-column label="操作" width="120" fixed="right">
            <template #default="scope">
              <el-button type="primary" link size="small" @click="goEdit(scope.row)">编辑</el-button>
              <el-button type="danger" link size="small" @click="handleDelete(scope.row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>

        <div v-if="tableData.length === 0 && !loading" class="tw-text-center tw-py-15 tw-text-[#909399]">
          <div class="tw-text-5xl tw-mb-3">🔒</div>
          <div>{{ hasSearch ? '未找到匹配的角色' : '暂无角色数据，点击"新增"创建第一条' }}</div>
        </div>

        <div v-if="total > 0" class="tw-mt-4 tw-flex tw-justify-center">
          <el-pagination
            v-model:current-page="pageNum"
            v-model:page-size="pageSize"
            :page-sizes="[10, 20, 50]"
            :total="total"
            layout="total, sizes, prev, pager, next"
            background
          />
        </div>
      </div>
    </template>

    <!-- ==================== 新增视图 ==================== -->
    <template v-if="mode === 'create'">
      <a class="tw-inline-block tw-text-sm tw-text-[#409eff] tw-cursor-pointer tw-no-underline tw-mb-2 hover:tw-text-[#66b1ff]" @click="goList">← 返回列表</a>

      <div class="tw-bg-white tw-rounded-lg tw-p-4 tw-mb-2">
        <div class="tw-text-[15px] tw-font-semibold tw-text-[#303133] tw-mb-4 tw-pb-2 tw-border-0 tw-border-b tw-border-solid tw-border-[#ebeef5]">基本信息</div>
        <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px" style="max-width:500px">
          <el-form-item label="角色编码" prop="code">
            <el-input v-model="form.code" placeholder="请输入角色编码" maxlength="60" />
            <div class="tw-text-xs tw-text-[#909399] tw-mt-1">仅允许字母、数字、下划线，创建后不可修改</div>
          </el-form-item>
          <el-form-item label="角色名称" prop="name">
            <el-input v-model="form.name" placeholder="请输入角色名称" maxlength="60" />
          </el-form-item>
          <el-form-item label="状态">
            <el-radio-group v-model="form.status">
              <el-radio value="ACTIVE">启用</el-radio>
              <el-radio value="INACTIVE">禁用</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-form>
      </div>

      <div class="tw-bg-white tw-rounded-lg tw-p-4 tw-mb-2">
        <div class="tw-text-[15px] tw-font-semibold tw-text-[#303133] tw-mb-4 tw-pb-2 tw-border-0 tw-border-b tw-border-solid tw-border-[#ebeef5]">菜单权限配置</div>
        <div class="tw-flex tw-gap-2">
          <div class="tw-flex-1 tw-min-w-0">
            <div class="tw-text-sm tw-font-semibold tw-text-[#303133] tw-mb-2 tw-pb-2 tw-border-0 tw-border-b tw-border-solid tw-border-[#ebeef5]">后台菜单</div>
            <div class="tw-border tw-border-solid tw-border-[#e4e7ed] tw-rounded-lg tw-p-4 tw-min-h-[360px] tw-max-h-[520px] tw-overflow-y-auto tw-bg-[#fafafa]">
              <div v-if="webMenuTree.length === 0" class="tw-text-center tw-py-15 tw-text-[#909399]">
                <div class="tw-text-5xl tw-mb-3">📁</div>
                <div>暂无菜单数据，请先在菜单管理中维护菜单</div>
              </div>
              <el-tree
                v-else
                ref="webTreeRef"
                :data="webMenuTree"
                show-checkbox
                node-key="id"
                default-expand-all
                :props="{ children: 'children', label: 'name' }"
                @check="onWebTreeCheck"
              >
                <template #default="{ data }">
                  <span class="tw-flex tw-items-center">
                    <span class="tw-mr-1 tw-text-[13px]">{{ getTypeIcon(data.type) }}</span>
                    <span :style="getNodeStyle(data.type)">{{ data.name }}</span>
                    <el-tag v-if="data.type === 'BUTTON'" size="small" class="tw-ml-1.5 tw-text-[11px] tw-scale-90" type="info">按钮</el-tag>
                  </span>
                </template>
              </el-tree>
            </div>
          </div>
          <div class="tw-flex-1 tw-min-w-0">
            <div class="tw-text-sm tw-font-semibold tw-text-[#303133] tw-mb-2 tw-pb-2 tw-border-0 tw-border-b tw-border-solid tw-border-[#ebeef5]">企微菜单</div>
            <div class="tw-border tw-border-solid tw-border-[#e4e7ed] tw-rounded-lg tw-p-4 tw-min-h-[360px] tw-max-h-[520px] tw-overflow-y-auto tw-bg-[#fafafa]">
              <div v-if="wecomMenuTree.length === 0" class="tw-text-center tw-py-15 tw-text-[#909399]">
                <div class="tw-text-5xl tw-mb-3">📱</div>
                <div>暂无菜单数据，请先在菜单管理中维护菜单</div>
              </div>
              <el-tree
                v-else
                ref="wecomTreeRef"
                :data="wecomMenuTree"
                show-checkbox
                node-key="id"
                default-expand-all
                :props="{ children: 'children', label: 'name' }"
                @check="onWecomTreeCheck"
              >
                <template #default="{ data }">
                  <span class="tw-flex tw-items-center">
                    <span class="tw-mr-1 tw-text-[13px]">{{ getTypeIcon(data.type) }}</span>
                    <span :style="getNodeStyle(data.type)">{{ data.name }}</span>
                    <el-tag v-if="data.type === 'BUTTON'" size="small" class="tw-ml-1.5 tw-text-[11px] tw-scale-90" type="info">按钮</el-tag>
                  </span>
                </template>
              </el-tree>
            </div>
          </div>
        </div>
      </div>

      <div class="tw-flex tw-justify-end tw-gap-3 tw-mt-2">
        <el-button @click="goList">取消</el-button>
        <el-button type="primary" @click="submitCreate" :loading="submitting">保存</el-button>
      </div>
    </template>

    <!-- ==================== 编辑视图 ==================== -->
    <template v-if="mode === 'edit'">
      <a class="tw-inline-block tw-text-sm tw-text-[#409eff] tw-cursor-pointer tw-no-underline tw-mb-2 hover:tw-text-[#66b1ff]" @click="goList">← 返回列表</a>

      <div class="tw-bg-white tw-rounded-lg tw-p-4 tw-mb-2">
        <div class="tw-text-[15px] tw-font-semibold tw-text-[#303133] tw-mb-4 tw-pb-2 tw-border-0 tw-border-b tw-border-solid tw-border-[#ebeef5]">基本信息</div>
        <el-form ref="formRef" :model="form" :rules="editFormRules" label-width="100px" style="max-width:500px">
          <el-form-item label="角色编码">
            <el-input v-model="form.code" disabled />
            <div class="tw-text-xs tw-text-[#909399] tw-mt-1">角色编码不可修改</div>
          </el-form-item>
          <el-form-item label="角色名称" prop="name">
            <el-input v-model="form.name" placeholder="请输入角色名称" maxlength="60" />
          </el-form-item>
          <el-form-item label="状态">
            <el-radio-group v-model="form.status">
              <el-radio value="ACTIVE">启用</el-radio>
              <el-radio value="INACTIVE">禁用</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-form>
      </div>

      <div class="tw-bg-white tw-rounded-lg tw-p-4 tw-mb-2">
        <div class="tw-text-[15px] tw-font-semibold tw-text-[#303133] tw-mb-4 tw-pb-2 tw-border-0 tw-border-b tw-border-solid tw-border-[#ebeef5]">菜单权限配置</div>
        <div class="tw-flex tw-gap-2">
          <div class="tw-flex-1 tw-min-w-0">
            <div class="tw-text-sm tw-font-semibold tw-text-[#303133] tw-mb-2 tw-pb-2 tw-border-0 tw-border-b tw-border-solid tw-border-[#ebeef5]">后台菜单</div>
            <div class="tw-border tw-border-solid tw-border-[#e4e7ed] tw-rounded-lg tw-p-4 tw-min-h-[360px] tw-max-h-[520px] tw-overflow-y-auto tw-bg-[#fafafa]">
              <div v-if="webMenuTree.length === 0" class="tw-text-center tw-py-15 tw-text-[#909399]">
                <div class="tw-text-5xl tw-mb-3">📁</div>
                <div>暂无菜单数据，请先在菜单管理中维护菜单</div>
              </div>
              <el-tree
                v-else
                ref="webTreeRef"
                :data="webMenuTree"
                show-checkbox
                node-key="id"
                default-expand-all
                :props="{ children: 'children', label: 'name' }"
                @check="onWebTreeCheck"
              >
                <template #default="{ data }">
                  <span class="tw-flex tw-items-center">
                    <span class="tw-mr-1 tw-text-[13px]">{{ getTypeIcon(data.type) }}</span>
                    <span :style="getNodeStyle(data.type)">{{ data.name }}</span>
                    <el-tag v-if="data.type === 'BUTTON'" size="small" class="tw-ml-1.5 tw-text-[11px] tw-scale-90" type="info">按钮</el-tag>
                  </span>
                </template>
              </el-tree>
            </div>
          </div>
          <div class="tw-flex-1 tw-min-w-0">
            <div class="tw-text-sm tw-font-semibold tw-text-[#303133] tw-mb-2 tw-pb-2 tw-border-0 tw-border-b tw-border-solid tw-border-[#ebeef5]">企微菜单</div>
            <div class="tw-border tw-border-solid tw-border-[#e4e7ed] tw-rounded-lg tw-p-4 tw-min-h-[360px] tw-max-h-[520px] tw-overflow-y-auto tw-bg-[#fafafa]">
              <div v-if="wecomMenuTree.length === 0" class="tw-text-center tw-py-15 tw-text-[#909399]">
                <div class="tw-text-5xl tw-mb-3">📱</div>
                <div>暂无菜单数据，请先在菜单管理中维护菜单</div>
              </div>
              <el-tree
                v-else
                ref="wecomTreeRef"
                :data="wecomMenuTree"
                show-checkbox
                node-key="id"
                default-expand-all
                :props="{ children: 'children', label: 'name' }"
                @check="onWecomTreeCheck"
              >
                <template #default="{ data }">
                  <span class="tw-flex tw-items-center">
                    <span class="tw-mr-1 tw-text-[13px]">{{ getTypeIcon(data.type) }}</span>
                    <span :style="getNodeStyle(data.type)">{{ data.name }}</span>
                    <el-tag v-if="data.type === 'BUTTON'" size="small" class="tw-ml-1.5 tw-text-[11px] tw-scale-90" type="info">按钮</el-tag>
                  </span>
                </template>
              </el-tree>
            </div>
          </div>
        </div>
      </div>

      <div class="tw-flex tw-justify-end tw-gap-3 tw-mt-2">
        <el-button @click="goList">取消</el-button>
        <el-button type="primary" @click="submitEdit" :loading="submitting">保存</el-button>
      </div>
    </template>
  </div>
</template>
