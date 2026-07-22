<template>
  <div class="tw-space-y-3">
    <!-- 工具栏 -->
    <div class="tw-bg-white tw-rounded-[--border-radius-card] tw-px-5 tw-py-3 tw-shadow-[--shadow-card] tw-flex tw-items-center tw-gap-3">
      <el-button @click="goBack" text>← 返回</el-button>
      <span class="tw-text-base tw-font-semibold">客户档案详情</span>
      <div class="tw-ml-auto tw-flex tw-gap-2">
        <el-button v-if="detail?.auditStatus === 'APPROVED'" type="primary" size="small" @click="openEdit">
          编辑业务属性
        </el-button>
        <el-button v-if="detail?.auditStatus === 'APPROVED'" type="primary" size="small" @click="openBind">
          绑定账号
        </el-button>
        <el-button v-if="detail?.auditStatus === 'PENDING'" type="warning" size="small" @click="openAssign">
          分配审核人
        </el-button>
      </div>
    </div>

    <div v-if="!loading && detail" class="tw-space-y-4" style="max-width: 1000px;">
      <!-- 信息标题卡片 -->
      <div class="tw-bg-gradient-to-r tw-from-blue-50 tw-to-indigo-50 tw-rounded-xl tw-px-6 tw-py-5 tw-shadow-[--shadow-card]">
        <div class="tw-flex tw-items-center tw-gap-3 tw-mb-2">
          <h2 class="tw-text-xl tw-font-bold tw-text-slate-800">{{ detail.companyName }}</h2>
          <el-tag :type="statusType(detail.auditStatus)" effect="plain">
            {{ statusText(detail.auditStatus) }}
          </el-tag>
        </div>
        <div class="tw-flex tw-flex-wrap tw-items-center tw-gap-x-4 tw-gap-y-1 tw-text-sm tw-text-slate-600">
          <span>SAP编码: {{ detail.sapCustomerCode || '待获取' }}</span>
          <span>提交时间: {{ detail.createdAt }}</span>
          <span v-if="detail.salesRegionName">{{ detail.salesRegionName }}</span>
        </div>
      </div>

      <!-- 标签页 -->
      <el-tabs v-model="activeTab">
        <!-- ===== 基本信息 ===== -->
        <el-tab-pane label="基本信息" name="basic">
          <div class="tw-space-y-4">
            <div class="section-card">
              <div class="section-card-header"><span class="section-card-dot tw-bg-blue-500"></span>档案信息</div>
              <div class="detail-row"><span class="detail-label">公司名称</span><span class="detail-value tw-font-medium">{{ detail.companyName }}</span></div>
              <div class="detail-row"><span class="detail-label">SAP客户编码</span><span class="detail-value">{{ detail.sapCustomerCode || '待获取' }}</span></div>
              <div class="detail-row"><span class="detail-label">营业执照编号</span><span class="detail-value">{{ detail.licenseNo || '-' }}</span></div>
              <div class="detail-row"><span class="detail-label">审核状态</span><span class="detail-value"><el-tag :type="statusType(detail.auditStatus)" size="small" effect="plain">{{ statusText(detail.auditStatus) }}</el-tag></span></div>
              <div class="detail-row"><span class="detail-label">注册时间</span><span class="detail-value">{{ detail.createdAt }}</span></div>
              <div class="detail-row"><span class="detail-label">最近更新</span><span class="detail-value">{{ detail.updatedAt }}</span></div>
              <div class="detail-row"><span class="detail-label">门头照片</span><span class="detail-value"><span v-if="detail.doorPhoto" class="tw-text-[--color-primary] tw-cursor-pointer hover:tw-underline" @click="preview(detail.doorPhoto)">查看照片</span><span v-else class="tw-text-gray-400">未上传</span></span></div>
              <div class="detail-row"><span class="detail-label">营业执照照片</span><span class="detail-value"><span v-if="detail.licensePhoto" class="tw-text-[--color-primary] tw-cursor-pointer hover:tw-underline" @click="preview(detail.licensePhoto)">查看照片</span><span v-else class="tw-text-gray-400">未上传</span></span></div>
            </div>

            <div class="section-card">
              <div class="section-card-header"><span class="section-card-dot tw-bg-emerald-500"></span>收货人信息</div>
              <div class="detail-row"><span class="detail-label">收货人</span><span class="detail-value">{{ detail.contactName }}</span></div>
              <div class="detail-row"><span class="detail-label">联系电话</span><span class="detail-value">{{ detail.contactPhone }}</span></div>
            </div>

            <div class="section-card">
              <div class="section-card-header"><span class="section-card-dot tw-bg-orange-500"></span>收货地址</div>
              <div class="detail-row"><span class="detail-label">省份</span><span class="detail-value">{{ detail.province }}</span></div>
              <div class="detail-row"><span class="detail-label">城市</span><span class="detail-value">{{ detail.city }}</span></div>
              <div class="detail-row"><span class="detail-label">区县</span><span class="detail-value">{{ detail.district }}</span></div>
              <div class="detail-row"><span class="detail-label">坐标</span><span class="detail-value">{{ displayCoord }}</span></div>
              <div class="detail-row"><span class="detail-label">详细地址</span><span class="detail-value">{{ detail.address }}</span></div>
            </div>

            <div class="section-card">
              <div class="section-card-header">
                <span class="section-card-dot tw-bg-purple-500"></span>收货配置
                <el-tag size="small" type="info" effect="plain" class="tw-ml-2">{{ detail.receiveTimeStart || '00:00' }} - {{ detail.receiveTimeEnd || '08:00' }}</el-tag>
              </div>
              <div class="detail-row"><span class="detail-label">存放位置照片</span><span class="detail-value"><span v-if="detail.storagePhotos" class="tw-text-[--color-primary] tw-cursor-pointer hover:tw-underline" @click="preview(detail.storagePhotos)">查看照片</span><span v-else class="tw-text-gray-400">未上传</span></span></div>
              <div class="detail-row"><span class="detail-label">收货要求</span><span class="detail-value">{{ detail.receiveRequirement || '无' }}</span></div>
            </div>
          </div>
        </el-tab-pane>

        <!-- ===== 业务属性 ===== -->
        <el-tab-pane label="业务属性" name="business">
          <div class="section-card">
            <div class="section-card-header"><span class="section-card-dot tw-bg-blue-500"></span>业务属性配置</div>
            <div class="detail-row"><span class="detail-label">销售大区</span><span class="detail-value tw-text-[--color-primary] tw-font-medium">{{ detail.salesRegionName || '-' }}</span></div>
            <div class="detail-row"><span class="detail-label">归属业务员</span><span class="detail-value">{{ detail.salesmanName || '无' }}</span></div>
            <div class="detail-row"><span class="detail-label">价格组</span><span class="detail-value"><el-tag v-if="detail.priceGroup" size="small" type="success" effect="plain">{{ detail.priceGroup }}</el-tag><span v-else class="tw-text-gray-400">-</span></span></div>
            <div class="detail-row"><span class="detail-label">结算公司</span><span class="detail-value">{{ detail.settleCompany || '-' }}</span></div>
            <div class="detail-row"><span class="detail-label">经营类型</span><span class="detail-value">{{ detail.businessType || '-' }}</span></div>
            <div class="detail-row"><span class="detail-label">结算类型</span><span class="detail-value"><template v-if="detail.settleType">{{ detail.settleType === 'CASH' ? '现结' : '账期' }}</template><span v-else class="tw-text-gray-400">-</span></span></div>
            <div class="detail-row"><span class="detail-label">审核处理人</span><span class="detail-value">{{ detail.auditorName || '-' }}</span></div>
            <div class="detail-row"><span class="detail-label">审核人类型</span><span class="detail-value">{{ auditorTypeText }}</span></div>
            <div class="detail-row"><span class="detail-label">驳回意见</span><span class="detail-value"><span :class="detail.auditRejectReason ? 'tw-text-red-500' : ''">{{ detail.auditRejectReason || '-' }}</span></span></div>
            <div class="detail-row"><span class="detail-label">审核时间</span><span class="detail-value">{{ detail.auditTime || '-' }}</span></div>
            <div class="detail-row"><span class="detail-label">内部备注</span><span class="detail-value">{{ detail.internalRemark || '无' }}</span></div>
          </div>
        </el-tab-pane>

        <!-- ===== 审核记录 ===== -->
        <el-tab-pane label="审核记录" name="audit">
          <div class="tw-bg-white tw-rounded-xl tw-px-5 tw-py-4 tw-shadow-[--shadow-card]">
            <div class="tw-flex tw-items-center tw-gap-2 tw-mb-4 tw-pb-3 tw-border-b tw-border-gray-100">
              <span class="tw-w-1 tw-h-4 tw-bg-amber-500 tw-rounded-full" />
              <span class="tw-text-sm tw-font-semibold tw-text-slate-700">审核流程</span>
              <span v-if="detail.auditorName" class="tw-ml-auto tw-text-sm tw-text-gray-500">
                当前审核人: <strong class="tw-text-slate-700">{{ detail.auditorName }}</strong>
              </span>
            </div>
            <div v-if="detail.auditLogs?.length" class="tw-space-y-3">
              <div v-for="(log, idx) in detail.auditLogs" :key="log.id" class="tw-relative">
                <div v-if="idx < detail.auditLogs.length - 1" class="tw-absolute tw-left-4 tw-top-7 tw-bottom-0 tw-w-0.5 tw-bg-gray-100" />
                <div class="tw-flex tw-gap-4">
                  <div class="tw-flex-shrink-0 tw-flex tw-items-center tw-justify-center tw-w-9 tw-h-9">
                    <div class="tw-w-3 tw-h-3 tw-rounded-full tw-bg-blue-500 tw-ring-4 tw-ring-blue-50" />
                  </div>
                  <div class="tw-flex-1 tw-pb-5">
                    <div class="tw-flex tw-items-center tw-gap-2 tw-mb-1">
                      <span class="tw-font-medium tw-text-slate-800">{{ log.action }}</span>
                      <span class="tw-text-xs tw-text-gray-400">{{ log.createdAt }}</span>
                    </div>
                    <div class="tw-text-sm tw-text-gray-500">
                      <template v-if="log.operatorName">{{ log.operatorName }}</template>
                      <template v-if="log.remark"><span class="tw-text-red-500 tw-ml-2">{{ log.remark }}</span></template>
                    </div>
                  </div>
                </div>
              </div>
            </div>
            <el-empty v-else description="暂无审核记录" :image-size="80" />
          </div>
        </el-tab-pane>

        <!-- ===== 绑定用户 ===== -->
        <el-tab-pane label="绑定用户" name="users">
          <div class="tw-bg-white tw-rounded-xl tw-px-5 tw-py-4 tw-shadow-[--shadow-card]">
            <div class="tw-flex tw-items-center tw-gap-2 tw-mb-4 tw-pb-3 tw-border-b tw-border-gray-100">
              <span class="tw-w-1 tw-h-4 tw-bg-green-500 tw-rounded-full" />
              <span class="tw-text-sm tw-font-semibold tw-text-slate-700">已绑定小程序账号</span>
              <el-tag size="small" effect="plain" class="tw-ml-auto">{{ detail.boundUsers?.length || 0 }} 人</el-tag>
            </div>
            <template v-if="detail.boundUsers?.length">
              <el-table :data="detail.boundUsers" size="small" style="width: 100%">
                <el-table-column label="姓名" width="120">
                  <template #default="{ row }">
                    <div class="tw-flex tw-items-center tw-gap-2">
                      <el-avatar :size="28">{{ (row.userName || row.userMobile || 'U')?.charAt(0) }}</el-avatar>
                      <span>{{ row.userName || '-' }}</span>
                    </div>
                  </template>
                </el-table-column>
                <el-table-column prop="userMobile" label="手机号" width="150" />
                <el-table-column label="角色" width="100">
                  <template #default="{ row }">
                    <el-tag size="small" :type="row.memberRole === 'ADMIN' ? 'primary' : 'info'" effect="plain">
                      {{ row.memberRole === 'ADMIN' ? '管理员' : '成员' }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="createdAt" label="绑定时间" />
              </el-table>
            </template>
            <el-empty v-else description="暂无绑定用户" :image-size="80">
              <el-button v-if="detail.auditStatus === 'APPROVED'" type="primary" @click="openBind">绑定账号</el-button>
            </el-empty>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <div v-if="loading" class="tw-text-center tw-py-12">
      <el-icon class="is-loading tw-text-2xl tw-text-[--color-primary]"><Loading /></el-icon>
      <p class="tw-text-gray-400 tw-text-sm tw-mt-2">加载中...</p>
    </div>

    <!-- 弹窗 -->
    <EditDialog
      v-model:visible="editVisible"
      :archive-id="archiveId"
      :row="editDialogRow"
      @success="loadDetail"
    />
    <AssignDialog
      v-model:visible="assignVisible"
      :archive-id="archiveId"
      :company-name="detail?.companyName ?? ''"
      :current-auditor="detail?.auditorName ?? ''"
      @success="loadDetail"
    />
    <BindDialog
      v-model:visible="bindVisible"
      :archive-id="archiveId"
      :company-name="detail?.companyName ?? ''"
      @success="loadDetail"
    />

    <el-image-viewer
      v-if="imgVisible"
      :url-list="[imgUrl]"
      :initial-index="0"
      hide-on-click-modal
      @close="imgVisible = false"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import type { CustomerArchiveSummaryVO } from '@/api/modules/customer-archive'
import { useRouter, useRoute } from 'vue-router'
import { getCustomerArchiveDetail, type CustomerArchiveViewVO } from '@/api/modules/customer-archive'
import EditDialog from './components/EditDialog.vue'
import AssignDialog from './components/AssignDialog.vue'
import BindDialog from './components/BindDialog.vue'

const router = useRouter()
const route = useRoute()
const archiveId = Number(route.params.id)

const loading = ref(false)
const activeTab = ref('basic')
const detail = ref<CustomerArchiveViewVO | null>(null)

async function loadDetail() {
  loading.value = true
  try {
    const res = await getCustomerArchiveDetail(archiveId)
    if (res.code === 0 || res.code === 200) detail.value = res.data
  } finally { loading.value = false }
}

onMounted(loadDetail)
function goBack() { router.push({ name: 'CustomerArchive' }) }

function statusType(s: string): '' | 'success' | 'warning' | 'danger' | 'info' {
  return { APPROVED: 'success', PENDING: 'warning', REJECTED: 'danger' }[s] as never || 'info'
}
function statusText(s: string): string {
  return { APPROVED: '已通过', PENDING: '待审核', REJECTED: '已驳回' }[s] || s
}

const displayCoord = computed(() => {
  if (detail.value?.longitude != null && detail.value?.latitude != null) {
    return `${detail.value.longitude}, ${detail.value.latitude}`
  }
  return '-'
})

const auditorTypeText = computed(() => {
  const t = detail.value?.auditorType
  return t === 'SALESMAN' ? '业务员' : t === 'MANAGER' ? '区域经理' : '-'
})

// 图片预览
const imgVisible = ref(false)
const imgUrl = ref('')
function preview(url: string) { imgUrl.value = url; imgVisible.value = true }

// 构建编辑弹窗需要的行数据
const editDialogRow = computed<CustomerArchiveSummaryVO | null>(() => {
  if (!detail.value) return null
  return {
    id: detail.value.id,
    companyName: detail.value.companyName,
    contactName: detail.value.contactName,
    contactPhone: detail.value.contactPhone,
    priceGroup: detail.value.priceGroup,
    settleCompany: detail.value.settleCompany,
    businessType: detail.value.businessType,
    settleType: detail.value.settleType,
  } as CustomerArchiveSummaryVO
})

// 弹窗
const editVisible = ref(false)
const assignVisible = ref(false)
const bindVisible = ref(false)

function openEdit() { editVisible.value = true }
function openAssign() { assignVisible.value = true }
function openBind() { bindVisible.value = true }
</script>

<style scoped>
.section-card {
  background: #fff;
  border-radius: 14px;
  padding: 16px 20px;
  box-shadow: 0 4px 24px rgba(0,0,0,0.05);
}
.section-card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
  padding-bottom: 10px;
  border-bottom: 1px solid #f3f4f6;
  font-size: 13px;
  font-weight: 600;
  color: #334155;
}
.section-card-dot {
  width: 4px;
  height: 16px;
  border-radius: 2px;
  flex-shrink: 0;
}
.detail-row {
  display: flex;
  padding: 5px 0;
  font-size: 13px;
}
.detail-label {
  width: 100px;
  color: #909399;
  flex-shrink: 0;
}
.detail-value {
  color: #303133;
  flex: 1;
}
</style>
