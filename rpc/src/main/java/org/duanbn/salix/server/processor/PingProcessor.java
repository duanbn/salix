package org.duanbn.salix.server.processor;

import org.duanbn.salix.core.message.*;

public class PingProcessor extends AbstractProcessor {

    public Message process(Message in) throws Throwable {

        PingMessage msg = (PingMessage) in;
        msg.setElapse((int)(System.currentTimeMillis() - msg.getTime()));

        return msg;

    }

}
