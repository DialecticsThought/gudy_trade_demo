<template>
  <div>

    <!-- 搜索条件栏-->
    <div class="handle-box">
      <el-row>
        <!-- 自动提示框-->
        <el-col :span="4">
          <code-input/>
        </el-col>

        <!--日期选择器-->
        <div style="float:left; margin-left: 10px">
          <el-date-picker
              size="small"
              type="date"
              placeholder="选择日期"
              value-format="yyyyMMdd"
              v-model="query.startDate"
          />
          -

          <!--    作用：<el-date-picker> 是 Element UI 的日期选择组件，用于选择日期。
          属性：
          size：组件的大小，可以是 "medium", "small", 或 "mini"，决定组件的尺寸。
          type：选择器的类型，"date" 表示选择日期，"datetime" 表示选择日期和时间。
          placeholder：占位符文本，显示在输入框内，提示用户需要输入的内容。
          value-format：指定选择的日期的格式。当用户选择日期时，返回该格式的日期字符串，这里设置为 yyyyMMdd。
          v-model：Vue 的双向绑定，确保选择的日期被绑定到 query.startDate 或 query.endDate 数据属性上
           -->
          <el-date-picker
              size="small"
              type="date"
              placeholder="选择日期"
              value-format="yyyyMMdd"
              v-model="query.endDate"
          />

        </div>

        <!--搜索按钮-->
        <!--    作用：<el-button> 是 Element UI 的按钮组件，用于显示按钮。
        属性：
        style：内联样式，这里使用 float: left; margin-left: 10px 将按钮浮动到左边并增加左侧的间距。
        size：按钮的尺寸，"small" 表示小尺寸。
        type：按钮的类型，"primary" 表示主按钮，通常使用较为显眼的颜色。Element UI 提供了 primary, success, warning, danger 和 text 等类型。
        icon：按钮图标，通过 el-icon- 后面跟图标名来设置。此处是 el-icon-search，即一个搜索图标。
        @click：Vue 事件监听器，监听点击事件并触发 handleSearch 方法
         -->
        <el-button style="float: left;margin-left: 10px"
                   size="small"
                   type="primary" icon="el-icon-search"
                   @click="handleSearch"
        >
          搜索
        </el-button>

      </el-row>

    </div>

    <!-- 历史委托查询结果-->
    <!-- 作用：<el-table> 是 Element UI 的表格组件，用于展示数据。
    属性：
    :data：绑定数据源，这里通过 tableData.slice(...) 进行分页操作，控制显示当前页的数据。
    border：启用表格的边框。
    :cell-style：通过绑定样式方法 cellStyle，动态设置单元格样式。
    @sort-change：监听排序事件，触发 changeTableSort 方法，根据排序条件更新表格数据
     -->
    <el-table
        :data="
                tableData.slice
                (
                    (query.currentPage - 1) * query.pageSize,
                    query.currentPage * query.pageSize
                )"
        border
        :cell-style="cellStyle"
        @sort-change="changeTableSort"
    >
      <!--    作用：<el-table-column> 是 Element UI 表格的列组件，用来定义表格的每一列。
      属性：
      prop：该列的数据字段，prop="date" 表示列显示 tableData 中每个项的 date 字段的值。
      label：列头的标题，label="委托日期" 设置表格头部的名称。
      align：列内容的对齐方式，align="center" 表示内容居中显示。
      sortable：设置列是否可排序，默认为 false，设置为 true 时该列可以进行排序。
      :sort-orders：指定排序的方向，可以是 'ascending' 或 'descending'，
      这里绑定了一个数组 ['ascending', 'descending']，表示支持升序和降序的排序      -->
      <el-table-column prop="date" label="委托日期" align="center"
                       sortable :sort-orders="['ascending', 'descending']"/>
      <el-table-column prop="time" label="委托时间" align="center"/>
      <el-table-column prop="code" label="股票代码" align="center"/>
      <el-table-column prop="name" label="名称" align="center"/>
      <el-table-column prop="price" label="委托价格" align="center"/>
      <el-table-column prop="ocount" label="委托数量" align="center"/>
      <el-table-column prop="status" label="状态" align="center"/>
    </el-table>

    <!--分页控件-->

    <!--  作用：<el-pagination> 是 Element UI 的分页组件，用于展示分页控件。
    属性：
    background：使分页组件背景变为透明。
    layout：分页的布局，"total, prev, pager, next" 表示显示总数、上一页、页码和下一页按钮。
    :current-page：当前页码，通过绑定 query.currentPage 数据来动态更新。
    :page-size：每页显示的条目数，通过绑定 query.pageSize 动态更新。
    :total：数据总条数，通过绑定 pageTotal 来控制总条数。
    @current-change：监听页码改变事件，触发 handlePageChange 方法，更新当前页码    -->
    <div class="pagination">
      <el-pagination
          background
          layout="total, prev, pager, next"
          :current-page="query.currentPage"
          :page-size="query.pageSize"
          :total="pageTotal"
          @current-change="handlePageChange"/>
    </div>


  </div>
</template>

<script>

import CodeInput from './CodeInput';

export default {
  name: "HisOrderList",
  components: {
    CodeInput,
  },
  data() {//data 是一个函数，返回一个对象，定义了该组件的响应式数据
    return {
      tableData: [//这个数组包含了表格展示的数据。每个元素代表一个历史委托记录，包括委托的日期、时间、股票代码、名称、委托价格、委托数量和状态等信息
        {
          date: '20200105',
          time: '14:00:01',
          code: 600000,
          name: '浦发银行',
          price: 10,
          ocount: 100,
          status: 1
        },
        {
          date: '20200101',
          time: '14:00:02',
          code: 600000,
          name: '浦发银行',
          price: 11,
          ocount: 100,
          status: 1
        },
        {
          date: '20200103',
          time: '14:00:03',
          code: 600000,
          name: '浦发银行',
          price: 12,
          ocount: 100,
          status: 1
        },
        {
          date: '20200111',
          time: '14:00:04',
          code: 600000,
          name: '浦发银行',
          price: 13,
          ocount: 100,
          status: 1
        },
      ],
      query: {
        currentPage: 1, // 当前页码
        pageSize: 3, // 每页的数据条数,
        code: '', // 股票代码
        startDate: '',// 开始日期
        endDate: '',// 结束日期
      },
      pageTotal: 4, // 总数据量
    }
  },
  methods: {
    //该方法返回表格单元格的内联样式，用于给每个单元格添加一些额外的样式
    cellStyle({row, column, rowIndex, columnIndex}) {
      return "padding:2px;";//为每个单元格添加 2px 的内边距
    },
    //处理排序
    changeTableSort(column) {
      let sortingType = column.order;// 获取排序类型
      let fieldName = column.prop; // 获取排序字段
      if (sortingType == "descending") {
        // 排序依据是 fieldName 对应的字段值，a 和 b 是待比较的两个元素（即每一行数据）
        this.tableData = this.tableData.sort((a, b) => {
              if (b[fieldName] > a[fieldName]) {
                return 1;// 降序：b > a 时，b 排在前面
              } else if (b[fieldName] === a[fieldName]) {
                return 0;// 相等时，维持原位置
              } else {
                return -1;// 降序：a > b 时，a 排在前面
              }
            }
        );
      } else {
        // 排序依据是 fieldName 对应的字段值，a 和 b 是待比较的两个元素（即每一行数据）
        this.tableData = this.tableData.sort((a, b) => {
          if (b[fieldName] > a[fieldName]) {
            return -1; // 升序：b > a 时，b 排在后面
          } else if (b[fieldName] === a[fieldName]) {
            return 0; // 相等时，维持原位置
          } else {
            return 1;  // 升序：a > b 时，a 排在后面
          }
        });
      }
    },
    // 作用：处理分页控件的页码变化事件。
    // val 是分页控件传递的页码，更新 query.currentPage 的值
    // 分页导航
    handlePageChange(val) {
      this.$set(this.query, 'currentPage', val);
    },
    //该方法用于更新查询条件中的 code 字段。它会接收一个 item 参数，item.code 用来设置查询的股票代码
    updateSlectedCode(item) {
      this.query.code = item.code;
    },
    handleSearch() {

    },

  },
  //created 是 Vue 实例创建后的生命周期钩子函数。
  //在这里，使用 $bus.on() 监听 codeinput-selected 事件，事件触发时调用 updateSlectedCode 方法。
  //这个事件可能是由 CodeInput 组件发出的，当用户选择了一个股票代码时，该事件会触发
  created() {
    this.$bus.on("codeinput-selected", this.updateSlectedCode);
  },
  // beforeDestroy 是 Vue 实例销毁前的生命周期钩子
  //这里使用 $bus.off() 移除之前注册的 codeinput-selected 事件监听器，防止内存泄漏
  beforeDestroy() {
    this.$bus.off("codeinput-selected", this.updateSlectedCode);
  }
}
</script>

<style scoped>

</style>
