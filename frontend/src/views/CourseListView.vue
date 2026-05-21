<template>
  <section class="content-section">
    <div class="section-heading">
      <div>
        <h1>课程中心</h1>
        <p>浏览已上架课程，按分类和关键词快速筛选。</p>
      </div>
      <el-button type="primary" @click="$router.push('/ai')">咨询 AI 客服</el-button>
    </div>

    <div class="filter-bar">
      <el-select v-model="query.categoryId" clearable placeholder="全部分类" @change="loadCourses">
        <el-option v-for="item in categories" :key="item.id" :label="item.name" :value="item.id" />
      </el-select>
      <el-input v-model="query.keyword" clearable placeholder="搜索课程名称、简介" @keyup.enter="loadCourses" />
      <el-button type="primary" @click="loadCourses">搜索</el-button>
    </div>

    <el-skeleton :loading="loading" animated :rows="6">
      <div v-if="courses.length" class="course-grid">
        <article v-for="course in courses" :key="course.id" class="course-card" @click="$router.push(`/courses/${course.id}`)">
          <img :src="course.coverUrl || fallbackCover" :alt="course.title" />
          <div class="course-card-body">
            <div class="course-card-meta">
              <el-tag size="small">{{ courseTypeText[course.courseType] || course.courseType }}</el-tag>
              <span>{{ course.categoryName || '未分类' }}</span>
            </div>
            <h2>{{ course.title }}</h2>
            <p>{{ course.subtitle || course.durationDesc || '课程信息待完善' }}</p>
            <div class="course-card-footer">
              <strong>{{ formatMoney(course.price) }}</strong>
              <span>{{ course.teacher?.name || '暂无教师' }}</span>
            </div>
          </div>
        </article>
      </div>
      <el-empty v-else description="暂无课程" />
    </el-skeleton>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { listCategories } from '../api/categories'
import { searchCourses } from '../api/search'
import { courseTypeText, formatMoney } from '../utils/format'

const fallbackCover = 'https://images.unsplash.com/photo-1523580846011-d3a5bc25702b?auto=format&fit=crop&w=900&q=80'
const loading = ref(false)
const categories = ref([])
const courses = ref([])
const query = reactive({ categoryId: null, keyword: '' })

const loadCategories = async () => {
  categories.value = await listCategories()
}

const loadCourses = async () => {
  loading.value = true
  try {
    const result = await searchCourses({
      categoryId: query.categoryId || undefined,
      q: query.keyword || undefined,
      page: 0,
      size: 50
    })
    courses.value = (result.records || []).map((course) => ({
      ...course,
      teacher: {
        id: course.teacherId,
        name: course.teacherName
      }
    }))
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadCategories()
  loadCourses()
})
</script>
