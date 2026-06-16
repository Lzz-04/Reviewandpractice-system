package com.examreview.dto;

import lombok.Data;

import java.util.List;

@Data
public class AIGeneratedQuestion {

    /** 题型：single/multiple/judge */
    private String type;

    /** 题目内容 */
    private String content;

    /** 选项列表，如 ["A. 选项A", "B. 选项B", ...] */
    private List<String> options;

    /** 正确答案 */
    private String answer;

    /** 题目解析 */
    private String analysis;

    /** 难度 1-5 */
    private Integer difficulty;

    // 以下为前端预览辅助字段，非持久化

    /** 前端勾选状态 */
    private transient boolean selected;
}
