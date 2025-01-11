package org.example.shortlink.project.dao.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;
import org.example.shortlink.project.common.database.BaseDO;

import java.util.Date;
/**
 * 浏览器统计
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_link_browser_stats")
public class LinkBrowserStatsDO extends BaseDO {

    private Long id;
    private String fullShortUrl; // 完整短链接
    private String gid;          // 分组标识
    private Date date;           // 日期
    private Integer cnt;         // 访问量
    private String browser;      // 浏览器

}