package com.materialmaker.mapper;

import com.materialmaker.model.Material;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MaterialMapper {
    int insert(Material material);
    List<Material> findByTaskId(@Param("taskId") String taskId);
}
