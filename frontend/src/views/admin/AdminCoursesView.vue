<template>
  <section>
    <div class="section-heading">
      <div>
        <h1>课程管理</h1>
        <p>维护课程基础信息、价格、教师和上下架状态。</p>
      </div>
      <el-button type="primary" @click="openCreate">新增课程</el-button>
    </div>
    <div class="filter-bar">
      <el-select v-model="query.categoryId" clearable placeholder="全部分类" @change="load">
        <el-option v-for="item in categories" :key="item.id" :label="item.name" :value="item.id" />
      </el-select>
      <el-input v-model="query.keyword" clearable placeholder="搜索课程" @keyup.enter="load" />
      <el-button type="primary" @click="load">搜索</el-button>
    </div>
    <el-table v-loading="loading" :data="courses" border>
      <el-table-column prop="title" label="课程" min-width="180" />
      <el-table-column prop="categoryName" label="分类" width="120" />
      <el-table-column label="教师" width="120"><template #default="{ row }">{{ row.teacher?.name || '-' }}</template></el-table-column>
      <el-table-column label="类型" width="100"><template #default="{ row }">{{ courseTypeText[row.courseType] || row.courseType }}</template></el-table-column>
      <el-table-column label="价格" width="120"><template #default="{ row }">{{ formatMoney(row.price) }}</template></el-table-column>
      <el-table-column label="库存" width="120"><template #default="{ row }">{{ row.availableStock ?? 0 }} / {{ row.stock ?? 0 }}</template></el-table-column>
      <el-table-column label="状态" width="100"><template #default="{ row }">{{ courseStatusText[row.status] || row.status }}</template></el-table-column>
      <el-table-column label="操作" width="230" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="openEdit(row.id)">编辑</el-button>
          <el-button v-if="row.status !== 'ON_SALE'" size="small" type="success" @click="changeSale(row, true)">上架</el-button>
          <el-button v-else size="small" type="warning" @click="changeSale(row, false)">下架</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑课程' : '新增课程'" width="720px">
      <el-form :model="form" label-width="96px">
        <el-form-item label="课程标题"><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="副标题"><el-input v-model="form.subtitle" /></el-form-item>
        <el-form-item label="封面地址"><el-input v-model="form.coverUrl" /></el-form-item>
        <el-form-item label="分类">
          <el-select v-model="form.categoryId">
            <el-option v-for="item in categories" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="教师">
          <el-select v-model="form.teacherId">
            <el-option v-for="item in teachers" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="课程类型">
          <el-select v-model="form.courseType">
            <el-option label="线上课" value="ONLINE" />
            <el-option label="线下课" value="OFFLINE" />
            <el-option label="录播课" value="RECORDED" />
          </el-select>
        </el-form-item>
        <el-form-item label="价格"><el-input-number v-model="form.price" :precision="2" :min="0" /></el-form-item>
        <el-form-item label="原价"><el-input-number v-model="form.originalPrice" :precision="2" :min="0" /></el-form-item>
        <el-form-item label="库存"><el-input-number v-model="form.stock" :min="0" /></el-form-item>
        <el-form-item label="课时描述"><el-input v-model="form.durationDesc" /></el-form-item>
        <el-form-item label="适合人群"><el-input v-model="form.targetAudience" /></el-form-item>
        <el-form-item label="课程介绍"><el-input v-model="form.intro" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="课程大纲"><el-input v-model="form.outline" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status">
            <el-option label="草稿" value="DRAFT" />
            <el-option label="上架" value="ON_SALE" />
            <el-option label="下架" value="OFF_SALE" />
          </el-select>
        </el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sortOrder" :min="0" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { listAdminCategories } from '../../api/categories'
import { createCourse, getAdminCourse, listAdminCourses, offSaleCourse, onSaleCourse, updateCourse } from '../../api/courses'
import { listAdminTeachers } from '../../api/teachers'
import { courseStatusText, courseTypeText, formatMoney } from '../../utils/format'

const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const editingId = ref(null)
const courses = ref([])
const categories = ref([])
const teachers = ref([])
const query = reactive({ categoryId: null, keyword: '' })
const form = reactive({})

const emptyForm = () => ({
  categoryId: null,
  teacherId: null,
  title: '',
  subtitle: '',
  coverUrl: '',
  price: 0,
  originalPrice: 0,
  stock: 0,
  courseType: 'ONLINE',
  durationDesc: '',
  targetAudience: '',
  intro: '',
  outline: '',
  status: 'DRAFT',
  sortOrder: 0
})

const coursePayload = () => ({
  categoryId: form.categoryId,
  teacherId: form.teacherId,
  title: form.title,
  subtitle: form.subtitle,
  coverUrl: form.coverUrl,
  price: form.price,
  originalPrice: form.originalPrice,
  stock: form.stock,
  courseType: form.courseType,
  durationDesc: form.durationDesc,
  targetAudience: form.targetAudience,
  intro: form.intro,
  outline: form.outline,
  status: form.status,
  sortOrder: form.sortOrder
})
const resetForm = (data = emptyForm()) => {
  Object.assign(form, emptyForm(), {
    categoryId: data.categoryId,
    teacherId: data.teacher?.id || data.teacherId,
    title: data.title,
    subtitle: data.subtitle,
    coverUrl: data.coverUrl,
    price: data.price,
    originalPrice: data.originalPrice,
    stock: data.stock ?? 0,
    courseType: data.courseType,
    durationDesc: data.durationDesc,
    targetAudience: data.targetAudience,
    intro: data.intro,
    outline: data.outline,
    status: data.status,
    sortOrder: data.sortOrder
  })
}
const loadOptions = async () => {
  const [categoryData, teacherData] = await Promise.all([listAdminCategories(), listAdminTeachers()])
  categories.value = categoryData
  teachers.value = teacherData
}
const load = async () => {
  loading.value = true
  try {
    courses.value = await listAdminCourses({ categoryId: query.categoryId || undefined, keyword: query.keyword || undefined })
  } finally {
    loading.value = false
  }
}
const openCreate = () => {
  editingId.value = null
  resetForm()
  dialogVisible.value = true
}
const openEdit = async (id) => {
  editingId.value = id
  resetForm(await getAdminCourse(id))
  dialogVisible.value = true
}
const save = async () => {
  saving.value = true
  try {
    if (editingId.value) await updateCourse(editingId.value, coursePayload())
    else await createCourse(coursePayload())
    ElMessage.success('保存成功')
    dialogVisible.value = false
    load()
  } finally {
    saving.value = false
  }
}
const changeSale = async (row, shouldOnSale) => {
  if (shouldOnSale) await onSaleCourse(row.id)
  else await offSaleCourse(row.id)
  ElMessage.success('状态已更新')
  load()
}

onMounted(() => {
  loadOptions()
  load()
})
</script>
