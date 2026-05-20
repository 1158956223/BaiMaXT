<template>
  <section class="content-section narrow">
    <div class="section-heading">
      <div>
        <h1>订单详情</h1>
        <p>确认报课信息并完成模拟支付。</p>
      </div>
      <el-button @click="$router.push('/orders')">返回订单列表</el-button>
    </div>

    <el-skeleton :loading="loading" animated :rows="8">
      <template v-if="order">
        <div class="info-panel">
          <div class="info-panel-head">
            <div>
              <strong>{{ order.courseTitle }}</strong>
              <span>{{ order.orderNo }}</span>
            </div>
            <el-tag :type="order.orderStatus === 'PAID' ? 'success' : 'warning'">
              {{ orderStatusText[order.orderStatus] || order.orderStatus }}
            </el-tag>
          </div>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="课程">{{ order.courseTitle }}</el-descriptions-item>
            <el-descriptions-item label="教师">{{ order.teacherName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="应付金额">{{ formatMoney(order.payAmount) }}</el-descriptions-item>
            <el-descriptions-item label="支付状态">{{ payStatusText[order.payStatus] || order.payStatus }}</el-descriptions-item>
            <el-descriptions-item label="创建时间">{{ formatDateTime(order.createdAt) }}</el-descriptions-item>
            <el-descriptions-item label="过期时间">{{ formatDateTime(order.expireTime) }}</el-descriptions-item>
            <el-descriptions-item label="备注">{{ order.remark || '-' }}</el-descriptions-item>
          </el-descriptions>
          <div class="action-row">
            <el-button
              v-if="order.orderStatus === 'CREATED'"
              type="primary"
              :loading="paying"
              @click="goPay"
            >
              模拟支付
            </el-button>
            <el-button
              v-if="order.orderStatus === 'CREATED'"
              :loading="cancelling"
              @click="cancel"
            >
              取消订单
            </el-button>
          </div>
        </div>

        <div class="info-panel">
          <h2>状态记录</h2>
          <el-timeline v-if="detail.statusLogs?.length">
            <el-timeline-item
              v-for="log in detail.statusLogs"
              :key="log.id || `${log.fromStatus}-${log.toStatus}-${log.createdAt}`"
              :timestamp="formatDateTime(log.createdAt)"
            >
              {{ orderStatusText[log.newOrderStatus] || log.newOrderStatus || payStatusText[log.newPayStatus] || log.newPayStatus || log.operateType }} {{ log.remark || '' }}
            </el-timeline-item>
          </el-timeline>
          <el-empty v-else description="暂无状态记录" />
        </div>
      </template>
      <el-empty v-else description="订单不存在" />
    </el-skeleton>
  </section>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { cancelOrder, getOrder } from '../api/orders'
import { createPayment } from '../api/payments'
import { useAuthStore } from '../stores/auth'
import { formatDateTime, formatMoney, orderStatusText, payStatusText } from '../utils/format'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const loading = ref(false)
const paying = ref(false)
const cancelling = ref(false)
const detail = ref({})
const order = computed(() => detail.value.order)

const load = async () => {
  loading.value = true
  try {
    detail.value = await getOrder(route.params.id, auth.currentUserId)
  } finally {
    loading.value = false
  }
}

const goPay = async () => {
  paying.value = true
  try {
    const payment = await createPayment({
      orderNo: order.value.orderNo,
      userId: auth.currentUserId,
      payType: 'MOCK'
    })
    ElMessage.success('模拟支付单已创建')
    router.push(`/payments/${payment.payNo}`)
  } finally {
    paying.value = false
  }
}

const cancel = async () => {
  cancelling.value = true
  try {
    await cancelOrder(order.value.id, auth.currentUserId)
    ElMessage.success('订单已取消')
    await load()
  } finally {
    cancelling.value = false
  }
}

onMounted(load)
</script>
