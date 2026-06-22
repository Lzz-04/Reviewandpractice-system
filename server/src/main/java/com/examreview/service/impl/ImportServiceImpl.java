package com.examreview.service.impl;

import com.examreview.dto.ImportResultDTO;
import com.examreview.entity.Question;
import com.examreview.mapper.QuestionMapper;
import com.examreview.service.ImportService;
import lombok.RequiredArgsConstructor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 文件导入服务实现类
 * 支持 XLSX/DOCX/TXT 三种格式的题目解析和批量导入
 * 文本解析支持两种模式：标准标签格式和试卷格式自动识别
 */
@Service
@RequiredArgsConstructor
public class ImportServiceImpl implements ImportService {

    private final QuestionMapper questionMapper;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public String previewExtract(MultipartFile file) throws IOException {
        // 文件名校验
        String filename = file.getOriginalFilename();
        if (filename == null) {
            throw new IllegalArgumentException("文件名不能为空");
        }
        
        // 根据扩展名选择解析器
        String ext = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
        List<Question> questions = switch (ext) {
            case "xlsx" -> parseXlsx(file, null);
            case "docx" -> parseDocx(file, null);
            case "txt" -> parseTxt(file, null);
            default -> throw new IllegalArgumentException("不支持的文件格式：" + ext + "，仅支持 docx、xlsx、txt");
        };

        // 只取前 10 道单选题用于预览
        List<Question> singles = questions.stream()
            .filter(q -> "single".equals(q.getType()))
            .limit(10)
            .toList();

        // 格式化为标准文本格式
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < singles.size(); i++) {
            Question q = singles.get(i);
            sb.append("题型：单选\n");
            sb.append("题目：").append(q.getContent()).append("\n");
            try {
                @SuppressWarnings("unchecked")
                List<Map<String, String>> opts = OBJECT_MAPPER.readValue(q.getOptions(), List.class);
                for (Map<String, String> opt : opts) {
                    sb.append(opt.get("label")).append(".").append(opt.get("text")).append("\n");
                }
            } catch (Exception ignored) {}
            sb.append("答案：").append(q.getAnswer());
            if (i < singles.size() - 1) {
                sb.append("\n\n");
            }
        }
        return sb.toString();
    }

    @Override
    @Transactional
    public ImportResultDTO importQuestions(MultipartFile file, Integer subjectId, Long userId) throws IOException {
        // 文件名校验
        String filename = file.getOriginalFilename();
        if (filename == null) {
            throw new IllegalArgumentException("文件名不能为空");
        }

        // 根据扩展名选择解析器
        String ext = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
        List<Question> questions = switch (ext) {
            case "xlsx" -> parseXlsx(file, subjectId);
            case "docx" -> parseDocx(file, subjectId);
            case "txt" -> parseTxt(file, subjectId);
            default -> throw new IllegalArgumentException("不支持的文件格式：" + ext + "，仅支持 docx、xlsx、txt");
        };

        // 统计导入结果
        ImportResultDTO result = new ImportResultDTO();
        result.setTotal(questions.size());

        // 逐条插入，记录成功/失败数量
        for (Question q : questions) {
            try {
                validateQuestion(q);
                q.setUserId(userId);
                questionMapper.insert(q);
                result.setSuccess(result.getSuccess() + 1);
            } catch (Exception e) {
                result.setFailed(result.getFailed() + 1);
                result.getErrors().add("题目「" + truncate(q.getContent(), 30) + "」导入失败：" + e.getMessage());
            }
        }
        return result;
    }

    // ==================== XLSX 解析 ====================
    
    /**
     * 解析 Excel 文件
     * 假设第一行为表头，从第二行开始读取题目数据
     * 列顺序：题型 | 题目内容 | A 选项 | B 选项 | C 选项 | D 选项 | E 选项 | F 选项 | 答案 | 解析 | 难度
     */
    private List<Question> parseXlsx(MultipartFile file, Integer subjectId) throws IOException {
        List<Question> list = new ArrayList<>();
        try (XSSFWorkbook wb = new XSSFWorkbook(file.getInputStream())) {
            var sheet = wb.getSheetAt(0);
            // 第一行为表头，从第二行开始读
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                var row = sheet.getRow(i);
                if (row == null) continue;

                String type = getCellStr(row, 0);
                String content = getCellStr(row, 1);
                if (type.isEmpty() || content.isEmpty()) continue;

                Question q = new Question();
                q.setSubjectId(subjectId);
                q.setType(mapType(type));
                q.setContent(content);

                // 构建 options JSON
                List<Map<String, String>> options = new ArrayList<>();
                String[] labels = {"A", "B", "C", "D", "E", "F"};
                for (int j = 0; j < labels.length; j++) {
                    String optText = getCellStr(row, 2 + j);
                    if (!optText.isEmpty()) {
                        options.add(Map.of("label", labels[j], "text", optText));
                    }
                }
                q.setOptions(toJson(options));
                q.setAnswer(getCellStr(row, 8));
                q.setAnalysis(getCellStr(row, 9));
                try { q.setDifficulty(Integer.parseInt(getCellStr(row, 10))); } catch (Exception ignored) {}
                list.add(q);
            }
        }
        return list;
    }

    // ==================== DOCX 解析 ====================
    
    /**
     * 解析 Word 文档
     * 提取所有段落文本后，调用文本解析器处理
     */
    private List<Question> parseDocx(MultipartFile file, Integer subjectId) throws IOException {
        try (XWPFDocument doc = new XWPFDocument(file.getInputStream())) {
            StringBuilder sb = new StringBuilder();
            doc.getParagraphs().forEach(p -> sb.append(p.getText()).append("\n"));
            return parseText(sb.toString(), subjectId);
        }
    }
    // ==================== TXT 解析 ====================
    
    /**
     * 解析 TXT 文件
     * 按 UTF-8 编码读取后，调用文本解析器处理
     */
    private List<Question> parseTxt(MultipartFile file, Integer subjectId) throws IOException {
        String text = new String(file.getBytes(), java.nio.charset.StandardCharsets.UTF_8);
        return parseText(text, subjectId);
    }

    // ==================== 文本格式解析（DOCX/TXT 通用） ====================
    
    /**
     * 文本格式解析入口
     * 自动识别格式：有"题型："标签 → 标准格式，否则有编号 + 内联选项 → 试卷格式
     */
    private List<Question> parseText(String text, Integer subjectId) {
        // 格式检测：有 题型：标签 → 标准格式，否则有编号+内联选项 → 试卷格式
        if (isExamPaperFormat(text)) {
            return parseExamPaper(text, subjectId);
        }
        List<Question> list = new ArrayList<>();
        // 按空白行分割题目块
        String[] blocks = text.split("\n\\s*\n");
        for (String block : blocks) {
            block = block.trim();
            if (block.isEmpty()) continue;
            Question q = parseTextBlock(block);
            if (q != null) {
                q.setSubjectId(subjectId);
                list.add(q);
            }
        }
        return list;
    }

    private static final Pattern EXAM_PAPER_CHECK = Pattern.compile("(?m)^\\d+[.、．]\\s");

    /**
     * 判断是否为试卷格式
     * 试卷格式特征：有题号（1、2、3...）且无"题型："标签
     */
    private boolean isExamPaperFormat(String text) {
        if (text.contains("题型：") || text.contains("题型:")) return false;
        return EXAM_PAPER_CHECK.matcher(text).find();
    }

    // ==================== 试卷格式解析 ====================
    
    /**
     * 解析试卷格式文本
     * 特征：题号 + 内联选项 + 答案区
     */
    private List<Question> parseExamPaper(String text, Integer subjectId) {
        text = text.replace("\r\n", "\n").replace("\r", "\n");

        // 1. 先定位答案区边界：编号重置为1且紧跟单个答案字母的短行
        int answerStart = text.length();
        Pattern ansRestart = Pattern.compile("\\b1\\s*[.、．]\\s*[A-FT√×对错]");
        Matcher arm = ansRestart.matcher(text);
        while (arm.find()) {
            int after = arm.end();
            int nl = text.indexOf('\n', after);
            String rest = (nl == -1) ? text.substring(after).trim() : text.substring(after, nl).trim();
            if (rest.length() <= 4) {
                answerStart = arm.start();
                break;
            }
        }

        // 2. 找题目区所有题号位置（只取答案区之前的）
        Pattern qNum = Pattern.compile("(?<=^|[^A-Za-z\\d])(\\d+)[.、．]\\s");
        Matcher qm = qNum.matcher(text);
        java.util.List<Integer> starts = new java.util.ArrayList<>();
        while (qm.find()) {
            if (qm.start() < answerStart) starts.add(qm.start());
        }
        if (starts.isEmpty()) return java.util.Collections.emptyList();

        // 3. 解析答案区
        Map<Integer, String> answerMap = new LinkedHashMap<>();
        if (answerStart < text.length()) {
            Pattern ansEntry = Pattern.compile("(\\d+)\\s*[.、．]\\s*([A-FT√×对错]+)");
            Matcher am = ansEntry.matcher(text.substring(answerStart));
            while (am.find()) {
                int num = Integer.parseInt(am.group(1));
                String ans = am.group(2).toUpperCase();
                answerMap.putIfAbsent(num, ans);
            }
        }

        // 4. 解析题目区
        List<Question> list = new java.util.ArrayList<>();
        for (int i = 0; i < starts.size(); i++) {
            int s = starts.get(i);
            int e = (i + 1 < starts.size()) ? starts.get(i + 1) : answerStart;
            String block = text.substring(s, e).trim();
            if (block.isEmpty()) continue;
            Question q = parseExamQuestion(block, answerMap);
            if (q != null) {
                q.setSubjectId(subjectId);
                list.add(q);
            }
        }
        return list;
    }

    /**
     * 解析单个试卷题目块
     * 提取题号、题干、选项，并从答案映射中获取答案
     */
    private Question parseExamQuestion(String block, Map<Integer, String> answerMap) {
        // 提取题号
        Pattern numPat = Pattern.compile("^(\\d+)[.、．]\\s");
        Matcher nm = numPat.matcher(block);
        if (!nm.find()) return null;
        int qNum = Integer.parseInt(nm.group(1));
        String body = block.substring(nm.end());

        // 找第一个选项标记，切分题干和选项区
        Pattern firstOpt = Pattern.compile("([A-F])\\s*[.、．)]\\s*");
        Matcher om = firstOpt.matcher(body);

        String stem;
        List<Map<String, String>> options = new ArrayList<>();

        if (om.find()) {
            stem = body.substring(0, om.start()).trim();
            String optPart = body.substring(om.start());

            // 按选项边界切分
            Pattern optBoundary = Pattern.compile("([A-F])\\s*[.、．)]\\s*");
            Matcher obm = optBoundary.matcher(optPart);
            int lastEnd = 0;
            String lastLabel = null;
            while (obm.find()) {
                if (lastLabel != null) {
                    String optText = optPart.substring(lastEnd, obm.start()).trim();
                    options.add(Map.of("label", lastLabel, "text", optText));
                }
                lastLabel = obm.group(1);
                lastEnd = obm.end();
            }
            if (lastLabel != null) {
                String optText = optPart.substring(lastEnd).trim();
                options.add(Map.of("label", lastLabel, "text", optText));
            }
        } else {
            stem = body.trim();
        }

        // 清理题干尾部空白括号
        stem = stem.replaceAll("[（(]\\s*[）)]\\s*$", "").trim();
        if (stem.isEmpty()) return null;

        String answer = answerMap.get(qNum);
        if (answer == null) return null;

        Question q = new Question();
        q.setType(detectExamType(answer));
        q.setContent(stem);
        q.setAnswer(answer);
        q.setDifficulty(1);
        q.setOptions(toJson(options));
        return q;
    }

    /**
     * 根据答案判断题型
     */
    private String detectExamType(String answer) {
        if (answer.matches("[TF√×对错]")) return "judge";
        if (answer.length() > 1) return "multiple";
        return "single";
    }

    // ==================== 标准格式解析 ====================
    
    /**
     * 解析标准格式题目块
     * 标准格式标签：题型：/题目：/A./B./C./D./答案：/解析：
     */
    private Question parseTextBlock(String block) {
        String[] lines = block.split("\n");
        Question q = new Question();
        q.setDifficulty(1);

        List<Map<String, String>> options = new ArrayList<>();
        StringBuilder contentLines = new StringBuilder();

        for (String rawLine : lines) {
            String line = rawLine.trim();
            if (line.isEmpty()) continue;

            // 题型
            if (line.startsWith("题型：") || line.startsWith("题型:")) {
                q.setType(mapType(line.substring(3).trim()));
                continue;
            }
            // 答案
            if (line.startsWith("答案：") || line.startsWith("答案:")) {
                q.setAnswer(line.substring(3).trim().toUpperCase());
                continue;
            }
            // 解析
            if (line.startsWith("解析：") || line.startsWith("解析:")) {
                q.setAnalysis(line.substring(3).trim());
                continue;
            }
            // 难度
            if (line.startsWith("难度：") || line.startsWith("难度:")) {
                try { q.setDifficulty(Integer.parseInt(line.substring(3).trim())); } catch (Exception ignored) {}
                continue;
            }
            // 题目
            if (line.startsWith("题目：") || line.startsWith("题目:")) {
                contentLines.append(line.substring(3).trim());
                continue;
            }
            // 选项 (A. xxx / B. xxx)
            Pattern optPattern = Pattern.compile("^([A-F])\\.\\s*(.+)$");
            Matcher m = optPattern.matcher(line);
            if (m.matches()) {
                options.add(Map.of("label", m.group(1), "text", m.group(2).trim()));
                continue;
            }
            // 其他行作为题目内容追加
            contentLines.append(line);
        }

        if (q.getType() == null || contentLines.isEmpty() || q.getAnswer() == null) {
            return null;
        }
        q.setContent(contentLines.toString().trim());
        q.setOptions(toJson(options));
        return q;
    }

    // ==================== 工具方法 ====================

    /**
     * 获取单元格字符串值
     */
    private String getCellStr(org.apache.poi.ss.usermodel.Row row, int idx) {
        var cell = row.getCell(idx);
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> {
                double v = cell.getNumericCellValue();
                yield v == Math.floor(v) ? String.valueOf((long) v) : String.valueOf(v);
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> "";
        };
    }

    /**
     * 题型映射：中文/英文 → 标准题型（single/multiple/judge）
     */
    private String mapType(String raw) {
        String s = raw.trim();
        if (s.contains("单选") || s.equalsIgnoreCase("single")) return "single";
        if (s.contains("多选") || s.equalsIgnoreCase("multiple")) return "multiple";
        if (s.contains("判断") || s.equalsIgnoreCase("judge")) return "judge";
        return s;
    }

    /**
     * 选项列表转 JSON 字符串
     */
    private String toJson(List<Map<String, String>> options) {
        try {
            return OBJECT_MAPPER.writeValueAsString(options);
        } catch (Exception e) {
            return "[]";
        }
    }

    /**
     * 校验题目基本信息
     */
    private void validateQuestion(Question q) {
        if (q.getType() == null || q.getType().isEmpty()) throw new IllegalArgumentException("题型不能为空");
        if (!List.of("single", "multiple", "judge").contains(q.getType()))
            throw new IllegalArgumentException("无效题型：" + q.getType());
        if (q.getContent() == null || q.getContent().trim().isEmpty())
            throw new IllegalArgumentException("题目内容不能为空");
        if (q.getAnswer() == null || q.getAnswer().trim().isEmpty())
            throw new IllegalArgumentException("答案不能为空");
    }

    /**
     * 字符串截断（用于错误提示）
     */
    private String truncate(String s, int max) {
        return s != null && s.length() > max ? s.substring(0, max) + "…" : s;
    }
}
