<template>
    <div class="common-layout" style="height: 100%;">
        <el-container style="height: 100%;">
        <el-header style="height: 90px;background-color: #409eff;color: #fff;font-size: 20px;display: flex;justify-content: space-between;align-items: center;">
            <div class="header-left" style="display: flex;align-items: center;margin-left: 30px;">
                <el-icon size="40" style="margin-right: 20px;"><Monitor /></el-icon>
                <h1>驿站管理系统</h1>
            </div>
            <div class="header-right" style="display: flex;align-items: center;margin-right: 30px;">
                <p>当前用户：{{ username }}（{{ roleLabel }}）</p>
                <el-button type="danger" size="small" style="margin-left: 10px;" @click="logout">退出系统</el-button>
            </div>
        </el-header>
        <el-container style="height: 100%;">
            <el-aside width="200px">
                <el-menu active-text-color="#ffd04b" background-color="#545c64"
                    class="el-menu-vertical-demo" :default-active="route.path"
                    text-color="#fff" style="height: 100%;" router>

                    <!-- REGULAR 菜单 -->
                    <template v-if="role==='REGULAR'">
                        <el-sub-menu index="parcel-service">
                            <template #title><el-icon><Van /></el-icon><span>快递服务</span></template>
                            <el-menu-item index="/Welcome"><el-icon><HomeFilled /></el-icon><span>首页</span></el-menu-item>
                            <el-menu-item index="/ParcelTrack"><el-icon><Search /></el-icon><span>查件</span></el-menu-item>
                            <el-menu-item index="/ParcelPickup"><el-icon><Finished /></el-icon><span>取件</span></el-menu-item>
                            <el-menu-item index="/ParcelSend"><el-icon><Promotion /></el-icon><span>寄件</span></el-menu-item>
                        </el-sub-menu>
                        <el-sub-menu index="order-mgmt">
                            <template #title><el-icon><Document /></el-icon><span>订单管理</span></template>
                            <el-menu-item index="/MyUnpaid"><el-icon><Money /></el-icon><span>未付款订单</span></el-menu-item>
                        </el-sub-menu>
                        <el-sub-menu index="error-mgmt-user">
                            <template #title><el-icon><Warning /></el-icon><span>异常管理</span></template>
                            <el-menu-item index="/ErrorRegister"><el-icon><CirclePlus /></el-icon><span>异常登记</span></el-menu-item>
                        </el-sub-menu>
                    </template>

                    <!-- MANAGER 菜单 -->
                    <template v-if="role==='MANAGER'">
                        <el-sub-menu index="parcel-mgmt">
                            <template #title><el-icon><Box /></el-icon><span>包裹管理</span></template>
                            <el-menu-item index="/Welcome"><el-icon><HomeFilled /></el-icon><span>首页</span></el-menu-item>
                            <el-menu-item index="/ParcelSearch"><el-icon><Search /></el-icon><span>包裹查询</span></el-menu-item>
                            <el-menu-item index="/Inventory"><el-icon><DataAnalysis /></el-icon><span>库存盘点</span></el-menu-item>
                        </el-sub-menu>
                        <el-sub-menu index="approval-mgmt">
                            <template #title><el-icon><Checked /></el-icon><span>审批管理</span></template>
                            <el-menu-item index="/SendManage"><el-icon><List /></el-icon><span>寄件审批</span></el-menu-item>
                        </el-sub-menu>
                        <el-sub-menu index="error-mgmt-manager">
                            <template #title><el-icon><Warning /></el-icon><span>异常管理</span></template>
                            <el-menu-item index="/ErrorSearch"><el-icon><Search /></el-icon><span>异常查询</span></el-menu-item>
                        </el-sub-menu>
                    </template>

                    <!-- COURIER 菜单 -->
                    <template v-if="role==='COURIER'">
                        <el-sub-menu index="courier-home">
                            <template #title><el-icon><HomeFilled /></el-icon><span>首页</span></template>
                            <el-menu-item index="/Welcome"><el-icon><DataBoard /></el-icon><span>工作台</span></el-menu-item>
                        </el-sub-menu>
                        <el-sub-menu index="task-mgmt">
                            <template #title><el-icon><List /></el-icon><span>任务管理</span></template>
                            <el-menu-item index="/PickupQuery"><el-icon><Van /></el-icon><span>取件查询</span></el-menu-item>
                            <el-menu-item index="/ParcelDelivery"><el-icon><Box /></el-icon><span>快递入库</span></el-menu-item>
                        </el-sub-menu>
                    </template>

                </el-menu>
            </el-aside>
            <el-main>
                <router-view />
            </el-main>
        </el-container>
        </el-container>
  </div>
</template>

<script setup>
    import { ref } from 'vue'
    import { useRouter, useRoute } from 'vue-router'
    import { getUserInfo, clearToken } from '@/utils/auth'
    import { Monitor, HomeFilled, Van, Search, Finished, Promotion, Document, Money, Warning, CirclePlus, Box, DataAnalysis, Checked, List, DataBoard } from '@element-plus/icons-vue'

    const router = useRouter()
    const route = useRoute()
    const user = getUserInfo()
    const username = ref(user?.username || '')
    const role = ref(user?.role || '')

    const roleLabelMap = { REGULAR: '普通用户', COURIER: '快递员', MANAGER: '驿站管理员' }
    const roleLabel = ref(roleLabelMap[role.value] || '')

    const logout = () => {
      clearToken()
      router.push('/login')
    }
</script>

<style scoped>
</style>
