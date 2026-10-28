package com.materialmaker.mapper;

import com.materialmaker.model.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {
    int insert(User user);
    User findById(@Param("id") String id);
}
