package cz.trinera.dkt.utils;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;

public class MyApacheHttpClientProvider {

    private static final boolean USE_POOLING = false;

    //TODO: reenable
    private static PoolingHttpClientConnectionManager connManager;

    static {
        if (USE_POOLING) {
            connManager = new PoolingHttpClientConnectionManager();
            connManager.setMaxTotal(128);
            connManager.setDefaultMaxPerRoute(16);
        }
    }

    public static CloseableHttpClient getClient() {
        if (USE_POOLING) {
            return HttpClients.custom()
                    .setConnectionManager(connManager)
                    .build();
        } else {
            return HttpClients.createDefault();
        }
    }

    public static void cleanup() {
        if (USE_POOLING) {
            /*connManager.closeExpired();
            connManager.closeIdle(TimeValue.ofMinutes(5));*/
        }
    }
}
