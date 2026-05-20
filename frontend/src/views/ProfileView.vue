<template>
  <section class="content-section narrow">
    <div class="section-heading">
      <div>
        <h1>我的账号</h1>
        <p>当前登录账号和基础资料。</p>
      </div>
      <el-button type="primary" @click="$router.push('/orders')">查看我的订单</el-button>
    </div>
    <el-descriptions v-if="auth.user" :column="1" border>
      <el-descriptions-item label="用户 ID">{{ auth.currentUserId || '-' }}</el-descriptions-item>
      <el-descriptions-item label="用户名">{{ auth.user.username }}</el-descriptions-item>
      <el-descriptions-item label="角色">{{ roleText[auth.user.role] || auth.user.role }}</el-descriptions-item>
      <el-descriptions-item label="昵称">{{ auth.user.user?.nickname || '-' }}</el-descriptions-item>
      <el-descriptions-item label="手机号">{{ auth.user.user?.phone || '-' }}</el-descriptions-item>
      <el-descriptions-item label="邮箱">{{ auth.user.user?.email || '-' }}</el-descriptions-item>
    </el-descriptions>
  </section>
</template>

<script setup>
import { useAuthStore } from '../stores/auth'
import { roleText } from '../utils/format'

const auth = useAuthStore()
</script>
