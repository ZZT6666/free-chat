<template>
  <el-container class="chat-page">
    <el-aside width="260px" class="chat-list-box">
      <div class="chat-list-header">
        <el-input class="search-text" size="small" placeholder="搜索" v-model="searchText">
          <i class="el-icon-search el-input__icon" slot="prefix"> </i>
        </el-input>
      </div>
      <div>
        <el-button class="chat-list-btn" :class="{'active-btn': activeTab === 'all'}" @click="activeTab = 'all'"
                   type="success">所有</el-button>
        <el-button class="chat-list-btn" :class="{'active-btn': activeTab === 'unread'}" @click="activeTab = 'unread'"
                   type="success">未读</el-button>
        <el-button class="chat-list-btn" :class="{'active-btn': activeTab === '@me'}" @click="activeTab = '@me'"
                   type="success">@我</el-button>
      </div>
      <div class="chat-list-loading" v-if="loading" v-loading="true" element-loading-text="消息接收中..."
        element-loading-spinner="el-icon-loading" element-loading-background="#F9F9F9" element-loading-size="24">
      </div>
      <el-scrollbar class="chat-list-items" v-else>
        <div v-for="(chat, index) in chatStore.chats" :key="index">
          <chat-item v-show="!chat.delete && chat.showName.includes(searchText) &&
           (activeTab === 'all' || (activeTab === 'unread' && chat.unreadCount!== 0) || activeTab === '@me' && chat.atMe === true || chat.atAll === true )" :chat="chat" :index="index"
            @click.native="onActiveItem(index)" @delete="onDelItem(index)" @top="onTop(index)"
            :active="chat === chatStore.activeChat"></chat-item>
        </div>
      </el-scrollbar>
    </el-aside>
    <el-container class="chat-box">
      <chat-box v-if="chatStore.activeChat" :chat="chatStore.activeChat"></chat-box>
    </el-container>
  </el-container>
</template>

<script>
import ChatItem from "../components/chat/ChatItem.vue";
import ChatBox from "../components/chat/ChatBox.vue";

export default {
  name: "chat",
  components: {
    ChatItem,
    ChatBox
  },
  data() {
    return {
      activeTab: 'all', // 默认激活的是“所有”按钮
      searchText: "",
      messageContent: "",
      group: {},
      groupMembers: []
    }
  },
  methods: {
    onActiveItem(index) {
      this.$store.commit("activeChat", index);
    },
    onDelItem(index) {
      this.$store.commit("removeChat", index);
    },
    onTop(chatIdx) {
      this.$store.commit("moveTop", chatIdx);
    },
  },
  computed: {
    chatStore() {
      return this.$store.state.chatStore;
    },
    loading() {
      return this.chatStore.loadingGroupMsg || this.chatStore.loadingPrivateMsg
    }
  }
}
</script>

<style lang="scss">
.chat-page {
  .chat-list-box {
    display: flex;
    flex-direction: column;
    background: var(--im-background);

    .chat-list-header {
      height: 50px;
      display: flex;
      align-items: center;
      padding: 0 8px;
    }
    .chat-list-btn {
      background-color: transparent; /* 默认背景透明 */
      border-radius: 20px; /* 圆角边框 */
      border: 1px solid rgba(64, 158, 255, 0); /* 边框颜色 */
      margin-right: 10px; /* 按钮间距 */
      color: inherit; /* 保持字体颜色不变 */
      position: relative; /* 为高亮效果做准备 */
    }
    .active-btn {
      background-color: #409EFF; /* 设置点击后的高亮颜色 */
      color: white; /* 字体颜色不变 */
    }
    .chat-list-loading {
      height: 50px;
      background-color: #eee;

      .el-icon-loading {
        font-size: 24px;
        color: var(--im-text-color-light);
      }

      .el-loading-text {
        color: var(--im-text-color-light);
      }

      .chat-loading-box {
        height: 100%;
      }
    }

    .chat-list-items {
      flex: 1;
    }
  }
}
</style>
