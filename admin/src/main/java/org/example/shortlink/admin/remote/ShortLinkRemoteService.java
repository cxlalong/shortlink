package org.example.shortlink.admin.remote;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.shortlink.admin.common.convention.result.Result;
import org.example.shortlink.admin.dto.req.RecycleBinRecoverReqDTO;
import org.example.shortlink.admin.dto.req.RecycleBinRemoveReqDTO;
import org.example.shortlink.admin.dto.req.RecycleBinSaveReqDTO;
import org.example.shortlink.admin.remote.dto.req.*;
import org.example.shortlink.admin.remote.dto.resp.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 短链接中台 Feign 远程调用服务
 */
@Service
@FeignClient(name = "short-link-service", url = "http://127.0.0.1:8001")
public interface ShortLinkRemoteService {

    /**
     * 分页查询短链接
     */
    @GetMapping("/api/short-link/v1/page")
    Result<Page<ShortLinkPageRespDTO>> pageShortLink(@SpringQueryMap ShortLinkPageReqDTO requestParam);

    /**
     * 创建短链接
     */
    @PostMapping("/api/short-link/v1/create")
    Result<ShortLinkCreateRespDTO> createShortLink(@RequestBody ShortLinkCreateReqDTO requestParam);

    /**
     * 查询分组短链接总量
     */
    @GetMapping("/api/short-link/v1/count")
    Result<List<ShortLinkGroupCountQueryRespDTO>> listGroupShortLinkCount(@RequestParam("requestParam") List<String> gids);

    /**
     * 修改短链接
     */
    @PostMapping("/api/short-link/v1/update")
    void updateShortLink(@RequestBody ShortLinkUpdateReqDTO requestParam);

    /**
     * 获取 URL 标题
     */
    @GetMapping("/api/short-link/v1/title")
    Result<String> getUrlTitle(@RequestParam("url") String url);

    /**
     * 保存回收站
     */
    @PostMapping("/api/short-link/v1/recycle-bin/save")
    void saveRecycleBin(@RequestBody RecycleBinSaveReqDTO requestParam);

    /**
     * 恢复回收站短链接
     */
    @PostMapping("/api/short-link/v1/recycle-bin/recover")
    void recoverRecycleBin(@RequestBody RecycleBinRecoverReqDTO requestParam);

    /**
     * 移除回收站短链接
     */
    @PostMapping("/api/short-link/v1/recycle-bin/remove")
    void removeRecycleBin(@RequestBody RecycleBinRemoveReqDTO requestParam);

    /**
     * 回收站分页查询
     */
    @GetMapping("/api/short-link/v1/recycle-bin/page")
    Result<Page<ShortLinkPageRespDTO>> pageRecycleBinShortLink(@SpringQueryMap ShortLinkRecycleBinPageReqDTO requestParam);

    /**
     * 访问单个短链接指定时间内监控数据
     */
    @GetMapping("/api/short-link/v1/stats")
    Result<ShortLinkStatsRespDTO> oneShortLinkStats(
            @RequestParam("fullShortUrl") String fullShortUrl,
            @RequestParam("gid") String gid,
            @RequestParam("enableStatus") Integer enableStatus,
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate);

    /**
     * 访问分组短链接指定时间内监控数据
     */
    @GetMapping("/api/short-link/v1/stats/group")
    Result<ShortLinkStatsRespDTO> groupShortLinkStats(
            @RequestParam("gid") String gid,
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate);

    /**
     * 访问单个短链接指定时间内监控访问记录数据
     */
    @GetMapping("/api/short-link/v1/stats/access-record")
    Result<Page<ShortLinkStatsAccessRecordRespDTO>> shortLinkStatsAccessRecord(
            @RequestParam("fullShortUrl") String fullShortUrl,
            @RequestParam("gid") String gid,
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate,
            @RequestParam("enableStatus") Integer enableStatus,
            @RequestParam("current") Long current,
            @RequestParam("size") Long size);

    /**
     * 访问分组短链接指定时间内监控访问记录数据
     */
    @GetMapping("/api/short-link/v1/stats/access-record/group")
    Result<Page<ShortLinkStatsAccessRecordRespDTO>> groupShortLinkStatsAccessRecord(
            @RequestParam("gid") String gid,
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate,
            @RequestParam("current") Long current,
            @RequestParam("size") Long size);

    /**
     * 批量创建短链接
     */
    @PostMapping("/api/short-link/v1/create/batch")
    Result<ShortLinkBatchCreateRespDTO> batchCreateShortLink(@RequestBody ShortLinkBatchCreateReqDTO requestParam);
}