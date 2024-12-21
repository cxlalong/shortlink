package org.example.shortlink.project.service;

/**
 * 标题接口层
 */
public interface UrlTitleService {
    /**
     * 根据url获取标题
     * @param url
     * @return
     */
    String getUrlTitle(String url);
}
