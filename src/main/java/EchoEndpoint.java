
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by ak on 01/05/17.
 */
@ServerEndpoint(value = "websocket/echoa")
public class EchoEndpoint {

    @OnOpen
    public void onOpen(Session session, EndpointConfig endpointConfig) {
        RemoteEndpoint.Basic remoteEndpointBasic = session.getBasicRemote();
        session.addMessageHandler(new EchoMessageHandler(remoteEndpointBasic));

    }

    private static class EchoMessageHandler implements MessageHandler.Whole<String>{

        private final RemoteEndpoint.Basic remoteEndpointBasic;

        private EchoMessageHandler(RemoteEndpoint.Basic remoteEndpointBasic){
            this.remoteEndpointBasic = remoteEndpointBasic;
        }


        //send the originally received message back to the client
        public void onMessage(String s) {

            if (remoteEndpointBasic!=null) {
                try {
                    remoteEndpointBasic.sendText("annotated: "+s);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }


    }
}
