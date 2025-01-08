package com.gudy.counter.cache;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.gudy.counter.bean.Stock;
import com.gudy.counter.bean.StockInfo;
import com.gudy.counter.service.StockService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Log4j2
@Component
public class StockCache {
    @Resource
    private StockService service;

    /*
     * HashMultimap<String, StockBO> invertIndex 的本质就是 Map<String,List<StockInfo>>
     * 6 : 600086 -> 600025 ->
     * 60 : 600086 -> 600025 ->
     * 001 : 000001 ->
     * 0025 : 600025 ->
     * 那么
     * key 就是 6 , 60 , 001, 0025
     * value 就是对应的 stock集合
     * */
    private HashMultimap<String, StockInfo> invertIndex = HashMultimap.create();

    public Collection<StockInfo> getStocks(String key) {
        return invertIndex.get(key);
    }

    @PostConstruct
    private void createInvertIndex() {
        log.info("load stock from db");
        long st = System.currentTimeMillis();

        //1.加载股票数据
        List<Stock> stocks = service.queryAllStock();
        List<Map<String, Object>> allStocks = null;

        if (!CollectionUtils.isEmpty(stocks)) {
            allStocks = service.convertStockToMap(stocks);
        }
/*        if (CollectionUtils.isEmpty(allStocks)) {
            log.error("no stock find in db");
            return;
        }*/
        //2.建立倒排索引
        if (!CollectionUtils.isEmpty(allStocks)) {
            for (Map<String, Object> map : allStocks) {
                int code = Integer.parseInt(map.get("code").toString());
                String name = map.get("name").toString();
                String abbrname = map.get("abbrname").toString();
                StockInfo stock = new StockInfo(code, name, abbrname);
                //  000001 平安银行 payh
                // 股票代号的倒排索引
                List<String> codeMetas = splitData(String.format("%06d", code));
                // 股票缩写的倒排索引
                List<String> abbrNameMetas = splitData(abbrname);
                codeMetas.addAll(abbrNameMetas);

                for (String key : codeMetas) {
                    //限制索引数据列表长度
                    Collection<StockInfo> stockInfos = invertIndex.get(key);
                    if (!CollectionUtils.isEmpty(stockInfos)
                            && stockInfos.size() > 10) {
                        continue;
                    }
                    invertIndex.put(key, stock);
                }
            }
        }

        log.info("load stock finish,take :" +
                (System.currentTimeMillis() - st) + "ms");
    }

    /*
     * 有一个单词 payh 简历对应的倒排索引
     * p pa pay payh
     * a ay ayh
     * y yh
     * h
     * */
    private List<String> splitData(String code) {
        List<String> list = Lists.newArrayList();
        int outLength = code.length();
        for (int i = 0; i < outLength; i++) {
            int inLength = outLength + 1;
            for (int j = i + 1; j < inLength; j++) {
                list.add(code.substring(i, j));
            }
        }
        return list;
    }


}
