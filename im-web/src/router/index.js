import Vue from 'vue'
import VueRouter from 'vue-router'
import Login from '../view/Login'
import Register from '../view/Register'
import Home from '../view/Home'
import AdminLayout from '@/views/admin/AdminLayout'
import AdminIndex from '@/views/admin/Index'
import AdminFile from '@/views/admin/File'
import AdminMessage from '@/views/admin/Message'
import AdminGroup from '@/views/admin/Group'
import AdminUser from '@/views/admin/User'
import AdminSetting from '@/views/admin/Setting'
// 安装路由
Vue.use(VueRouter);

// 配置导出路由
export default new VueRouter({
  routes: [{
    path: "/",
    redirect: "/login"
  },
  {
    name: "Login",
    path: '/login',
    component: Login
  },
  {
    name: "Register",
    path: '/register',
    component: Register
  },
  {
    name: "Home",
    path: '/home',
    component: Home,
    children: [
      {
        name: "Chat",
        path: "/home/chat",
        component: () => import("../view/Chat"),
      },
      {
        name: "Friend",
        path: "/home/friend",
        component: () => import("../view/Friend"),
      },
      {
        name: "GROUP",
        path: "/home/group",
        component: () => import("../view/Group"),
      }
    ]
  },
  {
    path: '/admin',
    component: AdminLayout,
    children: [
      {
        path: '',
        redirect: '/admin/index'
      },
      {
        path: 'index',
        name: 'AdminIndex',
        component: AdminIndex
      },
      {
        path: 'file',
        name: 'AdminFile',
        component: AdminFile
      },
      {
        path: 'message',
        name: 'AdminMessage',
        component: AdminMessage
      },
      {
        path: 'group',
        name: 'AdminGroup',
        component: AdminGroup
      },
      {
        path: 'user',
        name: 'AdminUser',
        component: AdminUser
      },
      {
        path: 'setting',
        name: 'AdminSetting',
        component: AdminSetting
      }
    ]
  }
  ]
});
