<template>
  <div>
    <!-- 可用资金-->
    <el-row>
      <!--          Element UI 的列组件，表示占据 5 个格子的宽度（总共 24 格）。span 属性用于设置列的宽度-->
      <el-col :span="5">
        <!--              绑定当前可用资金的值-->
        可用资金:{{ balance }}
      </el-col>
    </el-row>
    <!-- 持仓列表 表格 -->
    <!--      :data="..."：绑定表格的数据，tableData.slice(...) 实现分页显示。
          使用 slice() 方法从 tableData 中根据当前页码 query.currentPage 和每页记录数 query.pageSize 提取相应数据。
          border：为表格添加边框。
          :cell-style="cellStyle"：为单元格设置样式，cellStyle 是一个方法，定义了单元格的样式。
          @sort-change="changeTableSort"：监听表格排序的变化，并触发 changeTableSort 方法。-->
    <el-table
        :data="
                    tableData.slice
                    (
                           (query.currentPage - 1) * query.pageSize,
                           query.currentPage * query.pageSize
                    )
                "
        border
        :cell-style="cellStyle"
        @sort-change="changeTableSort"
    >
      <!--          prop：绑定表格的属性，指示数据源中相应的字段名。
                label：列标题。
                align="center"：设置单元格内容的水平对齐方式。
                sortable：启用列排序功能。
                :sort-orders="['ascending','descending']"：设置排序的顺序（升序或降序）。
                :formatter="codeFormatter"：指定列的数据格式化方法，在这里通过 codeFormatter 对股票代码进行格式化-->
      <el-table-column prop="code" label="代码" align="center"
                       sortable :sort-orders="['ascending','descending']"
                       :formatter="codeFormatter"
      />
      <el-table-column prop="name" label="名称" align="center"/>
      <el-table-column prop="count" label="股票数量" align="center"/>
      <el-table-column prop="cost" label="总投入" align="center"
                       :formatter="moneyFormatter"/>
      <el-table-column label="成本" align="center"
                       :formatter="costFormatter"/>
    </el-table>

    <!-- 分页控件 刷新按钮-->
    <div class="pagination">
      <!--          round：按钮为圆角样式。
                type="primary"：设置按钮的类型为主按钮（通常为蓝色）。
                size="mini"：设置按钮的大小为迷你型。
                style="margin-top: 2px; float: right"：使用内联样式，设置按钮上方的外边距为 2px，且将按钮浮动到右侧。
                icon="el-icon-refresh"：设置按钮的图标为 Element UI 提供的 el-icon-refresh（刷新图标）。
                @click="queryRefresh"：绑定点击事件，当按钮被点击时，调用 queryRefresh 方法，执行数据刷新操作-->
      <el-button round
                 type="primary" size="mini"
                 style="margin-top: 2px;float: right"
                 icon="el-icon-refresh"
                 @click="queryRefresh">
        刷新
      </el-button>
      <!--          <el-pagination>: Element UI 提供的分页组件。
                  background：为分页控件添加背景。
                  layout="total,prev,pager,next"：设置分页控件的布局，包含总数、上一页、分页器和下一页。
                  :current-page="query.currentPage"：绑定当前页码的值，从 query.currentPage 获取。
                  :page-size="query.pageSize"：绑定每页显示的记录数，从 query.pageSize 获取。
                  :total="dataTotalCount"：绑定数据的总数量，dataTotalCount 是从数据源中获取的总记录数。
                  @current-change="handlePageChange"：监听页码控件的变化事件，当页码变化时，触发 handlePageChange 方法-->
      <el-pagination
          background
          layout="total,prev,pager,next"
          :current-page="query.currentPage"
          :page-size="query.pageSize"
          :total="dataTotalCount"
          @current-change="handlePageChange"
      />
    </div>

  </div>
</template>

<script>
//constants: 导入 constants 模块，通常包含常量配置（例如常用的多倍数因子等）。
// codeFormat, moneyFormat: 导入格式化方法，用于格式化股票代码和金额。
// queryPosi, queryBalance: 导入从 API 模块获取数据的方法，用于查询持仓数据和可用资金
import {constants} from '../api/constants'
import {codeFormat, moneyFormat} from '../api/formatter'
import {queryPosi, queryBalance} from '../api/orderApi'

export default {
  name: "PosiList",// 定义组件的名称为 PosiList
  created() { //  Vue 的生命周期钩子，在组件实例创建后调用。在这里，created 钩子用于初始化 tableData 和 balance，分别存储持仓数据和可用资金
    this.tableData = this.posiData;
    this.balance = this.balanceData;
  },
  computed: {
    posiData() {//从 Vuex 状态管理获取持仓数据（$store.state.posiData）
      return this.$store.state.posiData;
    },
    balanceData() {//获取可用资金数据，并通过 moneyFormat 函数进行格式化（如人民币、美元等格式）
      return moneyFormat(this.$store.state.balance);
    }
  },
  watch: {
    // 当 posiData 数据发生变化时，更新 tableData 和 dataTotalCount（数据的总条数）
    posiData: function (val) {
      this.tableData = val;
      this.dataTotalCount = val.length;
    },
    // 当 balanceData 发生变化时，更新 balance（可用资金）
    balanceData: function (val) {
      this.balance = val;
    }
  },
  data() {
    return {
      tableData: [],//存储表格显示的数据（持仓数据）
      balance: 0,//存储可用资金

      dataTotalCount: 0,//存储数据的总数量，用于分页显示

      query: {
        currentPage: 1, //当前页码
        pageSize: 2 //每页的记录数
      }
    }
  },
  methods: {
    queryRefresh() {//刷新查询，调用 queryPosi() 和 queryBalance() 方法获取持仓数据和可用资金
      queryPosi();
      queryBalance();
    },
    // 形参 ： 表单的 行和列
    costFormatter(row, column) {
      // 总消耗 / 股票数量 = 成本 保留小数
      return (row.cost / constants.MULTI_FACTOR /
          row.count).toFixed(2);
    },
    // 形参 ： 表单的 行和列
    moneyFormatter(row, column) {//资金格式化，使用 moneyFormat() 函数格式化“总投入”列
      return moneyFormat(row.cost);
    },
    // 形参 ： 表单的 行和列
    codeFormatter(row, column) {// 股票代码格式化，使用 codeFormat() 函数进行处理
      return codeFormat(row.code);
    },
    handlePageChange(val) {//处理分页的页码变化，更新当前页码 query.currentPage
      // this.query.currentPage = val;
      this.$set(this.query, 'currentPage', val);
    },
    cellStyle({row, column, rowIndex, columnIndex}) {//设置单元格的样式，返回 padding:2px
      return "padding:2px";
    },
    changeTableSort(column) {//当表格的排序列变化时，根据排序方式对 tableData 进行升序或降序排序
      if (column.order == "descending") {
        this.tableData = this.tableData.sort(
            // 600006  600001 000001
            (a, b) => b[column.prop] - a[column.prop]
        );
      } else {
        this.tableData = this.tableData.sort(
            // 600006  600001 000001
            (a, b) => a[column.prop] - b[column.prop]
        );
      }
    }
  }
}
</script>

<style scoped>

</style>
