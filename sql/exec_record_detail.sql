/*
 Navicat MySQL Data Transfer

 Source Server         : 本地数据库-case-server
 Source Server Type    : MySQL
 Source Server Version : 80026
 Source Host           : localhost:3306
 Source Schema         : case-server

 Target Server Type    : MySQL
 Target Server Version : 80026
 File Encoding         : 65001

 Date: 23/11/2022 19:28:35
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for exec_record
-- ----------------------------
DROP TABLE IF EXISTS `exec_record_detail`;
CREATE TABLE `exec_record_detail` (
                                      `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键id',
                                      `case_id` bigint NOT NULL DEFAULT '0' COMMENT '执行的用例id',
                                      `record_id` bigint NOT NULL DEFAULT '0' COMMENT '执行用例的record id',
                                      `username` varchar(20) NOT NULL DEFAULT '0' COMMENT '执行用例的用户名',
                                      `userid` bigint NOT NULL DEFAULT '0' COMMENT '执行用例的user id',
                                      `title` varchar(64) NOT NULL DEFAULT '' COMMENT '用例名称',
                                      `env` int NOT NULL DEFAULT '0' COMMENT '执行环境： 0、测试环境 1、预发环境 2.线上环境 3.冒烟qa 4.冒烟rd',
                                      `case_content` longtext COMMENT '任务执行内容',
                                      `is_delete` int NOT NULL DEFAULT '0' COMMENT '用例状态 0-正常 1-删除',
                                      `exec_count` int NOT NULL DEFAULT '0' COMMENT '执行个数pass',
                                      `success_count` int NOT NULL DEFAULT '0' COMMENT '成功个数',
                                      `ignore_count` int NOT NULL DEFAULT '0' COMMENT '不执行个数',
                                      `block_count` int NOT NULL DEFAULT '0' COMMENT '阻塞个数',
                                      `fail_count` int NOT NULL DEFAULT '0' COMMENT '失败个数',
                                      `creator` varchar(20) NOT NULL DEFAULT '' COMMENT '用例创建人',
                                      `gmt_created` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
                                      `gmt_modified` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录修改时间',
                                      `expect_start_time` timestamp NOT NULL DEFAULT '1971-01-01 00:00:00' COMMENT '预计开始时间',
                                      `expect_end_time` timestamp NOT NULL DEFAULT '1971-01-01 00:00:00' COMMENT '预计结束时间',
                                      PRIMARY KEY (`id`),
                                      KEY `idx_caseId_isdelete` (`case_id`,`is_delete`),
                                      KEY `idx_recordId_userId` (`record_id`,`userid`),
) ENGINE=InnoDB AUTO_INCREMENT=909 DEFAULT CHARSET=utf8mb3 COMMENT='用例执行详细记录';

SET FOREIGN_KEY_CHECKS = 1;
