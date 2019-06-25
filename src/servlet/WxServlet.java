package servlet;

import com.alibaba.fastjson.JSONObject;
import org.apache.log4j.Logger;
import org.json.JSONException;
import service.MsgService;
import service.WxService;
import util.UploadFileUtil;
import util.WxUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.websocket.Session;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@WebServlet("/WxServlet")
public class WxServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static Logger LOG = Logger.getLogger(WxServlet.class);

    public WxServlet() {
        super();
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String servletPath = request.getServletPath();
        String methodName = servletPath.substring(1);
        methodName = methodName.substring(0, methodName.length() - 3);
        try {
            Method method = getClass().getDeclaredMethod(methodName,
                    new Class[]{HttpServletRequest.class, HttpServletResponse.class});
            method.invoke(this, new Object[]{request, response});
        } catch (Exception e) {
            System.out.println("页面不存在");
        }
    }

    public void delete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        //System.out.println("方法为delete");
        Session session = WxUtil.WS_SESSION;
        if (session != null) {
            session.getBasicRemote().sendText("666666666");
        }

    }

    //微信服务器消息推送接口
    public void test(HttpServletRequest request, HttpServletResponse response) throws IOException {
        WxService wxservice = new WxService();
        Map<String, Object> map = wxservice.requestToMap(request);
        try {
            if (map.containsKey("Event")) {
                LOG.info("事件推送" + map);
                if (map.get("Event").toString().equals("LOCATION")) {
                    LOG.info("位置事件推送");
                    if(WxUtil.WS_SESSION!=null && map.get("FromUserName").toString().equals(WxUtil.CURRENT_USER_OPENID)){
                        LOG.info("准备向客户端推送位置消息");
                        MsgService msgService = new MsgService();
                        msgService.sendLocationToPc(map);
                    }
                }
            } else if (map.containsKey("MsgType")) {
                LOG.info("消息推送" + map);
                if (map.get("MsgType").toString().equals("text")) {
                    LOG.info("文本消息推送");
                    MsgService msgService = new MsgService();
                    msgService.sendMsgToPc(map);
                }
            } else {
                LOG.info("其他消息" + map);
            }
        } catch (Exception e) {
            LOG.error("消息解析出错");
        }

        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        out.println("success");
    }

    //根据code获取网页授权token和userid
    public void gettokenAndUserid(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String code = request.getParameter("code");
        WxService wxservice = new WxService();
        JSONObject jo = wxservice.getWebToken(code);

        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        out.println(jo);
    }

    //通过当前URL获取签名
    public void getSignature(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String url = request.getParameter("url");
        WxService wxservice = new WxService();
        JSONObject signature = wxservice.getsignature(url);

        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        out.println(signature);
    }

    //向用户发送消息
    public void postMessageToUser(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String openid = request.getParameter("openid");
        String content = request.getParameter("content");
        MsgService msgService = new MsgService();
        JSONObject jo = msgService.postToUser(openid, content);

        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        out.println(jo);
    }
   //照片上传
    public void uploadfile(HttpServletRequest request, HttpServletResponse response) throws IOException, JSONException {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String fordername=sdf.format(date);

        String realPath = getServletContext().getRealPath("/") + "images/"+fordername;

        UploadFileUtil uploadfile=new UploadFileUtil();
        org.json.JSONObject obj=uploadfile.uploadfile(request,response,realPath);

        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        System.out.println(obj.toString());
        out.println(obj);
    }

    //获取报警消息写入数据库
    public void urgent(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String user = request.getParameter("user");
        JSONObject userobj = JSONObject.parseObject(user);
        Map<String,Object> m = userobj;

        MsgService msgService = new MsgService();
        JSONObject ret = null;
        try{
            ret=msgService.saveUrgentMsg(m);
        }catch (Exception e){
            System.out.println(e);
        }
        System.out.println(ret);
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        out.println(ret);
    }
    //获取警务消息列表数据
    public void getUrgentMsg(HttpServletRequest request, HttpServletResponse response) throws IOException {
        MsgService da=new MsgService();
        JSONObject list=da.searchUrgentMsg();
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        out.println(list);
    }

    //更具id获取详细数据
    public void getUrgentMsgById(HttpServletRequest request, HttpServletResponse response) throws IOException {
        int id = Integer.parseInt(request.getParameter("id"));
        MsgService da=new MsgService();
        net.sf.json.JSONObject list=da.searchUrgentMsgById(id);
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        out.println(list);
    }

    //获取热力图数据
    public void getHotData(HttpServletRequest request, HttpServletResponse response) throws IOException {
        MsgService da=new MsgService();
        JSONObject list=da.searchHotData();
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        out.println(list);
    }

    //登录用户验证
    public void checkuser(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        //使用request对象的getSession()获取session，如果session不存在则创建一个
        HttpSession session = request.getSession();
        if(username.equals("root")&&password.equals("996")){
            //将数据存储到session中
            session.setAttribute("cheaked", true);
        }else{
            session.setAttribute("cheaked", false);
        }
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        out.println(session.getAttribute("cheaked"));
    }

}
