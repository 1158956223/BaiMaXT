<template>
  <section class="content-section narrow">
    <div class="section-heading">
      <div>
        <h1>模拟支付</h1>
        <p>第一版使用模拟支付完成报课流程。</p>
      </div>
      <el-button @click="$router.push('/orders')">我的订单</el-button>
    </div>

    <el-skeleton :loading="loading" animated :rows="6">
      <div v-if="payment" class="info-panel">
        <div class="info-panel-head">
          <div>
            <strong>{{ payment.subject || payment.orderNo }}</strong>
            <span>{{ payment.payNo }}</span>
          </div>
          <el-tag :type="payment.payStatus === 'SUCCESS' ? 'success' : 'warning'">
            {{ payStatusText[payment.payStatus] || payment.payStatus }}
          </el-tag>
        </div>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="订单号">{{ payment.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="支付金额">{{ formatMoney(payment.payAmount) }}</el-descriptions-item>
          <el-descriptions-item label="支付方式">{{ payTypeText[payment.payType] || payment.payType }}</el-descriptions-item>
          <el-descriptions-item label="支付状态">{{ payStatusText[payment.payStatus] || payment.payStatus }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ formatDateTime(payment.createdAt) }}</el-descriptions-item>
          <el-descriptions-item label="过期时间">{{ formatDateTime(payment.expireTime) }}</el-descriptions-item>
        </el-descriptions>
        <div class="action-row" v-if="payment.payStatus === 'WAITING'">
          <el-button type="primary" :loading="submitting" @click="finish(true)">支付成功</el-button>
          <el-button :loading="submitting" @click="finish(false)">支付失败</el-button>
        </div>
        <el-result
          v-else-if="payment.payStatus === 'SUCCESS'"
          icon="success"
          title="报课成功"
          sub-title="支付已完成，可以在我的订单中查看记录。"
        >
          <template #extra>
            <el-button type="primary" @click="$router.push('/orders')">查看我的订单</el-button>
          </template>
        </el-result>
        <el-result
          v-else
          icon="warning"
          title="支付未完成"
          sub-title="当前支付单不可继续支付，可以返回订单详情重新发起。"
        />
      </div>
      <el-empty v-else description="支付单不存在" />
    </el-skeleton>
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getPayment, mockPaymentFail, mockPaymentSuccess } from '../api/payments'
import { useAuthStore } from '../stores/auth'
import { formatDateTime, formatMoney, payStatusText, payTypeText } from '../utils/format'

const route = useRoute()
const auth = useAuthStore()
const loading = ref(false)
const submitting = ref(false)
const payment = ref(null)

const load = async () => {
  loading.value = true
  try {
    const detail = await getPayment(route.params.payNo, auth.currentUserId)
    payment.value = detail.payment || detail
  } finally {
    loading.value = false
  }
}

const finish = async (success) => {
  submitting.value = true
  try {
    payment.value = success
      ? await mockPaymentSuccess(route.params.payNo, auth.currentUserId)
      : await mockPaymentFail(route.params.payNo, auth.currentUserId)
    ElMessage.success(success ? '支付成功' : '已记录支付失败')
  } finally {
    submitting.value = false
  }
}

onMounted(load)
</script>
