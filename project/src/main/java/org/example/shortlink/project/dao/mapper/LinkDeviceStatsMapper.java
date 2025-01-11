package org.example.shortlink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.example.shortlink.project.dao.entity.LinkDeviceStatsDO;

public interface LinkDeviceStatsMapper extends BaseMapper<LinkDeviceStatsDO> {
    @Insert({
            "INSERT INTO t_link_device_stats (",
            "  full_short_url, gid, date, cnt, device, create_time, update_time, del_flag",
            ") VALUES (",
            "  #{linkDeviceStats.fullShortUrl,jdbcType=VARCHAR},",
            "  #{linkDeviceStats.gid,jdbcType=VARCHAR},",
            "  #{linkDeviceStats.date,jdbcType=DATE},",
            "  #{linkDeviceStats.cnt,jdbcType=INTEGER},",
            "  #{linkDeviceStats.device,jdbcType=VARCHAR},",
            "  NOW(),",
            "  NOW(),",
            "  0",
            ")",
            "ON DUPLICATE KEY UPDATE",
            "  cnt = cnt + #{linkDeviceStats.cnt},",
            "  update_time = NOW()"
    })
    void shortLinkDeviceStats(@Param("linkDeviceStats") LinkDeviceStatsDO linkDeviceStats);
}
