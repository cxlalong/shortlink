package org.example.shortlink.admin.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.shortlink.admin.dao.entity.GroupUniqueDO;

/**
 * 短链接分组唯一路由持久层
 * 公众号：马丁玩编程，回复：加群，添加马哥微信（备注：link）获取项目资料
 */
@Mapper
public interface GroupUniqueMapper extends BaseMapper<GroupUniqueDO> {
}