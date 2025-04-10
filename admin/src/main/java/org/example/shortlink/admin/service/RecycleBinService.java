package org.example.shortlink.admin.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.shortlink.admin.common.convention.result.Result;
import org.example.shortlink.admin.remote.dto.req.ShortLinkRecycleBinPageReqDTO;
import org.example.shortlink.admin.remote.dto.resp.ShortLinkPageRespDTO;

public interface RecycleBinService {

    /**
     * 分页查询回收站短链接
     *
     * @param requestParam 请求参数
     * @return 返回参数包装
     */
    Result<Page<ShortLinkPageRespDTO>> pageRecycleBinShortLink(ShortLinkRecycleBinPageReqDTO requestParam);
}