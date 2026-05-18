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
            </div>
            <el-button type="primary" size="large" @click="showPending">立即报课</el-button>
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
                <p>{{ course.teacher.title }} · {{ course.teacher.yearsExperience || 0 }} 年经验</p>
                <p>{{ course.teacher.specialties }}</p>
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
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getCourse } from '../api/courses'
import { courseTypeText, formatMoney } from '../utils/format'

const route = useRoute()
const course = ref(null)
const loading = ref(false)
const fallbackCover = 'https://images.unsplash.com/photo-1523580846011-d3a5bc25702b?auto=format&fit=crop&w=1000&q=80'
const fallbackAvatar = 'https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=240&q=80'

const showPending = () => {
  ElMessage.info('订单服务暂未接入')
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
