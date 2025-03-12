<template>
    <div class="dashboard">
        <el-row :gutter="20">
            <el-col :span="6">
                <el-card shadow="hover">
                    <div class="card-item">
                        <div class="card-title">总用户数</div>
                        <div class="card-number">1234</div>
                    </div>
                </el-card>
            </el-col>
            <el-col :span="6">
                <el-card shadow="hover">
                    <div class="card-item">
                        <div class="card-title">在线用户</div>
                        <div class="card-number">123</div>
                    </div>
                </el-card>
            </el-col>
            <el-col :span="6">
                <el-card shadow="hover">
                    <div class="card-item">
                        <div class="card-title">今日消息数</div>
                        <div class="card-number">5678</div>
                    </div>
                </el-card>
            </el-col>
        </el-row>
    </div>
</template>

<script>
export default {
    name: 'Dashboard'
}
</script>

<style lang="scss" scoped>
.dashboard {
    .card-item {
        text-align: center;

        .card-title {
            font-size: 14px;
            color: #666;
        }

        .card-number {
            font-size: 24px;
            font-weight: bold;
            margin-top: 10px;
        }
    }
}
</style>