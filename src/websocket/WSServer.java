package websocket;

import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import org.apache.log4j.Logger;
import util.WxUtil;

import java.util.Date;

@ServerEndpoint("/wss/{user}")
public class WSServer {
	private static Logger LOG = Logger.getLogger(WSServer.class);
	private String currentUser;
	
	//���Ӵ�ʱִ��
	@OnOpen
	public void onOpen(@PathParam("user") String user, Session session) {
		currentUser = user;
		WxUtil.WS_SESSION=session;
		LOG.info( new Date()+"����Ա����");
		//System.out.println("session ... " + session);
		//System.out.println("session.getId() ... " + session.getId());
		//System.out.println("currentUser ... " + currentUser);
	}

	//�յ���Ϣʱִ��
	@OnMessage
	public String onMessage(String message, Session session) {
		System.out.println(currentUser + "��" + message);
		return currentUser + "��" + message;
	}

	//���ӹر�ʱִ��
	@OnClose
	public void onClose(Session session, CloseReason closeReason) {
		WxUtil.WS_SESSION=null;
		WxUtil.CURRENT_USER_OPENID=null;
		LOG.info(new Date()+"����Ա����");
		//System.out.println(String.format("Session %s closed because of %s", session.getId(), closeReason));
	}

	//���Ӵ���ʱִ��
	@OnError
	public void onError(Throwable t) {
		t.printStackTrace();
	}
}
