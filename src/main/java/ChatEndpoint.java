import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by ak on 01/05/17.
 */

@ServerEndpoint(value = "websocket/chat")
public class ChatEndpoint {

    static Session session;
    static List<ChatEndpoint> clients = new CopyOnWriteArrayList<ChatEndpoint>();

    @OnOpen
    public void onOpen(Session session, EndpointConfig endpointConfig) {
        this.session = session;
        clients.add(this);
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        System.out.println("socket closed because of " + reason.toString());
        clients.remove(this);
    }

    @OnMessage
    public void onMessage(String msg) {
        broadcast(msg);

    }

    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    //handle the binary data
    @OnMessage
    public void onMessage(ByteBuffer byteBuffer, Boolean complete){
        try {
            buffer.write(byteBuffer.array());
            if(complete) {
                //just write the image to the server hard disc
                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream("/Volumes/Macintosh HD/Users/isaeff/temp");
                    fos.write(buffer.toByteArray());
                } finally {
                    if (fos!=null){
                        fos.close();
                        fos.flush();
                    }
                }
                for (ChatEndpoint client : clients) {
                    try {
                        final ByteBuffer sendData = ByteBuffer.allocate(buffer.toByteArray().length);
                        sendData.put(buffer.toByteArray());
                        client.session.getAsyncRemote().sendBinary(sendData, new SendHandler() {
                            public void onResult(SendResult sendResult) {
                                System.out.println(sendResult.isOK());
                            }
                        });
                        client.session.getBasicRemote().sendText("call :complete");

                    } catch (IOException e) {
                        clients.remove(this);
                        try {
                            client.session.close();
                        } catch (IOException e1) {
                            //do nothing
                        }
                    }
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void broadcast(String msg) {
        for (ChatEndpoint client : clients) {
            try {
                client.session.getBasicRemote().sendText(msg);

            } catch (IOException e) {
                clients.remove(this);
                try {
                    client.session.close();
                } catch (IOException e1) {
                    //do nothing
                }
            }
        }

    }
}



