<template>
  <div class="tw-space-y-3">
    <!-- ===== 查询区 ===== -->
    <div
      class="tw-bg-white tw-rounded-[--border-radius-card] tw-px-5 tw-pt-4 tw-pb-1 tw-shadow-[--shadow-card]"
    >
      <el-form :model="search" inline>
        <el-form-item label="销售大区">
          <el-select v-model="search.region" placeholder="全部" clearable style="width: 160px">
            <el-option
              v-for="item in regionOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="下单服务启用">
          <el-select
            v-model="search.serviceEnabled"
            placeholder="全部"
            clearable
            style="width: 130px"
          >
            <el-option label="启用" value="true" />
            <el-option label="停用" value="false" />
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
      <div class="tw-flex tw-justify-start tw-items-center tw-mb-4">
        <el-button type="primary" @click="openAdd">新增大区</el-button>
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
        <el-table-column prop="code" label="编号" width="100" fixed="left">
          <template #default="{ row }">
            <span
              class="tw-text-[--color-primary] tw-cursor-pointer tw-font-medium hover:tw-underline"
              @click="viewDetail(row)"
            >
              {{ row.code }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="名称" width="130" show-overflow-tooltip />
        <el-table-column label="下单服务" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.serviceEnabled ? 'success' : 'info'" size="small" effect="plain">
              {{ row.serviceEnabled ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="多天订购" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.multiDay ? 'success' : 'info'" size="small" effect="plain">
              {{ row.multiDay ? '开启' : '关闭' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="minDays" label="最少订购天数" width="110" align="center" />
        <el-table-column prop="bizHours" label="营业时间" width="140" />
        <el-table-column prop="orderType" label="起订类型" width="90" align="center" />
        <el-table-column prop="orderAmount" label="起订金额/重量" width="130" align="right" />
        <el-table-column label="到货日期" width="90" align="center">
          <template #default="{ row }">
            <span>T+{{ row.arrivalDays }}</span>
          </template>
        </el-table-column>
        <el-table-column label="价格审批" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.priceApproval ? 'warning' : 'info'" size="small" effect="plain">
              {{ row.priceApproval ? '开启' : '关闭' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="warehouseCount" label="已关联仓库" width="100" align="center" />
        <el-table-column prop="stdFreight" label="标准物流费" width="110" align="right" />
        <el-table-column prop="stdFreeAmount" label="免运费金额" width="110" align="right" />
        <el-table-column prop="newFreight" label="新客物流费" width="110" align="right" />
        <el-table-column prop="newFreeAmount" label="新客免运费" width="110" align="right" />
        <el-table-column prop="merchantNo" label="商户号" width="140" show-overflow-tooltip />
        <el-table-column prop="merchantName" label="商户号名称" width="140" show-overflow-tooltip />
        <el-table-column prop="updatedBy" label="修改人" width="100" />
        <el-table-column prop="updatedAt" label="修改时间" width="170" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="openEdit(row)">编辑</el-button>
            <el-button type="danger" link @click="confirmDelete(row)">删除</el-button>
          </template>
        </el-table-column>
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
      :title="dialog.isEdit ? `编辑大区 — ${form.code}` : '新增大区'"
      width="960px"
      :close-on-click-modal="false"
      top="3vh"
      @close="resetForm"
    >
      <div class="tw-flex tw-gap-6">
        <!-- ===== 左栏：基本配置 ===== -->
        <div class="tw-flex-1 tw-min-w-0">
          <div
            class="tw-text-sm tw-font-semibold tw-text-[--text-color] tw-mb-4 tw-pb-2 tw-border-b tw-border-gray-100"
          >
            基本配置
          </div>
          <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
            <el-form-item label="销售大区编号" prop="code">
              <el-input
                v-model="form.code"
                :disabled="dialog.isEdit"
                placeholder="请输入编号"
                maxlength="20"
              />
            </el-form-item>
            <el-form-item label="销售大区名称" prop="name">
              <el-input v-model="form.name" placeholder="请输入名称" maxlength="50" />
            </el-form-item>
            <el-form-item label="下单服务启用" prop="serviceEnabled">
              <el-radio-group v-model="form.serviceEnabled">
                <el-radio :value="true">启用</el-radio>
                <el-radio :value="false">停用</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="营业时间" prop="bizHours">
              <el-time-picker
                v-model="form.bizHours"
                is-range
                format="HH:mm"
                value-format="HH:mm"
                range-separator="~"
                start-placeholder="开始时间"
                end-placeholder="结束时间"
                style="width: 100%"
              />
            </el-form-item>
            <el-form-item label="到货日期">
              <el-select v-model="form.arrivalDays" placeholder="请选择" style="width: 100%">
                <el-option label="T+1（次日达）" :value="1" />
                <el-option label="T+2" :value="2" />
                <el-option label="T+3" :value="3" />
              </el-select>
              <span class="tw-text-xs tw-text-[--text-secondary] tw-ml-2">配送日期 T+N</span>
            </el-form-item>
            <el-form-item label="支付商户号" prop="merchantNo">
              <el-select v-model="form.merchantNo" placeholder="请选择商户号" style="width: 100%">
                <el-option
                  v-for="m in merchantOptions"
                  :key="m.value"
                  :label="m.label"
                  :value="m.value"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="支持多天订购">
              <el-checkbox v-model="form.multiDay">开启</el-checkbox>
            </el-form-item>
            <el-form-item v-if="form.multiDay" label="最少订购天数" prop="minDays">
              <el-input-number v-model="form.minDays" :min="2" :max="30" style="width: 100%" />
            </el-form-item>
            <el-form-item label="开启价格审批">
              <el-checkbox v-model="form.priceApproval">开启</el-checkbox>
            </el-form-item>
          </el-form>
        </div>

        <!-- ===== 右栏：物流与起订规则 ===== -->
        <div class="tw-flex-1 tw-min-w-0">
          <div
            class="tw-text-sm tw-font-semibold tw-text-[--text-color] tw-mb-4 tw-pb-2 tw-border-b tw-border-gray-100"
          >
            物流费用与起订规则
          </div>
          <el-form :model="form" label-width="120px">
            <el-form-item label="标准免运费金额">
              <el-input-number
                v-model="form.stdFreeAmount"
                :min="0"
                :precision="2"
                style="width: 100%"
                placeholder="元"
              />
            </el-form-item>
            <el-form-item label="标准运费">
              <el-input-number
                v-model="form.stdFreight"
                :min="0"
                :precision="2"
                style="width: 100%"
                placeholder="元"
              />
            </el-form-item>
            <el-form-item label="新客免运费金额">
              <el-input-number
                v-model="form.newFreeAmount"
                :min="0"
                :precision="2"
                style="width: 100%"
                placeholder="元"
              />
            </el-form-item>
            <el-form-item label="新客运费">
              <el-input-number
                v-model="form.newFreight"
                :min="0"
                :precision="2"
                style="width: 100%"
                placeholder="元"
              />
            </el-form-item>
            <el-form-item label="规则说明">
              <div
                class="tw-text-xs tw-text-[--text-secondary] tw-leading-relaxed tw-bg-slate-50 tw-rounded-[6px] tw-p-3"
              >
                <div>
                  新客规则：新客下单日起 T+6 天内，一天内订单满
                  <strong>{{ form.newFreeAmount || 'X' }}</strong> 元免运费，少于
                  <strong>{{ form.newFreeAmount || 'X' }}</strong> 元则支付运费每单
                  <strong>{{ form.newFreight || 'Y' }}</strong> 元
                </div>
                <div class="tw-mt-2">
                  标准规则：一天内订单满
                  <strong>{{ form.stdFreeAmount || 'X' }}</strong> 元免运费，少于
                  <strong>{{ form.stdFreeAmount || 'X' }}</strong> 元每单
                  <strong>{{ form.stdFreight || 'Y' }}</strong> 元
                </div>
              </div>
            </el-form-item>
            <el-form-item label="起订类型">
              <el-checkbox-group v-model="form.orderTypes">
                <el-checkbox label="按金额" :value="'按金额'" />
                <el-checkbox label="按重量" :value="'按重量'" />
              </el-checkbox-group>
            </el-form-item>
            <el-form-item label="起订金额/重量">
              <el-input
                v-model="form.orderAmount"
                placeholder="请输入起订金额或重量"
                style="width: 100%"
              />
            </el-form-item>
          </el-form>
        </div>
      </div>

      <!-- ===== 价格审批配置（条件展示） ===== -->
      <div
        v-if="form.priceApproval"
        class="tw-bg-slate-50 tw-rounded-[--border-radius-input] tw-p-4 tw-mt-4"
      >
        <div class="tw-text-sm tw-font-semibold tw-text-[--text-color] tw-mb-3">价格审批配置</div>
        <el-form :model="form" label-width="110px" inline>
          <el-form-item label="审批阈值(%)">
            <el-input-number
              v-model="form.approvalThreshold"
              :min="0"
              :max="100"
              :precision="1"
              style="width: 160px"
              placeholder="如 15"
            />
            <span class="tw-text-xs tw-text-[--text-secondary] tw-ml-2">
              价格变动超过 &plusmn;N% 时触发审批
            </span>
          </el-form-item>
        </el-form>
        <div class="tw-flex tw-items-center tw-gap-3 tw-mb-2">
          <span class="tw-text-sm tw-text-[--text-color]">审批人员：</span>
          <el-button size="small" @click="addApprover">添加用户</el-button>
        </div>
        <el-table
          v-if="form.approvers.length"
          :data="form.approvers"
          size="small"
          style="width: 100%"
        >
          <el-table-column prop="name" label="用户名称" width="150" />
          <el-table-column prop="phone" label="手机号" width="150" />
          <el-table-column label="操作" width="80">
            <template #default="{ $index }">
              <el-button type="danger" link size="small" @click="removeApprover($index)">
                移除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        <div v-else class="tw-text-xs tw-text-gray-300 tw-py-2">暂未添加审批人员</div>
      </div>

      <template #footer>
        <el-button @click="dialog.visible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, watch, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import {
  getSalesRegionList,
  getAllSalesRegions,
  createSalesRegion,
  updateSalesRegion,
  deleteSalesRegion,
  type SalesRegionViewVO,
  type SalesRegionCreationVO,
  type SalesRegionEditVO,
} from '@/api/modules/operation'

// ===== 搜索条件 =====
const search = reactive({
  region: '',
  serviceEnabled: '',
})

// ===== 下拉选项（从接口获取全部大区） =====
interface OptionItem {
  label: string
  value: string
}

const regionOptions = ref<OptionItem[]>([])

const merchantOptions = [
  { label: 'SH1001 - 钱鲜达主商户', value: 'SH1001' },
  { label: 'SH1002 - 钱鲜达华南', value: 'SH1002' },
  { label: 'SH1003 - 钱鲜达华东', value: 'SH1003' },
]

/** 商户号 → 名称映射 */
const merchantNameMap: Record<string, string> = {
  SH1001: '钱鲜达主商户',
  SH1002: '钱鲜达华南',
  SH1003: '钱鲜达华东',
}

/** 获取当前登录用户姓名 */
function getCurrentUserName(): string {
  return localStorage.getItem('realName') || ''
}

// ===== 表格数据 =====
type SalesRegion = SalesRegionViewVO

/** 审批人 */
interface Approver {
  id: number
  name: string
  phone: string
}

const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(20)
const total = ref(0)
const tableData = ref<SalesRegion[]>([])

// ===== 加载全部大区（下拉选项用） =====
async function loadRegionOptions() {
  try {
    const res = await getAllSalesRegions()
    if (res.code === 0) {
      regionOptions.value = res.data.records.map((r) => ({
        label: r.name,
        value: r.code,
      }))
    }
  } catch {
    // 错误已由拦截器统一提示
  }
}

// ===== 加载表格数据（分页） =====
async function loadData() {
  loading.value = true
  try {
    const res = await getSalesRegionList({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      region: search.region || undefined,
      serviceEnabled:
        !search.serviceEnabled
          ? undefined
          : search.serviceEnabled === 'true',
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
  search.region = ''
  search.serviceEnabled = ''
  pageNum.value = 1
  loadData()
}

watch([pageNum, pageSize], () => {
  loadData()
})

// ===== 查看详情 =====
function viewDetail(row: SalesRegion) {
  ElMessage.info(`查看大区详情：${row.code} ${row.name}`)
}

// ===== 删除 =====
function confirmDelete(row: SalesRegion) {
  ElMessageBox.confirm(`确定删除大区「${row.name}」吗？删除后不可恢复。`, '删除确认', {
    confirmButtonText: '确定删除',
    cancelButtonText: '取消',
    type: 'warning',
  })
    .then(async () => {
      try {
        await deleteSalesRegion(row.code)
        ElMessage.success('删除成功')
        loadRegionOptions()
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
  code: string
  name: string
  serviceEnabled: boolean
  bizHours: [string, string]
  arrivalDays: number
  merchantNo: string
  multiDay: boolean
  minDays: number
  priceApproval: boolean
  approvalThreshold: number
  stdFreeAmount: number | null
  stdFreight: number | null
  newFreeAmount: number | null
  newFreight: number | null
  orderTypes: string[]
  orderAmount: string
  approvers: Approver[]
}

const dialog = reactive({ visible: false, isEdit: false })
const formRef = ref<FormInstance | null>(null)

const defaultForm = (): FormData => ({
  code: '',
  name: '',
  serviceEnabled: true,
  bizHours: ['06:00', '22:00'],
  arrivalDays: 1,
  merchantNo: '',
  multiDay: false,
  minDays: 2,
  priceApproval: false,
  approvalThreshold: 15,
  stdFreeAmount: null,
  stdFreight: null,
  newFreeAmount: null,
  newFreight: null,
  orderTypes: ['按金额'],
  orderAmount: '',
  approvers: [],
})

const form = reactive<FormData>(defaultForm())

const rules: FormRules = {
  code: [
    { required: true, message: '请输入编号', trigger: 'blur' },
    { pattern: /^[A-Z0-9]+$/, message: '仅支持大写字母和数字', trigger: 'blur' },
  ],
  name: [{ required: true, message: '请输入名称', trigger: 'blur' }],
  merchantNo: [{ required: true, message: '请选择商户号', trigger: 'change' }],
}

function openAdd() {
  dialog.isEdit = false
  resetForm()
  dialog.visible = true
}

function openEdit(row: SalesRegion) {
  dialog.isEdit = true
  form.code = row.code
  form.name = row.name
  form.serviceEnabled = row.serviceEnabled
  form.bizHours = row.bizHours
    ? (row.bizHours.split(' ~ ') as [string, string])
    : ['06:00', '22:00']
  form.arrivalDays = row.arrivalDays
  form.merchantNo = row.merchantNo
  form.multiDay = row.multiDay
  form.minDays = row.minDays || 2
  form.priceApproval = row.priceApproval
  form.stdFreeAmount = row.stdFreeAmount ?? null
  form.stdFreight = row.stdFreight ?? null
  form.newFreeAmount = row.newFreeAmount ?? null
  form.newFreight = row.newFreight ?? null
  form.orderTypes = [row.orderType]
  form.orderAmount = row.orderAmount
  form.approvers = []
  form.approvalThreshold = row.approvalThreshold ?? 15
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

// ===== 审批人管理 =====
let approverIdCounter = 0

function addApprover() {
  approverIdCounter++
  form.approvers.push({
    id: approverIdCounter,
    name: `审批人${approverIdCounter}`,
    phone: `1380000${String(approverIdCounter).padStart(4, '0')}`,
  })
  ElMessage.success('已添加审批人')
}

function removeApprover(index: number) {
  form.approvers.splice(index, 1)
  ElMessage.success('已移除')
}

// ===== 提交表单（调用后端 API） =====
function submitForm() {
  formRef.value?.validate(async (valid) => {
    if (!valid) return

    const bizHoursStr = `${form.bizHours[0]} ~ ${form.bizHours[1]}`
    const approversJson = form.approvers.length > 0 ? JSON.stringify(form.approvers) : undefined

    try {
      if (dialog.isEdit) {
        const editData: SalesRegionEditVO = {
          name: form.name,
          serviceEnabled: form.serviceEnabled,
          multiDay: form.multiDay,
          minDays: form.multiDay ? form.minDays : undefined,
          bizHours: bizHoursStr,
          orderType: form.orderTypes[0] || '按金额',
          orderAmount: form.orderAmount,
          arrivalDays: form.arrivalDays,
          priceApproval: form.priceApproval,
          approvalThreshold: form.priceApproval ? form.approvalThreshold : undefined,
          approvers: approversJson,
          stdFreight: form.stdFreight ?? undefined,
          stdFreeAmount: form.stdFreeAmount ?? undefined,
          newFreight: form.newFreight ?? undefined,
          newFreeAmount: form.newFreeAmount ?? undefined,
          merchantNo: form.merchantNo,
          merchantName: merchantNameMap[form.merchantNo] || '',
          updatedBy: getCurrentUserName(),
        }
        await updateSalesRegion(form.code, editData)
        ElMessage.success('编辑成功')
      } else {
        const createData: SalesRegionCreationVO = {
          code: form.code,
          name: form.name,
          serviceEnabled: form.serviceEnabled,
          multiDay: form.multiDay,
          minDays: form.multiDay ? form.minDays : undefined,
          bizHours: bizHoursStr,
          orderType: form.orderTypes[0] || '按金额',
          orderAmount: form.orderAmount,
          arrivalDays: form.arrivalDays,
          priceApproval: form.priceApproval,
          approvalThreshold: form.priceApproval ? form.approvalThreshold : undefined,
          approvers: approversJson,
          stdFreight: form.stdFreight ?? undefined,
          stdFreeAmount: form.stdFreeAmount ?? undefined,
          newFreight: form.newFreight ?? undefined,
          newFreeAmount: form.newFreeAmount ?? undefined,
          merchantNo: form.merchantNo,
          merchantName: merchantNameMap[form.merchantNo] || '',
          createdBy: getCurrentUserName(),
        }
        await createSalesRegion(createData)
        ElMessage.success('新增大区成功')
      }
      dialog.visible = false
      loadRegionOptions()
      loadData()
    } catch {
      // 错误已由拦截器统一提示
    }
  })
}

// ===== 初始化 =====
loadRegionOptions()
loadData()
</script>
