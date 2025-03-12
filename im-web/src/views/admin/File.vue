<template>
    <div class="file-manager">
        <div class="file-sidebar">
            <div class="sidebar-item" :class="{ active: currentType === 'all' }" @click="filterByType('all')">
                <i class="fas fa-folder"></i>
                所有文件
            </div>
            <div class="sidebar-item" :class="{ active: currentType === 'document' }" @click="filterByType('document')">
                <i class="fas fa-file-alt" style="color: #4285f4"></i>
                文档
            </div>
            <div class="sidebar-item" :class="{ active: currentType === 'image' }" @click="filterByType('image')">
                <i class="fas fa-file-image" style="color: #34a853"></i>
                图片
            </div>
            <div class="sidebar-item" :class="{ active: currentType === 'audio' }" @click="filterByType('audio')">
                <i class="fas fa-file-audio" style="color: #fbbc05"></i>
                音频
            </div>
            <div class="sidebar-item" :class="{ active: currentType === 'video' }" @click="filterByType('video')">
                <i class="fas fa-file-video" style="color: #ea4335"></i>
                视频
            </div>
            <div class="sidebar-item" :class="{ active: currentType === 'other' }" @click="filterByType('other')">
                <i class="fas fa-file" style="color: #666666"></i>
                其他
            </div>
        </div>
        <div class="file-content">
            <div class="file-grid">
                <div v-for="file in filteredFiles" :key="file.fileName" class="file-item">
                    <!-- 图片类型显示缩略图 -->
                    <template v-if="getFileType(file.fileName).type === 'image'">
                        <div class="file-preview">
                            <el-image 
                                :src="file.url"
                                :preview-src-list="[file.url]"
                                fit="cover"
                                class="preview-image"
                            >
                                <template #error>
                                    <div class="image-error">
                                        <i class="fas fa-file-image"></i>
                                    </div>
                                </template>
                            </el-image>
                        </div>
                    </template>
                    <!-- 其他类型显示图标 -->
                    <template v-else>
                        <div class="file-icon" :style="{ color: getFileType(file.fileName).color }">
                            <i :class="getFileType(file.fileName).icon"></i>
                        </div>
                    </template>
                    <div class="file-info">
                        <div class="file-name" :title="file.fileName">{{ file.fileName }}</div>
                        <div class="file-size">{{ formatFileSize(file.size) }}</div>
                    </div>
                    <div class="file-actions-row">
                        <button @click.stop="viewFile(file)">查看</button>
                        <button @click.stop="downloadFile(file)">下载</button>
                        <button @click.stop="shareFile(file)">发送</button>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
export default {
    name: 'FileManager',
    data() {
        return {
            files: [],
            currentType: 'all'  // 新增：当前选中的文件类型
        }
    },
    computed: {
        filteredFiles() {
            if (this.currentType === 'all') {
                return this.files
            }
            return this.files.filter(file =>
                this.getFileType(file.fileName).type === this.currentType
            )
        }
    },
    methods: {
        getFileType(fileName) {
            const extension = fileName.split('.').pop().toLowerCase()

            // 文档类型
            if (['doc', 'docx', 'pdf', 'txt', 'xls', 'xlsx', 'ppt', 'pptx'].includes(extension)) {
                 // 文档类型
                if (['doc', 'docx'].includes(extension)) {
                    return { type: 'document',icon: 'el-icon-document',color: '#4285f4' }  // Word文档
                }
                if (['xls', 'xlsx'].includes(extension)) {
                    return { type: 'document',icon: 'el-icon-tickets', color: '#34a853' }   // Excel表格
                }
                if (['ppt', 'pptx'].includes(extension)) {
                    return { type: 'document',icon: 'el-icon-collection', color: '#fbbc05' } // PPT演示
                }
                if (extension === 'pdf') {
                    return { type: 'document',icon: 'el-icon-document-copy', color: '#ea4335' } // PDF文档
                }
                if (extension === 'txt') {
                    return { type: 'document',icon: 'el-icon-document', color: '#666666' }   // 文本文件
                }
            }
            // 图片类型
            if (['jpg', 'jpeg', 'png', 'gif', 'bmp', 'webp'].includes(extension)) {
                return {type: 'image',icon: 'el-icon-picture', color: '#34a853'}
            }
            // 音频类型
            if (['mp3', 'wav', 'ogg', 'flac', 'm4a'].includes(extension)) {
                return {type: 'audio',icon: 'el-icon-headset', color: '#fbbc05'}
            }
            // 视频类型
            if (['mp4', 'avi', 'mov', 'wmv', 'flv', 'mkv'].includes(extension)) {
                return { type: 'video',icon: 'el-icon-video-camera', color: '#ea4335' }
            }
             // 压缩文件
             if (['zip', 'rar', '7z', 'tar', 'gz'].includes(extension)) {
                return { icon: 'el-icon-folder', color: '#607d8b' }
            }

            // 代码文件
            if (['js', 'java', 'py', 'cpp', 'c', 'php', 'html', 'css'].includes(extension)) {
                return { icon: 'el-icon-edit-outline', color: '#ff9800' }
            }

            // 其他类型
            return {type: 'other',icon: 'el-icon-document', color: '#909399'}
        },
        async fetchFiles() {
            try {
                const response = await this.$http.get('/file/list')
                this.files = response
            } catch (error) {
                console.error('获取文件列表失败:', error)
            }
        },
        viewFile(file) {
            if (file.url) {
                window.open(file.url, '_blank')
            } else {
                this.$message.warning('文件链接不存在')
            }
        },
        downloadFile(file) {
            if (file.url) {
                // 创建一个隐藏的 a 标签
                const link = document.createElement('a')
                link.href = file.url
                link.target = '_blank'
                link.download = file.fileName || this.getFileName(file.url)  // 设置下载文件名
                document.body.appendChild(link)
                link.click()
                document.body.removeChild(link)
            } else {
                this.$message.warning('文件链接不存在')
            }
        },
        shareFile(file) {
            // 实现分享文件的逻辑
            console.log('分享文件:', file)
        },
        filterByType(type) {
            this.currentType = type
        },
        // 格式化文件大小
        formatFileSize(size) {
            if (!size) return '0 B'
            const units = ['B', 'KB', 'MB', 'GB', 'TB']
            let index = 0
            let fileSize = size

            while (fileSize >= 1024 && index < units.length - 1) {
                fileSize /= 1024
                index++
            }

            return `${fileSize.toFixed(2)} ${units[index]}`
        },
        // 获取文件名
        getFileName(url) {
            if (!url) return '未知文件'
            const parts = url.split('/')
            return parts[parts.length - 1]
        }
    },
    mounted() {
        this.fetchFiles()
    }
}
</script>

<style scoped>
.file-manager {
    display: flex;
    padding: 0;
    height: 100%;
}

.file-sidebar {
    width: 200px;
    background-color: #f8f9fa;
    padding: 20px 0;
    border-right: 1px solid #eee;
}

.sidebar-item {
    padding: 12px 24px;
    cursor: pointer;
    display: flex;
    align-items: center;
    transition: all 0.3s ease;
}

.sidebar-item i {
    margin-right: 12px;
    width: 20px;
    text-align: center;
}

.sidebar-item:hover {
    background-color: #f0f0f0;
}

.sidebar-item.active {
    background-color: #e9ecef;
    font-weight: bold;
}

.file-content {
    flex: 1;
    padding: 20px;
    overflow-y: auto;
}

.file-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
    gap: 15px;
    padding: 20px;
}

.file-item {
    position: relative;
    border: none;
    padding: 15px;
    text-align: center;
    transition: all 0.3s ease;
    display: flex;
    flex-direction: column;
    align-items: center;
    cursor: pointer;
    border-radius: 4px;
    border: 1px solid transparent;
}

.file-icon {
    width: 80px;
    height: 80px;
    display: flex;
    justify-content: center;
    align-items: center;
    margin-bottom: 8px;
    font-size: 40px;
    background-color: #f8f9fa;
    border-radius: 4px;
    transition: all 0.3s;
}

.file-info {
    width: 100%;
    text-align: center;
}

.file-name {
    max-width: 100%;
    font-size: 12px;
    color: #333;
    margin-bottom: 2px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.file-size {
    display: block;
    font-size: 11px;
    color: #999;
    margin-top: 2px;
}

.file-actions-row {
    display: none;
    justify-content: center;
    gap: 5px;
    margin-top: 5px;
    position: absolute;
    bottom: -5px;
    left: 0;
    right: 0;
    background-color: rgba(255, 255, 255, 0.9);
    padding: 5px 0;
}

.file-item:hover .file-actions-row {
    display: flex;
}

.file-actions-row button {
    font-size: 12px;
    padding: 2px 8px;
    border: none;
    background: none;
    color: #666;
    cursor: pointer;
}

.file-actions-row button:hover {
    color: #1890ff;
}

.file-actions {
    display: none;
}

.file-type,
.file-size {
    display: none;
}

.file-item:hover {
    border-color: #e6e6e6;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.file-item:hover .file-icon {
    transform: none;
}

.file-preview {
    width: 80px;
    height: 80px;
    margin-bottom: 8px;
    border-radius: 4px;
    overflow: hidden;
}

.preview-image {
    width: 100%;
    height: 100%;
    object-fit: cover;
}

.image-error {
    width: 100%;
    height: 100%;
    display: flex;
    justify-content: center;
    align-items: center;
    background-color: #f5f7fa;
    color: #909399;
}

:deep(.el-image) {
    width: 100%;
    height: 100%;
}

:deep(.el-image__inner) {
    width: 100%;
    height: 100%;
    object-fit: cover;
}
</style>
