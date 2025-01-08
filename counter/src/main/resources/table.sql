create table t_user
(
    id          int not null
        primary key,
    uid         bigint null,
    password    varchar(255) null,
    balance     bigint null,
    create_date datetime null,
    modify_date datetime null,
    create_time datetime null,
    update_time datetime null,
    constraint t_user_pk
        unique (id)
);

CREATE TABLE your_table_name
(
    id          INT         NOT NULL PRIMARY KEY, -- 主键，整数类型，非空
    uid         BIGINT      NOT NULL,             -- 用户ID，长整数类型，非空
    date        VARCHAR(8)  NOT NULL,             -- 日期，长度为8的字符串，非空
    time        VARCHAR(8)  NOT NULL,             -- 时间，长度为8的字符串，非空
    bank        VARCHAR(30) NOT NULL,             -- 银行信息，长度为30的字符串，非空
    type        INT         NOT NULL,             -- 类型，整数类型，非空
    moneytype   INT         NOT NULL,             -- 金额类型，整数类型，非空
    money       BIGINT      NOT NULL,             -- 金额，长整数类型，非空
    create_time datetime null,
    update_time datetime null
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

#
持仓数据
CREATE TABLE t_posi
(
    id          INT    NOT NULL AUTO_INCREMENT PRIMARY KEY, -- 主键，整数类型，非空
    uid         BIGINT NOT NULL,                            -- 用户ID，长整数类型，非空
    code        INT(10) NOT NULL,                           -- 股票代码字段，整数类型，长度为10，非空
    cost        BIGINT NOT NULL,                            -- 股票持有数量 * 当时买入价格
    count       BIGINT NOT NULL,                            -- 股票持有数量字段，长整数类型，非空
    create_time datetime null,
    update_time datetime null
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

#
记录委托表
CREATE TABLE t_order
(
    id          INT        NOT NULL AUTO_INCREMENT PRIMARY KEY, -- 主键，整数类型，非空
    uid         BIGINT     NOT NULL,                            -- 用户ID，长整数类型，非空
    code        INT        NOT NULL,                            -- 股票代码字段，整数类型，非空
    direction   INT        NOT NULL,                            -- 买卖方向字段，整数类型，非空
    type        INT        NOT NULL,                            -- 委托类型字段，整数类型，非空
    price       BIGINT     NOT NULL,                            -- 委托价格字段，长整数类型，非空
    order_count BIGINT     NOT NULL,                            -- 委托数量字段，长整数类型，非空
    status      INT        NOT NULL,                            -- 委托状态字段，整数类型，非空
    order_date  VARCHAR(8) NOT NULL,                            -- 委托的发生日期字段，长度为 8 的字符串，非空
    create_time VARCHAR(8) NOT NULL,
    update_time datetime null
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

#
记录交易表
CREATE TABLE t_trade
(
    id          INT      NOT NULL AUTO_INCREMENT PRIMARY KEY, -- 主键字段，长整数类型，非空
    uid         BIGINT   NOT NULL,                            -- 用户ID字段，长整数类型，非空
    code        INT      NOT NULL,                            -- 代码字段，整数类型，非空
    direction   INT      NOT NULL,                            -- 方向字段，整数类型，非空
    price       BIGINT   NOT NULL,                            -- 价格字段，长整数类型，非空
    trade_count BIGINT   NOT NULL,                            -- 成交数量字段，长整数类型，非空
    order_id    INT      NOT NULL,                            -- 委托编号订，整数类型，非空
    trade_date  datetime NOT NULL,                            -- 成交日期字段，非空
    create_time datetime null,
    update_time datetime null
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

#
股票信息表
CREATE TABLE t_stock
(
    id          INT         NOT NULL AUTO_INCREMENT PRIMARY KEY,
    code        INT         NOT NULL,                                          -- 股票代码，
    name        VARCHAR(20) NOT NULL,                                          -- 股票名称，
    abbr_name    VARCHAR(10) NOT NULL,                                          -- 股票简称，
    status      INT         NOT NULL,                                          -- 状态字段， 停牌 退市 正常交易
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,                            -- 创建时间，
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP -- 更新时间，默认当前时间并在更新时自动刷新
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
