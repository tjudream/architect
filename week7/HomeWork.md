# Week7 
## 1. 性能压测的时候，随着并发压力的增加，系统响应时间和吞吐量如何变化，为什么？

随着并发压力的增加，系统响应时间会越来越长，吞吐量会先增加，后下降，最后直至系统崩溃，吞吐量变为0.

随着并发压力的增加，系统需要更多的线程处理更多的请求，这就有可能会导致一些锁竞争、线程间调度等问题，
从而导致系统的响应时间逐步增加。

随着并发压力的增加，系统为了处理更多的请求需要创建更多的线程，消耗更多的cpu、内存等硬件资源，
当硬件资源尚未被完全被用满时，系统单位时间的处理请求数随着并发的增加而增加，因此系统的吞吐量增加。

但是随着并发压力的不断增加，操作系统资源或者硬件资源被用完，比如文件句柄数、端口数、cpu、内存等，
随着系统资源被用完，系统的处理能力达到极限，再来更多的请求只能排队等待资源，此时系统的吞吐量达到最高点。

此时并发再增加，逐步超过了系统的处理能力导致，为了处理过多的请求系统需要使用部分资源处理线程间调度，
维护队列，导致吞吐量逐步下降，直到突破系统的临界点，系统崩溃，吞吐量变为0.

## 2. 用你熟悉的编程语言写一个 web 性能压测工具，输入参数：URL，请求总次数，并发数。输出参数：平均响应时间，95% 响应时间。用这个测试工具以 10 并发、100 次请求压测 www.baidu.com

采用线程模拟并发请求，多少个并发就起多少个线程。因此采用线程池来进行并发压力测试。

使用 Future 来获取线程的执行结果。

设计两个类，一个 RequestTask 类实现 Callable 接口，用于发送请求，并计算请求时间。
一个 Pressure 类，用于创建线程池并进行并发请求，同时计算结果。

* Pressure 类
```java
public class Pressure {
    /**
     * 压测方法
     * @param url 请求的url
     * @param totalNum 请求总数
     * @param concurrencyNum 并发数
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public void pressureUrl(String url, Long totalNum, Integer concurrencyNum) throws ExecutionException, InterruptedException {
        List<Future<Long>> list = new ArrayList<>();
        /**
         * 创建固定数量线程的线程池，线程数为并发数
         */
        ExecutorService executorService = Executors.newFixedThreadPool(concurrencyNum);
        /**
         * 向线程池中提交任务，任务个数为请求总数
         */
        for (int i = 0; i < totalNum; i++) {
            RequestTask rt = new RequestTask(url);
            /**
             * Future 类异步获取结果
             */
            Future<Long> result = executorService.submit(rt);
            list.add(result);
        }
        executorService.shutdown();
        /**
         * 获取结果并存储到 time 列表中
         */
        List<Long> time = new ArrayList<>();
        for (Future<Long> res : list) {
            time.add(res.get());
        }
        System.out.println(Arrays.toString(time.toArray()));
        /**
         * 排序
         */
        Collections.sort(time);
        System.out.println(Arrays.toString(time.toArray()));
        /**
         * 计算总时间
         */
        Long totalTime = 0L;
        for (Long t : time) {
            totalTime += t;
        }
        /**
         * 计算平均时间
         */
        Long avg = totalTime/totalNum;
        /**
         * 计算 95% 时间
         */
        int pstart = (int)(totalNum*95/100);
        int pend = (int)(totalNum*96/100) - 1;
        if (pend < pstart) {
            pend = pstart;
        }
        List<Long> p95List = new ArrayList<>();
        for (int i = pstart; i <= pend; i++) {
            p95List.add(time.get(i));
        }

        System.out.println("平均响应时间 : " + avg + "ms");
        System.out.println("95% 响应时间 : " + p95List.toString());
    }
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        Pressure pressure = new Pressure();
        String url = "https://www.baidu.com";
//        String url = "https://leetcode-cn.com/";
//        String url = "http://news.baidu.com/";
//        String url = "https://www.taobao.com";
//        String url = "http://www.qq.com";
//        String url = "https://www.jd.com";
        Long totalNum = 100L;
        int c = 10;
        pressure.pressureUrl(url, totalNum, c);
    }
```

* RequestTask 类
```java
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
```

压测百度获取结果：
* 平均响应时间 : 59ms
* 95% 响应时间 : [495]

```text
[496, 491, 497, 498, 492, 496, 495, 491, 494, 489, 37, 39, 46, 40, 32, 49, 35, 12, 45, 33, 9, 14, 13, 12, 10, 10, 10, 10, 13, 7, 10, 10, 8, 10, 10, 9, 10, 8, 7, 7, 9, 10, 9, 11, 8, 9, 8, 9, 8, 7, 7, 6, 6, 5, 6, 6, 6, 5, 6, 6, 8, 8, 8, 6, 6, 7, 8, 7, 6, 7, 6, 6, 6, 5, 6, 7, 8, 6, 6, 7, 6, 8, 9, 8, 10, 8, 7, 8, 7, 8, 6, 8, 8, 13, 10, 8, 12, 11, 10, 10]
[5, 5, 5, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 9, 9, 9, 9, 9, 9, 9, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 11, 11, 12, 12, 12, 13, 13, 13, 14, 32, 33, 35, 37, 39, 40, 45, 46, 49, 489, 491, 491, 492, 494, 495, 496, 496, 497, 498]
平均响应时间 : 59ms
95% 响应时间 : [495]
```

每个线程的第一次请求时间较长，后边时间都较短，百度做了优化