package org.example.shortlink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.shortlink.project.dao.entity.LinkNetworkStatsDO;
import org.example.shortlink.project.dto.req.ShortLinkGroupStatsReqDTO;
import org.example.shortlink.project.dto.req.ShortLinkStatsReqDTO;

import java.util.List;

public interface LinkNetworkStatsMapper extends BaseMapper<LinkNetworkStatsDO> {

    @Insert({
            "INSERT INTO t_link_network_stats (",
            "  full_short_url, gid, date, cnt, network, create_time, update_time, del_flag",
            ") VALUES (",
            "  #{linkNetworkStats.fullShortUrl,jdbcType=VARCHAR},",
            "  #{linkNetworkStats.gid,jdbcType=VARCHAR},",
            "  #{linkNetworkStats.date,jdbcType=DATE},",
            "  #{linkNetworkStats.cnt,jdbcType=INTEGER},",
            "  #{linkNetworkStats.network,jdbcType=VARCHAR},",
            "  NOW(), NOW(), 0",
            ")",
            "ON DUPLICATE KEY UPDATE",
            "  cnt = cnt + #{linkNetworkStats.cnt},",
            "  network = #{linkNetworkStats.network},", // 如果需要更新network字段
            "  update_time = NOW()"
    })
    void shortLinkNetworkStats(@Param("linkNetworkStats") LinkNetworkStatsDO linkNetworkStats);

    /**
     * 根据短链接获取指定日期内访问网络监控数据
     */
    @Select("SELECT " +
            "    tlns.network, " +
            "    SUM(tlns.cnt) AS cnt " +
            "FROM " +
            "    t_link tl INNER JOIN " +
            "    t_link_network_stats tlns ON tl.full_short_url = tlns.full_short_url " +
            "WHERE " +
            "    tlns.full_short_url = #{param.fullShortUrl} " +
            "    AND tl.gid = #{param.gid} " +
            "    AND tl.del_flag = '0' " +
            "    AND tl.enable_status = #{param.enableStatus} " +
            "    AND tlns.date BETWEEN #{param.startDate} and #{param.endDate} " +
            "GROUP BY " +
            "    tlns.full_short_url, tl.gid, tlns.network;")
    List<LinkNetworkStatsDO> listNetworkStatsByShortLink(@Param("param") ShortLinkStatsReqDTO requestParam);

    /**
     * 根据分组获取指定日期内访问网络监控数据
     */
    @Select("SELECT " +
            "    tlns.network, " +
            "    SUM(tlns.cnt) AS cnt " +
            "FROM " +
            "    t_link tl INNER JOIN " +
            "    t_link_network_stats tlns ON tl.full_short_url = tlns.full_short_url " +
            "WHERE " +
            "    tl.gid = #{param.gid} " +
            "    AND tl.del_flag = '0' " +
            "    AND tl.enable_status = '0' " +
            "    AND tlns.date BETWEEN #{param.startDate} and #{param.endDate} " +
            "GROUP BY " +
            "    tl.gid, tlns.network;")
    List<LinkNetworkStatsDO> listNetworkStatsByGroup(@Param("param") ShortLinkGroupStatsReqDTO requestParam);
}
