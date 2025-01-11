package org.example.shortlink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.example.shortlink.project.dao.entity.LinkNetworkStatsDO;

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
}
