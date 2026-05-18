<template>
  <main class="auth-page">
    <section class="auth-panel">
      <div class="auth-copy">
        <router-link class="brand" to="/courses">白马学堂</router-link>
        <h1>欢迎回来</h1>
        <p>登录后继续管理课程、教师和学员信息。</p>
      </div>
      <el-form class="auth-form" :model="form" label-position="top" @submit.prevent>
        <el-form-item label="用户名">
          <el-input v-model="form.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" show-password placeholder="请输入密码" />
        </el-form-item>
        <el-button type="primary" size="large" :loading="loading" @click="submit">登录</el-button>
        <p class="auth-switch">还没有账号？<router-link to="/register">立即注册</router-link></p>
      </el-form>
    </section>
  </main>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../stores/auth'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const loading = ref(false)
const form = reactive({ username: '', password: '' })

const submit = async () => {
  if (!form.username || !form.password) {
    ElMessage.warning('请输入用户名和密码')
    return
  }
  loading.value = true
  try {
    await auth.login(form)
    ElMessage.success('登录成功')
    router.push(route.query.redirect || '/courses')
  } finally {
    loading.value = false
  }
}
</script>
