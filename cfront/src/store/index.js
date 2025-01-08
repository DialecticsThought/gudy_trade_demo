import Vue from 'vue'
import Vuex from 'vuex'

Vue.use(Vuex)

/*
* 为什么需要 Vuex？
在 Vue 应用中，组件之间的通信主要通过以下方式：

父子组件通信：通过 props 和 emit 进行数据传递。
兄弟组件通信：可以使用事件总线或共享状态。
跨层级组件通信：需要将数据逐层传递，可能会导致“props drilling（属性传递地狱）”。
当应用规模变大、组件变得复杂时，这种方式可能会变得难以维护。这时，Vuex 提供了一种解决方案：

集中式状态管理：将状态保存在全局的 store 中。
统一的修改方式：通过定义的规则（mutations 和 actions）来修改状态。
易于调试：通过 Vue DevTools，可以轻松追踪状态的变化
*
* */
export default new Vuex.Store({
    /*
    * 用于存储应用的全局状态（数据）。
    * 所有组件都可以直接读取 state 中的数据，但不能直接修改。
    * 通过 store.state 访问
    *
    * */
    state: {
        posiData: [],
        orderData: [],
        tradeData: [],
        balance: 0,
    },
    getters: {},
    /*
    * 是唯一能够直接修改 state 的地方。
    * 必须是同步的。
    * 通过 commit 方法触发
    * */
    mutations: {
        updatePosi(state, posiInfo) {
            state.posiData = posiInfo;
        },
        updateOrder(state, orderInfo) {
            state.orderData = orderInfo;
        },
        updateTrade(state, tradeInfo) {
            state.tradeData = tradeInfo;
        },
        updateBalance(state, balance) {
            state.balance = balance;
        },
    },
    /*
    * 用于处理异步操作（如 API 请求）。
    * 不能直接修改 state，而是通过提交 mutations 来间接修改。
    * 通过 dispatch 方法触发
    * */
    actions: {},
    /*
    * 当应用变得复杂时，可以将 store 分成多个模块，每个模块都有自己的 state、mutations、actions 和 getters。
    * 模块可以是嵌套的
    * */
    modules: {}
})
