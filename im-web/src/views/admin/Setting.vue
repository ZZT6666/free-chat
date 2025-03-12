<template>
    <div class="admin-setting">
        <div class="page-header">
            <h2>设置</h2>
        </div>
        <el-form :model="settings" label-width="120px">
            <el-form-item label="系统名称">
                <el-input v-model="settings.systemName"></el-input>
            </el-form-item>
            <el-form-item label="最大群成员数">
                <el-input-number v-model="settings.maxGroupMembers" :min="1" :max="1000"></el-input-number>
            </el-form-item>
            <el-form-item label="允许注册">
                <el-switch v-model="settings.allowRegister"></el-switch>
            </el-form-item>
            <el-form-item>
                <el-button type="primary">保存设置</el-button>
            </el-form-item>
        </el-form>
    </div>
</template>

<script>
export default {
    name: 'AdminSetting',
    data() {
        return {
            settings: {
                systemName: '',
                maxGroupMembers: 100,
                allowRegister: true
            }
        }
    }
}
</script>