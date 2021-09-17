package com.wmg.controller;

import cn.hutool.http.HttpRequest;
import com.fasterxml.jackson.databind.*;
import com.google.common.base.Splitter;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
public class Http {

    private static ObjectMapper objectMapper = new ObjectMapper();

    private static CloseableHttpClient httpClient = HttpClientBuilder.create().build();
    static {
        // 转换为格式化的json
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        // 如果json中有新增的字段并且是实体类类中不存在的，不报错
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    //主函数
    @RequestMapping("mi")
    public String mainHandler(@RequestParam(value="phoneNumber",required=true)String phoneNumber, @RequestParam(value="password",required=true)String password, @RequestParam(value="steps",required=true)Integer steps) {
        HashMap<String, String> map = new HashMap<>();
        if (StringUtils.isEmpty(phoneNumber)){
            return "手机号码为空，不允许访问";
        }
        if (StringUtils.isEmpty(password)){
            return "密码为空，请检查";
        }
        //自己的账号密码
        map.put(phoneNumber, password);
        map.forEach((s, s2) -> {
            String accessCode = getAccessCode(s, s2);
            Map<String, String> login = login(accessCode);
            String login_token = login.get("login_token");
            String user_id = login.get("user_id");
            String appToken = getAppToken(login_token);
            updateStep(appToken, user_id,steps);
        });
        System.out.println("打卡成功，步数为："+ steps);
        return "打卡成功，步数为："+ steps;
    }

    public String getAccessCode(String account,String password) {
        try {
            URIBuilder builder = new URIBuilder("https://api-user.huami.com/registrations/+86" + account + "/tokens");
            HashMap<String, String> data = new HashMap<>();
            data.put("client_id", "HuaMi");
            data.put("password",password);
            data.put("redirect_uri", "https://s3-us-west-2.amazonaws.com/hm-registration/successsignin.html");
            data.put("token", "access");
            data.forEach(builder::setParameter);
            HttpPost httpPost = new HttpPost(builder.build());
            httpPost.setConfig(RequestConfig.custom()
                    .setSocketTimeout(5000)
                    ////默认允许自动重定向
                    .setRedirectsEnabled(false)
                    .build());
            httpPost.addHeader("User-Agent", "MiFit/4.6.0 (iPhone; iOS 14.0.1; Scale/2.00)");
            httpPost.addHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
            HttpResponse execute = httpClient.execute(httpPost);
            int statusCode = execute.getStatusLine().getStatusCode();
            Header location = execute.getFirstHeader("Location");
            String params = location.getValue().substring(location.getValue().indexOf("?") + 1);
            Map<String, String> split = Splitter.on("&").withKeyValueSeparator("=").split(params);
            String s = split.get("access");
            return s;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public Map<String, String> login(String accessCode) {
        try {
            HashMap<String, String> data1 = new HashMap<>();
            data1.put("app_version", "4.6.0");
            data1.put("code",accessCode);
            data1.put("country_code", "CN");
            data1.put("device_id", "2C8B4939-0CCD-4E94-8CBA-CB8EA6E613A1");
            data1.put("device_model", "phone");
            data1.put("grant_type", "access_token");
            data1.put("third_name", "huami_phone");
            data1.put("app_name", "com.xiaomi.hm.health");
            URIBuilder builder1 = new URIBuilder("https://account.huami.com/v2/client/login");
            data1.forEach(builder1::setParameter);
            HttpPost httpPost1 = new HttpPost(builder1.build());
            httpPost1.addHeader("User-Agent", "MiFit/4.6.0 (iPhone; iOS 14.0.1; Scale/2.00)");
            httpPost1.addHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
            HttpResponse execute1 = httpClient.execute(httpPost1);
            String s1 = EntityUtils.toString(execute1.getEntity());
            JsonNode jsonNode = objectMapper.readTree(s1);
            String login_token = jsonNode.get("token_info").get("login_token").asText();
            String user_id = jsonNode.get("token_info").get("user_id").asText();
            HashMap<String, String> map = new HashMap<>();
            map.put("login_token", login_token);
            map.put("user_id", user_id);
            return map;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getAppToken(String login_token) {
        try {
            HttpGet httpGet = new HttpGet("https://account-cn.huami.com/v1/client/app_tokens?app_name=com.xiaomi.hm.health&dn=api-user.huami.com%2Capi-mifit.huami.com%2Capp-analytics.huami.com&login_token=" + login_token + "&os_version=4.1.0");
            CloseableHttpResponse execute3 = httpClient.execute(httpGet);
            String s3 = EntityUtils.toString(execute3.getEntity());
            String app_token = objectMapper.readTree(s3).get("token_info").get("app_token").asText();
            return app_token;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void updateStep(String appToken, String userId, Integer step) {
        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String format = simpleDateFormat.format(date);
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH");
        LocalDateTime localDateTime = LocalDateTime.now();
        String hour = dateTimeFormatter.format(localDateTime);
        if(step == null || step <= 0){
            step = 0;
        }
        if(step >= 98800){
            //超出则最大为98800
            step = 98800;
        }
        //需要自定义随机步数，可以在次数修改
        /*
        int integer = Integer.parseInt(hour);
        if (integer == 10) {
            step = 19999;
        } else if (integer == 11) {
            step = 23456;
        } else if (integer >= 12) {
            step = 66666;
        } else {
            step = 19999;
        }
         */

        String jsonData = "[{\"data_hr\":\"\\/\\/\\/\\/\\/\\/9L\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/Vv\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/0v\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/9e\\/\\/\\/\\/\\/0n\\/a\\/\\/\\/S\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/0b\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/1FK\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/R\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/9PTFFpaf9L\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/R\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/0j\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/9K\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/Ov\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/zf\\/\\/\\/86\\/zr\\/Ov88\\/zf\\/Pf\\/\\/\\/0v\\/S\\/8\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/Sf\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/z3\\/\\/\\/\\/\\/\\/0r\\/Ov\\/\\/\\/\\/\\/\\/S\\/9L\\/zb\\/Sf9K\\/0v\\/Rf9H\\/zj\\/Sf9K\\/0\\/\\/N\\/\\/\\/\\/0D\\/Sf83\\/zr\\/Pf9M\\/0v\\/Ov9e\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/S\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/zv\\/\\/z7\\/O\\/83\\/zv\\/N\\/83\\/zr\\/N\\/86\\/z\\/\\/Nv83\\/zn\\/Xv84\\/zr\\/PP84\\/zj\\/N\\/9e\\/zr\\/N\\/89\\/03\\/P\\/89\\/z3\\/Q\\/9N\\/0v\\/Tv9C\\/0H\\/Of9D\\/zz\\/Of88\\/z\\/\\/PP9A\\/zr\\/N\\/86\\/zz\\/Nv87\\/0D\\/Ov84\\/0v\\/O\\/84\\/zf\\/MP83\\/zH\\/Nv83\\/zf\\/N\\/84\\/zf\\/Of82\\/zf\\/OP83\\/zb\\/Mv81\\/zX\\/R\\/9L\\/0v\\/O\\/9I\\/0T\\/S\\/9A\\/zn\\/Pf89\\/zn\\/Nf9K\\/07\\/N\\/83\\/zn\\/Nv83\\/zv\\/O\\/9A\\/0H\\/Of8\\/\\/zj\\/PP83\\/zj\\/S\\/87\\/zj\\/Nv84\\/zf\\/Of83\\/zf\\/Of83\\/zb\\/Nv9L\\/zj\\/Nv82\\/zb\\/N\\/85\\/zf\\/N\\/9J\\/zf\\/Nv83\\/zj\\/Nv84\\/0r\\/Sv83\\/zf\\/MP\\/\\/\\/zb\\/Mv82\\/zb\\/Of85\\/z7\\/Nv8\\/\\/0r\\/S\\/85\\/0H\\/QP9B\\/0D\\/Nf89\\/zj\\/Ov83\\/zv\\/Nv8\\/\\/0f\\/Sv9O\\/0ZeXv\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/1X\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/9B\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/TP\\/\\/\\/1b\\/\\/\\/\\/\\/\\/0\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/9N\\/\\/\\/\\/\\/\\/\\/\\/\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\\/v7+\",\"date\":\"" +format+ "\",\"data\":[{\"start\":0,\"stop\":1439,\"value\":\"UA8AUBQAUAwAUBoAUAEAYCcAUBkAUB4AUBgAUCAAUAEAUBkAUAwAYAsAYB8AYB0AYBgAYCoAYBgAYB4AUCcAUBsAUB8AUBwAUBIAYBkAYB8AUBoAUBMAUCEAUCIAYBYAUBwAUCAAUBgAUCAAUBcAYBsAYCUAATIPYD0KECQAYDMAYB0AYAsAYCAAYDwAYCIAYB0AYBcAYCQAYB0AYBAAYCMAYAoAYCIAYCEAYCYAYBsAYBUAYAYAYCIAYCMAUB0AUCAAUBYAUCoAUBEAUC8AUB0AUBYAUDMAUDoAUBkAUC0AUBQAUBwAUA0AUBsAUAoAUCEAUBYAUAwAUB4AUAwAUCcAUCYAUCwKYDUAAUUlEC8IYEMAYEgAYDoAYBAAUAMAUBkAWgAAWgAAWgAAWgAAWgAAUAgAWgAAUBAAUAQAUA4AUA8AUAkAUAIAUAYAUAcAUAIAWgAAUAQAUAkAUAEAUBkAUCUAWgAAUAYAUBEAWgAAUBYAWgAAUAYAWgAAWgAAWgAAWgAAUBcAUAcAWgAAUBUAUAoAUAIAWgAAUAQAUAYAUCgAWgAAUAgAWgAAWgAAUAwAWwAAXCMAUBQAWwAAUAIAWgAAWgAAWgAAWgAAWgAAWgAAWgAAWgAAWREAWQIAUAMAWSEAUDoAUDIAUB8AUCEAUC4AXB4AUA4AWgAAUBIAUA8AUBAAUCUAUCIAUAMAUAEAUAsAUAMAUCwAUBYAWgAAWgAAWgAAWgAAWgAAWgAAUAYAWgAAWgAAWgAAUAYAWwAAWgAAUAYAXAQAUAMAUBsAUBcAUCAAWwAAWgAAWgAAWgAAWgAAUBgAUB4AWgAAUAcAUAwAWQIAWQkAUAEAUAIAWgAAUAoAWgAAUAYAUB0AWgAAWgAAUAkAWgAAWSwAUBIAWgAAUC4AWSYAWgAAUAYAUAoAUAkAUAIAUAcAWgAAUAEAUBEAUBgAUBcAWRYAUA0AWSgAUB4AUDQAUBoAXA4AUA8AUBwAUA8AUA4AUA4AWgAAUAIAUCMAWgAAUCwAUBgAUAYAUAAAUAAAUAAAUAAAUAAAUAAAUAAAUAAAUAAAWwAAUAAAcAAAcAAAcAAAcAAAcAAAcAAAcAAAcAAAcAAAcAAAcAAAcAAAcAAAcAAAcAAAcAAAcAAAcAAAcAAAcAAAcAAAcAAAcAAAcAAAcAAAeSEAeQ8AcAAAcAAAcAAAcAAAcAAAcAAAcAAAcAAAcAAAcAAAcAAAcAAAcBcAcAAAcAAAcCYOcBUAUAAAUAAAUAAAUAAAUAUAUAAAcAAAcAAAcAAAcAAAcAAAcAAAcAAAcAAAcAAAcAAAcAAAcAAAcAAAcAAAcAAAcCgAeQAAcAAAcAAAcAAAcAAAcAAAcAYAcAAAcBgAeQAAcAAAcAAAegAAegAAcAAAcAcAcAAAcAAAcAAAcAAAcAAAcAAAcAAAcAAAcAAAcAAAcAAAcAAAcAAAcAAAcAAAcCkAeQAAcAcAcAAAcAAAcAwAcAAAcAAAcAIAcAAAcAAAcAAAcAAAcAAAcAAAcAAAcAAAcAAAcAAAcAAAcAAAcAAAcAAAcAAAcAAAcAAAcAAAcCIAeQAAcAAAcAAAcAAAcAAAcAAAeRwAeQAAWgAAUAAAUAAAUAAAUAAAUAAAcAAAcAAAcBoAeScAeQAAegAAcBkAeQAAUAAAUAAAUAAAUAAAUAAAUAAAcAAAcAAAcAAAcAAAcAAAcAAAegAAegAAcAAAcAAAcBgAeQAAcAAAcAAAcAAAcAAAcAAAcAkAegAAegAAcAcAcAAAcAcAcAAAcAAAcAAAcAAAcA8AeQAAcAAAcAAAeRQAcAwAUAAAUAAAUAAAUAAAUAAAUAAAcAAAcBEAcA0AcAAAWQsAUAAAUAAAUAAAUAAAUAAAcAAAcAoAcAAAcAAAcAAAcAAAcAAAcAAAcAAAcAYAcAAAcAAAcAAAcAAAcAAAcAAAcAAAcAAAcBYAegAAcAAAcAAAegAAcAcAcAAAcAAAcAAAcAAAcAAAeRkAegAAegAAcAAAcAAAcAAAcAAAcAAAcAAAcAAAcAEAcAAAcAAAcAAAcAUAcAQAcAAAcBIAeQAAcAAAcAAAcAAAcAAAcAAAcAAAcAAAcAAAcAAAcAAAcAAAcAAAcBsAcAAAcAAAcBcAeQAAUAAAUAAAUAAAUAAAUAAAUBQAcBYAUAAAUAAAUAoAWRYAWTQAWQAAUAAAUAAAUAAAcAAAcAAAcAAAcAAAcAAAcAMAcAAAcAQAcAAAcAAAcAAAcDMAeSIAcAAAcAAAcAAAcAAAcAAAcAAAcAAAcAAAcAAAcAAAcAAAcAAAcAAAcAAAcAAAcAAAcAAAcAAAcAAAcBQAeQwAcAAAcAAAcAAAcAMAcAAAeSoAcA8AcDMAcAYAeQoAcAwAcFQAcEMAeVIAaTYAbBcNYAsAYBIAYAIAYAIAYBUAYCwAYBMAYDYAYCkAYDcAUCoAUCcAUAUAUBAAWgAAYBoAYBcAYCgAUAMAUAYAUBYAUA4AUBgAUAgAUAgAUAsAUAsAUA4AUAMAUAYAUAQAUBIAASsSUDAAUDAAUBAAYAYAUBAAUAUAUCAAUBoAUCAAUBAAUAoAYAIAUAQAUAgAUCcAUAsAUCIAUCUAUAoAUA4AUB8AUBkAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAAfgAA\",\"tz\":32,\"did\":\"DA932FFFFE8816E7\",\"src\":24}],\"summary\":\"{\\\"v\\\":6,\\\"slp\\\":{\\\"st\\\":1628296479,\\\"ed\\\":1628296479,\\\"dp\\\":0,\\\"lt\\\":0,\\\"wk\\\":0,\\\"usrSt\\\":-1440,\\\"usrEd\\\":-1440,\\\"wc\\\":0,\\\"is\\\":0,\\\"lb\\\":0,\\\"to\\\":0,\\\"dt\\\":0,\\\"rhr\\\":0,\\\"ss\\\":0},\\\"stp\\\":{\\\"ttl\\\":"+step+",\\\"dis\\\":10627,\\\"cal\\\":510,\\\"wk\\\":41,\\\"rn\\\":50,\\\"runDist\\\":7654,\\\"runCal\\\":397,\\\"stage\\\":[{\\\"start\\\":327,\\\"stop\\\":341,\\\"mode\\\":1,\\\"dis\\\":481,\\\"cal\\\":13,\\\"step\\\":680},{\\\"start\\\":342,\\\"stop\\\":367,\\\"mode\\\":3,\\\"dis\\\":2295,\\\"cal\\\":95,\\\"step\\\":2874},{\\\"start\\\":368,\\\"stop\\\":377,\\\"mode\\\":4,\\\"dis\\\":1592,\\\"cal\\\":88,\\\"step\\\":1664},{\\\"start\\\":378,\\\"stop\\\":386,\\\"mode\\\":3,\\\"dis\\\":1072,\\\"cal\\\":51,\\\"step\\\":1245},{\\\"start\\\":387,\\\"stop\\\":393,\\\"mode\\\":4,\\\"dis\\\":1036,\\\"cal\\\":57,\\\"step\\\":1124},{\\\"start\\\":394,\\\"stop\\\":398,\\\"mode\\\":3,\\\"dis\\\":488,\\\"cal\\\":19,\\\"step\\\":607},{\\\"start\\\":399,\\\"stop\\\":414,\\\"mode\\\":4,\\\"dis\\\":2220,\\\"cal\\\":120,\\\"step\\\":2371},{\\\"start\\\":415,\\\"stop\\\":427,\\\"mode\\\":3,\\\"dis\\\":1268,\\\"cal\\\":59,\\\"step\\\":1489},{\\\"start\\\":428,\\\"stop\\\":433,\\\"mode\\\":1,\\\"dis\\\":152,\\\"cal\\\":4,\\\"step\\\":238},{\\\"start\\\":434,\\\"stop\\\":444,\\\"mode\\\":3,\\\"dis\\\":2295,\\\"cal\\\":95,\\\"step\\\":2874},{\\\"start\\\":445,\\\"stop\\\":455,\\\"mode\\\":4,\\\"dis\\\":1592,\\\"cal\\\":88,\\\"step\\\":1664},{\\\"start\\\":456,\\\"stop\\\":466,\\\"mode\\\":3,\\\"dis\\\":1072,\\\"cal\\\":51,\\\"step\\\":1245},{\\\"start\\\":467,\\\"stop\\\":477,\\\"mode\\\":4,\\\"dis\\\":1036,\\\"cal\\\":57,\\\"step\\\":1124},{\\\"start\\\":478,\\\"stop\\\":488,\\\"mode\\\":3,\\\"dis\\\":488,\\\"cal\\\":19,\\\"step\\\":607},{\\\"start\\\":489,\\\"stop\\\":499,\\\"mode\\\":4,\\\"dis\\\":2220,\\\"cal\\\":120,\\\"step\\\":2371},{\\\"start\\\":500,\\\"stop\\\":511,\\\"mode\\\":3,\\\"dis\\\":1268,\\\"cal\\\":59,\\\"step\\\":1489},{\\\"start\\\":512,\\\"stop\\\":522,\\\"mode\\\":1,\\\"dis\\\":152,\\\"cal\\\":4,\\\"step\\\":238}]},\\\"goal\\\":8000,\\\"tz\\\":\\\"28800\\\"}\",\"source\":24,\"type\":0}]";
        HashMap<String, Object> dataUpdate = new HashMap<>();
        dataUpdate.put("data_json", jsonData);
        dataUpdate.put("userid", userId);
        dataUpdate.put("device_type", "0");
        dataUpdate.put("last_sync_data_time", "1597306380");
        dataUpdate.put("last_deviceid", "DA932FFFFE8816E7");
        HashMap<String, String> headers = new HashMap<>();
        headers.put("User-Agent","MiFit/4.6.0 (iPhone; iOS 14.0.1; Scale/2.00)");
        //headers.put("User-Agent", "Mozilla/5.0 (iPhone; CPU iPhone OS 13_4_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Mobile/15E148 MicroMessenger/7.0.12(0x17000c2d) NetType/WIFI Language/zh_CN");
        headers.put("apptoken", appToken);
        headers.put("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
        HttpRequest post = HttpRequest.post("https://api-mifit-cn.huami.com/v1/data/band_data.json?&t=" + System.currentTimeMillis());
        post.addHeaders(headers);
        post.form(dataUpdate);
        String body = post.execute().body();
    }
}
