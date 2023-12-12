package com.dying.usercenter.job;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dying.usercenter.model.domain.User;
import com.dying.usercenter.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 缓存预热
 *
 * @author dying
 */
@Component
@Slf4j
public class PreCacheJob {
    @Resource
    private UserService userService;

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private RedissonClient redissonClient;
    private List<Long> mainUserList = Arrays.asList(1L);

    //每天执行，预热推荐用户
    @Scheduled(cron = "0 0 0 * * *")
    public void doCacheRecommendUser(){
        RLock lock = redissonClient.getLock("dying:precachejob:dpcache:lock");
        try {
            //只有一个线程能获取锁
            //默认自动续期时间30秒，怕宕机，每隔10秒自动增加时间
            if(!lock.tryLock(0 , -1 , TimeUnit.MICROSECONDS)){
                return ;
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        Page<User> userPage = userService.page(new Page<>(1 , 20) ,  queryWrapper);
        for(Long userId : mainUserList){
            //无缓冲
            String redisKey = String.format("partner:user:recommend:%s" , userId);
            ValueOperations valueOperations = redisTemplate.opsForValue();
            try {
                valueOperations.set(redisKey , userPage  , 30000 , TimeUnit.MICROSECONDS);
            } catch (Exception e) {
                log.error("redis set key error" , e);
            }finally {
                //只能释放自己锁
                if(lock.isHeldByCurrentThread()){
                    lock.unlock();;
                }
            }
        }
    }
}
