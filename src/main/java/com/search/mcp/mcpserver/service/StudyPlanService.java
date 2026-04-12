package com.search.mcp.mcpserver.service;

import cn.hutool.json.JSONUtil;
import com.search.mcp.mcpserver.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 学习计划服务
 * 整合多个搜索工具，生成系统化的学习路径
 */
@Slf4j
@Service
public class StudyPlanService {

    private final MianshiyaService mianshiyaService;
    private final JavaGuideService javaGuideService;
    private final AiSummaryService aiSummaryService;

    public StudyPlanService(MianshiyaService mianshiyaService,
                           JavaGuideService javaGuideService,
                           AiSummaryService aiSummaryService) {
        this.mianshiyaService = mianshiyaService;
        this.javaGuideService = javaGuideService;
        this.aiSummaryService = aiSummaryService;
    }

    /**
     * 生成系统学习计划
     * 当用户提问"如何系统学习Java并发"等类似问题时调用
     *
     * @param topic 学习主题，如"Java并发"
     * @return 学习计划JSON
     */
    @Tool(description = "生成系统学习计划，当用户询问如何系统学习某个技术主题时调用，如'如何系统学习Java并发'")
    public String generateStudyPlan(String topic) {
        log.info("生成学习计划，主题：{}", topic);
        
        try {
            // 1. 调用三个搜索工具获取资源
            List<SearchResultItem> allResults = searchAllSources(topic);
            
            if (allResults.isEmpty()) {
                return JSONUtil.toJsonStr(Map.of(
                    "status", "empty",
                    "message", "未找到相关学习资源",
                    "topic", topic
                ));
            }

            // 2. 智能排序和去重
            List<SearchResultItem> sortedResults = sortAndDeduplicate(allResults, topic);

            // 3. 生成AI总结和学习路径
            String aiSummary = aiSummaryService.generateStudyPlanSummary(topic, sortedResults);

            // 4. 构建分阶段学习路径
            StudyPlanResponse plan = buildStudyPlan(topic, sortedResults, aiSummary);

            // 5. 返回JSON格式结果
            return JSONUtil.toJsonStr(plan);

        } catch (Exception e) {
            log.error("生成学习计划失败", e);
            return JSONUtil.toJsonStr(Map.of(
                "status", "error",
                "message", "生成学习计划失败: " + e.getMessage(),
                "topic", topic
            ));
        }
    }

    /**
     * 搜索所有来源
     */
    private List<SearchResultItem> searchAllSources(String topic) {
        List<SearchResultItem> allResults = new ArrayList<>();

        // 搜索面试鸭
        try {
            String mianshiyaResult = mianshiyaService.questionSearch(topic);
            List<SearchResultItem> mianshiyaItems = parseMarkdownResults(mianshiyaResult, "mianshiya");
            allResults.addAll(mianshiyaItems);
            log.info("面试鸭搜索结果：{}条", mianshiyaItems.size());
        } catch (Exception e) {
            log.warn("面试鸭搜索失败", e);
        }

        // 搜索JavaGuide
        try {
            String javaGuideResult = javaGuideService.searchArticles(topic);
            List<SearchResultItem> javaGuideItems = parseMarkdownResults(javaGuideResult, "javaguide");
            allResults.addAll(javaGuideItems);
            log.info("JavaGuide搜索结果：{}条", javaGuideItems.size());
        } catch (Exception e) {
            log.warn("JavaGuide搜索失败", e);
        }

        return allResults;
    }

    /**
     * 解析Markdown格式的搜索结果
     * 格式：- [标题](链接)
     */
    private List<SearchResultItem> parseMarkdownResults(String markdown, String source) {
        List<SearchResultItem> items = new ArrayList<>();
        
        if (markdown == null || markdown.isEmpty() 
            || markdown.contains("无搜索结果") 
            || markdown.contains("未找到")
            || markdown.contains("失败")
            || markdown.contains("异常")) {
            return items;
        }

        String[] lines = markdown.split("\n");
        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("- [")) {
                // 解析 - [标题](链接) 格式
                int titleStart = line.indexOf("[") + 1;
                int titleEnd = line.indexOf("]");
                int urlStart = line.indexOf("(") + 1;
                int urlEnd = line.indexOf(")");
                
                if (titleStart > 0 && titleEnd > titleStart && urlStart > titleEnd && urlEnd > urlStart) {
                    String title = line.substring(titleStart, titleEnd);
                    String url = line.substring(urlStart, urlEnd);
                    items.add(new SearchResultItem(title, url, source, ""));
                }
            }
        }

        return items;
    }

    /**
     * 智能排序和去重
     */
    private List<SearchResultItem> sortAndDeduplicate(List<SearchResultItem> results, String topic) {
        // 1. 去重：基于标题相似度
        Set<String> seenTitles = new HashSet<>();
        List<SearchResultItem> uniqueResults = new ArrayList<>();
        
        for (SearchResultItem item : results) {
            String normalizedTitle = item.getTitle().toLowerCase().replaceAll("\\s+", "");
            boolean isDuplicate = false;
            
            for (String seen : seenTitles) {
                // 如果标题相似度超过80%，认为是重复
                if (calculateSimilarity(normalizedTitle, seen) > 0.8) {
                    isDuplicate = true;
                    break;
                }
            }
            
            if (!isDuplicate) {
                seenTitles.add(normalizedTitle);
                uniqueResults.add(item);
            }
        }

        // 2. 计算相关性评分并排序
        String lowerTopic = topic.toLowerCase();
        for (SearchResultItem item : uniqueResults) {
            int score = calculateRelevanceScore(item, lowerTopic);
            item.setRelevanceScore(score);
        }

        // 按相关性评分降序排序
        uniqueResults.sort((a, b) -> b.getRelevanceScore().compareTo(a.getRelevanceScore()));

        // 3. 限制返回数量，优先保留不同来源的结果
        return balanceSources(uniqueResults, 15);
    }

    /**
     * 计算两个字符串的相似度（简单实现）
     */
    private double calculateSimilarity(String s1, String s2) {
        int longerLength = Math.max(s1.length(), s2.length());
        if (longerLength == 0) return 1.0;
        
        int commonLength = 0;
        for (char c : s1.toCharArray()) {
            if (s2.indexOf(c) >= 0) {
                commonLength++;
            }
        }
        
        return (double) commonLength / longerLength;
    }

    /**
     * 计算相关性评分
     */
    private int calculateRelevanceScore(SearchResultItem item, String topic) {
        int score = 0;
        String title = item.getTitle().toLowerCase();
        
        // 标题包含完整主题词
        if (title.contains(topic)) {
            score += 10;
        }
        
        // 标题包含主题词的部分关键词
        String[] keywords = topic.split("\\s+");
        for (String keyword : keywords) {
            if (keyword.length() > 1 && title.contains(keyword)) {
                score += 3;
            }
        }
        
        // 来源权重
        switch (item.getSource()) {
            case "mianshiya" -> score += 5;  // 面试鸭优先
            case "javaguide" -> score += 3;  // JavaGuide次之
        }
        
        return score;
    }

    /**
     * 平衡各来源的结果数量
     */
    private List<SearchResultItem> balanceSources(List<SearchResultItem> results, int maxTotal) {
        Map<String, List<SearchResultItem>> bySource = results.stream()
            .collect(Collectors.groupingBy(SearchResultItem::getSource));

        List<SearchResultItem> balanced = new ArrayList<>();
        int perSourceLimit = maxTotal / 2 + 1;

        // 从每个来源取结果
        for (List<SearchResultItem> sourceItems : bySource.values()) {
            int count = 0;
            for (SearchResultItem item : sourceItems) {
                if (count >= perSourceLimit) break;
                balanced.add(item);
                count++;
            }
        }

        // 按原有顺序排序
        balanced.sort((a, b) -> {
            int idxA = results.indexOf(a);
            int idxB = results.indexOf(b);
            return Integer.compare(idxA, idxB);
        });

        // 限制总数
        return balanced.size() > maxTotal ? balanced.subList(0, maxTotal) : balanced;
    }

    /**
     * 构建学习计划
     */
    private StudyPlanResponse buildStudyPlan(String topic, List<SearchResultItem> results, String aiSummary) {
        StudyPlanResponse plan = new StudyPlanResponse();
        plan.setTopic(topic);
        plan.setLevel("intermediate");
        plan.setTotalDays(21);
        plan.setAiSummary(aiSummary);

        // 构建推荐资源列表
        List<StudyPlanResponse.Resource> resources = results.stream()
            .map(item -> {
                StudyPlanResponse.Resource resource = new StudyPlanResponse.Resource();
                resource.setName(item.getTitle());
                resource.setUrl(item.getUrl());
                resource.setType(item.getSource());
                resource.setDescription(getSourceDescription(item.getSource()));
                return resource;
            })
            .collect(Collectors.toList());
        plan.setResources(resources);

        // 构建分阶段学习路径
        List<StudyPlanResponse.StudyPhase> phases = buildPhases(results, topic);
        plan.setPhases(phases);

        return plan;
    }

    /**
     * 获取来源描述
     */
    private String getSourceDescription(String source) {
        return switch (source) {
            case "mianshiya" -> "面试鸭面试题库，包含详细答案";
            case "javaguide" -> "JavaGuide开源文档，系统学习资料";
            default -> "学习资源";
        };
    }

    /**
     * 构建学习阶段
     */
    private List<StudyPlanResponse.StudyPhase> buildPhases(List<SearchResultItem> results, String topic) {
        List<StudyPlanResponse.StudyPhase> phases = new ArrayList<>();

        // 基础阶段
        StudyPlanResponse.StudyPhase phase1 = new StudyPlanResponse.StudyPhase();
        phase1.setName("基础阶段");
        phase1.setDescription("掌握" + topic + "的核心概念和基础知识");
        phase1.setDays(7);
        phase1.setKeyPoints(Arrays.asList(
            "理解核心概念",
            "掌握基础语法和API",
            "了解常见使用场景"
        ));
        phase1.setResources(getResourcesByPhase(results, "基础"));
        phases.add(phase1);

        // 进阶阶段
        StudyPlanResponse.StudyPhase phase2 = new StudyPlanResponse.StudyPhase();
        phase2.setName("进阶阶段");
        phase2.setDescription("深入理解原理，掌握高级特性");
        phase2.setDays(7);
        phase2.setKeyPoints(Arrays.asList(
            "深入源码分析",
            "理解底层实现原理",
            "掌握性能优化技巧"
        ));
        phase2.setResources(getResourcesByPhase(results, "进阶"));
        phases.add(phase2);

        // 实战阶段
        StudyPlanResponse.StudyPhase phase3 = new StudyPlanResponse.StudyPhase();
        phase3.setName("实战阶段");
        phase3.setDescription("通过面试题和实际案例巩固知识");
        phase3.setDays(7);
        phase3.setKeyPoints(Arrays.asList(
            "刷面试题巩固知识点",
            "分析实际应用场景",
            "总结常见问题和解决方案"
        ));
        phase3.setResources(getResourcesByPhase(results, "实战"));
        phases.add(phase3);

        return phases;
    }

    /**
     * 根据阶段获取相关资源
     */
    private List<String> getResourcesByPhase(List<SearchResultItem> results, String phase) {
        return results.stream()
            .limit(5)
            .map(item -> String.format("[%s](%s)", item.getTitle(), item.getUrl()))
            .collect(Collectors.toList());
    }

}
