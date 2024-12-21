package org.example.shortlink.admin.controller;

import lombok.RequiredArgsConstructor;
import org.example.shortlink.admin.common.convention.result.Result;
import org.example.shortlink.admin.common.convention.result.Results;
import org.example.shortlink.admin.remote.ShortLinkRemoteService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * URL标题控制层
 */
@RestController
@RequiredArgsConstructor
public class UrlTittleController {

    ShortLinkRemoteService shortLinkRemoteService  = new ShortLinkRemoteService() {
    };

    /**
     * 根据url获取网站标题
     * @param url
     * @return
     *
     */
    @GetMapping("/api/short-link/admin/v1/title")
    public Result<String> getUrlTitle(@RequestParam("url") String url) {
        return shortLinkRemoteService.getUrlTitle(url);
    }

}
