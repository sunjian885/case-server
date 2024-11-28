package com.xiaoju.framework.mapper;


import com.xiaoju.framework.entity.dto.AIResult;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AIResultMapper {

    int insert(AIResult aiResult);

    int updateByToken(AIResult aiResult);

    int updateByPrimaryKey(AIResult aiResult);

    AIResult selectByPrimaryKey(Long id);

    AIResult selectByUserid(Long userid);

    int deleteByPrimaryKey(Long id);

    List<AIResult> selectAIResults(@Param("username") String username,
                                   @Param("userid") Long userid,
                                   @Param("id") Long id);
}
