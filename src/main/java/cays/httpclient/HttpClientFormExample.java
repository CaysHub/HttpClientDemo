package cays.httpclient;

import org.apache.http.*;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * HttpClient表单提交数据
 *
 * @author Chai yansheng
 * @create 2019-08-15 13:38
 **/
public class HttpClientFormExample {

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
     * 提交表单数据
     * @param url
     */
    public void postForm(String url) {
        CloseableHttpClient closeableHttpClient = null;
        try {
            closeableHttpClient = HttpClients.createDefault();
            // 表单数据
            List<NameValuePair> form = new ArrayList<>();
            form.add(new BasicNameValuePair("name", "cays"));
            form.add(new BasicNameValuePair("password", "123456"));
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(form);
            // 设置表单数据
            HttpPost post = new HttpPost(url);
            post.setEntity(entity);
            System.out.println("Executing request " + post.getRequestLine());
            // 发起请求并接受结果
            String responseBody = closeableHttpClient.execute(post, responseHandler);
            System.out.println("=================================================================");
            System.out.println(responseBody);
            System.out.println("=================================================================");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (closeableHttpClient != null) {
                try {
                    closeableHttpClient.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 处理重定向
     * @param url
     */
    public void redirectHandling(String url) {
        CloseableHttpClient closeableHttpClient = null;
        try {
            closeableHttpClient = HttpClients.custom()
                    .setRedirectStrategy(new LaxRedirectStrategy())
                    .build();
            HttpClientContext httpClientContext = HttpClientContext.create();
            HttpGet httpGet = new HttpGet(url);
            System.out.println("Executing request " + httpGet.getRequestLine());
            System.out.println("=================================================================");
            closeableHttpClient.execute(httpGet, httpClientContext);
            HttpHost target = httpClientContext.getTargetHost();
            List<URI> redirectLocations = httpClientContext.getRedirectLocations();
            redirectLocations.forEach(redirectLocation -> System.out.println("中间地址：" + redirectLocation));
            URI destination = URIUtils.resolve(httpGet.getURI(), target, redirectLocations);
            System.out.println("目的地址：" + destination.toASCIIString());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (closeableHttpClient != null) {
                try {
                    closeableHttpClient.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 自定义请求头
     * @param url
     */
    public void setHeaders(String url) {
        // 设置请求头
        List<Header> headers = Arrays.asList(
                new BasicHeader("X-Default-Header", "default header httpclient")
        );
        CloseableHttpClient closeableHttpClient = null;
        try {
            closeableHttpClient = HttpClients.custom()
                    .setDefaultHeaders(headers)
                    .build();
            HttpUriRequest request = RequestBuilder.get()
                    .setUri(url)
                    .setHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                    .setHeader(HttpHeaders.FROM, "https://memorynotfound.com")
                    .setHeader("X-Custom-Header", "custom header http request")
                    .build();
            System.out.println("Executing request " + request.getRequestLine());
            String responseBody = closeableHttpClient.execute(request, responseHandler);
            System.out.println("=================================================================");
            System.out.println(responseBody);
            System.out.println("=================================================================");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (closeableHttpClient != null) {
                try {
                    closeableHttpClient.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 上传文件
     * @param url
     */
    public void doMultipart(String url) {
        CloseableHttpClient closeableHttpClient = null;
        try {
            closeableHttpClient = HttpClients.createDefault();
            File file = new File("src\\main\\java\\cays\\httpclient\\a.txt");
            String message = "文本部分";
            HttpEntity entity = MultipartEntityBuilder.create()
                    .setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
                    .addBinaryBody("file", file, ContentType.DEFAULT_BINARY, file.getName())
                    .addTextBody("message", message)
                    .build();
            HttpUriRequest request = RequestBuilder.post(url)
                    .setEntity(entity)
                    .build();
            System.out.println("Executing request " + request.getRequestLine());
            String responseBody = closeableHttpClient.execute(request, responseHandler);
            System.out.println("=================================================================");
            System.out.println(responseBody);
            System.out.println("=================================================================");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (closeableHttpClient != null) {
                try {
                    closeableHttpClient.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    public static void main(String[] args) {
        String url = "http://httpbin.org/post";
        HttpClientFormExample formExample = new HttpClientFormExample();
        // 表单提交
        formExample.postForm(url);
        // 重定向
        url = "http://httpbin.org/redirect/3";
        formExample.redirectHandling(url);
        // 自定义请求头
        url = "http://httpbin.org/headers";
        formExample.setHeaders(url);
        // 分段请求上传文件
        url = "http://httpbin.org/post";
        formExample.doMultipart(url);
    }
}
