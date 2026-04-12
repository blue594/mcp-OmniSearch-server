package com.search.mcp.mcpserver.service;

import cn.hutool.json.JSONUtil;
import com.search.mcp.mcpserver.dto.AiSummaryRequest;
import com.search.mcp.mcpserver.dto.AiSummaryResponse;
import com.search.mcp.mcpserver.dto.SearchResultItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * AI总结服务
 * 使用现有的LLM接口对搜索结果进行智能总结
 */
@Slf4j
@Service
public class AiSummaryService {

    private final ChatClient chatClient;

    public AiSummaryService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    /**
     * 对搜索结果进行AI总结
     *
     * @param request 总结请求
     * @return 总结响应
     */
    public AiSummaryResponse summarize(AiSummaryRequest request) {
        try {
            if (request.getResults() == null || request.getResults().isEmpty()) {
                return AiSummaryResponse.failed("没有可总结的搜索结果");
            }

            String promptText = buildSummaryPrompt(request);
            
            String response = chatClient.prompt()
                    .system("你是一位专业的技术学习顾问，擅长对技术搜索结果进行智能总结和提炼。请用简洁易懂的中文返回JSON格式结果。")
                    .user(promptText)
                    .call()
                    .content();

            // 尝试解析JSON响应
            AiSummaryResponse summaryResponse = parseSummaryResponse(response);
            summaryResponse.setOriginalCount(request.getResults().size());
            
            log.info("AI总结完成，关键词：{}，原始结果数：{}", request.getKeyword(), request.getResults().size());
            return summaryResponse;

        } catch (Exception e) {
            log.error("AI总结失败", e);
            return AiSummaryResponse.failed("AI总结失败: " + e.getMessage());
        }
    }

    /**
     * 生成学习计划总结
     *
     * @param topic   学习主题
     * @param results 搜索结果
     * @return 学习计划总结
     */
    public String generateStudyPlanSummary(String topic, List<SearchResultItem> results) {
        try {
            String promptText = buildStudyPlanPrompt(topic, results);
            
            return chatClient.prompt()
                    .system("你是一位专业的Java技术学习顾问，擅长制定系统化的学习计划。请用简洁易懂的中文回答。")
                    .user(promptText)
                    .call()
                    .content();

        } catch (Exception e) {
            log.error("生成学习计划失败", e);
            return "生成学习计划失败: " + e.getMessage();
        }
    }

    /**
     * 构建总结提示词
     */
    private String buildSummaryPrompt(AiSummaryRequest request) {
        StringBuilder sb = new StringBuilder();
        sb.append("请对以下关于\"").append(request.getKeyword()).append("\"的搜索结果进行智能总结和提炼。\n\n");
        sb.append("搜索结果：\n");
        
        int index = 1;
        for (SearchResultItem item : request.getResults()) {
            sb.append(index++).append(". ").append(item.getTitle());
            sb.append(" (来源：").append(getSourceName(item.getSource())).append(")\n");
            if (item.getDescription() != null && !item.getDescription().isEmpty()) {
                sb.append("   描述：").append(item.getDescription()).append("\n");
            }
            sb.append("   链接：").append(item.getUrl()).append("\n\n");
        }

        sb.append("\n请按以下JSON格式返回总结结果（只返回JSON，不要其他内容）：\n");
        sb.append("{\n");
        sb.append("  \"summary\": \"用2-3句话概括这些搜索结果的核心内容\",\n");
        sb.append("  \"keyPoints\": [\"关键要点1\", \"关键要点2\", \"关键要点3\"],\n");
        sb.append("  \"recommendedOrder\": [\"推荐学习顺序1\", \"推荐学习顺序2\"]\n");
        sb.append("}\n");

        return sb.toString();
    }

    /**
     * 构建学习计划提示词
     */
    private String buildStudyPlanPrompt(String topic, List<SearchResultItem> results) {
        StringBuilder sb = new StringBuilder();
        sb.append("请根据以下关于\"").append(topic).append("\"的搜索结果，生成一份系统化的学习计划。\n\n");
        sb.append("可用学习资源：\n");
        
        int index = 1;
        for (SearchResultItem item : results) {
            sb.append(index++).append(". ").append(item.getTitle());
            sb.append(" (").append(getSourceName(item.getSource())).append(")\n");
            sb.append("   链接：").append(item.getUrl()).append("\n\n");
        }

        sb.append("\n请生成一份分阶段的学习计划，包含：\n");
        sb.append("1. 学习阶段划分（如基础、进阶、实战）\n");
        sb.append("2. 每个阶段的学习目标和要点\n");
        sb.append("3. 推荐的资源使用顺序\n");
        sb.append("4. 预计学习时间\n");
        sb.append("\n请以清晰的结构返回，便于用户按步骤学习。");

        return sb.toString();
    }

    /**
     * 解析总结响应
     */
    private AiSummaryResponse parseSummaryResponse(String response) {
        try {
            // 尝试直接解析JSON
            if (response.trim().startsWith("{")) {
                return JSONUtil.toBean(response, AiSummaryResponse.class);
            }
            
            // 尝试提取JSON部分
            int start = response.indexOf("{");
            int end = response.lastIndexOf("}");
            if (start >= 0 && end > start) {
                String json = response.substring(start, end + 1);
                return JSONUtil.toBean(json, AiSummaryResponse.class);
            }
            
            // 如果解析失败，返回原始内容作为总结
            AiSummaryResponse result = new AiSummaryResponse();
            result.setSummary(response);
            result.setKeyPoints(new ArrayList<>());
            result.setRecommendedOrder(new ArrayList<>());
            result.setStatus("success");
            return result;
            
        } catch (Exception e) {
            // 解析失败，返回原始内容
            AiSummaryResponse result = new AiSummaryResponse();
            result.setSummary(response);
            result.setKeyPoints(new ArrayList<>());
            result.setRecommendedOrder(new ArrayList<>());
            result.setStatus("success");
            return result;
        }
    }

    /**
     * 获取来源名称
     */
    private String getSourceName(String source) {
        return switch (source != null ? source.toLowerCase() : "") {
            case "mianshiya" -> "面试鸭";
            case "javaguide" -> "JavaGuide";
            case "local" -> "本地文件";
            default -> "未知来源";
        };
    }

}
