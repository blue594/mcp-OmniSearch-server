# mcp-OmniSearch-server

## 简介

`mcp-OmniSearch-server` 是一个基于 MCP (Model Context Protocol) 协议的综合搜索服务器。它集成了多个数据源，旨在为智能体助手（如 `Claude`、`Cursor`、`Cherry Studio` 等）提供一站式的面试题目和学习资料搜索能力。

### 支持的功能

- **面试鸭题目搜索**：快速检索 [面试鸭](https://mianshiya.com/) 上的面试题目及链接。
- **JavaGuide 文章搜索**：从知名的 [JavaGuide](https://javaguide.cn/) 网站搜索相关的技术文章。
- **学习计划生成**：智能整合多个数据源，为用户生成系统化的学习路径。
- **AI 智能总结**：基于大模型对搜索结果进行智能提炼，返回简洁易懂的中文总结。

关于 MCP 协议的详细信息，请参阅 [官方文档](https://modelcontextprotocol.io/)。

本项目依赖 `Spring AI` 和 `MCP Java SDK` 开发。

## 工具列表

#### 1. 题目搜索 `questionSearch` (来自面试鸭)
- **功能**: 将面试题目检索为面试鸭里的题目链接。
- **输入**: `searchText` (搜索词)
- **输出**: `[题目](链接)` 列表。

#### 2. 文章搜索 `searchArticles` (来自 JavaGuide)
- **功能**: 从 JavaGuide 网站搜索相关的技术文章。
- **输入**: `searchText` (搜索词)
- **输出**: `[文章标题](链接)` 列表。

#### 3. 学习计划生成 `generateStudyPlan`
- **功能**: 当用户询问如何系统学习某个技术主题时，自动调用面试鸭和 JavaGuide 搜索相关资源，进行智能排序和去重后，生成一份分阶段的学习计划。
- **输入**: `topic` (学习主题，如 "Java并发")
- **输出**: JSON 格式的学习计划，包含：
  - `topic`: 学习主题
  - `totalDays`: 预计学习天数
  - `phases`: 分阶段学习路径（基础阶段、进阶阶段、实战阶段）
  - `resources`: 推荐资源列表
  - `aiSummary`: AI 生成的学习建议总结

#### 4. AI 智能总结服务
- **功能**: 自动对搜索结果进行智能提炼和总结，帮助用户快速理解核心内容。
- **工作原理**: 使用 Spring AI 的 ChatClient 调用大模型接口，对多条搜索结果进行分析。
- **输出**: JSON 格式的总结结果，包含：
  - `summary`: 核心内容概括（2-3句话）
  - `keyPoints`: 关键要点列表
  - `recommendedOrder`: 推荐学习顺序
- **特点**:
  - 复用现有 LLM 接口，不引入新服务
  - 代码轻量，独立 Service 组件
  - 返回格式统一为 JSON

## 快速开始

### 环境准备

- Java 17 或更高版本
- Maven

### 安装与构建

1. **克隆仓库**

``` bash
git clone https://github.com/blue594/mcp-OmniSearch-server
```

2. **构建项目**

``` bash
cd mcp-OmniSearch-server
mvn clean package
```

### 客户端接入 (以 Cherry Studio 为例)

1. 打开 `Cherry Studio` 的 `设置`，点击 `MCP 服务器`。
2. 点击 `编辑 JSON`，将以下配置添加到配置文件中（请根据实际路径替换 `jar` 包位置）：

``` json
{
  "mcpServers": {
    "omniSearchServer": {
      "command": "java",
      "args": [
        "-Dspring.ai.mcp.server.stdio=true",
        "-Dspring.main.web-application-type=none",
        "-Dlogging.pattern.console=",
        "-jar",
        "/yourPath/mcp-server-0.0.1-SNAPSHOT.jar"
      ],
      "env": {}
    }
  }
}
```

3. 在模型服务设置中，勾选工具函数调用功能。
4. 在对话框中勾选开启该 MCP 服务。
5. 现在你可以直接询问：
   - "帮我搜索一下 Java 集合的面试题"
   - "在 JavaGuide 中找找关于 Redis 的文章"
   - "如何系统学习 Java 并发"（将触发学习计划生成工具）

## 代码调用示例

### 1. 引入依赖

``` xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-mcp-client-spring-boot-starter</artifactId>
    <version>1.0.0-M6</version>
</dependency>
```

### 2. 初始化 ChatClient

``` java
@Bean
public ChatClient initChatClient(ChatClient.Builder chatClientBuilder,
                                 ToolCallbackProvider mcpTools) {
    return chatClientBuilder
    .defaultTools(mcpTools)
    .build();
}
```

### 3. 调用

``` java
Flux<String> content = chatClient.prompt()
        .user("帮我搜索面试鸭里关于 Spring Boot 的题目")
        .stream()
        .content();
```

## 项目架构

```
src/main/java/com/search/mcp/mcpserver/
├── McpServerApplication.java    # 主类，注册所有Tools
├── dto/                         # 数据传输对象
│   ├── BaseResponse.java        # 通用响应
│   ├── Page.java                # 分页对象
│   ├── QuestionVO.java          # 面试题目VO
│   ├── SearchResultItem.java    # 搜索结果项
│   ├── StudyPlanRequest.java    # 学习计划请求
│   ├── StudyPlanResponse.java   # 学习计划响应
│   ├── AiSummaryRequest.java    # AI总结请求
│   └── AiSummaryResponse.java   # AI总结响应
└── service/                     # 服务层
    ├── MianshiyaService.java    # 面试鸭搜索
    ├── JavaGuideService.java    # JavaGuide搜索
    ├── StudyPlanService.java    # 学习计划生成
    └── AiSummaryService.java    # AI总结服务
```
