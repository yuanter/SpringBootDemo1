package com.wmg.Service;

import cn.yiidii.pigeon.common.core.base.R;
import com.alibaba.fastjson.JSONObject;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author wmg
 * @Create 2022/4/2 16:34
 */

public interface DaKaService {
    /**
    *@Description: 执行打卡业务
    *@Param:  phoneNumber, password, steps
    *@return:
    *@Author: wmg
    *@date: 2022/4/2 16:38
    */
    String daka(String phoneNumber, String password, Integer steps) throws Exception;
}
