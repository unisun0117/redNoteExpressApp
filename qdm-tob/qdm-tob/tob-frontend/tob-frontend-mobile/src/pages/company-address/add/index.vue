<template>
  <view class="page">
    <!-- 顶部导航栏 -->
    <view class="nav-bar fixed top-0 left-0 right-0 bg-white z-50 flex items-center"
      :style="{ paddingTop: statusBarH + 'px', height: statusBarH + 44 + 'px' }">
      <view class="nav-back" @click="goBack">
        <text class="text-2xl">‹</text>
      </view>
      <view class="nav-title">{{ isEdit ? '编辑收货地址' : '新增收货地址' }}</view>
    </view>

    <!-- 页面内容区 -->
    <scroll-view scroll-y class="content" :style="{ top: (statusBarH + 44) + 'px' }">
      <!-- 驳回提示（仅编辑模式） -->
      <view v-if="isEdit && auditRejectReason" class="reject-notice">
        <view class="reject-notice-title">驳回原因</view>
        <view class="reject-notice-text">{{ auditRejectReason }}</view>
      </view>

      <!-- 区块一：公司资质 -->
      <view class="section-title">公司资质</view>

      <view class="form-item">
        <view class="form-label"><text class="required">*</text>公司名称</view>
        <input class="form-input" v-model="form.companyName" placeholder="请输入公司名称"
          maxlength="100" />
      </view>

      <view class="form-item">
        <view class="form-label">门头照片</view>
        <view class="upload-area">
          <view v-if="form.doorPhoto" class="upload-preview"
            @click="previewPhoto(form.doorPhoto)">
            <image :src="form.doorPhoto" mode="aspectFill" class="preview-img" />
            <view class="upload-delete" @click.stop="deletePhoto('door')">
              <text class="text-white">×</text>
            </view>
          </view>
          <view v-else class="upload-box" @click="choosePhoto('door')">
            <text class="upload-icon">+</text>
            <text class="upload-text">上传</text>
          </view>
        </view>
      </view>

      <view class="form-item">
        <view class="form-label"><text class="required">*</text>营业执照编号</view>
        <input class="form-input" v-model="form.licenseNo"
          placeholder="请输入18位统一社会信用代码" maxlength="18" />
      </view>

      <view class="form-item">
        <view class="form-label">营业执照照片</view>
        <view class="upload-area">
          <view v-if="form.licensePhoto" class="upload-preview"
            @click="previewPhoto(form.licensePhoto)">
            <image :src="form.licensePhoto" mode="aspectFill" class="preview-img" />
            <view class="upload-delete" @click.stop="deletePhoto('license')">
              <text class="text-white">×</text>
            </view>
          </view>
          <view v-else class="upload-box" @click="choosePhoto('license')">
            <text class="upload-icon">+</text>
            <text class="upload-text">上传</text>
          </view>
        </view>
      </view>

      <!-- 区块二：收货信息 -->
      <view class="section-title">收货信息</view>

      <view class="form-item">
        <view class="form-label"><text class="required">*</text>收货人姓名</view>
        <input class="form-input" v-model="form.contactName" placeholder="请输入收货人姓名"
          maxlength="50" />
      </view>

      <view class="form-item">
        <view class="form-label"><text class="required">*</text>收货人联系方式</view>
        <input class="form-input" v-model="form.contactPhone" type="number"
          placeholder="请输入手机号" maxlength="11" />
      </view>

      <view class="form-item">
        <view class="form-label"><text class="required">*</text>所在地区</view>
        <picker mode="region" :value="[form.province, form.city, form.district]"
          @change="onRegionChange">
          <view class="form-input form-picker">
            {{ regionText || '请选择省市区' }}
          </view>
        </picker>
      </view>

      <view class="form-item">
        <view class="form-label"><text class="required">*</text>详细收货地址</view>
        <view class="input-wrapper">
          <input class="form-input" v-model="form.address"
            placeholder="请输入详细地址" />
          <view class="locate-btn" @click="onLocate">
            <text class="locate-icon">📍</text>
          </view>
        </view>
      </view>

      <!-- 区块三：收货配置 -->
      <view class="section-title">收货配置</view>

      <view class="form-item">
        <view class="form-label"><text class="required">*</text>可收货时段</view>
        <view class="time-range">
          <picker mode="time" :value="form.receiveTimeStart"
            @change="onTimeChange('start', $event)">
            <view class="form-input form-picker flex-1">
              {{ form.receiveTimeStart }}
            </view>
          </picker>
          <view class="time-sep">至</view>
          <picker mode="time" :value="form.receiveTimeEnd"
            @change="onTimeChange('end', $event)">
            <view class="form-input form-picker flex-1">
              {{ form.receiveTimeEnd }}
            </view>
          </picker>
        </view>
      </view>

      <view class="form-item">
        <view class="form-label">收货存放位置照片（最多3张）</view>
        <view class="upload-area">
          <view v-for="(url, idx) in form.storagePhotos" :key="idx" class="upload-preview"
            @click="previewPhoto(url)">
            <image :src="url" mode="aspectFill" class="preview-img" />
            <view class="upload-delete" @click.stop="deleteDeliveryPhoto(idx)">
              <text class="text-white">×</text>
            </view>
          </view>
          <view v-if="form.storagePhotos.length < 3" class="upload-box"
            @click="choosePhoto('storage')">
            <text class="upload-icon">+</text>
            <text class="upload-text">上传</text>
          </view>
        </view>
      </view>

      <view class="form-item">
        <view class="form-label">收货要求</view>
        <textarea class="form-textarea" v-model="form.receiveRequirement"
          placeholder="如：需冷链配送，需提前1小时通知等" maxlength="500" />
      </view>

      <!-- 区块四：推荐信息 -->
      <view class="section-title">推荐信息</view>

      <view class="form-item">
        <view class="form-label">业务员推荐码</view>
        <input class="form-input" v-model="form.referralCode" placeholder="如有推荐码请输入"
          :disabled="isEdit && referralCodeFixed" @blur="onReferralBlur" />
        <view v-if="referralVerifyText" class="form-hint"
          :class="{ error: !referralVerifyResult?.valid }">
          {{ referralVerifyText }}
        </view>
      </view>

      <view class="form-bottom-hint">
        <text>提示：提交后将分配审核人，请确保信息真实有效</text>
      </view>
    </scroll-view>

    <!-- 底部提交按钮 -->
    <view class="bottom-bar">
      <view class="btn-primary" @click="onSubmit" :class="{ disabled: submitting }">
        {{ submitting ? '提交中...' : (isEdit ? '重新提交审核' : '提交审核') }}
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { companyAddressApi, type CompanyAddressDetail } from '@/api'
import { ossUpload } from '@/utils/oss'

// ---------------------------------------------------------------------------
// 页面参数
// ---------------------------------------------------------------------------

const pages = getCurrentPages()
const currPage = pages[pages.length - 1]
const options = currPage.options as Record<string, string | undefined>
const addressId = options?.id
const isEdit = !!addressId

// ---------------------------------------------------------------------------
// 状态
// ---------------------------------------------------------------------------

const sysInfo = uni.getSystemInfoSync()
const statusBarH = ref(sysInfo.statusBarHeight ?? 20)

const form = ref({
  companyName: '',
  doorPhoto: '',
  licenseNo: '',
  licensePhoto: '',
  contactName: '',
  contactPhone: '',
  province: '',
  city: '',
  district: '',
  address: '',
  longitude: undefined as number | undefined,
  latitude: undefined as number | undefined,
  receiveTimeStart: '00:00',
  receiveTimeEnd: '08:00',
  receiveRequirement: '',
  storagePhotos: [] as string[],
  referralCode: '',
})

const submitting = ref(false)
const auditRejectReason = ref('')
const referralVerifyResult = ref<{ valid: boolean; salespersonName?: string } | null>(null)
const referralCodeFixed = ref(false)

// ---------------------------------------------------------------------------
// 计算属性
// ---------------------------------------------------------------------------

const regionText = computed(() => {
  const parts: string[] = []
  if (form.value.province) parts.push(form.value.province)
  if (form.value.city) parts.push(form.value.city)
  if (form.value.district) parts.push(form.value.district)
  return parts.join(' / ')
})

const referralVerifyText = computed(() => {
  if (!form.value.referralCode) return ''
  if (!referralVerifyResult.value) return ''
  if (referralVerifyResult.value.valid) {
    return `验证通过：${referralVerifyResult.value.salespersonName || '业务员'}`
  }
  return '无效推荐码'
})

// ---------------------------------------------------------------------------
// 图片上传
// ---------------------------------------------------------------------------

async function choosePhoto(type: 'door' | 'license' | 'storage') {
  uni.chooseImage({
    count: type === 'storage' ? 3 - form.value.storagePhotos.length : 1,
    sizeType: ['compressed'],
    sourceType: ['album', 'camera'],
    success: async (res: any) => {
      const tempFilePaths = res.tempFilePaths || []
      if (!tempFilePaths.length) return

      try {
        for (const path of tempFilePaths) {
          const uploadRes = await ossUpload(path, { dir: 'customer-archive' })
          const url = uploadRes.url

          if (type === 'door') {
            form.value.doorPhoto = url
          } else if (type === 'license') {
            form.value.licensePhoto = url
          } else if (type === 'storage') {
            if (form.value.storagePhotos.length < 3) {
              form.value.storagePhotos.push(url)
            }
          }
        }
      } catch (e) {
        uni.showToast({ title: '上传失败，请重试', icon: 'none' })
      }
    },
  })
}

function previewPhoto(url: string) {
  uni.previewImage({ urls: [url], current: 0 })
}

function deletePhoto(type: 'door' | 'license') {
  if (type === 'door') {
    form.value.doorPhoto = ''
  } else if (type === 'license') {
    form.value.licensePhoto = ''
  }
}

function deleteDeliveryPhoto(idx: number) {
  form.value.storagePhotos.splice(idx, 1)
}

// ---------------------------------------------------------------------------
// 地区选择
// ---------------------------------------------------------------------------

function onRegionChange(e: any) {
  const [province, city, district] = e.detail.value as string[]
  form.value.province = province
  form.value.city = city
  form.value.district = district
}

// ---------------------------------------------------------------------------
// 定位
// ---------------------------------------------------------------------------

function onLocate() {
  uni.getLocation({
    type: 'gcj02',
    success: (res) => {
      form.value.longitude = res.longitude
      form.value.latitude = res.latitude
      // 可以使用逆地理编码获取地址文本，这里简化处理
      uni.showToast({ title: '定位成功', icon: 'success', duration: 1500 })
    },
    fail: () => {
      uni.showToast({ title: '定位失败，请手动输入地址', icon: 'none' })
    },
  })
}

// ---------------------------------------------------------------------------
// 时间选择
// ---------------------------------------------------------------------------

function onTimeChange(type: 'start' | 'end', e: any) {
  const time = e.detail.value as string
  if (type === 'start') {
    form.value.receiveTimeStart = time
  } else {
    form.value.receiveTimeEnd = time
  }
}

// ---------------------------------------------------------------------------
// 推荐码验证
// ---------------------------------------------------------------------------

async function onReferralBlur() {
  if (!form.value.referralCode || form.value.referralCode.trim().length === 0) {
    referralVerifyResult.value = null
    return
  }

  try {
    const res = await companyAddressApi.verifyReferralCode(form.value.referralCode.trim())
    if (res.code === 0 || res.code === 200) {
      referralVerifyResult.value = res.data || null
    }
  } catch {
    referralVerifyResult.value = { valid: false }
  }
}

// ---------------------------------------------------------------------------
// 表单验证
// ---------------------------------------------------------------------------

function validateForm(): boolean {
  if (!form.value.companyName.trim()) {
    uni.showToast({ title: '请输入公司名称', icon: 'none' })
    return false
  }
  if (!form.value.licenseNo.trim()) {
    uni.showToast({ title: '请输入营业执照编号', icon: 'none' })
    return false
  }
  // 简单的统一社会信用代码校验（18位）
  if (form.value.licenseNo.length !== 18) {
    uni.showToast({ title: '营业执照编号应为18位', icon: 'none' })
    return false
  }
  if (!form.value.contactName.trim()) {
    uni.showToast({ title: '请输入收货人姓名', icon: 'none' })
    return false
  }
  if (!form.value.contactPhone.trim()) {
    uni.showToast({ title: '请输入收货人联系方式', icon: 'none' })
    return false
  }
  // 简单手机号校验
  if (!/^1[3-9]\d{9}$/.test(form.value.contactPhone.trim())) {
    uni.showToast({ title: '请输入正确的手机号', icon: 'none' })
    return false
  }
  if (!form.value.province || !form.value.city || !form.value.district) {
    uni.showToast({ title: '请选择所在地区', icon: 'none' })
    return false
  }
  if (!form.value.address.trim()) {
    uni.showToast({ title: '请输入详细收货地址', icon: 'none' })
    return false
  }
  if (!form.value.receiveTimeStart || !form.value.receiveTimeEnd) {
    uni.showToast({ title: '请选择可收货时段', icon: 'none' })
    return false
  }

  return true
}

// ---------------------------------------------------------------------------
// 提交
// ---------------------------------------------------------------------------

async function onSubmit() {
  if (!validateForm()) return
  if (submitting.value) return

  submitting.value = true

  try {
    if (isEdit && addressId) {
      // 编辑模式：重新提交
      await companyAddressApi.updateAddress(addressId, form.value)
    } else {
      // 新增模式
      await companyAddressApi.createAddress(form.value)
    }

    uni.showToast({ title: '提交成功，请耐心等待', icon: 'success', duration: 1500 })
    setTimeout(() => {
      uni.navigateBack()
    }, 1500)
  } catch (e) {
    // 错误已由 request.ts 统一处理
  } finally {
    submitting.value = false
  }
}

// ---------------------------------------------------------------------------
// 加载编辑数据
// ---------------------------------------------------------------------------

async function loadEditData() {
  if (!addressId) return

  try {
    const res = await companyAddressApi.getAddressDetail(addressId)
    if (res.code === 0 || res.code === 200) {
      const data = res.data as CompanyAddressDetail
      form.value = {
        companyName: data.companyName,
        doorPhoto: data.doorPhoto,
        licenseNo: data.licenseNo,
        licensePhoto: data.licensePhoto,
        contactName: data.contactName,
        contactPhone: data.contactPhone,
        province: data.province,
        city: data.city,
        district: data.district,
        address: data.address,
        longitude: data.longitude,
        latitude: data.latitude,
        receiveTimeStart: data.receiveTimeStart,
        receiveTimeEnd: data.receiveTimeEnd,
        receiveRequirement: data.receiveRequirement,
        storagePhotos: data.storagePhotos || [],
        referralCode: '',
      }
      auditRejectReason.value = (data as any).auditRejectReason || ''
      // 编辑模式推荐码不可修改（如果已有绑定）
      referralCodeFixed.value = true
    }
  } catch (e) {
    console.error('加载地址详情失败', e)
  }
}

// ---------------------------------------------------------------------------
// 导航
// ---------------------------------------------------------------------------

function goBack() {
  uni.navigateBack()
}

// ---------------------------------------------------------------------------
// 生命周期
// ---------------------------------------------------------------------------

onMounted(() => {
  if (isEdit) {
    loadEditData()
  }
})
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background: #F5F6F7;
  position: relative;
}

.nav-bar {
  padding-left: 8px;
}

.nav-back {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #000000;
}

.nav-title {
  flex: 1;
  text-align: center;
  font-size: 17px;
  font-weight: 600;
  color: #000000;
  margin-right: 36px;
}

.content {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  padding: 12px 16px 100px;
  box-sizing: border-box;
}

.section-title {
  font-size: 15px;
  font-weight: 600;
  color: #000000;
  margin: 24px 0 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid #EEEEEE;
}

.form-item {
  margin-bottom: 16px;
}

.form-label {
  font-size: 14px;
  color: #333333;
  margin-bottom: 8px;
}

.required {
  color: #DC2626;
  margin-right: 2px;
}

.form-input {
  width: 100%;
  height: 48px;
  background: #FFFFFF;
  border: 1px solid #E5E5E5;
  border-radius: 8px;
  padding: 0 14px;
  font-size: 15px;
  box-sizing: border-box;
}

.form-textarea {
  width: 100%;
  min-height: 100px;
  background: #FFFFFF;
  border: 1px solid #E5E5E5;
  border-radius: 8px;
  padding: 12px 14px;
  font-size: 15px;
  box-sizing: border-box;
  line-height: 1.5;
}

.form-picker {
  display: flex;
  align-items: center;
  color: #000000;
}

.form-picker:empty {
  color: #CCCCCC;
}

.input-wrapper {
  position: relative;
}

.locate-btn {
  position: absolute;
  right: 10px;
  top: 50%;
  transform: translateY(-50%);
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.locate-icon {
  font-size: 20px;
}

.time-range {
  display: flex;
  align-items: center;
  gap: 12px;
}

.time-sep {
  font-size: 15px;
  color: #333333;
}

.form-hint {
  margin-top: 8px;
  font-size: 12px;
  color: #065F46;
}

.form-hint.error {
  color: #DC2626;
}

/* ===== 上传区 ===== */
.upload-area {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.upload-box {
  width: 88px;
  height: 88px;
  background: #F5F6F7;
  border: 1px dashed #DDDDDD;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 2px;
}

.upload-icon {
  font-size: 28px;
  line-height: 1;
  color: #999999;
}

.upload-text {
  font-size: 12px;
  color: #999999;
}

.upload-preview {
  position: relative;
  width: 88px;
  height: 88px;
  border-radius: 8px;
  overflow: hidden;
}

.preview-img {
  width: 100%;
  height: 100%;
  display: block;
}

.upload-delete {
  position: absolute;
  right: 0;
  top: 0;
  width: 24px;
  height: 24px;
  background: rgba(0, 0, 0, 0.5);
  border-radius: 0 8px 0 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  line-height: 1;
}

.form-bottom-hint {
  margin-top: 24px;
  padding: 12px 14px;
  background: #FFF7ED;
  border-radius: 8px;
  font-size: 12px;
  color: #92400E;
  line-height: 1.5;
}

/* ===== 驳回提示 ===== */
.reject-notice {
  margin-bottom: 16px;
  padding: 12px 14px;
  background: #FEF2F2;
  border: 1px solid #FCA5A5;
  border-radius: 8px;
}

.reject-notice-title {
  font-size: 13px;
  font-weight: 600;
  color: #DC2626;
  margin-bottom: 4px;
}

.reject-notice-text {
  font-size: 13px;
  color: #991B1B;
  line-height: 1.5;
}

/* ===== 底部按钮 ===== */
.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: #FFFFFF;
  padding: 12px 16px;
  padding-bottom: calc(12px + env(safe-area-inset-bottom));
  border-top: 1px solid #EEEEEE;
  z-index: 50;
}

.btn-primary {
  width: 100%;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #FF5722 0%, #FF4D2D 100%);
  border-radius: 24px;
  color: #FFFFFF;
  font-size: 16px;
  font-weight: 600;
}

.btn-primary.disabled {
  background: #CCCCCC;
}
</style>
