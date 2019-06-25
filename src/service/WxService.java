package service;

import com.alibaba.fastjson.JSONObject;
import listenerproject.InitFilter;
import org.apache.log4j.Logger;
import util.WxUtil;
import util.XmlUtil;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class WxService {
    private static Logger LOG = Logger.getLogger(InitFilter.class);

    //定时获取全局token和ticket保存在全局
    public void doontime() {
        Runnable runnable = new Runnable() {
            public void run() {
                gettoken();
            }
        };
        ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
        service.scheduleAtFixedRate(runnable, 0, 1, TimeUnit.HOURS);
    }

    public void gettoken() {
        LOG.debug(new Date() + "================get access_token=============");
        String url = WxUtil.getTokenUrl();
        Map<String, String> params = new HashMap<String, String>();
        params.put("grant_type", "client_credential");
        params.put("appid", WxUtil.APP_ID);
        params.put("secret", WxUtil.APP_SECRET);
        JSONObject jo = WxUtil.getToWx(url, params);
        if (jo.get("access_token") != null) {
            LOG.info("access_token 获取成功：" + jo);
            WxUtil.ACCESS_TOKEN = jo.get("access_token").toString();
            getticket();
        } else {
            LOG.error("access_token 获取失败，错误信息如下：" + jo);
        }
    }

    public void getticket() {
        LOG.debug(new Date() + "================get ticket=============");
        String url = WxUtil.getTicketUrl();
        Map<String, String> params = new HashMap<String, String>();
        params.put("access_token", WxUtil.ACCESS_TOKEN);
        params.put("type", "jsapi");
        JSONObject jo = WxUtil.getToWx(url, params);
        if (Integer.parseInt(jo.get("errcode").toString()) == 0) {
            LOG.info("ticket 获取成功：" + jo);
            WxUtil.TIKECT = jo.get("ticket").toString();
        } else {
            LOG.error("ticket 获取失败，错误信息如下：" + jo);
        }
    }

    //根据code获取网页授权token和userid，并更具token获取用户数据
    public JSONObject getWebToken(String code) {
        LOG.debug(new Date() + "================用户网页授权开始=============");
        String url = WxUtil.getWebTokenUrl();
        Map<String, String> params = new HashMap<String, String>();
        params.put("appid", WxUtil.APP_ID);
        params.put("secret", WxUtil.APP_SECRET);
        params.put("code", code);
        params.put("grant_type", "authorization_code");
        JSONObject jo = WxUtil.getToWx(url, params);
        if (jo.get("access_token") != null) {
            LOG.info("网页授权成功：" + jo);
            //通过webtoken获取用户信息
            JSONObject personformation = getPersonInformatioin(jo.get("access_token").toString(), jo.get("openid").toString());
            //personformation.put("access_token", jo.get("access_token"));
            LOG.info("最终返回的用户数据：" + personformation);
            return personformation;
        } else {
            LOG.error("网页授权失败：" + jo);
            return null;
        }
    }

    public JSONObject getPersonInformatioin(String webtoken, String openid) {
        LOG.debug(new Date() + "================开始获取用户信息（" + webtoken + "）(" + openid + ")=============");
        String url = WxUtil.getPersonInformationUrl();
        Map<String, String> params = new HashMap<String, String>();
        params.put("access_token", webtoken);
        params.put("openid", openid);
        params.put("lang", "zh_CN");
        JSONObject jo = WxUtil.getToWx(url, params);
        if (jo.get("openid") != null) {
            LOG.info("获取用户信息成功：" + jo);
            return jo;
        } else {
            LOG.error("获取用户信息失败：" + jo);
            return null;
        }
    }

    public JSONObject getsignature(String url) {
        String timestamp = Long.toString(System.currentTimeMillis() / 1000);
        String nonceStr = UUID.randomUUID().toString();

        String[] paramArr = new String[]{"jsapi_ticket=" + WxUtil.TIKECT,
                "timestamp=" + timestamp, "noncestr=" + nonceStr, "url=" + url};
        Arrays.sort(paramArr);
        // 将排序后的结果拼接成一个字符串
        String content = paramArr[0].concat("&" + paramArr[1]).concat("&" + paramArr[2])
                .concat("&" + paramArr[3]);
        LOG.info("准备生成签名，参数如下：" + content);
        String gensignature = null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            // 对拼接后的字符串进行 sha1 加密
            byte[] digest = md.digest(content.toString().getBytes());
            gensignature = byteToStr(digest);
        } catch (NoSuchAlgorithmException e) {
            LOG.error("sha1 加密出错！！！");
            e.printStackTrace();
        }

        JSONObject jo = new JSONObject();
        jo.put("app_id", WxUtil.APP_ID);
        jo.put("timestamp", timestamp);
        jo.put("nonceStr", nonceStr);
        jo.put("signature", gensignature);
        LOG.info("签名已生成，返回结果如下：" + jo);
        return jo;
    }

    // 将字节数组转换为十六进制字符串
    private static String byteToStr(byte[] byteArray) {
        String strDigest = "";
        for (int i = 0; i < byteArray.length; i++) {
            strDigest += byteToHexStr(byteArray[i]);
        }
        return strDigest;
    }

    //将字节转换为十六进制字符串
    private static String byteToHexStr(byte mByte) {
        char[] Digit = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A',
                'B', 'C', 'D', 'E', 'F'};
        char[] tempArr = new char[2];
        tempArr[0] = Digit[(mByte >>> 4) & 0X0F];
        tempArr[1] = Digit[mByte & 0X0F];

        String s = new String(tempArr);
        return s;
    }

    //将微信服务器推送的xml转为map
    public Map<String, Object> requestToMap(HttpServletRequest request){
        InputStream inStream;
        try {
            //获取微信服务器推送的xml流
            inStream = request.getInputStream();
            ByteArrayOutputStream outSteam = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = inStream.read(buffer)) != -1) {
                outSteam.write(buffer, 0, len);
            }
            outSteam.close();
            inStream.close();
            String result = new String(outSteam.toByteArray(), "utf-8");
            LOG.info("已获取到流转为的xml字符串：");

            Map<String, Object> map = null;
            try {
                map = XmlUtil.xmlToMap(result);
                LOG.info("已将xml字符串成功转为map：" + map);
                return map;
            } catch (Exception e) {
                LOG.error("xml字符串转为map时出错！！！");
                e.printStackTrace();
            }
        }catch (Exception e) {
            LOG.error("获取流转为map时出错！！！");
        }
        return null;
    }

}
