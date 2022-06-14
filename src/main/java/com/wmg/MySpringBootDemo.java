package com.wmg;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableAsync        // 2.开启多线程
@SuppressWarnings("all")
public class MySpringBootDemo {
    public static void main(String[] args) {
        //入口
        SpringApplication.run(MySpringBootDemo.class,args);
        System.out.println("http://127.0.0.1:8080");
    }


    /**
     * 如果key和value都使用的StringRedisSerializer序列化器，则推荐使用StringRedisTemplate
     *
     * 配置Redis的Key和Value的序列化器
     * @param redisTemplate 从容器中获取RedisTemplate
     * @return 修改后的RedisTemple
     */
    @Autowired
    private RedisTemplate redisTemplate;
    @Bean
    public RedisTemplate<String, Object> stringSerializerRedisTemplate() {
        RedisSerializer<String> stringSerializer = new StringRedisSerializer();
        redisTemplate.setKeySerializer(stringSerializer);
        redisTemplate.setValueSerializer(stringSerializer);
        redisTemplate.setHashKeySerializer(stringSerializer);
        redisTemplate.setHashValueSerializer(stringSerializer);
        return redisTemplate;
    }


}
