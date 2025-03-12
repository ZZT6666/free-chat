<template>
    <el-dialog :title="title" :visible.sync="dialogVisible" width="600px" :before-close="handleClose">
        <div class="choose-container">
            <div class="search-box">
                <el-input v-model="searchKey" placeholder="搜索" prefix-icon="el-icon-search" clearable>
                </el-input>
            </div>
            <el-scrollbar style="height: 400px;">
                <div v-for="chat in filteredChats" :key="chat.id" class="chat-item" @click="handleSelect(chat)">
                    <head-image :size="40" :url="chat.headImage" :name="chat.showName"></head-image>
                    <div class="chat-info">
                        <div class="chat-name">{{ chat.showName }}</div>
                        <div class="chat-type">{{ chat.type === 'GROUP' ? '群聊' : '私聊' }}</div>
                    </div>
                </div>
            </el-scrollbar>
        </div>
    </el-dialog>
</template>

<script>
import HeadImage from './HeadImage.vue'

export default {
    name: 'ChooseDialog',
    components: {
        HeadImage
    },
    props: {
        visible: {
            type: Boolean,
            default: false
        },
        title: {
            type: String,
            default: '选择'
        },
        allUser: {
            type: Array,
            default: () => []
        }
    },
    data() {
        return {
            searchKey: ''
        }
    },
    computed: {
        dialogVisible: {
            get() {
                return this.visible
            },
            set(val) {
                this.$emit('update:visible', val)
            }
        },
        filteredChats() {
            return this.allUser.filter(chat => {
                const matchSearch = !this.searchKey ||
                    chat.showName.toLowerCase().includes(this.searchKey.toLowerCase())
                return matchSearch
            })
        }
    },
    methods: {
        handleClose() {
            this.searchKey = ''
            this.$emit('update:visible', false)
        },
        handleSelect(chat) {
            this.$emit('selectChat', chat)
            this.handleClose()
        }
    }
}
</script>

<style lang="scss" scoped>
.choose-container {
    .search-box {
        margin-bottom: 15px;
    }

    .chat-item {
        display: flex;
        align-items: center;
        padding: 10px;
        cursor: pointer;

        &:hover {
            background-color: #f5f7fa;
        }

        .chat-info {
            margin-left: 10px;
            flex: 1;

            .chat-name {
                font-size: 14px;
                color: #303133;
                margin-bottom: 4px;
            }

            .chat-type {
                font-size: 12px;
                color: #909399;
            }
        }
    }
}
</style>