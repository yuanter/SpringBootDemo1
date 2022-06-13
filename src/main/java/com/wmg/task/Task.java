package com.wmg.task;

import cn.hutool.core.util.StrUtil;
import cn.yiidii.pigeon.common.core.exception.BizException;
import com.alibaba.fastjson.JSONObject;
import com.wmg.Service.ExecService;
import com.wmg.util.TimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;
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
@Component//需要注册到bean中
@Slf4j
//所有的定时任务都放在一个线程池中，定时任务启动时使用不同都线程。
public class Task implements SchedulingConfigurer {

    private final static Executor executor = Executors.newCachedThreadPool();//启用多线程
    static int count = 0;

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
        taskRegistrar.addTriggerTask(() -> {
            try {
                process();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, triggerContext ->{
            //此处的corn表达式可以换成数据库的方式接入，如使用Mapper
            if (corn.isEmpty()) {
                throw new BizException("定时打卡表达式为空");
            }
            return new CronTrigger(corn).nextExecutionTime(triggerContext);
        } );
    }

    /**
    *@Description: 执行打卡任务
    *@Param:
    *@return:
    *@Author: wmg
    *@date: 2022/6/9 1:06
    */
    public void process() throws InterruptedException {
        Set<String> keys = redisTemplate.keys("XiaoMiYunDong_*");
        if (!Objects.isNull(keys)) {
            for (String str : keys) {
                //final int j=i; //关键是这一句代码，将 i 转化为  j，这样j 还是final类型的参与线程
                final String key = str;
                count++;
                int number=(int)(Math.random()*(1)+1);
                Thread.sleep(number*1000);
//                executor.execute(new Runnable() {
//                    @Override
//                    public void run() {
//                    }
//                });
                String phoneNumber = "";
                try{
                    Object redisData = redisTemplate.opsForValue().get(key);
                    //log.info("查询数据：{}",redisData);
                    if (!Objects.isNull(redisData)){
                        JSONObject responseJo = JSONObject.parseObject(redisData.toString());
//                                System.out.println(responseJo);
                        phoneNumber = responseJo.getString("phoneNumber");
                        String password = responseJo.getString("password");
                        Integer minSteps = responseJo.getInteger("minSteps");
                        Integer maxSteps = responseJo.getInteger("maxSteps");
                        Integer steps = ThreadLocalRandom.current().nextInt(minSteps, maxSteps+1);
                        execService.exec(phoneNumber,password,steps);



                    }
                }catch(Exception e){
                    System.out.println(StrUtil.format("当前账号：{}打卡失败，打卡时间为：{},异常为：{}", phoneNumber,TimeUtil.getOkDate(new Date().toString()),e.getMessage()));
                }
                if (count >= 30){
                    number=(int)(Math.random()*(10)+600);
                    System.out.println("5分钟内打卡数达到30个，解黑休眠"+number+"秒，继续打卡");
                    Thread.sleep(number*1000);
                    count = 0;
                }
            }
        }

    }


    /**
    *@Description: 校验失效账号
    *@Param:
    *@return:
    *@Author: wmg
    *@date: 2022/6/9 1:34
    */
    public void check() throws Exception {
        Set<String> keys = redisTemplate.keys("XiaoMiYunDong_*");
        if (!Objects.isNull(keys)) {
            for (String key : keys) {
                //执行三次打卡,定时10秒一次，三次失效，删除
                int flag = 0;
                String phoneNumber = "";
                String password = "";
                String minSteps = "";
                String maxSteps = "";
                for (int i = 0; i < 3; i++){
                    try{
                        Object redisData = redisTemplate.opsForValue().get(key);
                        if (!Objects.isNull(redisData)){
                            JSONObject responseJo = JSONObject.parseObject(redisData.toString());
                            phoneNumber = responseJo.getString("phoneNumber");
                            System.out.println(StrUtil.format("当前账号：{}，正在执行第{}次检测操作",phoneNumber,i));
                            password = responseJo.getString("password");
                            minSteps = responseJo.getString("minSteps");
                            maxSteps = responseJo.getString("maxSteps");
                            execService.check(phoneNumber, password);
                        }else {
                            continue;
                        }
                    }catch(Exception e){
                        System.out.println("执行错误信息为："+e.getMessage());
                        flag++;
                    }

                    int number=(int)(Math.random()*(5-1)+5);
                    System.out.println(StrUtil.format("随机休眠{}秒，继续执行检测操作",number));
                    Thread.sleep(number*1000);
                }
                if (flag == 3){
                    System.out.println(StrUtil.format("当前账号：{}，已失效，执行删除操作",phoneNumber));
                    //移除失效账号
                    redisTemplate.delete(key);
                    //新增失效账号
                    //表示多少秒过期，可以设置时间的计数单位，有分，小时，年，月，日等
                    //redisTemplate.opsForValue().set("XiaoMiYunDong_"+phoneNumber, jsonObject, 600, TimeUnit.SECONDS);
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("phoneNumber",phoneNumber);
                    jsonObject.put("password",password);
                    jsonObject.put("minSteps",minSteps);
                    jsonObject.put("maxSteps",maxSteps);
                    redisTemplate.opsForValue().set("XMYD_ShiXiao_"+phoneNumber, jsonObject.toString());
                }
                flag = 0;
            }
        }
    }

}