alter table config add column prompt varchar(2048) not null comment '请求提示语' AFTER `key`;


