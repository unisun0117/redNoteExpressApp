<template>
  <div class="tw-space-y-3">
    <!-- ===== 查询区 ===== -->
    <div
      class="tw-bg-white tw-rounded-[--border-radius-card] tw-px-5 tw-pt-4 tw-pb-1 tw-shadow-[--shadow-card]"
    >
      <el-form :model="search" inline>
        <el-form-item label="分类级别">
          <el-select v-model="search.level" placeholder="全部" clearable style="width: 130px">
            <el-option label="大分类" :value="0" />
            <el-option label="中分类" :value="1" />
            <el-option label="小分类" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="分类编码">
          <el-input
            v-model="search.code"
            placeholder="分类编码"
            clearable
            style="width: 150px"
            @keyup.enter="doSearch"
          />
        </el-form-item>
        <el-form-item label="分类名称">
          <el-input
            v-model="search.name"
            placeholder="分类名称"
            clearable
            style="width: 150px"
            @keyup.enter="doSearch"
          />
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
        <span class="tw-text-[13px] tw-text-[--text-secondary]">
          数据来源于 SAP 同步，仅支持编辑自定义名称
        </span>
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
        <el-table-column prop="id" label="分类编码" width="100" />
        <el-table-column prop="name" label="分类名称" width="120" show-overflow-tooltip />
        <el-table-column label="自定义名称" width="140" show-overflow-tooltip>
          <template #default="{ row }">
            <span :class="{ 'tw-text-gray-400': !row.alias }">
              {{ row.alias || '-' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="上级分类" width="120" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.parentName || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="分类级别" width="100" align="center">
          <template #default="{ row }">
            <el-tag size="small" effect="plain">
              {{ row.levelName }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="70" align="center" />
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag
              :type="row.status === 'ACTIVE' ? 'success' : 'danger'"
              size="small"
              effect="plain"
            >
              {{ row.status === 'ACTIVE' ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="memo" label="备注" width="140" show-overflow-tooltip />
        <el-table-column prop="updatedAt" label="更新时间" min-width="160" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="openEdit(row)">编辑别名</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="tw-mt-4 tw-flex">
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next"
          background
          @current-change="onPageChange"
          @size-change="onPageSizeChange"
        />
      </div>
    </div>

    <!-- ===== 编辑别名弹窗 ===== -->
    <el-dialog
      v-model="dialog.visible"
      title="编辑自定义名称"
      width="460px"
      :close-on-click-modal="false"
    >
      <el-form :model="form" label-width="100px">
        <el-form-item label="分类编码">
          <span class="tw-text-[--text-secondary]">{{ form.id }}</span>
        </el-form-item>
        <el-form-item label="分类名称">
          <span class="tw-text-[--text-secondary]">{{ form.name }}</span>
        </el-form-item>
        <el-form-item label="分类级别">
          <el-tag size="small" effect="plain">{{ form.levelName }}</el-tag>
        </el-form-item>
        <el-form-item label="自定义名称" prop="alias">
          <el-input
            v-model="form.alias"
            placeholder="输入自定义名称（可为空）"
            maxlength="32"
            clearable
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
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  getCategoryPage,
  updateCategoryAlias,
  type CategoryViewVO,
} from '@/api/modules/category'

// ===== 搜索条件 =====
const search = reactive({
  level: '' as number | '',
  code: '',
  name: '',
})

// ===== 表格数据 =====
const loading = ref(false)
const submitting = ref(false)
const pageNum = ref(1)
const pageSize = ref(20)
const total = ref(0)
const tableData = ref<CategoryViewVO[]>([])

async function loadData() {
  loading.value = true
  try {
    const res = await getCategoryPage({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      level: search.level !== '' ? search.level as number : undefined,
      code: search.code || undefined,
      name: search.name || undefined,
    })
    if (res.code === 0) {
      tableData.value = res.data.records
      total.value = Number(res.data.total) || 0
    }
  } finally {
    loading.value = false
  }
}

function doSearch() {
  pageNum.value = 1
  loadData()
}

function onPageChange(_val: number) {
  loadData()
}

function onPageSizeChange(_val: number) {
  pageNum.value = 1
  loadData()
}

function resetSearch() {
  search.level = ''
  search.code = ''
  search.name = ''
  pageNum.value = 1
  loadData()
}

// ===== 编辑弹窗 =====
const dialog = reactive({ visible: false })

const form = reactive({
  id: '',
  name: '',
  levelName: '',
  alias: '' as string | null,
})

function openEdit(row: CategoryViewVO) {
  form.id = row.id
  form.name = row.name
  form.levelName = row.levelName
  form.alias = row.alias
  dialog.visible = true
}

async function submitForm() {
  submitting.value = true
  try {
    await updateCategoryAlias(form.id, { alias: form.alias || null })
    ElMessage.success('自定义名称已更新')
    dialog.visible = false
    loadData()
  } finally {
    submitting.value = false
  }
}

// ===== 初始化 =====
onMounted(() => {
  loadData()
})
</script>
