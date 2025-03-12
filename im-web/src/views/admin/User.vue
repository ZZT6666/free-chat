<template>
    <div class="admin-user">
        <div class="operation-bar">
            <el-button type="primary" size="small" @click="handleAdd">
                <i class="el-icon-plus"></i> 添加成员
            </el-button>
            <div class="search-area">
                <el-input v-model="searchKey" placeholder="请输入用户名/昵称" size="small" clearable @clear="handleSearch"
                    style="width: 200px; margin-right: 10px">
                </el-input>
                <el-button type="primary" size="small" @click="handleSearch">
                    <i class="el-icon-search"></i> 搜索
                </el-button>
            </div>
        </div>

        <el-table :data="userList" stripe style="width: 100%;border: solid 1px #e3e3e3;" :height="'calc(100vh - 200px)'"
            :header-cell-style="{ 'background-color': '#f5f7fa', 'color': '#909399' }" v-loading="loading"
            class="small-font-table" :scrollbar-always-on="true">
            <el-table-column prop="id" label="id" width="80" fixed></el-table-column>
            <el-table-column prop="userName" label="用户名" width="120" fixed></el-table-column>
            <el-table-column prop="nickName" label="昵称" width="120"></el-table-column>
            <el-table-column prop="head_image" label="用户头像" width="120"></el-table-column>
            <el-table-column prop="sex" label="性别" width="80">
                <template #default="scope">
                    {{ scope.row.sex === 0 ? '男' : '女' }}
                </template>
            </el-table-column>
            <el-table-column prop="type" label="角色" width="120">
                <template #default="scope">
                    {{ scope.row.type === 0 ? '超级管理员' : (scope.row.type === 1 ? '普通用户' : '管理员') }}
                </template>
            </el-table-column>
            <el-table-column prop="isBanned" label="状态" width="100">
                <template #default="scope">
                    <el-tag :type="scope.row.isBanned ? 'danger' : 'success'">
                        {{ scope.row.isBanned === 0 ? '正常' : '封禁' }}
                    </el-tag>
                </template>
            </el-table-column>
            <el-table-column prop="createdTime" label="注册时间" width="180"></el-table-column>
            <el-table-column prop="lastLoginTime" label="最后登录时间" width="180"></el-table-column>
            <el-table-column fixed="right" label="操作" width="200">
                <template #default="scope">
                    <div class="operation-buttons">
                        <el-button size="small" type="text">详情</el-button>
                        <el-divider direction="vertical"></el-divider>
                        <template v-if="canEdit(scope.row)">
                            <el-button size="small" type="text" @click="handleEdit(scope.row)">编辑</el-button>
                            <el-divider direction="vertical"></el-divider>
                        </template>
                        <el-button size="small" type="text" @click="handleChangePassword(scope.row)">改密</el-button>
                    </div>
                </template>
            </el-table-column>
        </el-table>

        <!-- 添加成员对话框 -->
        <el-dialog title="添加成员" :visible.sync="dialogVisible" width="500px">
            <el-form :model="userForm" :rules="rules" ref="userForm" label-width="80px">
                <el-form-item label="用户名" prop="username">
                    <el-input v-model="userForm.username"></el-input>
                </el-form-item>
                <el-form-item label="密码" prop="password">
                    <el-input v-model="userForm.password" type="password"></el-input>
                </el-form-item>
                <el-form-item label="昵称" prop="nickname">
                    <el-input v-model="userForm.nickname"></el-input>
                </el-form-item>
                <el-form-item label="性别" prop="sex">
                    <el-radio-group v-model="userForm.sex">
                        <el-radio :label="0">男</el-radio>
                        <el-radio :label="1">女</el-radio>
                    </el-radio-group>
                </el-form-item>
            </el-form>
            <div slot="footer" class="dialog-footer">
                <el-button @click="dialogVisible = false">取 消</el-button>
                <el-button type="primary" @click="submitForm">确 定</el-button>
            </div>
        </el-dialog>

        <!-- 编辑用户对话框 -->
        <el-dialog title="编辑用户" :visible.sync="editDialogVisible" width="500px">
            <el-form :model="editForm" :rules="editRules" ref="editForm" label-width="80px">
                <el-form-item label="用户名" prop="userName">
                    <el-input v-model="editForm.userName" disabled></el-input>
                </el-form-item>
                <el-form-item label="昵称" prop="nickName">
                    <el-input v-model="editForm.nickName"></el-input>
                </el-form-item>
                <el-form-item label="性别" prop="sex">
                    <el-radio-group v-model="editForm.sex">
                        <el-radio :label="0">男</el-radio>
                        <el-radio :label="1">女</el-radio>
                    </el-radio-group>
                </el-form-item>
                <el-form-item label="角色" prop="type" width="120">
                    <el-select v-model="editForm.type" placeholder="请选择角色" :disabled="editForm.type === 0">
                        <el-option label="普通用户" :value="1"></el-option>
                        <el-option label="管理员" :value="2"></el-option>
                    </el-select>
                </el-form-item>
                <el-form-item label="状态" prop="isBanned">
                    <el-radio-group v-model="editForm.isBanned">
                        <el-radio :label="0">正常</el-radio>
                        <el-radio :label="1">封禁</el-radio>
                    </el-radio-group>
                </el-form-item>
            </el-form>
            <div slot="footer" class="dialog-footer">
                <el-button @click="editDialogVisible = false">取 消</el-button>
                <el-button type="primary" @click="submitEditForm">确 定</el-button>
            </div>
        </el-dialog>

        <!-- 修改密码对话框 -->
        <el-dialog title="修改密码" :visible.sync="passwordDialogVisible" width="500px">
            <el-form :model="passwordForm" :rules="passwordRules" ref="passwordForm" label-width="100px">
                <el-form-item label="用户名">
                    <el-input v-model="passwordForm.userName" disabled></el-input>
                </el-form-item>
                <el-form-item label="新密码" prop="newPassword">
                    <el-input v-model="passwordForm.newPassword" type="password" show-password></el-input>
                </el-form-item>
                <el-form-item label="确认新密码" prop="confirmPassword">
                    <el-input v-model="passwordForm.confirmPassword" type="password" show-password></el-input>
                </el-form-item>
            </el-form>
            <div slot="footer" class="dialog-footer">
                <el-button @click="passwordDialogVisible = false">取 消</el-button>
                <el-button type="primary" @click="submitPasswordForm">确 定</el-button>
            </div>
        </el-dialog>
    </div>
</template>

<script>
import dayjs from 'dayjs'

export default {
    name: 'AdminUser',
    data() {
        // 密码确认验证
        const validateConfirmPassword = (rule, value, callback) => {
            if (value !== this.passwordForm.newPassword) {
                callback(new Error('两次输入的密码不一致'))
            } else {
                callback()
            }
        }

        return {
            searchKey: '',
            dialogVisible: false,
            loading: false,
            userList: [], // 用户列表数据
            userForm: {
                username: '',
                password: '',
                nickname: '',
                sex: 1
            },
            rules: {
                username: [
                    { required: true, message: '请输入用户名', trigger: 'blur' },

                ],
                password: [
                    { required: true, message: '请输入密码', trigger: 'blur' },
                    { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
                ],
                nickname: [
                    { required: true, message: '请输入昵称', trigger: 'blur' }
                ]
            },
            editDialogVisible: false,
            editForm: {
                id: '',
                userName: '',
                nickName: '',
                sex: 0,
                type: 1,
                isBanned: false
            },
            editRules: {
                nickName: [
                    { required: true, message: '请输入昵称', trigger: 'blur' }
                ],
                sex: [
                    { required: true, message: '请选择性别', trigger: 'change' }
                ],
                type: [
                    { required: true, message: '请选择角色', trigger: 'change' }
                ]
            },
            currentUser: null, // 当前登录用户信息
            passwordDialogVisible: false,
            passwordForm: {
                id: '',
                userName: '',
                newPassword: '',
                confirmPassword: ''
            },
            passwordRules: {
                newPassword: [
                    { required: true, message: '请输入新密码', trigger: 'blur' },
                    { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
                ],
                confirmPassword: [
                    { required: true, message: '请再次输入新密码', trigger: 'blur' },
                    { validator: validateConfirmPassword, trigger: 'blur' }
                ]
            }
        }
    },
    methods: {
        // 获取用户列表
        async loadUserList() {
            try {
                this.loading = true
                const res = await this.$http({
                    url: '/user/findAllUser',
                    method: 'get'
                })
                if (res) {
                    this.userList = res
                    // 处理时间格式化
                    this.userList.forEach(user => {
                        if (user.createdTime) {
                            user.createdTime = dayjs(user.createdTime).format('YYYY-MM-DD HH:mm:ss')
                        }
                        if (user.lastLoginTime) {
                            user.lastLoginTime = dayjs(user.lastLoginTime).format('YYYY-MM-DD HH:mm:ss')
                        }
                    })
                }
            } catch (error) {
                console.error('获取用户列表失败:', error)
                this.$message.error('获取用户列表失败')
            } finally {
                this.loading = false
            }
        },

        // 搜索用户
        handleSearch() {
            if (this.searchKey) {
                this.userList = this.userList.filter(user =>
                    user.username.includes(this.searchKey) ||
                    user.nickname.includes(this.searchKey)
                )
            } else {
                this.loadUserList() // 如果搜索框清空，重新加载所有用户
            }
        },

        handleAdd() {
            this.userForm = {
                username: '',
                password: '',
                nickname: '',
                sex: 1
            }
            this.dialogVisible = true
        },

        // 提交添加用户表单
        submitForm() {
            this.$refs.userForm.validate(async (valid) => {
                if (valid) {
                    try {
                        this.loading = true  // 添加loading状态
                        // 等待添加用户操作完成
                        await this.$http({
                            url: '/register',
                            method: 'post',
                            data: {
                                userName: this.userForm.username,
                                password: this.userForm.password,
                                nickName: this.userForm.nickname,
                                sex: this.userForm.sex,
                            }
                        })

                        this.$message.success('添加成功')
                        this.dialogVisible = false

                        // 等待列表重新加载完成
                        await this.loadUserList()

                        // 清空表单
                        this.userForm = {
                            username: '',
                            password: '',
                            nickname: '',
                            sex: 1
                        }
                    } catch (error) {
                        console.error('添加用户失败:', error)
                        this.$message.error('添加用户失败')
                    } finally {
                        this.loading = false  // 结束loading状态
                    }
                }
            })
        },

        // 打开编辑对话框
        handleEdit(row) {
            this.editForm = {
                id: row.id,
                userName: row.userName,
                nickName: row.nickName,
                sex: row.sex,
                type: row.type,
                isBanned: row.isBanned
            }
            this.editDialogVisible = true
        },

        // 提交编辑表单
        submitEditForm() {
            this.$refs.editForm.validate(async (valid) => {
                if (valid) {
                    try {
                        await this.$http({
                            url: '/user/update',
                            method: 'put',
                            data: this.editForm
                        })
                        this.$message.success('更新成功')
                        this.editDialogVisible = false
                        this.loadUserList() // 重新加载用户列表
                    } catch (error) {
                        console.error('更新用户信息失败:', error)
                        this.$message.error('更新用户信息失败')
                    }
                }
            })
        },

        // 获取当前登录用户信息
        async getCurrentUser() {
            try {
                const res = await this.$http({
                    url: '/user/self',
                    method: 'get'
                })
                if (res) {
                    this.currentUser = res
                }
            } catch (error) {
                console.error('获取当前用户信息失败:', error)
            }
        },

        // 判断是否可以编辑用户
        canEdit(user) {
            if (!this.currentUser) return false

            // 如果是超级管理员，可以编辑除了超级管理员以外的所有用户
            if (this.currentUser.type === 0) {
                return user.type !== 0
            }

            // 如果是管理员，只能编辑普通用户
            if (this.currentUser.type === 2) {
                return user.type === 1
            }

        },

        // 打开修改密码对话框
        handleChangePassword(row) {
            this.passwordForm = {
                id: row.id,
                userName: row.userName,
                newPassword: '',
                confirmPassword: ''
            }
            this.passwordDialogVisible = true
        },

        // 提交修改密码表单
        submitPasswordForm() {
            this.$refs.passwordForm.validate(async (valid) => {
                if (valid) {
                    try {
                        await this.$http({
                            url: '/user/adminModifyPwd',
                            method: 'put',
                            data: {
                                id: this.passwordForm.id,
                                newPassword: this.passwordForm.newPassword
                            }
                        })
                        this.$message.success('密码修改成功')
                        this.passwordDialogVisible = false
                        // 清空表单
                        this.passwordForm.newPassword = ''
                        this.passwordForm.confirmPassword = ''
                    } catch (error) {
                        console.error('修改密码失败:', error)
                        this.$message.error('修改密码失败')
                    }
                }
            })
        }
    },
    async mounted() {
        await this.getCurrentUser() // 先获取当前用户信息
        this.loadUserList()
    }
}
</script>

<style lang="scss" scoped>
.admin-user {
    .operation-bar {
        margin-bottom: 20px;
        display: flex;
        justify-content: space-between;
        align-items: center;
    }

    .search-area {
        display: flex;
        align-items: center;
    }

    .operation-buttons {
        display: flex;
        align-items: center;

        .el-button {
            padding: 0 5px;
        }

        .el-divider {
            margin: 0 5px;
        }
    }

    // 添加表格字体样式
    .small-font-table {
        font-size: 13px; // 设置表格内容字体大小

        // 设置表头字体大小
        :deep(.el-table__header-wrapper) {
            font-size: 13px;
        }

        // 设置单元格内容字体大小
        :deep(.el-table__body-wrapper) {
            font-size: 13px;
        }

        // 设置表格中按钮的字体大小
        :deep(.el-button--text) {
            font-size: 13px;
        }

        // 设置状态标签的字体大小
        :deep(.el-tag) {
            font-size: 12px;
        }

        // 自定义滚动条样式
        :deep(.el-table__body-wrapper::-webkit-scrollbar) {
            width: 8px;
            height: 8px;
        }

        :deep(.el-table__body-wrapper::-webkit-scrollbar-thumb) {
            background: #ddd;
            border-radius: 4px;
        }

        :deep(.el-table__body-wrapper::-webkit-scrollbar-track) {
            background: #f5f5f5;
        }
    }
}
</style>
