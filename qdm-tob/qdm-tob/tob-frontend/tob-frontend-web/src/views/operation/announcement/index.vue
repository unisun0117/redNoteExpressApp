<template>
  <div class="tw-space-y-3">
    <!-- ===== 查询区 ===== -->
    <div
      class="tw-bg-white tw-rounded-[--border-radius-card] tw-px-5 tw-pt-4 tw-pb-1 tw-shadow-[--shadow-card]"
    >
      <el-form :model="search" inline>
        <el-form-item label="销售大区">
          <el-select v-model="search.regionCode" placeholder="全部" clearable style="width: 180px">
            <el-option
              v-for="item in regionOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="search.enabled" placeholder="全部" clearable style="width: 130px">
            <el-option label="启用" value="true" />
            <el-option label="禁用" value="false" />
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
      <div class="tw-flex tw-justify-end tw-items-center tw-mb-4">
        <el-button type="primary" @click="openAdd">新增公告</el-button>
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
        <el-table-column prop="regionName" label="销售大区" min-width="140" show-overflow-tooltip />
        <el-table-column prop="content" label="公告内容" min-width="300" show-overflow-tooltip />
        <el-table-column label="状态" min-width="90" align="center">
          <template #default="{ row }">
            <el-switch v-model="row.enabled" :before-change="() => beforeToggle(row)" />
          </template>
        </el-table-column>
        <el-table-column prop="updatedBy" label="更新人" min-width="110" />
        <el-table-column prop="updatedAt" label="更新时间" min-width="170" />
        <el-table-column label="操作" min-width="90" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="openEdit(row)">编辑</el-button>
          </template>
        </el-table-column>

        <!-- 空状态 -->
        <template #empty>
          <div
            class="tw-flex tw-flex-col tw-items-center tw-justify-center tw-py-10 tw-text-[--text-secondary]"
          >
            <el-icon class="tw-text-4xl tw-mb-2"><Bell /></el-icon>
            <span class="tw-text-sm">暂无公告数据</span>
          </div>
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
      :title="dialog.isEdit ? '编辑公告' : '新增公告'"
      width="600px"
      align-center
      :close-on-click-modal="false"
      @close="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="销售大区" prop="regionCode">
          <el-select
            v-model="form.regionCode"
            :disabled="dialog.isEdit"
            placeholder="请选择销售大区"
            style="width: 100%"
          >
            <el-option
              v-for="item in regionOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="enabled">
          <el-switch v-model="form.enabled" />
        </el-form-item>
        <el-form-item label="公告内容" prop="content">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="6"
            maxlength="500"
            show-word-limit
            placeholder="请输入公告内容"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, watch, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import {
  getAnnouncementPage,
  createAnnouncement,
  updateAnnouncement,
  toggleAnnouncement,
} from '@/api/modules/announcement'
import { getAllSalesRegions } from '@/api/modules/operation'
import type { AnnouncementVO, AnnouncementSaveDTO } from '@/types/announcement'

// ===== 销售大区下拉选项（复用运营管理接口，避免硬编码） =====
interface OptionItem {
  label: string
  value: string
}

const regionOptions = ref<OptionItem[]>([])

async function loadRegionOptions() {
  try {
    const res = await getAllSalesRegions()
    if (res.code === 0) {
      regionOptions.value = res.data.records.map((r) => ({ label: r.name, value: r.code }))
    }
  } catch {
    // 错误已由拦截器统一提示
  }
}

// ===== 搜索条件 =====
const search = reactive({
  regionCode: '',
  /** '' 全部 / 'true' 启用 / 'false' 禁用 */
  enabled: '',
})

// ===== 表格数据 =====
const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(20)
const total = ref(0)
const tableData = ref<AnnouncementVO[]>([])

async function loadData() {
  loading.value = true
  try {
    const res = await getAnnouncementPage({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      regionCode: search.regionCode || undefined,
      enabled: !search.enabled ? undefined : search.enabled === 'true',
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
  search.regionCode = ''
  search.enabled = ''
  pageNum.value = 1
  loadData()
}

watch([pageNum, pageSize], () => {
  loadData()
})

// ===== 行内状态切换（el-switch before-change：接口成功才翻转，失败自动保持） =====
function beforeToggle(row: AnnouncementVO): Promise<boolean> {
  return toggleAnnouncement(row.id)
    .then((res) => {
      if (res.code === 0) {
        // row.enabled 此刻仍是切换前的值
        ElMessage.success(row.enabled ? '已禁用' : '已启用')
        return true
      }
      return false
    })
    .catch(() => false)
}

// ===== 新增 / 编辑弹窗 =====
const dialog = reactive({ visible: false, isEdit: false })
const formRef = ref<FormInstance | null>(null)
const submitting = ref(false)
const editingId = ref<number | null>(null)

const defaultForm = (): AnnouncementSaveDTO => ({
  regionCode: '',
  content: '',
  enabled: true,
})

const form = reactive<AnnouncementSaveDTO>(defaultForm())

const rules: FormRules = {
  regionCode: [{ required: true, message: '请选择销售大区', trigger: 'change' }],
  content: [
    { required: true, message: '请输入公告内容', trigger: 'blur' },
    { max: 500, message: '公告内容不能超过 500 字', trigger: 'blur' },
  ],
}

function openAdd() {
  dialog.isEdit = false
  editingId.value = null
  resetForm()
  dialog.visible = true
}

function openEdit(row: AnnouncementVO) {
  dialog.isEdit = true
  editingId.value = row.id
  form.regionCode = row.regionCode
  form.content = row.content
  form.enabled = row.enabled
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
      if (dialog.isEdit && editingId.value !== null) {
        await updateAnnouncement(editingId.value, {
          content: form.content,
          enabled: form.enabled,
        })
        ElMessage.success('编辑成功')
      } else {
        await createAnnouncement({
          regionCode: form.regionCode,
          content: form.content,
          enabled: form.enabled,
        })
        ElMessage.success('新增成功')
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
loadRegionOptions()
loadData()
</script>
