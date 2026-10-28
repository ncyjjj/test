package com.materialmaker.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.materialmaker.config.SimulationProperties;
import com.materialmaker.mapper.MaterialMapper;
import com.materialmaker.mapper.TaskMapper;
import com.materialmaker.mapper.TemplateMapper;
import com.materialmaker.model.Material;
import com.materialmaker.model.Task;
import com.materialmaker.model.Template;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class TaskService {
    private final TaskMapper taskMapper;
    private final MaterialMapper materialMapper;
    private final TemplateMapper templateMapper;
    private final TaskProcessor taskProcessor;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public TaskService(TaskMapper taskMapper, MaterialMapper materialMapper, TemplateMapper templateMapper, TaskProcessor taskProcessor) {
        this.taskMapper = taskMapper;
        this.materialMapper = materialMapper;
        this.templateMapper = templateMapper;
        this.taskProcessor = taskProcessor;
    }

    @Transactional
    public Task createTask(String userId, String templateId, List<MultipartFile> files) throws IOException {
        Template template = templateMapper.findById(templateId);
        if (template == null) {
            throw new IllegalArgumentException("template not found");
        }
        // 服务端校验：数量
        if (files == null || files.size() != template.getRequiredImageCount()) {
            throw new IllegalArgumentException("material count mismatch");
        }
        // 校验类型和大小
        List<String> accepted = Arrays.asList(objectMapper.readValue(template.getAcceptedMimeTypes(), String[].class));
        for (MultipartFile f : files) {
            if (f.isEmpty()) throw new IllegalArgumentException("empty file");
            if (!accepted.contains(f.getContentType())) throw new IllegalArgumentException("invalid mime type");
            if (f.getSize() > template.getMaxFileSizeBytes()) throw new IllegalArgumentException("file too large");
        }

        String taskId = UUID.randomUUID().toString();
        Task task = new Task();
        task.setId(taskId);
        task.setUserId(userId);
        task.setTemplateId(templateId);
        task.setStatus("WAITING");
        task.setStage(null);
        Map<String, Object> snapshot = new HashMap<>();
        snapshot.put("templateId", templateId);
        snapshot.put("fileCount", files.size());
        task.setRequestSnapshot(objectMapper.writeValueAsString(snapshot));
        taskMapper.insert(task);

        // 保存素材到本地
        Path uploadDir = Paths.get("uploads", "materials", taskId);
        Files.createDirectories(uploadDir);
        int idx = 0;
        for (MultipartFile f : files) {
            idx++;
            String ext = getExt(f.getOriginalFilename());
            String materialId = UUID.randomUUID().toString();
            String storedName = materialId + (ext.isEmpty() ? "" : "." + ext);
            Path stored = uploadDir.resolve(storedName);
            Files.copy(f.getInputStream(), stored);
            Material m = new Material();
            m.setId(materialId);
            m.setTaskId(taskId);
            m.setOriginalName(f.getOriginalFilename());
            m.setMimeType(f.getContentType());
            m.setSizeBytes(f.getSize());
            m.setStoredPath(stored.toString());
            m.setPublicUrl("/uploads/materials/" + taskId + "/" + storedName);
            materialMapper.insert(m);
        }

        // 提交异步处理（不等待）
        taskProcessor.execute(taskId);
        return findTask(taskId);
    }

    public Task findTask(String taskId) {
        return taskMapper.findById(taskId);
    }

    public List<Task> findUserTasks(String userId) {
        return taskMapper.findByUserId(userId);
    }

    private String getExt(String name) {
        if (name == null) return "";
        int i = name.lastIndexOf('.');
        if (i < 0) return "";
        return name.substring(i + 1).toLowerCase();
    }
}
