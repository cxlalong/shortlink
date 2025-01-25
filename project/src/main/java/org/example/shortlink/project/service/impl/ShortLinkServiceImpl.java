package org.example.shortlink.project.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.text.StrBuilder;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.example.shortlink.project.common.convention.exception.ClientException;
import org.example.shortlink.project.common.convention.exception.ServiceException;
import org.example.shortlink.project.common.enums.ValidDateTypeEnum;
import org.example.shortlink.project.config.GotoDomainWhiteListConfiguration;
import org.example.shortlink.project.dao.entity.ShortLinkDO;
import org.example.shortlink.project.dao.entity.ShortLinkGotoDO;
import org.example.shortlink.project.dao.mapper.ShortLinkGotoMapper;
import org.example.shortlink.project.dao.mapper.ShortLinkMapper;
import org.example.shortlink.project.dto.biz.ShortLinkStatsRecordDTO;
import org.example.shortlink.project.dto.req.ShortLinkBatchCreateReqDTO;
import org.example.shortlink.project.dto.req.ShortLinkCreateReqDTO;
import org.example.shortlink.project.dto.req.ShortLinkPageReqDTO;
import org.example.shortlink.project.dto.req.ShortLinkUpdateReqDTO;
import org.example.shortlink.project.dto.resp.*;
import org.example.shortlink.project.mq.producer.ShortLinkStatsSaveProducer;
import org.example.shortlink.project.service.ShortLinkService;
import org.example.shortlink.project.util.HashUtil;
import org.example.shortlink.project.util.IpAddressUtil;
import org.example.shortlink.project.util.LinkUtil;
import org.example.shortlink.project.util.UserAgentUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.example.shortlink.project.common.constant.RedisKeyConstant.*;

/**
 * 短链接接口层
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ShortLinkServiceImpl extends ServiceImpl<ShortLinkMapper, ShortLinkDO> implements ShortLinkService {

    private final RBloomFilter<String> shortUriCreateCachePenetrationBloomFilter;

    private final GotoDomainWhiteListConfiguration gotoDomainWhiteListConfiguration;

    private final ShortLinkGotoMapper shortLinkGotoMapper;

    private final StringRedisTemplate stringRedisTemplate;

    private final RedissonClient redissonClient;

    private final ShortLinkStatsSaveProducer shortLinkStatsSaveProducer;

    @Value("${short-link.stats.locale.amap-key}")
    private String key;

    @Value("${short-link.domain.default}")
    private String createShortLinkDefaultDomain;
    @Override
    public ShortLinkCreateRespDTO createShortLink(ShortLinkCreateReqDTO requestParam) {
        verificationWhitelist(requestParam.getOriginUrl());
        String shortLinkSuffix = generateSuffix(requestParam);
        String fullShortUrl = StrBuilder.create(createShortLinkDefaultDomain)
                .append("/")
                .append(shortLinkSuffix)
                .toString();
        ShortLinkDO shortLinkDO = ShortLinkDO.builder()
                .domain(createShortLinkDefaultDomain)
                .originUrl(requestParam.getOriginUrl())
                .gid(requestParam.getGid())
                .createdType(requestParam.getCreatedType())
                .validDateType(requestParam.getValidDateType())
                .validDate(requestParam.getValidDate())
                .describe(requestParam.getDescribe())
                .shortUri(shortLinkSuffix)
                .enableStatus(0)
                .totalPv(0)
                .totalUv(0)
                .totalUip(0)
                .delTime(0L)
                .fullShortUrl(fullShortUrl)
                .favicon(getFavicon(requestParam.getOriginUrl()))
                .build();
        ShortLinkGotoDO shortLinkGotoDO = ShortLinkGotoDO.builder()
                .fullShortUrl(fullShortUrl)
                .gid(requestParam.getGid())
                .build();
        try {
            baseMapper.insert(shortLinkDO);
            shortLinkGotoMapper.insert(shortLinkGotoDO);
        } catch (DuplicateKeyException ex) {
            LambdaQueryWrapper<ShortLinkDO> queryWrapper = Wrappers.lambdaQuery(ShortLinkDO.class)
                    .eq(ShortLinkDO::getFullShortUrl, fullShortUrl);
            ShortLinkDO hasShortLinkDO = baseMapper.selectOne(queryWrapper);
            if (hasShortLinkDO != null) {
                log.warn("短链接：{}重复入库", fullShortUrl);
                throw new ServiceException(String.format("短链接：%s 生成重复", fullShortUrl));
            }
        }
        //缓存预热
        stringRedisTemplate.opsForValue().set(
                String.format(GOTO_SHORT_LINK_KEY, fullShortUrl),
                requestParam.getOriginUrl(),
                LinkUtil.getLinkCacheValidDate(requestParam.getValidDate()),
                TimeUnit.MILLISECONDS);
        shortUriCreateCachePenetrationBloomFilter.add(fullShortUrl);
        return ShortLinkCreateRespDTO.builder()
                .fullShortUrl("http://" + shortLinkDO.getFullShortUrl())
                .originUrl(requestParam.getOriginUrl())
                .gid(requestParam.getGid())
                .build();
    }

    @Override
    public IPage<ShortLinkPageRespDTO> pageShortLink(ShortLinkPageReqDTO requestParam) {
        IPage<ShortLinkDO> resultPage = baseMapper.pageLink(requestParam);
        return resultPage.convert(each -> {
            ShortLinkPageRespDTO result = BeanUtil.toBean(each, ShortLinkPageRespDTO.class);
            result.setDomain("http://" + result.getDomain());
            return result;
        });
    }

    @Override
    public ShortLinkBatchCreateRespDTO batchCreateShortLink(ShortLinkBatchCreateReqDTO requestParam) {
        List<String> originUrls = requestParam.getOriginUrls();
        List<String> describes = requestParam.getDescribes();
        // 创建一个模板对象，用于后续复制
        ShortLinkCreateReqDTO templateRequest = BeanUtil.toBean(requestParam, ShortLinkCreateReqDTO.class);
        List<ShortLinkBaseInfoRespDTO> result = new ArrayList<>();

        // 使用线程池来并发执行任务
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        CompletionService<ShortLinkBaseInfoRespDTO> ecs = new ExecutorCompletionService<>(executor);

        for (int i = 0; i < originUrls.size(); i++) {
            final int index = i;
            ecs.submit(() -> {
                try {
                    // 复制模板对象并设置特定属性
                    ShortLinkCreateReqDTO req = BeanUtil.copyProperties(templateRequest, ShortLinkCreateReqDTO.class);
                    req.setOriginUrl(originUrls.get(index));
                    req.setDescribe(describes.get(index));

                    ShortLinkCreateRespDTO shortLink = createShortLink(req);
                    return ShortLinkBaseInfoRespDTO.builder()
                            .fullShortUrl(shortLink.getFullShortUrl())
                            .originUrl(shortLink.getOriginUrl())
                            .describe(describes.get(index))
                            .build();
                } catch (Throwable ex) {
                    log.error("批量创建短链接失败，原始参数：{}", originUrls.get(index), ex);
                    return null; // 或者返回一个表示错误的对象
                }
            });
        }

        // 收集结果
        for (int i = 0; i < originUrls.size(); i++) {
            try {
                Future<ShortLinkBaseInfoRespDTO> future = ecs.take();
                ShortLinkBaseInfoRespDTO linkBaseInfoRespDTO = future.get();
                if (linkBaseInfoRespDTO != null) { // 忽略因错误而返回null的结果
                    result.add(linkBaseInfoRespDTO);
                }
            } catch (InterruptedException | ExecutionException e) {
                Thread.currentThread().interrupt();
                log.error("收集批量创建短链接结果时出错", e);
            }
        }

        executor.shutdown();

        return ShortLinkBatchCreateRespDTO.builder()
                .total(result.size())
                .baseLinkInfos(result)
                .build();
    }

    @Override
    public List<ShortLinkGroupCountQueryRespDTO> listGroupShortLinkCount(List<String> requestParam) {
        QueryWrapper<ShortLinkDO> queryWrapper = Wrappers.query(new ShortLinkDO())
                .select("gid as gid, count(*) as shortLinkCount")
                .in("gid", requestParam)
                .eq("enable_status", 0)
                .eq("del_flag", 0)
                .eq("del_time", 0L)
                .groupBy("gid");
        List<Map<String, Object>> shortLinkDOList = baseMapper.selectMaps(queryWrapper);
        return BeanUtil.copyToList(shortLinkDOList, ShortLinkGroupCountQueryRespDTO.class);
    }
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateShortLink(ShortLinkUpdateReqDTO requestParam) {
        verificationWhitelist(requestParam.getOriginUrl());
        LambdaQueryWrapper<ShortLinkDO> queryWrapper = Wrappers.lambdaQuery(ShortLinkDO.class)
                .eq(ShortLinkDO::getGid, requestParam.getOriginGid())
                .eq(ShortLinkDO::getFullShortUrl, requestParam.getFullShortUrl())
                .eq(ShortLinkDO::getDelFlag, 0)
                .eq(ShortLinkDO::getEnableStatus, 0);
        ShortLinkDO hasShortLinkDO = baseMapper.selectOne(queryWrapper);
        if (hasShortLinkDO == null) {
            throw new ClientException("短链接记录不存在");
        }
        if (Objects.equals(hasShortLinkDO.getGid(), requestParam.getGid())) {
            LambdaUpdateWrapper<ShortLinkDO> updateWrapper = Wrappers.lambdaUpdate(ShortLinkDO.class)
                    .eq(ShortLinkDO::getFullShortUrl, requestParam.getFullShortUrl())
                    .eq(ShortLinkDO::getGid, requestParam.getGid())
                    .eq(ShortLinkDO::getDelFlag, 0)
                    .eq(ShortLinkDO::getEnableStatus, 0)
                    .set(Objects.equals(requestParam.getValidDateType(), ValidDateTypeEnum.PERMANENT.getType()), ShortLinkDO::getValidDate, null);
            ShortLinkDO shortLinkDO = ShortLinkDO.builder()
                    .domain(hasShortLinkDO.getDomain())
                    .shortUri(hasShortLinkDO.getShortUri())
                    .favicon(Objects.equals(requestParam.getOriginUrl(), hasShortLinkDO.getOriginUrl()) ? hasShortLinkDO.getFavicon() : getFavicon(requestParam.getOriginUrl()))
                    .createdType(hasShortLinkDO.getCreatedType())
                    .gid(requestParam.getGid())
                    .originUrl(requestParam.getOriginUrl())
                    .describe(requestParam.getDescribe())
                    .validDateType(requestParam.getValidDateType())
                    .validDate(requestParam.getValidDate())
                    .build();
            baseMapper.update(shortLinkDO, updateWrapper);
        } else {
            RReadWriteLock readWriteLock = redissonClient.getReadWriteLock(String.format(LOCK_GID_UPDATE_KEY, requestParam.getFullShortUrl()));
            RLock rLock = readWriteLock.writeLock();
            rLock.lock();
            try {
                LambdaUpdateWrapper<ShortLinkDO> linkUpdateWrapper = Wrappers.lambdaUpdate(ShortLinkDO.class)
                        .eq(ShortLinkDO::getFullShortUrl, requestParam.getFullShortUrl())
                        .eq(ShortLinkDO::getGid, hasShortLinkDO.getGid())
                        .eq(ShortLinkDO::getDelFlag, 0)
                        .eq(ShortLinkDO::getDelTime, 0L)
                        .eq(ShortLinkDO::getEnableStatus, 0);
                ShortLinkDO delShortLinkDO = ShortLinkDO.builder()
                        .delTime(System.currentTimeMillis())
                        .build();
                delShortLinkDO.setDelFlag(1);
                baseMapper.update(delShortLinkDO, linkUpdateWrapper);
                ShortLinkDO shortLinkDO = ShortLinkDO.builder()
                        .domain(createShortLinkDefaultDomain)
                        .originUrl(requestParam.getOriginUrl())
                        .gid(requestParam.getGid())
                        .createdType(hasShortLinkDO.getCreatedType())
                        .validDateType(requestParam.getValidDateType())
                        .validDate(requestParam.getValidDate())
                        .describe(requestParam.getDescribe())
                        .shortUri(hasShortLinkDO.getShortUri())
                        .enableStatus(hasShortLinkDO.getEnableStatus())
                        .totalPv(hasShortLinkDO.getTotalPv())
                        .totalUv(hasShortLinkDO.getTotalUv())
                        .totalUip(hasShortLinkDO.getTotalUip())
                        .fullShortUrl(hasShortLinkDO.getFullShortUrl())
                        .favicon(Objects.equals(requestParam.getOriginUrl(), hasShortLinkDO.getOriginUrl()) ? hasShortLinkDO.getFavicon() : getFavicon(requestParam.getOriginUrl()))
                        .delTime(0L)
                        .build();
                baseMapper.insert(shortLinkDO);
                LambdaQueryWrapper<ShortLinkGotoDO> linkGotoQueryWrapper = Wrappers.lambdaQuery(ShortLinkGotoDO.class)
                        .eq(ShortLinkGotoDO::getFullShortUrl, requestParam.getFullShortUrl())
                        .eq(ShortLinkGotoDO::getGid, hasShortLinkDO.getGid());
                ShortLinkGotoDO shortLinkGotoDO = shortLinkGotoMapper.selectOne(linkGotoQueryWrapper);
                shortLinkGotoMapper.delete(linkGotoQueryWrapper);
                shortLinkGotoDO.setGid(requestParam.getGid());
                shortLinkGotoMapper.insert(shortLinkGotoDO);
            } finally {
                rLock.unlock();
            }
        }
        if (!Objects.equals(hasShortLinkDO.getValidDateType(), requestParam.getValidDateType())
                || !Objects.equals(hasShortLinkDO.getValidDate(), requestParam.getValidDate())
                || !Objects.equals(hasShortLinkDO.getOriginUrl(), requestParam.getOriginUrl())) {
            stringRedisTemplate.delete(String.format(GOTO_SHORT_LINK_KEY, requestParam.getFullShortUrl()));
            Date currentDate = new Date();
            if (hasShortLinkDO.getValidDate() != null && hasShortLinkDO.getValidDate().before(currentDate)) {
                if (Objects.equals(requestParam.getValidDateType(), ValidDateTypeEnum.PERMANENT.getType()) || requestParam.getValidDate().after(currentDate)) {
                    stringRedisTemplate.delete(String.format(GOTO_IS_NULL_SHORT_LINK_KEY, requestParam.getFullShortUrl()));
                }
            }
        }
    }

    /**
     * 根据短链接还原原始URL。
     * 该方法首先尝试从Redis中获取短链接对应的原始URL。如果存在，则重定向到原始URL。
     * 如果短链接在Bloom Filter中不存在，表示该短链接可能不存在，直接重定向到未找到页面。
     * 如果短链接在Bloom Filter中存在，但Redis中不存在对应的原始URL，则尝试从数据库中查询。
     * 如果数据库中也不存在，则在Redis中设置标记，并重定向到未找到页面。
     * 如果数据库中存在对应的短链接信息，但该短链接已过期，则在Redis中设置标记，并重定向到未找到页面。
     * 如果数据库中的短链接信息有效，则将原始URL存储到Redis中，并重定向到原始URL。
     * 使用Redisson的分布式锁来确保并发访问时的线程安全。
     *
     * @param shortUri 短链接URI。
     * @param request  Servlet请求对象。
     * @param response Servlet响应对象。
     */
    @SneakyThrows
    @Override
    public void restoreUrl(String shortUri, ServletRequest request, ServletResponse response) {
        // 构建完整的短链接URL
        String serverName = request.getServerName();
        String serverPort = Optional.of(request.getServerPort())
                .filter(each -> !Objects.equals(each, 80))
                .map(String::valueOf)
                .map(each -> ":" + each)
                .orElse("");
        String fullShortUrl = serverName + serverPort + "/" + shortUri;

        // 尝试从Redis中直接获取原始URL
        String originLink = stringRedisTemplate.opsForValue().get(String.format(GOTO_SHORT_LINK_KEY, fullShortUrl));
        if (StrUtil.isNotBlank(originLink)) {
            shortLinkStats(fullShortUrl, null, buildLinkStatsRecordAndSetUser(fullShortUrl, request, response));
            ((HttpServletResponse) response).sendRedirect(originLink);
            return;
        }

        // 检查短链接是否可能不存在
        boolean contains = shortUriCreateCachePenetrationBloomFilter.contains(fullShortUrl);
        if (!contains) {
            ((HttpServletResponse) response).sendRedirect("/page/notfound");
            return;
        }

        // 检查Redis中是否存在标记，表示该短链接确实不存在
        String gotoIsNullShortLink = stringRedisTemplate.opsForValue().get(String.format(GOTO_IS_NULL_SHORT_LINK_KEY, fullShortUrl));
        if (StrUtil.isNotBlank(gotoIsNullShortLink)) {
            ((HttpServletResponse) response).sendRedirect("/page/notfound");
            return;
        }

        // 使用分布式锁防止并发问题
        RLock lock = redissonClient.getLock(String.format(LOCK_GOTO_SHORT_LINK_KEY, fullShortUrl));
        lock.lock();
        try {
            // 再次尝试从Redis中获取原始URL，可能有其他线程已经处理并存储了
            originLink = stringRedisTemplate.opsForValue().get(String.format(GOTO_SHORT_LINK_KEY, fullShortUrl));
            if (StrUtil.isNotBlank(originLink)) {
                shortLinkStats(fullShortUrl, null, buildLinkStatsRecordAndSetUser(fullShortUrl, request, response));
                ((HttpServletResponse) response).sendRedirect(originLink);
                return;
            }

            // 从数据库中查询短链接信息
            LambdaQueryWrapper<ShortLinkGotoDO> linkGotoqueryWrapper = Wrappers.lambdaQuery(ShortLinkGotoDO.class)
                    .eq(ShortLinkGotoDO::getFullShortUrl, fullShortUrl);
            ShortLinkGotoDO shortLinkGotoDO = shortLinkGotoMapper.selectOne(linkGotoqueryWrapper);
            if (shortLinkGotoDO == null) {
                ((HttpServletResponse) response).sendRedirect("/page/notfound");
                stringRedisTemplate.opsForValue().set(String.format(GOTO_IS_NULL_SHORT_LINK_KEY, fullShortUrl), "-", 30, TimeUnit.MINUTES);
                return;
            }

            // 根据短链接信息查询具体的短链接数据
            LambdaQueryWrapper<ShortLinkDO> queryWrapper = Wrappers.lambdaQuery(ShortLinkDO.class)
                    .eq(ShortLinkDO::getGid, shortLinkGotoDO.getGid())
                    .eq(ShortLinkDO::getFullShortUrl, fullShortUrl)
                    .eq(ShortLinkDO::getDelFlag, 0)
                    .eq(ShortLinkDO::getEnableStatus, 0);
            ShortLinkDO shortLinkDO = baseMapper.selectOne(queryWrapper);
            if (shortLinkDO == null || shortLinkDO.getValidDate().before(new Date())) {
                stringRedisTemplate.opsForValue().set(String.format(GOTO_IS_NULL_SHORT_LINK_KEY, fullShortUrl), "-", 30, TimeUnit.MINUTES);
                ((HttpServletResponse) response).sendRedirect("/page/notfound");
                return;
            }
            // 将原始URL存储到Redis中，并设置过期时间
            stringRedisTemplate.opsForValue().set(
                    String.format(GOTO_SHORT_LINK_KEY, fullShortUrl),
                    shortLinkDO.getOriginUrl(),
                    LinkUtil.getLinkCacheValidDate(shortLinkDO.getValidDate()),
                    TimeUnit.MILLISECONDS);
            shortLinkStats(fullShortUrl, shortLinkDO.getGid(), buildLinkStatsRecordAndSetUser(fullShortUrl, request, response));
            ((HttpServletResponse) response).sendRedirect(shortLinkDO.getOriginUrl());
        } finally {
            lock.unlock();
        }
    }

    private ShortLinkStatsRecordDTO buildLinkStatsRecordAndSetUser(String fullShortUrl, ServletRequest request, ServletResponse response) {
        AtomicBoolean uvFirstFlag = new AtomicBoolean();
        Cookie[] cookies = ((HttpServletRequest) request).getCookies();
        AtomicReference<String> uv = new AtomicReference<>();
        Runnable addResponseCookieTask = () -> {
            uv.set(UUID.fastUUID().toString());
            Cookie uvCookie = new Cookie("uv", uv.get());
            uvCookie.setMaxAge(60 * 60 * 24);
            uvCookie.setPath(StrUtil.sub(fullShortUrl, fullShortUrl.indexOf("/"), fullShortUrl.length()));
            ((HttpServletResponse) response).addCookie(uvCookie);
            uvFirstFlag.set(Boolean.TRUE);
            stringRedisTemplate.opsForSet().add(SHORT_LINK_STATS_UV_KEY + fullShortUrl, uv.get());
        };
        if (ArrayUtil.isNotEmpty(cookies)) {
            Arrays.stream(cookies)
                    .filter(each -> Objects.equals(each.getName(), "uv"))
                    .findFirst()
                    .map(Cookie::getValue)
                    .ifPresentOrElse(each -> {
                        uv.set(each);
                        Long uvAdded = stringRedisTemplate.opsForSet().add(SHORT_LINK_STATS_UV_KEY + fullShortUrl, each);
                        uvFirstFlag.set(uvAdded != null && uvAdded > 0L);
                    }, addResponseCookieTask);
        } else {
            addResponseCookieTask.run();
        }
        String remoteAddr = IpAddressUtil.getUserIp(((HttpServletRequest) request));
        String os = UserAgentUtils.getOperatingSystem(((HttpServletRequest) request));
        String browser = UserAgentUtils.getBrowser(((HttpServletRequest) request));
        String device = UserAgentUtils.getDevice(((HttpServletRequest) request));
        String network = UserAgentUtils.getNetwork(((HttpServletRequest) request));
        Long uipAdded = stringRedisTemplate.opsForSet().add(SHORT_LINK_STATS_UIP_KEY + fullShortUrl, remoteAddr);
        boolean uipFirstFlag = uipAdded != null && uipAdded > 0L;
        return ShortLinkStatsRecordDTO.builder()
                .fullShortUrl(fullShortUrl)
                .uv(uv.get())
                .uvFirstFlag(uvFirstFlag.get())
                .uipFirstFlag(uipFirstFlag)
                .remoteAddr(remoteAddr)
                .os(os)
                .browser(browser)
                .device(device)
                .network(network)
                .currentDate(new Date())
                .build();
    }

    public void shortLinkStats(String fullShortUrl, String gid, ShortLinkStatsRecordDTO statsRecord) {
        Map<String, String> producerMap = new HashMap<>();
        producerMap.put("fullShortUrl", fullShortUrl);
        producerMap.put("gid", gid);
        producerMap.put("statsRecord", JSON.toJSONString(statsRecord));
        // 消息队列为什么选用RocketMQ？详情查看：https://nageoffer.com/shortlink/question
        shortLinkStatsSaveProducer.send(producerMap);
    }


    private String generateSuffix(ShortLinkCreateReqDTO requestParam) {
        String shortUri;
        int customGenerateCount = 0;
        while (true) {
            if(customGenerateCount > 10) {
                throw new ClientException("短链接频繁生成，请稍后再试");
            }
            String originUrl = requestParam.getOriginUrl();
            originUrl += System.currentTimeMillis();
            shortUri = HashUtil.hashToBase62(originUrl);
            if (!shortUriCreateCachePenetrationBloomFilter.contains(requestParam.getDomain() + "/" + shortUri)) {
                break;
            }
            customGenerateCount++;
        }
        return shortUri;
    }

    private String fetchFaviconUrl(String siteUrl) {
        try {
            // 确保URL以斜杠结尾
            if (!siteUrl.endsWith("/")) {
                siteUrl += "/";
            }

            // 尝试从HTML文档中获取favicon链接
            Document document = Jsoup.connect(siteUrl).get();
            Element link = document.select("link[rel~=(?i)^(shortcut|icon)$]").first();

            if (link != null) {
                // 使用 abs:href 来获取绝对路径
                return link.attr("abs:href");
            } else {
                // 如果没有找到favicon链接，则尝试获取根目录下的favicon.ico
                URL url = new URL(siteUrl);
                return new URL(url.getProtocol(), url.getHost(), "/favicon.ico").toString();
            }
        } catch (IOException e) {
            // 处理异常情况，例如日志记录，并返回一个友好的错误消息或者默认图标URL
            // 这里只是简单地打印堆栈跟踪，实际应用中应根据需求做适当处理
            e.printStackTrace();
            return "Error fetching favicon: " + e.getMessage();
        }
    }

    private void verificationWhitelist(String originUrl) {
        Boolean enable = gotoDomainWhiteListConfiguration.getEnable();
        if (enable == null || !enable) {
            return;
        }
        String domain = UserAgentUtils.extractDomain(originUrl);
        if (StrUtil.isBlank(domain)) {
            throw new ClientException("跳转链接填写错误");
        }
        List<String> details = gotoDomainWhiteListConfiguration.getDetails();
        if (!details.contains(domain)) {
            throw new ClientException("该网站不安全");
        }
    }

    @SneakyThrows
    private String getFavicon(String url) {
        URL targetUrl = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) targetUrl.openConnection();
        connection.setRequestMethod("GET");
        connection.connect();
        int responseCode = connection.getResponseCode();
        if (HttpURLConnection.HTTP_OK == responseCode) {
            Document document = Jsoup.connect(url).get();
            Element faviconLink = document.select("link[rel~=(?i)^(shortcut )?icon]").first();
            if (faviconLink != null) {
                return faviconLink.attr("abs:href");
            }
        }
        return null;
    }
}
