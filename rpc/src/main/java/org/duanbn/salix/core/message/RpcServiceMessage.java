package org.duanbn.salix.core.message;

import java.util.List;

public class RpcServiceMessage extends Message {

    private List<String> rpcServiceNames;

    public void setRpcServiceNames(List<String> rpcServiceNames) {
        this.rpcServiceNames = rpcServiceNames;
    }

    public List<String> getRpcServiceNames() {
        return this.rpcServiceNames;
    }

}
