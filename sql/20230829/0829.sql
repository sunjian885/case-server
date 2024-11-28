create table config
(
    id              bigint unsigned auto_increment comment '主键id'  primary key,
    type            varchar(255)  default ''                not null comment '配置类型',
    `key`             varchar(255) default ''                 not null comment '配置的key',
    value           varchar(4096) default ''                not null comment '配置的value',
    status          tinyint       default 1                 not null comment '状态：1是有效，0 无效',
    gmt_updated     timestamp     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    gmt_created     timestamp     default CURRENT_TIMESTAMP not null comment '创建时间'
)ENGINE=InnoDB AUTO_INCREMENT=0 DEFAULT CHARSET=utf8mb4 COMMENT='配置表';

create index idx_type_key
    on config (type, key);

create index idx_key
    on config (key);


create table ai_result
(
    id              bigint unsigned auto_increment comment '主键id'  primary key,
    userid          bigint                                  not null comment '用户杉泰id',
    prompt          varchar(4096)  default ''               not null comment '提示词',
    token           varchar(128) default ''                 not null comment '获取结果用的token',
    result          varchar(4096) default ''                not null comment '配置的value',
    `status`          tinyint       default 1                 not null comment '结果：1是成功，0是失败',
    gmt_updated     timestamp     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    gmt_created     timestamp     default CURRENT_TIMESTAMP not null comment '创建时间'
)ENGINE=InnoDB AUTO_INCREMENT=0 DEFAULT CHARSET=utf8mb4 COMMENT='ai结果表';


create index idx_type_key
    on ai_result (type, key);

create index idx_userid
    on ai_result (userid);


create index idx_token
    on ai_result (token);