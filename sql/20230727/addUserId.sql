alter table test_case add creator_id bigint comment '创建者的userid' AFTER creator;
alter table test_case add modifier_id bigint comment '修改者的的userid' AFTER modifier;

alter table case_backup add userid bigint comment '备份人的userid' AFTER creator;