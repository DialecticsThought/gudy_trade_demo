import Vue from 'vue'
import VueRouter from 'vue-router'
import HomeView from '../views/HomeView.vue'

Vue.use(VueRouter)

const routes = [
  {
    //默认的跳转地址变成登录页面
    path: '/',
    name: 'Login',
    component: () => import('../views/Login.vue')
  },
  {
    //默认的跳转地址变成登录页面
    path: '/',
    name: 'Home',
    component: () => import('../views/Home.vue'),
    // 子路由
    children: [
      {
        //默认的跳转地址变成登录页面
        path: '/dashboard',
        name: 'Dashboard',
        component: () => import('../views/Dashboard.vue')
      },
      {
        path: '/pwdsetting',
        name: 'PwdSetting',
        component: () => import('../views/PwdSetting.vue'),
        meta: {requiredAuth: false}
      },
      {
        path: '/transfer',
        name: 'Transfer',
        component: () => import('../views/Transfer.vue'),
        meta: {requiredAuth: false}
      },
      {
        path: '/orderquery',
        name: 'OrderQuery',
        component: () => import('../views/OrderQuery.vue'),
        meta: {requiredAuth: false}
      },
      {
        path: '/tradequery',
        name: 'TradeQuery',
        component: () => import('../views/TradeQuery.vue'),
        meta: {requiredAuth: false}
      },
      {
        path: '/hisorderquery',
        name: 'HisOrderQuery',
        component: () => import('../views/HisOrderQuery.vue'),
        meta: {requiredAuth: false}
      },
      {
        path: '/buy',
        name: 'Buy',
        component: () => import('../views/Buy.vue'),
        meta: {requiredAuth: false}
      },
      {
        path: '/sell',
        name: 'Sell',
        component: () => import('../views/Sell.vue'),
        meta: {requiredAuth: false}
      },
    ]
  },
  {
    //默认的跳转地址变成登录页面
    path: '/404',
    name: '404',
    component: () => import('../views/404.vue')
  },
  {
    //前面所有的路由都不无法找到 直接来到404
    path: '/*',
    name: '404',
    component: () => import('../views/404.vue')
  },
  {
    path: '/about',
    name: 'about',
    // route level code-splitting
    // this generates a separate chunk (about.[hash].js) for this route
    // which is lazy-loaded when the route is visited.
    component: () => import(/* webpackChunkName: "about" */ '../views/AboutView.vue')
  }
]

const router = new VueRouter({
  mode: 'history',
  base: process.env.BASE_URL,
  routes
})
//路由拦截器 它会在每次路由切换时被调用
//beforeEach 是 Vue Router 提供的一个导航守卫，它允许你在路由跳转之前进行一些操作（例如验证权限、检查登录状态等）
// to：目标路由对象，包含路由的相关信息，例如路径、路由参数和 meta 信息。
// from：当前路由对象，表示导航离开的路由信息。
// next：导航守卫的钩子函数，控制路由的继续或中断。
router.beforeEach((to,from,next) =>{
  // 判断目标路由（to）是否需要授权
  if(to.meta.requiredAuth){
    // 如果获取浏览器sessionStorage中存在uid（用户已经登录），继续导航
    if(Boolean(sessionStorage.getItem("uid"))){
      next(); // 调用next()，允许导航到目标路由
    }else {
      // 如果没有登录（uid不存在），则重定向到首页（'/'）
      next({
        path: '/',
      })
    }
  }else {
    // 如果目标路由不需要授权 即 requiredAuth 为 false 或未设置，直接进行导航
    next();
  }
});

export default router
