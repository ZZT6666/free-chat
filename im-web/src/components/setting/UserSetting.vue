<template>
  <el-dialog class="user-setting" title="系统设置" :visible.sync="visible" width="420px" :before-close="onClose" :close-on-click-modal="true" :close-on-press-escape="true">
    <el-tabs v-model="activeTab">
      <!-- 修改密码面板 -->
      <el-tab-pane label="修改密码" name="password">
        <el-form :model="passwordForm" :rules="passwordRules" ref="passwordForm" label-width="80px" size="small">
          <el-form-item label="原密码" prop="oldPassword">
            <el-input v-model="passwordForm.oldPassword" type="password" show-password></el-input>
          </el-form-item>
          <el-form-item label="新密码" prop="newPassword">
            <el-input v-model="passwordForm.newPassword" type="password" show-password></el-input>
          </el-form-item>
          <el-form-item label="确认密码" prop="confirmPassword">
            <el-input v-model="passwordForm.confirmPassword" type="password" show-password></el-input>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="updatePassword">修改密码</el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>

      <!-- 通知设置面板 -->
      <el-tab-pane label="通知设置" name="notification">
        <el-form :model="notificationForm" label-width="100px" size="small">
          <el-form-item label="消息通知">
            <el-switch v-model="notificationForm.messageNotification"></el-switch>
          </el-form-item>
          <el-form-item label="声音提醒">
            <el-switch v-model="notificationForm.soundNotification"></el-switch>
          </el-form-item>
          <el-form-item label="桌面通知">
            <el-switch v-model="notificationForm.desktopNotification"></el-switch>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="updateNotificationSettings">保存设置</el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>

      <!-- 隐私设置面板 -->
      <el-tab-pane label="隐私设置" name="privacy">
        <el-form :model="privacyForm" label-width="120px" size="small">
          <el-form-item label="允许添加好友">
            <el-switch v-model="privacyForm.allowAddFriend"></el-switch>
          </el-form-item>
          <el-form-item label="显示在线状态">
            <el-switch v-model="privacyForm.showOnlineStatus"></el-switch>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="updatePrivacySettings">保存设置</el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>
    </el-tabs>
  </el-dialog>
</template>

<script>
export default {
  name: "UserSetting",
  data() {
    const validatePass = (rule, value, callback) => {
      if (value === '') {
        callback(new Error('请输入密码'));
      } else {
        if (this.passwordForm.confirmPassword !== '') {
          this.$refs.passwordForm.validateField('confirmPassword');
        }
        callback();
      }
    };
    const validatePass2 = (rule, value, callback) => {
      if (value === '') {
        callback(new Error('请再次输入密码'));
      } else if (value !== this.passwordForm.newPassword) {
        callback(new Error('两次输入密码不一致!'));
      } else {
        callback();
      }
    };

    return {
      activeTab: 'password',
      // 密码表单
      passwordForm: {
        oldPassword: '',
        newPassword: '',
        confirmPassword: ''
      },
      // 密码验证规则
      passwordRules: {
        oldPassword: [
          { required: true, message: '请输入原密码', trigger: 'blur' }
        ],
        newPassword: [
          { required: true, validator: validatePass, trigger: 'blur' },
          { min: 6, message: '密码长度不能小于6位', trigger: 'blur' }
        ],
        confirmPassword: [
          { required: true, validator: validatePass2, trigger: 'blur' }
        ]
      },
      // 通知设置表单
      notificationForm: {
        messageNotification: true,
        soundNotification: true,
        desktopNotification: false
      },
      // 隐私设置表单
      privacyForm: {
        allowAddFriend: true,
        showOnlineStatus: true
      }
    }
  },
  props: {
    visible: {
      type: Boolean,
      default: false
    }
  },
  methods: {
    onClose() {
      this.resetForms();
      this.$emit('update:visible', false);
      this.$emit('close');
    },
    
    // 重置所有表单
    resetForms() {
      this.passwordForm.oldPassword = '';
      this.passwordForm.newPassword = '';
      this.passwordForm.confirmPassword = '';
      if (this.$refs.passwordForm) {
        this.$refs.passwordForm.resetFields();
      }
    },

    // 更新密码
    updatePassword() {
      this.$refs.passwordForm.validate((valid) => {
        if (valid) {
          this.$http({
            url: '/modifyPwd',
            method: 'put',
            data: {
              id: this.$store.state.userStore.userInfo.id,
              oldPassword: this.passwordForm.oldPassword,
              newPassword: this.passwordForm.newPassword
            }
          }).then(() => {
            this.$message.success('密码修改成功，请重新登录');
            this.resetForms();
            // 退出登录
            this.$store.dispatch('logout');
          }).catch(() => {
            this.$message.error('密码修改失败，请检查原密码是否正确');
          });
        }
      });
    },

    // 更新通知设置
    updateNotificationSettings() {
      this.$http({
        url: '/user/notification/settings',
        method: 'put',
        data: this.notificationForm
      }).then(() => {
        this.$message.success('通知设置更新成功');
      });
    },

    // 更新隐私设置
    updatePrivacySettings() {
      this.$http({
        url: '/user/privacy/settings',
        method: 'put',
        data: this.privacyForm
      }).then(() => {
        this.$message.success('隐私设置更新成功');
      });
    }
  }
}
</script>

<style lang="scss" scoped>
.user-setting {
  .el-tabs {
    padding: 0 20px;
  }

  .el-form {
    margin-top: 20px;
  }

  .el-form-item:last-child {
    margin-bottom: 0;
    text-align: right;
  }
}
</style> 