-- 手动迁移脚本：为已有数据库添加 user_id 列
-- 如果自动迁移（schema.sql 中的 ALTER TABLE ADD COLUMN IF NOT EXISTS）失败了，可以手动执行此脚本

-- 考试记录表
ALTER TABLE exam_records ADD COLUMN IF NOT EXISTS user_id BIGINT NOT NULL DEFAULT 0;

-- 答题记录表
ALTER TABLE answer_records ADD COLUMN IF NOT EXISTS user_id BIGINT NOT NULL DEFAULT 0;

-- 错题本表
ALTER TABLE wrong_questions ADD COLUMN IF NOT EXISTS user_id BIGINT NOT NULL DEFAULT 0;

-- 索引（如果不存在会自动跳过失败）
CREATE INDEX IF NOT EXISTS idx_exam_records_user ON exam_records(user_id);
CREATE INDEX IF NOT EXISTS idx_answer_records_user ON answer_records(user_id);
CREATE INDEX IF NOT EXISTS idx_wrong_questions_user ON wrong_questions(user_id);

-- 验证列是否存在
SELECT COLUMN_NAME, DATA_TYPE
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'exam_review'
  AND TABLE_NAME IN ('exam_records', 'answer_records', 'wrong_questions')
  AND COLUMN_NAME = 'user_id';
