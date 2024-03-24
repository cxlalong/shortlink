package org.example.shortlink.admin.dto.req;

import lombok.Data;

/**
 * 短链接分组新增请求参数
 */
@Data
public class ShortLinkGroupSaveReqDTO {
    /**
     * 分组名
     */
    private String name;
}
