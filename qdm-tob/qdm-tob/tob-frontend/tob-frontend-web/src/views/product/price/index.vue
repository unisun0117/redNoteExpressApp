<template>
  <div class="tw-space-y-3">
    <!-- Tab 切换 -->
    <div
      class="tw-bg-white tw-rounded-[--border-radius-card] tw-px-5 tw-pt-4 tw-shadow-[--shadow-card]"
    >
      <el-tabs v-model="activeTab" @tab-change="onTabChange">
        <el-tab-pane label="价格组" name="group">
          <div
            class="tw-bg-white tw-rounded-[--border-radius-card] tw-pb-1 tw-shadow-[--shadow-card]"
          >
            <el-form :model="groupSearch" inline>
              <el-form-item label="销售大区">
                <el-select
                  v-model="groupSearch.salesRegionCode"
                  placeholder="全部"
                  clearable
                  style="width: 160px"
                >
                  <el-option
                    v-for="item in regionOptions"
                    :key="item.code"
                    :label="item.name"
                    :value="item.code"
                  />
                </el-select>
              </el-form-item>
              <el-form-item label="价格组名称">
                <el-input
                  v-model="groupSearch.priceGroupName"
                  placeholder="请输入价格组名称"
                  clearable
                  style="width: 180px"
                />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="doGroupSearch">查询</el-button>
                <el-button @click="resetGroupSearch">重置</el-button>
              </el-form-item>
            </el-form>
          </div>
        </el-tab-pane>
        <el-tab-pane label="价格组明细" name="detail">
                    <div
              class="tw-bg-white tw-rounded-[--border-radius-card] tw-pb-1 tw-shadow-[--shadow-card]"
            >
              <el-form :model="detailSearch" inline>
                <el-form-item label="销售大区">
                  <el-select
                    v-model="detailSearch.salesRegionCode"
                    placeholder="全部"
                    clearable
                    style="width: 160px"
                  >
                    <el-option
                      v-for="item in regionOptions"
                      :key="item.code"
                      :label="item.name"
                      :value="item.code"
                    />
                  </el-select>
                </el-form-item>
                <el-form-item label="价格组名称">
                  <el-input
                    v-model="detailSearch.priceGroupName"
                    placeholder="请输入价格组名称"
                    clearable
                    style="width: 160px"
                  />
                </el-form-item>
                <el-form-item label="商品">
                  <el-input
                    v-model="detailSearch.keyword"
                    placeholder="商品条码/名称"
                    clearable
                    style="width: 180px"
                  />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="doDetailSearch">查询</el-button>
                  <el-button @click="resetDetailSearch">重置</el-button>
                </el-form-item>
              </el-form>
            </div>
        </el-tab-pane>
      </el-tabs>
    </div>
    <!-- ================================================================ -->
    <!-- Tab 1：价格组 -->
    <!-- ================================================================ -->
    <template v-if="activeTab === 'group'">
      <!-- 数据区 -->
      <div
        class="tw-bg-white tw-rounded-[--border-radius-card] tw-px-5 tw-pt-4 tw-pb-1 tw-shadow-[--shadow-card]"
      >
        <div class="tw-mb-3 tw-flex tw-justify-between tw-items-center">
          <el-button type="primary" @click="openGroupCreate">新增</el-button>
          <span class="tw-text-sm tw-text-[--text-secondary]"
            >共 {{ groupTotal }} 条记录</span
          >
        </div>

        <el-table
          v-loading="groupLoading"
          :data="groupTableData"
          stripe
          :header-cell-style="{
            background: '#F1F5F9',
            color: '#475569',
            fontWeight: 600,
            fontSize: '13px',
          }"
          style="width: 100%"
        >
          <el-table-column prop="salesRegionName" label="销售大区" min-width="100" />
          <el-table-column prop="priceGroupCode" label="价格组编码" min-width="120" />
          <el-table-column prop="priceGroupName" label="价格组名称" min-width="120" />
          <el-table-column prop="description" label="描述说明" min-width="160" show-overflow-tooltip />
          <el-table-column prop="updatedBy" label="更新人" min-width="100" />
          <el-table-column prop="updatedAt" label="更新时间" min-width="160" />
          <el-table-column label="操作" min-width="80" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" link size="small" @click="openGroupEdit(row)"
                >编辑</el-button
              >
            </template>
          </el-table-column>
          <template #empty>
            <el-empty description="暂无数据" />
          </template>
        </el-table>

        <div class="tw-mt-4 tw-flex">
          <el-pagination
            v-model:current-page="groupPageNum"
            v-model:page-size="groupPageSize"
            :page-sizes="[10, 20, 50]"
            :total="groupTotal"
            layout="total, sizes, prev, pager, next"
            background
          />
        </div>
      </div>
    </template>

    <!-- ================================================================ -->
    <!-- Tab 2：价格组明细 -->
    <!-- ================================================================ -->
    <template v-if="activeTab === 'detail'">
      <!-- 数据区 -->
      <div
        class="tw-bg-white tw-rounded-[--border-radius-card] tw-px-5 tw-pt-4 tw-pb-1 tw-shadow-[--shadow-card]"
      >
        <div class="tw-mb-3 tw-flex tw-justify-between tw-items-center">
          <div class="tw-space-x-2">
            <el-button type="primary" @click="openDetailCreate">新增</el-button>
            <el-button @click="handleExport">导出</el-button>
            <el-button @click="openImport">导入</el-button>
          </div>
          <span class="tw-text-sm tw-text-[--text-secondary]"
            >共 {{ detailTotal }} 条记录</span
          >
        </div>

        <el-table
          v-loading="detailLoading"
          :data="detailTableData"
          stripe
          :header-cell-style="{
            background: '#F1F5F9',
            color: '#475569',
            fontWeight: 600,
            fontSize: '13px',
          }"
          style="width: 100%"
        >
          <el-table-column prop="salesRegionCode" label="销售大区" min-width="90" />
          <el-table-column prop="priceGroupCode" label="价格组编码" min-width="110" />
          <el-table-column prop="priceGroupName" label="价格组名称" min-width="100" show-overflow-tooltip />
          <el-table-column prop="productBarcode" label="商品条码" min-width="120" />
          <el-table-column prop="productName" label="商品名称" min-width="120" show-overflow-tooltip />
          <el-table-column prop="price" label="售价" min-width="90">
            <template #default="{ row }">
              <span>{{ row.price != null ? '¥' + row.price.toFixed(2) : '-' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="审批状态" min-width="100">
            <template #default="{ row }">
              <el-tag
                v-if="row.approvalStatus === 'PENDING'"
                type="warning"
                size="small"
              >
                审批中
              </el-tag>
              <el-tag
                v-else-if="row.approvalStatus === 'APPROVED'"
                type="success"
                size="small"
              >
                审批通过
              </el-tag>
              <el-tag
                v-else-if="row.approvalStatus === 'REJECTED'"
                type="danger"
                size="small"
              >
                审批驳回
              </el-tag>
              <span v-else class="tw-text-[--text-secondary]">-</span>
            </template>
          </el-table-column>
          <el-table-column prop="updatedBy" label="更新人" min-width="90" />
          <el-table-column prop="updatedAt" label="更新时间" min-width="160" />
          <el-table-column label="操作" min-width="80" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" link size="small" @click="openDetailEdit(row)"
                >编辑</el-button
              >
            </template>
          </el-table-column>
          <template #empty>
            <el-empty description="暂无数据" />
          </template>
        </el-table>

        <div class="tw-mt-4 tw-flex">
          <el-pagination
            v-model:current-page="detailPageNum"
            v-model:page-size="detailPageSize"
            :page-sizes="[10, 20, 50]"
            :total="detailTotal"
            layout="total, sizes, prev, pager, next"
            background
          />
        </div>
      </div>
    </template>

    <!-- ================================================================ -->
    <!-- 价格组新增/编辑弹窗 -->
    <!-- ================================================================ -->
    <el-dialog
      v-model="groupDialog.visible"
      :title="groupDialog.isEdit ? '编辑价格组' : '新增价格组'"
      width="500px"
      align-center
      :close-on-click-modal="false"
      destroy-on-close
    >
      <el-form
        ref="groupFormRef"
        :model="groupForm"
        :rules="groupFormRules"
        label-width="110px"
      >
        <el-form-item label="销售大区" prop="salesRegionCode">
          <el-select
            v-model="groupForm.salesRegionCode"
            placeholder="请选择"
            :disabled="groupDialog.isEdit"
            style="width: 100%"
            @change="onGroupRegionChange"
          >
            <el-option
              v-for="item in regionOptions"
              :key="item.code"
              :label="item.name"
              :value="item.code"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="价格组编码" prop="priceGroupCode">
          <el-input
            v-model="groupForm.priceGroupCode"
            placeholder="请输入价格组编码"
            :disabled="groupDialog.isEdit"
          />
        </el-form-item>
        <el-form-item label="价格组名称" prop="priceGroupName">
          <el-input
            v-model="groupForm.priceGroupName"
            placeholder="请输入价格组名称"
            maxlength="100"
          />
        </el-form-item>
        <el-form-item label="描述说明">
          <el-input
            v-model="groupForm.description"
            placeholder="选填，最长500字符"
            maxlength="500"
            type="textarea"
            :rows="3"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="groupDialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="groupSubmitting" @click="submitGroupForm">
          保存
        </el-button>
      </template>
    </el-dialog>

    <!-- ================================================================ -->
    <!-- 价格组明细新增弹窗 -->
    <!-- ================================================================ -->
    <el-dialog
      v-model="detailCreateDialog.visible"
      title="新增价格明细"
      width="500px"
      align-center
      :close-on-click-modal="false"
      destroy-on-close
    >
      <el-form
        ref="detailCreateFormRef"
        :model="detailCreateForm"
        :rules="detailCreateFormRules"
        label-width="110px"
      >
        <el-form-item label="销售大区" prop="salesRegionCode">
          <el-select
            v-model="detailCreateForm.salesRegionCode"
            placeholder="请选择"
            style="width: 100%"
            @change="onDetailRegionChange"
          >
            <el-option
              v-for="item in regionOptions"
              :key="item.code"
              :label="item.name"
              :value="item.code"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="价格组名称" prop="priceGroupCode">
          <el-select
            v-model="detailCreateForm.priceGroupCode"
            placeholder="请先选择销售大区"
            style="width: 100%"
            :disabled="!detailCreateForm.salesRegionCode"
          >
            <el-option
              v-for="item in priceGroupOptions"
              :key="item.priceGroupCode"
              :label="item.priceGroupName"
              :value="item.priceGroupCode"
            />
            <template #empty>
              <div class="tw-p-3 tw-text-center tw-text-[--text-secondary]">
                该销售大区下暂无价格组，请先创建价格组
              </div>
            </template>
          </el-select>
        </el-form-item>
        <el-form-item label="商品条码" prop="productBarcode">
          <el-input
            v-model="detailCreateForm.productBarcode"
            placeholder="请输入商品条码，回车校验"
            @keyup.enter="onBarcodeLookup"
          >
            <template #suffix>
              <el-button
                type="primary"
                link
                size="small"
                :loading="barcodeLookupLoading"
                @click="onBarcodeLookup"
              >
                校验
              </el-button>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="商品名称">
          <el-input
            v-model="detailCreateForm.productName"
            disabled
            placeholder="条码校验后自动带出"
          />
        </el-form-item>
        <el-form-item label="售价" prop="price">
          <el-input-number
            v-model="detailCreateForm.price"
            :precision="2"
            :min="0.01"
            style="width: 100%"
            controls-position="right"
            placeholder="请输入售价（元）"
          >
            <template #suffix>
              <span class="tw-text-sm tw-text-[--text-secondary]">元</span>
            </template>
          </el-input-number>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="detailCreateDialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="detailCreateSubmitting" @click="submitDetailCreateForm">
          保存
        </el-button>
      </template>
    </el-dialog>

    <!-- ================================================================ -->
    <!-- 价格组明细编辑弹窗 -->
    <!-- ================================================================ -->
    <el-dialog
      v-model="detailEditDialog.visible"
      title="编辑价格明细"
      width="500px"
      align-center
      :close-on-click-modal="false"
      destroy-on-close
    >
      <el-form
        ref="detailEditFormRef"
        :model="detailEditForm"
        :rules="detailEditFormRules"
        label-width="110px"
      >
        <el-form-item label="销售大区">
          <el-input :model-value="detailEditDialog.salesRegionCode" disabled />
        </el-form-item>
        <el-form-item label="价格组名称">
          <el-input :model-value="detailEditDialog.priceGroupName" disabled />
        </el-form-item>
        <el-form-item label="商品条码">
          <el-input :model-value="detailEditDialog.productBarcode" disabled />
        </el-form-item>
        <el-form-item label="商品名称">
          <el-input :model-value="detailEditDialog.productName" disabled />
        </el-form-item>
        <el-form-item label="售价" prop="price">
          <el-input-number
            v-model="detailEditForm.price"
            :precision="2"
            :min="0.01"
            style="width: 100%"
            controls-position="right"
          >
            <template #suffix>
              <span class="tw-text-sm tw-text-[--text-secondary]">元</span>
            </template>
          </el-input-number>
        </el-form-item>
        <el-form-item label="变动原因" prop="changeReason">
          <el-input
            v-model="detailEditForm.changeReason"
            placeholder="请输入变动原因（必填，最多50字符）"
            maxlength="50"
          />
        </el-form-item>
        <div class="tw-ml-[110px] tw-mb-4">
          <el-alert
            title="价格变动比率超过阈值时将生成待审批记录，审批完成后售价才生效"
            type="warning"
            :closable="false"
            show-icon
          />
        </div>
      </el-form>
      <template #footer>
        <el-button @click="detailEditDialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="detailEditSubmitting" @click="submitDetailEditForm">
          保存
        </el-button>
      </template>
    </el-dialog>

    <!-- ================================================================ -->
    <!-- 导入弹窗 -->
    <!-- ================================================================ -->
    <el-dialog
      v-model="importDialog.visible"
      title="导入价格明细"
      width="520px"
      align-center
      :close-on-click-modal="false"
      destroy-on-close
    >
      <el-upload
        ref="uploadRef"
        :auto-upload="false"
        :limit="1"
        accept=".xlsx,.xls"
        :on-change="onFileChange"
        :on-remove="onFileRemove"
        drag
      >
        <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
        <div class="el-upload__text">将 Excel 文件拖到此处，或点击上传</div>
        <template #tip>
          <div class="el-upload__tip tw-mt-2">
            仅支持 .xlsx / .xls 格式，单次上限 1000 条
          </div>
        </template>
      </el-upload>

      <div v-if="importDialog.result" class="tw-mt-4">
        <el-alert
          :title="`导入完成：新增 ${importDialog.result.createdCount} 条，更新 ${importDialog.result.updatedCount} 条，失败 ${importDialog.result.failedCount} 条`"
          :type="importDialog.result.failedCount > 0 ? 'warning' : 'success'"
          :closable="false"
        />
        <div
          v-if="importDialog.result.errors && importDialog.result.errors.length > 0"
          class="tw-mt-2 tw-max-h-40 tw-overflow-y-auto tw-text-sm"
        >
          <div
            v-for="(err, idx) in importDialog.result.errors"
            :key="idx"
            class="tw-text-red-500 tw-py-1"
          >
            第 {{ err.row }} 行：{{ err.reason }}
          </div>
        </div>
      </div>

      <template #footer>
        <el-button @click="importDialog.visible = false">关闭</el-button>
        <el-button
          type="primary"
          :loading="importSubmitting"
          :disabled="!importDialog.file"
          @click="submitImport"
        >
          开始导入
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, watch } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules, UploadInstance } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'
import {
  getPriceGroupList,
  createPriceGroup,
  updatePriceGroup,
  getPriceGroupOptions,
  getPriceDetailList,
  createPriceDetail,
  updatePriceDetail,
  lookupPriceBarcode,
  exportPriceDetail,
  importPriceDetail,
} from '@/api/modules/product-price'
import { getAllSalesRegions } from '@/api/modules/operation'
import type {
  PriceGroupViewVO,
  PriceDetailViewVO,
} from '@/api/modules/product-price'

// ================================================================
// Tab 状态
// ================================================================
const activeTab = ref('group')

function onTabChange() {
  if (activeTab.value === 'group') {
    loadGroupData()
  } else {
    loadDetailData()
  }
}

// ================================================================
// 大区选项（共用）
// ================================================================
const regionOptions = ref<{ code: string; name: string }[]>([])

async function loadRegions() {
  try {
    const res = await getAllSalesRegions()
    if (res.code === 0) {
      regionOptions.value = res.data?.records || []
    }
  } catch {
    // 下拉数据加载失败不阻塞页面
  }
}

// ================================================================
// Tab 1: 价格组
// ================================================================
const groupLoading = ref(false)
const groupTableData = ref<PriceGroupViewVO[]>([])
const groupTotal = ref(0)
const groupPageNum = ref(1)
const groupPageSize = ref(20)

const groupSearch = reactive({
  salesRegionCode: '',
  priceGroupName: '',
})

async function loadGroupData() {
  groupLoading.value = true
  try {
    const res = await getPriceGroupList({
      pageNum: groupPageNum.value,
      pageSize: groupPageSize.value,
      ...(groupSearch.salesRegionCode ? { salesRegionCode: groupSearch.salesRegionCode } : {}),
      ...(groupSearch.priceGroupName ? { priceGroupName: groupSearch.priceGroupName } : {}),
    })
    if (res.code === 0) {
      groupTableData.value = res.data.records || []
      groupTotal.value = Number(res.data.total) || 0
    }
  } finally {
    groupLoading.value = false
  }
}

function doGroupSearch() {
  groupPageNum.value = 1
  loadGroupData()
}

function resetGroupSearch() {
  groupSearch.salesRegionCode = ''
  groupSearch.priceGroupName = ''
  doGroupSearch()
}

watch([groupPageNum, groupPageSize], () => loadGroupData())

// 价格组新增/编辑
const groupFormRef = ref<FormInstance>()
const groupSubmitting = ref(false)

const groupDialog = reactive({
  visible: false,
  isEdit: false,
  editId: 0,
})

const groupForm = reactive({
  salesRegionCode: '',
  salesRegionName: '',
  priceGroupCode: '',
  priceGroupName: '',
  description: '',
})

const groupFormRules: FormRules = {
  salesRegionCode: [{ required: true, message: '请选择销售大区', trigger: 'change' }],
  priceGroupCode: [{ required: true, message: '请输入价格组编码', trigger: 'blur' }],
  priceGroupName: [{ required: true, message: '请输入价格组名称', trigger: 'blur' }],
}

function onGroupRegionChange(code: string) {
  const region = regionOptions.value.find((r) => r.code === code)
  groupForm.salesRegionName = region?.name || ''
}

function resetGroupForm() {
  groupForm.salesRegionCode = ''
  groupForm.salesRegionName = ''
  groupForm.priceGroupCode = ''
  groupForm.priceGroupName = ''
  groupForm.description = ''
}

function openGroupCreate() {
  groupDialog.isEdit = false
  groupDialog.editId = 0
  resetGroupForm()
  groupDialog.visible = true
}

function openGroupEdit(row: PriceGroupViewVO) {
  groupDialog.isEdit = true
  groupDialog.editId = row.id
  groupForm.salesRegionCode = row.salesRegionCode
  groupForm.salesRegionName = row.salesRegionName
  groupForm.priceGroupCode = row.priceGroupCode
  groupForm.priceGroupName = row.priceGroupName
  groupForm.description = row.description || ''
  groupDialog.visible = true
}

async function submitGroupForm() {
  const valid = await groupFormRef.value?.validate().catch(() => false)
  if (!valid) return

  groupSubmitting.value = true
  try {
    if (groupDialog.isEdit) {
      await updatePriceGroup({
        id: groupDialog.editId,
        priceGroupName: groupForm.priceGroupName,
        description: groupForm.description,
        updatedBy: '',
      })
      ElMessage.success('编辑成功')
    } else {
      await createPriceGroup({
        salesRegionCode: groupForm.salesRegionCode,
        salesRegionName: groupForm.salesRegionName,
        priceGroupCode: groupForm.priceGroupCode,
        priceGroupName: groupForm.priceGroupName,
        description: groupForm.description,
        createdBy: '',
      })
      ElMessage.success('新增成功')
    }
    groupDialog.visible = false
    loadGroupData()
  } finally {
    groupSubmitting.value = false
  }
}

// ================================================================
// Tab 2: 价格组明细
// ================================================================
const detailLoading = ref(false)
const detailTableData = ref<PriceDetailViewVO[]>([])
const detailTotal = ref(0)
const detailPageNum = ref(1)
const detailPageSize = ref(20)

const detailSearch = reactive({
  salesRegionCode: '',
  priceGroupName: '',
  keyword: '',
})

async function loadDetailData() {
  detailLoading.value = true
  try {
    const res = await getPriceDetailList({
      pageNum: detailPageNum.value,
      pageSize: detailPageSize.value,
      ...(detailSearch.salesRegionCode ? { salesRegionCode: detailSearch.salesRegionCode } : {}),
      ...(detailSearch.priceGroupName ? { priceGroupName: detailSearch.priceGroupName } : {}),
      ...(detailSearch.keyword ? { keyword: detailSearch.keyword } : {}),
    })
    if (res.code === 0) {
      detailTableData.value = res.data.records || []
      detailTotal.value = Number(res.data.total) || 0
    }
  } finally {
    detailLoading.value = false
  }
}

function doDetailSearch() {
  detailPageNum.value = 1
  loadDetailData()
}

function resetDetailSearch() {
  detailSearch.salesRegionCode = ''
  detailSearch.priceGroupName = ''
  detailSearch.keyword = ''
  doDetailSearch()
}

watch([detailPageNum, detailPageSize], () => loadDetailData())

// 价格组下拉选项（联动大区）
const priceGroupOptions = ref<PriceGroupViewVO[]>([])

async function loadPriceGroupOptions(salesRegionCode?: string) {
  try {
    const res = await getPriceGroupOptions(salesRegionCode)
    if (res.code === 0) {
      priceGroupOptions.value = res.data || []
    }
  } catch {
    priceGroupOptions.value = []
  }
}

// 明细新增
const detailCreateFormRef = ref<FormInstance>()
const detailCreateSubmitting = ref(false)
const barcodeLookupLoading = ref(false)

const detailCreateDialog = reactive({
  visible: false,
})

const detailCreateForm = reactive({
  salesRegionCode: '',
  priceGroupCode: '',
  priceGroupName: '',
  productBarcode: '',
  productName: '',
  price: undefined as number | undefined,
})

const detailCreateFormRules: FormRules = {
  salesRegionCode: [{ required: true, message: '请选择销售大区', trigger: 'change' }],
  priceGroupCode: [{ required: true, message: '请选择价格组', trigger: 'change' }],
  productBarcode: [{ required: true, message: '请输入商品条码', trigger: 'blur' }],
  price: [
    { required: true, message: '请输入售价', trigger: 'blur' },
    {
      type: 'number',
      min: 0.01,
      message: '售价必须大于0',
      trigger: 'blur',
    },
  ],
}

function onDetailRegionChange(code: string) {
  detailCreateForm.priceGroupCode = ''
  detailCreateForm.priceGroupName = ''
  priceGroupOptions.value = []
  if (code) {
    loadPriceGroupOptions(code)
  }
}

async function onBarcodeLookup() {
  const barcode = detailCreateForm.productBarcode
  if (!barcode) {
    ElMessage.warning('请输入商品条码')
    return
  }
  barcodeLookupLoading.value = true
  try {
    const res = await lookupPriceBarcode(barcode)
    if (res.code === 0) {
      detailCreateForm.productName = res.data.productName || ''
      ElMessage.success('商品条码校验通过')
    }
  } catch {
    detailCreateForm.productName = ''
  } finally {
    barcodeLookupLoading.value = false
  }
}

function resetDetailCreateForm() {
  detailCreateForm.salesRegionCode = ''
  detailCreateForm.priceGroupCode = ''
  detailCreateForm.priceGroupName = ''
  detailCreateForm.productBarcode = ''
  detailCreateForm.productName = ''
  detailCreateForm.price = undefined
  priceGroupOptions.value = []
}

function openDetailCreate() {
  resetDetailCreateForm()
  detailCreateDialog.visible = true
}

async function submitDetailCreateForm() {
  const valid = await detailCreateFormRef.value?.validate().catch(() => false)
  if (!valid) return

  detailCreateSubmitting.value = true
  try {
    const priceGroup = priceGroupOptions.value.find(
      (g) => g.priceGroupCode === detailCreateForm.priceGroupCode,
    )
    await createPriceDetail({
      salesRegionCode: detailCreateForm.salesRegionCode,
      priceGroupCode: detailCreateForm.priceGroupCode,
      priceGroupName: priceGroup?.priceGroupName || '',
      productBarcode: detailCreateForm.productBarcode,
      price: detailCreateForm.price!,
      createdBy: '',
    })
    ElMessage.success('新增成功')
    detailCreateDialog.visible = false
    loadDetailData()
  } finally {
    detailCreateSubmitting.value = false
  }
}

// 明细编辑
const detailEditFormRef = ref<FormInstance>()
const detailEditSubmitting = ref(false)

const detailEditDialog = reactive({
  visible: false,
  editRow: null as PriceDetailViewVO | null,
  salesRegionCode: '',
  priceGroupName: '',
  productBarcode: '',
  productName: '',
})

const detailEditForm = reactive({
  price: undefined as number | undefined,
  changeReason: '',
})

const detailEditFormRules: FormRules = {
  price: [
    { required: true, message: '请输入售价', trigger: 'blur' },
    {
      type: 'number',
      min: 0.01,
      message: '售价必须大于0',
      trigger: 'blur',
    },
  ],
  changeReason: [
    { required: true, message: '请输入变动原因', trigger: 'blur' },
  ],
}

function openDetailEdit(row: PriceDetailViewVO) {
  detailEditDialog.editRow = row
  detailEditDialog.salesRegionCode = row.salesRegionCode
  detailEditDialog.priceGroupName = row.priceGroupName
  detailEditDialog.productBarcode = row.productBarcode
  detailEditDialog.productName = row.productName
  detailEditForm.price = row.price
  detailEditForm.changeReason = ''
  detailEditDialog.visible = true
}

async function submitDetailEditForm() {
  const valid = await detailEditFormRef.value?.validate().catch(() => false)
  if (!valid) return

  if (!detailEditDialog.editRow) return

  detailEditSubmitting.value = true
  try {
    const res = await updatePriceDetail({
      id: detailEditDialog.editRow.id,
      price: detailEditForm.price!,
      changeReason: detailEditForm.changeReason,
      updatedBy: '',
    })
    if (res.code === 0) {
      const result = res.data
      if (result.approvalRequired) {
        ElMessage.warning(result.message)
      } else {
        ElMessage.success(result.message)
      }
    }
    detailEditDialog.visible = false
    loadDetailData()
  } finally {
    detailEditSubmitting.value = false
  }
}

// ================================================================
// 导出
// ================================================================
async function handleExport() {
  try {
    const res = await exportPriceDetail({
      ...(detailSearch.salesRegionCode ? { salesRegionCode: detailSearch.salesRegionCode } : {}),
      ...(detailSearch.priceGroupName ? { priceGroupName: detailSearch.priceGroupName } : {}),
      ...(detailSearch.keyword ? { keyword: detailSearch.keyword } : {}),
    })
    const blob = res as unknown as Blob
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `价格组明细_${new Date().toISOString().slice(0, 10)}.xlsx`
    link.click()
    window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch {
    // 错误已在拦截器中处理
  }
}

// ================================================================
// 导入
// ================================================================
const uploadRef = ref<UploadInstance>()
const importSubmitting = ref(false)

const importDialog = reactive<{
  visible: boolean
  file: File | null
  result: {
    createdCount: number
    updatedCount: number
    failedCount: number
    errors: Array<{ row: number; reason: string }>
  } | null
}>({
  visible: false,
  file: null,
  result: null,
})

function openImport() {
  importDialog.visible = true
  importDialog.file = null
  importDialog.result = null
}

function onFileChange(file: { raw?: File }) {
  importDialog.file = file.raw || null
  importDialog.result = null
}

function onFileRemove() {
  importDialog.file = null
  importDialog.result = null
}

async function submitImport() {
  if (!importDialog.file) return

  importSubmitting.value = true
  try {
    const res = await importPriceDetail(importDialog.file)
    if (res.code === 0) {
      importDialog.result = res.data
      ElMessage.success('导入完成')
      loadDetailData()
    }
  } finally {
    importSubmitting.value = false
  }
}

// ================================================================
// 初始化
// ================================================================
loadRegions()
loadGroupData()
</script>
