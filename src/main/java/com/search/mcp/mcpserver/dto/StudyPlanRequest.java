package com.search.mcp.mcpserver.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 学习计划请求
 */
@Data
public class StudyPlanRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 学习主题
     */
    private String topic;

    /**
     * 学习阶段：beginner-初级, intermediate-中级, advanced-高级
     */
    private String level;

    /**
     * 预计学习天数
     */
    private Integer days;

}
