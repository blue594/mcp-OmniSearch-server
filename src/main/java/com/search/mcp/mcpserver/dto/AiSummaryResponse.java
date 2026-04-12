package com.search.mcp.mcpserver.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * AI总结响应
 */
@Data
public class AiSummaryResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 总结内容
     */
    private String summary;

    /**
     * 关键要点
     */
    private List<String> keyPoints;

    /**
     * 推荐学习顺序
     */
    private List<String> recommendedOrder;

    /**
     * 原始结果数量
     */
    private Integer originalCount;

    /**
     * 处理状态：success-成功, failed-失败
     */
    private String status;

    /**
     * 错误信息
     */
    private String errorMessage;

    public static AiSummaryResponse success(String summary, List<String> keyPoints, Integer originalCount) {
        AiSummaryResponse response = new AiSummaryResponse();
        response.setSummary(summary);
        response.setKeyPoints(keyPoints);
        response.setOriginalCount(originalCount);
        response.setStatus("success");
        return response;
    }

    public static AiSummaryResponse failed(String errorMessage) {
        AiSummaryResponse response = new AiSummaryResponse();
        response.setStatus("failed");
        response.setErrorMessage(errorMessage);
        return response;
    }

}
