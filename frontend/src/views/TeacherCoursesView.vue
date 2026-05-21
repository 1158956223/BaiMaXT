<template>
  <section>
    <div class="section-heading">
      <div>
        <h1>我的课程</h1>
        <p>维护自己创建的课程，可保存草稿、上架展示或删除隐藏。</p>
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
      <el-table-column label="类型" width="100">
        <template #default="{ row }">{{ courseTypeText[row.courseType] || row.courseType }}</template>
      </el-table-column>
      <el-table-column label="价格" width="120">
        <template #default="{ row }">{{ formatMoney(row.price) }}</template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">{{ courseStatusText[row.status] || row.status }}</template>
      </el-table-column>
      <el-table-column label="创建时间" width="170">
        <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
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
    <el-empty v-if="!loading && !courses.length" description="暂无课程" />

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
import { courseStatusText, courseTypeText, formatDateTime, formatMoney } from '../utils/format'

const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const editingId = ref(null)
const courses = ref([])
const categories = ref([])
const query = reactive({ categoryId: null, keyword: '' })
const form = reactive({})

const emptyForm = () => ({
  categoryId: null,
  title: '',
  subtitle: '',
  coverUrl: '',
  price: 0,
  originalPrice: 0,
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

const load = async () => {
  loading.value = true
  try {
    courses.value = await listTeacherCourses({
      categoryId: query.categoryId || undefined,
      keyword: query.keyword || undefined
    })
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
    load()
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
  load()
}

onMounted(() => {
  loadOptions()
  load()
})
</script>

<style scoped>
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
</style>
