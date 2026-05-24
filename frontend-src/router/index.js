import { createRouter, createWebHashHistory } from 'vue-router'
import { getToken, getUserInfo } from '@/utils/auth'

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
        path: 'ParcelPickup',
        name: 'ParcelPickup',
        component: () => import('../views/ParcelPickup.vue'),
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
      }
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
  const token = getToken()

  if (to.meta.noAuth) {
    if (token && to.path === '/login') {
      next('/Welcome')
    } else {
      next()
    }
    return
  }

  if (!token) {
    next('/login')
    return
  }

  const user = getUserInfo()
  if (to.meta.role && user && user.role !== to.meta.role) {
    next('/Welcome')
    return
  }

  next()
})

export default router
