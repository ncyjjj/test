package com.materialmaker.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.materialmaker.config.SimulationProperties;
import com.materialmaker.mapper.MaterialMapper;
import com.materialmaker.mapper.TaskMapper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class TaskProcessor {
    private final TaskMapper taskMapper;
    private final MaterialMapper materialMapper;
    private final SimulationProperties simulationProperties;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public TaskProcessor(TaskMapper taskMapper, MaterialMapper materialMapper, SimulationProperties simulationProperties) {
        this.taskMapper = taskMapper;
        this.materialMapper = materialMapper;
        this.simulationProperties = simulationProperties;
    }

    @Async("taskExecutor")
    public void execute(String taskId) {
        try {
            // Step 1: ASSET_CHECK
            updateStage(taskId, "PROCESSING", "ASSET_CHECK");
            taskMapper.insertEvent(taskId, "ASSET_CHECK", "START", "开始素材检查");
            sleep(simulationProperties.getDelayMs().getAssetCheck());
            if (shouldFail("ASSET_CHECK")) {
                fail(taskId, "ASSET_CHECK", "模拟失败：素材检查未通过（可在配置中调整 fail-at-stage）");
                return;
            }
            // 简单校验：素材是否存在
            var materials = materialMapper.findByTaskId(taskId);
            if (materials == null || materials.isEmpty()) {
                fail(taskId, "ASSET_CHECK", "未找到已上传素材");
                return;
            }
            taskMapper.insertEvent(taskId, "ASSET_CHECK", "DONE", "素材检查通过");

            // Step 2: GENERATE
            updateStage(taskId, "PROCESSING", "GENERATE");
            taskMapper.insertEvent(taskId, "GENERATE", "START", "开始模拟生成");
            sleep(simulationProperties.getDelayMs().getGenerate());
            if (shouldFail("GENERATE")) {
                fail(taskId, "GENERATE", "模拟失败：生成环节异常（可在配置中调整 fail-at-stage）");
                return;
            }
            taskMapper.insertEvent(taskId, "GENERATE", "DONE", "模拟生成完成");

            // Step 3: FINALIZE
            updateStage(taskId, "PROCESSING", "FINALIZE");
            taskMapper.insertEvent(taskId, "FINALIZE", "START", "开始结果整理");
            sleep(simulationProperties.getDelayMs().getFinalize());
            if (shouldFail("FINALIZE")) {
                fail(taskId, "FINALIZE", "模拟失败：结果整理异常（可在配置中调整 fail-at-stage）");
                return;
            }
            // 生成结果文件（复制示例结果）
            Path resultDir = Paths.get("uploads", "results", taskId);
            Files.createDirectories(resultDir);
            Path src = Paths.get("backend", "src", "main", "resources", "static", "results", "result-sample.svg");
            if (!Files.exists(src)) {
                // 回退：直接在运行目录下也可能
                src = Paths.get("src", "main", "resources", "static", "results", "result-sample.svg");
            }
            Path dst = resultDir.resolve("result.svg");
            if (Files.exists(src)) {
                Files.copy(src, dst, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            } else {
                String content = "<svg width='800' height='500' xmlns='http://www.w3.org/2000/svg'><rect width='800' height='500' fill='#111827'/><text x='400' y='250' font-size='32' fill='#22d3ee' text-anchor='middle'>Material Maker Result</text></svg>";
                Files.writeString(dst, content);
            }
            String resultUrl = "/uploads/results/" + taskId + "/result.svg";

            Map<String, Object> meta = new HashMap<>();
            meta.put("generatedAt", LocalDateTime.now().toString());
            meta.put("steps", new String[]{"ASSET_CHECK", "GENERATE", "FINALIZE"});
            com.materialmaker.model.Task t = new com.materialmaker.model.Task();
            t.setId(taskId);
            t.setStage("FINALIZE");
            t.setResultUrl(resultUrl);
            try {
                t.setResultMeta(objectMapper.writeValueAsString(meta));
            } catch (Exception ignored) {}
            taskMapper.updateSuccess(t);
            taskMapper.insertEvent(taskId, "FINALIZE", "DONE", "结果整理完成，任务成功");
        } catch (Exception e) {
            fail(taskId, "GENERATE", "后台处理异常：" + e.getMessage());
        }
    }

    private void updateStage(String taskId, String status, String stage) {
        com.materialmaker.model.Task t = new com.materialmaker.model.Task();
        t.setId(taskId);
        t.setStatus(status);
        t.setStage(stage);
        taskMapper.updateStatusAndStage(t);
        taskMapper.insertEvent(taskId, stage, "PROGRESS", status);
    }

    private boolean shouldFail(String stage) {
        String cfg = simulationProperties.getFailAtStage();
        if (cfg == null) return false;
        return stage.equals(cfg.trim());
    }

    private void fail(String taskId, String stage, String reason) {
        com.materialmaker.model.Task t = new com.materialmaker.model.Task();
        t.setId(taskId);
        t.setStage(stage);
        t.setFailStage(stage);
        t.setFailReason(reason);
        taskMapper.updateFailed(t);
        taskMapper.insertEvent(taskId, stage, "FAIL", reason);
    }

    private void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
    }
}
