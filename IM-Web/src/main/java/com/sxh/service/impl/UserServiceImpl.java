package com.sxh.service.impl;

import com.sxh.entity.User;
import com.sxh.entity.UserExample;
import com.sxh.mapper.UserMapper;
import com.sxh.service.UserService;
import com.sxh.utils.UuidUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author sxh
 * @date 2022/2/17
 */
@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;

    @Override
    public User save(User user) {
        user.setUserUid(UuidUtil.getUid());
        int effectRow = userMapper.insert(user);
        if (effectRow == 0) {
            throw new RuntimeException("新增失败！");
        }
        return userMapper.selectByPrimaryKey(user.getUserUid());
    }

    @Override
    public User selectByAccount(String account) {
        UserExample userExample = new UserExample();
        userExample.createCriteria()
                .andAccountEqualTo(account)
                .andIsDeleteEqualTo(0);
        List<User> users = userMapper.selectByExample(userExample);
        if (CollectionUtils.isNotEmpty(users)) {
            return users.get(0);
        }
        return null;
    }
}
