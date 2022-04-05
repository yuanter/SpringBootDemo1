package com.wmg.Service;

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
}
