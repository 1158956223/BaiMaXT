<template>
  <section class="content-section">
    <div class="section-heading">
      <div>
        <h1>我的订单</h1>
        <p>查看报课记录、支付状态和后续操作。</p>
      </div>
      <el-button type="primary" @click="$router.push('/courses')">继续选课</el-button>
    </div>

    <el-table v-loading="loading" :data="orders" border>
      <el-table-column prop="orderNo" label="订单号" min-width="180" />
      <el-table-column prop="courseTitle" label="课程" min-width="180" />
      <el-table-column prop="teacherName" label="教师" width="120" />
      <el-table-column label="金额" width="120">
        <template #default="{ row }">{{ formatMoney(row.payAmount) }}</template>
      </el-table-column>
      <el-table-column label="订单状态" width="110">
        <template #default="{ row }">
          <el-tag :type="orderTagType(row.orderStatus)">{{ orderStatusText[row.orderStatus] || row.orderStatus }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="支付状态" width="110">
        <template #default="{ row }">
          <el-tag :type="row.payStatus === 'PAID' ? 'success' : 'warning'">{{ payStatusText[row.payStatus] || row.payStatus }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" width="170">
        <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="primary" @click="$router.push(`/orders/${row.id}`)">查看</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-empty v-if="!loading && !orders.length" description="暂无订单，先去课程中心看看吧" />
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { listMyOrders } from '../api/orders'
import { useAuthStore } from '../stores/auth'
import { formatDateTime, formatMoney, orderStatusText, payStatusText } from '../utils/format'

const auth = useAuthStore()
const loading = ref(false)
const orders = ref([])

const orderTagType = (status) => {
  if (status === 'PAID') return 'success'
  if (status === 'CANCELLED' || status === 'EXPIRED') return 'info'
  return 'warning'
}

const load = async () => {
  loading.value = true
  try {
    orders.value = await listMyOrders(auth.currentUserId)
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>
