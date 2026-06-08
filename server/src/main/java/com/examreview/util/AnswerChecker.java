package com.examreview.util;

import org.springframework.stereotype.Component;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class AnswerChecker {

    /**
     * 比对用户答案与正确答案
     * @param type 题型: single, multiple, judge
     * @param userAnswer 用户提交的答案
     * @param correctAnswer 正确答案
     * @return 是否正确
     */
    public boolean check(String type, String userAnswer, String correctAnswer) {
        if (userAnswer == null || correctAnswer == null) {
            return false;
        }

        switch (type) {
            case "single":
                return userAnswer.trim().equalsIgnoreCase(correctAnswer.trim());
            case "judge":
                return userAnswer.trim().equalsIgnoreCase(correctAnswer.trim());
            case "multiple":
                return checkMultiple(userAnswer, correctAnswer);
            default:
                return false;
        }
    }

    private boolean checkMultiple(String userAnswer, String correctAnswer) {
        List<String> userList = parseMultiple(userAnswer);
        List<String> correctList = parseMultiple(correctAnswer);
        
        if (userList.size() != correctList.size()) {
            return false;
        }
        
        Collections.sort(userList);
        Collections.sort(correctList);
        
        return userList.equals(correctList);
    }

    private List<String> parseMultiple(String answer) {
        return Arrays.stream(answer.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }
}
