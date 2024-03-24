package org.example.shortlink.admin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.example.shortlink.admin.dao.entity.GroupDO;
import org.example.shortlink.admin.dao.mapper.GroupMapper;
import org.example.shortlink.admin.service.GroupService;
import org.springframework.stereotype.Service;

/**
 * 短链接分组接口实现层
 */
@Service
@RequiredArgsConstructor
public class GroupServiceImpl extends ServiceImpl<GroupMapper, GroupDO> implements GroupService {
}
