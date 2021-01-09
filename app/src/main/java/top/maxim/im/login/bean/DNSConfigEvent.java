
package top.maxim.im.login.bean;

/**
 * Description : DNS配置 Created by Mango on 2020/7/2.
 */
public class DNSConfigEvent {

    private String appId;

    private String server;

    private int port;

    private String restServer;

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getRestServer() {
        return restServer;
    }

    public void setRestServer(String restServer) {
        this.restServer = restServer;
    }
}
