package org.example.shortlink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.example.shortlink.project.dao.entity.LinkLocaleStatsDO;

/**
 * 地区统计 Mapper
 */
public interface LinkLocaleStatsMapper extends BaseMapper<LinkLocaleStatsDO> {
    @Insert({
            "INSERT INTO t_link_locale_stats (",
            "  full_short_url, gid, date, cnt, province, city, adcode, country, create_time, update_time, del_flag",
            ") VALUES (",
            "  #{linkLocaleStats.fullShortUrl,jdbcType=VARCHAR},",
            "  #{linkLocaleStats.gid,jdbcType=VARCHAR},",
            "  #{linkLocaleStats.date,jdbcType=DATE},",
            "  #{linkLocaleStats.cnt,jdbcType=INTEGER},",
            "  #{linkLocaleStats.province,jdbcType=VARCHAR},",
            "  #{linkLocaleStats.city,jdbcType=VARCHAR},",
            "  #{linkLocaleStats.adcode,jdbcType=VARCHAR},",
            "  #{linkLocaleStats.country,jdbcType=VARCHAR},",
            "  NOW(), NOW(), 0",
            ")",
            "ON DUPLICATE KEY UPDATE",
            "  cnt = cnt + #{linkLocaleStats.cnt},",
            "  update_time = NOW()"
    })
    void shortLinkLocaleStats(@Param("linkLocaleStats") LinkLocaleStatsDO linkLocaleStatsDO);
}
