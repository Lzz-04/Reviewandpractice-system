package com.examreview.util;

import com.examreview.entity.Question;

import java.util.Arrays;

public final class AnswerChecker {

    private AnswerChecker() {
    }

    public static boolean checkAnswer(Question question, String selected) {
        if (selected == null || selected.isEmpty()) return false;
        String correct = question.getAnswer();

        if ("judge".equals(question.getType())) {
            return correct.equalsIgnoreCase(selected);
        }

        String[] selArr = selected.replace(" ", "").split(",");
        String[] corArr = correct.replace(" ", "").split(",");
        Arrays.sort(selArr);
        Arrays.sort(corArr);
        return Arrays.equals(selArr, corArr);
    }
}
