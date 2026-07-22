<template>
  <div class="tw-space-y-3">
    <!-- ===== 查询区 ===== -->
    <div class="tw-bg-white tw-rounded-[--border-radius-card] tw-px-5 tw-pt-4 tw-shadow-[--shadow-card]">
      <el-form :model="search" inline>
        <el-form-item label="公司名称">
          <el-input
            v-model="search.companyName"
            placeholder="公司名称"
            clearable
            style="width: 180px"
            @keyup.enter="doSearch"
          />
        </el-form-item>
        <el-form-item label="销售大区">
          <el-select v-model="search.salesRegionName" placeholder="全部" clearable style="width: 160px">
            <el-option
              v-for="r in salesRegionOptions"
              :key="r"
              :label="r"
              :value="r"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="所在地区">
          <el-cascader
            v-model="search.region"
            :options="chinaAreaOptions"
            :props="{ value: 'label', emitPath: true }"
            clearable
            filterable
            placeholder="全部"
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="业务员">
          <el-input
            v-model="search.salesmanName"
            placeholder="业务员姓名"
            clearable
            style="width: 130px"
          />
        </el-form-item>
        <el-form-item label="审核状态">
          <el-select v-model="search.auditStatus" placeholder="全部" clearable style="width: 110px">
            <el-option label="待审核" value="PENDING" />
            <el-option label="已通过" value="APPROVED" />
            <el-option label="已驳回" value="REJECTED" />
          </el-select>
        </el-form-item>
        <el-form-item label="审核人">
          <el-input
            v-model="search.auditorName"
            placeholder="审核人姓名"
            clearable
            style="width: 130px"
          />
        </el-form-item>
        <el-form-item label="注册时间">
          <el-date-picker
            v-model="search.dateRange"
            type="daterange"
            range-separator="~"
            start-placeholder="开始"
            end-placeholder="结束"
            value-format="YYYY-MM-DD"
            style="width: 240px"
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
      <div class="tw-flex tw-justify-between tw-items-center tw-mb-4">
        <el-button type="primary" @click="addVisible = true">新增客户档案</el-button>
        <span class="tw-text-[13px] tw-text-[--text-secondary]">共 {{ total }} 条记录</span>
      </div>

      <el-table
        v-loading="loading"
        element-loading-text="加载中..."
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
        <el-table-column label="SAP客户编码" width="130">
          <template #default="{ row }">
            <span>{{ row.sapCustomerCode || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="客户公司名称" width="180" show-overflow-tooltip>
          <template #default="{ row }">
            <span
              class="tw-cursor-pointer tw-text-[--color-primary] hover:tw-underline"
              @click="viewDetail(row)"
            >{{ row.companyName }}</span>
          </template>
        </el-table-column>
        <el-table-column label="已绑定账号" width="100" align="center">
          <template #default="{ row }">
            <span v-if="row.boundUserCount > 0" class="tw-text-[--color-primary] tw-cursor-pointer" @click="openBind(row)">
              {{ row.boundUserCount }} 人
            </span>
            <span v-else class="tw-text-gray-300">0</span>
          </template>
        </el-table-column>
        <el-table-column label="销售大区" width="100">
          <template #default="{ row }">{{ row.salesRegionName || '-' }}</template>
        </el-table-column>
        <el-table-column label="收货人" width="90">
          <template #default="{ row }">{{ row.contactName }}</template>
        </el-table-column>
        <el-table-column label="联系电话" width="120">
          <template #default="{ row }">{{ maskMobile(row.contactPhone) }}</template>
        </el-table-column>
        <el-table-column label="所在地区" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.province }} {{ row.city }} {{ row.district }} {{ row.address }}
          </template>
        </el-table-column>
        <el-table-column label="审核状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="statusType(row.auditStatus)" size="small" effect="plain">
              {{ statusText(row.auditStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="价格组" width="100">
          <template #default="{ row }">{{ row.priceGroup || '-' }}</template>
        </el-table-column>
        <el-table-column label="结算公司" width="130" show-overflow-tooltip>
          <template #default="{ row }">{{ row.settleCompany || '-' }}</template>
        </el-table-column>
        <el-table-column label="经营类型" width="100">
          <template #default="{ row }">{{ row.businessType || '-' }}</template>
        </el-table-column>
        <el-table-column label="结算类型" width="90" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.settleType" :type="row.settleType === 'CASH' ? '' : 'warning'" size="small" effect="plain">
              {{ row.settleType === 'CASH' ? '现结' : '账期' }}
            </el-tag>
            <span v-else class="tw-text-gray-300">-</span>
          </template>
        </el-table-column>
        <el-table-column label="业务员" width="90">
          <template #default="{ row }">{{ row.salesmanName || '-' }}</template>
        </el-table-column>
        <el-table-column label="审核处理人" width="100">
          <template #default="{ row }">{{ row.auditorName || '-' }}</template>
        </el-table-column>
        <el-table-column label="最近下单时间" width="150">
          <template #default="{ row }">{{ row.lastOrderTime || '-' }}</template>
        </el-table-column>
        <el-table-column label="注册提交时间" width="150">
          <template #default="{ row }">{{ row.createdAt }}</template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="viewDetail(row)">查看详情</el-button>
            <el-button v-if="row.auditStatus === 'APPROVED'" type="primary" link @click="openEdit(row)">
              编辑
            </el-button>
            <el-button v-if="row.auditStatus === 'PENDING'" type="warning" link @click="openAssign(row)">
              分配审核人
            </el-button>
            <el-button v-if="row.auditStatus === 'APPROVED'" type="primary" link @click="openBind(row)">
              绑定账号
            </el-button>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无客户档案数据" :image-size="100" />
        </template>
      </el-table>

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

    <!-- ===== 弹窗组件 ===== -->
    <AddDialog v-model:visible="addVisible" @success="loadData" />
    <EditDialog
      v-model:visible="editVisible"
      :archive-id="editRow?.id ?? null"
      :row="editRow"
      @success="loadData"
    />
    <AssignDialog
      v-model:visible="assignVisible"
      :archive-id="assignArchiveId"
      :company-name="assignCompanyName"
      :current-auditor="assignCurrentAuditor"
      @success="loadData"
    />
    <BindDialog
      v-model:visible="bindVisible"
      :archive-id="bindArchiveId"
      :company-name="bindCompanyName"
      @success="loadData"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, watch, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getCustomerArchivePage, type CustomerArchiveSummaryVO } from '@/api/modules/customer-archive'
import { getAllSalesRegions } from '@/api/modules/operation'
import { chinaAreaOptions } from '@/utils/chinaArea'
import AddDialog from './components/AddDialog.vue'
import EditDialog from './components/EditDialog.vue'
import AssignDialog from './components/AssignDialog.vue'
import BindDialog from './components/BindDialog.vue'

const router = useRouter()

// ===== 销售大区选项（来自 /operation/sales-region） =====
const salesRegionOptions = ref<string[]>([])

onMounted(async () => {
  try {
    const res = await getAllSalesRegions()
    if ((res.code === 0 || res.code === 200) && res.data.records) {
      salesRegionOptions.value = res.data.records.map((r) => r.name)
    }
  } catch { /* 兜底为空 */ }
})

// ===== 查询条件 =====
const search = reactive({
  companyName: '',
  salesRegionName: '',
  region: [] as string[],
  salesmanName: '',
  auditStatus: '',
  auditorName: '',
  dateRange: [] as string[],
})

// ===== 表格 =====
const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(20)
const total = ref(0)
const tableData = ref<CustomerArchiveSummaryVO[]>([])

function maskMobile(mobile: string | null): string {
  if (!mobile || mobile.length < 11) return mobile || '-'
  return `${mobile.slice(0, 3)}****${mobile.slice(7)}`
}

async function loadData() {
  loading.value = true
  try {
    const params: Record<string, unknown> = {
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      companyName: search.companyName || undefined,
      salesRegionName: search.salesRegionName || undefined,
      salesmanName: search.salesmanName || undefined,
      auditStatus: search.auditStatus || undefined,
      auditorName: search.auditorName || undefined,
    }
    if (search.region && search.region.length >= 1) params.province = search.region[0]
    if (search.region && search.region.length >= 2) params.city = search.region[1]
    if (search.region && search.region.length >= 3) params.district = search.region[2]
    if (search.dateRange && search.dateRange.length === 2) {
      params.startDate = search.dateRange[0]
      params.endDate = search.dateRange[1]
    }
    const res = await getCustomerArchivePage(params)
    if (res.code === 0 || res.code === 200) {
      tableData.value = res.data.records
      total.value = res.data.total
    }
  } finally {
    loading.value = false
  }
}

function doSearch() { pageNum.value = 1; loadData() }
function resetSearch() {
  search.companyName = ''; search.salesRegionName = ''; search.region = []
  search.salesmanName = ''; search.auditStatus = ''; search.auditorName = ''
  search.dateRange = []; pageNum.value = 1; loadData()
}
watch([pageNum, pageSize], () => loadData())

function statusType(s: string): '' | 'success' | 'warning' | 'danger' | 'info' {
  return { APPROVED: 'success', PENDING: 'warning', REJECTED: 'danger' }[s] as never || 'info'
}
function statusText(s: string): string {
  return { APPROVED: '已通过', PENDING: '待审核', REJECTED: '已驳回' }[s] || s
}

function viewDetail(row: CustomerArchiveSummaryVO) {
  router.push({ name: 'CustomerArchiveDetail', params: { id: row.id } })
}

// ===== 弹窗状态控制 =====

// 新增
const addVisible = ref(false)

// 编辑
const editVisible = ref(false)
const editRow = ref<CustomerArchiveSummaryVO | null>(null)

function openEdit(row: CustomerArchiveSummaryVO) {
  editRow.value = row
  editVisible.value = true
}

// 分配
const assignVisible = ref(false)
const assignArchiveId = ref<number | null>(null)
const assignCompanyName = ref('')
const assignCurrentAuditor = ref('')

function openAssign(row: CustomerArchiveSummaryVO) {
  assignArchiveId.value = row.id
  assignCompanyName.value = row.companyName
  assignCurrentAuditor.value = row.auditorName || ''
  assignVisible.value = true
}

// 绑定
const bindVisible = ref(false)
const bindArchiveId = ref<number | null>(null)
const bindCompanyName = ref('')

function openBind(row: CustomerArchiveSummaryVO) {
  bindArchiveId.value = row.id
  bindCompanyName.value = row.companyName
  bindVisible.value = true
}

loadData()
</script>
