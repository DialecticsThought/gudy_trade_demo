<template>
  <!--订单簿窗口-->
  <el-form label-width="80px">
    <!-- 行情时间-->

    <!--      hqtime：显示当前的行情时间。
          sell 和 buy：分别展示卖方和买方的五档行情数据，包括档位名称（如“卖五”、“买一”）、价格、成交量和成交量的占比（宽度）。
          filterempty：自定义的 Vue 过滤器，若数据值为 -1，则显示为 '-'，
          使用 el-row 和 el-col 分别展示每档的 档位名称、价格、成交量的宽度 和 成交量。
          成交量宽度用 CSS 动态设置，通过 v-bind:style 根据成交量计算其比例
          -->
    <el-form-item>
      <h4 style="color:#909399">
        行情时间:{{ hqtime }}
      </h4>
    </el-form-item>
    <!--五档行情-->
    <el-form-item>
      <!--卖-->
      <div class="orderBook">
        <el-row v-for="item in sell" :key="item.name">
          <!--档位名称-->
          <el-col :span="6">{{ item.name }}</el-col>
          <!--价格-->
          <el-col :span="6">{{ item.price | filterempty }}</el-col>
          <!--长度-->
          <el-col :span="6">
            <div class="volumeratio">
              <div class="sell" v-bind:style="
                                    {width: item.width+'%'}"
              />
            </div>
          </el-col>
          <!--量-->
          <el-col :span="6">
            {{ item.volume | filterempty }}
          </el-col>
        </el-row>
      </div>
      <!--买-->
      <div class="orderBook">
        <el-row v-for="item in buy" :key="item.name">
          <!--档位名称-->
          <el-col :span="6">{{ item.name }}</el-col>
          <!--价格-->
          <el-col :span="6">{{ item.price | filterempty }}</el-col>
          <!--长度-->
          <el-col :span="6">
            <div class="volumeratio">
              <div class="buy" v-bind:style="{width: item.width+'%'}"
              />
            </div>
          </el-col>
          <!--量-->
          <el-col :span="6">
            {{ item.volume | filterempty }}
          </el-col>
        </el-row>

      </div>

    </el-form-item>


  </el-form>
</template>

<script>
import {constants} from "../api/constants";
import * as moment from 'moment'

export default {
  name: "OrderBook",
  filters: {
    filterempty(value) {
      if (value === -1) {
        return '-';
      } else {
        return value;
      }
    }
  },
  data() {
    return {
      hqtime: '--:--:--',//hqtime：行情时间（初始值为 --:--:--）

      //sell 和 buy 存储买卖方的五档行情数据，默认每个档次的 price 和 volume 为 -1，表示没有数据，width 初始为 1
      sell: [
        {
          name: "卖五",
          price: -1,
          volume: -1,
          width: 1,
        },
        {
          name: "卖四",
          price: -1,
          volume: -1,
          width: 1,
        },
        {
          name: "卖三",
          price: -1,
          volume: -1,
          width: 1,
        },
        {
          name: "卖二",
          price: -1,
          volume: -1,
          width: 1,
        },
        {
          name: "卖一",
          price: -1,
          volume: -1,
          width: 1,
        },
      ],
      buy: [
        {
          name: "买一",
          price: -1,
          volume: -1,
          width: 1,
        },
        {
          name: "买二",
          price: -1,
          volume: -1,
          width: 1,
        },
        {
          name: "买三",
          price: -1,
          volume: -1,
          width: 1,
        },
        {
          name: "买四",
          price: -1,
          volume: -1,
          width: 1,
        },
        {
          name: "买五",
          price: -1,
          volume: -1,
          width: 1,
        },
      ],
    }
  },
  // 在 created 生命周期钩子中，组件通过事件总线 ($bus) 监听 "codeinput-selected" 事件。
  // 当接收到该事件时，调用 startL1Sub 方法来订阅实时的一级市场数据。
  // 在 beforeDestroy 生命周期钩子中，确保在组件销毁时移除事件监听，避免内存泄漏
  created() { // 监听“codeinput-selected”事件，初始化数据订阅
    this.$bus.on("codeinput-selected", this.startL1Sub);
  },
  beforeDestroy() {// 组件销毁时取消订阅
    this.$bus.off("codeinput-selected", this.startL1Sub);
  },
  methods: {
    // 该方法是核心的实时数据订阅逻辑。它首先通过 setInterval 每秒订阅一次指定股票的一级市场数据 (l1-market-data)。
    // 如果数据有效，检查代码和时间戳是否符合要求，然后更新买方和卖方的价格和成交量数据。
// buyPrices 和 buyVolumes：分别是买方的价格和成交量。
//sel lPrices 和 sellVolumes：分别是卖方的价格和成交量。
// 每次更新数据时，通过 width 计算成交量的占比，用于动态展示成交量的比例。
    startL1Sub(item) {
      let code = item.code;
      let _vm = this;
      this.resetData(true);
      // 设置定时器，每隔一定时间订阅一次实时行情数据
      _vm.intervalId = setInterval(() => {
        _vm.$eventBus.send('l1-market-data',
            {},
            {
              code: code,
            },
            (err, reply) => {
              if (err) {
                console.error('subscribe ' + item.code + ' l1 market data fail', err);
              } else {
                let l1MarketData = JSON.parse(reply.body);
                if (l1MarketData == null) {
                  return;
                }

                //判断代码
                // 判断代码是否匹配
                if (code != l1MarketData.code) {
                  console.error("wrong code hq,code= " + code + ",recv code= " + l1MarketData.code);
                  return;
                }

                //判断时间戳
                // 判断时间戳是否有效，避免使用过期数据
                if (l1MarketData.timestamp < _vm.hqtimestamp) {
                  return;
                }

                this.resetData(false);// 更新数据

                _vm.hqtimestamp = l1MarketData.timestamp;
                _vm.hqtime = moment(_vm.hqtimestamp).format("HH:mm:ss");// 更新时间戳

                // 处理买方数据
                let buyPrices = l1MarketData.buyPrices;
                let buyVolumes = l1MarketData.buyVolumes;
                let maxBuyVolume = -1;
                for (let i = 0; i < buyPrices.length; i++) {
                  // 格式化买价格
                  _vm.buy[i].price = (buyPrices[i] / constants.MULTI_FACTOR).toFixed(2);
                  _vm.buy[i].volume = buyVolumes[i];// 处理买成交量
                  if (buyVolumes[i] > maxBuyVolume) {
                    maxBuyVolume = buyVolumes[i];// 记录最大成交量
                  }
                }
                // 计算买方成交量比例
                for (let i = 0; i < buyVolumes.length; i++) {
                  if (maxBuyVolume != 0) {
                    _vm.buy[i].width = Math.floor(buyVolumes[i] / maxBuyVolume * 100);
                  } else {
                    _vm.buy[i].width = 1;
                  }
                }

                // 处理卖方数据
                let sellPrices = l1MarketData.sellPrices;
                let sellVolumes = l1MarketData.sellVolumes;
                let maxSellVolume = -1;
                for (let i = 0; i < sellPrices.length; i++) {
                  _vm.sell[4 - i].price = (sellPrices[i] / constants.MULTI_FACTOR).toFixed(2);
                  _vm.sell[4 - i].volume = sellVolumes[i];
                  if (sellVolumes[i] > maxSellVolume) {
                    maxSellVolume = sellVolumes[i];
                  }
                }
                // 计算卖方成交量比例
                for (let i = 0; i < sellVolumes.length; i++) {
                  if (maxSellVolume != 0) {
                    _vm.sell[4 - i].width = Math.floor(sellVolumes[i] / maxSellVolume * 100);
                  } else {
                    _vm.sell[4 - i].width = 1;
                  }
                }
              }
            }
        )
      }, 1000); // 每秒钟更新一次数据
    },

    //重置订单簿
    // resetData(true) 会将所有数据重置为初始状态（即没有数据时的默认值）。
    // resetData(false) 会根据新获取的数据更新买卖盘的状态
    resetData(isClearInterval) {
      this.hqtime = '--:--:--';
      this.hqtimestamp = 0;

      //清空原来数据
      // 如果是初始加载，设置所有数据为默认值
      this.buy.forEach(t => {
        t.price = -1;
        t.volume = -1;
        t.width = 1;
      });

      this.sell.forEach(t => {
        t.price = -1;
        t.volume = -1;
        t.width = 1;
      });

      if (this.intervalId != -1 && isClearInterval) {
        clearInterval(this.intervalId);
        this.intervalId = -1;
      }

    }


  }

}
</script>

<style lang="scss" scoped>
.orderBook {
  border: 1px solid #909399;;
  margin-left: 5%;
  margin-right: 5%;

  .el-row {
    .el-col {
      height: 35px;
      line-height: 35px;
    }
  }

  .volumeratio {
    margin: 10px auto;
    border: none;

    .sell {
      height: 10px;
      background: #67C23A;
    }

    .buy {
      height: 10px;
      background: #F56C6C;
    }
  }
}

</style>
