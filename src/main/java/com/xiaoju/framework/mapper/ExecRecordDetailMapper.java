package com.xiaoju.framework.mapper;

import com.xiaoju.framework.entity.persistent.ExecRecordDetail;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;


/**
 * 详细到个人的执行记录
 *
 * @author 任江涛
 * @date 2022/11/14
 * @see com.xiaoju.framework.entity.persistent.ExecRecordDetail
 */

@Repository
public interface ExecRecordDetailMapper {

    /**
     * 插入一条操作记录
     * @return
     */
    int insert(ExecRecordDetail execRecordDetail);


    /**
     * 更新一条操作记录
     * @return
     */
    int update(@Param("execRecordDetail") ExecRecordDetail execRecordDetail, @Param("oldVersion") Integer oldVersion);


    /**
     *  删除某个recordi对应的所有执行记录
     * @param recordId
     * @return
     */
    int delete(@Param("recordId") Long recordId);
    /**
     * 查询某个用户的所有执行记录
     * @return
     */
    List<ExecRecordDetail> searchExecRecordDetailsByUserid(@Param("userid") Long userid,
                                                           @Param("beginTime") Date beginTime,
                                                           @Param("endTime") Date endTime);


    /**
     * 查询所有用户的所有执行记录
     * @return
     */
    List<ExecRecordDetail> searchExecRecordDetails(@Param("beginTime") Date beginTime,
                                                   @Param("endTime") Date endTime);


    /**
     * 查询所有用户的所有执行记录
     * @return
     */
    List<ExecRecordDetail> searchDetailsByRecordIdAndUserid(@Param("recordId") Long recordId,
                                                   @Param("userid") Long userid);


    List<ExecRecordDetail> searchExecCountsByUserids(@Param("userids") List<Long> userids,
                                                     @Param("beginTime") Date beginTime,
                                                     @Param("endTime") Date endTime);

}
