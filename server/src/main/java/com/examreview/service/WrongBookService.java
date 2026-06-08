package com.examreview.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
public class WrongBookService {

    /**
     * 更新或插入错题记录
     * @param userId 用户ID
     * @param questionId 题目ID
     */
    @Transactional
    public void upsertWrongQuestion(Long userId, Long questionId) {
        // 逻辑实现：
        // 1. 检查是否存在该用户的该错题记录
        // 2. 如果存在：wrongCount++, mastered = 0, lastWrongAt = now
        // 3. 如果不存在：插入新记录，wrongCount = 1, mastered = 0
        System.out.println("Upserting wrong question for user " + userId + ", question " + questionId);
    }
}
