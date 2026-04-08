# mcp-OmniSearch-server

## 简介

`mcp-OmniSearch-server` 是一个基于 MCP (Model Context Protocol) 协议的综合搜索服务器。它集成了多个数据源，旨在为智能体助手（如 `Claude`、`Cursor`、`Cherry Studio` 等）提供一站式的面试题目和学习资料搜索能力。

### 支持的功能

- **面试鸭题目搜索**：快速检索 [面试鸭](https://mianshiya.com/) 上的面试题目及链接。
- **JavaGuide 文章搜索**：从知名的 [JavaGuide](https://javaguide.cn/) 网站搜索相关的技术文章。
- **本地资料搜索**：支持搜索本地指定目录（如 `D:\study\面试`）下的学习资料。

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

#### 3. 本地文件搜索 `searchFiles` (本地目录)
- **功能**: 从本地目录 `D:\study\面试` 中搜索匹配的文件。
- **输入**: `searchText` (搜索词)
- **输出**: `[文件名](file:///路径)` 列表。

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
5. 现在你可以直接询问：“帮我搜索一下 Java 集合的面试题”或“在 JavaGuide 中找找关于 Redis 的文章”。

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
