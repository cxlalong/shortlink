package org.example.shortlink.project.dao.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.shortlink.project.common.database.BaseDO;

import java.util.Date;

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