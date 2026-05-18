<template>
  <div class="admin-layout">
    <aside class="admin-sidebar">
      <router-link class="admin-brand" to="/admin">白马后台</router-link>
      <el-menu router :default-active="$route.path" class="admin-menu">
        <el-menu-item index="/admin">概览</el-menu-item>
        <el-menu-item index="/admin/courses">课程管理</el-menu-item>
        <el-menu-item index="/admin/categories">分类管理</el-menu-item>
        <el-menu-item index="/admin/teachers">教师管理</el-menu-item>
        <el-menu-item index="/admin/users">用户管理</el-menu-item>
        <el-menu-item index="/admin/orders">订单管理</el-menu-item>
        <el-menu-item index="/admin/knowledge">知识库管理</el-menu-item>
      </el-menu>
    </aside>
    <section class="admin-content">
      <header class="admin-header">
        <router-link to="/courses">返回用户端</router-link>
        <div class="admin-user">
          <span>{{ auth.user?.username }}</span>
          <el-button size="small" @click="auth.logout()">退出</el-button>
        </div>
      </header>
      <main class="admin-main">
        <router-view />
      </main>
    </section>
  </div>
</template>

<script setup>
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
</script>
