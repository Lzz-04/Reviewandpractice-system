-- Migration V2: 删除章节功能，题目直接归属科目
-- 如数据库外键名称与默认名称不同，请按实际 INFORMATION_SCHEMA 查询结果调整。

ALTER TABLE questions DROP FOREIGN KEY IF EXISTS questions_ibfk_1;
ALTER TABLE wrong_questions DROP FOREIGN KEY IF EXISTS wrong_questions_ibfk_3;

DROP INDEX IF EXISTS idx_questions_chapter ON questions;
DROP INDEX IF EXISTS idx_chapters_subject ON chapters;

ALTER TABLE questions DROP COLUMN IF EXISTS chapter_id;
ALTER TABLE wrong_questions DROP COLUMN IF EXISTS chapter_id;

DROP TABLE IF EXISTS chapters;
