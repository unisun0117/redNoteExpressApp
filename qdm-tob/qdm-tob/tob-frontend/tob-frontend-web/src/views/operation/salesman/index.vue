<template>
  <div class="tw-space-y-3">
    <el-tabs v-model="activeTab" type="border-card" class="tw-bg-white tw-rounded-[--border-radius-card] tw-shadow-[--shadow-card]">
      <!-- ==================== Tab 1: 业务员推荐码 ==================== -->
      <el-tab-pane label="销售员推荐码" name="referral">
        <!-- 查询区 -->
        <div class="tw-px-5 tw-pt-4 tw-pb-1">
          <el-form :model="refSearch" inline>
            <el-form-item label="销售员姓名">
              <el-input v-model="refSearch.name" placeholder="请输入姓名" clearable style="width:180px" />
            </el-form-item>
            <el-form-item label="手机号">
              <el-input v-model="refSearch.phone" placeholder="请输入手机号" clearable style="width:180px" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="doRefSearch">查询</el-button>
              <el-button @click="resetRefSearch">重置</el-button>
            </el-form-item>
          </el-form>
        </div>

        <!-- 数据区 -->
        <div class="tw-p-5">
          <div class="tw-flex tw-justify-start tw-mb-4">
            <el-button type="primary" @click="openAddDialog">添加销售员</el-button>
          </div>

          <el-table :data="refTableData" stripe v-loading="refLoading" style="width:100%"
            :header-cell-style="{ background: '#F1F5F9', color: '#475569', fontWeight: 600, fontSize: '13px' }">
            <el-table-column prop="name" label="销售员姓名" min-width="100" />
            <el-table-column label="销售员手机号" min-width="130">
              <template #default="{ row }">
                <span style="color:#909399;">{{ maskPhone(row.phone) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="销售客户数" min-width="100" align="center">
              <template #default="{ row }">
                <el-tag type="info" size="small">{{ row.customerCount }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="推荐码" min-width="100" align="center">
              <template #default="{ row }">
                <el-tag v-if="row.codeStatus === 'VALID'" type="success" size="small" effect="plain">{{ row.referralCode }}</el-tag>
                <span v-else-if="row.codeStatus === 'EMPTY'" style="color:#909399;">—</span>
                <el-tag v-else type="danger" size="small" effect="plain">{{ row.referralCode }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="updatedBy" label="修改人" min-width="80" />
            <el-table-column prop="updatedAt" label="最后修改时间" min-width="150" />
            <el-table-column label="操作" min-width="200" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" link size="small" @click="handleRegenerate(row)">重新生成</el-button>
                <el-button type="warning" link size="small" @click="handleClear(row)" :disabled="row.codeStatus !== 'VALID'">置空</el-button>
                <el-button type="danger" link size="small" @click="handleDelete(row)" :disabled="row.codeStatus !== 'VALID'">删除</el-button>
              </template>
            </el-table-column>
          </el-table>

          <div v-if="refTableData.length === 0 && !refLoading" class="tw-text-center tw-py-16 tw-text-gray-400">
            <div class="tw-text-5xl tw-mb-3">👤</div>
            <div>暂无业务员</div>
          </div>

          <div class="tw-mt-4 tw-flex">
            <el-pagination v-model:current-page="refPageNum" v-model:page-size="refPageSize"
              :page-sizes="[10, 20, 50]" :total="refTotal"
              layout="total, sizes, prev, pager, next" background />
          </div>
        </div>
      </el-tab-pane>

      <!-- ==================== Tab 2: 销售员绩效配置 ==================== -->
      <el-tab-pane label="销售员绩效配置" name="performance">
        <!-- 查询区 -->
        <div class="tw-px-5 tw-pt-4 tw-pb-1">
          <el-form :model="perfSearch" inline>
            <el-form-item label="销售员">
              <el-select v-model="perfSearch.salesmanId" placeholder="全部" clearable style="width:180px">
                <el-option v-for="s in allSalesmen" :key="s.id" :label="s.name" :value="s.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="月度">
              <el-date-picker v-model="perfSearch.month" type="month" value-format="YYYY-MM"
                placeholder="选择月度" clearable style="width:160px" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="doPerfSearch">查询</el-button>
              <el-button @click="resetPerfSearch">重置</el-button>
            </el-form-item>
          </el-form>
        </div>

        <!-- 数据区 -->
        <div class="tw-p-5">
          <div class="tw-flex tw-justify-start tw-mb-4 tw-gap-2">
            <el-button type="primary" @click="openPerfForm(null)">添加绩效</el-button>
          </div>

          <el-table :data="perfTableData" stripe v-loading="perfLoading" style="width:100%"
            :header-cell-style="{ background: '#F1F5F9', color: '#475569', fontWeight: 600, fontSize: '13px' }">
            <el-table-column prop="salesmanName" label="销售员" min-width="100" />
            <el-table-column prop="month" label="月度" min-width="100" align="center" />
            <el-table-column prop="orderCount" label="订单数" min-width="90" align="center" />
            <el-table-column prop="customerCount" label="下单客户数" min-width="100" align="center" />
            <el-table-column label="销售额（元）" min-width="130" align="right">
              <template #default="{ row }">
                {{ formatMoney(row.salesAmount) }}
              </template>
            </el-table-column>
            <el-table-column prop="updatedBy" label="修改人" min-width="80" />
            <el-table-column prop="updatedAt" label="最后修改时间" min-width="150" />
            <el-table-column label="操作" min-width="80" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" link size="small" @click="openPerfForm(row)">编辑</el-button>
              </template>
            </el-table-column>
          </el-table>

          <div v-if="perfTableData.length === 0 && !perfLoading" class="tw-text-center tw-py-16 tw-text-gray-400">
            <div class="tw-text-5xl tw-mb-3">📊</div>
            <div>暂无绩效数据</div>
          </div>

          <div class="tw-mt-4 tw-flex">
            <el-pagination v-model:current-page="perfPageNum" v-model:page-size="perfPageSize"
              :page-sizes="[10, 20, 50]" :total="perfTotal"
              layout="total, sizes, prev, pager, next" background />
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- ===== 添加业务员弹窗 ===== -->
    <el-dialog v-model="addDialog.visible" title="添加业务员" width="500px" :close-on-click-modal="false" align-center>
      <div v-if="!addDialog.hasUsers" class="tw-text-center tw-py-8 tw-text-gray-400">
        暂无可添加的用户（所有用户已添加为业务员）
      </div>
      <el-form v-else ref="addFormRef" :model="addForm" label-width="80px">
        <el-form-item label="选择用户" prop="userId" :rules="[{ required: true, message: '请选择用户' }]">
          <el-select v-model="addForm.userId" placeholder="搜索用户名/手机号" filterable style="width:100%"
            :loading="addDialog.searching" @focus="searchUsers('')">
            <el-option v-for="u in addDialog.userOptions" :key="u.id"
              :label="`${u.name}（${u.phone}）`" :value="String(u.id)" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addDialog.visible = false">取消</el-button>
        <el-button type="primary" @click="submitAddSalesman" :loading="addDialog.submitting">确认添加</el-button>
      </template>
    </el-dialog>

    <!-- ===== 绩效弹窗 ===== -->
    <el-dialog v-model="perfDialog.visible"
      :title="perfDialog.isEdit ? '编辑绩效' : '添加绩效'"
      width="520px" :close-on-click-modal="false" align-center @close="resetPerfForm">
      <el-form ref="perfFormRef" :model="perfForm" :rules="perfRules" label-width="110px">
        <el-form-item label="销售员" prop="salesmanId">
          <el-select v-model="perfForm.salesmanId" placeholder="请选择业务员"
            :disabled="perfDialog.isEdit" style="width:100%">
            <el-option v-for="s in allSalesmen" :key="s.id" :label="s.name" :value="s.id" />
          </el-select>
          <div v-if="allSalesmen.length === 0" class="tw-text-xs tw-text-gray-400 tw-mt-1">
            暂无业务员，请先在推荐码Tab中添加业务员
          </div>
        </el-form-item>
        <el-form-item label="月度" prop="month">
          <el-date-picker v-model="perfForm.month" type="month" value-format="YYYY-MM"
            placeholder="选择月度" :disabled="perfDialog.isEdit" style="width:100%" />
        </el-form-item>
        <el-form-item label="订单数" prop="orderCount">
          <el-input-number v-model="perfForm.orderCount" :min="0" :precision="0" style="width:100%" />
        </el-form-item>
        <el-form-item label="下单客户数" prop="customerCount">
          <el-input-number v-model="perfForm.customerCount" :min="0" :precision="0" style="width:100%" />
        </el-form-item>
        <el-form-item label="销售额（元）" prop="salesAmount">
          <el-input-number v-model="perfForm.salesAmount" :min="0" :precision="2" style="width:100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="perfDialog.visible = false">取消</el-button>
        <el-button type="primary" @click="submitPerfForm" :loading="perfDialog.submitting">
          确认{{ perfDialog.isEdit ? '保存' : '添加' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- ===== 确认弹窗（重新生成/置空/删除） ===== -->
    <el-dialog v-model="confirmDialog.visible" :title="confirmDialog.title"
      width="420px" :close-on-click-modal="false" align-center>
      <div class="tw-text-sm tw-text-gray-600 tw-leading-relaxed" v-html="confirmDialog.message" />
      <template #footer>
        <el-button @click="confirmDialog.visible = false">取消</el-button>
        <el-button type="primary" @click="confirmDialog.onConfirm" :loading="confirmDialog.loading">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, watch, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import {
  getSalesmanReferralList, addSalesman, regenerateCode, clearCode, deleteReferralCode,
  getAllSalesmen, getPerformanceList, addPerformance, editPerformance,
  type SalesmanReferralVO, type SalesmanPerformanceVO,
} from '@/api/modules/salesman'
import { getOperatorPage, type OperatorVO } from '@/api/modules/system'

// ===== Tab =====
const activeTab = ref('referral')

// ===== 工具方法 =====
function maskPhone(phone: string) {
  if (!phone) return '-'
  return phone.slice(0, 3) + '****' + phone.slice(7)
}
function formatMoney(val: number) {
  if (val == null) return '-'
  return Number(val).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}
function getCurrentUserName() {
  return localStorage.getItem('realName') || ''
}

// ==============================
// Tab 1: 业务员推荐码
// ==============================
const refSearch = reactive({ name: '', phone: '' })
const refPageNum = ref(1)
const refPageSize = ref(20)
const refTotal = ref(0)
const refLoading = ref(false)
const refTableData = ref<SalesmanReferralVO[]>([])

async function loadReferralData() {
  refLoading.value = true
  try {
    const res = await getSalesmanReferralList({
      pageNum: refPageNum.value, pageSize: refPageSize.value,
      name: refSearch.name || undefined, phone: refSearch.phone || undefined,
    })
    if (res.code === 0) {
      refTableData.value = res.data.records
      refTotal.value = res.data.total
    }
  } catch { /* handled by interceptor */ } finally { refLoading.value = false }
}
function doRefSearch() { refPageNum.value = 1; loadReferralData() }
function resetRefSearch() { refSearch.name = ''; refSearch.phone = ''; doRefSearch() }
watch([refPageNum, refPageSize], () => loadReferralData())

// 添加业务员弹窗
const addDialog = reactive({ visible: false, submitting: false, searching: false, hasUsers: true, userOptions: [] as { id: number; name: string; phone: string }[] })
const addFormRef = ref<FormInstance | null>(null)
const addForm = reactive({ userId: null as string | null })

async function searchUsers(_query: string) {
  addDialog.searching = true
  try {
    const res = await getOperatorPage({ page: 1, size: 999 })
    const existingIds = new Set(allSalesmen.value.map(s => s.userId))
    addDialog.userOptions = res.records
      .filter(u => !existingIds.has(String(u.id)))
      .map(u => ({ id: u.id, name: u.realName, phone: u.mobile }))
    addDialog.hasUsers = addDialog.userOptions.length > 0
  } catch { addDialog.userOptions = []; addDialog.hasUsers = false }
  finally { addDialog.searching = false }
}
function openAddDialog() {
  addForm.userId = null; addDialog.submitting = false
  addDialog.visible = true
  nextTick(() => { addFormRef.value?.clearValidate(); searchUsers('') })
}
async function submitAddSalesman() {
  if (!addFormRef.value) return
  addFormRef.value.validate(async (valid) => {
    if (!valid || addForm.userId == null) return
    addDialog.submitting = true
    try {
      await addSalesman({ userId: addForm.userId, createdBy: getCurrentUserName() })
      ElMessage.success('业务员添加成功')
      addDialog.visible = false
      loadReferralData(); loadAllSalesmen()
    } catch { /* handled */ } finally { addDialog.submitting = false }
  })
}

// 推荐码操作
const confirmDialog = reactive({ visible: false, title: '', message: '', loading: false, onConfirm: () => {} })

function handleRegenerate(row: SalesmanReferralVO) {
  confirmDialog.title = '重新生成推荐码'
  confirmDialog.message = `重新生成后，原推荐码 <b style="color:#f56c6c;">${row.referralCode || '—'}</b> 将立即失效，确认？`
  confirmDialog.loading = false
  confirmDialog.onConfirm = async () => {
    confirmDialog.loading = true
    try { await regenerateCode(row.id, getCurrentUserName()); ElMessage.success('推荐码已重新生成'); confirmDialog.visible = false; loadReferralData() } catch { /* */ } finally { confirmDialog.loading = false }
  }
  confirmDialog.visible = true
}
function handleClear(row: SalesmanReferralVO) {
  confirmDialog.title = '置空推荐码'
  confirmDialog.message = '置空后该业务员将暂无推荐码，无法通过推荐码发展新客户。确认？'
  confirmDialog.loading = false
  confirmDialog.onConfirm = async () => {
    confirmDialog.loading = true
    try { await clearCode(row.id, getCurrentUserName()); ElMessage.success('推荐码已置空'); confirmDialog.visible = false; loadReferralData() } catch { /* */ } finally { confirmDialog.loading = false }
  }
  confirmDialog.visible = true
}
function handleDelete(row: SalesmanReferralVO) {
  confirmDialog.title = '删除推荐码'
  confirmDialog.message = `删除后推荐码 <b style="color:#f56c6c;">${row.referralCode}</b> 将永久失效。确认？`
  confirmDialog.loading = false
  confirmDialog.onConfirm = async () => {
    confirmDialog.loading = true
    try { await deleteReferralCode(row.id, getCurrentUserName()); ElMessage.success('推荐码已删除'); confirmDialog.visible = false; loadReferralData() } catch { /* */ } finally { confirmDialog.loading = false }
  }
  confirmDialog.visible = true
}

// ==============================
// Tab 2: 业务员绩效配置
// ==============================
const allSalesmen = ref<SalesmanReferralVO[]>([])

async function loadAllSalesmen() {
  try {
    const res = await getAllSalesmen()
    if (res.code === 0) allSalesmen.value = res.data
  } catch { /* */ }
}

const perfSearch = reactive({ salesmanId: null as string | null, month: '' })
const perfPageNum = ref(1)
const perfPageSize = ref(20)
const perfTotal = ref(0)
const perfLoading = ref(false)
const perfTableData = ref<SalesmanPerformanceVO[]>([])

async function loadPerfData() {
  perfLoading.value = true
  try {
    const res = await getPerformanceList({
      pageNum: perfPageNum.value, pageSize: perfPageSize.value,
      salesmanId: perfSearch.salesmanId || undefined,
      month: perfSearch.month || undefined,
    })
    if (res.code === 0) { perfTableData.value = res.data.records; perfTotal.value = res.data.total }
  } catch { /* */ } finally { perfLoading.value = false }
}
function doPerfSearch() { perfPageNum.value = 1; loadPerfData() }
function resetPerfSearch() { perfSearch.salesmanId = null; perfSearch.month = ''; doPerfSearch() }
watch([perfPageNum, perfPageSize], () => loadPerfData())

// 绩效弹窗
const perfDialog = reactive({ visible: false, isEdit: false, submitting: false, editId: null as string | null })
const perfFormRef = ref<FormInstance | null>(null)
const perfForm = reactive({ salesmanId: null as string | null, month: '', orderCount: 0, customerCount: 0, salesAmount: 0 })
const perfRules: FormRules = {
  salesmanId: [{ required: true, message: '请选择业务员', trigger: 'change' }],
  month: [{ required: true, message: '请选择月度', trigger: 'change' }],
  orderCount: [{ required: true, message: '请输入订单数', trigger: 'blur' }],
  customerCount: [{ required: true, message: '请输入下单客户数', trigger: 'blur' }],
  salesAmount: [{ required: true, message: '请输入销售额', trigger: 'blur' }],
}

function openPerfForm(row: SalesmanPerformanceVO | null) {
  perfDialog.submitting = false
  if (row) {
    perfDialog.isEdit = true; perfDialog.editId = row.id ?? null
    perfForm.salesmanId = row.salesmanId; perfForm.month = row.month
    perfForm.orderCount = row.orderCount; perfForm.customerCount = row.customerCount
    perfForm.salesAmount = row.salesAmount
  } else {
    perfDialog.isEdit = false; perfDialog.editId = null
    perfForm.salesmanId = null; perfForm.month = ''
    perfForm.orderCount = 0; perfForm.customerCount = 0; perfForm.salesAmount = 0
  }
  perfDialog.visible = true
  nextTick(() => perfFormRef.value?.clearValidate())
}
function resetPerfForm() {
  perfForm.salesmanId = null; perfForm.month = ''
  perfForm.orderCount = 0; perfForm.customerCount = 0; perfForm.salesAmount = 0
}
async function submitPerfForm() {
  if (!perfFormRef.value) return
  perfFormRef.value.validate(async (valid) => {
    if (!valid) return
    perfDialog.submitting = true
    const selected = allSalesmen.value.find(s => s.id === perfForm.salesmanId)
    const perfData: SalesmanPerformanceVO = {
      id: perfDialog.editId ?? undefined,
      salesmanId: perfForm.salesmanId!, salesmanName: selected?.name || '',
      month: perfForm.month,
      orderCount: perfForm.orderCount, customerCount: perfForm.customerCount,
      salesAmount: perfForm.salesAmount,
      updatedBy: '', updatedAt: '',
    }
    try {
      if (perfDialog.isEdit) {
        await editPerformance(perfData, getCurrentUserName())
      } else {
        await addPerformance(perfData, getCurrentUserName())
      }
      ElMessage.success(perfDialog.isEdit ? '绩效更新成功' : '绩效添加成功')
      perfDialog.visible = false; loadPerfData()
    } catch { /* */ } finally { perfDialog.submitting = false }
  })
}

// ===== Tab 切换时加载对应数据 =====
watch(activeTab, (tab) => {
  if (tab === 'referral') loadReferralData()
  else if (tab === 'performance') { loadAllSalesmen(); loadPerfData() }
})

// ===== 初始化 =====
loadReferralData()
loadAllSalesmen()
</script>
