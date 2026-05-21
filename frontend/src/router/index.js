import { createRouter, createWebHistory } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../stores/auth'
import PublicLayout from '../layouts/PublicLayout.vue'
import AdminLayout from '../layouts/AdminLayout.vue'
import LoginView from '../views/LoginView.vue'
import RegisterView from '../views/RegisterView.vue'
import CourseListView from '../views/CourseListView.vue'
import CourseDetailView from '../views/CourseDetailView.vue'
import TeacherCoursesView from '../views/TeacherCoursesView.vue'
import ProfileView from '../views/ProfileView.vue'
import OrderListView from '../views/OrderListView.vue'
import OrderDetailView from '../views/OrderDetailView.vue'
import PaymentView from '../views/PaymentView.vue'
import AiView from '../views/AiView.vue'
import AdminDashboardView from '../views/admin/AdminDashboardView.vue'
import AdminCoursesView from '../views/admin/AdminCoursesView.vue'
import AdminCategoriesView from '../views/admin/AdminCategoriesView.vue'
import AdminTeachersView from '../views/admin/AdminTeachersView.vue'
import AdminUsersView from '../views/admin/AdminUsersView.vue'
import AdminOrdersView from '../views/admin/AdminOrdersView.vue'
import AdminPlaceholderView from '../views/admin/AdminPlaceholderView.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', redirect: '/courses' },
    { path: '/login', component: LoginView, meta: { guest: true } },
    { path: '/register', component: RegisterView, meta: { guest: true } },
    {
      path: '/',
      component: PublicLayout,
      meta: { requiresAuth: true },
      children: [
        { path: 'courses', component: CourseListView },
        { path: 'courses/:id', component: CourseDetailView },
        { path: 'teacher/courses', component: TeacherCoursesView, meta: { requiresAuth: true, requiresTeacher: true } },
        { path: 'profile', component: ProfileView, meta: { requiresAuth: true } },
        { path: 'orders', component: OrderListView, meta: { requiresAuth: true } },
        { path: 'orders/:id', component: OrderDetailView, meta: { requiresAuth: true } },
        { path: 'payments/:payNo', component: PaymentView, meta: { requiresAuth: true } },
        { path: 'ai', component: AiView }
      ]
    },
    {
      path: '/admin',
      component: AdminLayout,
      meta: { requiresAuth: true, requiresAdmin: true },
      children: [
        { path: '', component: AdminDashboardView },
        { path: 'courses', component: AdminCoursesView },
        { path: 'categories', component: AdminCategoriesView },
        { path: 'teachers', component: AdminTeachersView },
        { path: 'users', component: AdminUsersView },
        { path: 'orders', component: AdminOrdersView },
        {
          path: 'knowledge',
          component: AdminPlaceholderView,
          props: {
            title: '知识库管理',
            description: '知识库服务暂未接入，后续可在这里维护 FAQ、课程答疑和客服知识材料。'
          }
        }
      ]
    }
  ]
})

router.beforeEach(async (to) => {
  const auth = useAuthStore()
  if (auth.token && !auth.loaded) {
    await auth.fetchMe().catch(() => auth.logout(false))
  }

  if (to.meta.requiresAuth && !auth.token) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }

  if (to.meta.requiresAdmin && auth.user?.role !== 'ADMIN') {
    ElMessage.warning('需要管理员权限')
    return '/courses'
  }

  if (to.meta.requiresTeacher && auth.user?.role !== 'TEACHER') {
    ElMessage.warning('需要教师权限')
    return '/courses'
  }

  if (to.meta.guest && auth.token) {
    return '/courses'
  }
})

export default router
