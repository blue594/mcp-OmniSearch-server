package com.search.mcp.mcpserver;

import com.search.mcp.mcpserver.service.JavaGuideService;
import com.search.mcp.mcpserver.service.MianshiyaService;
import com.search.mcp.mcpserver.service.StudyPlanService;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class McpServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(McpServerApplication.class, args);
    }
    @Bean
    public ToolCallbackProvider serverTools(MianshiyaService mianshiyaService,
                                            JavaGuideService javaGuideService,
                                            StudyPlanService studyPlanService) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(mianshiyaService, javaGuideService, studyPlanService)
                .build();
    }

}
