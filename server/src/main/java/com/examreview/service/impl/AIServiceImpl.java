package com.examreview.service.impl;

import com.examreview.dto.AIGenerateRequest;
import com.examreview.dto.AIGeneratedQuestion;
import com.examreview.entity.Question;
import com.examreview.exception.BusinessException;
import com.examreview.mapper.QuestionMapper;
import com.examreview.service.AIService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;
import java.util.*;

@Slf4j
@Service
@ConditionalOnProperty(name = "deepseek.enabled", havingValue = "true")
public class AIServiceImpl implements AIService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final String model;
    private final QuestionMapper questionMapper;

    public AIServiceImpl(@Value("${deepseek.api-key}") String apiKey,
                         @Value("${deepseek.base-url}") String baseUrl,
                         @Value("${deepseek.model}") String model,
                         ObjectMapper objectMapper,
                         QuestionMapper questionMapper) {
        this.model = model;
        this.objectMapper = objectMapper;
        this.questionMapper = questionMapper;
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    @Override
    public List<AIGeneratedQuestion> generate(AIGenerateRequest request, Long userId) {
        String systemPrompt = buildSystemPrompt();
        String userPrompt = buildUserPrompt(request, userId);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", model);
        body.put("messages", List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", userPrompt)
        ));
        body.put("response_format", Map.of("type", "json_object"));
        body.put("temperature", 0.8);
        body.put("max_tokens", 4096);

        try {
            String response = webClient.post()
                    .uri("/v1/chat/completions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(60))
                    .block();

            return parseResponse(response);
        } catch (WebClientResponseException e) {
            log.error("DeepSeek API 返回错误: status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new BusinessException("AI 接口调用失败，请稍后重试");
        } catch (Exception e) {
            log.error("AI 出题异常", e);
            throw new BusinessException("AI 出题失败: " + e.getMessage());
        }
    }

    /**
     * 构建系统提示词
     */
    private String buildSystemPrompt() {
        return """
                你是一位专业的学科考试出题专家，擅长设计各种类型的高质量考试题目。
                你必须严格按照要求生成题目，并以 JSON 格式返回结果。
                每道题必须包含完整的题目内容、选项、正确答案和详细解析。
                选项必须具有干扰性，不能过于简单。
                解析必须详细、准确，能帮助学习者理解知识点。
                """;
    }

    /**
     * 构建用户提示词（参数化，包含已有题库样例）
     */
    private String buildUserPrompt(AIGenerateRequest req, Long userId) {
        StringBuilder sb = new StringBuilder();
        sb.append("请为以下学科出题：\n\n");
        sb.append("科目：").append(req.getSubjectName()).append("\n");

        if (req.getType() != null && !"mixed".equals(req.getType())) {
            String typeName = switch (req.getType()) {
                case "single" -> "单选题";
                case "multiple" -> "多选题";
                case "judge" -> "判断题";
                default -> "单选题";
            };
            sb.append("题型：").append(typeName).append("\n");
        } else {
            sb.append("题型：混合（适当分配单选题、多选题、判断题）\n");
        }

        sb.append("难度：").append(req.getDifficulty()).append("（1=非常简单, 2=简单, 3=中等, 4=较难, 5=困难）\n");
        sb.append("数量：").append(req.getCount()).append("道\n");

        // 获取已有题库作为参考样例
        List<Question> existing = questionMapper.selectBySubject(req.getSubjectId(), userId);
        if (existing != null && !existing.isEmpty()) {
            Collections.shuffle(existing);
            List<Question> samples = existing.subList(0, Math.min(10, existing.size()));
            sb.append("\n请参考以下已有题库样例，生成风格和难度相似但内容不同的新题目：\n");
            for (Question q : samples) {
                String typeName = switch (q.getType()) {
                    case "single" -> "单选";
                    case "multiple" -> "多选";
                    case "judge" -> "判断";
                    default -> q.getType();
                };
                String truncated = q.getContent().length() > 80
                        ? q.getContent().substring(0, 80) + "…"
                        : q.getContent();
                sb.append("- [").append(typeName).append("][难度").append(q.getDifficulty()).append("] ")
                        .append(truncated).append("\n");
            }
            sb.append("\n注意：生成的题目不得与上述样例重复，但应保持相似的出题风格和知识点覆盖面。\n");
        }

        sb.append("""

                请以如下 JSON 格式返回（不要包含 markdown 代码块标记）：
                {
                  "questions": [
                    {
                      "type": "single",
                      "content": "题目内容（仅题干的文字描述，不含选项）",
                      "options": ["A. 选项A内容", "B. 选项B内容", "C. 选项C内容", "D. 选项D内容"],
                      "answer": "A",
                      "analysis": "详细的解析说明，解释为什么选这个答案，其他选项为什么不对"
                    }
                  ]
                }

                注意：
                1. type 值为 single（单选）、multiple（多选）、judge（判断）
                2. 单选题必须有 4 个选项（A/B/C/D），多选题至少 4 个选项
                3. 单选题和多选题的 answer 为大写字母，如 "A" 或 "A,B,C"
                4. 判断题的 options 为 []（空数组），answer 为 "T"（正确）或 "F"（错误）
                5. analysis 必须详细，至少 30 字
                """);

        return sb.toString();
    }

    /**
     * 解析 AI 返回的 JSON 响应
     */
    private List<AIGeneratedQuestion> parseResponse(String response) {
        try {
            // 去除可能的 markdown 代码块标记
            String json = response.trim();
            if (json.startsWith("```")) {
                json = json.substring(json.indexOf("\n") + 1);
                if (json.endsWith("```")) {
                    json = json.substring(0, json.lastIndexOf("```"));
                }
                json = json.trim();
            }

            JsonNode root = objectMapper.readTree(json);

            // DeepSeek 返回格式：choices[0].message.content 包含 JSON
            if (root.has("choices")) {
                String content = root.get("choices").get(0).get("message").get("content").asText();
                root = objectMapper.readTree(content);
            }

            // 解析 questions 数组
            JsonNode questionsNode = root.get("questions");
            if (questionsNode == null || !questionsNode.isArray()) {
                throw new BusinessException("AI 返回的题目格式异常，请重试");
            }

            List<AIGeneratedQuestion> questions = objectMapper.convertValue(
                    questionsNode, new TypeReference<List<AIGeneratedQuestion>>() {});

            if (questions.isEmpty()) {
                throw new BusinessException("AI 未生成任何题目，请重试");
            }

            return questions;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("解析 AI 响应失败: {}", response, e);
            throw new BusinessException("AI 返回内容解析失败，请重试");
        }
    }

    @Override
    public String analyze(String systemPrompt, String userPrompt) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", model);
        body.put("messages", List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", userPrompt)
        ));
        body.put("temperature", 0.7);
        body.put("max_tokens", 1024);

        try {
            String response = webClient.post()
                    .uri("/v1/chat/completions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(30))
                    .block();

            JsonNode root = objectMapper.readTree(response);
            return root.get("choices").get(0).get("message").get("content").asText();
        } catch (WebClientResponseException e) {
            log.error("AI API 错误: {}", e.getResponseBodyAsString());
            throw new BusinessException("AI 分析失败，请稍后重试");
        } catch (Exception e) {
            log.error("AI 分析异常", e);
            throw new BusinessException("AI 分析失败: " + e.getMessage());
        }
    }
}
