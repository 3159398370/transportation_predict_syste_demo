package com.tps.springboot.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tps.springboot.controller.dto.UserDTO;
import com.tps.springboot.controller.dto.UserPasswordDTO;
import com.tps.springboot.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;


public interface IUserService extends IService<User> {


    UserDTO login(UserDTO userDTO);

    User register(UserDTO userDTO);

    void updatePassword(UserPasswordDTO userPasswordDTO);

    Page<User> findPage(Page<User> objectPage, String username, String email, String address);

    void saveUpdateUser(User user);


}
