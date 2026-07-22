<template>
  <el-dialog
    :model-value="visible"
    title="编辑客户档案"
    width="520px"
    :close-on-click-modal="false"
    @update:model-value="$emit('update:visible', $event)"
  >
    <el-form ref="formRef" :model="form" label-width="120px">
      <el-form-item label="公司名称">
        <el-input :model-value="companyName" disabled />
      </el-form-item>

      <div class="tw-text-sm tw-font-semibold tw-text-slate-700 tw-mb-3 tw-mt-2">收货人信息</div>
      <el-form-item label="收货人姓名">
        <el-input v-model="form.contactName" placeholder="请输入收货人姓名" maxlength="50" />
      </el-form-item>
      <el-form-item label="联系电话">
        <el-input v-model="form.contactPhone" placeholder="请输入联系电话" maxlength="20" />
      </el-form-item>

      <div class="tw-text-sm tw-font-semibold tw-text-slate-700 tw-mb-3 tw-mt-5">业务属性</div>
      <el-form-item label="价格组">
        <el-select v-model="form.priceGroup" placeholder="请选择价格组" clearable style="width: 100%">
          <el-option label="标准价" value="标准价" />
          <el-option label="批发价" value="批发价" />
          <el-option label="协议价A" value="协议价A" />
          <el-option label="协议价B" value="协议价B" />
        </el-select>
      </el-form-item>
      <el-form-item label="结算公司">
        <el-select v-model="form.settleCompany" placeholder="请选择结算公司" clearable style="width: 100%">
          <el-option label="钱鲜达供应链有限公司" value="钱鲜达供应链有限公司" />
          <el-option label="鲜美来食品有限公司" value="鲜美来食品有限公司" />
          <el-option label="鲜达物流有限公司" value="鲜达物流有限公司" />
        </el-select>
      </el-form-item>
      <el-form-item label="经营类型">
        <el-select v-model="form.businessType" placeholder="请选择经营类型" clearable style="width: 100%">
          <el-option label="面馆" value="面馆" />
          <el-option label="校食堂" value="校食堂" />
          <el-option label="酒店" value="酒店" />
          <el-option label="快餐店" value="快餐店" />
          <el-option label="火锅店" value="火锅店" />
          <el-option label="食堂承包" value="食堂承包" />
        </el-select>
      </el-form-item>
      <el-form-item label="结算类型">
        <el-select v-model="form.settleType" placeholder="请选择结算类型" clearable style="width: 100%">
          <el-option label="现结" value="CASH" />
          <el-option label="账期" value="PERIOD" />
        </el-select>
      </el-form-item>
      <el-form-item label="收货要求">
        <el-input v-model="form.internalRemark" type="textarea" :rows="3" maxlength="500" show-word-limit placeholder="审核人补充的收货要求" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="close">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="submit">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, watch, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { editCustomerArchive, type CustomerArchiveEditVO, type CustomerArchiveSummaryVO } from '@/api/modules/customer-archive'

const props = defineProps<{
  visible: boolean
  archiveId: number | null
  /** 直接用列表行数据回填，避免逐个传 prop 遗漏字段 */
  row: CustomerArchiveSummaryVO | null
}>()

const emit = defineEmits<{ (e: 'update:visible', v: boolean): void; (e: 'success'): void }>()

const formRef = ref()
const submitting = ref(false)

function emptyForm() {
  return {
    contactName: '', contactPhone: '',
    priceGroup: '', settleCompany: '', businessType: '', settleType: undefined as string | undefined,
    internalRemark: '',
  }
}
const form = reactive(emptyForm())

watch(() => props.visible, async (v) => {
  if (v && props.row) {
    await nextTick()
    form.contactName = props.row.contactName || ''
    form.contactPhone = props.row.contactPhone || ''
    form.priceGroup = props.row.priceGroup || ''
    form.settleCompany = props.row.settleCompany || ''
    form.businessType = props.row.businessType || ''
    form.settleType = (props.row.settleType as string | undefined) || undefined
    form.internalRemark = ''
  }
})

function close() { emit('update:visible', false) }

async function submit() {
  if (!props.archiveId) return
  submitting.value = true
  try {
    const data: CustomerArchiveEditVO = {
      contactName: form.contactName || undefined,
      contactPhone: form.contactPhone || undefined,
      priceGroup: form.priceGroup || undefined,
      settleCompany: form.settleCompany || undefined,
      businessType: form.businessType || undefined,
      settleType: form.settleType || undefined,
      internalRemark: form.internalRemark || undefined,
    }
    await editCustomerArchive(props.archiveId, data)
    ElMessage.success('信息更新成功（不触发重新审核）')
    close()
    emit('success')
  } finally { submitting.value = false }
}
</script>
