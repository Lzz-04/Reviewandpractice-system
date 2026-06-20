package com.examreview.service;

import com.examreview.entity.Subject;

import java.util.List;

/**
 * 科目服务接口
 * 提供科目管理相关的业务操作，支持用户数据隔离
 */
public interface SubjectService {

    /**
     * 获取当前用户的所有科目列表
     *
     * @param userId 用户ID
     * @return 科目列表，按 sort_order 升序排列
     */
    List<Subject> getAll(Long userId);

    /**
     * 根据ID获取科目详情
     *
     * @param id     科目ID
     * @param userId 用户ID（用于权限校验）
     * @return 科目详情
     */
    Subject getById(Integer id, Long userId);

    /**
     * 创建科目
     *
     * @param subject 科目信息
     * @param userId  用户ID
     * @return 创建后的科目
     */
    Subject create(Subject subject, Long userId);

    /**
     * 更新科目
     *
     * @param id      科目ID
     * @param subject 更新后的科目信息
     * @param userId  用户ID（用于权限校验）
     * @return 更新后的科目
     */
    Subject update(Integer id, Subject subject, Long userId);

    /**
     * 删除科目
     *
     * @param id     科目ID
     * @param userId 用户ID（用于权限校验）
     */
    void delete(Integer id, Long userId);
}
