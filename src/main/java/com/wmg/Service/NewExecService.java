package com.wmg.Service;

import com.alibaba.fastjson.JSONObject;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author wmg
 * @Create 2022/12/4 22:22
 */
public interface NewExecService {
    /***
     * @Description: 根据邮箱或者手机号打卡
     * @Params: username
     * @Params: password
     * @Params: steps
     * @Return: String
     * @Author: wmg
     * @date: 2022/12/4 22:21
     */
    String exec(String username, String password, Integer steps) throws Exception;

    /***
    * @Description: 删除失效账号
    * @Params: phoneNumber
    * @Params: password
    * @Return: JSONObject
    * @Author: wmg
    * @date: 2022/12/5 0:35
    */
    JSONObject check(String phoneNumber, String password) throws Exception;
}
