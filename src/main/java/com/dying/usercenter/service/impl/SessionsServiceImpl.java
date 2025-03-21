package com.dying.usercenter.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dying.usercenter.model.domain.Sessions;
import com.dying.usercenter.service.SessionsService;
import com.dying.usercenter.mapper.SessionsMapper;
import org.springframework.stereotype.Service;

/**
* @author 86198
* @description 针对表【sessions】的数据库操作Service实现
* @createDate 2025-03-17 17:20:37
*/
@Service
public class SessionsServiceImpl extends ServiceImpl<SessionsMapper, Sessions>
    implements SessionsService{

}




