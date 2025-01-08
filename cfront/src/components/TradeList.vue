<template>
    <!--  成交列表  -->
    <div>
<!--      <el-table>：使用 Element UI 的表格组件，数据来源于 tableData，并根据当前页和每页数据条数来分页显示。

        :data：绑定表格数据，并且通过 slice() 来分页。
        :default-sort：设置默认的排序规则，按照 time 字段降序排列。
        @sort-change：监听排序变化，调用 changeTableSort 方法。-->
        <el-table
                :data="tableData.slice( (query.currentPage - 1) * query.pageSize, query.currentPage * query.pageSize)"
                border
                :cell-style="cellStyle"
                style=" width: 100%;font-size: 14px;"
                :default-sort ="{prop:'time',order:'descending'}"
                @sort-change="changeTableSort"
            >
<!--          prop：指定每列绑定的字段名。
          label：列标题。
          :formatter：使用自定义的格式化函数来格式化字段值，如 codeFormatter、priceFormatter 等。
          sortable：使列可排序-->

            <el-table-column prop="time" label="成交时间" align="center"
                             sortable :sort-orders="['ascending', 'descending']"/>
            <el-table-column prop="code" label="股票代码" :formatter="codeFormatter" align="center"/>
            <el-table-column prop="name" label="名称" align="center"/>
            <el-table-column prop="price" label="成交价格(元)" :formatter="priceFormatter" align="center"/>
            <el-table-column prop="tcount" label="成交数量(股)" align="center"/>
            <el-table-column label="成交金额(元)" :formatter="tmoneyFormatter" align="center"/>
            <el-table-column label="方向" :formatter="directionFormatter" align="center"/>
        </el-table>

<!--      <el-pagination>：分页组件，绑定当前页、每页显示条数和总条数，并监听 @current-change 事件来处理页码变化。
        <el-button>：刷新按钮，点击时会调用 queryTrade 方法刷新数据-->

        <div class="pagination">
            <el-button round
                       type="primary" size="mini"
                       style="margin-top:2px;float: right"
                       icon="el-icon-refresh"
                       @click="queryTrade">
                刷新
            </el-button>


            <el-pagination
                    background
                    layout="total, prev, pager, next"
                    :current-page="query.currentPage"
                    :page-size="query.pageSize"
                    :total="dataTotalCount"
                    @current-change="handlePageChange"/>
        </div>
    </div>
</template>

<script>

    import {queryTrade,queryBalance} from "../api/orderApi";
    import {codeFormat,moneyFormat,directionFormat} from "../api/formatter";

    export default {
        name: "TradeList",
        data() {
            return {
                tableData: [], // 表格数据
                query: {
                    currentPage: 1, // 当前页码
                    pageSize: 4 // 每页的数据条数
                },
            };
        },
        methods: {
          // 设置单元格样式
            cellStyle({row, column, rowIndex, columnIndex}) {
                return "padding:2px;"; // 设置单元格的内边距
            },
            codeFormatter(row, column) { // 格式化股票代码
                return codeFormat(row.code);
            },
            priceFormatter(row, column) {// 格式化价格
                return moneyFormat(row.price);
            },
            tmoneyFormatter(row, column) { // 格式化成交金额
                return moneyFormat(row.tcount * row.price);
            },
            directionFormatter(row, column) { // 格式化方向
                return directionFormat(row.direction);
            },
            queryTrade() {// 刷新交易数据
                queryTrade(); // 调用 API 查询交易数据

                queryBalance(); // 调用 API 查询账户余额
            },
            // 触发搜索按钮
            handleSearch() {
                this.$set(this.query, 'pageIndex', 1);// 重置页码为第一页
            },
            // 分页导航
            handlePageChange(val) {
                this.$set(this.query, 'currentPage', val);// 设置当前页码
            },
            //处理排序
            changeTableSort(column) {
                let sortingType = column.order;// 获取排序类型
                let fieldName = column.prop; // 获取排序字段
                if (fieldName === "time") {
                    if (sortingType == "descending") {//如果当前排序是降序（即 descending），就执行降序排序
                      // 这里使用 JavaScript 的 Array.prototype.sort() 方法来对 tableData 数组进行排序
                        this.tableData = this.tableData.sort((a, b) => {
                          //如果 b[fieldName] 的值大于 a[fieldName] 的值，则返回 1，表示 b 应该排在 a 前面
                                if (b[fieldName] > a[fieldName]) {
                                    return 1;
                                    //如果两个值相等，则返回 0，表示顺序不变
                                } else if (b[fieldName] === a[fieldName]) {
                                    return 0;
                                } else {//如果 b[fieldName] 的值小于 a[fieldName]，则返回 -1，表示 a 应该排在 b 前面
                                    return -1;
                                }
                            }
                        );
                    } else {
                        this.tableData = this.tableData.sort((a, b) => {
                            if (b[fieldName] > a[fieldName]) {//如果 b[fieldName] 的值大于 a[fieldName]，则返回 -1，表示 a 应该排在 b 前面
                                return -1;
                            } else if (b[fieldName] === a[fieldName]) {//如果两个值相等，则返回 0，表示顺序不变
                                return 0;
                            } else {//如果 b[fieldName] 的值小于 a[fieldName]，则返回 1，表示 b 应该排在 a 前面
                                return 1;
                            }
                        });
                    }
                }
            }
        },
        computed: {
            tradeData() { // 从 Vuex 中获取交易数据
                return this.$store.state.tradeData;
            },
            dataTotalCount() {// 获取交易数据总数
                return this.$store.state.tradeData.length;
            }
        },
        watch: {// 监听 tradeData 变化，更新 tableData
            tradeData: function (val) {
                this.tableData = val;
            }
        },
        created() {  // 在创建时初始化表格数据
            this.tableData = this.tradeData;
        }
    }
</script>

<style scoped>

</style>
