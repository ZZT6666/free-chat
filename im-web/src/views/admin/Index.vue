<template>
    <div class="admin-index">
        <el-row :gutter="20">
            <el-col :span="6">
                <el-card shadow="hover">
                    <div class="card-item">
                        <div class="card-title">总用户数</div>
                        <div class="card-number">{{ statistics.userCount || 0 }}</div>
                    </div>
                </el-card>
            </el-col>
            <el-col :span="6">
                <el-card shadow="hover">
                    <div class="card-item">
                        <div class="card-title">在线用户</div>
                        <div class="card-number">{{ statistics.onlineCount || 0 }}</div>
                    </div>
                </el-card>
            </el-col>
            <el-col :span="6">
                <el-card shadow="hover">
                    <div class="card-item">
                        <div class="card-title">群聊数量</div>
                        <div class="card-number">{{ statistics.groupCount || 0 }}</div>
                    </div>
                </el-card>
            </el-col>
            <el-col :span="6">
                <el-card shadow="hover">
                    <div class="card-item">
                        <div class="card-title">文件数量</div>
                        <div class="card-number">{{ statistics.fileCount || 0 }}</div>
                    </div>
                </el-card>
            </el-col>
        </el-row>
    </div>
</template>

<script>
export default {
    name: 'AdminIndex',
    data() {
        return {
            statistics: {
                userCount: 0,
                onlineCount: 0,
                groupCount: 0,
                fileCount: 0
            },
            loading: false
        }
    },
    methods: {
        async fetchStatistics() {
            try {
                this.loading = true
                const response = await this.$http.get('/admin/statistics')
                if (response) {
                    this.statistics = response
                }
            } catch (error) {
                console.error('获取统计数据失败:', error)
                this.$message.error('获取统计数据失败')
            } finally {
                this.loading = false
            }
        }
    },
    mounted() {
        this.fetchStatistics()
    }
}
</script>

<style lang="scss" scoped>
.admin-index {
    padding: 20px;

    .el-card {
        transition: all 0.3s;

        &:hover {
            transform: translateY(-2px);
            box-shadow: 0 2px 12px 0 rgba(0,0,0,.1);
        }
    }

    .card-item {
        text-align: center;
        padding: 20px 0;

        .card-title {
            font-size: 14px;
            color: #666;
        }

        .card-number {
            font-size: 28px;
            font-weight: bold;
            margin-top: 10px;
            background: linear-gradient(45deg, #409EFF, #36D1DC);
            -webkit-background-clip: text;
            color: transparent;
        }
    }

    :deep(.el-card__body) {
        padding: 10px;
    }
}
</style>