package com.wmg.task;

import cn.yiidii.pigeon.common.core.exception.BizException;
import com.alibaba.fastjson.JSONObject;
import com.wmg.Service.ExecService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Description: 多线程执行定时任务 官网：www.fhadmin.org
 * Designer: jack
 * Date: 2017/8/10
 * Version: 1.0.0
 */
@Configuration
@Slf4j
//所有的定时任务都放在一个线程池中，定时任务启动时使用不同都线程。
public class Task implements SchedulingConfigurer {

    private final static Executor executor = Executors.newCachedThreadPool();//启用多线程

    //定时表达式
    @Value("${demo.corn}")
    private String corn;
    @Autowired
    private ExecService execService;
    //注入redis
    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        //设定一个长度20的定时任务线程池
        //taskRegistrar.setScheduler(Executors.newScheduledThreadPool(30));
        taskRegistrar.addTriggerTask(() ->process(),triggerContext ->{
            //此处的corn表达式可以换成数据库的方式接入，如使用Mapper
            if (corn.isEmpty()) {
                throw new BizException("定时打卡表达式为空");
            }
            return new CronTrigger(corn).nextExecutionTime(triggerContext);
        } );
    }
    private void process() {
        Set<String> keys = redisTemplate.keys("XiaoMiYunDong_*");
        if (!Objects.isNull(keys)) {
            for (String str : keys) {
                //final int j=i; //关键是这一句代码，将 i 转化为  j，这样j 还是final类型的参与线程
                final String key = str;
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        try{
                            Object redisData = redisTemplate.opsForValue().get(key);
                            //log.info("查询数据：{}",redisData);
                            if (!Objects.isNull(redisData)){
                                JSONObject responseJo = JSONObject.parseObject(redisData.toString());
//                                System.out.println(responseJo);
                                String phoneNumber = responseJo.getString("phoneNumber");
                                String password = responseJo.getString("password");
                                Integer minSteps = responseJo.getInteger("minSteps");
                                Integer maxSteps = responseJo.getInteger("maxSteps");
                                Integer steps = ThreadLocalRandom.current().nextInt(minSteps, maxSteps+1);
                                execService.exec(phoneNumber,password,steps);
                            }
                        }catch(Exception e){
                        }
                    }
                });
            }
        }

    }



}