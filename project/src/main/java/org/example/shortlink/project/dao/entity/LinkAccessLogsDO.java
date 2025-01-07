package org.example.shortlink.project.dao.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.shortlink.project.common.database.BaseDO;

/**
 * 访问记录表
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("t_link_access_logs")
public class LinkAccessLogsDO extends BaseDO {

    private Long id; // ID
    private String fullShortUrl; // 完整短链接
    private String gid; // 分组标识
    private String user; // 用户信息
    private String browser; // 浏览器
    private String os; // 操作系统
    private String ip; // IP
}