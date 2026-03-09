package com.search.mcp.mcpserver.service;

import cn.hutool.http.HttpRequest;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.StringJoiner;

/**
 * @author gulihua
 * @Description
 * @date 2025-03-26 11:35
 */
@Slf4j
@Service
public class JavaGuideService {

    private static final String BASE_URL = "https://javaguide.cn/";

    @Tool(description = "从JavaGuide网站搜索文章")
    public String searchArticles(String searchText) {
        try {
            String searchUrl = BASE_URL + "#" + searchText;
            String html = HttpRequest.get(searchUrl).execute().body();
            Document doc = Jsoup.parse(html);
            Elements articles = doc.select("a.sidebar-link");
            StringJoiner joiner = new StringJoiner("\n");
            int count = 0;
            for (Element article : articles) {
                String title = article.text();
                if (title.toLowerCase().contains(searchText.toLowerCase())) {
                    String link = article.attr("href");
                    if (!link.startsWith("http")) {
                        link = BASE_URL + link;
                    }
                    joiner.add(String.format("- [%s](%s)", title, link));
                    count++;
                    if (count >= 5) {
                        break;
                    }
                }
            }
            if (count > 0) {
                return joiner.toString();
            } else {
                return "在JavaGuide中未找到相关文章";
            }
        } catch (Exception e) {
            log.error("从JavaGuide抓取数据失败", e);
            return "从JavaGuide抓取数据失败: " + e.getMessage();
        }
    }
}
