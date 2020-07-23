package lambdasinaction.chap7;

import sun.net.www.http.HttpClient;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

public class RequestTask implements Callable<Long> {
    private String url;
    public RequestTask(String url) {
        this.url = url;
    }
    @Override
    public Long call() throws Exception {
        /**
         * 发送 GET 请求并计算耗时，单位：毫秒
         */
        Long t1 = System.currentTimeMillis();
        doGet(this.url);
        Long t2 = System.currentTimeMillis();
        return t2 - t1;
    }

    /**
     * 发送 Get 请求
     * @param urlstr 请求 url
     */
    public void doGet(String urlstr) {
        URL url = null;
        HttpURLConnection conn = null;
        BufferedReader in = null;
        try {
            url = new URL(urlstr);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("content-type", "application/x-www-form-urlencoded");
            conn.setRequestMethod("GET");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setUseCaches(false);
            //设置连接超时时间
            conn.setConnectTimeout(3000);
            //设置读取超时时间
            conn.setReadTimeout(3000);
            /**
             * 获取响应头
             */
            /*
            Map headers = conn.getHeaderFields();
            Set<String> keys = headers.keySet();
            for( String key : keys ){
                String val = conn.getHeaderField(key);
                System.out.println(key+"    "+val);
            }
             */
            StringBuilder result = new StringBuilder();
            in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            if(in != null) {
                String line = "";
                while ((line = in.readLine()) != null) {
                    result.append(line);
                }
            }
//            System.out.println("res = " + result.toString());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (in !=null) {
                    in.close();
                }
            }catch (Exception e) {
                e.printStackTrace();
            }
            if (conn != null) {
                conn.disconnect();
            }
        }
    }
}
