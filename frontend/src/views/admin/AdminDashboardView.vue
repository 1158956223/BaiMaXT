<template>
  <section>
    <div class="section-heading">
      <div>
        <h1>后台概览</h1>
        <p>维护课程、分类、教师、用户和报课订单数据。</p>
      </div>
    </div>
    <div class="metric-grid">
      <div class="metric"><strong>{{ stats.courses }}</strong><span>课程</span></div>
      <div class="metric"><strong>{{ stats.categories }}</strong><span>分类</span></div>
      <div class="metric"><strong>{{ stats.teachers }}</strong><span>教师</span></div>
      <div class="metric"><strong>{{ stats.users }}</strong><span>用户</span></div>
      <div class="metric"><strong>{{ stats.orders }}</strong><span>订单</span></div>
      <div class="metric"><strong>{{ stats.unpaidOrders }}</strong><span>待支付</span></div>
      <div class="metric"><strong>{{ stats.paidOrders }}</strong><span>已支付</span></div>
      <div class="metric"><strong>{{ stats.todayOrders }}</strong><span>今日订单</span></div>
    </div>

    <div class="info-panel dashboard-panel">
      <div class="section-heading compact">
        <div>
          <h1>最近订单</h1>
          <p>展示最新的报课订单。</p>
        </div>
        <el-button @click="$router.push('/admin/orders')">查看全部</el-button>
      </div>
      <el-table :data="recentOrders" border>
        <el-table-column prop="orderNo" label="订单号" min-width="170" />
        <el-table-column prop="courseTitle" label="课程" min-width="180" />
        <el-table-column label="金额" width="110">
          <template #default="{ row }">{{ formatMoney(row.payAmount) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="110">
          <template #default="{ row }">{{ orderStatusText[row.orderStatus] || row.orderStatus }}</template>
        </el-table-column>
        <el-table-column label="创建时间" width="170">
          <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
        </el-table-column>
      </el-table>
    </div>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { listAdminCategories } from '../../api/categories'
import { listAdminCourses } from '../../api/courses'
import { listAdminOrders } from '../../api/orders'
import { listAdminTeachers } from '../../api/teachers'
import { listUsers } from '../../api/users'
import { formatDateTime, formatMoney, orderStatusText } from '../../utils/format'

const stats = reactive({
  courses: 0,
  categories: 0,
  teachers: 0,
  users: 0,
  orders: 0,
  unpaidOrders: 0,
  paidOrders: 0,
  todayOrders: 0
})
const orders = ref([])
const recentOrders = computed(() => orders.value.slice(0, 5))

const isToday = (value) => {
  if (!value) return false
  const date = new Date(value)
  const now = new Date()
  return date.getFullYear() === now.getFullYear()
    && date.getMonth() === now.getMonth()
    && date.getDate() === now.getDate()
}

onMounted(async () => {
  const [courses, categories, teachers, users, orderResult] = await Promise.allSettled([
    listAdminCourses(),
    listAdminCategories(),
    listAdminTeachers(),
    listUsers(),
    listAdminOrders()
  ])
  orders.value = orderResult.value || []
  stats.courses = courses.value?.length || 0
  stats.categories = categories.value?.length || 0
  stats.teachers = teachers.value?.length || 0
  stats.users = users.value?.length || 0
  stats.orders = orders.value.length
  stats.unpaidOrders = orders.value.filter((item) => item.payStatus === 'UNPAID').length
  stats.paidOrders = orders.value.filter((item) => item.payStatus === 'PAID').length
  stats.todayOrders = orders.value.filter((item) => isToday(item.createdAt)).length
})
</script>
