<template>
  <section>
    <div class="section-heading">
      <div>
        <h1>我的课程</h1>
        <p>维护自己创建的课程，查看报名学生和课程经营数据。</p>
      </div>
      <el-button type="primary" @click="openCreate">新增课程</el-button>
    </div>

    <div class="teacher-summary">
      <div class="metric">
        <strong>{{ stats.totalCourses || 0 }}</strong>
        <span>课程数</span>
      </div>
      <div class="metric">
        <strong>{{ stats.paidEnrollments || 0 }}</strong>
        <span>报名人数</span>
      </div>
      <div class="metric">
        <strong>{{ stats.availableStock || 0 }}</strong>
        <span>剩余库存</span>
      </div>
      <div class="metric">
        <strong>{{ formatMoney(stats.revenue || 0) }}</strong>
        <span>销售额</span>
      </div>
    </div>

    <el-tabs v-model="activeTab" class="teacher-tabs">
      <el-tab-pane label="课程管理" name="courses">
        <div class="filter-bar">
          <el-select v-model="query.categoryId" clearable placeholder="全部分类" @change="loadCourses">
            <el-option v-for="item in categories" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
          <el-input v-model="query.keyword" clearable placeholder="搜索课程" @keyup.enter="loadCourses" />
          <el-button type="primary" @click="loadCourses">搜索</el-button>
        </div>

        <el-table v-loading="loadingCourses" :data="courses" border>
          <el-table-column prop="title" label="课程" min-width="180" />
          <el-table-column prop="categoryName" label="分类" width="120" />
          <el-table-column label="类型" width="100">
            <template #default="{ row }">{{ courseTypeText[row.courseType] || row.courseType }}</template>
          </el-table-column>
          <el-table-column label="价格" width="120">
            <template #default="{ row }">{{ formatMoney(row.price) }}</template>
          </el-table-column>
          <el-table-column label="库存" width="120">
            <template #default="{ row }">{{ row.availableStock ?? 0 }} / {{ row.stock ?? 0 }}</template>
          </el-table-column>
          <el-table-column label="已报名" width="100">
            <template #default="{ row }">{{ row.soldCount ?? 0 }}</template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template #default="{ row }">{{ courseStatusText[row.status] || row.status }}</template>
          </el-table-column>
          <el-table-column label="更新时间" width="170">
            <template #default="{ row }">{{ formatDateTime(row.updatedAt) }}</template>
          </el-table-column>
          <el-table-column label="排序" prop="sortOrder" width="90" />
          <el-table-column label="操作" width="180" fixed="right">
            <template #default="{ row }">
              <el-button size="small" @click="openEdit(row.id)">编辑</el-button>
              <el-button size="small" type="danger" @click="remove(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-if="!loadingCourses && !courses.length" description="暂无课程" />
      </el-tab-pane>

      <el-tab-pane label="报名学生" name="students">
        <div class="filter-bar">
          <el-select v-model="enrollmentQuery.courseId" clearable placeholder="全部课程" @change="loadEnrollments">
            <el-option v-for="item in courses" :key="item.id" :label="item.title" :value="item.id" />
          </el-select>
          <el-button type="primary" @click="loadEnrollments">刷新</el-button>
        </div>

        <el-table v-loading="loadingEnrollments" :data="enrollments" border>
          <el-table-column prop="studentName" label="学生" width="140" />
          <el-table-column prop="studentUserId" label="学生 ID" width="100" />
          <el-table-column prop="courseTitle" label="报名课程" min-width="180" />
          <el-table-column prop="orderNo" label="订单号" min-width="180" />
          <el-table-column label="实付金额" width="120">
            <template #default="{ row }">{{ formatMoney(row.payAmount) }}</template>
          </el-table-column>
          <el-table-column label="报名时间" width="170">
            <template #default="{ row }">{{ formatDateTime(row.payTime || row.createdAt) }}</template>
          </el-table-column>
        </el-table>
        <el-empty v-if="!loadingEnrollments && !enrollments.length" description="暂无报名学生" />
      </el-tab-pane>

      <el-tab-pane label="数据统计" name="stats">
        <el-table v-loading="loadingStats" :data="stats.courses || []" border>
          <el-table-column prop="courseTitle" label="课程" min-width="180" />
          <el-table-column label="报名人数" width="110">
            <template #default="{ row }">{{ row.paidEnrollments || 0 }}</template>
          </el-table-column>
          <el-table-column label="剩余库存" width="110">
            <template #default="{ row }">{{ row.availableStock ?? 0 }}</template>
          </el-table-column>
          <el-table-column label="总库存" width="100">
            <template #default="{ row }">{{ row.stock ?? 0 }}</template>
          </el-table-column>
          <el-table-column label="销售额" width="130">
            <template #default="{ row }">{{ formatMoney(row.revenue || 0) }}</template>
          </el-table-column>
        </el-table>
        <el-empty v-if="!loadingStats && !(stats.courses || []).length" description="暂无统计数据" />
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑课程' : '新增课程'" width="720px">
      <el-form :model="form" label-width="96px">
        <el-form-item label="课程标题"><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="副标题"><el-input v-model="form.subtitle" /></el-form-item>
        <el-form-item label="课程封面">
          <div class="cover-upload">
            <el-upload
              accept="image/jpeg,image/png,image/webp"
              :show-file-list="false"
              :http-request="uploadCover"
              :before-upload="beforeCoverUpload"
            >
              <img v-if="form.coverUrl" class="cover-preview" :src="form.coverUrl" alt="课程封面" />
              <el-button v-else>选择图片</el-button>
            </el-upload>
            <el-button v-if="form.coverUrl" text type="primary" @click="form.coverUrl = ''">移除封面</el-button>
          </div>
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="form.categoryId">
            <el-option v-for="item in categories" :key="item.id" :label="item.name" :value="item.id" />
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { listCategories } from '../api/categories'
import { createTeacherCourse, deleteTeacherCourse, getTeacherCourse, listTeacherCourses, updateTeacherCourse, uploadTeacherCourseCover } from '../api/courses'
import { getTeacherStats, listTeacherEnrollments } from '../api/orders'
import { courseStatusText, courseTypeText, formatDateTime, formatMoney } from '../utils/format'

const activeTab = ref('courses')
const loadingCourses = ref(false)
const loadingEnrollments = ref(false)
const loadingStats = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const editingId = ref(null)
const courses = ref([])
const categories = ref([])
const enrollments = ref([])
const stats = ref({})
const query = reactive({ categoryId: null, keyword: '' })
const enrollmentQuery = reactive({ courseId: null })
const form = reactive({})

const emptyForm = () => ({
  categoryId: null,
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

const resetForm = (data = emptyForm()) => {
  Object.assign(form, emptyForm(), {
    categoryId: data.categoryId,
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
    status: data.status === 'ON_SALE' ? 'ON_SALE' : 'DRAFT',
    sortOrder: data.sortOrder
  })
}

const coursePayload = () => ({
  categoryId: form.categoryId,
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

const loadOptions = async () => {
  categories.value = await listCategories()
}

const loadCourses = async () => {
  loadingCourses.value = true
  try {
    courses.value = await listTeacherCourses({
      categoryId: query.categoryId || undefined,
      keyword: query.keyword || undefined
    })
  } finally {
    loadingCourses.value = false
  }
}

const loadEnrollments = async () => {
  loadingEnrollments.value = true
  try {
    enrollments.value = await listTeacherEnrollments({
      courseId: enrollmentQuery.courseId || undefined
    })
  } finally {
    loadingEnrollments.value = false
  }
}

const loadStats = async () => {
  loadingStats.value = true
  try {
    stats.value = await getTeacherStats()
  } finally {
    loadingStats.value = false
  }
}

const refreshTeacherData = async () => {
  await Promise.all([loadCourses(), loadEnrollments(), loadStats()])
}

const openCreate = () => {
  editingId.value = null
  resetForm()
  dialogVisible.value = true
}

const openEdit = async (id) => {
  editingId.value = id
  resetForm(await getTeacherCourse(id))
  dialogVisible.value = true
}

const save = async () => {
  if (!form.title || !form.categoryId) {
    ElMessage.warning('请填写课程标题并选择分类')
    return
  }
  saving.value = true
  try {
    if (editingId.value) await updateTeacherCourse(editingId.value, coursePayload())
    else await createTeacherCourse(coursePayload())
    ElMessage.success('保存成功')
    dialogVisible.value = false
    await refreshTeacherData()
  } finally {
    saving.value = false
  }
}

const beforeCoverUpload = (file) => {
  const allowed = ['image/jpeg', 'image/png', 'image/webp']
  if (!allowed.includes(file.type)) {
    ElMessage.warning('请上传 JPG、PNG 或 WebP 图片')
    return false
  }
  if (file.size > 5 * 1024 * 1024) {
    ElMessage.warning('图片大小不能超过 5MB')
    return false
  }
  return true
}

const uploadCover = async ({ file, onSuccess, onError }) => {
  try {
    const result = await uploadTeacherCourseCover(file)
    form.coverUrl = result.url
    ElMessage.success('封面上传成功')
    onSuccess(result)
  } catch (error) {
    onError(error)
  }
}

const remove = async (row) => {
  try {
    await ElMessageBox.confirm(`确认删除课程“${row.title}”吗？删除后将不再对外展示。`, '删除课程', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消'
    })
  } catch {
    return
  }
  await deleteTeacherCourse(row.id)
  ElMessage.success('删除成功')
  await refreshTeacherData()
}

onMounted(() => {
  loadOptions()
  refreshTeacherData()
})
</script>

<style scoped>
.teacher-summary {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 16px;
}

.metric {
  padding: 16px;
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 6px;
}

.metric strong {
  display: block;
  color: #1f2937;
  font-size: 24px;
  line-height: 1.2;
}

.metric span {
  display: block;
  margin-top: 6px;
  color: #6b7280;
  font-size: 13px;
}

.teacher-tabs {
  margin-top: 8px;
}

.cover-upload {
  display: flex;
  align-items: center;
  gap: 12px;
}

.cover-preview {
  width: 160px;
  height: 90px;
  display: block;
  object-fit: cover;
  border: 1px solid #dcdfe6;
  border-radius: 6px;
}

@media (max-width: 900px) {
  .teacher-summary {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 560px) {
  .teacher-summary {
    grid-template-columns: 1fr;
  }
}
</style>
