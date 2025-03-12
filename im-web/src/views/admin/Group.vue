<template>
    <div class="pd-20">
        <el-container>
            <!-- 左侧群聊列表 -->
            <el-aside width="320px">
                <div class="lz-flex group-box">
                    <div class="group-box-header" v-if="!showSearch">
                        <div>群列表</div>
                        <div>
                            <el-button plain circle @click="showSearch = true" icon="el-icon-search"
                                title="搜索"></el-button>
                            <el-button plain round @click="handleCreateGroup">创建群聊</el-button>
                        </div>
                    </div>
                    <div class="group-box-header" v-else>
                        <el-input placeholder="请输入关键字搜索" style="width: 300px" @keyup.enter.native="handleSearch"
                            v-model="searchKey">
                            <el-button slot="prepend" icon="el-icon-back" @click="showSearch = false"></el-button>
                            <el-button slot="append" icon="el-icon-search" @click="handleSearch"></el-button>
                        </el-input>
                    </div>
                    <div class="group-box-list">
                        <el-scrollbar>
                            <div v-for="group in groupList" :key="group.id" class="chat-item"
                                @click="handleGroupSelect(group)"
                                :class="currentGroup?.id === group.id ? 'active' : ''">
                                <head-image class="avatar" :url="group.headImage" :name="group.name" radius="10%">
                                </head-image>
                                <div class="chat-content">
                                    <div class="chat-name">{{ group.name }}</div>
                                    <div class="chat-id">群id：{{ group.id }}</div>
                                </div>
                            </div>
                        </el-scrollbar>
                    </div>
                </div>
            </el-aside>

            <!-- 右侧成员列表 -->
            <el-main>
                <div class="lz-flex group-box" v-loading="memberLoading">
                    <template v-if="currentGroup">
                        <div class="group-box-header">
                            <div>群成员</div>
                            <div>
                                <el-button plain round @click="handleAddMember">添加成员</el-button>
                                <el-button type="warning" plain round @click="handleMonitor">群聊监控</el-button>
                                <el-button type="danger" plain round @click="handleDissolveGroup">解散群聊</el-button>
                            </div>
                        </div>
                        <div class="group-box-list">
                            <el-scrollbar>
                                <div v-for="member in memberList" :key="member.id" class="member-item">
                                    <div class="member-avatar">
                                        <head-image :size="40" :url="member.headImage"
                                            :name="member.showNickName"></head-image>
                                    </div>
                                    <div class="member-info">
                                        <div class="member-name">{{ member.showNickName }}</div>
                                        <div class="member-role">
                                            <el-tag size="mini" type="warning" v-if="member.type === 0">群主</el-tag>
                                            <el-tag size="mini" type="success"
                                                v-else-if="member.type === 1">管理员</el-tag>
                                            <span class="c-999" v-else>普通成员</span>
                                        </div>
                                    </div>
                                    <div class="member-action" v-if="member.type !== 0">
                                        <el-dropdown trigger="click">
                                            <span class="el-dropdown-link">
                                                <i class="el-icon-more"></i>
                                            </span>
                                            <el-dropdown-menu slot="dropdown">
                                                <el-dropdown-item @click.native="handleViewProfile(member)">
                                                    <i class="el-icon-user"></i> 查看资料
                                                </el-dropdown-item>
                                                <el-dropdown-item @click.native="handleSetAdmin(member)"
                                                    v-if="member.type === 2">
                                                    <i class="el-icon-s-custom"></i> 设置为管理员
                                                </el-dropdown-item>
                                                <el-dropdown-item @click.native="handleCancelAdmin(member)"
                                                    v-if="member.type === 1">
                                                    <i class="el-icon-s-custom"></i> 取消管理员
                                                </el-dropdown-item>
                                                <el-dropdown-item @click.native="handleTransferOwner(member)">
                                                    <i class="el-icon-s-flag"></i> 设置为群主
                                                </el-dropdown-item>
                                                <el-dropdown-item divided @click.native="handleRemoveMember(member)">
                                                    <span class="c-red"><i class="el-icon-delete"></i> 移出群聊</span>
                                                </el-dropdown-item>
                                            </el-dropdown-menu>
                                        </el-dropdown>
                                    </div>
                                </div>
                                <add-group-member :visible="showAddMember" :groupId="currentGroup.id"
                                    :members="memberList" @reload="loadGroupMembers" @close="showAddMember = false">
                                </add-group-member>
                            </el-scrollbar>
                        </div>
                    </template>
                    <template v-else>
                        <el-empty description="请选择群聊"></el-empty>
                    </template>
                </div>
            </el-main>
        </el-container>
    </div>
</template>

<script>
import dayjs from 'dayjs'
import AdminAddGroupMember from '@/components/group/AdminAddGroupMember.vue'
import HeadImage from '@/components/common/HeadImage.vue'

export default {
    name: 'AdminGroup',
    components: {
        HeadImage,
        AddGroupMember: AdminAddGroupMember
    },
    data() {
        return {
            showSearch: false,
            searchKey: '',
            loading: false,
            groupList: [],
            currentGroup: null,
            ownerMap: {}, // 用于缓存群主信息
            memberList: [],
            memberLoading: false,
            showAddMember: false
        }
    },
    methods: {
        // 获取群聊列表
        async loadGroupList() {
            try {
                this.loading = true
                const res = await this.$http({
                    url: '/group/groupList',
                    method: 'get'
                })
                if (res) {
                    this.groupList = res
                }
            } catch (error) {
                console.error('获取群聊列表失败:', error)
                this.$message.error('获取群聊列表失败')
            } finally {
                this.loading = false
            }
        },

        // 搜索
        handleSearch() {
            this.loadGroupList()
        },

        // 选择群聊
        async handleGroupSelect(group) {
            this.currentGroup = group
            await this.loadGroupMembers(group.id)
        },

        // 加载群成员
        async loadGroupMembers(groupId) {
            try {
                this.memberLoading = true
                const res = await this.$http({
                    url: `/group/admin/members/${groupId}`,
                    method: 'get'
                })
                if (res) {
                    this.memberList = res
                }
            } catch (error) {
                console.error('获取群成员失败:', error)
                this.$message.error('获取群成员失败')
            } finally {
                this.memberLoading = false
            }
        },

        // 移出成员
        async handleRemoveMember(member) {
            try {
                await this.$confirm(`确定将成员'${member.showNickName}'移出群聊吗？`, '确认移出?', {
                    confirmButtonText: '确定',
                    cancelButtonText: '取消',
                    type: 'warning'
                })
                await this.$http({
                    url: `/group/kick/${this.currentGroup.id}`,
                    method: 'delete',
                    params: {
                        userId: member.userId
                    }
                })
                this.$message.success(`已将${member.showNickName}移出群聊`)
                await this.loadGroupMembers(this.currentGroup.id)
            } catch (error) {
                if (error !== 'cancel') {
                    console.error('移出成员失败:', error)
                    this.$message.error('移出成员失败')
                }
            }
        },

        // 创建群聊
        async handleCreateGroup() {
            try {
                const { value } = await this.$prompt('请输入群聊名称', '创建群聊', {
                    confirmButtonText: '确定',
                    cancelButtonText: '取消',
                    inputPattern: /\S/,
                    inputErrorMessage: '请输入群聊名称'
                })

                await this.$http({
                    url: '/group/admin/create',
                    method: 'post',
                    data: {
                        name: value
                    }
                })
                this.$message.success('创建成功')
                await this.loadGroupList()
            } catch (error) {
                if (error !== 'cancel') {
                    console.error('创建群聊失败:', error)
                    this.$message.error('创建群聊失败')
                }
            }
        },

        // 添加成员
        handleAddMember() {
            this.showAddMember = true
        },

        // 群聊监控
        handleMonitor() {
            // 待实现 - 需要添加监控功能
        },

        // 解散群聊
        async handleDissolveGroup() {
            try {
                await this.$confirm(`确认要解散'${this.currentGroup.name}'吗?`, '确认解散?', {
                    confirmButtonText: '确定',
                    cancelButtonText: '取消',
                    type: 'warning'
                })
                await this.$http({
                    url: `/group/delete/${this.currentGroup.id}`,
                    method: 'delete'
                })
                this.$message.success(`群聊'${this.currentGroup.name}'已解散`)
                this.loadGroupList() // 重新加载群聊列表
                this.reset() // 重置当前选中的群聊
            } catch (error) {
                if (error !== 'cancel') {
                    console.error('解散群聊失败:', error)
                    this.$message.error('解散群聊失败')
                }
            }
        },

        // 重置当前群聊
        reset() {
            this.currentGroup = null
            this.memberList = []
        },

        // 查看资料
        handleViewProfile(member) {
            // 待实现 - 可以打开一个对话框显示用户详细信息
        },

        // 设置管理员
        async handleSetAdmin(member) {
            try {
                await this.$confirm(`确定将'${member.showNickName}'设置为管理员吗？`, '提示', {
                    confirmButtonText: '确定',
                    cancelButtonText: '取消',
                    type: 'warning'
                })
                await this.$http({
                    url: `/group/setAdmin/${this.currentGroup.id}`,
                    method: 'post',
                    params: {
                        userId: member.userId
                    }
                })
                this.$message.success(`已将'${member.showNickName}'设置为管理员`)
                await this.loadGroupMembers(this.currentGroup.id)
            } catch (error) {
                if (error !== 'cancel') {
                    console.error('设置管理员失败:', error)
                    this.$message.error('设置管理员失败')
                }
            }
        },

        // 取消管理员
        async handleCancelAdmin(member) {
            try {
                await this.$confirm(`确定取消'${member.showNickName}'的管理员权限吗？`, '提示', {
                    confirmButtonText: '确定',
                    cancelButtonText: '取消',
                    type: 'warning'
                })
                await this.$http({
                    url: `/group/cancelAdmin/${this.currentGroup.id}`,
                    method: 'post',
                    params: {
                        userId: member.userId
                    }
                })
                this.$message.success(`已取消'${member.showNickName}'的管理员权限`)
                await this.loadGroupMembers(this.currentGroup.id)
            } catch (error) {
                if (error !== 'cancel') {
                    console.error('取消管理员失败:', error)
                    this.$message.error('取消管理员失败')
                }
            }
        },

        // 转让群主
        async handleTransferOwner(member) {
            try {
                await this.$confirm(`确定将群主转让给'${member.showNickName}'吗？`, '提示', {
                    confirmButtonText: '确定',
                    cancelButtonText: '取消',
                    type: 'warning'
                })
                await this.$http({
                    url: `/group/setOwner/${this.currentGroup.id}`,
                    method: 'post',
                    params: {
                        userId: member.userId
                    }
                })
                this.$message.success(`已将群主转让给'${member.showNickName}'`)
                await this.loadGroupMembers(this.currentGroup.id)
            } catch (error) {
                if (error !== 'cancel') {
                    console.error('转让群主失败:', error)
                    this.$message.error('转让群主失败')
                }
            }
        }
    },
    mounted() {
        this.loadGroupList()
    }
}
</script>

<style lang="scss" scoped>
.pd-20 {
    padding: 20px;
    height: 100%;
    box-sizing: border-box;
}

.group-box {
    height: 100%;
    background: #fff;
    border-radius: 4px;
    flex-direction: column;

    .group-box-header {
        padding: 15px;
        display: flex;
        justify-content: space-between;
        align-items: center;
        border-bottom: 1px solid #eee;
    }

    .group-box-list {
        flex: 1;
        overflow: hidden;

        .el-scrollbar {
            height: 100%;
        }
    }
}

.chat-item {
    display: flex;
    align-items: center;
    padding: 10px 15px;
    cursor: pointer;

    &:hover,
    &.active {
        background: #f5f7fa;
    }

    .chat-avatar {
        margin-right: 10px;
    }

    .chat-content {
        flex: 1;
        margin: 0 10px;
        overflow: hidden;

        .chat-name {
            font-size: 14px;
            color: #303133;
            margin-bottom: 4px;
        }

        .chat-id {
            font-size: 12px;
            color: #909399;
        }
    }
}

.c-999 {
    color: #999;
}

.member-item {
    display: flex;
    align-items: center;
    padding: 10px 15px;

    .member-avatar {
        margin-right: 10px;
    }

    .member-info {
        flex: 1;
        margin: 0 10px;

        .member-name {
            font-size: 14px;
            color: #303133;
            margin-bottom: 4px;
        }

        .member-role {
            font-size: 12px;

            .el-tag {
                height: 20px;
                line-height: 18px;
                padding: 0 6px;
            }
        }
    }

    .member-action {
        .el-dropdown-link {
            cursor: pointer;
            color: #909399;
            font-size: 16px;
            padding: 0 8px;

            &:hover {
                color: #409EFF;
            }
        }
    }
}

.c-red {
    color: #f56c6c;
}

.group-invite {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 10px;

    .invite-member-btn {
        width: 38px;
        height: 38px;
        line-height: 38px;
        border: 1px solid #dcdfe6;
        font-size: 14px;
        cursor: pointer;
        text-align: center;
        border-radius: 4px;

        &:hover {
            border-color: #409EFF;
            color: #409EFF;
        }
    }

    .invite-member-text {
        font-size: 12px;
        color: #606266;
        margin-top: 8px;
    }
}

.el-main {
    overflow: hidden;
}
</style>
