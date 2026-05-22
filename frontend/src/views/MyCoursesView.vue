<template>
  <section class="content-section">
    <div class="section-heading">
      <div>
        <h1>我的课程</h1>
        <p>查看已经报名成功的课程，继续学习或回看订单信息。</p>
      </div>
      <el-button type="primary" @click="$router.push('/courses')">继续选课</el-button>
    </div>

    <el-skeleton :loading="loading" animated :rows="6">
      <div v-if="courses.length" class="student-course-grid">
        <article v-for="course in courses" :key="course.orderId" class="student-course-card">
          <div class="student-course-main">
            <el-tag type="success" size="small">已报名</el-tag>
            <h2>{{ course.courseTitle }}</h2>
            <p>{{ course.courseSubtitle || '暂无课程副标题' }}</p>
            <div class="student-course-meta">
              <span>教师：{{ course.teacherName || '待定' }}</span>
              <span>支付：{{ formatMoney(course.payAmount) }}</span>
              <span>报名时间：{{ formatDateTime(course.payTime || course.createdAt) }}</span>
            </div>
          </div>
          <div class="student-course-actions">
            <el-button type="primary" @click="$router.push(`/courses/${course.courseId}`)">查看课程</el-button>
            <el-button @click="$router.push(`/orders/${course.orderId}`)">订单详情</el-button>
          </div>
        </article>
      </div>
      <el-empty v-else description="暂无已报名课程，先去课程中心看看吧" />
    </el-skeleton>
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { listMyCourses } from '../api/orders'
import { useAuthStore } from '../stores/auth'
import { formatDateTime, formatMoney } from '../utils/format'

const auth = useAuthStore()
const loading = ref(false)
const courses = ref([])

const load = async () => {
  loading.value = true
  try {
    courses.value = await listMyCourses(auth.currentUserId)
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.student-course-grid {
  display: grid;
  gap: 16px;
}

.student-course-card {
  display: flex;
  justify-content: space-between;
  gap: 20px;
  padding: 20px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  background: #fff;
}

.student-course-main {
  min-width: 0;
}

.student-course-main h2 {
  margin: 10px 0 8px;
  font-size: 18px;
}

.student-course-main p {
  margin: 0 0 12px;
  color: #606266;
}

.student-course-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 10px 18px;
  color: #606266;
  font-size: 14px;
}

.student-course-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-shrink: 0;
}

@media (max-width: 720px) {
  .student-course-card {
    flex-direction: column;
  }

  .student-course-actions {
    align-items: stretch;
  }
}
</style>
