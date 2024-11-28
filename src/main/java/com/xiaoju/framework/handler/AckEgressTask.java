package com.xiaoju.framework.handler;

import com.corundumstudio.socketio.SocketIOClient;


public class AckEgressTask extends EgressTask{

    SocketIOClient client;

    public AckEgressTask(String name, PushMessage egressMsg, SocketIOClient client) {
        super(name, egressMsg);
        this.client = client;
    }

    @Override
    public void run() {
        //LOGGER.info("client sendEvent 执行了");
        client.sendEvent(name, egressMsg);
    }
}
