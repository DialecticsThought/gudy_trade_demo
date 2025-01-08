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

new Vue({
  router,
  store,
  render: h => h(App)
}).$mount('#app')
