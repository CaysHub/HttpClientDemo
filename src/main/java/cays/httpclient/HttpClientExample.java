package cays.httpclient;

import com.alibaba.fastjson.JSON;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.cache.CacheResponseStatus;
import org.apache.http.client.cache.HttpCacheContext;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.DateUtils;
import org.apache.http.conn.ManagedHttpClientConnection;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.cache.CacheConfig;
import org.apache.http.impl.client.cache.CachingHttpClients;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpCoreContext;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.*;

/**
 * HttpClient的GET方法
 *
 * @author Chai yansheng
 * @create 2019-08-15 9:08
 **/
public class HttpClientExample {
    // response handler 回调方法处理结果
    private ResponseHandler<String> responseHandler = response -> {
        int status = response.getStatusLine().getStatusCode();
        if (status >= 200 && status < 300) {
            HttpEntity entity = response.getEntity();
            return entity != null ? EntityUtils.toString(entity) : null;
        } else {
            throw new ClientProtocolException("Unexpected response code : " + status);
        }
    };
    /**
     * HttpClient的GET方法
     * @param url GET请求地址
     */
    public void getMethod(String url) {
        CloseableHttpClient httpClient = null;
        try {
            httpClient = HttpClients.createDefault();
            HttpGet httpGet = new HttpGet(url);
            System.out.println("Executing request " + httpGet.getRequestLine());
            // 发起请求并接受结果
            String responseBody = httpClient.execute(httpGet, responseHandler);
            System.out.println("=================================================================");
            System.out.println(responseBody);
            System.out.println("=================================================================");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (httpClient != null)httpClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * HttpClient的POST方法
     * @param url POST的访问路径
     */
    public void postMethod(String url) {
        CloseableHttpClient httpClient = null;
        try {
            httpClient = HttpClients.createDefault();
            HttpPost httpPost = new HttpPost(url);
            List<Map<String, Object>> params = new ArrayList<Map<String, Object>>();
            for (int i = 0; i < 3; i++) {
                Map<String, Object> param = new HashMap<String, Object>();
                param.put("id", "" + i);
                param.put("name", "name" + i);
                param.put("price", 5.12 + i);
                param.put("lastUpdateTime", new Date());
                param.put("message", "4今天，习近平总书记在辽宁忠旺集团考察时强调指出");
                params.add(param);
            }
            String entrtyJson = JSON.toJSONString(params);
            StringEntity strEntity = new StringEntity(entrtyJson, "UTF-8");
            strEntity.setContentEncoding("UTF-8");
            strEntity.setContentType("application/json");
            httpPost.setEntity(strEntity);
            System.out.println("Executing request " + httpPost.getRequestLine());
            // response handler 回调方法处理结果
            // 发起请求并接受结果
            String responseBody = httpClient.execute(httpPost, responseHandler);
            System.out.println("=================================================================");
            System.out.println(responseBody);
            System.out.println("=================================================================");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (httpClient != null)httpClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static final String PEER_CERTIFICATES = "PEER_CERTIFICATES";
    // 拦截器
    private static HttpResponseInterceptor interceptor = (httpResponse, context) -> {
        ManagedHttpClientConnection rutedConnnection = (ManagedHttpClientConnection)
                context.getAttribute(HttpCoreContext.HTTP_CONNECTION);
        // 获取Session
        SSLSession sslSession = rutedConnnection.getSSLSession();
        if (sslSession != null) {
            Certificate []certificates = sslSession.getPeerCertificates();
            // 保存到context
            context.setAttribute(PEER_CERTIFICATES, certificates);
        }
    };

    /**
     * 获取网站证书信息
     * @param url
     */
    public void getServerCertificate(String url) {
        // 创建HttpClient并添加拦截器
        CloseableHttpClient httpClient = HttpClients.custom().addInterceptorLast(interceptor).build();
        try {
            HttpGet httpGet = new HttpGet(url);
            System.out.println("Executing request " + httpGet.getRequestLine());
            HttpContext context = new BasicHttpContext();
            httpClient.execute(httpGet, context);
            // obtain the server certificates from the context
            Certificate[] peerCertificates = (Certificate[])context.getAttribute(PEER_CERTIFICATES);

            // loop over certificates and print meta-data
            for (Certificate certificate : peerCertificates){
                X509Certificate real = (X509Certificate) certificate;
                System.out.println("=================================================================");
                System.out.println("Type: " + real.getType());
                System.out.println("Signing Algorithm: " + real.getSigAlgName());
                System.out.println("IssuerDN Principal: " + real.getIssuerX500Principal());
                System.out.println("SubjectDN Principal: " + real.getSubjectX500Principal());
                System.out.println("Not After: " + DateUtils.formatDate(real.getNotAfter(), "yyyy-MM-dd HH:mm:ss"));
                System.out.println("Not Before: " + DateUtils.formatDate(real.getNotBefore(), "yyyy-MM-dd HH:mm:ss"));
            }
            System.out.println("=================================================================");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (httpClient != null) httpClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 接受自签名证书
     * @param url
     */
    public void acceptSelfSignedCertificate(String url) {
        CloseableHttpClient httpClient = null;
        try {
            httpClient = (CloseableHttpClient) createAcceptSelfSignedCertificate();
            HttpGet httpGet = new HttpGet(url);
            System.out.println("Executing request " + httpGet.getRequestLine());
            // 发起请求并接受结果
            //String responseBody = httpClient.execute(httpGet, responseHandler);
            //System.out.println("=================================================================");
            //System.out.println(responseBody);
            //System.out.println("=================================================================");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (httpClient != null)httpClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public HttpClient createAcceptSelfSignedCertificate()
            throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
        // 使用SSLContextBuilder设置SSLContext并使用TrustSelfSignedStrategy类来允许自签名证书。
        SSLContext sslContext = SSLContextBuilder.create()
                .loadTrustMaterial(new TrustSelfSignedStrategy())
                .build();
        // 使用NoopHostnameVerifier本质上关闭主机名验证。
        HostnameVerifier allowAllHosts = new NoopHostnameVerifier();
        // 创建SSLConnectionSocketFactory并传入SSLContext和HostNameVerifier
        SSLConnectionSocketFactory connectionFactory = new SSLConnectionSocketFactory(sslContext, allowAllHosts);
        return HttpClients.custom()
                .setSSLSocketFactory(connectionFactory)
                .build();
    }

    /**
     * 请求重试策略
     * @return
     */
    private HttpRequestRetryHandler requestRetryHandler() {
        return (e, i, httpContext) -> {
            System.out.println("try times : " + i);
            // 超过5次停止尝试
            if (i > 5) {
                return false;
            }
            // 外部打断停止尝试
            if (e instanceof InterruptedIOException) {
                return false;
            }
            // 未知主机名停止尝试
            if (e instanceof UnknownHostException) {
                return false;
            }
            // 证书问题停止尝试
            if (e instanceof SSLException) {
                return false;
            }
            HttpClientContext clientContext = HttpClientContext.adapt(httpContext);
            HttpRequest httpRequest = clientContext.getRequest();
            boolean idempotent = !(httpRequest instanceof HttpEntityEnclosingRequest);
            // Retry if the request is considered idempotent
            if (idempotent) {
                return true;
            }
            return false;
        };
    }

    /**
     * 重试请求
     * @param url
     */
    public void retryHandler(String url) {
        CloseableHttpClient httpClient = null;
        try {
            httpClient = HttpClients.custom().setRetryHandler(requestRetryHandler()).build();
            HttpGet httpGet = new HttpGet(url);
            System.out.println("Executing request " + httpGet.getRequestLine());
            // 发起请求并接受结果
            String responseBody = httpClient.execute(httpGet, responseHandler);
            System.out.println("=================================================================");
            System.out.println(responseBody);
            System.out.println("=================================================================");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (httpClient != null)httpClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 使用缓存方式缓存请求
     * @param url
     */
    public void caching(String url) {
        CacheConfig cacheConfig = CacheConfig.custom()
                .setMaxCacheEntries(3000)
                .setMaxObjectSize(10240)
                .build();
        CloseableHttpClient httpClient = null;
        try {
            httpClient = CachingHttpClients.custom()
                    .setCacheConfig(cacheConfig)
                    .build();
            for (int i = 0; i < 3; i++) {
                HttpCacheContext cacheContext = HttpCacheContext.create();
                HttpGet httpGet = new HttpGet(url);
                System.out.println("Executing request " + httpGet.getRequestLine());
                // 发起请求并接受结果
                CloseableHttpResponse response = httpClient.execute(httpGet, cacheContext);
                System.out.println("=================================================================");
                CacheResponseStatus responseStatus = cacheContext.getCacheResponseStatus();
                switch (responseStatus) {
                    case CACHE_HIT:
                        System.out.println("从缓存生成响应，没有向上游发送请求。");
                        break;
                    case CACHE_MODULE_RESPONSE:
                        System.out.println("响应由缓存模块直接生成。");
                        break;
                    case CACHE_MISS:
                        System.out.println("响应来自上游服务器。");
                        break;
                    case VALIDATED:
                        System.out.println("在使用源服务器验证条目后，从缓存生成响应。");
                        break;
                    default:break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (httpClient != null)httpClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public static void main(String[] args) {
        // GET请求地址
        String url = "http://httpbin.org/get";
        HttpClientExample httpClientExample = new HttpClientExample();
        httpClientExample.getMethod(url);
        // POST请求地址
        url = "http://httpbin.org/post";
        httpClientExample.postMethod(url);
        // 证书请求地址
        url = "https://www.baidu.com";
        httpClientExample.getServerCertificate(url);
        // 忽略证书地址
        url = "https://www.yiibai.com";
        httpClientExample.acceptSelfSignedCertificate(url);
        // 重试请求地址
        url = "http://localhost:1234";
        httpClientExample.retryHandler(url);
        // 缓存测试地址
        url = "http://httpbin.org/cache";
        httpClientExample.caching(url);
    }
}
