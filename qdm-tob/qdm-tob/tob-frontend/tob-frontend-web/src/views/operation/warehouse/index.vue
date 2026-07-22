<template>
  <div>
    <div class="tw-bg-white tw-rounded-[--border-radius-card] tw-px-5 tw-pt-4 tw-pb-1 tw-mb-3 tw-shadow-[--shadow-card]">
      <el-form :model="search" inline>
        <el-form-item label="仓库信息">
          <el-input v-model="search.keyword" placeholder="仓库编码/名称" clearable style="width:200px" @keyup.enter="doSearch" />
        </el-form-item>
        <el-form-item label="销售大区">
          <el-select v-model="search.region" placeholder="全部" clearable style="width:160px">
            <el-option v-for="r in regionOptions" :key="r.value" :label="r.label" :value="r.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="doSearch">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </div>
    <div class="tw-bg-white tw-rounded-[--border-radius-card] tw-p-5 tw-shadow-[--shadow-card]">
      <div class="tw-flex tw-justify-start tw-mb-4">
        <el-button type="primary" @click="openAdd">新增仓库</el-button>
      </div>
      <el-table :data="tableData" stripe v-loading="loading" style="width:100%"
        :header-cell-style="{ background: '#F1F5F9', color: '#475569', fontWeight: 600, fontSize: '13px' }">
        <el-table-column prop="code" label="仓库编码" min-width="100">
          <template #default="{ row }">
            <span class="tw-text-[--color-primary] tw-cursor-pointer tw-font-medium hover:tw-underline" @click="viewDetail(row)">{{ row.code }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="仓库名称" min-width="130" show-overflow-tooltip />
        <el-table-column label="销售大区" min-width="100">
          <template #default="{ row }"><span>{{ regionName(row.region) }}</span></template>
        </el-table-column>
        <el-table-column prop="type" label="仓库性质" min-width="100" />
        <el-table-column label="仓库地区" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">
            {{ districtDisplay(row) }}
          </template>
        </el-table-column>
        <el-table-column prop="address" label="仓库地址" min-width="180" show-overflow-tooltip />
        <el-table-column prop="lng" label="经度" min-width="100" align="right" />
        <el-table-column prop="lat" label="纬度" min-width="100" align="right" />
        <el-table-column prop="updatedBy" label="修改人" min-width="90" />
        <el-table-column prop="updatedAt" label="修改时间" min-width="160" />
        <el-table-column label="操作" min-width="80" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="openEdit(row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div v-if="tableData.length === 0 && !loading" class="tw-text-center tw-py-16 tw-text-gray-400">暂无数据</div>
      <div class="tw-mt-4 tw-flex">
        <el-pagination v-model:current-page="pageNum" v-model:page-size="pageSize" :page-sizes="[10, 20, 50]" :total="total" layout="total, sizes, prev, pager, next" background />
      </div>
    </div>

    <el-dialog v-model="dialog.visible" :title="dialog.isEdit ? '编辑仓库 - ' + form.code : '新增仓库'" width="560px" :close-on-click-modal="false" align-center @close="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="仓库编码" prop="code">
          <el-input v-model="form.code" :disabled="dialog.isEdit" placeholder="请输入仓库编码" maxlength="20" />
        </el-form-item>
        <el-form-item label="仓库名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入仓库名称" maxlength="50" />
        </el-form-item>
        <el-form-item label="销售大区" prop="region">
          <el-select v-model="form.region" placeholder="请选择销售大区" style="width:100%">
            <el-option v-for="r in regionOptions" :key="r.value" :label="r.label" :value="r.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="仓库性质" prop="type">
          <el-select v-model="form.type" placeholder="请选择仓库性质" style="width:100%">
            <el-option v-for="t in warehouseTypes" :key="t" :label="t" :value="t" />
          </el-select>
        </el-form-item>
        <el-form-item label="仓库地区" prop="district">
          <el-cascader v-model="form.district" :options="chinaAreaOptions" placeholder="请选择省/市/区" style="width:100%" />
        </el-form-item>
        <el-form-item label="仓库地址" prop="address">
          <el-input v-model="form.address" placeholder="请输入详细地址" maxlength="200" />
        </el-form-item>
        <el-form-item label="经度" prop="lng">
          <el-input-number v-model="form.lng" :precision="6" :min="-180" :max="180" style="width:100%" placeholder="经度" />
        </el-form-item>
        <el-form-item label="纬度" prop="lat">
          <el-input-number v-model="form.lat" :precision="6" :min="-90" :max="90" style="width:100%" placeholder="纬度" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog.visible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, watch, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { getWarehouseList, createWarehouse, updateWarehouse, type WarehouseViewVO, type WarehouseEditVO } from '@/api/modules/warehouse'
import { getAllSalesRegions } from '@/api/modules/operation'
import { chinaAreaOptions } from '@/utils/chinaArea'

const search = reactive({ keyword: '', region: '' })
const pageNum = ref(1); const pageSize = ref(20); const total = ref(0); const loading = ref(false)
const tableData = ref<WarehouseViewVO[]>([])

const regionOptions = ref<{ label: string; value: string }[]>([])
const warehouseTypes = ['蔬果仓', '猪肉仓', '水产仓', '禽肉仓', '综合仓']
async function loadRegions() {
  try { const r = await getAllSalesRegions(); if (r.code === 0) regionOptions.value = r.data.records.map((x: any) => ({ label: x.name, value: x.code })) } catch { }
}

function regionName(code: string): string {
  return regionOptions.value.find(r => r.value === code)?.label || code
}

// 从 chinaAreaOptions 构建 code → name 映射（含港澳台）
function buildAreaMap() {
  const map: Record<string, string> = {}
  function walk(list: typeof chinaAreaOptions) {
    for (const item of list) {
      map[item.value] = item.label
      if (item.children) walk(item.children)
    }
  }
  walk(chinaAreaOptions)
  return map
}

function districtDisplay(row: WarehouseViewVO): string {
  const map = buildAreaMap()
  // 港澳台只有 2 级（省→区），不显示空的市级
  const parts = [
    row.province ? (map[row.province] || row.province) : '',
    row.city ? (map[row.city] || row.city) : '',
    row.district ? (map[row.district] || row.district) : '',
  ].filter(Boolean)
  return parts.join(' ') || row.address || ''
}

async function loadData() {
  loading.value = true
  try {
    const r = await getWarehouseList({ pageNum: pageNum.value, pageSize: pageSize.value, keyword: search.keyword || undefined, region: search.region || undefined })
    if (r.code === 0) { tableData.value = r.data.records; total.value = r.data.total }
  } catch { } finally { loading.value = false }
}
function doSearch() { pageNum.value = 1; loadData() }
function resetSearch() { search.keyword = ''; search.region = ''; doSearch() }
watch([pageNum, pageSize], () => loadData())

function viewDetail(row: WarehouseViewVO) { ElMessage.info('仓库详情：' + row.code + ' ' + row.name) }

const dialog = reactive({ visible: false, isEdit: false })
const formRef = ref<FormInstance | null>(null)
const form = reactive({ code: '', name: '', region: '', type: '', district: [] as string[], address: '', lng: null as number | null, lat: null as number | null })
const rules: FormRules = {
  code: [{ required: true, message: '请输入仓库编码', trigger: 'blur' }, { pattern: /^[A-Z0-9]+$/, message: '编码仅支持大写字母和数字', trigger: 'blur' }],
  name: [{ required: true, message: '请输入仓库名称', trigger: 'blur' }],
  region: [{ required: true, message: '请选择销售大区', trigger: 'change' }],
  type: [{ required: true, message: '请选择仓库性质', trigger: 'change' }],
  district: [{ required: true, message: '请选择仓库地区', trigger: 'change' }],
  address: [{ required: true, message: '请输入详细地址', trigger: 'blur' }],
  lng: [{ required: true, message: '请输入经度', trigger: 'blur' }],
  lat: [{ required: true, message: '请输入纬度', trigger: 'blur' }],
}

function openAdd() { dialog.isEdit = false; resetForm(); dialog.visible = true }
function openEdit(row: WarehouseViewVO) {
  dialog.isEdit = true
  form.code = row.code; form.name = row.name; form.region = row.region; form.type = row.type
  form.district = [row.province, row.city, row.district].filter(Boolean)
  form.address = row.address; form.lng = row.lng; form.lat = row.lat
  dialog.visible = true; nextTick(() => formRef.value?.clearValidate())
}
function resetForm() {
  form.code = ''; form.name = ''; form.region = ''; form.type = ''
  form.district = []; form.address = ''; form.lng = null; form.lat = null
  nextTick(() => formRef.value?.clearValidate())
}
function getCurrentUser() { return localStorage.getItem('realName') || '' }

/** 兼容 2 级（港澳台）和 3 级（大陆）级联选择器输出，仅传编码 */
function getDistrictCodes(): [string, string, string] {
  if (form.district.length === 2) return [form.district[0], '', form.district[1]]
  return [form.district[0] || '', form.district[1] || '', form.district[2] || '']
}

function submitForm() {
  formRef.value?.validate(async (valid) => {
    if (!valid) return
    const userName = getCurrentUser()
    const [province, city, district] = getDistrictCodes()
    try {
      if (dialog.isEdit) {
        const d: WarehouseEditVO = { name: form.name, region: form.region, type: form.type, province, city, district, address: form.address, lng: form.lng ?? undefined, lat: form.lat ?? undefined, updatedBy: userName }
        await updateWarehouse(form.code, d); ElMessage.success('仓库信息更新成功')
      } else {
        await createWarehouse({ code: form.code, name: form.name, region: form.region, type: form.type, province, city, district, address: form.address, lng: form.lng ?? undefined, lat: form.lat ?? undefined, createdBy: userName })
        ElMessage.success('仓库创建成功')
      }
      dialog.visible = false; loadData(); loadRegions()
    } catch { }
  })
}

loadRegions(); loadData()
</script>
