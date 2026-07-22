<template>
  <div class="tw-space-y-3">
    <!-- ===== 查询区 ===== -->
    <div
      class="tw-bg-white tw-rounded-[--border-radius-card] tw-px-5 tw-pt-4 tw-pb-1 tw-shadow-[--shadow-card]"
    >
      <el-form :model="search" inline>
        <el-form-item label="商品条码">
          <el-input
            v-model="search.barcode"
            placeholder="商品条码"
            clearable
            style="width: 150px"
            @keyup.enter="doSearch"
          />
        </el-form-item>
        <el-form-item label="商品名称">
          <el-input
            v-model="search.name"
            placeholder="商品名称"
            clearable
            style="width: 150px"
            @keyup.enter="doSearch"
          />
        </el-form-item>
        <el-form-item label="小类编号">
          <el-input
            v-model="search.categoryId"
            placeholder="小类编号"
            clearable
            style="width: 130px"
            @keyup.enter="doSearch"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select
            v-model="search.status"
            placeholder="全部"
            clearable
            style="width: 130px"
          >
            <el-option label="启用" value="ACTIVE" />
            <el-option label="停用" value="INACTIVE" />
            <el-option label="删除" value="DELETED" />
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
      <div class="tw-flex tw-justify-between tw-items-center tw-mb-4">
        <span class="tw-text-[13px] tw-text-[--text-secondary]">
          数据来源于 SAP 同步
        </span>
        <span class="tw-text-[13px] tw-text-[--text-secondary]">共 {{ total }} 条记录</span>
      </div>

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
        <el-table-column prop="barcode" label="商品条码" min-width="130" />
        <el-table-column prop="name" label="商品名称" min-width="150" show-overflow-tooltip />
        <el-table-column prop="categoryId" label="小类编号" width="100" />
        <el-table-column prop="spec" label="规格" width="110" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.spec || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="originPlace" label="产地" width="110" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.originPlace || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="brand" label="品牌" width="110" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.brand || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="订购单位" width="90" align="center">
          <template #default="{ row }">
            <el-tag size="small" effect="plain">{{ row.unit1 }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="结算单位" width="90" align="center">
          <template #default="{ row }">
            <el-tag size="small" effect="plain" type="info">{{ row.unit2 }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="单位重量(kg)" width="110" align="center">
          <template #default="{ row }">
            {{ row.unitWeight ?? '-' }}
          </template>
        </el-table-column>
        <el-table-column label="保质天数" width="90" align="center">
          <template #default="{ row }">
            {{ row.qualityDays ?? '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="seasonFactor" label="季节因子" width="90" align="center">
          <template #default="{ row }">
            {{ row.seasonFactor || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag
              :type="statusTagType(row.status)"
              size="small"
              effect="plain"
            >
              {{ statusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.remark || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="updatedAt" label="更新时间" min-width="160" />
      </el-table>

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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getProductPage, type ProductViewVO } from '@/api/modules/product'

// ===== 搜索条件 =====
const search = reactive({
  barcode: '',
  name: '',
  categoryId: '',
  status: '',
})

// ===== 表格数据 =====
const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const tableData = ref<ProductViewVO[]>([])

async function loadData() {
  loading.value = true
  try {
    const res = await getProductPage({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      barcode: search.barcode || undefined,
      name: search.name || undefined,
      categoryId: search.categoryId || undefined,
      status: search.status || undefined,
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

function onPageChange(val: number) {
  pageNum.value = val
  loadData()
}

function onPageSizeChange(val: number) {
  pageSize.value = val
  pageNum.value = 1
  loadData()
}

function resetSearch() {
  search.barcode = ''
  search.name = ''
  search.categoryId = ''
  search.status = ''
  pageNum.value = 1
  loadData()
}

function statusTagType(status: string): 'success' | 'danger' | 'info' {
  if (status === 'ACTIVE') return 'success'
  if (status === 'INACTIVE') return 'danger'
  return 'info'
}

function statusLabel(status: string): string {
  if (status === 'ACTIVE') return '启用'
  if (status === 'INACTIVE') return '停用'
  if (status === 'DELETED') return '删除'
  return status
}

// ===== 初始化 =====
onMounted(() => {
  loadData()
})
</script>
