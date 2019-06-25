package dao;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import service.MsgService;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WxDao {

    public SqlSessionFactory getSqlSessionFactory() throws IOException{
        String resource = "mybatis-config.xml";
        InputStream inputStream = Resources.getResourceAsStream(resource);
        return new SqlSessionFactoryBuilder().build(inputStream);
    }

    //插入报警数据
    public boolean insertUrgentMessage(Map<String,Object> msg) throws IOException {
        SqlSessionFactory sqlSessionFactory=getSqlSessionFactory();
        SqlSession openSession=sqlSessionFactory.openSession();
        try{
            UserMapper userMapper=openSession.getMapper(UserMapper.class);
            boolean bol=userMapper.insertUrgentMessage(msg);
            openSession.commit();
            return bol;
        }finally {
            openSession.close();
        }
    }
    //获取消息列表
    public JSONArray searchUrgentMessage() throws IOException {
        SqlSessionFactory sqlSessionFactory=getSqlSessionFactory();
        SqlSession openSession=sqlSessionFactory.openSession();
        try{
            UserMapper userMapper=openSession.getMapper(UserMapper.class);
            List<Map<String,Object>> list=userMapper.searchUrgentMessage();
            JSONArray jsonarr= JSONArray.fromObject(list);
            return jsonarr;
        }finally {
            openSession.close();
        }
    }

    //通过id获取详细数据
    public JSONObject searchUrgentMessageById(int id) throws IOException {
        SqlSessionFactory sqlSessionFactory=getSqlSessionFactory();
        SqlSession openSession=sqlSessionFactory.openSession();
        try{
            UserMapper userMapper=openSession.getMapper(UserMapper.class);
            Map<String,Object> list=userMapper.getUrgentMsgById(id);
            JSONObject json = JSONObject.fromObject(list);
            return json;
        }finally {
            openSession.close();
        }
    }

    //获取热力图数据
    public JSONArray gethotdata() throws IOException {
        SqlSessionFactory sqlSessionFactory=getSqlSessionFactory();
        SqlSession openSession=sqlSessionFactory.openSession();
        try{
            UserMapper userMapper=openSession.getMapper(UserMapper.class);
            List<Map<String,Object>> list=userMapper.searchHotData();
            JSONArray jsonarr= JSONArray.fromObject(list);
            return jsonarr;
        }finally {
            openSession.close();
        }
    }

/*    public static void main(String[] args) throws IOException {
        WxDao da=new WxDao();
        JSONArray shuju=da.gethotdata();
        System.out.println(shuju);
    }*/

}
