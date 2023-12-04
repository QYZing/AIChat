package com.dying.usercenter.Once;

import com.dying.usercenter.mapper.UserMapper;
import com.dying.usercenter.model.domain.User;
import com.dying.usercenter.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;
import java.util.ArrayList;


@SpringBootTest
public class InsertUsersTest {
    @Resource
    private UserService userService;

    /**
     * 批量插入用户
     */
//    @Scheduled(initialDelay = 5000 , fixedRate = Long.MAX_VALUE)
    @Test
    public void doInsertUser(){
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        final int INSERT_NUM = 100000;
        ArrayList<User> userList = new ArrayList<>();
        for(int i = 0; i < INSERT_NUM; i++){
            User user = new User();
            user.setUsername("demoData");
            user.setUserAccount("dying");
            user.setAvatarUrl("https://gimg2.baidu.com/image_search/src=http%3A%2F%2Fsafe-img.xhscdn.com%2Fbw1%2F4d40b566-1f0a-4f8d-bc97-c513df8775b3%3FimageView2%2F2%2Fw%2F1080%2Fformat%2Fjpg&refer=http%3A%2F%2Fsafe-img.xhscdn.com&app=2002&size=f9999,10000&q=a80&n=0&g=0n&fmt=auto?sec=1704034020&t=240678e91d7cebd4557e13a5908eaf38");
            user.setGender(0);
            user.setUserPassword("123456789");
            user.setPhone("123456");
            user.setEmail("email");
            user.setUserStatus(0);
            user.setUserRole(0);
            user.setTags("[]");
            user.setProfile("个人简洁");
            userList.add(user);
        }
        userService.saveBatch(userList , 50000);
        stopWatch.stop();
        System.out.println(stopWatch.getTotalTimeMillis());
    }
}
