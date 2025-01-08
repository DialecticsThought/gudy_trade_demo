<template>
    <div>
        <!--委托列表-->

<!--      <el-table>：Element UI 表格组件，用于显示订单列表。

:data="tableData.slice(...)": 使用分页逻辑切割表格数据，只显示当前页的数据。tableData 是所有订单数据，query.currentPage 是当前页码，query.pageSize 是每页显示的记录数。
border：给表格添加边框。
@sort-change="changeTableSort"：监听表格的排序变化，当列排序发生变化时调用 changeTableSort 方法。
:default-sort="{prop: 'time', order: 'descending'}"：默认按 time 列降序排列-->
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
                :default-sort="{prop : 'time',order:'descending'}"
        >
            <!--委托时间 股票代码 名称 委托价格 委托数量 方向 状态-->
            <el-table-column prop="time" label="委托时间" align="center"
                             sortable :sort-orders="['ascending', 'descending']"/>
            <el-table-column prop="code" label="股票代码" :formatter="codeFormatter" align="center"/>
            <el-table-column prop="name" label="名称" align="center"/>
            <el-table-column prop="price" label="委托价格" :formatter="priceFormatter" align="center"/>
            <el-table-column prop="ocount" label="委托数量" align="center"/>
            <el-table-column prop="direction" label="方向" :formatter="directionFormatter" align="center"/>
            <el-table-column prop="status" label="状态" :formatter="statusFormatter" align="center"/>
            <el-table-column width="85">
                <template slot-scope="scope">
                    <el-button
                            v-show="isCancelBtnShow(scope.row.status)"
                            type="primary"
                            size="mini"
                            @click="handleCancel(scope.$index,scope.row)"
                    >撤单
                    </el-button>
                </template>
            </el-table-column>
        </el-table>

        <!--分页控件+刷新-->

<!--      刷新按钮：点击刷新按钮时调用 queryOrder 方法，刷新订单数据。
      分页组件 (el-pagination)：用于显示分页控件，包括当前页、总页数、页码选择等。
      :current-page="query.currentPage"：当前页码。
      :page-size="query.pageSize"：每页的记录数。
      :total="dataTotalCount"：总记录数，来自 dataTotalCount。
      @current-change="handlePageChange"：监听页码变化，当用户切换页码时调用 handlePageChange 方法-->
        <div class="pagination">
            <el-button round
                       type="primary" size="mini"
                       style="margin-top:2px;float: right"
                       icon="el-icon-refresh"
                       @click="queryOrder">
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

    import {queryOrder, queryBalance, cancelOrder} from "../api/orderApi";
    import {codeFormat, moneyFormat, directionFormat, statusFormat} from "../api/formatter";
    import {constants} from "../api/constants";

    export default {
        name: "OrderList",
        data() {
            return {
                tableData: [], // 表格数据
                query: {
                    currentPage: 1, // 当前页码
                    pageSize: 4 // 每页的数据条数
                }
            };
        },
        computed: {
            orderData() {
                return this.$store.state.orderData;  // 从 Vuex 获取订单数据
            },
            dataTotalCount() {
                return this.$store.state.orderData.length;// 订单总数
            }
        },
        watch: {
            orderData: function (val) {// 当 orderData 发生变化时更新 tableData
                this.tableData = val;
            }
        },
        created() {// 在创建时初始化表格数据
            this.tableData = this.orderData;
        },
        methods: {
            isCancelBtnShow(status) {
                //已报 部成成交的委托
              // 判断撤单按钮是否应该显示，只有在委托状态为“已报”(3) 或 “部分成交”(5)时显示
                if (status == 3 || status == 5) {
                    return true;
                } else {
                    return false;
                }
            },
            handleCancel(index, row) {
              // 提供确认撤单功能
                let message = (row.direction === constants.BUY ? "买入" : "卖出")
                    + "     " + row.name + "(" + codeFormat(row.code) + ")    "
                    + row.ocount + "股";
              // 弹出确认框，询问是否撤单
                this.$confirm(message, '撤单', {
                    confirmButtonText: '确定',
                    cancelButtonText: '取消',
                    type: 'warning'
                }).then(() => {
                  // 调用撤单 API
                    cancelOrder(
                        {
                            uid: sessionStorage.getItem("uid"),// 获取用户ID
                            counteroid: row.id, // 获取订单ID
                            code: row.code// 股票代码
                        },
                        undefined); // 撤单成功后无需进一步处理
                });

            },
            queryOrder() { // 查询订单列表和余额信息
                queryOrder();
                queryBalance();
            },
            codeFormatter(row, column) { // 格式化股票代码
                return codeFormat(row.code);
            },
            priceFormatter(row, column) {// 格式化委托价格
                return moneyFormat(row.price);
            },
            directionFormatter(row, column) {// 格式化委托方向，买入或卖出
                console.log(row);
                return directionFormat(row.direction);
            },
            // 禁用状态格式化
            statusFormatter(row, column) {   // 格式化委托状态，显示如已报、已成等状态
                // 委托状态：// 0.已报  1.已成 2.部成 3.废单 4.已撤
                return statusFormat(row.status);
            },

            handlePageChange(val) { // 页码切换时更新当前页码
                // this.query.currentPage = val;
                this.$set(this.query, 'currentPage', val);
            },
            cellStyle({row, column, rowIndex, columnIndex}) { // 设置单元格样式
                return "padding:2px";
            },
          // 处理表格排序功能。如果用户点击排序按钮，表格会根据 time 列的时间排序。
          // 通过判断 column.order 来确定是升序还是降序，采用 JavaScript 的 Array.sort() 方法对数据进行排序
            changeTableSort(column) {
              // 根据表格列排序的改变，重新对数据进行排序
                let fieldName = column.prop;
                let sortingType = column.order;
              // 按时间排序，降序或升序
                if (fieldName === 'time') {
                    if (sortingType == "descending") {
                        this.tableData = this.tableData.sort((a, b) => {
                                if (b[fieldName] > a[fieldName]) {
                                    return 1;
                                } else if (b[fieldName] === a[fieldName]) {
                                    return 0;
                                } else {
                                    return -1;
                                }
                            }
                        );
                    } else {
                        this.tableData = this.tableData.sort((a, b) => {
                                if (b[fieldName] > a[fieldName]) {
                                    return -1;
                                } else if (b[fieldName] === a[fieldName]) {
                                    return 0;
                                } else {
                                    return 1;
                                }
                            }
                        );
                    }
                }
            }
        }
    }
</script>

<style scoped>

</style>
