package com.gudy.gateway.thirdpart.fetchsurv;

import com.gudy.gateway.thirdpart.order.OrderCmd;

import java.util.List;

public interface IFetchService {

    List<OrderCmd> fetchData();

}
