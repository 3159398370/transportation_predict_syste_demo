package com.tps.springboot.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tps.springboot.entity.Message;


public interface MessageService extends IService<Message> {

    void  saveMessage(Message message);
    void updateMessage(Message message);
}
