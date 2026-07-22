<template>
  <div class="tw-space-y-3">
    <!-- ===== 查询区 ===== -->
    <div
      class="tw-bg-white tw-rounded-[--border-radius-card] tw-px-5 tw-pt-4 tw-pb-1 tw-shadow-[--shadow-card]"
    >
      <el-form :model="search" inline>
        <el-form-item label="姓名/手机号">
          <el-input
            v-model="search.keyword"
            placeholder="姓名或手机号"
            clearable
            style="width: 200px"
            @keyup.enter="doSearch"
          />
        </el-form-item>
        <el-form-item label="注册来源">
          <el-select v-model="search.source" placeholder="全部" clearable style="width: 130px">
            <el-option label="小程序" value="WECHAT" />
            <el-option label="后台" value="ADMIN" />
          </el-select>
        </el-form-item>
        <el-form-item label="账号状态">
          <el-select v-model="search.status" placeholder="全部" clearable style="width: 120px">
            <el-option label="正常" value="ACTIVE" />
            <el-option label="已禁用" value="INACTIVE" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="doSearch">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- ===== 数据区 ===== -->
    <div class="tw-bg-white tw-rounded-[--border-radius-card] tw-p-5 tw-shadow-[--shadow-card]">
      <!-- 工具栏 -->
      <div class="tw-flex tw-justify-between tw-items-center tw-mb-4">
        <el-button type="primary" @click="openAdd">新增登录账号</el-button>
        <span class="tw-text-[13px] tw-text-[--text-secondary]">共 {{ total }} 条记录</span>
      </div>

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
        <el-table-column type="selection" width="50" fixed="left" />
        <el-table-column label="客户ID" width="110" fixed="left">
          <template #default="{ row }">
            <span class="tw-font-medium">C{{ row.id }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="realName" label="姓名" width="100" show-overflow-tooltip />
        <el-table-column label="手机号" width="130">
          <template #default="{ row }">
            {{ maskMobile(row.mobile) }}
          </template>
        </el-table-column>
        <el-table-column label="注册来源" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.source === 'ADMIN' ? 'info' : ''" size="small" effect="plain">
              {{ row.source === 'ADMIN' ? '后台' : '小程序' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="已绑定客户" width="110" align="center">
          <template #default="{ row }">
            <span>{{ row.boundCount > 0 ? row.boundCount + ' 家' : '0' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="微信绑定" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.wechatOpenid ? 'success' : 'info'" size="small" effect="plain">
              {{ row.wechatOpenid ? '已绑定' : '未绑定' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="wechatNickname" label="微信昵称" width="120" show-overflow-tooltip />
        <el-table-column label="账号状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag
              :type="row.status === 'ACTIVE' ? 'success' : 'danger'"
              size="small"
              effect="plain"
            >
              {{ row.status === 'ACTIVE' ? '正常' : '已禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="registeredAt" label="注册时间" min-width="160" />
        <el-table-column label="最近登录" min-width="160">
          <template #default="{ row }">
            {{ row.lastLoginAt || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="openEdit(row)">编辑</el-button>
            <el-button
              :type="row.status === 'ACTIVE' ? 'warning' : 'success'"
              link
              @click="confirmToggle(row)"
            >
              {{ row.status === 'ACTIVE' ? '禁用' : '启用' }}
            </el-button>
            <el-button type="danger" link @click="confirmDelete(row)">删除</el-button>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无登录账号数据" :image-size="100" />
        </template>
      </el-table>

      <!-- 分页（水平居中） -->
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
      :title="dialog.isEdit ? '编辑登录账号' : '新增登录账号'"
      width="460px"
      :close-on-click-modal="false"
      @close="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="form.realName" placeholder="请输入姓名" maxlength="50" />
        </el-form-item>
        <el-form-item label="手机号" prop="mobile">
          <el-input
            v-model="form.mobile"
            :disabled="dialog.isEdit"
            placeholder="请输入11位手机号"
            maxlength="11"
          />
        </el-form-item>
        <el-form-item v-if="!dialog.isEdit" label="">
          <span class="tw-text-xs tw-text-[--text-secondary]"
            >后台新增免短信验证码，微信待客户自行绑定</span
          >
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitForm"> 确定 </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, watch, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import {
  getSysUserPage,
  createSysUser,
  editSysUser,
  toggleSysUserStatus,
  deleteSysUser,
  type SysUserSummaryVO,
  type SysUserCreationVO,
  type SysUserEditVO,
  type UserSource,
  type UserStatus,
} from '@/api/modules/customer'

// ===== 搜索条件 =====
const search = reactive({
  keyword: '',
  source: '' as '' | UserSource,
  status: '' as '' | UserStatus,
})

// ===== 表格数据 =====
const loading = ref(false)
const submitting = ref(false)
const pageNum = ref(1)
const pageSize = ref(20)
const total = ref(0)
const tableData = ref<SysUserSummaryVO[]>([])

/** 手机号脱敏：138****1234 */
function maskMobile(mobile: string | null): string {
  if (!mobile || mobile.length < 11) return mobile || '-'
  return `${mobile.slice(0, 3)}****${mobile.slice(7)}`
}

/** 加载列表 */
async function loadData() {
  loading.value = true
  try {
    const res = await getSysUserPage({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      keyword: search.keyword || undefined,
      source: search.source || undefined,
      status: search.status || undefined,
    })
    if (res.code === 0) {
      tableData.value = res.data.records
      total.value = res.data.total
    }
  } catch {
    // 错误已由拦截器统一提示
  } finally {
    loading.value = false
  }
}

function doSearch() {
  pageNum.value = 1
  loadData()
}

function resetSearch() {
  search.keyword = ''
  search.source = ''
  search.status = ''
  pageNum.value = 1
  loadData()
}

watch([pageNum, pageSize], () => {
  loadData()
})

// ===== 切换状态 =====
function confirmToggle(row: SysUserSummaryVO) {
  const action = row.status === 'ACTIVE' ? '禁用' : '启用'
  ElMessageBox.confirm(`确定${action}该账号？`, '操作确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  })
    .then(async () => {
      try {
        await toggleSysUserStatus(row.id)
        ElMessage.success(`账号已${action}`)
        loadData()
      } catch {
        // 错误已由拦截器统一提示
      }
    })
    .catch(() => {
      // 取消
    })
}

// ===== 删除 =====
function confirmDelete(row: SysUserSummaryVO) {
  ElMessageBox.confirm(
    `确定硬删除登录账号「${row.realName}」？数据物理删除不可恢复，手机号将被释放。`,
    '删除确认',
    {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning',
    },
  )
    .then(async () => {
      try {
        await deleteSysUser(row.id)
        ElMessage.success('登录账号已删除（硬删除，手机号已释放）')
        loadData()
      } catch {
        // 错误已由拦截器统一提示
      }
    })
    .catch(() => {
      // 取消
    })
}

// ===== 新增 / 编辑弹窗 =====
interface FormData {
  id: number | null
  realName: string
  mobile: string
}

const dialog = reactive({ visible: false, isEdit: false })
const formRef = ref<FormInstance | null>(null)

const defaultForm = (): FormData => ({
  id: null,
  realName: '',
  mobile: '',
})

const form = reactive<FormData>(defaultForm())

const rules: FormRules = {
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  mobile: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的11位手机号', trigger: 'blur' },
  ],
}

function openAdd() {
  dialog.isEdit = false
  resetForm()
  dialog.visible = true
}

function openEdit(row: SysUserSummaryVO) {
  dialog.isEdit = true
  form.id = row.id
  form.realName = row.realName
  form.mobile = row.mobile
  dialog.visible = true
  nextTick(() => {
    formRef.value?.clearValidate()
  })
}

function resetForm() {
  Object.assign(form, defaultForm())
  nextTick(() => {
    formRef.value?.clearValidate()
  })
}

function submitForm() {
  formRef.value?.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    try {
      if (dialog.isEdit) {
        const editData: SysUserEditVO = {
          id: form.id as number,
          realName: form.realName.trim(),
        }
        await editSysUser(editData)
        ElMessage.success('登录账号信息更新成功')
      } else {
        const createData: SysUserCreationVO = {
          realName: form.realName.trim(),
          mobile: form.mobile.trim(),
        }
        await createSysUser(createData)
        ElMessage.success('登录账号新增成功')
      }
      dialog.visible = false
      loadData()
    } catch {
      // 错误已由拦截器统一提示
    } finally {
      submitting.value = false
    }
  })
}

// ===== 初始化 =====
loadData()
</script>
