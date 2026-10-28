package com.materialmaker.controller;

import com.materialmaker.dto.ApiResponse;
import com.materialmaker.model.Task;
import com.materialmaker.service.TaskService;
import com.materialmaker.util.UserContext;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {
    private final TaskService taskService;
    private final UserContext userContext;

    public TaskController(TaskService taskService, UserContext userContext) {
        this.taskService = taskService;
        this.userContext = userContext;
    }

    @PostMapping
    public ApiResponse<Task> create(@RequestParam("templateId") String templateId,
                                    @RequestParam("files") List<MultipartFile> files) throws IOException {
        String userId = userContext.getUserId();
        try {
            Task task = taskService.createTask(userId, templateId, files);
            return ApiResponse.ok("task accepted", task);
        } catch (IllegalArgumentException e) {
            return ApiResponse.fail(e.getMessage());
        }
    }

    @GetMapping("/{taskId}")
    public ApiResponse<Task> get(@PathVariable("taskId") String taskId) {
        Task task = taskService.findTask(taskId);
        if (task == null) {
            return ApiResponse.fail("task not found");
        }
        // 只读查询，不推进任务
        return ApiResponse.ok(task);
    }

    @GetMapping
    public ApiResponse<List<Task>> list() {
        String userId = userContext.getUserId();
        return ApiResponse.ok(taskService.findUserTasks(userId));
    }
}
