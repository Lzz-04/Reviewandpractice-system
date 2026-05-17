-- 数据库由 JDBC URL 中的 createDatabaseIfNotExist=true 自动创建
-- 科目表
CREATE TABLE IF NOT EXISTS subjects (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL,
    description VARCHAR(200) DEFAULT '',
    icon VARCHAR(100) DEFAULT '',
    sort_order INT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 章节表
CREATE TABLE IF NOT EXISTS chapters (
    id INT PRIMARY KEY AUTO_INCREMENT,
    subject_id INT NOT NULL,
    name VARCHAR(100) NOT NULL,
    sort_order INT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (subject_id) REFERENCES subjects(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 题目表
CREATE TABLE IF NOT EXISTS questions (
    id INT PRIMARY KEY AUTO_INCREMENT,
    chapter_id INT NOT NULL,
    subject_id INT NOT NULL,
    type VARCHAR(10) NOT NULL COMMENT 'single/multiple/judge',
    content TEXT NOT NULL,
    options JSON NOT NULL COMMENT '选项JSON',
    answer VARCHAR(100) NOT NULL COMMENT '正确答案',
    analysis TEXT DEFAULT NULL,
    difficulty TINYINT DEFAULT 1 COMMENT '难度1-5',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (chapter_id) REFERENCES chapters(id),
    FOREIGN KEY (subject_id) REFERENCES subjects(id),
    CONSTRAINT chk_type CHECK (type IN ('single','multiple','judge')),
    CONSTRAINT chk_difficulty CHECK (difficulty BETWEEN 1 AND 5)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 试卷表
CREATE TABLE IF NOT EXISTS exam_papers (
    id INT PRIMARY KEY AUTO_INCREMENT,
    subject_id INT NOT NULL,
    title VARCHAR(100) NOT NULL,
    duration INT NOT NULL COMMENT '考试时长(分钟)',
    question_count INT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (subject_id) REFERENCES subjects(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 试卷-题目关联表
CREATE TABLE IF NOT EXISTS exam_questions (
    id INT PRIMARY KEY AUTO_INCREMENT,
    exam_id INT NOT NULL,
    question_id INT NOT NULL,
    sort_order INT DEFAULT 0,
    FOREIGN KEY (exam_id) REFERENCES exam_papers(id),
    FOREIGN KEY (question_id) REFERENCES questions(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 答题记录表
CREATE TABLE IF NOT EXISTS answer_records (
    id INT PRIMARY KEY AUTO_INCREMENT,
    question_id INT NOT NULL,
    exam_id INT DEFAULT NULL,
    session_id VARCHAR(36) NOT NULL,
    selected_answer VARCHAR(100) DEFAULT '',
    is_correct TINYINT DEFAULT 0,
    answered_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    time_spent INT DEFAULT 0 COMMENT '答题耗时(秒)',
    FOREIGN KEY (question_id) REFERENCES questions(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 考试记录表
CREATE TABLE IF NOT EXISTS exam_records (
    id INT PRIMARY KEY AUTO_INCREMENT,
    exam_id INT NOT NULL,
    subject_id INT NOT NULL,
    score DECIMAL(5,1) DEFAULT 0,
    total_questions INT DEFAULT 0,
    correct_count INT DEFAULT 0,
    wrong_count INT DEFAULT 0,
    status VARCHAR(16) DEFAULT 'in_progress' COMMENT 'in_progress/paused/finished',
    duration_used INT DEFAULT 0 COMMENT '实际用时(秒)',
    duration_remaining INT DEFAULT 0 COMMENT '暂停时剩余时间(秒)',
    started_at DATETIME DEFAULT NULL,
    finished_at DATETIME DEFAULT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (exam_id) REFERENCES exam_papers(id),
    FOREIGN KEY (subject_id) REFERENCES subjects(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 错题本表
CREATE TABLE IF NOT EXISTS wrong_questions (
    id INT PRIMARY KEY AUTO_INCREMENT,
    question_id INT NOT NULL UNIQUE,
    subject_id INT NOT NULL,
    chapter_id INT NOT NULL,
    wrong_count INT DEFAULT 1,
    last_wrong_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    reviewed_count INT DEFAULT 0,
    mastered TINYINT DEFAULT 0 COMMENT '0=未掌握,1=已掌握',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (question_id) REFERENCES questions(id),
    FOREIGN KEY (subject_id) REFERENCES subjects(id),
    FOREIGN KEY (chapter_id) REFERENCES chapters(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 外键补充
ALTER TABLE exam_records ADD FOREIGN KEY IF NOT EXISTS (exam_id) REFERENCES exam_papers(id);

-- 增量迁移：为已有数据库补充缺失字段（continue-on-error: true 保障兼容性）
ALTER TABLE exam_records ADD COLUMN IF NOT EXISTS status VARCHAR(16) DEFAULT 'in_progress' COMMENT 'in_progress/paused/finished';
ALTER TABLE exam_records ADD COLUMN IF NOT EXISTS duration_remaining INT DEFAULT 0 COMMENT '暂停时剩余时间(秒)';
ALTER TABLE exam_records ADD COLUMN IF NOT EXISTS session_id VARCHAR(128) DEFAULT NULL COMMENT '答题批次标识，用于精确关联本次答题记录';
ALTER TABLE subjects ADD UNIQUE INDEX idx_subjects_name (name);

-- 索引
CREATE INDEX idx_questions_chapter ON questions(chapter_id);
CREATE INDEX idx_questions_subject ON questions(subject_id);
CREATE INDEX idx_questions_type ON questions(type);
CREATE INDEX idx_questions_difficulty ON questions(difficulty);
CREATE INDEX idx_answer_records_session ON answer_records(session_id);
CREATE INDEX idx_answer_records_question ON answer_records(question_id);
CREATE INDEX idx_answer_records_exam ON answer_records(exam_id);
CREATE INDEX idx_answer_records_answered_at ON answer_records(answered_at);
CREATE INDEX idx_exam_records_subject ON exam_records(subject_id);
CREATE INDEX idx_exam_records_exam ON exam_records(exam_id);
CREATE INDEX idx_wrong_questions_subject ON wrong_questions(subject_id);
CREATE INDEX idx_chapters_subject ON chapters(subject_id);
