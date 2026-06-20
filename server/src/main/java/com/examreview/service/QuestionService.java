package com.examreview.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.examreview.entity.Question;

import java.util.List;

/**
 * 题库服务接口
 * 提供题目管理相关的业务操作，支持分页查询、CRUD、批量操作和随机抽题
 */
public interface QuestionService {

    /**
     * 分页查询题目列表
     *
     * @param page       页码
     * @param pageSize   每页大小
     * @param subjectId  科目ID（可选）
     * @param type       题型（single/multiple/judge，可选）
     * @param keyword    关键词（模糊匹配题目内容，可选）
     * @param difficulty 难度（可选）
     * @param sortBy     排序字段（difficulty或默认按创建时间降序）
     * @param userId     用户ID（用于数据隔离）
     * @return 分页题目列表
     */
    Page<Question> getList(Integer page, Integer pageSize, Integer subjectId, String type,
                           String keyword, Integer difficulty, String sortBy, Long userId);

    /**
     * 根据ID获取题目详情
     *
     * @param id     题目ID
     * @param userId 用户ID（用于权限校验）
     * @return 题目详情
     */
    Question getById(Integer id, Long userId);

    /**
     * 创建题目
     * 校验题型必须为 single/multiple/judge，选项以JSON格式存储
     *
     * @param question 题目信息
     * @param userId   用户ID
     * @return 创建后的题目
     */
    Question create(Question question, Long userId);

    /**
     * 更新题目
     * 支持部分字段更新
     *
     * @param id       题目ID
     * @param question 更新后的题目信息
     * @param userId   用户ID（用于权限校验）
     * @return 更新后的题目
     */
    Question update(Integer id, Question question, Long userId);

    /**
     * 删除题目
     * 级联删除关联的错题记录、答题记录和试卷题目关联
     *
     * @param id     题目ID
     * @param userId 用户ID（用于权限校验）
     */
    void delete(Integer id, Long userId);

    /**
     * 批量删除题目
     * 使用@Transactional保证原子性，任一失败全部回滚
     *
     * @param ids    题目ID列表
     * @param userId 用户ID（用于权限校验）
     */
    void batchDelete(List<Integer> ids, Long userId);

    /**
     * 随机抽题
     * 从指定科目随机抽取指定数量的题目
     *
     * @param subjectId 科目ID
     * @param count     抽取数量
     * @param userId    用户ID（用于权限校验）
     * @return 随机题目列表
     */
    List<Question> getRandomQuestions(Integer subjectId, Integer count, Long userId);

    /**
     * 根据ID列表批量获取题目
     *
     * @param ids    题目ID列表
     * @param userId 用户ID（用于权限校验）
     * @return 题目列表
     */
    List<Question> getQuestionsByIds(List<Integer> ids, Long userId);
}
