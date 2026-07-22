<template>
  <el-dialog
    :model-value="visible"
    title="手动分配审核人"
    width="480px"
    :close-on-click-modal="false"
    @update:model-value="$emit('update:visible', $event)"
  >
    <el-form label-width="120px">
      <el-form-item label="公司名称">
        <span>{{ companyName }}</span>
      </el-form-item>
      <el-form-item label="当前审核人">
        <span>{{ currentAuditor || '未分配' }}</span>
      </el-form-item>
      <el-form-item label="分配审核人">
        <el-select v-model="auditorId" placeholder="请选择审核人" style="width: 100%">
          <el-option
            v-for="s in salesmanList"
            :key="s.id"
            :label="`${s.name}（${s.phone}）`"
            :value="s.id"
          />
        </el-select>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="close">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="submit">确认分配</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { assignAuditor, getSalesmanReferralList, type SalesmanReferralVO } from '@/api/modules/customer-archive'

const props = defineProps<{ visible: boolean; archiveId: number | null; companyName: string; currentAuditor: string }>()
const emit = defineEmits<{ (e: 'update:visible', v: boolean): void; (e: 'success'): void }>()

const auditorId = ref<number | null>(null)
const submitting = ref(false)
const salesmanList = ref<SalesmanReferralVO[]>([])

watch(() => props.visible, async (v) => {
  if (v) {
    auditorId.value = null
    try {
      const res = await getSalesmanReferralList()
      if (res.code === 0 || res.code === 200) {
        salesmanList.value = res.data.records
      }
    } catch { salesmanList.value = [] }
  }
})

function close() {
  emit('update:visible', false)
}

async function submit() {
  if (!props.archiveId || !auditorId.value) {
    ElMessage.warning('请选择审核人')
    return
  }
  const selected = salesmanList.value.find((s) => s.id === auditorId.value)
  submitting.value = true
  try {
    await assignAuditor(props.archiveId, {
      auditorId: auditorId.value,
      auditorName: selected?.name || '',
      auditorType: 'SALESMAN',
    })
    ElMessage.success('审核人分配成功')
    close()
    emit('success')
  } finally {
    submitting.value = false
  }
}
</script>
