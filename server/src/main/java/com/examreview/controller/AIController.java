package com.examreview.controller;

import com.examreview.service.AIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AIController {

    @Autowired
    private AIService aiService;

    @PostMapping("/generate")
    public List<Map<String, Object>> generate(@RequestBody Map<String, Object> request) {
        Long subjectId = Long.valueOf(request.get("subjectId").toString());
        Long chapterId = Long.valueOf(request.get("chapterId").toString());
        String type = (String) request.get("type");
        int count = (int) request.get("count");
        
        return aiService.generateQuestions(subjectId, chapterId, type, count);
    }
}
