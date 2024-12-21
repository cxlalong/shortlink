package org.example.shortlink.project.service.impl;

import org.example.shortlink.project.service.UrlTitleService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

/**
 * 标题接口实现层
 */
@Service
public class UrlTitleServiceImpl implements UrlTitleService {
    /**
     * 根据url获取网页标题
     * @param url
     * @return
     */
    public String getUrlTitle(String url) {
        try {
            // 发送GET请求并解析HTML文档
            Document document = Jsoup.connect(url).get();

            // 获取<title>标签的内容
            return document.title();
        } catch (Exception e) {
            // 处理异常情况
            e.printStackTrace();
            return "Error fetching title";
        }
    }
}
