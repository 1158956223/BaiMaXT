<template>
  <section>
    <div class="section-heading">
      <div>
        <h1>后台概览</h1>
        <p>维护课程、分类、教师和用户基础数据。</p>
      </div>
    </div>
    <div class="metric-grid">
      <div class="metric"><strong>{{ stats.courses }}</strong><span>课程</span></div>
      <div class="metric"><strong>{{ stats.categories }}</strong><span>分类</span></div>
      <div class="metric"><strong>{{ stats.teachers }}</strong><span>教师</span></div>
      <div class="metric"><strong>{{ stats.users }}</strong><span>用户</span></div>
    </div>
  </section>
</template>

<script setup>
import { onMounted, reactive } from 'vue'
import { listAdminCategories } from '../../api/categories'
import { listAdminCourses } from '../../api/courses'
import { listAdminTeachers } from '../../api/teachers'
import { listUsers } from '../../api/users'

const stats = reactive({ courses: 0, categories: 0, teachers: 0, users: 0 })

onMounted(async () => {
  const [courses, categories, teachers, users] = await Promise.allSettled([
    listAdminCourses(),
    listAdminCategories(),
    listAdminTeachers(),
    listUsers()
  ])
  stats.courses = courses.value?.length || 0
  stats.categories = categories.value?.length || 0
  stats.teachers = teachers.value?.length || 0
  stats.users = users.value?.length || 0
})
</script>
