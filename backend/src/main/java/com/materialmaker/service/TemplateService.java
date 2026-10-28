package com.materialmaker.service;

import com.materialmaker.mapper.TemplateMapper;
import com.materialmaker.model.Template;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TemplateService {
    private final TemplateMapper templateMapper;

    public TemplateService(TemplateMapper templateMapper) {
        this.templateMapper = templateMapper;
    }

    public List<Template> list(String category) {
        return templateMapper.findAllEnabled(category);
    }

    public Template get(String id) {
        return templateMapper.findById(id);
    }
}
