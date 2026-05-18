<template>
  <div class="page-shell">
    <header class="topbar">
      <router-link class="brand" to="/courses">白马学堂</router-link>
      <nav class="nav-links">
        <router-link to="/courses">课程</router-link>
        <router-link to="/ai">AI 客服</router-link>
        <router-link v-if="auth.user?.role === 'ADMIN'" to="/admin">后台</router-link>
      </nav>
      <div class="topbar-actions">
        <template v-if="auth.token">
          <router-link to="/profile">{{ auth.user?.user?.nickname || auth.user?.username || '我的' }}</router-link>
          <el-button size="small" @click="auth.logout()">退出</el-button>
        </template>
        <template v-else>
          <router-link to="/login">登录</router-link>
          <el-button size="small" type="primary" @click="$router.push('/register')">注册</el-button>
        </template>
      </div>
    </header>
    <main class="public-main">
      <router-view />
    </main>
  </div>
</template>

<script setup>
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
</script>
