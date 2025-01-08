<template>
  <!-- 每一个template都有一个跟标签 -->
  <div>
    <!--    引入头部导航栏-->
    <v-header></v-header>
    <!--  用来显示嵌入子页面 和router/index.js相对应-->
    <router-view></router-view>
  </div>

</template>

<script>
// 以为内默认也有Header组件，自定义标签不能再用相同的名字
// 导入Header组件
import vHeader from '../components/Header.vue'

export default { // 定义一个Vue组件
  name: 'Home',//设置组件的名称为Hom
  data() {//组件的数据
    return {
      collapse: false,// collapse变量用来控制某些内容的展开/折叠状态
    }
  },
  components: { // 注册组件
    vHeader,  // 注册vHeader组件
    vSidebar // 注册vSidebar组件
  },
  created() {// 组件创建时调用的钩子函数，主要用于初始化事件监听器和其他操作。
    console.log(sessionStorage.getItem("uid"));// 从sessionStorage获取uid并打印
    // 监听“collapse-content”事件，当事件触发时，更新collapse变量
    this.$bus.on("collapse-content", msg => {
      this.collapse = msg;
    });
    // 监听“tradechange”事件，当事件触发时，处理返回的交易数据
    this.$bus.on("tradechange", res => {
      // 解析交易数据
      let jres = JSON.parse(res);
      // 格式化并拼接消息
      let msg = "已成: " + (jres.direction == "BUY" ? "买入  " : "卖出  ")
          + codeFormat(jres.code) + "  " + jres.volume + "股";
      // 显示交易完成的通知
      this.$notify({
        title: '新成交',// 通知标题
        message: msg,// 通知内容
        position: 'bottom-right', // 通知显示位置
        type: 'success' // 通知类型
      })
    })
  },
  beforeDestroy() { // 组件销毁前的钩子函数
    // 移除监听的“collapse-content”事件
    this.$bus.off("collapse-content", msg => {
      this.collapse = msg;
    });
  },
  eventbus: {// 定义一个事件总线对象
    handlers: [// 定义事件处理函数
      {
        // 监听订单变化的事件地址
        address: 'orderchange-' + sessionStorage.getItem("uid"),
        // 事件的头部信息
        headers: {},
        callback: function (err, msg) { // 事件回调函数
          // 打印收到订单变化消息
          console.log("recv order change");
          // 调用查询方法刷新相关数据
          queryOrder();

          queryTrade();

          queryPosi();

          queryBalance();
        },

      },
      {
        // 监听交易变化的事件地址
        address: 'tradechange-' + sessionStorage.getItem("uid"),
        headers: {}, // 事件的头部信息
        callback: function (err, msg) {// 事件回调函数
          vue.$bus.emit("tradechange", msg.body);// 触发“tradechange”事件并传递消息
        },
      }
    ]
  }
}
</script>
