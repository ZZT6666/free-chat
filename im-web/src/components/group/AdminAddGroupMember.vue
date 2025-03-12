<template>
    <el-dialog title="邀请用户" :visible.sync="dialogVisible" width="620px" :before-close="handleClose">
        <div class="agm-container">
            <div class="agm-l-box">
                <div class="search">
                    <el-input placeholder="搜索用户" v-model="searchKey" size="small">
                        <i class="el-icon-search el-input__icon" slot="suffix"></i>
                    </el-input>
                </div>
                <el-scrollbar style="height:400px;">
                    <div v-for="user in filteredUsers" :key="user.id">
                        <div class="friend-item" @click="handleSwitchCheck(user)"
                            :class="{ 'disabled': user.disabled }">
                            <el-checkbox :disabled="user.disabled" @click.native.stop="" class="agm-friend-checkbox"
                                v-model="user.isCheck" size="medium">
                            </el-checkbox>
                            <head-image :size="40" :url="user.headImage" :name="user.nickName"></head-image>
                            <div class="friend-info">
                                <div class="friend-name">{{ user.nickName }}</div>
                                <div class="friend-id">ID: {{ user.id }}</div>
                            </div>
                        </div>
                    </div>
                </el-scrollbar>
            </div>
            <div class="agm-arrow el-icon-d-arrow-right"></div>
            <div class="agm-r-box">
                <div class="agm-select-tip">已勾选 {{ checkCount }} 位用户</div>
                <el-scrollbar style="height:400px;">
                    <div v-for="user in selectedList" :key="user.id">
                        <div class="friend-item">
                            <head-image :size="40" :url="user.headImage" :name="user.nickName"></head-image>
                            <div class="friend-info">
                                <div class="friend-name">{{ user.nickName }}</div>
                            </div>
                            <i class="el-icon-close" @click="handleRemoveUser(user)"></i>
                        </div>
                    </div>
                </el-scrollbar>
            </div>
        </div>
        <span slot="footer" class="dialog-footer">
            <el-button @click="handleClose">取 消</el-button>
            <el-button type="primary" @click="handleConfirm" :loading="loading">确 定</el-button>
        </span>
    </el-dialog>
</template>

<script>
import HeadImage from '@/components/common/HeadImage.vue'

export default {
    name: 'AdminAddGroupMember',
    components: { HeadImage },
    props: {
        visible: Boolean,
        groupId: [String, Number],
        members: {
            type: Array,
            default: () => []
        }
    },
    data() {
        return {
            searchKey: '',
            userList: [],
            loading: false
        }
    },
    computed: {
        dialogVisible: {
            get() { return this.visible },
            set(val) { this.$emit('close') }
        },
        filteredUsers() {
            return this.userList.filter(user => {
                const matchSearch = !this.searchKey ||
                    user.nickName.toLowerCase().includes(this.searchKey.toLowerCase()) ||
                    user.id.toString().includes(this.searchKey)
                return matchSearch
            })
        },
        selectedList() {
            return this.userList.filter(user => user.isCheck && !user.disabled)
        },
        checkCount() {
            return this.selectedList.length
        }
    },
    methods: {
        async loadUsers() {
            try {
                const res = await this.$http({
                    url: '/user/findAllUser',
                    method: 'get'
                })
                if (res) {
                    this.userList = res.map(user => ({
                        ...user,
                        disabled: this.members.some(m => m.userId === user.id),
                        isCheck: false
                    }))
                }
            } catch (error) {
                console.error('获取用户列表失败:', error)
                this.$message.error('获取用户列表失败')
            }
        },
        handleSwitchCheck(user) {
            if (!user.disabled) {
                this.$set(user, 'isCheck', !user.isCheck)
            }
        },
        handleRemoveUser(user) {
            user.isCheck = false
        },
        async handleConfirm() {
            const userIds = this.selectedList.map(user => user.id)
            if (!userIds.length) {
                this.$message.warning('请选择要添加的成员')
                return
            }
            try {
                this.loading = true
                await this.$http({
                    url: `/group/admin/addMember`,
                    method: 'post',
                    data: {
                        groupId: this.groupId,
                        friendIds: userIds
                    }
                })
                this.$message.success('添加成功')
                this.handleClose()
                await this.$emit('reload', this.groupId)
            } catch (error) {
                console.error('添加成员失败:', error)
                this.$message.error('添加成员失败')
            } finally {
                this.loading = false
            }
        },
        handleClose() {
            this.searchKey = ''
            this.userList = []
            this.$emit('close')
        }
    },
    watch: {
        visible(val) {
            if (val) {
                this.loadUsers()
            }
        }
    }
}
</script>

<style lang="scss">
.agm-container {
    display: flex;

    .agm-l-box {
        flex: 1;
        overflow: hidden;
        border: 1px solid #DCDFE6;

        .search {
            height: 40px;
            display: flex;
            align-items: center;

            .el-input__inner {
                border: unset;
                border-bottom: 1px solid #DCDFE6;
            }
        }

        .agm-friend-checkbox {
            margin-right: 20px;
        }
    }

    .agm-arrow {
        display: flex;
        align-items: center;
        font-size: 18px;
        padding: 10px;
        font-weight: 600;
        color: #409EFF;
    }

    .agm-r-box {
        flex: 1;
        border: 1px solid #DCDFE6;

        .agm-select-tip {
            text-align: left;
            height: 40px;
            line-height: 40px;
            text-indent: 6px;
            color: #909399;
        }
    }
}

.friend-item {
    display: flex;
    align-items: center;
    padding: 10px;
    cursor: pointer;

    &:hover {
        background-color: #f5f7fa;
    }

    &.disabled {
        cursor: not-allowed;
        opacity: 0.7;
    }

    .friend-info {
        margin-left: 10px;
        flex: 1;

        .friend-name {
            font-size: 14px;
            color: #303133;
            margin-bottom: 4px;
        }

        .friend-id {
            font-size: 12px;
            color: #909399;
        }
    }

    .el-icon-close {
        cursor: pointer;
        color: #909399;
        font-size: 16px;
        padding: 0 8px;

        &:hover {
            color: #f56c6c;
        }
    }
}
</style>