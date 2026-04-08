package com.search.mcp.mcpserver.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

@Slf4j
@Service
public class LocalDirectoryService {

    private static final String DIRECTORY_PATH = "D:\\study\\面试";

    @Tool(description = "从本地目录‘D:\\study\\面试’搜索文件")
    public String searchFiles(String searchText) {
        File directory = new File(DIRECTORY_PATH);
        if (!directory.exists() || !directory.isDirectory()) {
            return "本地目录 'D:\\study\\面试' 不存在或不是一个目录";
        }

        List<File> matchingFiles = searchFiles(directory, searchText);
        if (matchingFiles.isEmpty()) {
            return "在本地目录中未找到匹配的文件";
        }

        StringJoiner joiner = new StringJoiner("\n");
        for (File file : matchingFiles) {
            joiner.add(String.format("- [%s](file:///%s)", file.getName(), file.getAbsolutePath().replace('\\', '/')));
        }
        return joiner.toString();
    }

    private List<File> searchFiles(File directory, String searchText) {
        List<File> matchingFiles = new ArrayList<>();
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.getName().toLowerCase().contains(searchText.toLowerCase())) {
                    matchingFiles.add(file);
                }
            }
        }
        return matchingFiles;
    }
}
