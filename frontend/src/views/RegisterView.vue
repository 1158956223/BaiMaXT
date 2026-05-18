<template>
  <main class="auth-page">
    <section class="auth-panel wide">
      <div class="auth-copy">
        <router-link class="brand" to="/courses">白马学堂</router-link>
        <h1>创建账号</h1>
        <p>填写基础资料后即可浏览课程和进入对应工作台。</p>
      </div>
      <el-form class="auth-form" :model="form" label-position="top" @submit.prevent>
        <el-form-item label="用户名"><el-input v-model="form.username" /></el-form-item>
        <el-form-item label="密码"><el-input v-model="form.password" type="password" show-password /></el-form-item>
        <el-form-item label="昵称"><el-input v-model="form.nickname" /></el-form-item>
        <el-form-item label="手机号"><el-input v-model="form.phone" /></el-form-item>
        <el-form-item label="邮箱"><el-input v-model="form.email" /></el-form-item>
        <el-form-item label="角色">
          <el-select v-model="form.role">
            <el-option label="学员" value="STUDENT" />
            <el-option label="教师" value="TEACHER" />
          </el-select>
        </el-form-item>
        <el-button type="primary" size="large" :loading="loading" @click="submit">注册</el-button>
        <p class="auth-switch">已有账号？<router-link to="/login">去登录</router-link></p>
      </el-form>
    </section>
  </main>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const auth = useAuthStore()
const loading = ref(false)
const form = reactive({
  username: '',
  password: '',
  nickname: '',
  phone: '',
  email: '',
  role: 'STUDENT'
})

const submit = async () => {
  if (!form.username || !form.password || !form.nickname) {
    ElMessage.warning('请填写用户名、密码和昵称')
    return
  }
  loading.value = true
  try {
    await auth.register(form)
    ElMessage.success('注册成功')
    router.push('/courses')
  } finally {
    loading.value = false
  }
}
</script>
