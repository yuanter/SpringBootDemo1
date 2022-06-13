package com.wmg.Service;

import cn.yiidii.pigeon.common.core.base.R;
import com.alibaba.fastjson.JSONObject;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author wmg
 * @Create 2022/4/6 2:28
 */
public interface ExecService {

    /**
    *@Description: 执行打卡业务
    *@Param: phoneNumber, password, steps
    *@return:
    *@Author: wmg
    *@date: 2022/4/6 2:28
    */
    String exec(String phoneNumber, String password, Integer steps) throws Exception;

    /**
    *@Description: 删除失效账号
    *@Param:
    *@return:
    *@Author: wmg
    *@date: 2022/6/9 1:10
    */
    JSONObject check(String phoneNumber, String password) throws Exception;
}
