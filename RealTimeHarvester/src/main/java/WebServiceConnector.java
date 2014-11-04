import com.google.gson.JsonObject;
import org.glassfish.tyrus.client.ClientManager;

import javax.websocket.ClientEndpoint;
import java.util.logging.Logger;

import org.glassfish.tyrus.client.ClientManager;

import javax.websocket.*;
import java.net.URI;
/**
 * @author Yikai Gong
 */

@ClientEndpoint
public class WebServiceConnector {
    private ClientManager client = ClientManager.createClient();
    private Logger logger = Logger.getLogger(this.getClass().getName());
    private Session session = null;

    @OnOpen
    public void onOpen(Session session) {
        logger.info("Connected ... " + session.getId());
        this.session = session;
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        logger.info("Got message: "+message);
    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        this.session = null;
        logger.info(String.format("Session %s close because of %s", session.getId(), closeReason));
    }

    public void connectToServer(String urlStr) throws Exception {
        client.connectToServer(this, new URI(urlStr));
    }

    public void send (JsonObject json) throws Exception{
        if (this.session == null){
            throw new Exception("No session");
        }
        this.session.getBasicRemote().sendText(json.toString());
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public Session getSession() {
        return session;
    }
}
