import { createRouter, createWebHashHistory } from 'vue-router'
import { getUserInfo } from '@/utils/auth'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/LoginView.vue'),
    meta: { noAuth: true }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/RegisterView.vue'),
    meta: { noAuth: true }
  },
  {
    path: '/forgot-password',
    name: 'ForgotPassword',
    component: () => import('../views/ForgotPasswordView.vue'),
    meta: { noAuth: true }
  },
  {
    path: '/public-checkout',
    name: 'PublicCheckout',
    component: () => import('../views/PublicCheckout.vue'),
    meta: { noAuth: true }
  },
  {
    path: '/admin/data-init',
    name: 'AdminDataInit',
    component: () => import('../views/AdminDataInit.vue'),
    meta: { noAuth: true }
  },
  {
    path: '/',
    component: () => import('../views/Index.vue'),
    redirect: '/Welcome',
    children: [
      {
        path: 'Welcome',
        name: 'Welcome',
        component: () => import('../views/Welcome.vue')
      },
      {
        path: 'ParcelTrack',
        name: 'ParcelTrack',
        component: () => import('../views/ParcelTrack.vue'),
        meta: { role: 'REGULAR' }
      },
      {
        path: 'IdentityCode',
        name: 'IdentityCode',
        component: () => import('../views/IdentityCode.vue'),
        meta: { role: 'REGULAR' }
      },
      {
        path: 'ParcelSend',
        name: 'ParcelSend',
        component: () => import('../views/ParcelSend.vue'),
        meta: { role: 'REGULAR' }
      },
      {
        path: 'MyUnpaid',
        name: 'MyUnpaid',
        component: () => import('../views/MyUnpaid.vue'),
        meta: { role: 'REGULAR' }
      },
      {
        path: 'ErrorRegister',
        name: 'ErrorRegister',
        component: () => import('../views/ErrorRegister.vue'),
        meta: { role: 'REGULAR' }
      },
      {
        path: 'MyErrors',
        name: 'MyErrors',
        component: () => import('../views/MyErrors.vue'),
        meta: { role: 'REGULAR' }
      },
      {
        path: 'ParcelSearch',
        name: 'ParcelSearch',
        component: () => import('../views/ParcelSearch.vue'),
        meta: { role: 'MANAGER' }
      },
      {
        path: 'ErrorSearch',
        name: 'ErrorSearch',
        component: () => import('../views/ErrorSearch.vue'),
        meta: { role: 'MANAGER' }
      },
      {
        path: 'Inventory',
        name: 'Inventory',
        component: () => import('../views/Inventory.vue'),
        meta: { role: 'MANAGER' }
      },
      {
        path: 'SendManage',
        name: 'SendManage',
        component: () => import('../views/SendManage.vue'),
        meta: { role: 'MANAGER' }
      },
      {
        path: 'ErrorHandle',
        name: 'ErrorHandle',
        component: () => import('../views/ErrorHandle.vue'),
        meta: { role: 'MANAGER' }
      },
      {
        path: 'AutoCheckout',
        name: 'AutoCheckout',
        component: () => import('../views/AutoCheckout.vue'),
        meta: { role: 'MANAGER' }
      },
      {
        path: 'PickupQuery',
        name: 'PickupQuery',
        component: () => import('../views/PickupQuery.vue'),
        meta: { role: 'COURIER' }
      },
      {
        path: 'ParcelDelivery',
        name: 'ParcelDelivery',
        component: () => import('../views/ParcelDelivery.vue'),
        meta: { role: 'COURIER' }
      },
      {
        path: 'SendDetail',
        name: 'SendDetail',
        component: () => import('../views/SendDetail.vue')
      },
      {
        path: 'Settings',
        name: 'Settings',
        component: () => import('../views/Settings.vue')
      },
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/Welcome'
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const user = getUserInfo()

  if (to.meta.noAuth) {
    if (user && to.path === '/login') {
      next('/Welcome')//已经登录，不允许访问登录页，重定向到欢迎页
    } else {
      next()//放行
    }
    return
  }

  if (!user) {
    next('/login')//未登录，重定向到登录页
    return
  }

  if (to.meta.role && user.role !== to.meta.role) {
    next('/Welcome')//直接在地址栏输地址但是角色权限不够，重定向到欢迎页
    return
  }

  next()//其余的都放行
})

router.afterEach(() => {
  window.scrollTo(0, 0)
})

export default router
