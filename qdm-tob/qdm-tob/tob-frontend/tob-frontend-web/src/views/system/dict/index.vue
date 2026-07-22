<script setup lang="ts">
import { ref, reactive, watch, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import {
  getDictList,
  getDictItems,
  createDict,
  updateDict,
  deleteDict,
  updateDictItem,
  batchCreateItems,
  deleteDictItem,
} from '@/api/modules/dict'
import type { DictVO, DictItemVO } from '@/types/dict'

// ======================== 左侧：字典列表 ========================
const dictLoading = ref(false)
const dictList = ref<DictVO[]>([])
const dictTotal = ref(0)
const dictPage = ref(1)
const dictSize = ref(10)
const dictKeyword = ref('')
const selectedDictCode = ref('')

async function loadDictList() {
  dictLoading.value = true
  try {
    const result = await getDictList({
      page: dictPage.value,
      size: dictSize.value,
      keyword: dictKeyword.value || undefined,
    })
    dictList.value = result.records
    dictTotal.value = result.total
  } finally {
    dictLoading.value = false
  }
}

function onDictPageChange(page: number) {
  dictPage.value = page
  loadDictList()
}

function onDictSizeChange(size: number) {
  dictSize.value = size
  dictPage.value = 1
  loadDictList()
}

function onSearch() {
  dictPage.value = 1
  loadDictList()
}

function selectDict(code: string) {
  selectedDictCode.value = code
  loadItems()
}

// ======================== 右侧：字典头部编辑 ========================
const editingHeader = ref(false)
const savingHeader = ref(false)
const headerForm = reactive({
  name: '',
  description: '',
  status: 'ACTIVE' as 'ACTIVE' | 'INACTIVE',
})

const selectedDict = ref<DictVO | undefined>()

watch(selectedDictCode, (code) => {
  selectedDict.value = dictList.value.find((d) => d.code === code)
})

function startEditHeader() {
  const dict = selectedDict.value
  if (!dict) return
  headerForm.name = dict.name
  headerForm.description = dict.description
  headerForm.status = dict.status
  editingHeader.value = true
}

function cancelEditHeader() {
  editingHeader.value = false
}

async function saveHeader() {
  if (savingHeader.value) return
  savingHeader.value = true
  try {
    await updateDict({
      code: selectedDictCode.value,
      name: headerForm.name,
      description: headerForm.description,
      status: headerForm.status,
    })
    editingHeader.value = false
    loadDictList()
    ElMessage.success('字典保存成功')
  } finally {
    savingHeader.value = false
  }
}

// ======================== 右侧：字典项表格 ========================
const itemsLoading = ref(false)
const savingItem = ref(false)
const items = ref<DictItemVO[]>([])
const editingItemValue = ref<string | null>(null)
const editingItemForm = reactive({ label: '', sort: 0, status: 'ACTIVE' as 'ACTIVE' | 'INACTIVE' })

async function loadItems() {
  if (!selectedDictCode.value) return
  itemsLoading.value = true
  try {
    const result = await getDictItems({ code: selectedDictCode.value })
    items.value = result || []
  } finally {
    itemsLoading.value = false
  }
}

function startEditItem(item: DictItemVO) {
  editingItemValue.value = item.value
  editingItemForm.label = item.label
  editingItemForm.sort = item.sort
  editingItemForm.status = item.status
}

function cancelEditItem() {
  editingItemValue.value = null
}

async function saveItem(item: DictItemVO) {
  if (savingItem.value) return
  savingItem.value = true
  try {
    await updateDictItem({
      dictCode: item.dictCode,
      value: item.value,
      label: editingItemForm.label,
      sort: editingItemForm.sort,
      status: editingItemForm.status,
    })
    editingItemValue.value = null
    loadItems()
    ElMessage.success('字典项保存成功')
  } finally {
    savingItem.value = false
  }
}

// ======================== 字典项批量新增 ========================
interface NewItemRow {
  value: string
  label: string
  sort: number
  status: 'ACTIVE' | 'INACTIVE'
}

const newItems = ref<NewItemRow[]>([])
const savingNewItems = ref(false)

function addNewRow() {
  newItems.value.push({ value: '', label: '', sort: newItems.value.length + 1, status: 'ACTIVE' })
}

function removeNewRow(index: number) {
  newItems.value.splice(index, 1)
}

async function saveNewItems() {
  if (savingNewItems.value) return
  const valid = newItems.value.filter((i) => i.value.trim() && i.label.trim())
  if (valid.length === 0) {
    ElMessage.warning('请至少填写一个字典项的编码和名称')
    return
  }
  savingNewItems.value = true
  try {
    await batchCreateItems(
      selectedDictCode.value,
      valid.map((i) => ({
        value: i.value.trim(),
        label: i.label.trim(),
        sort: i.sort,
        status: i.status,
      })),
    )
    newItems.value = []
    loadItems()
    ElMessage.success(`成功添加 ${valid.length} 个字典项`)
  } finally {
    savingNewItems.value = false
  }
}

// ======================== 删除 ========================
async function handleDeleteDict(dict: DictVO) {
  await ElMessageBox.confirm(
    `确定要删除字典「${dict.name}(${dict.code})」及其全部字典项吗？`,
    '删除确认',
    { type: 'warning', confirmButtonText: '确认', cancelButtonText: '取消' },
  )
  await deleteDict(dict.code)
  if (selectedDictCode.value === dict.code) {
    selectedDictCode.value = ''
    items.value = []
  }
  loadDictList()
  ElMessage.success('字典已删除')
}

async function handleDeleteItem(item: DictItemVO) {
  await ElMessageBox.confirm(`确定要删除字典项「${item.value} - ${item.label}」吗？`, '删除确认', {
    type: 'warning',
    confirmButtonText: '确认',
    cancelButtonText: '取消',
  })
  await deleteDictItem(item.dictCode, item.value)
  loadItems()
  ElMessage.success('字典项已删除')
}

// ======================== 新增字典弹窗 ========================
const createVisible = ref(false)
const creating = ref(false)
const createFormRef = ref<FormInstance>()
const createForm = reactive({
  code: '',
  name: '',
  description: '',
  items: [{ value: '', label: '', sort: 1, status: 'ACTIVE' as 'ACTIVE' | 'INACTIVE' }],
})

const createRules: FormRules = {
  code: [
    { required: true, message: '请输入字典编码', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_]+$/, message: '仅支持字母、数字、下划线', trigger: 'blur' },
  ],
  name: [{ required: true, message: '请输入字典名称', trigger: 'blur' }],
}

// 字典项编码校验：编码为空但名称有值时 → 编码不能为空
function getItemValueRule(idx: number) {
  return {
    validator: (_rule: unknown, _value: unknown, callback: (msg?: Error) => void) => {
      const label = createForm.items[idx]?.label?.trim()
      if (!String(_value || '').trim() && label) {
        callback(new Error('编码不能为空'))
      } else {
        callback()
      }
    },
    trigger: 'blur',
  }
}

// 字典项名称校验：名称为空但编码有值时 → 名称不能为空
function getItemLabelRule(idx: number) {
  return {
    validator: (_rule: unknown, _value: unknown, callback: (msg?: Error) => void) => {
      const code = createForm.items[idx]?.value?.trim()
      if (!String(_value || '').trim() && code) {
        callback(new Error('名称不能为空'))
      } else {
        callback()
      }
    },
    trigger: 'blur',
  }
}

function openCreateDialog() {
  createForm.code = ''
  createForm.name = ''
  createForm.description = ''
  createForm.items = [{ value: '', label: '', sort: 1, status: 'ACTIVE' }]
  createVisible.value = true
  nextTick(() => {
    createFormRef.value?.clearValidate()
  })
}

function addCreateItem() {
  createForm.items.push({
    value: '',
    label: '',
    sort: createForm.items.length + 1,
    status: 'ACTIVE',
  })
}

function removeCreateItem(index: number) {
  createForm.items.splice(index, 1)
}

async function submitCreate() {
  if (creating.value) return
  const valid = await createFormRef.value?.validate().catch(() => false)
  if (!valid) return

  const filledItems = createForm.items.filter((i) => i.value.trim() && i.label.trim())

  creating.value = true
  try {
    await createDict({
      code: createForm.code,
      name: createForm.name,
      description: createForm.description,
      status: 'ACTIVE',
      items: filledItems.map((item) => ({
        value: item.value.trim(),
        label: item.label.trim(),
        sort: item.sort,
        status: item.status,
      })),
    })
    createVisible.value = false
    dictPage.value = 1
    await loadDictList()
    selectedDictCode.value = createForm.code
    await loadItems()
    ElMessage.success('字典创建成功')
  } finally {
    creating.value = false
  }
}

// ======================== 初始化 ========================
onMounted(() => {
  loadDictList()
})
</script>

<template>
  <div class="tw-flex tw-gap-4 tw-h-full">
    <!-- ===== 左侧：字典列表 ===== -->
    <div
      class="tw-w-72 tw-flex-shrink-0 tw-border tw-border-gray-200 tw-rounded-lg tw-overflow-hidden tw-flex tw-flex-col"
    >
      <div class="tw-p-3 tw-border-b tw-border-gray-200 tw-bg-gray-50">
        <el-input
          v-model="dictKeyword"
          placeholder="搜索字典编码/名称"
          size="small"
          clearable
          @keyup.enter="onSearch"
          @clear="onSearch"
        >
          <template #append>
            <el-button @click="onSearch">查询</el-button>
          </template>
        </el-input>
      </div>

      <div
        class="tw-px-3 tw-py-2 tw-border-b tw-border-gray-200 tw-bg-gray-50 tw-flex tw-justify-between tw-items-center"
      >
        <span class="tw-text-xs tw-text-gray-500">共 {{ dictTotal }} 个字典</span>
        <el-button type="primary" size="small" @click="openCreateDialog">+ 新增字典</el-button>
      </div>

      <div v-loading="dictLoading" class="tw-flex-1 tw-overflow-y-auto">
        <div
          v-if="dictList.length === 0"
          class="tw-text-center tw-py-10 tw-text-gray-400 tw-text-sm"
        >
          暂无字典数据
        </div>
        <div
          v-for="dict in dictList"
          :key="dict.code"
          class="tw-relative tw-px-4 tw-py-3 tw-border-l-4 tw-cursor-pointer tw-border-b tw-border-gray-100 hover:tw-bg-gray-50 tw-transition-colors tw-min-h-[72px]"
          :class="
            selectedDictCode === dict.code
              ? 'tw-border-l-emerald-500 tw-bg-emerald-50'
              : 'tw-border-l-transparent'
          "
          @click="selectDict(dict.code)"
        >
          <div class="tw-text-sm tw-font-semibold tw-text-slate-800 tw-truncate tw-pr-16">{{ dict.name }}</div>
          <div class="tw-text-xs tw-text-slate-400 tw-mt-0.5 tw-pr-16">{{ dict.code }}</div>
          <div class="tw-text-xs tw-text-slate-400 tw-mt-0.5 tw-pr-16 tw-truncate">{{ dict.description || ' ' }}</div>
          <el-tag class="!tw-absolute tw-top-3 tw-right-4" :type="dict.status === 'ACTIVE' ? 'success' : 'info'" size="small">
            {{ dict.status === 'ACTIVE' ? '启用' : '停用' }}
          </el-tag>
        </div>
      </div>

      <div
        v-if="dictTotal > 10"
        class="tw-p-3 tw-border-t tw-border-gray-200 tw-flex tw-justify-center"
      >
        <el-pagination
          v-model:current-page="dictPage"
          v-model:page-size="dictSize"
          :total="dictTotal"
          :page-sizes="[10, 20, 50]"
          layout="prev, pager, next, sizes"
          small
          background
          @current-change="onDictPageChange"
          @size-change="onDictSizeChange"
        />
      </div>
    </div>

    <!-- ===== 右侧：字典详情 ===== -->
    <div
      class="tw-flex-1 tw-border tw-border-gray-200 tw-rounded-lg tw-overflow-hidden tw-flex tw-flex-col"
    >
      <!-- 空状态 -->
      <div
        v-if="!selectedDictCode"
        class="tw-flex-1 tw-flex tw-items-center tw-justify-center tw-text-gray-400 tw-text-sm"
      >
        📖 请从左侧选择一个字典
      </div>

      <template v-else>
        <!-- 字典头部 -->
        <div class="tw-p-4 tw-border-b tw-border-gray-200 tw-bg-gray-50">
          <div class="tw-flex tw-justify-between tw-items-center">
            <div class="tw-flex tw-items-center tw-gap-3">
              <el-tag type="info" size="small">{{ selectedDictCode }}</el-tag>
              <template v-if="editingHeader">
                <el-input
                  v-model="headerForm.name"
                  size="small"
                  style="width: 130px"
                  placeholder="字典名称"
                />
                <el-input
                  v-model="headerForm.description"
                  size="small"
                  style="width: 180px"
                  placeholder="用途描述"
                />
                <el-select v-model="headerForm.status" size="small" style="width: 90px">
                  <el-option label="启用" value="ACTIVE" />
                  <el-option label="停用" value="INACTIVE" />
                </el-select>
                <el-button type="success" size="small" :loading="savingHeader" @click="saveHeader"
                  >保存</el-button
                >
                <el-button size="small" @click="cancelEditHeader">取消</el-button>
              </template>
              <template v-else>
                <span class="tw-text-base tw-font-semibold">{{ selectedDict?.name }}</span>
                <span class="tw-text-sm tw-text-gray-500">{{ selectedDict?.description }}</span>
                <el-tag :type="selectedDict?.status === 'ACTIVE' ? 'success' : 'info'" size="small">
                  {{ selectedDict?.status === 'ACTIVE' ? '启用' : '停用' }}
                </el-tag>
              </template>
            </div>
            <div v-if="!editingHeader" class="tw-flex tw-items-center tw-gap-2">
              <el-button size="small" @click="startEditHeader">编辑</el-button>
              <el-button type="danger" plain size="small" @click="handleDeleteDict(selectedDict!)">删除</el-button>
            </div>
          </div>
        </div>

        <!-- 字典项工具栏 -->
        <div
          class="tw-px-4 tw-py-2 tw-border-b tw-border-gray-200 tw-flex tw-justify-between tw-items-center"
        >
          <el-button size="small" @click="addNewRow">+ 添加一行</el-button>
          <el-button
            v-if="newItems.length > 0"
            type="primary"
            size="small"
            :loading="savingNewItems"
            @click="saveNewItems"
          >
            全部保存 ({{ newItems.length }})
          </el-button>
        </div>

        <!-- 字典项表格（可滚动） -->
        <div v-loading="itemsLoading" class="tw-flex-1 tw-overflow-y-auto">
          <el-table :data="items" stripe size="small" style="width: 100%">
            <el-table-column prop="value" label="编码" width="100">
              <template #default="{ row }">
                <code class="tw-text-xs">{{ row.value }}</code>
              </template>
            </el-table-column>
            <el-table-column prop="label" label="名称">
              <template #default="{ row }">
                <template v-if="editingItemValue === row.value">
                  <el-input v-model="editingItemForm.label" size="small" />
                </template>
                <template v-else>
                  <span class="tw-font-medium">{{ row.label }}</span>
                </template>
              </template>
            </el-table-column>
            <el-table-column prop="sort" label="顺序" width="100" align="center">
              <template #default="{ row }">
                <template v-if="editingItemValue === row.value">
                  <div class="tw-inline-flex tw-items-center tw-gap-0">
                    <el-button size="small" style="min-width:28px;padding:2px 6px" @click="editingItemForm.sort = Math.max(1, editingItemForm.sort - 1)">-</el-button>
                    <span class="tw-w-8 tw-text-center tw-tabular-nums">{{ editingItemForm.sort }}</span>
                    <el-button size="small" style="min-width:28px;padding:2px 6px" @click="editingItemForm.sort++">+</el-button>
                  </div>
                </template>
                <template v-else>{{ row.sort }}</template>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="80" align="center">
              <template #default="{ row }">
                <template v-if="editingItemValue === row.value">
                  <el-select v-model="editingItemForm.status" size="small">
                    <el-option label="启用" value="ACTIVE" />
                    <el-option label="停用" value="INACTIVE" />
                  </el-select>
                </template>
                <template v-else>
                  <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'info'" size="small">
                    {{ row.status === 'ACTIVE' ? '启用' : '停用' }}
                  </el-tag>
                </template>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120" align="center" fixed="right">
              <template #default="{ row }">
                <template v-if="editingItemValue === row.value">
                  <el-button
                    type="success"
                    link
                    size="small"
                    :loading="savingItem"
                    @click="saveItem(row)"
                    >✓ 保存</el-button
                  >
                  <el-button type="info" link size="small" @click="cancelEditItem">✕</el-button>
                </template>
                <template v-else>
                  <el-button type="primary" link size="small" @click="startEditItem(row)"
                    >编辑</el-button
                  >
                  <el-button type="danger" link size="small" @click="handleDeleteItem(row)"
                    >删除</el-button
                  >
                </template>
              </template>
            </el-table-column>
          </el-table>

          <!-- 新增行（绿色背景） -->
          <div
            v-for="(item, idx) in newItems"
            :key="'new-' + idx"
            class="tw-flex tw-items-center tw-gap-3 tw-px-3 tw-py-1.5 tw-bg-emerald-50 tw-border-b tw-border-emerald-100 tw-text-sm"
          >
            <div style="width: 100px">
              <el-input v-model="item.value" placeholder="编码" size="small" />
            </div>
            <div class="tw-flex-1">
              <el-input v-model="item.label" placeholder="名称" size="small" />
            </div>
            <div class="tw-flex tw-items-center tw-gap-0" style="width:82px">
              <el-button size="small" style="min-width:24px;padding:1px 5px" @click="item.sort = Math.max(1, item.sort - 1)">-</el-button>
              <span class="tw-w-6 tw-text-center tw-tabular-nums tw-text-sm">{{ item.sort }}</span>
              <el-button size="small" style="min-width:24px;padding:1px 5px" @click="item.sort++">+</el-button>
            </div>
            <div style="width: 90px">
              <el-select v-model="item.status" size="small">
                <el-option label="启用" value="ACTIVE" />
                <el-option label="停用" value="INACTIVE" />
              </el-select>
            </div>
            <el-button type="danger" link size="small" @click="removeNewRow(idx)">✕</el-button>
          </div>
        </div>
      </template>
    </div>

    <!-- ===== 新增字典弹窗 ===== -->
    <el-dialog v-model="createVisible" title="新增字典" width="640px" :close-on-click-modal="false">
      <el-form ref="createFormRef" :model="createForm" :rules="createRules" label-width="90px">
        <el-form-item label="字典编码" prop="code">
          <el-input
            v-model="createForm.code"
            placeholder="如 product_status（字母、数字、下划线）"
            maxlength="64"
          />
          <div class="tw-text-xs tw-text-gray-400 tw-mt-1">全局唯一，创建后不可修改</div>
        </el-form-item>
        <el-form-item label="字典名称" prop="name">
          <el-input v-model="createForm.name" placeholder="如 商品状态" maxlength="100" />
        </el-form-item>
        <el-form-item label="用途描述">
          <el-input
            v-model="createForm.description"
            type="textarea"
            :rows="2"
            placeholder="描述该字典的使用场景"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="字典项列表">
          <div class="tw-w-full">
            <div
              v-for="(item, idx) in createForm.items"
              :key="idx"
              class="tw-p-2 tw-mb-2 tw-bg-gray-50 tw-rounded"
            >
              <div class="tw-flex tw-items-center tw-gap-2">
                <span class="tw-text-xs tw-text-gray-400 tw-pt-1 tw-w-8">{{ idx + 1 }}.</span>
                <el-form-item
                  :prop="`items.${idx}.value`"
                  :rules="[getItemValueRule(idx)]"
                  class="tw-mb-0"
                >
                  <el-input
                    v-model="item.value"
                    placeholder="编码"
                    size="small"
                    style="width: 100px"
                    maxlength="32"
                  />
                </el-form-item>
                <el-form-item
                  :prop="`items.${idx}.label`"
                  :rules="[getItemLabelRule(idx)]"
                  class="tw-mb-0"
                >
                  <el-input
                    v-model="item.label"
                    placeholder="名称"
                    size="small"
                    style="width: 130px"
                    maxlength="64"
                  />
                </el-form-item>
                <span class="tw-text-xs tw-text-gray-400 tw-pt-1">顺序</span>
                <div class="tw-flex tw-items-center tw-gap-0 tw-shrink-0">
                  <el-button size="small" style="min-width:24px;padding:1px 5px" @click="item.sort = Math.max(1, item.sort - 1)">-</el-button>
                  <span class="tw-w-6 tw-text-center tw-tabular-nums tw-text-sm">{{ item.sort }}</span>
                  <el-button size="small" style="min-width:24px;padding:1px 5px" @click="item.sort++">+</el-button>
                </div>
                <el-select v-model="item.status" size="small" style="width: 80px">
                  <el-option label="启用" value="ACTIVE" />
                  <el-option label="停用" value="INACTIVE" />
                </el-select>
                <el-button
                  v-if="createForm.items.length > 1"
                  type="danger"
                  link
                  size="small"
                  @click="removeCreateItem(idx)"
                  >删除</el-button
                >
              </div>
            </div>
            <el-button type="primary" link size="small" @click="addCreateItem"
              >+ 添加字典项</el-button
            >
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createVisible = false">取消</el-button>
        <el-button type="primary" :loading="creating" @click="submitCreate">确认创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>
