<template>
    <div class="orderForm">
        <el-form label-width="80px">
<!--          该表单项显示一个标题 买入股票 或 卖出股票，通过 direction 来判断是买入还是卖出
          :style="direction === 0 ? 'color: #F56C6C' : 'color:#67C23A' " 动态设置标题颜色：
          若 direction === 0，则为红色（买入），否则为绿色（卖出）-->
            <el-form-item>
                <h3 :style="direction ===0 ?
                                'color: #F56C6C' : 'color:#67C23A' ">
                    {{direction === 0 ? '买入' : '卖出'}}股票
                </h3>
            </el-form-item>
            <el-form-item label="证券代码">
                <code-input/>
            </el-form-item>
<!--          el-input: 使用了 Element UI 的输入框组件。readonly 属性使该输入框为只读，即用户无法编辑证券名称。
          v-model="name": Vue 的双向绑定，name 是组件的 data 中的变量，显示证券名称-->
            <el-form-item label="证券名称">
                <el-input readonly v-model="name"/>
            </el-form-item>
            <!--可买 可卖数量-->
<!--          动态标签：标签内容根据 direction 的值动态显示 "可买(股)" 或 "可卖(股)"。
          el-input readonly: 显示用户可买或可卖的股数。v-model="affordCount" 用来绑定组件的数据-->
            <el-form-item :label="'可' + (direction === 0 ?
                '买' : '卖') + '(股)'">
                <el-input readonly v-model="affordCount"/>
            </el-form-item>
<!--          el-input-number: 用于数字输入，v-model="price" 绑定 price 变量，表示用户输入的价格。
          controls-position="right": 让控件的位置显示在右边。
          @change="handlePrice": 当价格变化时调用 handlePrice 方法。
          :step="0.01": 设置价格输入的步长为 0.01，即价格增量为 0.01。
          :min="0.01": 设置价格的最小值为 0.01-->
            <el-form-item label="价格">
                <el-input-number v-model="price"
                                 controls-position="right"
                                 @change="handlePrice"
                                 :step="0.01"
                                 :min="0.01"/>
            </el-form-item>

<!--          v-model="volume" 绑定股票数量的变量。
          :max="affordCount": 设置最大输入值为 affordCount，即用户可买或可卖的最大股数。
          :min="0": 设置最小输入值为 0，即不能输入负数-->
            <el-form-item :label="(direction === 0 ?
                '买入' : '卖出') + '(股)'">
                <el-input-number
                        v-model="volume"
                        controls-position="right"
                        :max="affordCount"
                        :min="0"/>
            </el-form-item>
            <!--按钮-->
<!--          el-button: Element UI 的按钮组件。
          :type="direction === 0 ? 'danger' : 'success'": 通过 direction 判断按钮的样式类型。如果是买入 (direction === 0)，按钮类型为 danger（通常是红色，表示警告或危险），如果是卖出（direction === 1），按钮类型为 success（通常是绿色，表示成功）。
          style="float: right": 将按钮浮动到右侧。
          @click="onOrder": 点击按钮时触发 onOrder 方法，该方法会执行买入或卖出的操作。
          {{ direction === 0 ? '买入' : '卖出'}}: 通过 direction 动态显示按钮上的文字，若是买入，则显示 "买入"，若是卖出，则显示 "卖出"-->
            <el-form-item>
                <el-button :type="direction === 0 ?
                            'danger' : 'success'" style="float: right"
                           @click="onOrder">
                    {{ direction === 0 ? '买入' : '卖出'}}
                </el-button>
            </el-form-item>
        </el-form>


    </div>

</template>

<script>

    import CodeInput from './CodeInput'
    import {sendOrder} from '../api/orderApi'
    import {constants} from '../api/constants'
    import * as moment from 'moment'

    export default {
        name: "OrderWidget",
        data() {
            return {
                code: '',// 当前证券的代码
                name: '',// 当前证券的名称
                affordCount: undefined,// 用户可卖出的数量
                price: undefined,// 用户输入的价格
                volume: undefined,// 用户输入的买入/卖出股数
            }
        },
        components: {// 声明当前组件依赖的子组件
            CodeInput
        },
        created() {//created() 生命周期钩子在组件创建时触发，
          // 通过 this.$bus.on() 监听 codeinput-selected 事件，当该事件触发时调用 updateSlectedCode 方法
            this.$bus.on("codeinput-selected", this.updateSelectedCode);
        },
        beforeDestroy() {//beforeDestroy() 生命周期钩子在组件销毁前触发，清除事件监听器
            this.$bus.off("codeinput-selected", this.updateSelectedCode);
        },
        methods: {
            handlePrice() {
              // 卖出：通过查找 posiData（持仓数据），如果找到该股票，设置 affordCount 为对应的股票数量。
              // 买入：通过当前余额和价格计算用户可以购买的股票数量，余额（除以 MULTI_FACTOR）除以价格。
                if (this.direction === constants.SELL) {// 如果是卖委托页面中
                  // 能卖出最大股票的数量 就是持仓数量
                  // 持仓数量已经之前存放到vuex中
                    let posiArr = this.$store.state.posiData;
                    // 编译客户所有的持仓
                    for (let i = 0, len = posiArr.length; i < len; i++) {
                      // 当前委托页面的股票 == 遍历到的股票
                        if (posiArr[i].code == this.code) {
                          // 把可用数量 变成 客户的持仓量
                            this.affordCount = posiArr[i].count;
                        }
                    }
                } else {// 如果是买委托页面中
                    //当前客户的委托价格 和客户的资金量 做上限估算
                  // 客户的总资金 / 委托价格 = 客户最大的购买量
                    this.affordCount = parseInt((this.$store.state.balance / constants.MULTI_FACTOR) / this.price);
                }
            },
            updateSelectedCode(item) {//更新选择的股票代码和名称，当 CodeInput 组件选择一个股票时触发
              // item 是选中的股票对象，更新 code、name
                this.code = item.code;
                this.name = item.name;
                this.price = undefined;
                this.volume = undefined;
            },
            handleOrderRes(code, msg, data) {//处理买卖委托的响应，成功时显示成功消息，否则显示错误消息
                if (code === 0) {
                    this.$message.success("委托送往交易所");
                } else {
                    this.$message.error("委托失败:" + msg);
                }
            },
            onOrder() {//发送买入或卖出请求
              // 使用 sendOrder 函数发送订单，包含用户 ID、订单类型（新订单）、当前时间戳、股票代码、买卖方向、价格、数量、订单类型等信息
                sendOrder({
                        uid: sessionStorage.getItem("uid"),
                        type: constants.NEW_ORDER,
                        timestamp: moment.now(),
                        code: this.code,
                        direction: this.direction,
                        price: this.price * constants.MULTI_FACTOR,
                        volume: this.volume,
                        ordertype: constants.LIMIT
                    },
                    this.handleOrderRes
                )
            },
        },
      //组件的属性（外部传入的值）
      //direction 属性决定买入还是卖出，0 表示买入，1 表示卖出
        props: {
            direction: {types: Number, required: true}
        }
    }
</script>

<style lang="scss">

    .orderForm {
        input {
            text-align: center;
        }

        .el-input-number {
            width: 100%;
        }
    }

</style>
