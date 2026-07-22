<template>
  <div class="tw-space-y-3">
    <!-- ===== 工具栏：搜索 + 统计 + 新增 ===== -->
    <div
      class="tw-bg-white tw-rounded-[--border-radius-card] tw-px-5 tw-pt-4 tw-pb-1 tw-shadow-[--shadow-card]"
    >
      <div class="tw-flex tw-flex-wrap tw-gap-3 tw-items-center">
        <el-input
          v-model="search.employeeCode"
          placeholder="工号"
          clearable
          style="width: 140px"
          @keyup.enter="doSearch"
        />
        <el-input
          v-model="search.realName"
          placeholder="用户名"
          clearable
          style="width: 140px"
          @keyup.enter="doSearch"
        />
        <el-input
          v-model="search.mobile"
          placeholder="手机号"
          clearable
          style="width: 140px"
          @keyup.enter="doSearch"
        />
        <el-select
          v-model="search.status"
          placeholder="状态"
          clearable
          style="width: 110px"
          @change="doSearch"
        >
          <el-option label="启用" value="ACTIVE" />
          <el-option label="停用" value="INACTIVE" />
        </el-select>
        <el-select
          v-model="search.type"
          placeholder="是否业务员"
          clearable
          style="width: 130px"
          @change="doSearch"
        >
          <el-option label="是" value="SALESMAN" />
          <el-option label="否" value="ADMIN" />
        </el-select>
        <el-button type="primary" @click="doSearch">查询</el-button>
        <el-button @click="resetSearch">重置</el-button>
        <span class="tw-text-sm tw-text-[--text-secondary] tw-ml-auto"
          >共 <b class="tw-text-[--text-color]">{{ total }}</b> 条记录</span
        >
        <el-button type="primary" @click="openAdd">+ 新增用户</el-button>
      </div>
    </div>

    <!-- ===== 数据区 ===== -->
    <div
      class="tw-bg-white tw-rounded-[--border-radius-card] tw-px-5 tw-pb-5 tw-shadow-[--shadow-card]"
    >
      <!-- 表格 -->
      <el-table
        v-loading="loading"
        :data="tableData"
        stripe
        style="width: 100%"
        :header-cell-style="{
          background: '#F1F5F9',
          color: '#475569',
          fontWeight: 600,
          fontSize: '13px',
        }"
      >
        <el-table-column label="账号" min-width="100">
          <template #default="{ row }">
            <b>{{ row.employeeCode }}</b>
          </template>
        </el-table-column>
        <el-table-column prop="realName" label="用户名" min-width="100" />
        <el-table-column label="手机号码" min-width="120">
          <template #default="{ row }">
            <span class="tw-text-[--text-secondary]">{{ maskPhone(row.mobile) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" min-width="80" align="center">
          <template #default="{ row }">
            <el-tag
              :type="row.status === 'ACTIVE' ? 'success' : 'info'"
              size="small"
              effect="plain"
            >
              {{ row.status === 'ACTIVE' ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="是否业务员" min-width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.type === 'SALESMAN'" type="primary" size="small" effect="plain"
              >是</el-tag
            >
            <span v-else class="tw-text-[--text-secondary]">否</span>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" min-width="160" />
        <el-table-column label="最后登录" min-width="160">
          <template #default="{ row }">
            {{ row.lastLoginAt || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="createdBy" label="创建人" min-width="80" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="openEdit(row)">编辑</el-button>
            <el-button type="success" link size="small" @click="openRoleConfig(row)">角色</el-button>
            <el-button type="warning" link size="small" @click="openPermConfig(row)">权限</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 空状态 -->
      <div
        v-if="tableData.length === 0 && !loading"
        class="tw-text-center tw-py-16 tw-text-[--text-secondary]"
      >
        <div class="tw-text-5xl tw-mb-3">👤</div>
        <div>暂无用户数据</div>
      </div>

      <!-- 分页 -->
      <div class="tw-mt-4 tw-flex">
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

    <!-- ===== 新增 / 编辑弹窗 ===== -->
    <el-dialog
      v-model="dialog.visible"
      :title="dialog.isEdit ? '编辑用户' : '新增用户'"
      width="560px"
      :close-on-click-modal="false"
      @close="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-form-item label="账号" prop="employeeCode">
          <el-input
            v-model="form.employeeCode"
            placeholder="请输入钱大妈统一工号"
            maxlength="32"
            :disabled="dialog.isEdit"
          />
          <div class="tw-text-xs tw-text-[--text-secondary] tw-mt-1">
            钱大妈统一工号，创建后不可修改
          </div>
        </el-form-item>
        <el-form-item label="用户名" prop="realName">
          <el-input v-model="form.realName" placeholder="请输入用户名" maxlength="64" />
        </el-form-item>
        <el-form-item label="手机号码" prop="mobile">
          <el-input v-model="form.mobile" placeholder="请输入11位手机号" maxlength="11" />
          <div class="tw-text-xs tw-text-[--text-secondary] tw-mt-1">11位手机号，全局唯一</div>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio value="ACTIVE">启用</el-radio>
            <el-radio value="INACTIVE">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="是否业务员" prop="type">
          <el-radio-group v-model="form.type">
            <el-radio value="ADMIN">否</el-radio>
            <el-radio value="SALESMAN">是</el-radio>
          </el-radio-group>
          <div class="tw-text-xs tw-text-[--text-secondary] tw-mt-1">
            标记为业务员后，在特定功能表单中会触发额外权限控制
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="dialog.submitting" @click="submitForm">
          {{ dialog.isEdit ? '保存' : '创建' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- ===== 配置角色弹窗 ===== -->
    <el-dialog
      v-model="roleDialog.visible"
      :title="roleDialog.user ? '配置角色 — ' + roleDialog.user.realName + '（' + roleDialog.user.employeeCode + '）' : '配置角色'"
      width="500px"
      :close-on-click-modal="false"
    >
      <div class="tw-text-sm tw-text-[--text-secondary] tw-mb-4">
        选择该用户绑定的角色（支持多选，保存时全量覆盖）
      </div>

      <!-- 空状态 -->
      <div v-if="allRoles.length === 0" class="tw-text-center tw-py-15 tw-text-[--text-secondary]">
        <div class="tw-text-5xl tw-mb-3">🔒</div>
        <div>暂无可用角色，请先在角色管理中创建角色</div>
      </div>

      <!-- 角色勾选 -->
      <el-checkbox-group v-model="roleDialog.checkedIds" v-else>
        <div class="tw-border tw-border-solid tw-border-[#e4e7ed] tw-rounded-lg tw-max-h-70 tw-overflow-y-auto">
          <div
            v-for="role in allRoles" :key="role.id"
            class="tw-flex tw-items-center tw-gap-2.5 tw-px-4 tw-py-3 tw-cursor-pointer tw-transition-colors hover:tw-bg-[#f5f7fa]"
            :class="{ 'tw-border-0 tw-border-t tw-border-solid tw-border-[#ebeef5]': role.id !== allRoles[0]?.id }"
          >
            <el-checkbox :label="role.id" :value="role.id" class="!tw-mr-0">{{ role.name }}</el-checkbox>
          </div>
        </div>
      </el-checkbox-group>

      <template #footer>
        <el-button @click="roleDialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="roleDialog.submitting" @click="submitRoleConfig">保存</el-button>
      </template>
    </el-dialog>

    <!-- ===== 配置数据权限弹窗 ===== -->
    <el-dialog
      v-model="permDialog.visible"
      :title="permDialog.user ? '配置数据权限 — ' + permDialog.user.realName + '（' + permDialog.user.employeeCode + '）' : '配置数据权限'"
      width="500px"
      :close-on-click-modal="false"
    >
      <div class="tw-text-sm tw-text-[--text-secondary] tw-mb-4">
        数据维度：<b class="tw-text-[--text-color]">销售大区</b>（来源于运营管理 → 销售大区管理）
      </div>

      <!-- 空状态 -->
      <div v-if="allRegions.length === 0" class="tw-text-center tw-py-15 tw-text-[--text-secondary]">
        <div class="tw-text-5xl tw-mb-3">🗺️</div>
        <div>暂无可用销售大区，请先在运营管理中创建销售大区</div>
      </div>

      <!-- 大区勾选 -->
      <el-checkbox-group v-model="permDialog.checkedIds" v-else>
        <div class="tw-border tw-border-solid tw-border-[#e4e7ed] tw-rounded-lg tw-max-h-70 tw-overflow-y-auto">
          <div
            v-for="region in allRegions" :key="region.code"
            class="tw-flex tw-items-center tw-gap-2.5 tw-px-4 tw-py-3 tw-cursor-pointer tw-transition-colors hover:tw-bg-[#f5f7fa]"
            :class="{ 'tw-border-0 tw-border-t tw-border-solid tw-border-[#ebeef5]': region.code !== allRegions[0]?.code }"
          >
            <el-checkbox :label="region.code" :value="region.code" class="!tw-mr-0">{{ region.name }}</el-checkbox>
          </div>
        </div>
      </el-checkbox-group>

      <template #footer>
        <el-button @click="permDialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="permDialog.submitting" @click="submitPermConfig">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, watch, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import {
  getOperatorPage,
  getOperator,
  createOperator,
  updateOperator,
  getOperatorRoles,
  setOperatorRoles,
  getOperatorRegions,
  setOperatorRegions,
  getAllRoles,
  getAllRegions,
  type OperatorVO,
  type OperatorSaveDTO,
  type RoleSimpleVO,
  type RegionSimpleVO,
} from '@/api/modules/system'

// ===== 工具函数 =====
function maskPhone(phone: string): string {
  if (!phone) return '-'
  return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
}

// ===== 搜索条件 =====
const search = reactive({
  employeeCode: '',
  realName: '',
  mobile: '',
  status: '',
  type: '',
})

// ===== 表格数据 =====
const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(20)
const total = ref(0)
const tableData = ref<OperatorVO[]>([])

async function loadData() {
  loading.value = true
  try {
    const res = await getOperatorPage({
      page: pageNum.value,
      size: pageSize.value,
      employeeCode: search.employeeCode || undefined,
      realName: search.realName || undefined,
      mobile: search.mobile || undefined,
      status: search.status || undefined,
      type: search.type || undefined,
    })
    tableData.value = res.records
    total.value = res.total
  } catch {
    // 拦截器已统一提示
  } finally {
    loading.value = false
  }
}

function doSearch() {
  pageNum.value = 1
  loadData()
}

function resetSearch() {
  search.employeeCode = ''
  search.realName = ''
  search.mobile = ''
  search.status = ''
  search.type = ''
  pageNum.value = 1
  loadData()
}

watch([pageNum, pageSize], () => {
  loadData()
})

// ===== 新增 / 编辑弹窗 =====
const dialog = reactive({
  visible: false,
  isEdit: false,
  submitting: false,
  editId: null as number | null,
})

const formRef = ref<FormInstance | null>(null)

const defaultForm = (): OperatorSaveDTO => ({
  employeeCode: '',
  realName: '',
  mobile: '',
  status: 'ACTIVE',
  type: 'ADMIN',
})

const form = reactive<OperatorSaveDTO>(defaultForm())

const rules: FormRules = {
  employeeCode: [{ required: true, message: '请输入工号', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  mobile: [
    { required: true, message: '请输入手机号码', trigger: 'blur' },
    { pattern: /^\d{11}$/, message: '请输入正确的11位手机号', trigger: 'blur' },
  ],
}

function openAdd() {
  dialog.isEdit = false
  dialog.editId = null
  Object.assign(form, defaultForm())
  dialog.visible = true
  nextTick(() => formRef.value?.clearValidate())
}

async function openEdit(row: OperatorVO) {
  dialog.isEdit = true
  dialog.editId = row.id
  // 先展示列表数据，loading 时用脱敏信息占位
  form.employeeCode = row.employeeCode
  form.realName = row.realName
  form.mobile = row.mobile
  form.status = row.status
  form.type = row.type
  dialog.visible = true
  nextTick(() => formRef.value?.clearValidate())
  // 调详情接口获取完整手机号
  try {
    const detail = await getOperator(row.id)
    form.mobile = detail.mobile
  } catch {
    // 获取失败保留脱敏信息，用户可自行补充
  }
}

function resetForm() {
  dialog.editId = null
}

async function submitForm() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return

    dialog.submitting = true
    try {
      if (dialog.isEdit && dialog.editId) {
        const { employeeCode: _, ...editData } = form as OperatorSaveDTO
        await updateOperator(dialog.editId, editData as OperatorSaveDTO)
        ElMessage.success('用户更新成功')
      } else {
        await createOperator(form as OperatorSaveDTO)
        ElMessage.success('用户创建成功')
      }
      dialog.visible = false
      loadData()
    } catch {
      // 拦截器已统一提示
    } finally {
      dialog.submitting = false
    }
  })
}

// ===== 配置角色弹窗 =====
const allRoles = ref<RoleSimpleVO[]>([])

const roleDialog = reactive({
  visible: false,
  submitting: false,
  user: null as OperatorVO | null,
  checkedIds: [] as number[],
})

async function openRoleConfig(row: OperatorVO) {
  roleDialog.user = row
  roleDialog.submitting = false
  // 并行加载角色列表和用户已绑定角色
  const [roles, boundIds] = await Promise.all([
    getAllRoles().catch(() => [] as RoleSimpleVO[]),
    getOperatorRoles(row.id).catch(() => [] as number[]),
  ])
  allRoles.value = roles
  roleDialog.checkedIds = [...boundIds]
  roleDialog.visible = true
}

async function submitRoleConfig() {
  if (!roleDialog.user) return
  roleDialog.submitting = true
  try {
    await setOperatorRoles(roleDialog.user.id, roleDialog.checkedIds)
    ElMessage.success('角色配置已保存')
    roleDialog.visible = false
  } catch {
    // 拦截器已处理 toast
  } finally {
    roleDialog.submitting = false
  }
}

// ===== 配置数据权限弹窗 =====
const allRegions = ref<RegionSimpleVO[]>([])

const permDialog = reactive({
  visible: false,
  submitting: false,
  user: null as OperatorVO | null,
  checkedIds: [] as string[],
})

async function openPermConfig(row: OperatorVO) {
  permDialog.user = row
  permDialog.submitting = false
  const [regions, boundCodes] = await Promise.all([
    getAllRegions().catch(() => [] as RegionSimpleVO[]),
    getOperatorRegions(row.id).catch(() => [] as string[]),
  ])
  allRegions.value = regions
  permDialog.checkedIds = [...boundCodes]
  permDialog.visible = true
}

async function submitPermConfig() {
  if (!permDialog.user) return
  permDialog.submitting = true
  try {
    await setOperatorRegions(permDialog.user.id, permDialog.checkedIds)
    ElMessage.success('数据权限配置已保存')
    permDialog.visible = false
  } catch {
    // 拦截器已处理 toast
  } finally {
    permDialog.submitting = false
  }
}

// ===== 初始化 =====
loadData()
</script>
