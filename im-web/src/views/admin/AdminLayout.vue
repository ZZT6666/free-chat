<template>
    <el-container class="admin-layout">
        <el-aside width="200px">
            <el-menu :default-active="$route.path" class="admin-menu" background-color="#304156" text-color="#fff"
                active-text-color="#409EFF" router>
                <el-menu-item index="/admin/index">
                    <i class="el-icon-s-home"></i>
                    <span>概况</span>
                </el-menu-item>
                <el-menu-item index="/admin/file">
                    <i class="el-icon-folder"></i>
                    <span>文件</span>
                </el-menu-item>
                <el-menu-item index="/admin/message">
                    <i class="el-icon-chat-line-square"></i>
                    <span>消息</span>
                </el-menu-item>
                <el-menu-item index="/admin/group">
                    <i class="el-icon-s-grid"></i>
                    <span>群聊</span>
                </el-menu-item>
                <el-menu-item index="/admin/user">
                    <i class="el-icon-user"></i>
                    <span>成员</span>
                </el-menu-item>
                <el-menu-item index="/admin/setting">
                    <i class="el-icon-setting"></i>
                    <span>设置</span>
                </el-menu-item>
            </el-menu>
        </el-aside>
        <el-container>
            <el-header>
                <div class="header-right">
                    <el-button type="primary" size="small" @click="backToChat" class="back-btn">
                        <i class="el-icon-back"></i> 返回聊天
                    </el-button>
                    <el-dropdown trigger="click" class="user-dropdown">
                        <span class="el-dropdown-link">
                            <div class="user-info">
                                <head-image :name="$store.state.userStore.userInfo.nickName" :size="32"
                                    :url="$store.state.userStore.userInfo.headImageThumb">
                                </head-image>
                                <span class="username">{{ $store.state.userStore.userInfo.nickName }}</span>
                                <i class="el-icon-arrow-down el-icon--right"></i>
                            </div>
                        </span>
                        <el-dropdown-menu slot="dropdown">
                            <el-dropdown-item @click="handleLogout">退出登录</el-dropdown-item>
                        </el-dropdown-menu>
                    </el-dropdown>
                </div>
            </el-header>
            <el-main>
                <router-view></router-view>
            </el-main>
        </el-container>
    </el-container>
</template>

<script>
import HeadImage from '@/components/common/HeadImage.vue'

export default {
    name: 'AdminLayout',
    components: {
        HeadImage
    },
    methods: {
        backToChat() {
            this.$router.push('/home/chat');
        },
        handleLogout() {
            // 清除token
            localStorage.removeItem('token')
            // 清除用户信息
            this.$store.commit('userStore/clearUserInfo')
            // 跳转到登录页
            this.$router.push('/login')
        }
    }
}
</script>

<style lang="scss" scoped>
.admin-layout {
    height: 100%;

    .el-aside {
        background-color: #304156;

        .admin-menu {
            border: none;
        }
    }

    .el-header {
        background-color: #fff;
        box-shadow: 0 1px 4px rgba(0, 21, 41, .08);
        display: flex;
        align-items: center;
        justify-content: flex-end;
        padding: 0 20px;
        position: relative;
    }

    .header-right {
        display: flex;
        align-items: center;
        position: absolute;
        right: 20px;
        max-width: 300px;

        .back-btn {
            margin-right: 20px;
        }

        .user-dropdown {
            min-width: 120px;
        }

        .user-info {
            display: flex;
            align-items: center;
            max-width: 200px;

            .username {
                margin: 0 8px;
                font-size: 14px;
                color: #606266;
                max-width: 120px;
                overflow: hidden;
                text-overflow: ellipsis;
                white-space: nowrap;
            }
        }

        .el-dropdown-link {
            display: flex;
            align-items: center;
        }
    }
}
</style>