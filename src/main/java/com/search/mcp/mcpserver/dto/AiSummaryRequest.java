package com.search.mcp.mcpserver.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * AI总结请求
 */
@Data
public class AiSummaryRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 搜索关键词
     */
    private String keyword;

    /**
     * 搜索结果列表
     */
    private List<SearchResultItem> results;

    /**
     * 总结类型：simple-简单总结, detailed-详细总结, study_plan-学习计划
     */
    private String summaryType;

}
