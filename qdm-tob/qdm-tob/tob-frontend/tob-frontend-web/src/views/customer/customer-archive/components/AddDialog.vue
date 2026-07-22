<template>
  <el-dialog
    :model-value="visible"
    title="新增客户档案"
    width="620px"
    top="3vh"
    :close-on-click-modal="false"
    @update:model-value="$emit('update:visible', $event)"
    @close="resetForm"
  >
    <el-scrollbar max-height="68vh">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
      <div class="tw-text-sm tw-font-semibold tw-text-slate-700 tw-mb-3">公司资质</div>
      <el-form-item label="公司名称" prop="companyName">
        <el-input v-model="form.companyName" placeholder="请输入公司名称" maxlength="100" />
      </el-form-item>
      <el-form-item label="营业执照编号" prop="licenseNo">
        <el-input v-model="form.licenseNo" placeholder="18位统一社会信用代码" maxlength="50" />
      </el-form-item>
      <el-form-item label="门头照片" prop="doorPhoto" required>
        <ImageUpload ref="doorRef" v-model="form.doorPhoto" />
        <span class="tw-ml-1 tw-text-xs tw-text-gray-400">JPG/PNG，≤5MB</span>
      </el-form-item>
      <el-form-item label="营业执照照片" prop="licensePhoto" required>
        <ImageUpload ref="licenseRef" v-model="form.licensePhoto" />
        <span class="tw-ml-1 tw-text-xs tw-text-gray-400">JPG/PNG，≤5MB</span>
      </el-form-item>

      <div class="tw-text-sm tw-font-semibold tw-text-slate-700 tw-mb-3 tw-mt-5">收货信息</div>
      <el-form-item label="收货人姓名" prop="contactName">
        <el-input v-model="form.contactName" placeholder="请输入收货人姓名" maxlength="50" />
      </el-form-item>
      <el-form-item label="联系电话" prop="contactPhone">
        <el-input v-model="form.contactPhone" placeholder="请输入11位手机号" maxlength="20" />
      </el-form-item>
      <el-form-item label="所在地区" prop="region">
        <el-cascader
          v-model="form.region"
          :options="chinaAreaOptions"
          :props="{ value: 'label', emitPath: true }"
          placeholder="请选择省/市/区"
          filterable
          style="width: 100%"
        />
      </el-form-item>
      <el-form-item label="详细地址" prop="address">
        <el-input v-model="form.address" placeholder="请输入详细地址" maxlength="500" />
      </el-form-item>

      <div class="tw-text-sm tw-font-semibold tw-text-slate-700 tw-mb-3 tw-mt-5">收货配置</div>
      <el-form-item label="销售大区">
        <el-select v-model="form.salesRegionName" placeholder="请选择销售大区" clearable style="width: 100%">
          <el-option v-for="r in salesRegionOptions" :key="r" :label="r" :value="r" />
        </el-select>
      </el-form-item>
      <el-form-item label="可收货时段">
        <el-time-picker
          v-model="form.receiveTime"
          is-range format="HH:mm" value-format="HH:mm"
          range-separator="-" start-placeholder="开始" end-placeholder="结束"
          style="width: 100%"
        />
      </el-form-item>
      <el-form-item label="收货要求">
        <el-input
          v-model="form.receiveRequirement" type="textarea" :rows="3"
          maxlength="500" show-word-limit placeholder="选填，请输入收货要求"
        />
      </el-form-item>
    </el-form>
    </el-scrollbar>
    <template #footer>
      <el-button @click="close">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="submit">确认新增</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { createCustomerArchive, type CustomerArchiveCreateVO } from '@/api/modules/customer-archive'
import { getAllSalesRegions } from '@/api/modules/operation'
import { chinaAreaOptions } from '@/utils/chinaArea'
import ImageUpload from '@/components/ImageUpload.vue'

defineProps<{ visible: boolean }>()
const emit = defineEmits<{ (e: 'update:visible', v: boolean): void; (e: 'success'): void }>()

const formRef = ref<FormInstance | null>(null)
const doorRef = ref<InstanceType<typeof ImageUpload> | null>(null)
const licenseRef = ref<InstanceType<typeof ImageUpload> | null>(null)
const submitting = ref(false)
const salesRegionOptions = ref<string[]>([])

onMounted(async () => {
  try {
    const res = await getAllSalesRegions()
    if ((res.code === 0 || res.code === 200) && res.data.records) {
      salesRegionOptions.value = res.data.records.map((r) => r.name)
    }
  } catch { /* 兜底为空 */ }
})

const defaultForm = () => ({
  companyName: '', licenseNo: '', doorPhoto: '', licensePhoto: '',
  contactName: '', contactPhone: '',
  province: '', city: '', district: '', address: '',
  receiveTimeStart: '00:00', receiveTimeEnd: '08:00',
  salesRegionName: '' as string,
})

const form = reactive({
  ...defaultForm(),
  region: [] as string[],
  receiveTime: [] as string[],
  receiveRequirement: '' as string,
})

const rules: FormRules = {
  companyName: [{ required: true, message: '请输入公司名称', trigger: 'blur' }],
  licenseNo: [{ required: true, message: '请输入营业执照编号', trigger: 'blur' }],
  doorPhoto: [{ required: true, message: '请选择门头照片', trigger: 'change' }],
  licensePhoto: [{ required: true, message: '请选择营业执照照片', trigger: 'change' }],
  contactName: [{ required: true, message: '请输入收货人姓名', trigger: 'blur' }],
  contactPhone: [{ required: true, message: '请输入联系电话', trigger: 'blur' }],
  address: [{ required: true, message: '请输入详细地址', trigger: 'blur' }],
}

function resetForm() {
  Object.assign(form, { ...defaultForm(), region: [], receiveTime: [], receiveRequirement: '' })
}

function close() { emit('update:visible', false) }

async function submit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  if (!form.region || form.region.length < 3) {
    ElMessage.error('请选择完整的省市区')
    return
  }
  submitting.value = true
  try {
    // 提交时才把本地暂存的图片上传到 OSS（上传失败则中止创建，弹窗保持打开）
    await Promise.all([doorRef.value?.submit(), licenseRef.value?.submit()])
    const data: CustomerArchiveCreateVO = {
      companyName: form.companyName, licenseNo: form.licenseNo,
      doorPhoto: form.doorPhoto, licensePhoto: form.licensePhoto,
      contactName: form.contactName, contactPhone: form.contactPhone,
      province: form.region[0], city: form.region[1], district: form.region[2],
      address: form.address,
      receiveTimeStart: form.receiveTime?.[0] || '00:00',
      receiveTimeEnd: form.receiveTime?.[1] || '08:00',
      receiveRequirement: form.receiveRequirement || undefined,
      salesRegionName: form.salesRegionName || undefined,
    }
    await createCustomerArchive(data)
    ElMessage.success('客户档案已创建，状态：已通过')
    close()
    emit('success')
  } catch {
    // 上传失败已由 ImageUpload 提示；创建失败由响应拦截器提示。此处仅中止流程，保留弹窗
  } finally {
    submitting.value = false
  }
}
</script>
