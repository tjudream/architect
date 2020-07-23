package lambdasinaction.chap7;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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
}
