package com.wmg.controller;

import cn.hutool.core.util.PhoneUtil;
import cn.yiidii.pigeon.common.core.base.R;
import cn.yiidii.pigeon.common.core.exception.BizException;
import com.alibaba.fastjson.JSONObject;
import com.wmg.Service.ExecService;
import com.wmg.task.Task;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotNull;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

@RestController
@Slf4j
@SuppressWarnings("all")
public class HttpController {


    @Autowired
    private ExecService execService;
    //注入redis
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private Task task;

    //是否启用定时打卡配置
    @Value("${demo.isSave}")
    private String isSave;
    //定时表达式
    @Value("${demo.corn}")
    private String corn;
    //页面提示文字
    @Value("${demo.tip}")
    private String tip;


    /**
    *@Description: 根据配置文件返回是否需要加载自动保存账号功能
    *@Param:
    *@return:
    *@Author: wmg
    *@date: 2022/4/2 13:22
    */
    @GetMapping("isShow")
    public R<JSONObject> isShow() throws Exception {
        //默认启动
        Boolean flag = true;
        //为空则说明不开启
        if (isSave == null || isSave.equals("") || isSave.equals("false")){
            flag = false;
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("tip",tip);
        jsonObject.put("flag",flag);
        return R.ok(jsonObject);
    }


    //主函数
    @RequestMapping("mi")
    public R mainHandler(@RequestParam @NotNull(message = "手机号码为空，不允许访问") String phoneNumber, @RequestParam @NotNull(message = "密码为空，请检查") String password,
                         @RequestParam(value="mode",required=false) String mode, @RequestParam(value="steps",required=false) Integer steps,
                         @RequestParam(value="minSteps",required=false) Integer minSteps,@RequestParam(value="maxSteps",required=false) Integer maxSteps) throws Exception{
        Assert.isTrue(PhoneUtil.isMobile(phoneNumber), () -> {
            throw new BizException("手机号格式不正确");
        });

        //兼容api接口
        if (mode == null || mode.equals("")){
            mode = "noSave";
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("phoneNumber",phoneNumber);
        jsonObject.put("password",password);
        //自动打卡
        if (mode.equals("save")){
            //检查最小最大步数参数
            if (!(minSteps instanceof Integer)){
                throw new BizException("最小步数类型出错");
            }
            if (minSteps < 1){
                throw new BizException("最小步数不能小于1");
            }
            if (!(maxSteps instanceof Integer)){
                throw new BizException("最大步数类型出错");
            }
            if (maxSteps > 100000){
                throw new BizException("最大步数不能大于100000");
            }
            jsonObject.put("minSteps",minSteps);
            jsonObject.put("maxSteps",maxSteps);
        }else if (mode.equals("noSave")){
            //检查步数参数
            if (!(steps instanceof Integer)){
                throw new BizException("步数类型出错");
            }
            if (steps > 100000 || steps < 1){
                throw new BizException("请检查步数是否正确，步数范围：1~100000");
            }
        }

        if (steps == null || steps == 0){
            steps = ThreadLocalRandom.current().nextInt(minSteps, maxSteps+1);
            System.out.println("自动打卡生成步数为：" + steps);
        }

//        log.info(StrUtil.format("手机号码：{}，密码：{}", phoneNumber,password));

        String result = execService.exec(phoneNumber, password, steps);

        //判断当前账号密码是否需要存储
        //默认需要存储打卡
        boolean flag = false;
        if (mode != null && mode.equals("save")){
            flag = true;
        }

        //先判断redis是否存在当前手机号，存在则更新数据
        Object redisData = redisTemplate.opsForValue().get("XiaoMiYunDong_"+phoneNumber);
        if (!Objects.isNull(redisData)){
            //根据手机号码删除信息
            //redisTemplate.delete("XiaoMiYunDong_"+phoneNumber);
        }
        if (flag){
            //表示多少秒过期，可以设置时间的计数单位，有分，小时，年，月，日等
            //redisTemplate.opsForValue().set("XiaoMiYunDong_"+phoneNumber, jsonObject, 600, TimeUnit.SECONDS);
            redisTemplate.opsForValue().set("XiaoMiYunDong_"+phoneNumber, jsonObject.toString());

        }

        return R.ok(result);
    }


    @GetMapping("all")
    public R allPush() throws InterruptedException {
        task.process();
        return R.ok().setMsg("全部用户手动打卡成功");
    }

    @GetMapping("check")
    public R check() throws Exception {
        task.check();
        return R.ok().setMsg("全部用户是否账号失效检测成功");
    }
    
    /**
    *@Description: 直接新增数据
    *@Param: 
    *@return: 
    *@Author: wmg
    *@date: 2022/6/13 14:26
    */
    @GetMapping("create")
    public R createData(@RequestParam @NotNull(message = "手机号码为空，不允许访问") String phoneNumber, @RequestParam @NotNull(message = "密码为空，请检查") String password,@RequestParam(value="minSteps",required=false) Integer minSteps,@RequestParam(value="maxSteps",required=false) Integer maxSteps) throws Exception{
        Assert.isTrue(PhoneUtil.isMobile(phoneNumber), () -> {
            throw new BizException("手机号格式不正确");
        });

        //检查最小最大步数参数
        if (!(minSteps instanceof Integer)){
            throw new BizException("最小步数类型出错!");
        }
        if (minSteps < 1){
            throw new BizException("最小步数不能小于1");
        }
        System.out.println("最小步数："+minSteps);

        if (!(maxSteps instanceof Integer)){
            throw new BizException("最大步数类型出错!");
        }
        if (maxSteps > 100000){
            throw new BizException("最大步数不能大于100000");
        }
        System.out.println("最大步数："+maxSteps);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("phoneNumber",phoneNumber);
        jsonObject.put("password",password);
        jsonObject.put("minSteps",minSteps);
        jsonObject.put("maxSteps",maxSteps);
        //表示多少秒过期，可以设置时间的计数单位，有分，小时，年，月，日等
        //redisTemplate.opsForValue().set("XiaoMiYunDong_"+phoneNumber, jsonObject, 600, TimeUnit.SECONDS);
        redisTemplate.opsForValue().set("XiaoMiYunDong_"+phoneNumber, jsonObject.toString());
        return R.ok();
    }

}
