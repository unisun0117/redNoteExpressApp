<template>
  <div class="tw-space-y-3">
    <!-- 筛选区域 -->
    <div
      class="tw-bg-white tw-rounded-[--border-radius-card] tw-px-5 tw-pt-4 tw-pb-1 tw-shadow-[--shadow-card]"
    >
      <el-form :model="search" inline>
        <el-form-item label="销售大区">
          <el-select
            v-model="search.salesRegionCode"
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
        <el-form-item label="商品条码">
          <el-input
            v-model="search.productBarcode"
            placeholder="请输入商品条码"
            clearable
            style="width: 160px"
          />
        </el-form-item>
        <el-form-item label="商品名称">
          <el-input
            v-model="search.productName"
            placeholder="请输入商品名称"
            clearable
            style="width: 160px"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select
            v-model="search.status"
            placeholder="全部"
            clearable
            style="width: 120px"
          >
            <el-option label="已上架" value="LISTED" />
            <el-option label="已下架" value="UNLISTED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="doSearch">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 数据区域 -->
    <div
      class="tw-bg-white tw-rounded-[--border-radius-card] tw-px-5 tw-pt-4 tw-pb-1 tw-shadow-[--shadow-card]"
    >
      <!-- 操作栏 -->
      <div class="tw-mb-3 tw-flex tw-justify-between tw-items-center">
        <div class="tw-space-x-2">
          <el-button type="primary" @click="openCreate">新增</el-button>
          <el-button @click="handleExport">导出</el-button>
          <el-button @click="openImport">导入</el-button>
        </div>
        <span class="tw-text-sm tw-text-[--text-secondary]"
          >共 {{ total }} 条记录</span
        >
      </div>

      <!-- 表格 -->
      <el-table
        v-loading="loading"
        :data="tableData"
        stripe
        :header-cell-style="{
          background: '#F1F5F9',
          color: '#475569',
          fontWeight: 600,
          fontSize: '13px',
        }"
        style="width: 100%"
      >
        <el-table-column
          prop="salesRegionName"
          label="销售大区"
          min-width="100"
        />
        <el-table-column
          prop="productBarcode"
          label="商品条码"
          min-width="120"
        />
        <el-table-column prop="productName" label="商品名称" min-width="140" />
        <el-table-column prop="warehouseCode" label="仓库编码" min-width="100" />
        <el-table-column prop="warehouseName" label="仓库名称" min-width="120" />
        <el-table-column label="状态" min-width="80">
          <template #default="{ row }">
            <el-tag
              :type="row.status === 'LISTED' ? 'success' : 'info'"
              size="small"
            >
              {{ row.status === 'LISTED' ? '已上架' : '已下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
          prop="miniappName"
          label="小程序名称"
          min-width="120"
        />
        <el-table-column prop="orderBaseQty" label="订购基数" min-width="90" />
        <el-table-column prop="orderMinQty" label="订购下限" min-width="90" />
        <el-table-column prop="orderMaxQty" label="订购上限" min-width="90" />
        <el-table-column prop="dailyStock" label="每日可用库存" min-width="110" />
        <el-table-column
          prop="dailyAvailable"
          label="今日可用数量"
          min-width="110"
        />
        <el-table-column
          prop="dailySold"
          label="今日已售数量"
          min-width="110"
        />
        <el-table-column label="操作" min-width="160" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="openEdit(row)"
              >编辑</el-button
            >
            <el-button
              type="warning"
              link
              size="small"
              @click="openStockAdjust(row)"
            >
              调整库存
            </el-button>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无商品资料" />
        </template>
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
          @current-change="loadData"
          @size-change="onPageSizeChange"
        />
      </div>
    </div>

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      v-model="formDialog.visible"
      :title="formDialog.isEdit ? '编辑商品资料' : '新增商品资料'"
      width="700px"
      align-center
      :close-on-click-modal="false"
      destroy-on-close
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="formRules"
        label-width="110px"
        style="max-height: 60vh; overflow-y: auto; padding-right: 10px"
      >
        <el-divider content-position="left">基础信息</el-divider>
        <el-form-item label="销售大区" prop="salesRegionCode">
          <el-select
            v-model="form.salesRegionCode"
            placeholder="请选择"
            :disabled="formDialog.isEdit"
            style="width: 100%"
            @change="onRegionChange"
          >
            <el-option
              v-for="item in regionOptions"
              :key="item.code"
              :label="item.name"
              :value="item.code"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="商品条码" prop="productBarcode">
          <el-input
            v-model="form.productBarcode"
            placeholder="请输入商品条码"
            :disabled="formDialog.isEdit"
            @blur="onBarcodeBlur"
          />
        </el-form-item>
        <el-form-item label="商品名称">
          <el-input v-model="form.productName" disabled placeholder="输入条码后自动查询" />
        </el-form-item>
        <el-form-item label="仓库" prop="warehouseCode">
          <el-select
            v-model="form.warehouseCode"
            placeholder="请先选择销售大区"
            style="width: 100%"
            @change="onWarehouseChange"
          >
            <el-option
              v-for="item in warehouseOptions"
              :key="item.code"
              :label="item.name"
              :value="item.code"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="form.status" style="width: 100%">
            <el-option label="已上架" value="LISTED" />
            <el-option label="已下架" value="UNLISTED" />
          </el-select>
        </el-form-item>
        <el-form-item label="小程序名称">
          <el-input
            v-model="form.miniappName"
            placeholder="选填，最长100字符"
            maxlength="100"
          />
        </el-form-item>

        <el-divider content-position="left">订购约束</el-divider>
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="订购基数" prop="orderBaseQty" label-width="80px">
              <el-input-number
                v-model="form.orderBaseQty"
                :precision="2"
                :min="0"
                style="width: 100%"
                controls-position="right"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="订购下限" prop="orderMinQty" label-width="80px">
              <el-input-number
                v-model="form.orderMinQty"
                :precision="2"
                :min="0"
                style="width: 100%"
                controls-position="right"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="订购上限" prop="orderMaxQty" label-width="80px">
              <el-input-number
                v-model="form.orderMaxQty"
                :precision="2"
                :min="0"
                style="width: 100%"
                controls-position="right"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">库存</el-divider>
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="每日可用库存" prop="dailyStock" label-width="105px">
              <el-input-number
                v-model="form.dailyStock"
                :precision="2"
                :min="0"
                :disabled="formDialog.isEdit"
                style="width: 100%"
                controls-position="right"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="今日可用数量" label-width="105px">
              <el-input-number
                :model-value="form.dailyStock"
                :precision="2"
                disabled
                style="width: 100%"
                controls-position="right"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="今日已售数量" label-width="105px">
              <el-input-number
                :model-value="0"
                :precision="2"
                disabled
                style="width: 100%"
                controls-position="right"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">图片与详情</el-divider>
        <el-form-item label="商品主图">
          <el-input v-model="form.mainImage" placeholder="图片URL（上传功能待开放）" />
        </el-form-item>
        <el-form-item label="轮播图">
          <el-input
            v-model="form.carouselImages"
            placeholder="多个URL用逗号分隔（最多9张）"
          />
        </el-form-item>
        <el-form-item label="商品详情">
          <el-input
            v-model="form.productDetail"
            type="textarea"
            :rows="5"
            placeholder="富文本编辑器待集成"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formDialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="formSubmitting" @click="submitForm"
          >保存</el-button
        >
      </template>
    </el-dialog>

    <!-- 库存调整弹窗 -->
    <el-dialog
      v-model="stockDialog.visible"
      title="调整库存"
      width="480px"
      align-center
      :close-on-click-modal="false"
      destroy-on-close
    >
      <el-form :model="stockDialog" label-width="110px">
        <el-form-item label="销售大区">
          <el-input :model-value="stockDialog.salesRegionName" disabled />
        </el-form-item>
        <el-form-item label="商品条码">
          <el-input :model-value="stockDialog.productBarcode" disabled />
        </el-form-item>
        <el-form-item label="商品名称">
          <el-input :model-value="stockDialog.productName" disabled />
        </el-form-item>
        <el-form-item label="今日可用库存" required>
          <el-input-number
            v-model="stockDialog.newDailyStock"
            :precision="2"
            :min="0"
            style="width: 100%"
            controls-position="right"
          />
        </el-form-item>
        <el-form-item label="今日可用数量">
          <el-input-number
            :model-value="stockAdjustedAvailable"
            :precision="2"
            disabled
            style="width: 100%"
            controls-position="right"
          />
          <span class="tw-text-xs tw-text-[--text-secondary] tw-mt-1"
            >= 今日可用库存 - 今日已售数量</span
          >
        </el-form-item>
        <el-form-item label="今日已售数量">
          <el-input-number
            :model-value="stockDialog.dailySold"
            :precision="2"
            disabled
            style="width: 100%"
            controls-position="right"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="stockDialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="stockSubmitting" @click="submitStockAdjust"
          >确认</el-button
        >
      </template>
    </el-dialog>

    <!-- 导入弹窗 -->
    <el-dialog
      v-model="importDialog.visible"
      title="导入商品资料"
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
            <el-button type="primary" link size="small" @click="downloadTemplate"
              >下载导入模板</el-button
            >
            仅支持 .xlsx / .xls 格式，单次上限 1000 条
          </div>
        </template>
      </el-upload>

      <!-- 导入结果 -->
      <div v-if="importDialog.result" class="tw-mt-4">
        <el-alert
          :title="`导入完成：新增 ${importDialog.result.createdCount} 条，更新 ${importDialog.result.updatedCount} 条，失败 ${importDialog.result.failedCount} 条`"
          :type="importDialog.result.failedCount > 0 ? 'warning' : 'success'"
          :closable="false"
        />
        <div
          v-if="importDialog.result.errors.length > 0"
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
import { ref, reactive, computed, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules, UploadInstance } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'
import {
  getProductCatalogPage,
  createProductCatalog,
  updateProductCatalog,
  adjustStock,
  lookupBarcode,
  getWarehouses,
  exportProductCatalog,
  importProductCatalog,
} from '@/api/modules/product-catalog'
import { getSalesRegionList, getSalesRegionDetail, getAllSalesRegions } from '@/api/modules/operation'
import type {
  ProductCatalogViewVO,
  ProductCatalogQuery,
  ImportResultVO,
  WarehouseSimpleVO,
} from '@/api/modules/product-catalog'

// ================================================================
// 列表数据
// ================================================================
const loading = ref(false)
const tableData = ref<ProductCatalogViewVO[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(20)

const search = reactive<ProductCatalogQuery>({
  pageNum: 1,
  pageSize: 20,
  salesRegionCode: '',
  productBarcode: '',
  productName: '',
  status: '',
})

// 大区选项
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

async function loadData() {
  loading.value = true
  try {
    const query: ProductCatalogQuery = {
      pageNum: pageNum.value,
      pageSize: pageSize.value,
    }
    if (search.salesRegionCode) query.salesRegionCode = search.salesRegionCode
    if (search.productBarcode) query.productBarcode = search.productBarcode
    if (search.productName) query.productName = search.productName
    if (search.status) query.status = search.status

    const res = await getProductCatalogPage(query)
    if (res.code === 0) {
      tableData.value = res.data.records || []
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

function onPageSizeChange() {
  pageNum.value = 1
  loadData()
}

function resetSearch() {
  search.salesRegionCode = ''
  search.productBarcode = ''
  search.productName = ''
  search.status = ''
  doSearch()
}

// ================================================================
// 新增/编辑
// ================================================================
const formRef = ref<FormInstance>()
const formSubmitting = ref(false)
const warehouseOptions = ref<WarehouseSimpleVO[]>([])

const formDialog = reactive({
  visible: false,
  isEdit: false,
  editId: 0,
})

const form = reactive({
  salesRegionCode: '',
  salesRegionName: '',
  productBarcode: '',
  productName: '',
  warehouseCode: '',
  warehouseName: '',
  status: 'LISTED',
  miniappName: '',
  mainImage: '',
  carouselImages: '',
  productDetail: '',
  orderBaseQty: 0,
  orderMinQty: 0,
  orderMaxQty: 0,
  dailyStock: 0,
})

const formRules: FormRules = {
  salesRegionCode: [{ required: true, message: '请选择销售大区', trigger: 'change' }],
  productBarcode: [{ required: true, message: '请输入商品条码', trigger: 'blur' }],
  warehouseCode: [{ required: true, message: '请选择仓库', trigger: 'change' }],
  orderBaseQty: [{ required: true, message: '请输入订购基数', trigger: 'blur' }],
  orderMinQty: [{ required: true, message: '请输入订购下限', trigger: 'blur' }],
  orderMaxQty: [{ required: true, message: '请输入订购上限', trigger: 'blur' }],
  dailyStock: [{ required: true, message: '请输入每日可用库存', trigger: 'blur' }],
}

function resetForm() {
  form.salesRegionCode = ''
  form.salesRegionName = ''
  form.productBarcode = ''
  form.productName = ''
  form.warehouseCode = ''
  form.warehouseName = ''
  form.status = 'LISTED'
  form.miniappName = ''
  form.mainImage = ''
  form.carouselImages = ''
  form.productDetail = ''
  form.orderBaseQty = 0
  form.orderMinQty = 0
  form.orderMaxQty = 0
  form.dailyStock = 0
  warehouseOptions.value = []
}

async function loadWarehouses() {
  if (!form.salesRegionCode) {
    warehouseOptions.value = []
    return
  }
  try {
    const res = await getWarehouses(form.salesRegionCode)
    if (res.code === 0) {
      warehouseOptions.value = res.data || []
    }
  } catch {
    warehouseOptions.value = []
  }
}

function onRegionChange() {
  form.warehouseCode = ''
  form.warehouseName = ''
  // 大区名称回填
  const region = regionOptions.value.find((r) => r.code === form.salesRegionCode)
  form.salesRegionName = region?.name || ''
  loadWarehouses()
}

function onWarehouseChange() {
  const wh = warehouseOptions.value.find((w) => w.code === form.warehouseCode)
  form.warehouseName = wh?.name || ''
}

async function onBarcodeBlur() {
  if (!form.productBarcode || formDialog.isEdit) return
  try {
    const res = await lookupBarcode(form.productBarcode)
    if (res.code === 0) {
      form.productName = res.data.productName
    }
  } catch {
    form.productName = ''
  }
}

function openCreate() {
  resetForm()
  formDialog.isEdit = false
  formDialog.editId = 0
  formDialog.visible = true
  nextTick(() => formRef.value?.clearValidate())
}

function openEdit(row: ProductCatalogViewVO) {
  resetForm()
  formDialog.isEdit = true
  formDialog.editId = row.id
  form.salesRegionCode = row.salesRegionCode
  form.salesRegionName = row.salesRegionName
  form.productBarcode = row.productBarcode
  form.productName = row.productName
  form.warehouseCode = row.warehouseCode
  form.warehouseName = row.warehouseName
  form.status = row.status
  form.miniappName = row.miniappName
  form.mainImage = row.mainImage
  form.carouselImages = row.carouselImages
  form.productDetail = row.productDetail
  form.orderBaseQty = row.orderBaseQty
  form.orderMinQty = row.orderMinQty
  form.orderMaxQty = row.orderMaxQty
  form.dailyStock = row.dailyStock
  formDialog.visible = true
  loadWarehouses()
  nextTick(() => formRef.value?.clearValidate())
}

async function submitForm() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    formSubmitting.value = true
    try {
      if (formDialog.isEdit) {
        await updateProductCatalog(formDialog.editId, {
          warehouseCode: form.warehouseCode,
          warehouseName: form.warehouseName,
          status: form.status,
          miniappName: form.miniappName,
          mainImage: form.mainImage,
          carouselImages: form.carouselImages,
          productDetail: form.productDetail,
          orderBaseQty: form.orderBaseQty,
          orderMinQty: form.orderMinQty,
          orderMaxQty: form.orderMaxQty,
          updatedBy: '',
        })
        ElMessage.success('编辑成功')
      } else {
        await createProductCatalog({
          salesRegionCode: form.salesRegionCode,
          salesRegionName: form.salesRegionName,
          productBarcode: form.productBarcode,
          warehouseCode: form.warehouseCode,
          warehouseName: form.warehouseName,
          status: form.status,
          miniappName: form.miniappName,
          mainImage: form.mainImage,
          carouselImages: form.carouselImages,
          productDetail: form.productDetail,
          orderBaseQty: form.orderBaseQty,
          orderMinQty: form.orderMinQty,
          orderMaxQty: form.orderMaxQty,
          dailyStock: form.dailyStock,
          createdBy: '',
        })
        ElMessage.success('新增成功')
      }
      formDialog.visible = false
      loadData()
    } finally {
      formSubmitting.value = false
    }
  })
}

// ================================================================
// 库存调整
// ================================================================
const stockSubmitting = ref(false)

const stockDialog = reactive({
  visible: false,
  id: 0,
  salesRegionName: '',
  productBarcode: '',
  productName: '',
  dailySold: 0,
  newDailyStock: 0,
})

const stockAdjustedAvailable = computed(() => {
  return +(stockDialog.newDailyStock - stockDialog.dailySold).toFixed(2)
})

function openStockAdjust(row: ProductCatalogViewVO) {
  stockDialog.id = row.id
  stockDialog.salesRegionName = row.salesRegionName
  stockDialog.productBarcode = row.productBarcode
  stockDialog.productName = row.productName
  stockDialog.dailySold = row.dailySold
  stockDialog.newDailyStock = row.dailyStock
  stockDialog.visible = true
}

async function submitStockAdjust() {
  stockSubmitting.value = true
  try {
    await adjustStock(stockDialog.id, {
      newDailyStock: stockDialog.newDailyStock,
    })
    ElMessage.success('库存调整成功')
    stockDialog.visible = false
    loadData()
  } finally {
    stockSubmitting.value = false
  }
}

// ================================================================
// 导出
// ================================================================
async function handleExport() {
  try {
    const query: ProductCatalogQuery = {}
    if (search.salesRegionCode) query.salesRegionCode = search.salesRegionCode
    if (search.productBarcode) query.productBarcode = search.productBarcode
    if (search.productName) query.productName = search.productName
    if (search.status) query.status = search.status

    const res = await exportProductCatalog(query)
    const blob = new Blob([res as unknown as BlobPart], {
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
    })
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = '商品资料.xlsx'
    a.click()
    window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch {
    ElMessage.error('导出失败')
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
  result: ImportResultVO | null
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
}

function downloadTemplate() {
  // 导出空模板：调用导出接口获取表头
  ElMessage.info('请先导出一份数据作为模板参考')
  handleExport()
}

async function submitImport() {
  if (!importDialog.file) {
    ElMessage.warning('请选择文件')
    return
  }
  importSubmitting.value = true
  try {
    const res = await importProductCatalog(importDialog.file)
    if (res.code === 0) {
      importDialog.result = res.data
      loadData()
    }
  } finally {
    importSubmitting.value = false
  }
}

// ================================================================
// 初始化
// ================================================================
loadRegions()
loadData()
</script>
