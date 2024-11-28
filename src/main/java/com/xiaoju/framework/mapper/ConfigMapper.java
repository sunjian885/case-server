package com.xiaoju.framework.mapper;

import com.xiaoju.framework.entity.dto.Config;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConfigMapper {

    Config getConfig(@Param("type") String type,@Param("key") String key);

    List<Config> getAllByType(@Param("type") String type);

    Config selectByPrimaryKey(Long id);


    int insert(Config config);

    int updateByPrimaryKeySelective(Config config);

    Config selectByKey(String key);


    int deleteByPrimaryKey(Long id);
}
