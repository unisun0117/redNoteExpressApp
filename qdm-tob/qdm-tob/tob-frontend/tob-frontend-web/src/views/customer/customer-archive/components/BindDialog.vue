<template>
  <el-dialog
    :model-value="visible"
    title="绑定小程序账号"
    width="550px"
    :close-on-click-modal="false"
    @update:model-value="$emit('update:visible', $event)"
    @close="reset"
  >
    <div class="tw-mb-3 tw-text-sm tw-text-slate-700">
      公司：<strong>{{ companyName }}</strong>
    </div>
    <div class="tw-mb-4">
      <div class="tw-text-sm tw-text-slate-700 tw-mb-2">已绑定：</div>
      <el-tag
        v-for="(user, idx) in boundUsers"
        :key="idx"
        size="small"
        closable
        style="margin: 2px 4px"
        @close="confirmUnbind(user, idx)"
      >{{ user.userName || user.userMobile || '用户' }}</el-tag>
      <span v-if="!boundUsers.length" class="tw-text-gray-400 tw-text-sm">暂无</span>
    </div>
    <el-divider class="tw-my-3" />
    <div class="tw-flex tw-gap-2 tw-mb-4">
      <el-input v-model="searchKeyword" placeholder="搜索小程序用户（手机号）" clearable style="flex: 1" @keyup.enter="doSearch" />
      <el-button type="primary" @click="doSearch">搜索</el-button>
    </div>
    <el-table v-if="searchResults.length > 0" :data="searchResults" size="small" style="width: 100%" max-height="200">
      <el-table-column prop="userName" label="姓名" width="120">
        <template #default="{ row }">
          <div class="tw-flex tw-items-center tw-gap-2">
            <el-avatar :size="24">{{ (row.userName || row.userMobile || 'U')?.charAt(0) }}</el-avatar>
            <span>{{ row.userName || '-' }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="userMobile" label="手机号" width="150" />
      <el-table-column label="操作" width="80">
        <template #default="{ row }">
          <el-button type="primary" link size="small" @click="doBind(row)">绑定</el-button>
        </template>
      </el-table-column>
    </el-table>
    <div v-if="searched && searchResults.length === 0" class="tw-text-center tw-py-8 tw-text-gray-400 tw-text-sm">
      未找到匹配的小程序用户
    </div>
    <template #footer>
      <el-button @click="close">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getBoundUsers, bindUser, unbindUser, type BoundUser } from '@/api/modules/customer-archive'
import { getSysUserPage } from '@/api/modules/customer'

const props = defineProps<{ visible: boolean; archiveId: number | null; companyName: string }>()
const emit = defineEmits<{ (e: 'update:visible', v: boolean): void; (e: 'success'): void }>()

const boundUsers = ref<BoundUser[]>([])
const searchKeyword = ref('')
const searchResults = ref<BoundUser[]>([])
const searched = ref(false)

watch(() => props.visible, async (v) => {
  if (v && props.archiveId) {
    try {
      const res = await getBoundUsers(props.archiveId)
      if (res.code === 0 || res.code === 200) boundUsers.value = res.data || []
    } catch { boundUsers.value = [] }
    searchKeyword.value = ''
    searchResults.value = []
    searched.value = false
  }
})

function close() {
  emit('update:visible', false)
}

function reset() {
  searchKeyword.value = ''
  searchResults.value = []
  searched.value = false
}

async function doSearch() {
  searched.value = true
  try {
    const res = await getSysUserPage({ pageNum: 1, pageSize: 50, keyword: searchKeyword.value })
    if (res.code === 0 || res.code === 200) {
      const ids = boundUsers.value.map(u => u.userId)
      const users: BoundUser[] = res.data.records.map((u) => ({
        userId: u.id,
        userName: u.realName,
        userMobile: u.mobile,
      }))
      searchResults.value = users.filter((u) => !ids.includes(u.userId))
    }
  } catch { searchResults.value = [] }
}

async function doBind(user: BoundUser) {
  if (!props.archiveId) return
  try {
    await bindUser(props.archiveId, user.userId, user.userName, user.userMobile)
    ElMessage.success(`已将 ${user.userName || '用户'} 绑定到该公司档案`)
    boundUsers.value.push({ ...user, memberRole: 'MEMBER', bindingStatus: 'BOUND' })
    searchResults.value = searchResults.value.filter(u => u.userId !== user.userId)
    emit('success')
  } catch { /* 拦截器统一提示 */ }
}

function confirmUnbind(user: BoundUser, idx: number) {
  if (!props.archiveId) return
  ElMessageBox.confirm(`确定要解绑 ${user.userName || '用户'} 吗？`, '解绑确认', {
    confirmButtonText: '确定解绑', cancelButtonText: '取消', type: 'warning',
  }).then(async () => {
    try {
      await unbindUser(props.archiveId!, user.userId)
      ElMessage.success('已解绑')
      boundUsers.value.splice(idx, 1)
      emit('success')
    } catch { /* 拦截器统一提示 */ }
  }).catch(() => {})
}
</script>
