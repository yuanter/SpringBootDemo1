package com.wmg.util;

import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * Author: wmg
 * Date: 2022/12/5 2:05
 * 当前注释由自定义模板生成
 * To change this template use File | Settings | File Templates.
 * Description:
 */
public class EmailUtil {
    /**
     * 校验邮箱
     *
     * @param email
     * @return
     * [a-zA-Z0-9]+@[a-zA-Z0-9]+\.[a-zA-Z0-9]+
     */
    public static boolean isEmail(String email) {
        if ((email != null) && (!email.isEmpty())) {
            return Pattern.matches("^(\\w+([-.][A-Za-z0-9]+)*){3,18}@\\w+([-.][A-Za-z0-9]+)*\\.\\w+([-.][A-Za-z0-9]+)*$", email);
        }
        return false;
    }
}