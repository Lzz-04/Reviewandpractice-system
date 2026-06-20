-- 种子数据：插入示例科目和题目

-- 默认管理员用户（密码 123456 的 BCrypt 加密值由应用启动时自动创建，此处仅作占位）
-- INSERT IGNORE INTO users (username, password, nickname) VALUES ('admin', '<bcrypt-hash>', '管理员');

-- 科目
INSERT IGNORE INTO subjects (id, name, description, icon, sort_order, user_id) VALUES
(1, '高等数学', '高等数学（上册）期末复习', '📐', 1, 1),
(2, '大学英语', '大学英语四级核心词汇与语法', '📖', 2, 1),
(3, '数据结构', '数据结构与算法基础', '💻', 3, 1);


-- 题目示例 — 高等数学
INSERT IGNORE INTO questions (id, subject_id, type, content, options, answer, analysis, difficulty) VALUES
(1, 1, 'single', '函数 f(x)=x² 在 x=1 处的导数值为？', '[
  {"label":"A","text":"0"},
  {"label":"B","text":"1"},
  {"label":"C","text":"2"},
  {"label":"D","text":"3"}
]', 'C', 'f''(x)=2x，代入 x=1 得 f''(1)=2', 1),
(2, 1, 'single', '极限 lim(x→0) sin(x)/x 的值为？', '[
  {"label":"A","text":"0"},
  {"label":"B","text":"1"},
  {"label":"C","text":"∞"},
  {"label":"D","text":"不存在"}
]', 'B', '这是一个重要极限，lim(x→0) sin(x)/x = 1', 2),
(3, 1, 'judge', '可导函数必定连续。', '[]', 'T', '可导必连续，反之不成立', 1),
(4, 1, 'single', '函数 f(x)=x³-3x 的驻点个数为？', '[
  {"label":"A","text":"0"},
  {"label":"B","text":"1"},
  {"label":"C","text":"2"},
  {"label":"D","text":"3"}
]', 'C', 'f''(x)=3x²-3=0，解得 x=±1，共2个驻点', 3),
(5, 1, 'multiple', '下列关于导数的说法中，正确的有？', '[
  {"label":"A","text":"常数的导数为0"},
  {"label":"B","text":"(u+v)'' = u'' + v''"},
  {"label":"C","text":"(uv)'' = u''v''"},
  {"label":"D","text":"(xⁿ)'' = nxⁿ⁻¹"}
]', 'A,B,D', 'C错误，正确的乘积法则为 (uv)'' = u''v + uv''', 2);

-- 题目示例 — 大学英语
INSERT IGNORE INTO questions (id, subject_id, type, content, options, answer, analysis, difficulty) VALUES
(6, 2, 'single', '"abandon" 的中文含义是？', '[
  {"label":"A","text":"接受"},
  {"label":"B","text":"抛弃"},
  {"label":"C","text":"遵守"},
  {"label":"D","text":"吸收"}
]', 'B', 'abandon 意为"抛弃、放弃"', 1),
(7, 2, 'single', 'If I ___ you, I would study harder.', '[
  {"label":"A","text":"am"},
  {"label":"B","text":"was"},
  {"label":"C","text":"were"},
  {"label":"D","text":"be"}
]', 'C', '虚拟语气，if I were you 是固定表达', 2);

-- 题目示例 — 数据结构
INSERT IGNORE INTO questions (id, subject_id, type, content, options, answer, analysis, difficulty) VALUES
(8, 3, 'single', '在一个长度为n的顺序表中删除第i个元素，需要向前移动多少个元素？', '[
  {"label":"A","text":"i"},
  {"label":"B","text":"n-i"},
  {"label":"C","text":"n-i+1"},
  {"label":"D","text":"i-1"}
]', 'B', '删除第i个元素后，其后的 n-i 个元素都需要向前移动一位', 2),
(9, 3, 'judge', '二叉搜索树的中序遍历结果是一个递增序列。', '[]', 'T', '二叉搜索树的性质：左子树 < 根 < 右子树', 1),
(10, 3, 'multiple', '以下哪些属于树的基本遍历方式？', '[
  {"label":"A","text":"先序遍历"},
  {"label":"B","text":"中序遍历"},
  {"label":"C","text":"层序遍历"},
  {"label":"D","text":"对角线遍历"}
]', 'A,B,C', '树的三种基本遍历为：先序、中序、后序，层序遍历也很常用', 1);

-- === 补充题目至20道 ===

-- 高等数学 — 补充5题
INSERT IGNORE INTO questions (id, subject_id, type, content, options, answer, analysis, difficulty) VALUES
(11, 1, 'judge', '函数 f(x)=|x| 在 x=0 处连续。', '[]', 'T', '|x| 在 x=0 处左右极限均为0且等于函数值，故连续', 1),
(12, 1, 'multiple', '函数 f(x)=x³-3x 在区间[-2,2]上的极值情况为？', '[
  {"label":"A","text":"x=-1处取得极大值"},
  {"label":"B","text":"x=1处取得极小值"},
  {"label":"C","text":"x=0处取得极值"},
  {"label":"D","text":"端点处取得最值"}
]', 'A,B', 'f\'(x)=3x²-3=3(x-1)(x+1)，x=-1处f\'\'(-1)=-6<0为极大值，x=1处f\'\'(1)=6>0为极小值', 3),
(13, 1, 'single', '∫ x² dx 的结果为？', '[
  {"label":"A","text":"x³ + C"},
  {"label":"B","text":"x³/3 + C"},
  {"label":"C","text":"2x + C"},
  {"label":"D","text":"x³/2 + C"}
]', 'B', '幂函数的积分公式：∫ xⁿ dx = xⁿ⁺¹/(n+1) + C，n=2时得 x³/3 + C', 1),
(14, 1, 'single', '定积分 ∫₀¹ x dx 的值为？', '[
  {"label":"A","text":"0"},
  {"label":"B","text":"1"},
  {"label":"C","text":"0.5"},
  {"label":"D","text":"2"}
]', 'C', '∫₀¹ x dx = [x²/2]₀¹ = 1/2 - 0 = 0.5', 2),
(15, 1, 'judge', '不定积分 ∫ (1/x) dx = ln|x| + C。', '[]', 'T', '这是基本积分公式之一，1/x 的原函数为 ln|x|', 1);

-- 大学英语 — 补充3题
INSERT IGNORE INTO questions (id, subject_id, type, content, options, answer, analysis, difficulty) VALUES
(16, 2, 'single', '"elaborate" 的中文含义是？', '[
  {"label":"A","text":"简化的"},
  {"label":"B","text":"详细阐述的"},
  {"label":"C","text":"优雅的"},
  {"label":"D","text":"选举的"}
]', 'B', 'elaborate 意为"详细的、精心制作的"，作动词表示"详细阐述"', 2),
(17, 2, 'single', 'She has been studying English ___ three years.', '[
  {"label":"A","text":"since"},
  {"label":"B","text":"for"},
  {"label":"C","text":"during"},
  {"label":"D","text":"in"}
]', 'B', '现在完成时中，for + 时间段表示持续的时间长度', 1),
(18, 2, 'multiple', '以下哪些是英语中表示"尽管"的连词？', '[
  {"label":"A","text":"although"},
  {"label":"B","text":"despite"},
  {"label":"C","text":"therefore"},
  {"label":"D","text":"even though"}
]', 'A,D', 'although 和 even though 是连词引导让步状语从句；despite 是介词；therefore 表示"因此"', 2);

-- 数据结构 — 补充2题
INSERT IGNORE INTO questions (id, subject_id, type, content, options, answer, analysis, difficulty) VALUES
(19, 3, 'single', '链表与顺序表相比，其优势在于？', '[
  {"label":"A","text":"随机存取"},
  {"label":"B","text":"插入和删除操作高效"},
  {"label":"C","text":"存储密度高"},
  {"label":"D","text":"查找速度快"}
]', 'B', '链表的插入和删除仅需修改指针，时间复杂度为O(1)；顺序表在插入删除时需移动大量元素', 2),
(20, 3, 'single', '深度为k的二叉树最多有多少个节点？', '[
  {"label":"A","text":"k"},
  {"label":"B","text":"2k-1"},
  {"label":"C","text":"2ᵏ - 1"},
  {"label":"D","text":"2ᵏ⁻¹"}
]', 'C', '满二叉树节点数公式：2ᵏ - 1（k为深度）', 2);

-- ============================================================
-- 扩充至每科目20题：高等数学+10，大学英语+15，数据结构+15
-- ============================================================

-- === 高等数学 — 第一章 函数与极限 (+3) ===
INSERT IGNORE INTO questions (id, subject_id, type, content, options, answer, analysis, difficulty) VALUES
(21, 1, 'single', '极限 lim(x→∞) (1 + 1/x)ˣ 的值为？', '[
  {"label":"A","text":"0"},
  {"label":"B","text":"1"},
  {"label":"C","text":"e"},
  {"label":"D","text":"∞"}
]', 'C', '重要极限：lim(x→∞) (1 + 1/x)ˣ = e', 2),
(22, 1, 'judge', '无穷小量就是很小的数。', '[]', 'F', '无穷小量是以0为极限的变量，不是具体的很小的数', 1),
(23, 1, 'multiple', '以下哪些是函数在一点连续的条件？', '[
  {"label":"A","text":"该点有定义"},
  {"label":"B","text":"该点极限存在"},
  {"label":"C","text":"极限值等于函数值"},
  {"label":"D","text":"该点可导"}
]', 'A,B,C', '连续三要素：有定义、有极限、极限值等于函数值；可导是更强的条件', 2);

-- === 高等数学 — 第二章 导数与微分 (+4) ===
INSERT IGNORE INTO questions (id, subject_id, type, content, options, answer, analysis, difficulty) VALUES
(24, 1, 'single', '函数 f(x)=eˣ 的导数等于？', '[
  {"label":"A","text":"xeˣ⁻¹"},
  {"label":"B","text":"eˣ"},
  {"label":"C","text":"eˣ/x"},
  {"label":"D","text":"ln x"}
]', 'B', '指数函数 eˣ 的导数仍为 eˣ，这是它的重要性质', 1),
(25, 1, 'judge', '若函数在一点可导，则必在该点连续。', '[]', 'T', '可导必连续，连续未必可导', 1),
(26, 1, 'single', '函数 f(x)=ln x 的导数为？', '[
  {"label":"A","text":"eˣ"},
  {"label":"B","text":"1/x"},
  {"label":"C","text":"x"},
  {"label":"D","text":"ln x"}
]', 'B', '对数函数的导数：(ln x)\' = 1/x', 2),
(27, 1, 'multiple', '关于微分的说法正确的有？', '[
  {"label":"A","text":"dy = f\'(x)dx"},
  {"label":"B","text":"微分是函数改变量的线性主部"},
  {"label":"C","text":"微分与增量相等"},
  {"label":"D","text":"可微与可导等价"}
]', 'A,B,D', '对于一元函数，可微 ⇔ 可导；dy = f\'(x)dx 是微分的定义式；微分是增量的线性主部而非全部', 3);

-- === 高等数学 — 第三章 不定积分 (+3) ===
INSERT IGNORE INTO questions (id, subject_id, type, content, options, answer, analysis, difficulty) VALUES
(28, 1, 'judge', '∫ sin x dx = -cos x + C。', '[]', 'T', 'sin x 的不定积分为 -cos x + C，因为 (-cos x)\' = sin x', 1),
(29, 1, 'single', '∫ eˣ dx 的结果为？', '[
  {"label":"A","text":"eˣ + C"},
  {"label":"B","text":"xeˣ + C"},
  {"label":"C","text":"eˣ/x + C"},
  {"label":"D","text":"ln eˣ + C"}
]', 'A', 'eˣ 的不定积分是它本身，即 ∫ eˣ dx = eˣ + C', 1),
(30, 1, 'multiple', '以下积分公式正确的有？', '[
  {"label":"A","text":"∫ cos x dx = sin x + C"},
  {"label":"B","text":"∫ sec²x dx = tan x + C"},
  {"label":"C","text":"∫ 1/(1+x²) dx = arctan x + C"},
  {"label":"D","text":"∫ eˣ dx = eˣ + C"}
]', 'A,B,C,D', '四个选项均为基本积分公式，全部正确', 2);

-- === 大学英语 — 第四章 核心词汇 (+8) ===
INSERT IGNORE INTO questions (id, subject_id, type, content, options, answer, analysis, difficulty) VALUES
(31, 2, 'single', '"ambiguous" 的中文含义是？', '[
  {"label":"A","text":"明显的"},
  {"label":"B","text":"模糊不清的"},
  {"label":"C","text":"有野心的"},
  {"label":"D","text":"充足的"}
]', 'B', 'ambiguous 意为"模糊不清的、有歧义的"', 2),
(32, 2, 'single', '"phenomenon" 的复数形式是？', '[
  {"label":"A","text":"phenomenons"},
  {"label":"B","text":"phenomena"},
  {"label":"C","text":"phenomenas"},
  {"label":"D","text":"phenomenes"}
]', 'B', 'phenomenon 是不规则名词，复数形式为 phenomena', 2),
(33, 2, 'judge', '"evaluate" 意为"评估、评价"。', '[]', 'T', 'evaluate = assess = appraise，意为评估、评价', 1),
(34, 2, 'single', '与 "significant" 意思最接近的是？', '[
  {"label":"A","text":"trivial"},
  {"label":"B","text":"important"},
  {"label":"C","text":"similar"},
  {"label":"D","text":"simple"}
]', 'B', 'significant = important = 重要的；trivial 是其反义词', 1),
(35, 2, 'judge', '"contemporary" 的意思是"暂时的"。', '[]', 'F', 'contemporary 意为"当代的、同时代的"；temporary 才是"暂时的"', 2),
(36, 2, 'multiple', '以下哪些单词表示"减少"？', '[
  {"label":"A","text":"decline"},
  {"label":"B","text":"decrease"},
  {"label":"C","text":"diminish"},
  {"label":"D","text":"dedicate"}
]', 'A,B,C', 'decline、decrease、diminish 都有"减少"之意；dedicate 意为"奉献"', 2),
(37, 2, 'single', '"inevitable" 的中文含义是？', '[
  {"label":"A","text":"可避免的"},
  {"label":"B","text":"不可避免的"},
  {"label":"C","text":"无形的"},
  {"label":"D","text":"难以想象的"}
]', 'B', 'inevitable = unavoidable = 不可避免的', 1),
(38, 2, 'multiple', '以下哪些是大学英语四级核心词汇？', '[
  {"label":"A","text":"consequence"},
  {"label":"B","text":"sufficient"},
  {"label":"C","text":"acknowledge"},
  {"label":"D","text":"apple"}
]', 'A,B,C', 'consequence(后果)、sufficient(充足的)、acknowledge(承认)均为四级核心词汇；apple 为基础词汇', 1);

-- === 大学英语 — 第五章 语法专项 (+7) ===
INSERT IGNORE INTO questions (id, subject_id, type, content, options, answer, analysis, difficulty) VALUES
(39, 2, 'single', 'Not only ___ good at math, but he also excels in English.', '[
  {"label":"A","text":"he is"},
  {"label":"B","text":"is he"},
  {"label":"C","text":"he was"},
  {"label":"D","text":"does he"}
]', 'B', 'Not only 位于句首时，主句需部分倒装', 3),
(40, 2, 'judge', '英语中，介词后面可以接动词原形。', '[]', 'F', '介词后应接动名词(doing)，不能接动词原形', 1),
(41, 2, 'single', 'It is high time that we ___ action.', '[
  {"label":"A","text":"take"},
  {"label":"B","text":"took"},
  {"label":"C","text":"taken"},
  {"label":"D","text":"taking"}
]', 'B', 'It is high time that... 句型中，从句用过去式表示虚拟', 3),
(42, 2, 'multiple', '以下哪些句子使用了被动语态？', '[
  {"label":"A","text":"The book was written by him."},
  {"label":"B","text":"She is writing a letter."},
  {"label":"C","text":"The window was broken."},
  {"label":"D","text":"English is spoken worldwide."}
]', 'A,C,D', 'A和D是标准的被动语态(be+过去分词)；C是get型被动；B是主动语态的现在进行时', 2),
(43, 2, 'single', 'I wish I ___ a bird.', '[
  {"label":"A","text":"am"},
  {"label":"B","text":"was"},
  {"label":"C","text":"were"},
  {"label":"D","text":"be"}
]', 'C', 'wish 后的宾语从句用虚拟语气，be 动词一律用 were', 2),
(44, 2, 'judge', '定语从句中，关系代词 that 可以用于非限制性定语从句。', '[]', 'F', 'that 只能用于限制性定语从句，非限制性定语从句需用 which/who/whom', 2),
(45, 2, 'single', 'By the time he arrives, we ___ for two hours.', '[
  {"label":"A","text":"wait"},
  {"label":"B","text":"will wait"},
  {"label":"C","text":"will have been waiting"},
  {"label":"D","text":"waited"}
]', 'C', 'By the time + 现在时，主句用将来完成时(或将来完成进行时)表示到某时已持续的状态', 3);

-- === 数据结构 — 第六章 线性表 (+8) ===
INSERT IGNORE INTO questions (id, subject_id, type, content, options, answer, analysis, difficulty) VALUES
(46, 3, 'single', '栈的操作特性是？', '[
  {"label":"A","text":"先进先出"},
  {"label":"B","text":"先进后出"},
  {"label":"C","text":"随机进出"},
  {"label":"D","text":"按优先级"}
]', 'B', '栈(stack)是后进先出(LIFO)的线性结构', 1),
(47, 3, 'judge', '队列是一种先进先出的线性结构。', '[]', 'T', '队列(queue)的操作特性为先进先出(FIFO)', 1),
(48, 3, 'single', '在一个长度为n的顺序表中插入第i个位置，需要移动多少个元素？', '[
  {"label":"A","text":"i"},
  {"label":"B","text":"n-i"},
  {"label":"C","text":"n-i+1"},
  {"label":"D","text":"n-1"}
]', 'C', '插入时需将第i到第n个元素共 n-i+1 个元素后移', 2),
(49, 3, 'multiple', '以下哪些是线性结构？', '[
  {"label":"A","text":"顺序表"},
  {"label":"B","text":"链表"},
  {"label":"C","text":"栈"},
  {"label":"D","text":"图"}
]', 'A,B,C', '顺序表、链表、栈、队列都是线性结构；图是非线性结构', 1),
(50, 3, 'judge', '循环队列可以解决顺序队列的"假溢出"问题。', '[]', 'T', '循环队列利用取余运算使队尾指针循环利用数组空间，避免假溢出', 2),
(51, 3, 'single', '单链表中删除某个节点的后继节点，需要修改几个指针？', '[
  {"label":"A","text":"0"},
  {"label":"B","text":"1"},
  {"label":"C","text":"2"},
  {"label":"D","text":"3"}
]', 'B', '只需将被删节点的前驱指针指向被删节点的后继，即修改1个指针', 2),
(52, 3, 'multiple', '关于顺序栈的说法正确的有？', '[
  {"label":"A","text":"入栈操作时间复杂度为O(1)"},
  {"label":"B","text":"出栈操作时间复杂度为O(1)"},
  {"label":"C","text":"需要连续的内存空间"},
  {"label":"D","text":"不存在栈满的情况"}
]', 'A,B,C', '顺序栈入栈出栈均为O(1)；需要连续内存；存在栈满上溢的情况', 2),
(53, 3, 'single', '链栈的入栈操作相当于在链表何处插入？', '[
  {"label":"A","text":"表头"},
  {"label":"B","text":"表尾"},
  {"label":"C","text":"中间"},
  {"label":"D","text":"任意位置"}
]', 'A', '链栈在表头(栈顶)进行插入和删除，入栈即头插法', 2);

-- === 数据结构 — 第七章 树与二叉树 (+7) ===
INSERT IGNORE INTO questions (id, subject_id, type, content, options, answer, analysis, difficulty) VALUES
(54, 3, 'single', '一棵完全二叉树有100个节点，其叶子节点数为？', '[
  {"label":"A","text":"49"},
  {"label":"B","text":"50"},
  {"label":"C","text":"51"},
  {"label":"D","text":"52"}
]', 'B', 'n₀ = n₂ + 1，完全二叉树中 n₁ = 0或1。100个节点时为50个叶子', 3),
(55, 3, 'judge', '哈夫曼树（最优二叉树）的带权路径长度是最小的。', '[]', 'T', '哈夫曼树构造算法保证了WPL最小，即为最优二叉树', 1),
(56, 3, 'single', '二叉树的前序遍历序列为 ABDCEF，中序遍历序列为 DBAECF，后序遍历序列为？', '[
  {"label":"A","text":"DBEFCA"},
  {"label":"B","text":"DBFECA"},
  {"label":"C","text":"DBEFAC"},
  {"label":"D","text":"DEBFCA"}
]', 'A', '由前序和中序重建二叉树：A为根，BD为左子树(前序BD,中序DB→B为左根D为左子)，CEF为右子树→后序DBEFCA', 3),
(57, 3, 'multiple', '关于二叉搜索树的说法正确的有？', '[
  {"label":"A","text":"左子树所有节点值 < 根节点值"},
  {"label":"B","text":"右子树所有节点值 > 根节点值"},
  {"label":"C","text":"中序遍历得到递增序列"},
  {"label":"D","text":"可以是空树"}
]', 'A,B,C,D', '二叉搜索树的定义：左<根<右，中序递增，空树也是二叉搜索树', 2),
(58, 3, 'judge', '树可以转换为二叉树，二叉树也可以还原为树。', '[]', 'T', '树与二叉树可通过"左孩子右兄弟"法则相互转换', 2),
(59, 3, 'single', '在一棵度为3的树中，度为3的节点有2个，度为2的节点有1个，度为1的节点有2个，叶子节点有？', '[
  {"label":"A","text":"4"},
  {"label":"B","text":"5"},
  {"label":"C","text":"6"},
  {"label":"D","text":"7"}
]', 'C', '总边数 = 3×2 + 2×1 + 1×2 = 10，节点总数 = 边数 + 1 = 11，叶子数 = 11 - 2 - 1 - 2 = 6', 3),
(60, 3, 'multiple', '在二叉树中，关于度为0、1、2的节点数(n₀,n₁,n₂)关系正确的有？', '[
  {"label":"A","text":"n₀ = n₂ + 1"},
  {"label":"B","text":"n = n₀ + n₁ + n₂"},
  {"label":"C","text":"n = 2n₂ + n₁ + 1"},
  {"label":"D","text":"n₀ = n₁ + 1"}
]', 'A,B,C', '二叉树性质：n₀ = n₂ + 1；n = n₀ + n₁ + n₂；代入得 n = 2n₂ + n₁ + 1；D选项错误', 2);

-- 将已有种子数据的 user_id 设为 1（管理员账号），确保迁移后旧数据可见
UPDATE subjects SET user_id = 1 WHERE user_id = 0;
UPDATE questions SET user_id = 1 WHERE user_id = 0;
UPDATE exam_papers SET user_id = 1 WHERE user_id = 0;
