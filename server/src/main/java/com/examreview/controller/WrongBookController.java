package com.examreview.controller;

import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/wrongbook")
public class WrongBookController {

    @GetMapping
    public Object list(@RequestParam(required = false) Long subjectId, 
                       @RequestParam(required = false) Integer mastered,
                       @RequestParam(defaultValue = "1") int page) {
        // 返回分页错题列表
        return null;
    }

    @GetMapping("/stats")
    public Map<String, Object> getStats() {
        // 返回统计数据: total, mastered, unMastered
        return Map.of("total", 0, "mastered", 0, "unMastered", 0);
    }

    @PostMapping("/{id}/review")
    public void markReview(@PathVariable Long id) {
        // reviewedCount++
    }

    @PostMapping("/{id}/master")
    public void toggleMaster(@PathVariable Long id) {
        // 切换掌握状态 0 <-> 1
    }

    @DeleteMapping("/{id}")
    public void remove(@PathVariable Long id) {
        // 移除错题记录
    }
}
