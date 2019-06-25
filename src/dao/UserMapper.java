package dao;


import java.util.List;
import java.util.Map;

public interface UserMapper {

    public boolean insertUrgentMessage(Map<String,Object> msg);

    public List<Map<String,Object>> searchUrgentMessage();

    public Map<String,Object> getUrgentMsgById(int id);

    public List<Map<String,Object>> searchHotData();
}
