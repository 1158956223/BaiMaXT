<template>
  <section>
    <div class="section-heading">
      <div>
        <h1>订单管理</h1>
        <p>查看报课订单、支付状态和用户报名记录。</p>
      </div>
      <el-button @click="resetFilters">重置筛选</el-button>
    </div>

    <div class="filter-bar order-filter">
      <el-input v-model="query.userId" clearable placeholder="用户 ID" @keyup.enter="load" />
      <el-select v-model="query.orderStatus" clearable placeholder="订单状态" @change="load">
        <el-option v-for="(label, value) in orderStatusText" :key="value" :label="label" :value="value" />
      </el-select>
      <el-select v-model="query.payStatus" clearable placeholder="支付状态" @change="load">
        <el-option v-for="item in orderPayStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
      </el-select>
      <el-button type="primary" @click="load">查询</el-button>
    </div>

    <el-table v-loading="loading" :data="orders" border>
      <el-table-column prop="orderNo" label="订单号" min-width="180" />
      <el-table-column prop="userId" label="用户 ID" width="100" />
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
    </el-table>

    <el-empty v-if="!loading && !orders.length" description="暂无订单" />
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { listAdminOrders } from '../../api/orders'
import { formatDateTime, formatMoney, orderStatusText, payStatusText } from '../../utils/format'

const loading = ref(false)
const orders = ref([])
const query = reactive({ userId: '', orderStatus: '', payStatus: '' })
const orderPayStatusOptions = [
  { value: 'UNPAID', label: payStatusText.UNPAID },
  { value: 'PAID', label: payStatusText.PAID }
]

const orderTagType = (status) => {
  if (status === 'PAID') return 'success'
  if (status === 'CANCELLED' || status === 'EXPIRED') return 'info'
  return 'warning'
}

const load = async () => {
  loading.value = true
  try {
    orders.value = await listAdminOrders({
      userId: query.userId || undefined,
      orderStatus: query.orderStatus || undefined,
      payStatus: query.payStatus || undefined
    })
  } finally {
    loading.value = false
  }
}

const resetFilters = () => {
  query.userId = ''
  query.orderStatus = ''
  query.payStatus = ''
  load()
}

onMounted(load)
</script>
