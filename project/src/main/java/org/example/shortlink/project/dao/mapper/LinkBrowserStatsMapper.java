package org.example.shortlink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.example.shortlink.project.dao.entity.LinkBrowserStatsDO;

public interface LinkBrowserStatsMapper extends BaseMapper<LinkBrowserStatsDO> {
    @Insert({
            "INSERT INTO t_link_browser_stats (",
            "  full_short_url, gid, date, cnt, browser, create_time, update_time, del_flag",
            ") VALUES (",
            "  #{linkBrowserStats.fullShortUrl,jdbcType=VARCHAR},",
            "  #{linkBrowserStats.gid,jdbcType=VARCHAR},",
            "  #{linkBrowserStats.date,jdbcType=DATE},",
            "  #{linkBrowserStats.cnt,jdbcType=INTEGER},",
            "  #{linkBrowserStats.browser,jdbcType=VARCHAR},",
            "  NOW(), NOW(), 0",
            ")",
            "ON DUPLICATE KEY UPDATE",
            "  cnt = cnt + #{linkBrowserStats.cnt},",
            "  update_time = NOW()"
    })
    void shortLinkBrowserStats(@Param("linkBrowserStats") LinkBrowserStatsDO linkBrowserStatsDO);
}
