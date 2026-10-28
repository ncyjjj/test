package com.materialmaker.mapper;

import com.materialmaker.model.Task;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TaskMapper {
    int insert(Task task);
    Task findById(@Param("id") String id);
    List<Task> findByUserId(@Param("userId") String userId);
    int updateStatusAndStage(Task task);
    int updateSuccess(Task task);
    int updateFailed(Task task);
    int insertEvent(@Param("taskId") String taskId, @Param("stage") String stage, @Param("event") String event, @Param("detail") String detail);
}
