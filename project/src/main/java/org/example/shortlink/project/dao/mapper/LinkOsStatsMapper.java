package org.example.shortlink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.example.shortlink.project.dao.entity.LinkOsStatsDO;
/**
 * 短链地区访问 Mapper
 */
public interface LinkOsStatsMapper extends BaseMapper<LinkOsStatsDO> {
    @Insert({
            "INSERT INTO t_link_os_stats (",
            "  full_short_url, gid, date, cnt, os, create_time, update_time, del_flag",
            ") VALUES (",
            "  #{linkOsStats.fullShortUrl,jdbcType=VARCHAR},",
            "  #{linkOsStats.gid,jdbcType=VARCHAR},",
            "  #{linkOsStats.date,jdbcType=DATE},",
            "  #{linkOsStats.cnt,jdbcType=INTEGER},",
            "  #{linkOsStats.os,jdbcType=VARCHAR},",
            "  NOW(), NOW(), 0",
            ")",
            "ON DUPLICATE KEY UPDATE",
            "  cnt = cnt + #{linkOsStats.cnt},",
            "  update_time = NOW()"
    })
    void shortLinkOsStats(@Param("linkOsStats") LinkOsStatsDO linkOsStatsDO);
}
