import { createRouter, createWebHistory } from 'vue-router';

const router = createRouter({
  history: createWebHistory(),
  routes: [
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
          path: 'todos',
          name: 'Todos',
          component: () => import('@/views/user/tools/TodoManage.vue'),
          meta: { title: '待办事项', icon: 'List' },
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
          path: 'order-sources',
          name: 'AdminOrderSources',
          component: () => import('@/views/admin/system/OrderSourceManage.vue'),
          meta: { title: '来源管理', icon: 'Coin' },
        },
        {
          path: 'payment-methods',
          name: 'AdminPaymentMethods',
          component: () => import('@/views/admin/system/PaymentMethodManage.vue'),
          meta: { title: '支付方式管理', icon: 'Coin' },
        },
        {
          path: 'package-types',
          name: 'AdminPackageTypes',
          component: () => import('@/views/admin/system/PackageTypeManage.vue'),
          meta: { title: '套餐类型管理', icon: 'Coin' },
        },
        {
          path: 'vip-levels',
          name: 'AdminVipLevels',
          component: () => import('@/views/admin/system/VipLevelManage.vue'),
          meta: { title: '优惠等级配置', icon: 'TrendCharts' },
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
  const isPublicRoute = ['Login', 'Register', 'AdminLogin'].includes(to.name as string);

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
