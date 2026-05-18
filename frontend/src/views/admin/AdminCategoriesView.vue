<template>
  <section>
    <div class="section-heading">
      <div>
        <h1>分类管理</h1>
        <p>维护课程分类和展示顺序。</p>
      </div>
      <el-button type="primary" @click="openCreate">新增分类</el-button>
    </div>
    <el-table v-loading="loading" :data="categories" border>
      <el-table-column prop="name" label="分类名称" />
      <el-table-column prop="parentId" label="父级 ID" width="120" />
      <el-table-column prop="sortOrder" label="排序" width="100" />
      <el-table-column label="状态" width="100"><template #default="{ row }">{{ enabledText(row.status) }}</template></el-table-column>
      <el-table-column label="操作" width="220">
        <template #default="{ row }">
          <el-button size="small" @click="openEdit(row)">编辑</el-button>
          <el-button v-if="!enabledValue(row.status)" size="small" type="success" @click="toggle(row, true)">启用</el-button>
          <el-button v-else size="small" type="warning" @click="toggle(row, false)">禁用</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑分类' : '新增分类'" width="480px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="父级 ID"><el-input-number v-model="form.parentId" :min="0" /></el-form-item>
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
import { createCategory, disableCategory, enableCategory, listAdminCategories, updateCategory } from '../../api/categories'
import { enabledText, enabledValue } from '../../utils/format'

const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const editingId = ref(null)
const categories = ref([])
const form = reactive({ parentId: null, name: '', sortOrder: 0 })

const load = async () => {
  loading.value = true
  try {
    categories.value = await listAdminCategories()
  } finally {
    loading.value = false
  }
}
const openCreate = () => {
  editingId.value = null
  Object.assign(form, { parentId: null, name: '', sortOrder: 0 })
  dialogVisible.value = true
}
const openEdit = (row) => {
  editingId.value = row.id
  Object.assign(form, { parentId: row.parentId, name: row.name, sortOrder: row.sortOrder })
  dialogVisible.value = true
}
const save = async () => {
  saving.value = true
  try {
    if (editingId.value) await updateCategory(editingId.value, form)
    else await createCategory(form)
    ElMessage.success('保存成功')
    dialogVisible.value = false
    load()
  } finally {
    saving.value = false
  }
}
const toggle = async (row, enable) => {
  if (enable) await enableCategory(row.id)
  else await disableCategory(row.id)
  ElMessage.success('状态已更新')
  load()
}

onMounted(load)
</script>
