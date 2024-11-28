-- auto-generated definition
create table user
(
    id              bigint unsigned auto_increment comment '主键id'
        primary key,
    username        varchar(255)  default ''                not null comment '用户名',
    password        varchar(1023) default ''                not null comment '密码',
    salt            varchar(1023) default ''                not null comment '盐',
    authority_name  varchar(63)   default ''                null,
    is_delete       int           default 0                 not null comment '是否删除',
    channel         int           default 0                 not null comment '渠道',
    userid          bigint                                  not null comment '用户杉泰id',
    real_name       varchar(15)                             not null comment '用户真实姓名，最终展示在前端的字段',
    gmt_updated     timestamp     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    gmt_created     timestamp     default CURRENT_TIMESTAMP not null comment '注册时间',
    product_line_id bigint        default 0                 not null comment '业务线'
)
    comment '用户信息' charset = utf8;




-- auto-generated definition
create table test_case
(
    id              bigint unsigned auto_increment comment '主键id'
        primary key,
    group_id        bigint        default 0                 not null comment '用例集id',
    title           varchar(64)   default 'testcase'        not null comment '用例名称',
    description     varchar(512)  default ''                not null comment '用例描述',
    is_delete       int           default 0                 not null comment '用例状态 0-正常 1-删除',
    creator         varchar(20)   default ''                not null comment '用例创建人',
    modifier        varchar(1000) default ''                not null comment '用例修改人',
    case_content    longtext charset utf8mb4                null,
    gmt_created     timestamp     default CURRENT_TIMESTAMP not null comment '记录创建时间',
    gmt_modified    timestamp     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '修改时间',
    extra           varchar(256)  default ''                not null comment '扩展字段',
    product_line_id bigint        default 0                 not null comment '业务线id 默认0',
    case_type       int           default 0                 not null comment '0-需求用例，1-核心用例，2-冒烟用例',
    module_node_id  bigint        default 0                 not null comment '模块节点id',
    requirement_id  varchar(1000) default '0'               not null comment '需求id',
    smk_case_id     bigint        default 0                 not null comment '冒烟case的id',
    channel         int           default 0                 not null comment '渠道标志 现默认1',
    biz_id          varchar(500)  default '-1'              not null comment '关联的文件夹id'
)
    comment '测试用例' charset = utf8;

create index idx_productline_isdelete
    on test_case (product_line_id, is_delete);

create index idx_requirement_id
    on test_case (requirement_id);




-- auto-generated definition
create table exec_record
(
    id                bigint unsigned auto_increment comment '主键id'
        primary key,
    case_id           bigint        default 0                     not null comment '执行的用例id',
    title             varchar(64)   default ''                    not null comment '用例名称',
    env               int           default 0                     not null comment '执行环境： 0、测试环境 1、预发环境 2.线上环境 3.冒烟qa 4.冒烟rd',
    case_content      longtext                                    null comment '任务执行内容',
    is_delete         int           default 0                     not null comment '用例状态 0-正常 1-删除',
    pass_count        int           default 0                     not null comment '执行个数',
    total_count       int           default 0                     not null comment '需执行总个数',
    success_count     int           default 0                     not null comment '成功个数',
    ignore_count      int           default 0                     not null comment '不执行个数',
    block_count       int           default 0                     not null comment '阻塞个数',
    fail_count        int           default 0                     not null comment '失败个数',
    creator           varchar(20)   default ''                    not null comment '用例创建人',
    modifier          varchar(20)   default ''                    not null comment '用例修改人',
    executors         varchar(200)  default ''                    not null comment '执行人',
    description       varchar(1000) default ''                    not null comment '描述',
    choose_content    varchar(200)  default ''                    not null comment '圈选用例内容',
    gmt_created       timestamp     default CURRENT_TIMESTAMP     not null comment '记录创建时间',
    gmt_modified      timestamp     default CURRENT_TIMESTAMP     not null comment '记录修改时间',
    expect_start_time timestamp     default '1971-01-01 00:00:00' not null comment '预计开始时间',
    expect_end_time   timestamp     default '1971-01-01 00:00:00' not null comment '预计结束时间',
    actual_start_time timestamp     default '1971-01-01 00:00:00' not null comment '实际开始时间',
    actual_end_time   timestamp     default '1971-01-01 00:00:00' not null comment '实际结束时间',
    owner             varchar(200)  default ''                    not null comment '负责人'
)
    comment '用例执行记录' charset = utf8;

create index idx_caseId_isdelete
    on exec_record (case_id, is_delete);





-- auto-generated definition
create table case_backup
(
    id             bigint unsigned auto_increment comment '主键id'
        primary key,
    case_id        bigint       default 0                 not null comment '用例集id',
    title          varchar(64)  default ''                not null comment '用例名称',
    creator        varchar(20)  default ''                not null comment '用例保存人',
    gmt_created    timestamp    default CURRENT_TIMESTAMP not null comment '用例保存时间',
    case_content   longtext charset utf8mb4               null,
    record_content longtext                               null comment '任务执行内容',
    extra          varchar(256) default ''                not null comment '扩展字段',
    is_delete      int          default 0                 not null comment '是否删除'
)
    comment '测试备份' charset = utf8;

create index idx_caseId
    on case_backup (case_id);



-- auto-generated definition
create table biz
(
    id              bigint auto_increment comment '文件夹主键'
        primary key,
    product_line_id bigint    default 0                 not null comment '业务线名称',
    content         mediumtext                          not null comment '文件数内容',
    channel         int       default 0                 not null comment '渠道',
    is_delete       int       default 0                 not null comment '逻辑删除',
    gmt_created     timestamp default CURRENT_TIMESTAMP not null comment '创建时间',
    gmt_modified    timestamp default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间'
)
    comment '文件夹' charset = utf8;
-- auto-generated definition
create table authority
(
    id                bigint unsigned auto_increment comment '主键id'
        primary key,
    authority_name    varchar(63)  default ''                not null comment '权限名称，ROLE_开头，全大写',
    authority_desc    varchar(255) default ''                not null comment '权限描述',
    authority_content varchar(1023)                          not null comment '权限内容，可访问的url，多个时用,隔开',
    gmt_created       timestamp    default CURRENT_TIMESTAMP not null comment '创建时间',
    gmt_updated       timestamp    default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '修改时间'
)ENGINE=InnoDB comment '权限信息' charset = utf8;


INSERT INTO `authority` (id,authority_name,authority_desc,authority_content) VALUES (1, 'ROLE_USER', '普通用户', '/api/dir/list,/api/record/list,/api/record/getRecordInfo,/api/user/**,/api/case/list*');
INSERT INTO `authority` (id,authority_name,authority_desc,authority_content) VALUES (2, 'ROLE_ADMIN', '管理员', '/api/dir/list,/api/backup/**,/api/record/**,/api/file/**,/api/user/**,/api/case/**');
INSERT INTO `authority` (id,authority_name,authority_desc,authority_content) VALUES (3, 'ROLE_SA', '超级管理员','/api/**');

