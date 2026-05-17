package com.examreview.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.examreview.dto.ExamGenerateDTO;
import com.examreview.dto.ExamResultDTO;
import com.examreview.dto.ExamSubmitDTO;
import com.examreview.dto.QuestionResultItem;
import com.examreview.entity.*;
import com.examreview.exception.BusinessException;
import com.examreview.mapper.*;
import com.examreview.service.ExamService;
import com.examreview.service.WrongBookService;
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
    public List<ExamPaper> getList(Integer subjectId) {
        LambdaQueryWrapper<ExamPaper> wrapper = new LambdaQueryWrapper<>();
        if (subjectId != null) {
            wrapper.eq(ExamPaper::getSubjectId, subjectId);
        }
        wrapper.orderByDesc(ExamPaper::getCreatedAt);
        return examPaperMapper.selectList(wrapper);
    }

    @Override
    public ExamPaper getById(Integer id) {
        ExamPaper paper = examPaperMapper.selectById(id);
        if (paper == null) {
            throw new BusinessException("试卷不存在");
        }
        return paper;
    }

    @Override
    public ExamPaper create(ExamPaper examPaper) {
        examPaperMapper.insert(examPaper);
        return examPaper;
    }

    @Override
    @Transactional
    public ExamPaper generate(ExamGenerateDTO dto) {
        // 校验科目存在
        if (subjectMapper.selectById(dto.getSubjectId()) == null) {
            throw new BusinessException("科目不存在");
        }
        // 查询题目池
        List<Question> pool = questionMapper.selectBySubject(dto.getSubjectId());
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
    public ExamRecord startExam(Integer examId) {
        ExamPaper paper = getById(examId);
        // 检查是否已有进行中的考试记录
        Long inProgressCount = examRecordMapper.selectCount(
                new LambdaQueryWrapper<ExamRecord>()
                        .eq(ExamRecord::getExamId, examId)
                        .eq(ExamRecord::getStatus, "in_progress"));
        if (inProgressCount > 0) {
            throw new BusinessException("该考试已有进行中的记录，请先完成或暂停后再开始");
        }
        ExamRecord record = new ExamRecord();
        record.setExamId(examId);
        record.setSubjectId(paper.getSubjectId());
        record.setTotalQuestions(paper.getQuestionCount());
        record.setStartedAt(LocalDateTime.now());
        record.setStatus("in_progress");
        examRecordMapper.insert(record);
        return record;
    }

    @Override
    @Transactional
    public ExamResultDTO submitExam(ExamSubmitDTO dto) {
        ExamRecord record = examRecordMapper.selectById(dto.getRecordId());
        if (record == null) {
            throw new BusinessException("考试记录不存在");
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
                // 错题同步到错题本
                wrongBookService.upsertWrongQuestion(question);
            }

            // 保存答题记录
            AnswerRecord ar = new AnswerRecord();
            ar.setQuestionId(question.getId());
            ar.setExamId(record.getExamId());
            ar.setSessionId(sessionId);
            ar.setSelectedAnswer(selected);
            ar.setIsCorrect(isCorrect ? 1 : 0);
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

        // 更新考试记录
        LocalDateTime now = LocalDateTime.now();
        record.setScore(score);
        record.setCorrectCount(correctCount);
        record.setWrongCount(wrongCount);
        record.setFinishedAt(now);
        record.setStatus("finished");
        if (record.getStartedAt() != null) {
            record.setDurationUsed((int) Duration.between(record.getStartedAt(), now).getSeconds());
        }
        examRecordMapper.updateById(record);

        // 构建返回结果
        return buildResultDTO(record, examQuestions, questionMap, answerMap);
    }

    @Override
    public void pauseExam(Integer recordId, Integer remainingSeconds) {
        ExamRecord record = examRecordMapper.selectById(recordId);
        if (record == null) {
            throw new BusinessException("考试记录不存在");
        }
        if (!"in_progress".equals(record.getStatus())) {
            throw new BusinessException("考试未在进行中，无法暂停");
        }
        record.setStatus("paused");
        record.setDurationRemaining(remainingSeconds);
        examRecordMapper.updateById(record);
    }

    @Override
    public ExamRecord resumeExam(Integer recordId) {
        ExamRecord record = examRecordMapper.selectById(recordId);
        if (record == null) {
            throw new BusinessException("考试记录不存在");
        }
        if (!"paused".equals(record.getStatus())) {
            throw new BusinessException("考试未处于暂停状态");
        }
        if (record.getFinishedAt() != null) {
            throw new BusinessException("考试已交卷，无法恢复");
        }
        record.setStatus("in_progress");
        // 更新开始时间以正确计算考试用时
        long elapsedSeconds = record.getDurationUsed() != null ? record.getDurationUsed() : 0;
        long remainingSeconds = record.getDurationRemaining() != null ? record.getDurationRemaining() : 0;
        record.setStartedAt(LocalDateTime.now().minusSeconds(elapsedSeconds));
        long totalDuration = elapsedSeconds + remainingSeconds;
        if (totalDuration > 0) {
            record.setDurationRemaining(null); // 清空暂停剩余时间
        }
        examRecordMapper.updateById(record);
        return record;
    }

    @Override
    public List<ExamRecord> getRecords() {
        return examRecordMapper.selectList(
                new LambdaQueryWrapper<ExamRecord>().orderByDesc(ExamRecord::getCreatedAt));
    }

    @Override
    public ExamResultDTO getRecordDetail(Integer recordId) {
        ExamRecord record = examRecordMapper.selectById(recordId);
        if (record == null) {
            throw new BusinessException("考试记录不存在");
        }
        List<ExamQuestion> examQuestions = examQuestionMapper.selectByExamId(record.getExamId());
        List<Integer> qIds = examQuestions.stream().map(ExamQuestion::getQuestionId).collect(Collectors.toList());
        List<Question> questions = questionMapper.selectBatchIds(qIds);
        Map<Integer, Question> questionMap = questions.stream()
                .collect(Collectors.toMap(Question::getId, q -> q));

        // 查询答题记录
        List<AnswerRecord> answers = answerRecordMapper.selectList(
                new LambdaQueryWrapper<AnswerRecord>()
                        .eq(AnswerRecord::getExamId, record.getExamId()));

        Map<Integer, String> answerMap = new HashMap<>();
        for (AnswerRecord ar : answers) {
            answerMap.put(ar.getQuestionId(), ar.getSelectedAnswer());
        }

        return buildResultDTO(record, examQuestions, questionMap, answerMap);
    }

    @Override
    public void deleteExam(Integer id) {
        getById(id);
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
