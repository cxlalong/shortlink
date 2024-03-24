package org.example.shortlink.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.shortlink.admin.dao.entity.GroupDO;
import org.example.shortlink.admin.dto.req.ShortLinkGroupUpdateReqDTO;
import org.example.shortlink.admin.dto.resp.ShrotLinkGroupResqDTO;

import java.util.List;

/**
 * 短链接分组接口
 */
public interface GroupService extends IService<GroupDO> {
    /**
     * 新增短链接分组
     * @param groupName 短链接分组名称
     */
    void saveGroup(String groupName);

    /**
     * 查询用户短链接分组集合
     * @return 用户短链接分组集合
     */
    List<ShrotLinkGroupResqDTO> listGroup();

    /**
     * 修改短链接分组名称
     * @param requestParam 修改短链接分组对象
     */
    void updateGroup(ShortLinkGroupUpdateReqDTO requestParam);
}
