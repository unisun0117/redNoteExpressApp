<template>
  <div class="tw-space-y-3">
    <!-- ===== 查询区 ===== -->
    <div class="tw-bg-white tw-rounded-[--border-radius-card] tw-px-5 tw-pt-4 tw-pb-2 tw-shadow-[--shadow-card]">
      <el-form :model="search" inline>
        <!-- 第一行 -->
        <el-form-item label="收支类型">
          <el-radio-group v-model="search.incomeExpenseType" @change="doSearch">
            <el-radio-button value="">全部</el-radio-button>
            <el-radio-button value="INCOME">收入</el-radio-button>
            <el-radio-button value="EXPENSE">支出</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="查询时间">
          <el-date-picker
            v-model="timeRange" type="datetimerange" range-separator="~"
            start-placeholder="开始" end-placeholder="结束" format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DDTHH:mm:ss" style="width: 360px" @change="doSearch"
          />
        </el-form-item>
        <el-form-item>
          <el-button size="small" @click="quickDate('today')">今日</el-button>
          <el-button size="small" @click="quickDate('yesterday')">昨日</el-button>
          <el-button size="small" @click="quickDate('7d')">7日</el-button>
          <el-button size="small" @click="quickDate('30d')">30日</el-button>
        </el-form-item>
        <el-form-item v-if="!collapsed" label="付款方式">
          <el-select v-model="search.paymentMethod" placeholder="全部" clearable style="width: 120px" @change="doSearch">
            <el-option label="微信" value="WECHAT" />
            <el-option label="预付款" value="PREPAID" />
            <el-option label="账期" value="CREDIT" />
            <el-option label="银行卡" value="BANK_CARD" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="!collapsed" label="账务类型">
          <el-select v-model="search.transactionType" placeholder="全部" clearable style="width: 120px" @change="doSearch">
            <el-option label="充值" value="RECHARGE" />
            <el-option label="提现" value="WITHDRAW" />
          </el-select>
        </el-form-item>

        <!-- 第二行（可折叠） -->
        <template v-if="!collapsed">
          <br />
          <el-form-item label="客户信息">
            <el-input v-model="search.customerKeyword" placeholder="客户名称/编码" clearable style="width: 180px" @keyup.enter="doSearch" />
          </el-form-item>
          <el-form-item label="结算账户">
            <el-input v-model="search.settlementKeyword" placeholder="账户名称/编号" clearable style="width: 180px" @keyup.enter="doSearch" />
          </el-form-item>
          <el-form-item label="第三方流水号">
            <el-input v-model="search.thirdPartyFlowNo" placeholder="微信/支付宝流水号" clearable style="width: 180px" @keyup.enter="doSearch" />
          </el-form-item>
          <el-form-item label="业务单号">
            <el-input v-model="search.businessNo" placeholder="业务单号" clearable style="width: 160px" @keyup.enter="doSearch" />
          </el-form-item>
          <el-form-item label="流水号">
            <el-input v-model="search.flowNo" placeholder="流水号" clearable style="width: 160px" @keyup.enter="doSearch" />
          </el-form-item>
          <el-form-item label="订单号">
            <el-input v-model="search.orderNo" placeholder="订单号" clearable style="width: 160px" @keyup.enter="doSearch" />
          </el-form-item>
          <el-form-item label="操作人">
            <el-input v-model="search.operatorKeyword" placeholder="姓名/手机" clearable style="width: 160px" @keyup.enter="doSearch" />
          </el-form-item>
        </template>

        <el-form-item>
          <el-button type="primary" @click="doSearch">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
          <el-button text type="primary" @click="collapsed = !collapsed">
            {{ collapsed ? '展开' : '收起' }} <el-icon><component :is="collapsed ? 'ArrowDown' : 'ArrowUp'" /></el-icon>
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- ===== 汇总卡片 ===== -->
    <div class="tw-grid tw-grid-cols-2 tw-gap-4">
      <div class="tw-bg-white tw-rounded-[--border-radius-card] tw-px-6 tw-py-4 tw-shadow-[--shadow-card] tw-flex tw-justify-between tw-items-center">
        <div>
          <div class="tw-text-sm tw-text-[--text-secondary]">收入</div>
          <div class="tw-text-2xl tw-font-bold tw-text-green-600">+{{ formatMoney(summary.incomeAmount) }} 元</div>
          <div class="tw-text-xs tw-text-[--text-secondary] tw-mt-1">
            {{ summary.incomeCount }} 笔
            <el-button link type="primary" size="small" @click="search.incomeExpenseType = 'INCOME'; doSearch()">详细</el-button>
          </div>
        </div>
      </div>
      <div class="tw-bg-white tw-rounded-[--border-radius-card] tw-px-6 tw-py-4 tw-shadow-[--shadow-card] tw-flex tw-justify-between tw-items-center">
        <div>
          <div class="tw-text-sm tw-text-[--text-secondary]">支出</div>
          <div class="tw-text-2xl tw-font-bold tw-text-red-600">-{{ formatMoney(summary.expenseAmount) }} 元</div>
          <div class="tw-text-xs tw-text-[--text-secondary] tw-mt-1">
            {{ summary.expenseCount }} 笔
            <el-button link type="primary" size="small" @click="search.incomeExpenseType = 'EXPENSE'; doSearch()">详细</el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- ===== 数据区 ===== -->
    <div class="tw-bg-white tw-rounded-[--border-radius-card] tw-p-5 tw-shadow-[--shadow-card]">
      <!-- 工具栏 -->
      <div class="tw-flex tw-justify-between tw-items-center tw-mb-4">
        <div class="tw-flex tw-gap-2">
          <el-button @click="exportData">导出</el-button>
        </div>
        <span class="tw-text-[13px] tw-text-[--text-secondary]">共 {{ total }} 条记录</span>
      </div>

      <!-- 表格 -->
      <el-table v-loading="loading" :data="tableData" stripe style="width: 100%"
        :header-cell-style="{ background: '#F1F5F9', color: '#475569', fontWeight: 600, fontSize: '13px' }">
        <el-table-column type="selection" width="40" />
        <el-table-column prop="createdAt" label="入账时间" width="160" />
        <el-table-column prop="transactionNo" label="流水号" min-width="130" show-overflow-tooltip />
        <el-table-column label="账务类型" width="80" align="center">
          <template #default="{ row }">{{ typeLabel(row.transactionType) }}</template>
        </el-table-column>
        <el-table-column label="付款方式" width="80" align="center">
          <template #default="{ row }">{{ methodLabel(row.paymentMethod) }}</template>
        </el-table-column>
        <el-table-column label="客户" min-width="130" show-overflow-tooltip>
          <template #default="{ row }">{{ row.customerName || row.customerCode || '-' }}</template>
        </el-table-column>
        <el-table-column label="结算账户" min-width="120" show-overflow-tooltip>
          <template #default="{ row }">{{ row.settlementAccountName || row.settlementAccountCode || '-' }}</template>
        </el-table-column>
        <el-table-column label="操作人" width="90" show-overflow-tooltip>
          <template #default="{ row }">{{ row.operatorName || '-' }}</template>
        </el-table-column>
        <el-table-column label="收支金额" width="130" align="right">
          <template #default="{ row }">
            <span :class="row.incomeExpenseType === 'INCOME' ? 'tw-text-green-600' : 'tw-text-red-600'" class="tw-font-semibold">
              {{ row.incomeExpenseType === 'INCOME' ? '+' : '-' }}{{ formatMoney(row.amount) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="账户余额" width="120" align="right">
          <template #default="{ row }">{{ row.balanceAfter != null ? formatMoney(row.balanceAfter) : '-' }}</template>
        </el-table-column>
        <el-table-column prop="businessNo" label="业务单号" min-width="120" show-overflow-tooltip />
        <el-table-column prop="orderNo" label="订单号" min-width="120" show-overflow-tooltip />
        <el-table-column prop="thirdPartyFlowNo" label="第三方流水号" min-width="150" show-overflow-tooltip />
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 'SUCCESS' ? 'success' : row.status === 'FAILED' ? 'danger' : 'warning'" size="small" effect="plain">
              {{ statusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="120" show-overflow-tooltip>
          <template #default="{ row }">{{ row.remark || '-' }}</template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无流水记录" :image-size="100" />
        </template>
      </el-table>

      <div class="tw-mt-4 tw-flex tw-justify-center" v-if="total > 0">
        <el-pagination v-model:current-page="pageNum" v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50]" :total="total" layout="total, sizes, prev, pager, next" background />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, watch, onMounted } from 'vue'
import { ArrowDown, ArrowUp } from '@element-plus/icons-vue'
import {
  getTransactionPage, getTransactionSummary,
  typeLabel, methodLabel, statusLabel,
  type AccountTransactionViewVO,
} from '@/api/modules/transaction'

// ===== 搜索 =====
const collapsed = ref(true)
const search = reactive({
  incomeExpenseType: '',
  paymentMethod: '',
  transactionType: '',
  customerKeyword: '',
  settlementKeyword: '',
  thirdPartyFlowNo: '',
  businessNo: '',
  flowNo: '',
  orderNo: '',
  operatorKeyword: '',
})
const timeRange = ref<[string, string] | null>(null)

// ===== 表格 =====
const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(20)
const total = ref(0)
const tableData = ref<AccountTransactionViewVO[]>([])
const summary = reactive({ incomeAmount: 0, incomeCount: 0, expenseAmount: 0, expenseCount: 0 })

function formatMoney(v: number | null) {
  if (v == null) return '0.00'
  return Number(v).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

function quickDate(opt: string) {
  const now = new Date()
  let start: Date, end: Date = now
  switch (opt) {
    case 'today': start = new Date(now.getFullYear(), now.getMonth(), now.getDate()); break
    case 'yesterday':
      end = new Date(now.getFullYear(), now.getMonth(), now.getDate(), 0, 0, 0)
      start = new Date(end.getTime() - 86400000); break
    case '7d': start = new Date(now.getTime() - 7 * 86400000); break
    case '30d': start = new Date(now.getTime() - 30 * 86400000); break
    default: return
  }
  const fmt = (d: Date) => {
    const pad = (n: number) => String(n).padStart(2, '0')
    return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
  }
  timeRange.value = [fmt(start), fmt(end)]
  doSearch()
}

async function loadData() {
  loading.value = true
  try {
    const params: Record<string, unknown> = {
      pageNum: pageNum.value, pageSize: pageSize.value,
      incomeExpenseType: search.incomeExpenseType || undefined,
      paymentMethod: search.paymentMethod || undefined,
      transactionType: search.transactionType || undefined,
      customerKeyword: search.customerKeyword || undefined,
      settlementKeyword: search.settlementKeyword || undefined,
      thirdPartyFlowNo: search.thirdPartyFlowNo || undefined,
      businessNo: search.businessNo || undefined,
      flowNo: search.flowNo || undefined,
      orderNo: search.orderNo || undefined,
      operatorKeyword: search.operatorKeyword || undefined,
    }
    if (timeRange.value) {
      params.startTime = timeRange.value[0]
      params.endTime = timeRange.value[1]
    }
    const [listRes, sumRes] = await Promise.all([
      getTransactionPage(params as any),
      getTransactionSummary(Object.fromEntries(Object.entries(params).filter(([k]) => k !== 'pageNum' && k !== 'pageSize')) as any),
    ])
    if (listRes.code === 0) {
      tableData.value = listRes.data.records
      total.value = listRes.data.total
    }
    if (sumRes.code === 0) {
      Object.assign(summary, sumRes.data)
    }
  } catch { /* 拦截器统一处理 */ }
  finally { loading.value = false }
}

function doSearch() { pageNum.value = 1; loadData() }

function resetSearch() {
  search.incomeExpenseType = ''; search.paymentMethod = ''; search.transactionType = ''
  search.customerKeyword = ''; search.settlementKeyword = ''
  search.thirdPartyFlowNo = ''; search.businessNo = ''; search.flowNo = ''; search.orderNo = ''
  search.operatorKeyword = ''; timeRange.value = null
  pageNum.value = 1; loadData()
}

function exportData() {
  alert('导出功能对接后端 /api/admin/order/transaction/export')
}

watch([pageNum, pageSize], () => loadData())
onMounted(() => loadData())
</script>
