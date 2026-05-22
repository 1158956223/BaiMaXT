<template>
  <section class="content-section">
    <el-skeleton :loading="loading" animated :rows="10">
      <template v-if="course">
        <div class="detail-hero">
          <img :src="course.coverUrl || fallbackCover" :alt="course.title" />
          <div class="detail-summary">
            <el-tag>{{ courseTypeText[course.courseType] || course.courseType }}</el-tag>
            <h1>{{ course.title }}</h1>
            <p>{{ course.subtitle }}</p>
            <div class="price-line">
              <strong>{{ formatMoney(course.price) }}</strong>
              <span v-if="course.originalPrice">原价 {{ formatMoney(course.originalPrice) }}</span>
            </div>
            <div class="summary-grid">
              <span>分类：{{ course.categoryName || '未分类' }}</span>
              <span>课时：{{ course.durationDesc || '待定' }}</span>
              <span>教师：{{ course.teacher?.name || '待定' }}</span>
              <span>库存：{{ course.availableStock ?? 0 }} / {{ course.stock ?? 0 }}</span>
            </div>
            <el-button type="primary" size="large" :loading="ordering" :disabled="isSoldOut || !canEnroll" @click="submitOrder">
              {{ actionText }}
            </el-button>
          </div>
        </div>

        <div class="detail-sections">
          <section>
            <h2>适合人群</h2>
            <p>{{ course.targetAudience || '暂无适合人群说明' }}</p>
          </section>
          <section>
            <h2>课程介绍</h2>
            <p>{{ course.intro || '暂无课程介绍' }}</p>
          </section>
          <section>
            <h2>课程大纲</h2>
            <p>{{ course.outline || '暂无课程大纲' }}</p>
          </section>
          <section v-if="course.teacher">
            <h2>授课教师</h2>
            <div class="teacher-strip">
              <img :src="course.teacher.avatarUrl || fallbackAvatar" :alt="course.teacher.name" />
              <div>
                <strong>{{ course.teacher.name }}</strong>
                <p>{{ course.teacher.title || '授课教师' }} · {{ course.teacher.yearsExperience || 0 }} 年经验</p>
                <p>{{ course.teacher.specialties || '暂无擅长领域' }}</p>
              </div>
            </div>
          </section>
        </div>
      </template>
      <el-empty v-else description="课程不存在" />
    </el-skeleton>
  </section>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getCourse } from '../api/courses'
import { createOrder } from '../api/orders'
import { useAuthStore } from '../stores/auth'
import { courseTypeText, formatMoney } from '../utils/format'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const course = ref(null)
const loading = ref(false)
const ordering = ref(false)
const fallbackCover = 'https://images.unsplash.com/photo-1523580846011-d3a5bc25702b?auto=format&fit=crop&w=1000&q=80'
const fallbackAvatar = 'https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=240&q=80'
const isSoldOut = computed(() => (course.value?.availableStock ?? 0) <= 0)
const canEnroll = computed(() => !auth.token || auth.user?.role === 'STUDENT')
const actionText = computed(() => {
  if (isSoldOut.value) return '已售罄'
  if (!canEnroll.value) return '仅学生可报名'
  return '立即报课'
})

const submitOrder = async () => {
  if (isSoldOut.value) {
    ElMessage.warning('课程库存不足')
    return
  }
  if (!canEnroll.value) {
    ElMessage.warning('只有学生账号可以报名课程')
    return
  }
  if (!auth.token) {
    router.push({ path: '/login', query: { redirect: route.fullPath } })
    return
  }
  if (!auth.currentUserId) {
    ElMessage.error('当前登录用户信息不完整，请重新登录')
    return
  }
  ordering.value = true
  try {
    const order = await createOrder({
      userId: auth.currentUserId,
      courseId: Number(route.params.id),
      remark: ''
    })
    ElMessage.success('报课订单已创建')
    router.push(`/orders/${order.id}`)
  } finally {
    ordering.value = false
  }
}

onMounted(async () => {
  loading.value = true
  try {
    course.value = await getCourse(route.params.id)
  } finally {
    loading.value = false
  }
})
</script>
