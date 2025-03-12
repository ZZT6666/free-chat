<template>
    <div class="admin-message">
        <div class="operation-bar">
            <div class="filter-section">
                <el-radio-group v-model="contentType" size="small">
                    <el-radio-button label="all">全部</el-radio-button>
                    <el-radio-button label="text">文本</el-radio-button>
                    <el-radio-button label="image">图片</el-radio-button>
                    <el-radio-button label="audio">音频</el-radio-button>
                    <el-radio-button label="video">视频</el-radio-button>
                    <el-radio-button label="file">文件</el-radio-button>
                    <el-radio-button label="other">其他</el-radio-button>
                </el-radio-group>
                <el-divider direction="vertical"></el-divider>
                <el-radio-group v-model="chatType" size="small">
                    <el-radio-button label="all">全部</el-radio-button>
                    <el-radio-button label="private">单聊</el-radio-button>
                    <el-radio-button label="group">群聊</el-radio-button>
                </el-radio-group>
            </div>
            <div class="search-area">
                <el-input v-model="searchKeyword" placeholder="请输入关键字搜索" size="small" clearable @clear="handleSearch"
                    style="width: 200px; margin-right: 10px">
                    <template #suffix>
                        <i class="el-icon-search"></i>
                    </template>
                </el-input>
                <el-button type="primary" size="small" @click="handleSearch">
                    <i class="el-icon-search"></i> 搜索
                </el-button>
            </div>
        </div>

        <el-table :data="filteredMessages" stripe style="width: 100%;border: solid 1px #e3e3e3;"
            :height="'calc(100vh - 200px)'" :header-cell-style="{ 'background-color': '#f5f7fa', 'color': '#909399' }"
            v-loading="loading" class="small-font-table" :scrollbar-always-on="true">
            <el-table-column prop="id" label="id" width="96" sortable fixed></el-table-column>
            <el-table-column prop="content" label="内容" width="280" show-overflow-tooltip>
                <template #default="scope">
                    <!-- 图片消息显示缩略图 -->
                    <template v-if="scope.row.type === 1">
                        <el-image 
                            style="width: 50px; height: 50px; border-radius: 4px;"
                            :src="parseContent(scope.row.content).thumbUrl"
                            :preview-src-list="[parseContent(scope.row.content).originUrl]"
                            fit="cover"
                            :preview-teleported="true"
                        >
                            <template #error>
                                <div class="image-error">
                                    <i class="el-icon-picture-outline"></i>
                                </div>
                            </template>
                        </el-image>
                    </template>
                    <!-- 表情消息显示 -->
                    <template v-else-if="isEmojiMessage(scope.row.content)">
                        <div class="emoji-content">
                            <img 
                                :src="getEmojiUrl(scope.row.content)" 
                                :alt="getEmojiName(scope.row.content)"
                                class="emoji-image"
                            />
                            <span>{{ getEmojiName(scope.row.content) }}</span>
                        </div>
                    </template>
                    <!-- 音频消息显示链接 -->
                    <template v-else-if="scope.row.type === 3">
                        <div class="audio-content">
                            <i class="el-icon-headset"></i>
                            <a :href="parseContent(scope.row.content).url" target="_blank">
                                音频文件 ({{ parseContent(scope.row.content).duration }}秒)
                            </a>
                        </div>
                    </template>
                    <!-- 文件消息显示链接 -->
                    <template v-else-if="scope.row.type === 2">
                        <div class="file-content">
                            <i 
                                :class="getFileIcon(getFileName(parseContent(scope.row.content).url)).icon"
                                :style="{ color: getFileIcon(getFileName(parseContent(scope.row.content).url)).color }"
                            ></i>
                            <a 
                                href="javascript:;" 
                                @click="downloadFile(
                                    parseContent(scope.row.content).url, 
                                    getFileName(parseContent(scope.row.content).url)
                                )"
                            >
                                {{ getFileName(parseContent(scope.row.content).url) }}
                            </a>
                        </div>
                    </template>
                    <!-- 其他类型消息显示文本内容 -->
                    <template v-else>
                        {{ scope.row.content }}
                    </template>
                </template>
            </el-table-column>
            <el-table-column prop="type" label="类型" width="90">
                <template #default="scope">
                    <el-tag :type="getTypeTag(scope.row.type)">
                        {{ messageTypes[scope.row.type] || '提示消息' }}
                    </el-tag>
                </template>
            </el-table-column>
            <el-table-column prop="sendTime" label="发送时间" width="200" sortable></el-table-column>
            <el-table-column prop="sender" label="发送对象" width="130"></el-table-column>
            <el-table-column prop="receiver" label="接收对象" width="130"></el-table-column>
            <el-table-column fixed="right" label="操作" width="160">
                <template #default="scope">
                    <div class="operation-buttons">
                        <el-button size="small" type="text" @click="handleBlock(scope.row)">屏蔽</el-button>
                        <el-divider direction="vertical"></el-divider>
                        <el-button size="small" type="text" @click="handleDelete(scope.row)"
                            class="delete-button">删除</el-button>
                    </div>
                </template>
            </el-table-column>
        </el-table>

        <!-- 添加屏蔽确认对话框 -->
        <el-dialog
            :visible.sync="blockDialogVisible"
            width="400px"
            :close-on-click-modal="false"
        >
            <div class="dialog-content">
                <p>确定要屏蔽该消息吗？</p>
                <p class="dialog-info">屏蔽后该消息将不会显示给用户。</p>
            </div>
            <template #footer>
                <span class="dialog-footer">
                    <el-button @click="blockDialogVisible = false">取 消</el-button>
                    <el-button type="warning" @click="confirmBlock">确 定</el-button>
                </span>
            </template>
        </el-dialog>

        <!-- 添加删除确认对话框 -->
        <el-dialog
            :visible.sync="deleteDialogVisible"
            width="400px"
            :close-on-click-modal="false"
        >
            <div class="dialog-content">
                <p>确定要删除该消息吗？</p>
                <p class="dialog-info">删除后不可恢复，请谨慎操作！</p>
            </div>
            <template #footer>
                <span class="dialog-footer">
                    <el-button @click="deleteDialogVisible = false">取 消</el-button>
                    <el-button type="danger" @click="confirmDelete">确 定</el-button>
                </span>
            </template>
        </el-dialog>
    </div>
</template>

<script>
import dayjs from 'dayjs'  // 导入 dayjs
import emotion from '@/api/emotion'  // 导入表情包API

export default {
    name: 'AdminMessage',
    data() {
        return {
            contentType: 'all',
            chatType: 'all',
            searchKeyword: '',
            messages: [], // 消息列表
            loading: false,
            privateMessages: [], // 私聊消息
            groupMessages: [], // 群聊消息
            // 定义消息类型映射
            messageTypes: {
                0: '文字消息',
                1: '图片消息',
                2: '文件消息',
                3: '语音消息',
                4: '提示消息'  // 添加提示消息类型
            },
            blockDialogVisible: false,
            deleteDialogVisible: false,
            currentMessage: null  // 当前操作的消息
        }
    },
    methods: {
        getTypeTag(type) {
            const types = {
                0: '',        // 文字消息
                1: 'success', // 图片消息
                2: 'primary', // 文件消息
                3: 'warning', // 语音消息
                4: 'info'     // 提示消息
            }
            return types[type] || 'info'  // 默认使用 info 类型
        },
        // 加载私聊消息
        async loadPrivateMessages() {
            try {
                const res = await this.$http({
                    url: '/message/private/list',
                    method: 'get'
                })
                if (res) {
                    this.privateMessages = res
                }
            } catch (error) {
                console.error('获取私聊消息列表失败:', error)
                this.$message.error('获取私聊消息列表失败')
            }
        },

        // 加载群聊消息
        async loadGroupMessages() {
            try {
                const res = await this.$http({
                    url: '/message/group/list',
                    method: 'get'
                })
                if (res) {
                    this.groupMessages = res
                }
            } catch (error) {
                console.error('获取群聊消息列表失败:', error)
                this.$message.error('获取群聊消息列表失败')
            }
        },

        // 加载所有消息
        async loadMessages() {
            try {
                this.loading = true
                await Promise.all([
                    this.loadPrivateMessages(),
                    this.loadGroupMessages()
                ])
                // 合并消息列表并格式化时间
                this.messages = [
                    ...this.privateMessages.map(msg => ({
                        ...msg,
                        chatType: 'private',
                        sendTime: msg.sendTime ? dayjs(msg.sendTime).format('YYYY-MM-DD HH:mm:ss') : ''
                    })),
                    ...this.groupMessages.map(msg => ({
                        ...msg,
                        chatType: 'group',
                        sendTime: msg.sendTime ? dayjs(msg.sendTime).format('YYYY-MM-DD HH:mm:ss') : ''
                    }))
                ]
            } catch (error) {
                console.error('获取消息列表失败:', error)
                this.$message.error('获取消息列表失败')
            } finally {
                this.loading = false
            }
        },

        // 修改筛选逻辑
        filterMessages() {
            let filteredMessages = [...this.messages]

            // 按聊天类型筛选
            if (this.chatType !== 'all') {
                filteredMessages = filteredMessages.filter(msg => msg.chatType === this.chatType)
            }

            // 按内容类型筛选
            if (this.contentType !== 'all') {
                const typeMap = {
                    'text': 0,
                    'image': 1,
                    'file': 2,
                    'audio': 3,
                    'other': 4  // 添加其他类型的映射
                }
                filteredMessages = filteredMessages.filter(msg => {
                    if (this.contentType === 'other') {
                        // 如果选择"其他"，显示除了已知类型之外的所有消息
                        return ![0, 1, 2, 3].includes(msg.type)
                    }
                    return msg.type === typeMap[this.contentType]
                })
            }

            // 按关键字搜索
            if (this.searchKeyword) {
                const keyword = this.searchKeyword.toLowerCase()
                filteredMessages = filteredMessages.filter(msg =>
                    msg.content.toLowerCase().includes(keyword) ||
                    msg.sender.toLowerCase().includes(keyword) ||
                    msg.receiver.toLowerCase().includes(keyword)
                )
            }

            return filteredMessages
        },
        handleSearch() {
            // TODO: 实现搜索功能
            this.loadMessages()
        },
        handleBlock(row) {
            this.currentMessage = row
            this.blockDialogVisible = true
        },
        handleDelete(row) {
            this.currentMessage = row
            this.deleteDialogVisible = true
        },
        async confirmBlock() {
            try {
                this.loading = true
                // TODO: 调用屏蔽消息的 API
                await this.$http({
                    url: '/message/block',
                    method: 'post',
                    data: {
                        id: this.currentMessage.id
                    }
                })
                this.$message.success('屏蔽成功')
                this.blockDialogVisible = false
                this.loadMessages()  // 重新加载消息列表
            } catch (error) {
                console.error('屏蔽消息失败:', error)
                this.$message.error('屏蔽消息失败')
            } finally {
                this.loading = false
            }
        },
        async confirmDelete() {
            try {
                this.loading = true
                // TODO: 调用删除消息的 API
                await this.$http({
                    url: '/message/delete',
                    method: 'delete',
                    params: {
                        id: this.currentMessage.id
                    }
                })
                this.$message.success('删除成功')
                this.deleteDialogVisible = false
                this.loadMessages()  // 重新加载消息列表
            } catch (error) {
                console.error('删除消息失败:', error)
                this.$message.error('删除消息失败')
            } finally {
                this.loading = false
            }
        },
        // 解析JSON内容
        parseContent(content) {
            try {
                const parsed = typeof content === 'string' ? JSON.parse(content) : content
                // 如果是图片消息，返回包含原图和缩略图URL的对象
                if (parsed.originUrl && parsed.thumbUrl) {
                    return {
                        originUrl: parsed.originUrl,
                        thumbUrl: parsed.thumbUrl
                    }
                }
                // 如果是音频消息，返回包含音频URL和时长的对象
                if (parsed.url && parsed.duration !== undefined) {
                    return {
                        url: parsed.url,
                        duration: parsed.duration
                    }
                }
                // 其他情况返回原始解析结果
                return parsed
            } catch (e) {
                return { url: '', duration: 0, originUrl: '', thumbUrl: '' }
            }
        },

        // 从URL中获取文件名
        getFileName(url) {
            if (!url) return '未知文件'
            const parts = url.split('/')
            return parts[parts.length - 1]
        },

        // 判断是否为表情消息
        isEmojiMessage(content) {
            return /^#[\u4E00-\u9FA5]{1,3};$/.test(content)
        },

        // 获取表情名称
        getEmojiName(content) {
            return content.replace(/^#(.*?);$/, '$1')
        },

        // 获取表情图片URL
        getEmojiUrl(content) {
            return emotion.textToUrl(content)
        },

        // 获取文件图标和颜色
        getFileIcon(fileName) {
            const extension = fileName.split('.').pop().toLowerCase()
            
            // 文档类型
            if (['doc', 'docx'].includes(extension)) {
                return { icon: 'el-icon-document', color: '#4285f4' }  // Word文档
            }
            if (['xls', 'xlsx'].includes(extension)) {
                return { icon: 'el-icon-tickets', color: '#34a853' }   // Excel表格
            }
            if (['ppt', 'pptx'].includes(extension)) {
                return { icon: 'el-icon-collection', color: '#fbbc05' } // PPT演示
            }
            if (extension === 'pdf') {
                return { icon: 'el-icon-document-copy', color: '#ea4335' } // PDF文档
            }
            if (extension === 'txt') {
                return { icon: 'el-icon-document', color: '#666666' }   // 文本文件
            }

            // 图片类型
            if (['jpg', 'jpeg', 'png', 'gif', 'bmp', 'webp'].includes(extension)) {
                return { icon: 'el-icon-picture', color: '#34a853' }
            }

            // 音频类型
            if (['mp3', 'wav', 'ogg', 'flac', 'm4a'].includes(extension)) {
                return { icon: 'el-icon-headset', color: '#fbbc05' }
            }

            // 视频类型
            if (['mp4', 'avi', 'mov', 'wmv', 'flv', 'mkv'].includes(extension)) {
                return { icon: 'el-icon-video-camera', color: '#ea4335' }
            }

            // 压缩文件
            if (['zip', 'rar', '7z', 'tar', 'gz'].includes(extension)) {
                return { icon: 'el-icon-folder', color: '#607d8b' }
            }

            // 代码文件
            if (['js', 'java', 'py', 'cpp', 'c', 'php', 'html', 'css'].includes(extension)) {
                return { icon: 'el-icon-edit-outline', color: '#ff9800' }
            }

            // 默认图标
            return { icon: 'el-icon-document', color: '#909399' }
        },

        // 下载文件
        downloadFile(url, fileName) {
            if (url) {
                const link = document.createElement('a')
                link.href = url
                link.target = '_blank'
                link.download = fileName || this.getFileName(url)
                document.body.appendChild(link)
                link.click()
                document.body.removeChild(link)
            }
        }
    },
    computed: {
        // 过滤后的消息列表
        filteredMessages() {
            return this.filterMessages()
        }
    },
    watch: {
        // 监听筛选条件变化
        chatType() {
            this.loadMessages()
        },
        contentType() {
            this.loadMessages()
        },
        searchKeyword() {
            this.loadMessages()
        }
    },
    mounted() {
        this.loadMessages()
    }
}
</script>

<style lang="scss" scoped>
.admin-message {
    .operation-bar {
        margin-bottom: 20px;
        display: flex;
        justify-content: space-between;
        align-items: center;

        .filter-section {
            display: flex;
            align-items: center;
            gap: 10px;

            .el-radio-group {
                line-height: 1;
            }

            .el-divider--vertical {
                height: 20px;
                margin: 0 5px;
            }
        }

        .search-area {
            display: flex;
            align-items: center;
        }
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

        .delete-button {
            color: #F56C6C;
        }
    }

    // 表格样式
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

    .image-error {
        width: 50px;
        height: 50px;
        display: flex;
        justify-content: center;
        align-items: center;
        background-color: #f5f7fa;
        color: #909399;
        font-size: 20px;
        border-radius: 4px;
    }

    // 添加图片预览相关样式
    :deep(.el-image) {
        cursor: pointer;
        transition: all 0.3s;

        &:hover {
            opacity: 0.8;
        }
    }

    .audio-content, .file-content {
        display: flex;
        align-items: center;
        gap: 5px;

        i {
            font-size: 16px;
            color: #909399;
        }

        a {
            color: #409EFF;
            text-decoration: none;
            &:hover {
                text-decoration: underline;
            }
        }
    }

    .emoji-content {
        display: flex;
        align-items: center;
        gap: 8px;
        color: #606266;

        .emoji-image {
            width: 24px;
            height: 24px;
            object-fit: contain;
        }

        span {
            color: #606266;
            font-size: 13px;
        }
    }

    .dialog-content {
        padding: 20px 0;
        text-align: center;

        p {
            margin: 0;
            line-height: 1.8;

            &.dialog-info {
                font-size: 13px;
                color: #909399;
                margin-top: 10px;
            }
        }
    }

    .dialog-footer {
        text-align: right;
    }

    .file-content {
        i {
            font-size: 18px;  // 稍微调大图标尺寸
            transition: all 0.3s;
        }

        &:hover i {
            transform: scale(1.1);  // 悬停时图标缩放效果
        }

        a {
            cursor: pointer;  // 添加鼠标手型
        }
    }
}
</style>