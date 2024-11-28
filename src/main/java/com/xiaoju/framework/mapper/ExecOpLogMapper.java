package com.xiaoju.framework.mapper;

import com.xiaoju.framework.entity.persistent.CaseBackup;
import com.xiaoju.framework.entity.persistent.ExecOpLog;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


/**
 * 每次操作record的执行记录
 *
 * @author 任江涛
 * @date 2022/11/14
 * @see CaseBackup
 */

@Repository
public interface ExecOpLogMapper {

    int insert(ExecOpLog execOpLog);

    //int update(ExecOpLog execOpLog);

    //int delete(Long id);

    List<ExecOpLog> searchByUseridAndRecordId(@Param("userid") long userid, @Param("recordId") long recordId);
}
