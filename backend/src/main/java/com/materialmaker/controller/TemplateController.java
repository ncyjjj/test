package com.materialmaker.controller;

import com.materialmaker.dto.ApiResponse;
import com.materialmaker.model.Template;
import com.materialmaker.service.TemplateService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/templates")
public class TemplateController {
    private final TemplateService templateService;

    public TemplateController(TemplateService templateService) {
        this.templateService = templateService;
    }

    @GetMapping
    public ApiResponse<List<Template>> list(@RequestParam(value = "category", required = false) String category) {
        return ApiResponse.ok(templateService.list(category));
    }

    @GetMapping("/{id}")
    public ApiResponse<Template> get(@PathVariable("id") String id) {
        Template t = templateService.get(id);
        if (t == null) {
            return ApiResponse.fail("template not found");
        }
        return ApiResponse.ok(t);
    }
}
