package org.example.shortlink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.shortlink.project.dao.entity.LinkAccessStatsDO;

/**
 * 基础访问统计 Mapper
 */
@Mapper
public interface LinkAccessStatsMapper extends BaseMapper<LinkAccessStatsDO> {

    @Insert("INSERT INTO t_link_access_stats ( full_short_url, gid, date, pv, uv, uip, HOUR, weekday, create_time, update_time, del_flag )" +
            "VALUES" +
            "(#{linkAccessStats.fullShortUrl}, #{linkAccessStats.gid}, #{linkAccessStats.date}, #{linkAccessStats.pv}, #{linkAccessStats.uv}, #{linkAccessStats.uip}, #{linkAccessStats.hour}, #{linkAccessStats.weakday}, NOW(), NOW(), 0 ) " +
            "ON DUPLICATE KEY UPDATE pv = pv + #{linkAccessStats.pv}," +
            "uv = uv + #{linkAccessStats.uv}," +
            "uip = uip + #{linkAccessStats.uip};")
    void ShortLinkStats(@Param("linkAccessStats") LinkAccessStatsDO linkAccessStats);
}
