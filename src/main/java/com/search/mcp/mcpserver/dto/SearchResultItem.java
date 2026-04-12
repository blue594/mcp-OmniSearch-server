package com.search.mcp.mcpserver.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 搜索结果项
 */
@Data
public class SearchResultItem implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 标题
     */
    private String title;

    /**
     * 链接
     */
    private String url;

    /**
     * 来源：mianshiya-面试鸭, javaguide-JavaGuide, local-本地文件
     */
    private String source;

    /**
     * 描述/摘要
     */
    private String description;

    /**
     * 相关性评分（用于排序）
     */
    private Integer relevanceScore;

    public SearchResultItem() {
    }

    public SearchResultItem(String title, String url, String source, String description) {
        this.title = title;
        this.url = url;
        this.source = source;
        this.description = description;
        this.relevanceScore = 0;
    }

}
