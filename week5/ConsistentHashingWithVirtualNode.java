package test;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ConsistentHashingWithVirtualNode {
    private String[] servers = {
            "172.16.52.80",
            "172.16.52.81",
            "172.16.52.82",
            "172.16.52.83",
            "172.16.52.84",
            "172.16.52.85",
            "172.16.52.86",
            "172.16.52.87",
            "172.16.52.88",
            "172.16.52.89"
    };
    /**
     * 考虑到有可能会经常增加删除节点，用链表存储服务器节点
     */
    private List<String> realNodes = new LinkedList<>();
    /**
     * 虚拟节点，key 是虚拟节点的 hash 值， value 是虚拟节点的名称
     */
    private SortedMap<Integer, String> virtualNodes = new TreeMap<>();

    /**
     * key 的个数
     */
    private int KEY_NUMS = 1_000_000;

    /**
     * 每个物理节点对应虚拟节点的个数
     */
    private int VIRTUAL_NODES = 150;

    public void init() {
        realNodes = new LinkedList<>();
        virtualNodes = new TreeMap<>();
        /**
         * 初始化服务器列表
         */
        for (String server : servers) {
            realNodes.add(server);
        }
        /**
         * 初始化虚拟节点
         */
        for (String node : realNodes) {
            for (int i=0; i<VIRTUAL_NODES; i++) {
                String vNodeName = node + "&&VN" + i;
                int hash = getHash(vNodeName);
                virtualNodes.put(hash, vNodeName);
            }
        }
    }

    /**
     * 目前比较常用的一致性Hash算法有CRC32_HASH、FNV1_32_HASH、KETAMA_HASH等，其中FNV1_32_HASH号称是速度最快的一致性Hash算法.
     * 这里使用 FNV1_32_HASH 算法: https://en.wikipedia.org/wiki/Fowler%E2%80%93Noll%E2%80%93Vo_hash_function
     */
    public int getHash(String str) {
        final int p = 16777619;
        int hash = (int) 2166136261L;
        for (int i = 0; i < str.length(); i++) {
            hash = (hash ^ str.charAt(i)) * p;
        }
        hash += hash << 13;
        hash ^= hash >> 7;
        hash += hash << 3;
        hash ^= hash >> 17;
        hash += hash << 5;
        // 如果算出来的值为负数则取其绝对值
        if (hash < 0)
            hash = Math.abs(hash);
        return hash;
    }

    public String getServer(String key) {
        String node = null;
        int hash = getHash(key);
        SortedMap<Integer,String> subMap = virtualNodes.tailMap(hash);
        if (subMap.isEmpty()) {
            node = virtualNodes.get(virtualNodes.firstKey());
        } else {
            node = subMap.get(subMap.firstKey());
        }
        if (node != null) {
            node = node.substring(0, node.indexOf('&'));
        }
        return node;
    }

    /**
     * 生成 n 个 key
     */
    public Set<String> getKeySet(int n) {
        /**
         * 用 uuid 生成 100w 个 key
         */
        Set<String> keySet = new HashSet<>();
        while (keySet.size() < n) {
            String key = UUID.randomUUID().toString();
            if (keySet.contains(key)) {
                continue;
            }
            keySet.add(key);
        }
        return keySet;
    }
    /**
     * 标准差计算公式： https://baike.baidu.com/item/%E6%A0%87%E5%87%86%E5%B7%AE%E5%85%AC%E5%BC%8F
     * 样本标准差=方差的算术平方根=s=sqrt(((x1-x)^2 +(x2-x)^2 +......(xn-x)^2)/（n-1）)
     * 总体标准差=σ=sqrt(((x1-x)^2 +(x2-x)^2 +......(xn-x)^2)/n )
     * 我们这里计算的是总体的标准差
     * 这里 n=10 是服务器台数
     * xi 表示第i台服务器上 key 的个数
     * x 为 x1...x10 的平均数
     * @param keySet
     */
    public double standardDeviation(Set<String> keySet) {
        /**
         * 计算 key 在每台服务器上的分布
         * 将每台服务器中 key 的个数存储在 map 中，其中 map 中的 key 表示服务器 IP ， value 表示这台服务器上存储的 key 的个数
         */
        int[] nums = new int[servers.length];
        for (String s : keySet) {
            String server = getServer(s);
//            int i = realNodes.indexOf(server);
            int i = server.charAt(server.length() - 1) - '0';
            nums[i]++;
        }
        for (int i=0; i<nums.length; i++) {
//            System.out.println("第" + i + "台服务器包括的 key 的数量 : " + nums[i]);
        }
        /**
         * 计算平均数 = 100w 个 key / 10 台服务器 = 10w
         */
        double avg = KEY_NUMS/servers.length;
        /**
         * 计算标准差
         */
        int nsum = 0;
        for (int i=0; i<nums.length; i++) {
            int d = (int)Math.abs(nums[i] - avg);
            int x = (int)Math.pow(d, 2);
            nsum += x;
//            System.out.println("d = " + d + ", x = " + x);
        }
//        System.out.println("平方和 : " + nsum);
        /**
         * 方差
         */
        double s = (double)nsum/(double)nums.length;
        /**
         * 标准差
         */
        double sd = Math.sqrt(s);
//        System.out.println("s = " + s + ", sd = " + sd);
        return sd;
    }

    /**
     * 找到最优的虚拟节点数
     */
    public int findOptimalVNodeNum() {
        /**
         * 生成 100w 个 key
         */
        Set<String> keySet = getKeySet(KEY_NUMS);
        /**
         * 对于虚拟节点从 150 ~ 200 分别计算标准差
         */
        double min = Double.MAX_VALUE;
        int mini = 0;
        for (int i=150; i<=200; i++) {
            VIRTUAL_NODES = i;
            init();
            double sd = standardDeviation(keySet);
            if (min > sd) {
                min = sd;
                mini = i;
            }
//            System.out.printf(VIRTUAL_NODES + " | %.2f\n", sd);
        }
        System.out.printf("i = %d, min = %.2f\n", mini, min);
        return mini;
    }


    public static void main(String[] args) {
        /**
         * 4 核，计算密集型，创建 6 个线程
         */
        ExecutorService pool = Executors.newFixedThreadPool(6);
        for (int i=0; i<10; i++) {
            pool.execute(() -> {
                new ConsistentHashingWithVirtualNode().findOptimalVNodeNum();
            });
        }
        pool.shutdown();
    }
}
