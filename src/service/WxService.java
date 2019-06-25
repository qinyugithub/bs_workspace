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

    //��ʱ��ȡȫ��token��ticket������ȫ��
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
            LOG.info("access_token ��ȡ�ɹ���" + jo);
            WxUtil.ACCESS_TOKEN = jo.get("access_token").toString();
            getticket();
        } else {
            LOG.error("access_token ��ȡʧ�ܣ�������Ϣ���£�" + jo);
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
            LOG.info("ticket ��ȡ�ɹ���" + jo);
            WxUtil.TIKECT = jo.get("ticket").toString();
        } else {
            LOG.error("ticket ��ȡʧ�ܣ�������Ϣ���£�" + jo);
        }
    }

    //����code��ȡ��ҳ��Ȩtoken��userid��������token��ȡ�û�����
    public JSONObject getWebToken(String code) {
        LOG.debug(new Date() + "================�û���ҳ��Ȩ��ʼ=============");
        String url = WxUtil.getWebTokenUrl();
        Map<String, String> params = new HashMap<String, String>();
        params.put("appid", WxUtil.APP_ID);
        params.put("secret", WxUtil.APP_SECRET);
        params.put("code", code);
        params.put("grant_type", "authorization_code");
        JSONObject jo = WxUtil.getToWx(url, params);
        if (jo.get("access_token") != null) {
            LOG.info("��ҳ��Ȩ�ɹ���" + jo);
            //ͨ��webtoken��ȡ�û���Ϣ
            JSONObject personformation = getPersonInformatioin(jo.get("access_token").toString(), jo.get("openid").toString());
            //personformation.put("access_token", jo.get("access_token"));
            LOG.info("���շ��ص��û����ݣ�" + personformation);
            return personformation;
        } else {
            LOG.error("��ҳ��Ȩʧ�ܣ�" + jo);
            return null;
        }
    }

    public JSONObject getPersonInformatioin(String webtoken, String openid) {
        LOG.debug(new Date() + "================��ʼ��ȡ�û���Ϣ��" + webtoken + "��(" + openid + ")=============");
        String url = WxUtil.getPersonInformationUrl();
        Map<String, String> params = new HashMap<String, String>();
        params.put("access_token", webtoken);
        params.put("openid", openid);
        params.put("lang", "zh_CN");
        JSONObject jo = WxUtil.getToWx(url, params);
        if (jo.get("openid") != null) {
            LOG.info("��ȡ�û���Ϣ�ɹ���" + jo);
            return jo;
        } else {
            LOG.error("��ȡ�û���Ϣʧ�ܣ�" + jo);
            return null;
        }
    }

    public JSONObject getsignature(String url) {
        String timestamp = Long.toString(System.currentTimeMillis() / 1000);
        String nonceStr = UUID.randomUUID().toString();

        String[] paramArr = new String[]{"jsapi_ticket=" + WxUtil.TIKECT,
                "timestamp=" + timestamp, "noncestr=" + nonceStr, "url=" + url};
        Arrays.sort(paramArr);
        // �������Ľ��ƴ�ӳ�һ���ַ���
        String content = paramArr[0].concat("&" + paramArr[1]).concat("&" + paramArr[2])
                .concat("&" + paramArr[3]);
        LOG.info("׼������ǩ�����������£�" + content);
        String gensignature = null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            // ��ƴ�Ӻ���ַ������� sha1 ����
            byte[] digest = md.digest(content.toString().getBytes());
            gensignature = byteToStr(digest);
        } catch (NoSuchAlgorithmException e) {
            LOG.error("sha1 ���ܳ�������");
            e.printStackTrace();
        }

        JSONObject jo = new JSONObject();
        jo.put("app_id", WxUtil.APP_ID);
        jo.put("timestamp", timestamp);
        jo.put("nonceStr", nonceStr);
        jo.put("signature", gensignature);
        LOG.info("ǩ�������ɣ����ؽ�����£�" + jo);
        return jo;
    }

    // ���ֽ�����ת��Ϊʮ�������ַ���
    private static String byteToStr(byte[] byteArray) {
        String strDigest = "";
        for (int i = 0; i < byteArray.length; i++) {
            strDigest += byteToHexStr(byteArray[i]);
        }
        return strDigest;
    }

    //���ֽ�ת��Ϊʮ�������ַ���
    private static String byteToHexStr(byte mByte) {
        char[] Digit = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A',
                'B', 'C', 'D', 'E', 'F'};
        char[] tempArr = new char[2];
        tempArr[0] = Digit[(mByte >>> 4) & 0X0F];
        tempArr[1] = Digit[mByte & 0X0F];

        String s = new String(tempArr);
        return s;
    }

    //��΢�ŷ��������͵�xmlתΪmap
    public Map<String, Object> requestToMap(HttpServletRequest request){
        InputStream inStream;
        try {
            //��ȡ΢�ŷ��������͵�xml��
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
            LOG.info("�ѻ�ȡ����תΪ��xml�ַ�����");

            Map<String, Object> map = null;
            try {
                map = XmlUtil.xmlToMap(result);
                LOG.info("�ѽ�xml�ַ����ɹ�תΪmap��" + map);
                return map;
            } catch (Exception e) {
                LOG.error("xml�ַ���תΪmapʱ��������");
                e.printStackTrace();
            }
        }catch (Exception e) {
            LOG.error("��ȡ��תΪmapʱ��������");
        }
        return null;
    }

}
