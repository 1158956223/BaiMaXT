<template>
  <section>
    <div class="section-heading">
      <div>
        <h1>教师管理</h1>
        <p>维护教师资料、头像、职称和擅长方向。</p>
      </div>
      <el-button type="primary" @click="openCreate">新增教师</el-button>
    </div>
    <el-table v-loading="loading" :data="teachers" border>
      <el-table-column prop="name" label="姓名" width="130" />
      <el-table-column prop="title" label="职称" width="160" />
      <el-table-column prop="specialties" label="擅长方向" min-width="180" />
      <el-table-column prop="yearsExperience" label="经验" width="100" />
      <el-table-column label="状态" width="100"><template #default="{ row }">{{ enabledText(row.status) }}</template></el-table-column>
      <el-table-column label="操作" width="220">
        <template #default="{ row }">
          <el-button size="small" @click="openEdit(row)">编辑</el-button>
          <el-button v-if="!enabledValue(row.status)" size="small" type="success" @click="toggle(row, true)">启用</el-button>
          <el-button v-else size="small" type="warning" @click="toggle(row, false)">禁用</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑教师' : '新增教师'" width="620px">
      <el-form :model="form" label-width="96px">
        <el-form-item label="姓名"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="头像地址"><el-input v-model="form.avatarUrl" /></el-form-item>
        <el-form-item label="职称"><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="擅长方向"><el-input v-model="form.specialties" /></el-form-item>
        <el-form-item label="从业年限"><el-input-number v-model="form.yearsExperience" :min="0" /></el-form-item>
        <el-form-item label="简介"><el-input v-model="form.bio" type="textarea" :rows="4" /></el-form-item>
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
import { createTeacher, disableTeacher, enableTeacher, listAdminTeachers, updateTeacher } from '../../api/teachers'
import { enabledText, enabledValue } from '../../utils/format'

const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const editingId = ref(null)
const teachers = ref([])
const form = reactive({ name: '', avatarUrl: '', title: '', bio: '', specialties: '', yearsExperience: 0 })

const load = async () => {
  loading.value = true
  try {
    teachers.value = await listAdminTeachers()
  } finally {
    loading.value = false
  }
}
const openCreate = () => {
  editingId.value = null
  Object.assign(form, { name: '', avatarUrl: '', title: '', bio: '', specialties: '', yearsExperience: 0 })
  dialogVisible.value = true
}
const openEdit = (row) => {
  editingId.value = row.id
  Object.assign(form, {
    name: row.name,
    avatarUrl: row.avatarUrl,
    title: row.title,
    bio: row.bio,
    specialties: row.specialties,
    yearsExperience: row.yearsExperience
  })
  dialogVisible.value = true
}
const save = async () => {
  saving.value = true
  try {
    if (editingId.value) await updateTeacher(editingId.value, form)
    else await createTeacher(form)
    ElMessage.success('保存成功')
    dialogVisible.value = false
    load()
  } finally {
    saving.value = false
  }
}
const toggle = async (row, enable) => {
  if (enable) await enableTeacher(row.id)
  else await disableTeacher(row.id)
  ElMessage.success('状态已更新')
  load()
}

onMounted(load)
</script>
