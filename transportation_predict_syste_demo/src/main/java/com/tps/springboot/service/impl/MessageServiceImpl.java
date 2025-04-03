package com.tps.springboot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tps.springboot.entity.Message;
import com.tps.springboot.entity.User;
import com.tps.springboot.mapper.MessageMapper;
import com.tps.springboot.mapper.UserMapper;
import com.tps.springboot.service.MessageService;
import com.tps.springboot.utils.TokenUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message> implements MessageService {
    @Autowired
    private MessageMapper messageMapper;
    @Autowired
    private UserMapper userMapper;

    @Override
    public void saveMessage(Message message) {
        User currentUser = TokenUtils.getCurrentUser();
        message.setSendUserId(currentUser.getId());
        message.setSendUserName(currentUser.getUsername());
        message.setSendRealName(currentUser.getNickname());
        messageMapper.insert(message);
    }

    @Override
    public void updateMessage(Message message) {
        LambdaQueryWrapper<Message> LambdaqueryWrapper = new LambdaQueryWrapper<>();
        LambdaqueryWrapper.eq(Message::getId,message.getId());
        Message newMessage = messageMapper.selectOne(LambdaqueryWrapper);

        User currentUser = TokenUtils.getCurrentUser();
        newMessage.setSendUserId(currentUser.getId());

        newMessage.setSendUserName(currentUser.getUsername());
        newMessage.setSendRealName(currentUser.getNickname());

        newMessage.setTitle(message.getTitle());
        newMessage.setContent(message.getContent());
        newMessage.setType(message.getType());
        newMessage.setReceiveUserName(message.getReceiveUserName());
        messageMapper.updateById(newMessage);
    }
}
