<template>
  <main class="auth-page">
    <section class="auth-panel wide">
      <div class="auth-copy">
        <router-link class="brand" to="/courses">白马学堂</router-link>
        <h1>创建账号</h1>
        <p>{{ selectedIdentity.description }}</p>
      </div>

      <div class="auth-form">
        <div v-if="step === 'identity'" class="identity-picker">
          <button
            v-for="item in identityOptions"
            :key="item.role"
            class="identity-option"
            :class="{ active: form.role === item.role }"
            type="button"
            @click="selectIdentity(item.role)"
          >
            <span>{{ item.label }}</span>
            <strong>{{ item.title }}</strong>
            <small>{{ item.hint }}</small>
          </button>
        </div>

        <el-form v-else :model="form" label-position="top" @submit.prevent>
          <div class="form-section-title">{{ selectedIdentity.formTitle }}</div>
          <div class="form-grid">
            <el-form-item label="用户名">
              <el-input v-model="form.username" autocomplete="username" />
            </el-form-item>
            <el-form-item label="密码">
              <el-input v-model="form.password" type="password" show-password autocomplete="new-password" />
            </el-form-item>
            <el-form-item label="昵称">
              <el-input v-model="form.nickname" />
            </el-form-item>
            <el-form-item label="手机号">
              <el-input v-model="form.phone" />
            </el-form-item>
            <el-form-item label="邮箱">
              <el-input v-model="form.email" />
            </el-form-item>
            <el-form-item v-if="form.role === 'STUDENT'" label="学习目标">
              <el-input v-model="studentProfile.goal" placeholder="例如：升学备考、职业提升" />
            </el-form-item>
            <el-form-item v-if="form.role === 'TEACHER'" label="职称">
              <el-input v-model="teacherProfile.title" placeholder="例如：高级讲师、金牌导师" />
            </el-form-item>
            <el-form-item v-if="form.role === 'TEACHER'" label="教龄">
              <el-input-number v-model="teacherProfile.yearsExperience" :min="0" :max="60" />
            </el-form-item>
          </div>

          <el-form-item v-if="form.role === 'TEACHER'" label="擅长领域">
            <el-input v-model="teacherProfile.specialties" placeholder="例如：数学竞赛、Java 后端、考研英语" />
          </el-form-item>
          <el-form-item v-if="form.role === 'TEACHER'" label="教师简介">
            <el-input v-model="teacherProfile.bio" type="textarea" :rows="3" />
          </el-form-item>
          <div class="form-actions">
            <el-button size="large" @click="step = 'identity'">上一步</el-button>
            <el-button type="primary" size="large" :loading="loading" @click="submit">注册</el-button>
          </div>
          <p class="auth-switch">已有账号？<router-link to="/login">去登录</router-link></p>
        </el-form>
      </div>
    </section>
  </main>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const auth = useAuthStore()
const loading = ref(false)
const step = ref('identity')

const identityOptions = [
  {
    role: 'STUDENT',
    label: '学生注册',
    title: '我要报名学习',
    hint: '填写联系方式和学习目标，注册后即可浏览课程并提交报课订单。',
    description: '先选择你的身份，再填写对应资料。',
    formTitle: '学生基础资料'
  },
  {
    role: 'TEACHER',
    label: '教师注册',
    title: '我要成为教师',
    hint: '填写联系方式、职称、教龄和擅长领域，便于后续完善教师资料。',
    description: '教师账号会生成对应教师资料，后续可由后台继续维护。',
    formTitle: '教师基础资料'
  }
]

const form = reactive({
  username: '',
  password: '',
  nickname: '',
  phone: '',
  email: '',
  role: 'STUDENT'
})

const studentProfile = reactive({
  goal: ''
})

const teacherProfile = reactive({
  title: '',
  yearsExperience: 0,
  specialties: '',
  bio: ''
})

const selectedIdentity = computed(() => identityOptions.find((item) => item.role === form.role) || identityOptions[0])

const selectIdentity = (role) => {
  form.role = role
  step.value = 'form'
}

const submit = async () => {
  if (!form.username || !form.password || !form.nickname) {
    ElMessage.warning('请填写用户名、密码和昵称')
    return
  }
  if (form.role === 'TEACHER' && !teacherProfile.specialties) {
    ElMessage.warning('请填写教师擅长领域')
    return
  }

  loading.value = true
  try {
    await auth.register({
      username: form.username,
      password: form.password,
      nickname: form.nickname,
      phone: form.phone,
      email: form.email,
      role: form.role,
      teacherTitle: form.role === 'TEACHER' ? teacherProfile.title : undefined,
      teacherBio: form.role === 'TEACHER' ? teacherProfile.bio : undefined,
      teacherSpecialties: form.role === 'TEACHER' ? teacherProfile.specialties : undefined,
      teacherYearsExperience: form.role === 'TEACHER' ? teacherProfile.yearsExperience : undefined
    })
    ElMessage.success('注册成功')
    router.push('/courses')
  } finally {
    loading.value = false
  }
}
</script>
