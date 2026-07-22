<template>
  <div class="tw-space-y-3">
    <!-- ===== 查询区 ===== -->
    <div class="tw-bg-white tw-rounded-[--border-radius-card] tw-px-5 tw-pt-4 tw-pb-1 tw-shadow-[--shadow-card]">
      <el-form :model="search" inline>
        <el-form-item label="客户信息">
          <el-input v-model="search.keyword" placeholder="客户编号/名称" clearable style="width: 200px" @keyup.enter="doSearch" />
        </el-form-item>
        <el-form-item label="营业执照编号">
          <el-input v-model="search.licenseNo" placeholder="统一社会信用代码" clearable style="width: 200px" @keyup.enter="doSearch" />
        </el-form-item>
        <el-form-item label="账户类型">
          <el-select v-model="search.accountType" placeholder="全部" clearable style="width: 140px">
            <el-option label="预付款" value="PREPAID" />
            <el-option label="账期" value="CREDIT" />
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
        <el-button type="primary" @click="openAdd">新增</el-button>
        <span class="tw-text-[13px] tw-text-[--text-secondary]">共 {{ total }} 条记录</span>
      </div>

      <el-table v-loading="loading" :data="tableData" stripe style="width: 100%"
        :header-cell-style="{ background: '#F1F5F9', color: '#475569', fontWeight: 600, fontSize: '13px' }">
        <el-table-column type="selection" width="50" />
        <el-table-column label="序号" width="60" align="center">
          <template #default="{ $index }">{{ (pageNum - 1) * pageSize + $index + 1 }}</template>
        </el-table-column>
        <el-table-column prop="customerCode" label="客户编号" min-width="110" show-overflow-tooltip />
        <el-table-column prop="customerName" label="客户名称" min-width="130" show-overflow-tooltip />
        <el-table-column label="账户金额" min-width="130" align="right">
          <template #default="{ row }">
            <span class="tw-font-semibold">{{ formatMoney(row.balance) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="账户类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.accountType === 'PREPAID' ? '' : 'warning'" size="small" effect="plain">
              {{ row.accountType === 'PREPAID' ? '预付款' : '账期' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="账期天数" width="100" align="center">
          <template #default="{ row }">{{ row.creditDays || '-' }}</template>
        </el-table-column>
        <el-table-column label="下一对账日期" min-width="120" align="center">
          <template #default="{ row }">{{ row.nextReconciliationDate || '-' }}</template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="140" show-overflow-tooltip>
          <template #default="{ row }">{{ row.remark || '-' }}</template>
        </el-table-column>
        <el-table-column prop="licenseNo" label="营业执照编号" min-width="180" show-overflow-tooltip />
        <el-table-column prop="companyName" label="公司名称" min-width="160" show-overflow-tooltip />
        <el-table-column label="营业执照照片" min-width="100" align="center">
          <template #default="{ row }">
            <el-button v-if="row.licensePhoto" link type="primary" @click="viewLicense(row)">
              <el-icon><Picture /></el-icon>
            </el-button>
            <span v-else class="tw-text-[--text-secondary]">-</span>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无账户数据" :image-size="100" />
        </template>
      </el-table>

      <div class="tw-mt-4 tw-flex tw-justify-center" v-if="total > 0">
        <el-pagination v-model:current-page="pageNum" v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50]" :total="total" layout="total, sizes, prev, pager, next" background />
      </div>
    </div>

    <!-- ===== 新增弹窗 ===== -->
    <el-dialog v-model="dialog.visible" title="新增" width="500px" :close-on-click-modal="false" @close="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="客户编码" prop="customerCode" required>
          <el-input v-model="form.customerCode" placeholder="客户编码" maxlength="50" @blur="queryCustomer">
            <template #prepend><span class="tw-font-bold tw-text-[--color-primary]">1</span></template>
          </el-input>
        </el-form-item>
        <el-form-item label="客户名称" prop="customerName">
          <el-input v-model="form.customerName" placeholder="根据编码自动带出" disabled />
        </el-form-item>
        <el-form-item label="账户类型" prop="accountType" required>
          <el-radio-group v-model="form.accountType">
            <el-radio value="PREPAID">预充值</el-radio>
            <el-radio value="CREDIT">账期</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="流水类型" prop="transactionType" required>
          <el-radio-group v-model="form.transactionType">
            <el-radio value="RECHARGE">充值</el-radio>
            <el-radio value="WITHDRAW">提现</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="流水金额" prop="amount" required>
          <el-input-number v-model="form.amount" :min="0.01" :precision="2" style="width: 100%" placeholder="请输入金额">
            <template #prepend><span class="tw-font-bold tw-text-[--color-primary]">2</span></template>
          </el-input-number>
          <div class="tw-text-xs tw-text-[--text-secondary] tw-mt-1">单位：元</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="dialog.submitting" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, watch, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { Picture } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import {
  getAccountPage,
  getCustomerByCode,
  createTransaction,
  type AccountSummaryVO,
  type AccountType,
  type TransactionType,
} from '@/api/modules/account'

// ===== 搜索条件 =====
const search = reactive({
  keyword: '',
  licenseNo: '',
  accountType: '' as '' | AccountType,
})

// ===== 表格数据 =====
const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(20)
const total = ref(0)
const tableData = ref<AccountSummaryVO[]>([])

function formatMoney(val: number | null): string {
  if (val == null) return '0.00'
  return Number(val).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

function viewLicense(row: AccountSummaryVO) {
  if (row.licensePhoto) {
    window.open(row.licensePhoto, '_blank')
  } else {
    ElMessage.info('暂无营业执照照片')
  }
}

async function loadData() {
  loading.value = true
  try {
    const res = await getAccountPage({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      keyword: search.keyword || undefined,
      licenseNo: search.licenseNo || undefined,
      accountType: search.accountType || undefined,
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
  search.licenseNo = ''
  search.accountType = ''
  pageNum.value = 1
  loadData()
}

watch([pageNum, pageSize], () => {
  loadData()
})

// ===== 新增弹窗 =====
interface FormData {
  customerCode: string
  customerName: string
  accountType: AccountType
  transactionType: TransactionType
  amount: number | null
}

const dialog = reactive({ visible: false, submitting: false })
const formRef = ref<FormInstance | null>(null)

const defaultForm = (): FormData => ({
  customerCode: '',
  customerName: '',
  accountType: 'PREPAID',
  transactionType: 'RECHARGE',
  amount: null,
})

const form = reactive<FormData>(defaultForm())

const rules: FormRules = {
  customerCode: [{ required: true, message: '请输入客户编码', trigger: 'blur' }],
  accountType: [{ required: true, message: '请选择账户类型', trigger: 'change' }],
  transactionType: [{ required: true, message: '请选择流水类型', trigger: 'change' }],
  amount: [{ required: true, message: '请输入流水金额', trigger: 'blur' }],
}

async function queryCustomer() {
  if (!form.customerCode) {
    form.customerName = ''
    return
  }
  try {
    const res = await getCustomerByCode(form.customerCode.trim())
    if (res.code === 0 && res.data) {
      form.customerName = res.data.customerName
    } else {
      form.customerName = ''
    }
  } catch {
    form.customerName = ''
  }
}

function openAdd() {
  dialog.submitting = false
  resetForm()
  dialog.visible = true
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

    if (!form.customerName) {
      ElMessage.warning('客户编码不存在，请确认后重新输入')
      return
    }

    dialog.submitting = true
    try {
      await createTransaction({
        customerCode: form.customerCode.trim(),
        accountType: form.accountType,
        transactionType: form.transactionType,
        amount: form.amount as number,
      })
      ElMessage.success('流水新增成功')
      dialog.visible = false
      loadData()
    } catch {
      // 错误已由拦截器统一提示
    } finally {
      dialog.submitting = false
    }
  })
}

loadData()
</script>