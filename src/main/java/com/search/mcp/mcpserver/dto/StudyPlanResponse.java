package com.search.mcp.mcpserver.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 学习计划响应
 */
@Data
public class StudyPlanResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 学习主题
     */
    private String topic;

    /**
     * 学习阶段
     */
    private String level;

    /**
     * 总预计天数
     */
    private Integer totalDays;

    /**
     * 学习阶段列表
     */
    private List<StudyPhase> phases;

    /**
     * 推荐资源
     */
    private List<Resource> resources;

    /**
     * AI总结
     */
    private String aiSummary;

    @Data
    public static class StudyPhase implements Serializable {
        private static final long serialVersionUID = 1L;

        /**
         * 阶段名称
         */
        private String name;

        /**
         * 阶段描述
         */
        private String description;

        /**
         * 预计天数
         */
        private Integer days;

        /**
         * 学习要点
         */
        private List<String> keyPoints;

        /**
         * 相关资源链接
         */
        private List<String> resources;
    }

    @Data
    public static class Resource implements Serializable {
        private static final long serialVersionUID = 1L;

        /**
         * 资源名称
         */
        private String name;

        /**
         * 资源链接
         */
        private String url;

        /**
         * 资源类型：mianshiya-面试鸭, javaguide-JavaGuide, local-本地文件
         */
        private String type;

        /**
         * 资源描述
         */
        private String description;
    }

}
