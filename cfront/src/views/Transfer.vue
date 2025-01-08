<template>
  <div>
    <!-- 信息提示-->
    <div class="crumbs">
      <!--      Element UI 提供的面包屑导航组件
            Element UI 提供的面包屑导航组件-->
      <el-breadcrumb separator="/">
        <!--        <el-breadcrumb-item>: 面包屑项，表示一个导航条目-->
        <el-breadcrumb-item>
          <i class="el-icon-baseball"></i> 银证转账
        </el-breadcrumb-item>
      </el-breadcrumb>
    </div>
    <!-- shadow="hover" 表示当鼠标悬停时显示阴影，class="container" 是给卡片组件添加一个 CSS 类   -->
    <el-card shadow="hover" class="container">
      <!--el-row 用于布局，在这里用来包裹整个表单，并通过 style="padding: 0 15%;" 来设置左右内边距        -->
      <el-row style="padding: 0 15%;">
        <!--     el-form 是 Element UI 提供的表单组件，label-width="80px" 设置表单项标签的宽度为 80px。   -->
        <el-form label-width="80px">
          <!--转账方式-->
          <el-form-item label="转账方式">
            <!--            el-form 是 Element UI 提供的表单组件，label-width="80px" 设置表单项标签的宽度为 80px。-->
            <el-select style="width: 100%" v-model="type">
              <!--
               v-for="item in form.type"：遍历 form.type 数组来渲染多个 el-option 选项
               :key="item.id"：为每个选项提供唯一的 key，这是 Vue 渲染列表时的最佳实践。
              :label="item.name"：每个选项的显示文本是 item.name。
              :value="item.id"：选项的值是 item.id，这会被绑定到 v-model="type"-->
              <el-option v-for="item in form.type"
                         :key="item.id"
                         :label="item.name"
                         :value="item.id"/>
            </el-select>
          </el-form-item>
          <!--银行-->
          <el-form-item label="选择银行">
            <el-select style="width: 100%" v-model="bank">
              <el-option v-for="item in form.bank"
                         :key="item.id"
                         :label="item.name"
                         :value="item.name"/>
            </el-select>
          </el-form-item>
          <!--银行密码-->
          <el-form-item label="银行密码">
            <!--            el-input 是输入框组件，type="'password'" 设置为密码输入框-->
            <!--    'password' 的单引号表示是一个变量而不是一个字符串        -->
            <!--            v-model="form.password" 将输入框的值与 form.password 绑定。-->
            <el-input :type="'password'" v-model="form.password"/>
          </el-form-item>
          <!--选择币种-->
          <el-form-item label="选择币种">
            <!--            el-select 用于选择币种，v-model="moneytype" 将选中的币种与 moneytype 绑定-->
            <el-select style="width: 100%" v-model="moneytype">
              <!--              v-for="item in form.moneytype"：循环 form.moneytype 数组，渲染每个币种选项。
                            :key="item.id"：每个选项的唯一标识符。
                            :label="item.name"：选项的显示文本（币种名称）。
                            :value="item.id"：选项的值，绑定到 moneytype 数据中。-->
              <el-option v-for="item in form.moneytype"
                         :key="item.id"
                         :label="item.name"
                         :value="item.id"/>
            </el-select>
          </el-form-item>
          <!--金额-->
          <el-form-item label="金额">
            <!--            v-model="form.money"：双向绑定金额输入值到 form.money。-->
            <el-input v-model="form.money"/>
          </el-form-item>
          <el-form-item>
            <!--            style="float: right;"：使用 CSS 样式将按钮右对齐。
                        size="small"：设置按钮的大小为小型。
                        type="primary"：设置按钮的类型为 primary，表示这是一个主要操作按钮，通常显示为蓝色。-->
            <el-button style="float: right;" size="small" type="primary">转账</el-button>
          </el-form-item>


        </el-form>


      </el-row>

    </el-card>

  </div>
</template>

<script>
export default {
  name: "Transfer",
  data() {
    return {
      // 表单数据
      form: {
        // 转账方式：银行转证券 (转入) 或 证券转银行 (转出)
        type: [{id: 1, name: '银行转证券(转入)'}, {id: 0, name: '证券转银行(转出)'}],
        // 银行选项：招商银行和建设银行
        bank: [{id: 0, name: '招商银行'}, {id: 1, name: '建设银行'}],
        // 币种选项：人民币和美元
        moneytype: [{id: 0, name: '人民币'}, {id: 1, name: '美元'}],
        // 表单字段
        // 用户输入的银行密码
        password: '',
        // 输入的金额
        money: 0,
      },
// 初始化选中的值
      type: 0, // 默认转账方式为“证券转银行”
      bank: '招商银行', // 默认选择“招商银行”
      moneytype: 0, // 默认选择“人民币”
    }
  }
}
</script>

<style scoped>

</style>
