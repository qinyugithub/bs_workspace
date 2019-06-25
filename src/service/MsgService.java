package service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import dao.WxDao;
import net.sf.json.JSONArray;
import org.apache.log4j.Logger;
import util.WxUtil;

import javax.websocket.Session;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MsgService {
    private static Logger LOG = Logger.getLogger(MsgService.class);

    //ͨ��websocket��PC�������û���Ϣ
    public void sendMsgToPc(Map<String, Object> map) throws IOException {
        Session session = WxUtil.WS_SESSION;
        if (session != null) {
            String openid = map.get("FromUserName").toString();
            JSONObject permsg = getInformationById(openid);
            permsg.put("content", map.get("Content").toString());
            permsg.put("messagetype", "message");
            session.getBasicRemote().sendText(permsg.toString());
        }

    }

    //ͨ��websocket��PC�������û�λ��
    public void sendLocationToPc(Map<String, Object> map) throws IOException {
        Session session = WxUtil.WS_SESSION;
        if (session != null) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("latitude", map.get("Latitude").toString());
            jsonObject.put("longitude", map.get("Longitude").toString());
            jsonObject.put("precision", map.get("Precision").toString());//λ�þ���
            jsonObject.put("messagetype", "location");
            session.getBasicRemote().sendText(jsonObject.toString());
        }

    }

    //ͨ��΢�ŷ�����������openid��ȡ�û�������Ϣ
    public JSONObject getInformationById(String openid) {
        String url = WxUtil.getPersonInformationByIdUrl();
        Map<String, String> params = new HashMap<String, String>();
        params.put("access_token", WxUtil.ACCESS_TOKEN);
        params.put("openid", openid);
        params.put("lang", "zh_CN");
        JSONObject jo = WxUtil.getToWx(url, params);
        if (jo.get("openid") != null) {
            LOG.info("ͨ��openid��ȡ�û���Ϣ�ɹ���" + jo);
            return jo;
        } else {
            LOG.error("ͨ��openid��ȡ�û���Ϣʧ�ܣ�" + jo);
            return null;
        }
    }

    //���û�������Ϣ
    public JSONObject postToUser(String openid, String content) {
        String url = WxUtil.postMessageUrl();
        // String url="https://api.weixin.qq.com/cgi-bin/message/custom/send?access_token=18_9q4krlf3ER9XPD_YFMU9L8-7N8DB9L1ZYNTvKChFEbb_39hcK_-Sc2xs1KLEtVYJ9PxIM2xxMRo3AN4s0WyP2cWg7MYKOuWmR-6xXohxs5Xpmh1XMN_ijF8QM4UzT9gmYv3bytQ_0vPM5gWsHWCbAEATIO";
        Map<String, Object> params = new HashMap<String, Object>();
        Map<String, Object> params2 = new HashMap<String, Object>();
        params2.put("content", content);

        params.put("touser", openid);
        params.put("msgtype", "text");
        params.put("text", params2);

        String pams = JSON.toJSONString(params, SerializerFeature.DisableCircularReferenceDetect);
        JSONObject ret = WxUtil.postToWx(url, pams);
        if((Integer)ret.get("errcode")==0){
            LOG.info("��Ϣ�ظ��ɹ�������" + ret.toString());
            return ret;
        }else{
            LOG.error("��Ϣ�ظ���������" + ret.toString());
        }
        return null;
    }

    //ͨ�����汨����Ϣ
    public JSONObject saveUrgentMsg(Map<String, Object> msg) throws IOException {
        WxDao da=new WxDao();
        boolean bo=da.insertUrgentMessage(msg);
        //����ģ����Ϣ
        sendTemplateMsg(msg);
        //���������ݼ�ʱ���͵��ͻ���
        Session session = WxUtil.WS_SESSION;
        if (session != null) {
            JSONObject permsg = new JSONObject();
            permsg.put("messagetype", "newUrgentMsg");
            session.getBasicRemote().sendText(permsg.toString());
        }
        JSONObject ret=new JSONObject();
        ret.put("msg",bo);
        return ret;
    }

    //��ȡδ����ľ�����Ϣ
    public JSONObject searchUrgentMsg() throws IOException {
        WxDao da=new WxDao();
        List<Map<String,Object>> list=da.searchUrgentMessage();
        for(int i=0;i<list.size();i++){
            Map<String,Object> user=new HashMap<>();
            user.put("id",list.get(i).get("openid"));
            user.put("avatar",list.get(i).get("headimgurl"));
            user.put("username",list.get(i).get("nickname"));
            user.put("sign","");
            list.get(i).put("user",user);
            list.get(i).put("from",166488);
        }
        JSONArray jsonarr= JSONArray.fromObject(list);
        JSONObject ret=new JSONObject();
        ret.put("code",0);
        ret.put("pages",1);
        ret.put("data",jsonarr);
        return ret;
    }

    //����id��ȡ��ϸ����
    public net.sf.json.JSONObject searchUrgentMsgById(int id) throws IOException {
        WxDao da=new WxDao();
        net.sf.json.JSONObject data=da.searchUrgentMessageById(id);
        if(data.size()!=0){
            WxUtil.CURRENT_USER_OPENID=data.get("openid").toString();
        }
        return data;
    }

    //��ȡ����ͼ����
    public JSONObject searchHotData() throws IOException {
        WxDao da=new WxDao();
        List<Map<String,Object>> list=da.gethotdata();
        JSONArray jsonarr= JSONArray.fromObject(list);
        JSONObject ret=new JSONObject();
        ret.put("data",jsonarr);
        ret.put("max",100);
        ret.put("min",0);
        return ret;
    }


    public void sendTemplateMsg(Map<String, Object> msg){
        String resp = null;
        JSONObject obj = new JSONObject();
        System.out.println(msg);
        System.out.println(msg.get("personinformation").toString());
        JSONObject jsStr = JSONObject.parseObject(msg.get("personinformation").toString());
        System.out.println(jsStr.get("city"));
//      ���ò�ѯģ�壨ʹ�õ�Ԥ���ӿڣ�
        JSONObject obj_1 = new JSONObject();
        obj_1.put("value", "���΢���˺š�"+jsStr.get("nickname")+"�������˽�����������");

        JSONObject obj_2 = new JSONObject();
        obj_2.put("value", jsStr.get("nickname"));
//		obj_2.put("color", "#173177");

        JSONObject obj_3 = new JSONObject();
        obj_3.put("value", jsStr.get("openid"));

        JSONObject obj_4 = new JSONObject();
        obj_4.put("value", "��"+msg.get("longitude").toString()+"��"+msg.get("latitude").toString()+"��");

        JSONObject obj_5 = new JSONObject();
        obj_5.put("value", msg.get("message").toString());

        JSONObject obj_6 = new JSONObject();
        if(msg.get("addresstype").toString().equals("0")){
            obj_6.put("value", "�̶�λ��");
        }else{
            obj_6.put("value", "ʵʱ׷��");
        }

        JSONObject obj_7 = new JSONObject();
        Date now=new Date();
        SimpleDateFormat sf = new SimpleDateFormat("yyyy��MM��dd�� HH:mm:ss");
        obj_7.put("value", sf.format(now));

        JSONObject obj_8 = new JSONObject();
        obj_8.put("value", "��ͨ���·������;�����Ա������ϵ");

        JSONObject data = new JSONObject();
        data.put("heard", obj_1);
        data.put("name", obj_2);
        data.put("id", obj_3);
        data.put("address", obj_4);
        data.put("detil", obj_5);
        data.put("addresstype", obj_6);
        data.put("time", obj_7);
        data.put("foot", obj_8);

        obj.put("touser",obj_3.get("value"));
        obj.put("url","https://www.qinyu.online");
        obj.put("template_id","dmjO3ujRlKIlUv-lbLPNpV4gcpt0R_QhjlHipUPC4W0");
        obj.put("data",data);

        String query = obj.toString();
        LOG.info("���͵�ģ����Ϣ����Ϊ"+ query);
        try {
            URL url = new URL("https://api.weixin.qq.com/cgi-bin/message/template/send?access_token="+WxUtil.ACCESS_TOKEN); // url��ַ

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setUseCaches(false);
            connection.setInstanceFollowRedirects(true);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.connect();

            try (OutputStream os = connection.getOutputStream()) {
                os.write(query.getBytes("UTF-8"));
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String lines;
                StringBuffer sbf = new StringBuffer();
                while ((lines = reader.readLine()) != null) {
                    lines = new String(lines.getBytes(), "utf-8");
                    sbf.append(lines);
                }
                LOG.info("ģ����Ϣ�������ı��ģ�" + sbf.toString());
                resp = sbf.toString();

            }
            connection.disconnect();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // JSONObject json = (JSONObject)JSON.parse(resp);
        }
    }

/*    public static void main(String[] args) throws IOException {
        MsgService da=new MsgService();
        JSONObject list=da.searchHotData();
        System.out.println(list);
    }*/

}
