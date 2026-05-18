<template>
  <section>
    <div class="section-heading">
      <div>
        <h1>用户管理</h1>
        <p>查看和维护平台用户资料。</p>
      </div>
    </div>
    <el-table v-loading="loading" :data="users" border>
      <el-table-column prop="username" label="用户名" width="140" />
      <el-table-column prop="nickname" label="昵称" width="140" />
      <el-table-column label="角色" width="110"><template #default="{ row }">{{ roleText[row.role] || row.role }}</template></el-table-column>
      <el-table-column prop="phone" label="手机号" width="150" />
      <el-table-column prop="email" label="邮箱" min-width="180" />
      <el-table-column label="状态" width="100"><template #default="{ row }">{{ userStatusText[row.status] || row.status }}</template></el-table-column>
      <el-table-column label="操作" width="220">
        <template #default="{ row }">
          <el-button size="small" @click="openEdit(row)">编辑</el-button>
          <el-button v-if="row.status === 'DISABLED'" size="small" type="success" @click="toggle(row, true)">启用</el-button>
          <el-button v-else size="small" type="warning" @click="toggle(row, false)">禁用</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-dialog v-model="dialogVisible" title="编辑用户" width="520px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="昵称"><el-input v-model="form.nickname" /></el-form-item>
        <el-form-item label="手机号"><el-input v-model="form.phone" /></el-form-item>
        <el-form-item label="邮箱"><el-input v-model="form.email" /></el-form-item>
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
import { disableUser, enableUser, listUsers, updateUser } from '../../api/users'
import { roleText, userStatusText } from '../../utils/format'

const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const editingId = ref(null)
const users = ref([])
const form = reactive({ nickname: '', phone: '', email: '' })

const load = async () => {
  loading.value = true
  try {
    users.value = await listUsers()
  } finally {
    loading.value = false
  }
}
const openEdit = (row) => {
  editingId.value = row.id
  Object.assign(form, { nickname: row.nickname, phone: row.phone, email: row.email })
  dialogVisible.value = true
}
const save = async () => {
  saving.value = true
  try {
    await updateUser(editingId.value, form)
    ElMessage.success('保存成功')
    dialogVisible.value = false
    load()
  } finally {
    saving.value = false
  }
}
const toggle = async (row, enable) => {
  if (enable) await enableUser(row.id)
  else await disableUser(row.id)
  ElMessage.success('状态已更新')
  load()
}

onMounted(load)
</script>
