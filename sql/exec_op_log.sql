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
DROP TABLE IF EXISTS `exec_op_log`;
CREATE TABLE `exec_op_log` (
                                      `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键id',
                                      `case_id` bigint NOT NULL DEFAULT '0' COMMENT '执行的用例id',
                                      `record_id` bigint NOT NULL DEFAULT '0' COMMENT '执行用例的record id',
                                      `username` varchar(20) NOT NULL DEFAULT '0' COMMENT '执行用例的用户名',
                                      `userid` bigint NOT NULL DEFAULT '0' COMMENT '执行用例的user id',
                                      `case_content` longtext COMMENT '任务执行内容',
                                      `gmt_created` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
                                      `gmt_modified` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录修改时间',
                                      PRIMARY KEY (`id`),
                                      key `index_userId` (`userid`),
                                      key `index_recordId` (`record_id`),
                                      key `index_caseId` (`case_id`)
) ENGINE=InnoDB AUTO_INCREMENT=0 DEFAULT CHARSET=utf8mb4 COMMENT='用例操作记录';

SET FOREIGN_KEY_CHECKS = 1;
