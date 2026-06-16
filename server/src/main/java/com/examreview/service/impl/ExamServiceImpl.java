package com.examreview.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.examreview.dto.ExamGenerateDTO;
import com.examreview.dto.ExamResultDTO;
import com.examreview.dto.ExamSubmitDTO;
import com.examreview.dto.PauseExamDTO;
import com.examreview.dto.QuestionResultItem;
import com.examreview.dto.ResumeExamResponse;
import com.examreview.entity.*;
import com.examreview.exception.BusinessException;
import com.examreview.mapper.*;
import com.examreview.service.ExamService;
import com.examreview.service.WrongBookService;
import com.examreview.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import com.examreview.util.AnswerChecker;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExamServiceImpl implements ExamService {

    private final ExamPaperMapper examPaperMapper;
    private final ExamQuestionMapper examQuestionMapper;
    private final ExamRecordMapper examRecordMapper;
    private final QuestionMapper questionMapper;
    private final AnswerRecordMapper answerRecordMapper;
    private final WrongBookService wrongBookService;
    private final SubjectMapper subjectMapper;

    @Override
    public com.baomidou.mybatisplus.extension.plugins.pagination.Page<ExamPaper> getList(
            Integer page, Integer pageSize, Integer subjectId, Long userId) {
        if (page == null || page < 1) page = 1;
        if (pageSize == null || pageSize < 1) pageSize = 20;
        LambdaQueryWrapper<ExamPaper> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(!SecurityUtil.isAdmin(), ExamPaper::getUserId, userId);
        if (subjectId != null) {
            wrapper.eq(ExamPaper::getSubjectId, subjectId);
        }
        wrapper.orderByDesc(ExamPaper::getCreatedAt);
        return examPaperMapper.selectPage(
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, pageSize), wrapper);
    }

    @Override
    public ExamPaper getById(Integer id, Long userId) {
        ExamPaper paper = examPaperMapper.selectOne(
                new LambdaQueryWrapper<ExamPaper>()
                        .eq(ExamPaper::getId, id)
                        .eq(!SecurityUtil.isAdmin(), ExamPaper::getUserId, userId));
        if (paper == null) {
            throw new BusinessException("试卷不存在");
        }
        return paper;
    }

    @Override
    public ExamPaper create(ExamPaper examPaper, Long userId) {
        examPaper.setUserId(userId);
        examPaperMapper.insert(examPaper);
        return examPaper;
    }

    @Override
    @Transactional
    public ExamPaper generate(ExamGenerateDTO dto, Long userId) {
        // 校验科目存在且属于当前用户
        if (subjectMapper.selectOne(
                new LambdaQueryWrapper<Subject>()
                        .eq(Subject::getId, dto.getSubjectId())
                        .eq(Subject::getUserId, userId)) == null) {
            throw new BusinessException("科目不存在");
        }
        // 查询题目池（仅当前用户）
        List<Question> pool = questionMapper.selectBySubject(dto.getSubjectId(), userId);
        if (dto.getChapterIds() != null && !dto.getChapterIds().isEmpty()) {
            pool = pool.stream()
                    .filter(q -> dto.getChapterIds().contains(q.getChapterId()))
                    .collect(Collectors.toList());
        }
        if (pool.size() < dto.getTotalCount()) {
            throw new BusinessException("题库题目数量不足，当前可用: " + pool.size() + " 道");
        }

        // 洗牌
        Collections.shuffle(pool);

        // 按题型比例分配
        Map<String, List<Question>> byType = pool.stream()
                .collect(Collectors.groupingBy(Question::getType));

        List<Question> selected = new ArrayList<>();
        String[] types = {"single", "multiple", "judge"};
        while (selected.size() < dto.getTotalCount()) {
            boolean anyAdded = false;
            for (String type : types) {
                if (selected.size() >= dto.getTotalCount()) break;
                List<Question> typeList = byType.get(type);
                if (typeList != null && !typeList.isEmpty()) {
                    selected.add(typeList.remove(0));
                    anyAdded = true;
                }
            }
            if (!anyAdded) break;
        }

        // 创建试卷
        ExamPaper paper = new ExamPaper();
        paper.setSubjectId(dto.getSubjectId());
        paper.setTitle(dto.getTitle());
        paper.setDuration(dto.getDuration());
        paper.setQuestionCount(selected.size());
        paper.setUserId(userId);
        examPaperMapper.insert(paper);

        // 关联题目
        for (int i = 0; i < selected.size(); i++) {
            ExamQuestion eq = new ExamQuestion();
            eq.setExamId(paper.getId());
            eq.setQuestionId(selected.get(i).getId());
            eq.setSortOrder(i + 1);
            examQuestionMapper.insert(eq);
        }

        return paper;
    }

    @Override
    public ExamRecord startExam(Integer examId, Long userId) {
        ExamPaper paper = getById(examId, userId);
        // 检查是否已有活跃的考试记录（进行中或已暂停）
        LambdaQueryWrapper<ExamRecord> activeWrapper = new LambdaQueryWrapper<ExamRecord>()
                .eq(ExamRecord::getExamId, examId)
                .eq(!SecurityUtil.isAdmin(), ExamRecord::getUserId, userId)
                .in(ExamRecord::getStatus, "in_progress", "paused");
        ExamRecord existing = examRecordMapper.selectOne(activeWrapper);
        if (existing != null) {
            String hint = "paused".equals(existing.getStatus()) ? "该考试已暂停，请继续之前的考试" : "该考试已有进行中的记录";
            throw new BusinessException(hint);
        }
        ExamRecord record = new ExamRecord();
        record.setExamId(examId);
        record.setSubjectId(paper.getSubjectId());
        record.setTotalQuestions(paper.getQuestionCount());
        record.setUserId(userId);
        record.setStartedAt(LocalDateTime.now());
        record.setStatus("in_progress");
        examRecordMapper.insert(record);
        return record;
    }

    @Override
    @Transactional
    public ExamResultDTO submitExam(ExamSubmitDTO dto, Long userId) {
        ExamRecord record = examRecordMapper.selectById(dto.getRecordId());
        if (record == null) {
            throw new BusinessException("考试记录不存在");
        }
        if (!SecurityUtil.isAdmin() && !record.getUserId().equals(userId)) {
            throw new BusinessException("无权操作此考试记录");
        }
        if (record.getFinishedAt() != null) {
            throw new BusinessException("考试已交卷");
        }

        // 获取试卷题目
        List<ExamQuestion> examQuestions = examQuestionMapper.selectByExamId(record.getExamId());
        List<Integer> questionIds = examQuestions.stream()
                .map(ExamQuestion::getQuestionId)
                .collect(Collectors.toList());
        List<Question> questions = questionMapper.selectBatchIds(questionIds);
        Map<Integer, Question> questionMap = questions.stream()
                .collect(Collectors.toMap(Question::getId, q -> q));

        // 交卷答案映射
        Map<Integer, String> answerMap = new HashMap<>();
        if (dto.getAnswers() != null) {
            for (ExamSubmitDTO.AnswerItem item : dto.getAnswers()) {
                answerMap.put(item.getQuestionId(), item.getSelectedAnswer());
            }
        }

        int correctCount = 0;
        int wrongCount = 0;
        String sessionId = "exam-" + record.getId() + "-" + System.currentTimeMillis();

        // 逐题评分
        for (ExamQuestion eq : examQuestions) {
            Question question = questionMap.get(eq.getQuestionId());
            if (question == null) continue;

            String selected = answerMap.getOrDefault(eq.getQuestionId(), "");
            boolean isCorrect = AnswerChecker.checkAnswer(question, selected);

            if (isCorrect) {
                correctCount++;
            } else {
                wrongCount++;
                // 错题同步到错题本（绑定当前用户）
                wrongBookService.upsertWrongQuestion(question, userId);
            }

            // 保存答题记录（绑定当前用户）
            AnswerRecord ar = new AnswerRecord();
            ar.setQuestionId(question.getId());
            ar.setExamId(record.getExamId());
            ar.setSessionId(sessionId);
            ar.setSelectedAnswer(selected);
            ar.setIsCorrect(isCorrect ? 1 : 0);
            ar.setUserId(userId);
            ar.setAnsweredAt(LocalDateTime.now());
            answerRecordMapper.insert(ar);
        }

        // 计算得分（百分制）
        BigDecimal score = BigDecimal.ZERO;
        if (record.getTotalQuestions() > 0) {
            score = BigDecimal.valueOf(correctCount)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(record.getTotalQuestions()), 1, RoundingMode.HALF_UP);
        }

        // 保存 sessionId 到考试记录，用于后续精确查询答题记录
        record.setSessionId(sessionId);

        // 更新考试记录
        LocalDateTime now = LocalDateTime.now();
        record.setScore(score);
        record.setCorrectCount(correctCount);
        record.setWrongCount(wrongCount);
        record.setFinishedAt(now);
        record.setStatus("finished");
        if (record.getStartedAt() != null) {
            int currentSessionElapsed = (int) Duration.between(record.getStartedAt(), now).getSeconds();
            int previousElapsed = record.getDurationUsed() != null ? record.getDurationUsed() : 0;
            record.setDurationUsed(previousElapsed + Math.max(currentSessionElapsed, 0));
        }
        examRecordMapper.updateById(record);

        // 构建返回结果
        return buildResultDTO(record, examQuestions, questionMap, answerMap);
    }

    @Override
    @Transactional
    public void pauseExam(Integer recordId, PauseExamDTO dto, Long userId) {
        ExamRecord record = examRecordMapper.selectById(recordId);
        if (record == null) {
            throw new BusinessException("考试记录不存在");
        }
        if (!SecurityUtil.isAdmin() && !record.getUserId().equals(userId)) {
            throw new BusinessException("无权操作此考试记录");
        }
        if (!"in_progress".equals(record.getStatus())) {
            throw new BusinessException("考试未在进行中，无法暂停");
        }
        // 保存已消耗时间
        if (record.getStartedAt() != null) {
            int elapsed = (int) Duration.between(record.getStartedAt(), LocalDateTime.now()).getSeconds();
            record.setDurationUsed(Math.max(elapsed, 0));
        }
        // 保存答题进度：更新 currentIndex 和 durationRemaining
        if (dto.getCurrentIndex() != null) {
            record.setCurrentIndex(dto.getCurrentIndex());
        }
        record.setDurationRemaining(dto.getRemainingSeconds());
        record.setStatus("paused");
        examRecordMapper.updateById(record);

        // 保存已答题目到 answer_records
        if (dto.getAnswers() != null && !dto.getAnswers().isEmpty()) {
            String sessionId = "exam-" + record.getId() + "-" + System.currentTimeMillis();
            for (Map.Entry<Integer, String> entry : dto.getAnswers().entrySet()) {
                AnswerRecord ar = new AnswerRecord();
                ar.setQuestionId(entry.getKey());
                ar.setExamId(record.getExamId());
                ar.setSessionId(sessionId);
                ar.setSelectedAnswer(entry.getValue());
                ar.setIsCorrect(0); // 暂停时不做判分
                ar.setUserId(userId);
                ar.setAnsweredAt(LocalDateTime.now());
                answerRecordMapper.insert(ar);
            }
        }
    }

    @Override
    public ResumeExamResponse resumeExam(Integer recordId, Long userId) {
        ExamRecord record = examRecordMapper.selectById(recordId);
        if (record == null) {
            throw new BusinessException("考试记录不存在");
        }
        if (!SecurityUtil.isAdmin() && !record.getUserId().equals(userId)) {
            throw new BusinessException("无权操作此考试记录");
        }
        if (!"paused".equals(record.getStatus())) {
            throw new BusinessException("考试未处于暂停状态");
        }
        if (record.getFinishedAt() != null) {
            throw new BusinessException("考试已交卷，无法恢复");
        }
        record.setStatus("in_progress");
        record.setStartedAt(LocalDateTime.now());
        examRecordMapper.updateById(record);

        // 查询已保存的答题进度
        Map<Integer, String> savedAnswers = new HashMap<>();
        List<AnswerRecord> answers = answerRecordMapper.selectList(
                new LambdaQueryWrapper<AnswerRecord>()
                        .eq(AnswerRecord::getExamId, record.getExamId())
                        .eq(!SecurityUtil.isAdmin(), AnswerRecord::getUserId, userId)
                        .orderByDesc(AnswerRecord::getAnsweredAt));
        for (AnswerRecord ar : answers) {
            // 只保留每道题最新的一条答案
            savedAnswers.putIfAbsent(ar.getQuestionId(), ar.getSelectedAnswer());
        }

        return new ResumeExamResponse(record, savedAnswers);
    }

    @Override
    public com.baomidou.mybatisplus.extension.plugins.pagination.Page<ExamRecord> getRecords(
            Integer page, Integer pageSize, Long userId) {
        if (page == null || page < 1) page = 1;
        if (pageSize == null || pageSize < 1) pageSize = 20;
        return examRecordMapper.selectPage(
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, pageSize),
                new LambdaQueryWrapper<ExamRecord>()
                        .eq(!SecurityUtil.isAdmin(), ExamRecord::getUserId, userId)
                        .orderByDesc(ExamRecord::getCreatedAt));
    }

    @Override
    public ExamResultDTO getRecordDetail(Integer recordId, Long userId) {
        ExamRecord record = examRecordMapper.selectById(recordId);
        if (record == null) {
            throw new BusinessException("考试记录不存在");
        }
        if (!SecurityUtil.isAdmin() && !record.getUserId().equals(userId)) {
            throw new BusinessException("无权查看此考试记录");
        }
        List<ExamQuestion> examQuestions = examQuestionMapper.selectByExamId(record.getExamId());
        List<Integer> qIds = examQuestions.stream().map(ExamQuestion::getQuestionId).collect(Collectors.toList());
        List<Question> questions = questionMapper.selectBatchIds(qIds);
        Map<Integer, Question> questionMap = questions.stream()
                .collect(Collectors.toMap(Question::getId, q -> q));

        // 按 sessionId 精确查询本次考试的答题记录，避免多次考试混在一起
        LambdaQueryWrapper<AnswerRecord> answerWrapper = new LambdaQueryWrapper<>();
        answerWrapper.eq(!SecurityUtil.isAdmin(), AnswerRecord::getUserId, userId);
        if (record.getSessionId() != null) {
            answerWrapper.eq(AnswerRecord::getSessionId, record.getSessionId());
        } else {
            // 兼容旧记录（无 sessionId），按 examId + 时间范围回退
            answerWrapper.eq(AnswerRecord::getExamId, record.getExamId())
                         .between(AnswerRecord::getAnsweredAt, record.getStartedAt(), record.getFinishedAt());
        }
        List<AnswerRecord> answers = answerRecordMapper.selectList(answerWrapper);

        Map<Integer, String> answerMap = new HashMap<>();
        for (AnswerRecord ar : answers) {
            answerMap.put(ar.getQuestionId(), ar.getSelectedAnswer());
        }

        return buildResultDTO(record, examQuestions, questionMap, answerMap);
    }

    @Override
    public List<Question> getExamQuestions(Integer examId, Long userId) {
        // 验证试卷属于当前用户
        getById(examId, userId);
        List<ExamQuestion> examQuestions = examQuestionMapper.selectByExamId(examId);
        List<Integer> qIds = examQuestions.stream().map(ExamQuestion::getQuestionId).collect(Collectors.toList());
        if (qIds.isEmpty()) return Collections.emptyList();
        List<Question> questions = questionMapper.selectBatchIds(qIds);
        Map<Integer, Question> qMap = questions.stream().collect(Collectors.toMap(Question::getId, q -> q));
        return examQuestions.stream().map(eq -> qMap.get(eq.getQuestionId())).filter(Objects::nonNull).collect(Collectors.toList());
    }

    @Override
    public List<ExamRecord> getActiveRecords(Long userId) {
        return examRecordMapper.selectList(
                new LambdaQueryWrapper<ExamRecord>()
                        .eq(!SecurityUtil.isAdmin(), ExamRecord::getUserId, userId)
                        .in(ExamRecord::getStatus, "in_progress", "paused")
                        .orderByDesc(ExamRecord::getCreatedAt));
    }

    @Override
    @Transactional
    public void deleteExam(Integer id, Long userId) {
        getById(id, userId);
        // 先删答题记录，再删除考试记录，最后删试卷关联和试卷
        answerRecordMapper.delete(new LambdaQueryWrapper<AnswerRecord>().eq(AnswerRecord::getExamId, id));
        examRecordMapper.delete(new LambdaQueryWrapper<ExamRecord>().eq(ExamRecord::getExamId, id));
        examQuestionMapper.delete(new LambdaQueryWrapper<ExamQuestion>().eq(ExamQuestion::getExamId, id));
        examPaperMapper.deleteById(id);
    }

    // === private helper methods ===

    private ExamResultDTO buildResultDTO(ExamRecord record, List<ExamQuestion> examQuestions,
                                          Map<Integer, Question> questionMap, Map<Integer, String> answerMap) {
        ExamPaper paper = examPaperMapper.selectById(record.getExamId());

        ExamResultDTO dto = new ExamResultDTO();
        dto.setId(record.getId());
        dto.setExamId(record.getExamId());
        dto.setExamTitle(paper != null ? paper.getTitle() : "");
        Subject subject = subjectMapper.selectById(record.getSubjectId());
        dto.setSubjectName(subject != null ? subject.getName() : "");
        dto.setScore(record.getScore());
        dto.setTotalQuestions(record.getTotalQuestions());
        dto.setCorrectCount(record.getCorrectCount());
        dto.setWrongCount(record.getWrongCount());
        dto.setDurationUsed(record.getDurationUsed());
        dto.setStartedAt(record.getStartedAt());
        dto.setFinishedAt(record.getFinishedAt());

        List<QuestionResultItem> items = new ArrayList<>();
        for (int i = 0; i < examQuestions.size(); i++) {
            ExamQuestion eq = examQuestions.get(i);
            Question question = questionMap.get(eq.getQuestionId());
            if (question == null) continue;

            String selected = answerMap.getOrDefault(eq.getQuestionId(), "");
            boolean isCorrect = AnswerChecker.checkAnswer(question, selected);

            QuestionResultItem item = new QuestionResultItem();
            item.setQuestionId(question.getId());
            item.setQuestionIndex(i + 1);
            item.setType(question.getType());
            item.setContent(question.getContent());
            item.setOptions(question.getOptions());
            item.setCorrectAnswer(question.getAnswer());
            item.setSelectedAnswer(selected);
            item.setIsCorrect(isCorrect);
            item.setAnalysis(question.getAnalysis());
            items.add(item);
        }
        dto.setQuestions(items);
        return dto;
    }
}
