<template>
  <div>
    <!-- 信息提示-->
    <div class="crumbs">
      <!--          <el-breadcrumb separator="/">: Element UI 提供的面包屑导航组件。
                  separator="/"：设置面包屑项之间的分隔符为 /-->
      <el-breadcrumb separator="/">
        <!--
            第一个 el-breadcrumb-item 显示的是图标和文字“查询”。
            <i class="el-icon-s-order"></i>: 使用 Element UI 提供的图标类 el-icon-s-order，显示一个订单相关的图标
        -->
        <el-breadcrumb-item>
          <i class="el-icon-s-order"></i> 查询
        </el-breadcrumb-item>
        <!--              第二个 el-breadcrumb-item 显示的是文字“当日委托”-->
        <el-breadcrumb-item>当日委托</el-breadcrumb-item>
      </el-breadcrumb>
    </div>
    <!--    <el-card>: Element UI 的卡片组件，用来包裹内容并展示一个卡片样式。
          shadow="hover"：为卡片添加悬浮阴影效果，即当鼠标悬停在卡片上时，卡片会显示阴影。
          class="container"：为 el-card 添加 container 类，通常用于自定义样式。-->
    <el-card shadow="hover" class="container">
      <!--      这是一个自定义的子组件 OrderList，在这个卡片内部渲染，用于显示当日的订单列表-->
      <order-list/>
    </el-card>

  </div>
</template>

<script>
// 使用 import 语法导入 OrderList 组件。它位于 ../components/OrderList 路径下。
// 该组件用于显示订单列表，是这个页面的一个子组件
import OrderList from '../components/OrderList'

export default {
  name: "OrderQuery",// 这是当前 Vue 组件的名称，指定该组件的名称为 OrderQuery
  components: {//这里声明了当前组件依赖的子组件 OrderList，它会被注册到当前组件中，并可以在模板中使用
    OrderList,
  }
}
</script>

<style scoped>

</style>
