package com.sxh.service;

import com.sxh.entity.User;

/**
 * 用户信息相关Service
 * @author sxh
 * @date 2022/2/17
 */
public interface UserService {
    /**
     * 新增一条User
     * @param user
     * @return
     */
    User save(User user);

    /**
     * 根据账号查询User
     * @param account
     * @return
     */
    User selectByAccount(String account);
}
