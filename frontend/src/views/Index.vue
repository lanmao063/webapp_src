<template>
  <div class="layout-container">
    <el-container class="layout-shell">
      <el-header class="layout-header">
        <div class="header-left">
          <el-icon class="collapse-btn" :size="22" @click="toggleCollapse">
            <Fold v-if="!isCollapse" />
            <Expand v-else />
          </el-icon>
          <img src="@/assets/logo.png" alt="logo" class="header-logo-img" />
          <h1 class="header-title">莱鸟</h1>
        </div>
        <div class="header-right">
          <el-avatar :src="avatarSrc" :size="32" class="header-avatar" @click="goSettings">
            {{ avatarSrc ? '' : username.charAt(0) }}
          </el-avatar>
          <span class="header-user">当前用户：{{ username }}（{{ roleLabel }}）</span>
          <el-button type="danger" size="small" @click="logout">退出系统</el-button>
        </div>
      </el-header>
      <el-container class="layout-body">
        <el-aside :width="isMobile ? '0px' : (isCollapse ? '64px' : '220px')" class="layout-aside">
          <el-menu
            :collapse="isCollapse"
            :default-active="route.path"
            class="sidebar-menu"
            router
          >
            <!-- REGULAR -->
            <template v-if="role==='REGULAR'">
              <el-sub-menu index="parcel-service">
                <template #title><el-icon><Van /></el-icon><span>快递服务</span></template>
                <el-menu-item index="/Welcome"><el-icon><HomeFilled /></el-icon><span>首页</span></el-menu-item>
                <el-menu-item index="/ParcelTrack"><el-icon><Search /></el-icon><span>查件</span></el-menu-item>
                <el-menu-item index="/IdentityCode"><el-icon><Postcard /></el-icon><span>身份码</span></el-menu-item>
                <el-menu-item index="/ParcelSend"><el-icon><Promotion /></el-icon><span>寄件</span></el-menu-item>
              </el-sub-menu>
              <el-sub-menu index="order-mgmt">
                <template #title><el-icon><Document /></el-icon><span>订单管理</span></template>
                <el-menu-item index="/MyUnpaid"><el-icon><Money /></el-icon><span>待付款订单</span></el-menu-item>
              </el-sub-menu>
              <el-sub-menu index="error-mgmt-user">
                <template #title><el-icon><Warning /></el-icon><span>异常管理</span></template>
                <el-menu-item index="/ErrorRegister"><el-icon><CirclePlus /></el-icon><span>异常登记</span></el-menu-item>
                <el-menu-item index="/MyErrors"><el-icon><Document /></el-icon><span>我的异常记录</span></el-menu-item>
              </el-sub-menu>
            </template>

            <!-- MANAGER -->
            <template v-if="role==='MANAGER'">
              <el-sub-menu index="parcel-mgmt">
                <template #title><el-icon><Box /></el-icon><span>包裹管理</span></template>
                <el-menu-item index="/Welcome"><el-icon><HomeFilled /></el-icon><span>首页</span></el-menu-item>
                <el-menu-item index="/ParcelSearch"><el-icon><Search /></el-icon><span>包裹查询</span></el-menu-item>
                <el-menu-item index="/Inventory"><el-icon><DataAnalysis /></el-icon><span>库存盘点</span></el-menu-item>
                <el-menu-item index="/AutoCheckout"><el-icon><Clock /></el-icon><span>自动出库记录</span></el-menu-item>
              </el-sub-menu>
              <el-sub-menu index="approval-mgmt">
                <template #title><el-icon><Checked /></el-icon><span>审批管理</span></template>
                <el-menu-item index="/SendManage"><el-icon><List /></el-icon><span>寄件审批</span></el-menu-item>
              </el-sub-menu>
              <el-sub-menu index="error-mgmt-manager">
                <template #title><el-icon><Warning /></el-icon><span>异常管理</span></template>
                <el-menu-item index="/ErrorSearch"><el-icon><Search /></el-icon><span>异常查询</span></el-menu-item>
                <el-menu-item index="/ErrorHandle"><el-icon><EditPen /></el-icon><span>异常处理</span></el-menu-item>
              </el-sub-menu>
            </template>

            <!-- COURIER -->
            <template v-if="role==='COURIER'">
              <el-sub-menu index="courier-home">
                <template #title><el-icon><HomeFilled /></el-icon><span>首页</span></template>
                <el-menu-item index="/Welcome"><el-icon><DataBoard /></el-icon><span>工作台</span></el-menu-item>
              </el-sub-menu>
              <el-sub-menu index="task-mgmt">
                <template #title><el-icon><List /></el-icon><span>任务管理</span></template>
                <el-menu-item index="/PickupQuery"><el-icon><Van /></el-icon><span>揽收</span></el-menu-item>
                <el-menu-item index="/ParcelDelivery"><el-icon><Box /></el-icon><span>快递入库</span></el-menu-item>
              </el-sub-menu>
            </template>

            <el-menu-item index="/Settings" class="menu-settings">
              <el-icon><Setting /></el-icon><span>设置</span>
            </el-menu-item>
          </el-menu>
        </el-aside>
        <el-main class="layout-main">
          <div class="main-bg" :style="{ backgroundImage: `url(${bgImage})` }"></div>
          <div class="main-content">
            <router-view v-slot="{ Component, route: r }">
              <transition name="page-fade" mode="out-in">
                <component :is="Component" :key="r.path" />
              </transition>
            </router-view>
          </div>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { getUserInfo, clearAuth } from '@/utils/auth'
import request from '@/utils/request'
import { getAvatarUrl } from '@/utils/avatar'
import { getRandomBackground } from '@/utils/background'
import {
  HomeFilled, Van, Search, Postcard, Promotion, Document,
  Money, Warning, CirclePlus, Box, DataAnalysis, Checked, List,
  DataBoard, EditPen, Clock, Setting, Fold, Expand
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const user = getUserInfo()
const username = ref(user?.username || '')
const role = ref(user?.role || '')
const avatarSrc = ref('')

const roleLabelMap = { REGULAR: '普通用户', COURIER: '快递员', MANAGER: '驿站管理员' }
const roleLabel = ref(roleLabelMap[role.value] || '')

const isCollapse = ref(false)
const toggleCollapse = () => { isCollapse.value = !isCollapse.value }

const bgImage = getRandomBackground()

const isMobile = ref(false)
let resizeHandler = null

const fetchAvatar = async () => {
  try {
    const res = await request.get('/auth/current')
    if (res.data.avatar) {
      const url = getAvatarUrl(res.data.avatar)
      avatarSrc.value = url || res.data.avatar
    }
  } catch {}
}

onMounted(() => {
  fetchAvatar()
  window.addEventListener('user-profile-updated', fetchAvatar)
  resizeHandler = () => { isMobile.value = window.innerWidth < 768 }
  resizeHandler()
  window.addEventListener('resize', resizeHandler)
})

onUnmounted(() => {
  window.removeEventListener('user-profile-updated', fetchAvatar)
  if (resizeHandler) window.removeEventListener('resize', resizeHandler)
})

const goSettings = () => router.push('/Settings')

const logout = async () => {
  try { await request.post('/auth/logout') } catch {}
  clearAuth()
  router.push('/login')
}
</script>

<style scoped>
.layout-container {
  height: 100%;
}
.layout-shell {
  height: 100%;
}

/* === Header — Deep Blue === */
.layout-header {
  height: var(--header-height) !important;
  background: var(--color-primary);
  color: #fff;
  font-size: var(--font-size-lg);
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 1px 6px rgba(var(--color-primary-rgb), .3);
  padding: 0 var(--spacing-lg);
  z-index: 100;
}
.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}
.collapse-btn {
  cursor: pointer;
  color: rgba(255, 255, 255, .7);
  transition: color var(--transition-fast);
}
.collapse-btn:hover {
  color: #fff;
}
.header-logo-img {
  width: 32px;
  height: 32px;
  object-fit: contain;
}
.header-title {
  font-size: 20px;
  font-weight: 600;
  color: #fff;
  white-space: nowrap;
}
.header-right {
  display: flex;
  align-items: center;
  gap: 10px;
}
.header-avatar {
  cursor: pointer;
  flex-shrink: 0;
}
.header-user {
  font-size: var(--font-size-sm);
  color: rgba(255, 255, 255, .75);
  white-space: nowrap;
}

/* === Sidebar === */
.layout-body {
  height: calc(100% - var(--header-height));
}
.layout-aside {
  transition: width var(--transition-slow);
  overflow: hidden;
  background: var(--color-bg-card);
}
.sidebar-menu {
  height: 100%;
  border-right: 1px solid var(--color-border-light);
}
.sidebar-menu:not(.el-menu--collapse) {
  width: 220px;
}

.sidebar-menu .el-sub-menu__title,
.sidebar-menu .el-menu-item {
  color: var(--color-text-regular);
}
.sidebar-menu .el-menu-item.is-active {
  color: var(--color-primary);
  background-color: var(--color-primary-light);
}
.sidebar-menu .el-sub-menu__title:hover,
.sidebar-menu .el-menu-item:hover {
  color: var(--color-primary);
  background-color: var(--color-bg-hover);
}

.menu-settings {
  position: absolute !important;
  bottom: 0;
  width: 100%;
}

/* === Main with Background === */
.layout-main {
  position: relative;
  padding: var(--spacing-lg);
  overflow-y: auto;
  background: #e8ecf1;
}

/* Blurred background layer */
.main-bg {
  position: absolute;
  inset: 0;
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  filter: blur(10px);
  transform: scale(1.05);
  z-index: 0;
}
.main-bg::after {
  content: '';
  position: absolute;
  inset: 0;
  background: rgba(255, 255, 255, .05);
}

/* Content above background */
.main-content {
  position: relative;
  z-index: 1;
}
</style>
