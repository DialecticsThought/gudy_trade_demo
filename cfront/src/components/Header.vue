<template>
  <div class="header">
    <!-- 第一步 折叠按钮 -->
    <!--        <div class="collapse-btn">-->
    <!--            <i class="el-icon-s-unfold" />-->
    <!--        </div>-->

    <!-- 第二步 折叠按钮 -->
    <!--  当点击的时候触发 修改状态值的方法    -->
    <div class="collapse-btn" @click="collapseChage">
      <!--   折叠 按钮   情况1 查看标志位collapse=1 -->
      <i v-if="!collapse" class="el-icon-s-fold"></i>
      <!-- 情况2 -->
      <i v-else class="el-icon-s-unfold"></i>
    </div>

    <!-- 第三步 LOGO -->
    <div class="logo">Gudy证券交易系统</div>

    <!-- 第四步  右侧下拉菜单-->
    <div class="header-right">
      <div class="header-user-con">
        <!--       下拉菜单       -->
        <!--    出发下拉菜单的方式 = click 也就是点击            -->
        <el-dropdown class="user-name" trigger="click" @command="handleCommand">
          <!--               下拉菜单的提示信息   这里是用户信息作为提示信息   -->
          <span class="el-dropdown-link">
                        {{ username }}
            <!-- 下拉箭头-->
                        <i class="el-icon-caret-bottom"></i>
                    </span>
          <!--具体的下拉菜单                   -->
          <el-dropdown-menu slot="dropdown">
            <!-- 用command属性代表单击的具体执行内容   也就是方法  -->
            <el-dropdown-item command="loginout">退出登录</el-dropdown-item>
          </el-dropdown-menu>
        </el-dropdown>
      </div>
    </div>

  </div>
</template>
<script>

export default {
  data() {
    return {
      collapse: true,// 表示侧边栏是否折叠，默认为折叠
      fullscreen: false,// 全屏状态，默认为不全屏
      message: 2// 存储消息的数量，初始值为 2
    };
  },
  computed: {// computed 属性用于定义依赖于其他数据的计算属性
    // 计算属性：获取当前用户名
    username() {
      let acc = sessionStorage.getItem("uid");// 从 sessionStorage 中获取 "uid"（用户 ID）
      return acc ? acc : "guest";  // 如果有获取到 "uid" 值，返回该值；否则返回 "guest"
    }
  },
  methods: {
    // 用户名下拉菜单选择事件
    handleCommand(command) {
      if (command == 'loginout') {// 如果选择了 "loginout" 命令（退出登录）
        //TODO logout
        // logout();
      }
    },
    // 点某一个按钮 侧边栏折叠
    // 控制侧边栏折叠或展开的按钮点击事件处理
    collapseChange() {
      this.collapse = !this.collapse; // 切换 collapse 状态（折叠或展开）
      // 往消息总线中 发一个消息 消息名字 = collapse 值 = this.collapse
      // sideBar.vue和Home.vue来接收消息
      this.$bus.emit('collapse', this.collapse);
    },
  },

  // 接受Header.vue的消息
  // 订阅消息

  // 第五步  右侧下拉菜单
  // mounted() 生命周期钩子，当组件挂载到 DOM 后执行
  mounted() {
    // 如果页面的宽度小于 1500px
    if (document.body.clientWidth < 1500) {
      this.collapseChange();// 自动折叠侧边栏
    }
  }
};
</script>
<style scoped>
.header {
  position: relative;
  box-sizing: border-box;
  width: 100%;
  height: 70px;
  font-size: 22px;
  color: #fff;
}

.collapse-btn {
  float: left;
  padding: 0 21px;
  cursor: pointer;
  line-height: 70px;
}

.header .logo {
  float: left;
  width: 250px;
  line-height: 70px;
}

.header-right {
  float: right;
  padding-right: 50px;
}

.header-user-con {
  display: flex;
  height: 70px;
  align-items: center;
}

.btn-bell .el-icon-bell {
  color: #fff;
}

.user-name {
  margin-left: 10px;
}

.user-avator {
  margin-left: 20px;
}

.user-avator img {
  display: block;
  width: 40px;
  height: 40px;
  border-radius: 50%;
}

.el-dropdown-link {
  color: #fff;
  cursor: pointer;
}

.el-dropdown-menu__item {
  text-align: center;
}
</style>
