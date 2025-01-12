package org.example.shortlink.admin.remote;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.shortlink.admin.common.biz.user.UserContext;
import org.example.shortlink.admin.common.convention.exception.ServiceException;
import org.example.shortlink.admin.common.convention.result.Result;
import org.example.shortlink.admin.dto.req.RecycleBinRecoverReqDTO;
import org.example.shortlink.admin.dto.req.RecycleBinRemoveReqDTO;
import org.example.shortlink.admin.dto.req.RecycleBinSaveReqDTO;
import org.example.shortlink.admin.remote.dto.req.ShortLinkCreateReqDTO;
import org.example.shortlink.admin.remote.dto.req.ShortLinkPageReqDTO;
import org.example.shortlink.admin.remote.dto.req.ShortLinkRecycleBinPageReqDTO;
import org.example.shortlink.admin.remote.dto.req.ShortLinkUpdateReqDTO;
import org.example.shortlink.admin.remote.dto.resp.*;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.example.shortlink.admin.common.convention.errorcode.BaseErrorCode.REMOTE_ERROR;

/**
 * 短链接中台远程调用服务
 */
public interface ShortLinkRemoteService {
    /**
     * 分页查询短链接
     * @param requestParam 分页查询短链接请求参数
     * @return 分页查询短链接响应
     */
    default Result<IPage<ShortLinkPageRespDTO>> pageShortLink(ShortLinkPageReqDTO requestParam) {
        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("gid", requestParam.getGid());
        requestMap.put("current", requestParam.getCurrent());
        requestMap.put("size", requestParam.getSize());
        String resultPageStr = HttpUtil.get("http://127.0.0.1:8001/api/short-link/v1/page", requestMap);
        return JSON.parseObject(resultPageStr, new TypeReference<>() {
        });
    }

    /**
     * 创建短链接
     * @param requestParam 创建短链接请求参数
     * @return 创建短链接创建响应
     */
    default Result<ShortLinkCreateRespDTO> createShrotLink(@RequestBody ShortLinkCreateReqDTO requestParam) {
        String resultBodyStr = HttpUtil.post("http://127.0.0.1:8001/api/short-link/v1/create", JSON.toJSONString(requestParam));
        return JSON.parseObject(resultBodyStr, new TypeReference<>() {});
    }

    /**
     * 查询分组短链接总量
     * @param requestParam 查询分组短链接总量请求参数
     * @return 查询分组短链接总量响应
     */
    default Result<List<ShortLinkGroupCountQueryRespDTO>> listGroupShortLinkCount( List<String> requestParam) {
        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("requestParam", requestParam);
        String resultPageStr = HttpUtil.get("http://127.0.0.1:8001/api/short-link/v1/count", requestMap);
        return JSON.parseObject(resultPageStr, new TypeReference<>() {
        });
    }

    /**
     * 修改短链接
     * @param requestParam 修改短链接请求参数
     *@return 短链接修改返回响应
     */
    default void updateShortLink(@RequestBody ShortLinkUpdateReqDTO requestParam) {
        HttpUtil.post("http://127.0.0.1:8001/api/short-link/v1/update", JSON.toJSONString(requestParam));
    }

    /**
     * 获取url标题
     * @param url
     * @return
     */
    default Result<String> getUrlTitle(@RequestParam("url") String url) {
        String resultBodyStr =HttpUtil.get("http://127.0.0.1:8001/api/short-link/v1/title?url=" + url);
        return JSON.parseObject(resultBodyStr, new TypeReference<>() {});
    }

    default void saveRecycleBin(RecycleBinSaveReqDTO requestParam) {
        HttpUtil.post("http://127.0.0.1:8001/api/short-link/v1/recycle-bin/save", JSON.toJSONString(requestParam));
    }

    default void recoverRecycleBin(RecycleBinRecoverReqDTO requestParam) {
        HttpUtil.post("http://127.0.0.1:8001/api/short-link/v1/recycle-bin/recover", JSON.toJSONString(requestParam));
    }

    default void removeRecycleBin(RecycleBinRemoveReqDTO requestParam) {
        HttpUtil.post("http://127.0.0.1:8001/api/short-link/v1/recycle-bin/remove", JSON.toJSONString(requestParam));
    }

    default Result<IPage<ShortLinkPageRespDTO>> pageShortLink(ShortLinkRecycleBinPageReqDTO requestParam) {
        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("gid", requestParam.getGidList());
        requestMap.put("current", requestParam.getCurrent());
        requestMap.put("size", requestParam.getSize());
        String resultPageStr = HttpUtil.get("http://127.0.0.1:8001/api/short-link/v1/recycle-bin/page", requestMap);
        return JSON.parseObject(resultPageStr, new TypeReference<>() {
        });
    }

    /**
     * 访问单个短链接指定时间内监控数据
     *
     * @param fullShortUrl 完整短链接
     * @param gid          分组标识
     * @param startDate    开始时间
     * @param endDate      结束时间
     * @return 短链接监控信息
     */
    default Result<ShortLinkStatsRespDTO> oneShortLinkStats(@RequestParam("fullShortUrl") String fullShortUrl,
                                                    @RequestParam("gid") String gid,
                                                    @RequestParam("enableStatus") Integer enableStatus,
                                                    @RequestParam("startDate") String startDate,
                                                    @RequestParam("endDate") String endDate) {
        String url = "http://127.0.0.1:8001/api/short-link/v1/stats?fullShortUrl=" + fullShortUrl + "&gid=" + gid + "&enableStatus=" + enableStatus + "&startDate=" + startDate + "&endDate=" + endDate;
        HttpRequest request = new HttpRequest(url)
                .header("username", UserContext.getUsername());
        try {
            String resultBodyStr = request.execute().body();
            return JSON.parseObject(resultBodyStr, new TypeReference<>() {
            });
        } catch (Exception e) {
            throw new ServiceException(REMOTE_ERROR);
        }
    }

    /**
     * 访问分组短链接指定时间内监控数据
     *
     * @param gid       分组标识
     * @param startDate 开始时间
     * @param endDate   结束时间
     * @return 分组短链接监控信息
     */
    default Result<ShortLinkStatsRespDTO> groupShortLinkStats(@RequestParam("gid") String gid,
                                                      @RequestParam("startDate") String startDate,
                                                      @RequestParam("endDate") String endDate) {
        String url = "http://127.0.0.1:8001/api/short-link/v1/stats/group?gid=" + gid + "&startDate=" + startDate + "&endDate=" + endDate;
        HttpRequest request = new HttpRequest(url)
                .header("username", UserContext.getUsername());
        try {
            String resultBodyStr = request.execute().body();
            return JSON.parseObject(resultBodyStr, new TypeReference<>() {
            });
        } catch (Exception e) {
            throw new ServiceException(REMOTE_ERROR);
        }
    }


    /**
     * 访问单个短链接指定时间内监控访问记录数据
     *
     * @param fullShortUrl 完整短链接
     * @param gid          分组标识
     * @param startDate    开始时间
     * @param endDate      结束时间
     * @param current      当前页
     * @param size         一页数据量
     * @return 短链接监控访问记录信息
     */
    default Result<Page<ShortLinkStatsAccessRecordRespDTO>> shortLinkStatsAccessRecord(@RequestParam("fullShortUrl") String fullShortUrl,
                                                                               @RequestParam("gid") String gid,
                                                                               @RequestParam("startDate") String startDate,
                                                                               @RequestParam("endDate") String endDate,
                                                                               @RequestParam("enableStatus") Integer enableStatus,
                                                                               @RequestParam("current") Long current,
                                                                               @RequestParam("size") Long size) {
        String url = "http://127.0.0.1:8001/api/short-link/v1/stats/access-record?fullShortUrl=" + fullShortUrl + "&gid=" + gid + "&startDate=" + startDate + "&endDate=" + endDate + "&enableStatus=" + enableStatus + "&current=" + current + "&size=" + size;
        HttpRequest request = new HttpRequest(url)
                .header("username", UserContext.getUsername());
        try {
            String resultBodyStr = request.execute().body();
            return JSON.parseObject(resultBodyStr, new TypeReference<>() {
            });
        } catch (Exception e) {
            throw new ServiceException(REMOTE_ERROR);
        }
    }

    /**
     * 访问分组短链接指定时间内监控访问记录数据
     *
     * @param gid       分组标识
     * @param startDate 开始时间
     * @param endDate   结束时间
     * @param current   当前页
     * @param size      一页数据量
     * @return 分组短链接监控访问记录信息
     */
    default Result<Page<ShortLinkStatsAccessRecordRespDTO>> groupShortLinkStatsAccessRecord(@RequestParam("gid") String gid,
                                                                                    @RequestParam("startDate") String startDate,
                                                                                    @RequestParam("endDate") String endDate,
                                                                                    @RequestParam("current") Long current,
                                                                                    @RequestParam("size") Long size) {
        String url = "http://127.0.0.1:8001/api/short-link/v1/stats/access-record/group?gid=" + gid + "&startDate=" + startDate + "&endDate=" + endDate + "&current=" + current + "&size=" + size;
        HttpRequest request = new HttpRequest(url)
                .header("username", UserContext.getUsername());
        try {
            String resultBodyStr = request.execute().body();
            return JSON.parseObject(resultBodyStr, new TypeReference<>() {
            });
        } catch (Exception e) {
            throw new ServiceException(REMOTE_ERROR);
        }
    }

}
