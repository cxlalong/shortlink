package org.example.shortlink.project.util;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;

import java.util.Date;
import java.util.Optional;

import static org.example.shortlink.project.common.constant.ShortLinkConstant.DEFAULT_CACHE_VALID_DATE;

/**
 * 短链接工具类
 */
public class LinkUtil {

    /**
     * 获取按链接缓存有效期时间
     * @param validDate 有效期时间
     * @return 有效期时间戳
     */
    public static long getLinkCacheValidDate(Date validDate) {
        return Optional.ofNullable(validDate).map(each -> DateUtil.between(new Date(), each, DateUnit.MS))
                .orElse(DEFAULT_CACHE_VALID_DATE);
    }
}
