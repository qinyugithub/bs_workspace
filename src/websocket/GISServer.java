package websocket;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

//ws://127.0.0.1:8087/Demo1/ws/����
@ServerEndpoint("/gis/{user}")
public class GISServer {
	private String currentUser;
	private Session currentsession=null;
	
	//���Ӵ�ʱִ��
	@OnOpen
	public void onOpen(@PathParam("user") String user, Session session) {

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
		System.out.println(String.format("Session %s closed because of %s", session.getId(), closeReason));
	}

	//���Ӵ���ʱִ��
	@OnError
	public void onError(Throwable t) {
		t.printStackTrace();
	}
}
