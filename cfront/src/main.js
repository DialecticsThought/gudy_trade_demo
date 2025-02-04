import Vue from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'

//导入默认的样式和整个依赖包
import ElementUI from 'element-ui'
import 'element-ui/lib/theme-chalk/index.css'
import  VueBus from 'vue-bus'
Vue.use(VueBus);
Vue.use(ElementUI);

Vue.config.productionTip = false

// 	因为是委托终端,一打开就要去连了,
// 	所以我们要把这部分连接的代码放在Main.js里面。
// 	也就是在这个View创建的时候。
// 	这个View创建出来之后,
// 	我们需要把EventBus绑定到这个View上。
// 	先导入EventBus的依赖,
// 	然后对EventBus指定一些连接的配置。

new Vue({
  router,
  store,
  render: h => h(App)
}).$mount('#app')
