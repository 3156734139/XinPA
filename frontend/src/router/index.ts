import { createRouter, createWebHistory } from 'vue-router';

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/showcase',
      name: 'Showcase',
      component: () => import('@/views/showcase/ShowcasePage.vue'),
      meta: { title: '小兔酱 · 陪玩主页' },
    },
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/user/Login.vue'),
      meta: { title: '用户登录' },
    },
    {
      path: '/register',
      name: 'Register',
      component: () => import('@/views/user/Register.vue'),
      meta: { title: '用户注册' },
    },
    {
      path: '/admin/login',
      name: 'AdminLogin',
      component: () => import('@/views/admin/Login.vue'),
      meta: { title: '管理员登录' },
    },
    {
      path: '/',
      component: () => import('@/layouts/UserLayout.vue'),
      redirect: '/dashboard',
      children: [
        {
          path: 'dashboard',
          name: 'Dashboard',
          component: () => import('@/views/user/Dashboard.vue'),
          meta: { title: '工作台', icon: 'Monitor' },
        },
        {
          path: 'settings',
          name: 'Settings',
          component: () => import('@/views/user/settings/PersonalCenter.vue'),
          meta: { title: '个人中心', icon: 'Setting' },
        },
        {
          path: 'profile',
          name: 'Profile',
          component: () => import('@/views/user/profile/ProfilePage.vue'),
          meta: { title: '人设主页', icon: 'User' },
        },
        {
          path: 'packages',
          name: 'Packages',
          component: () => import('@/views/user/packages/PackageList.vue'),
          meta: { title: '价目套餐', icon: 'PriceTag' },
        },
        {
          path: 'materials',
          name: 'Materials',
          component: () => import('@/views/user/materials/MaterialList.vue'),
          meta: { title: '素材库', icon: 'Folder' },
        },
        {
          path: 'orders',
          name: 'Orders',
          component: () => import('@/views/user/orders/OrderList.vue'),
          meta: { title: '订单管理', icon: 'List' },
        },
        {
          path: 'orders/:id',
          name: 'OrderDetail',
          component: () => import('@/views/user/orders/OrderDetail.vue'),
          meta: { title: '订单详情' },
        },
        {
          path: 'appointments',
          name: 'Appointments',
          component: () => import('@/views/user/orders/AppointmentCalendar.vue'),
          meta: { title: '预约日历' },
        },
        {
          path: 'customers',
          name: 'Customers',
          component: () => import('@/views/user/customers/CustomerList.vue'),
          meta: { title: '客户管理', icon: 'UserFilled' },
        },
        {
          path: 'customers/:id',
          name: 'CustomerDetail',
          component: () => import('@/views/user/customers/CustomerDetail.vue'),
          meta: { title: '客户详情' },
        },
        {
          path: 'finance',
          name: 'Finance',
          component: () => import('@/views/user/finance/FinancePage.vue'),
          meta: { title: '财务记账', icon: 'Money' },
        },
        {
          path: 'ai',
          name: 'AI',
          component: () => import('@/views/user/ai/AiTools.vue'),
          meta: { title: 'AI辅助', icon: 'MagicStick' },
        },
      ],
    },
    {
      path: '/admin',
      component: () => import('@/layouts/AdminLayout.vue'),
      redirect: '/admin/dashboard',
      children: [
        {
          path: 'dashboard',
          name: 'AdminDashboard',
          component: () => import('@/views/admin/dashboard/Overview.vue'),
          meta: { title: '平台总览', icon: 'DataAnalysis' },
        },
        {
          path: 'stats',
          name: 'AdminStats',
          component: () => import('@/views/admin/dashboard/StatsDetail.vue'),
          meta: { title: '数据统计', icon: 'DataLine' },
        },
        {
          path: 'admins',
          name: 'AdminManage',
          component: () => import('@/views/admin/system/AdminManage.vue'),
          meta: { title: '管理员管理', icon: 'Setting' },
        },
        {
          path: 'ai-logs',
          name: 'AdminAiLogs',
          component: () => import('@/views/admin/logs/AiLogs.vue'),
          meta: { title: 'AI调用日志', icon: 'Document' },
        },
        {
          path: 'announcements',
          name: 'AdminAnnouncements',
          component: () => import('@/views/admin/system/AnnouncementManage.vue'),
          meta: { title: '公告管理', icon: 'Bell' },
        },
      ],
    },
  ],
});

// 路由守卫
router.beforeEach((to, from, next) => {
  document.title = `${to.meta.title} - 陪玩星助手`;

  const isAdminRoute = to.path.startsWith('/admin') && to.path !== '/admin/login';
  const isUserRoute = !to.path.startsWith('/admin');
  const isPublicRoute = ['Showcase', 'Login', 'Register', 'AdminLogin'].includes(to.name as string);

  if (isPublicRoute) {
    next();
    return;
  }

  const token = localStorage.getItem('token');
  if (!token) {
    if (isAdminRoute) {
      next('/admin/login');
    } else {
      next('/login');
    }
    return;
  }

  // 管理员只能访问管理员页面
  const userType = localStorage.getItem('userType');
  if (isAdminRoute && userType !== 'ADMIN') {
    next('/login');
    return;
  }
  if (!isAdminRoute && userType === 'ADMIN') {
    next('/admin/dashboard');
    return;
  }

  next();
});

export default router;
