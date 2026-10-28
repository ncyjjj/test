package com.materialmaker.controller;

import com.materialmaker.config.SimulationProperties;
import com.materialmaker.dto.ApiResponse;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/dev/simulation")
@Profile({"dev", "default"})
public class DevSimulationController {
    private final SimulationProperties simulationProperties;

    public DevSimulationController(SimulationProperties simulationProperties) {
        this.simulationProperties = simulationProperties;
    }

    @GetMapping("/config")
    public ApiResponse<SimulationProperties> get() {
        return ApiResponse.ok(simulationProperties);
    }

    @PostMapping("/config")
    public ApiResponse<Map<String, Object>> set(@RequestBody Map<String, Object> body) {
        if (body.containsKey("failAtStage")) {
            Object v = body.get("failAtStage");
            simulationProperties.setFailAtStage(v == null ? "" : String.valueOf(v));
        }
        if (body.containsKey("delayMs")) {
            Object dm = body.get("delayMs");
            if (dm instanceof Map) {
                Map<?, ?> m = (Map<?, ?>) dm;
                if (m.containsKey("assetCheck")) simulationProperties.getDelayMs().setAssetCheck(Long.parseLong(String.valueOf(m.get("assetCheck"))));
                if (m.containsKey("generate")) simulationProperties.getDelayMs().setGenerate(Long.parseLong(String.valueOf(m.get("generate"))));
                if (m.containsKey("finalize")) simulationProperties.getDelayMs().setFinalize(Long.parseLong(String.valueOf(m.get("finalize"))));
            }
        }
        Map<String, Object> res = new HashMap<>();
        res.put("ok", true);
        res.put("config", simulationProperties);
        return ApiResponse.ok(res);
    }
}
