# Week11 课后作业
## 1. 导致系统不可用的原因有哪些？保障系统稳定高可用的方案有哪些？请分别列举并简述。
#### 1.1 导致系统不可用的原因可能有以下情况
* 硬件故障
* 软件 bug
* 依赖的第三方服务故障
* 系统发布
* 并发压力
* 网络故障
* 外部灾害
#### 1.2 保障系统稳定高可用的方案
##### 1.2.1 解耦
* 高内聚、低耦合的组件设计原则
* 面向对象设计原则
* 面向对象设计模式
* 领域驱动设计
##### 1.2.2 隔离
* 业务与子系统隔离
* 微服务与中台架构
* 生产者消费者隔离
* 虚拟机与容器隔离
##### 1.2.3 异步
* 多线程编程
* 反应式编程
* 异步通信网络编程
* 事件驱动异步架构
##### 1.2.4 备份
* 集群设计
* 数据库复制： CAP原理
##### 1.2.5 Failover（失效转移）
* 数据库主主失效转移
* 负载均衡失效转移
* 设计无状态的服务
##### 1.2.6 事务补偿
通过执行业务逻辑逆操作，使事务回滚到事务前状态
##### 1.2.7 重试
调用者通过重试的方式修复单次调用的故障
##### 1.2.8 熔断
当某个服务出现故障，响应延迟或者失败率增加，继续调用这个服务会导致调用者请求
阻塞，资源消耗增加，进而出现服务级联失效，这种情况下使用断路器阻断对故障服务
的调用。
* 断路器三种状态：关闭、打开，半开
* Spring Cloud 断路器实现：Hystrix
##### 1.2.9 限流
* 计数器算法（固定窗口，滑动窗口）
* 令牌桶算法
* 漏桶算法
* 自适应限流
##### 1.2.10 降级
高并发状态下，将一些非核心功能下线，保障核心功能的可用性
##### 1.2.11 异地多活
将数据中心分布在多个不同地点的 机房里，这些机房都可以对外提供服务，用户可以连接任何一个机房进行访问，这样每 个机房都可以提供完整的系统服务，即使某一个机房不可用，系统也不会宕机，依然 保持可用。
## 2. 请用你熟悉的编程语言写一个用户密码验证函数，Boolean checkPW（String 用户 ID，String 密码明文，String 密码密文）返回密码是否正确 boolean 值，密码加密算法使用你认为合适的加密算法。
```java
public class User {
    /**
     * 校验密码，使用md5算法，使用id和常量字符串GeekTime作为salt
     * @param id 用户id
     * @param pass 明文密码
     * @param secret 密文密码
     * @return 密码是否正确
     */
    public Boolean checkPw(String id, String pass, String secret) {
        if (id == null || "".equals(id) || pass == null || "".equals(pass) || secret == null || "".equals(secret)) {
            return false;
        }
        return secret.equals(md5(id + "_" + md5(pass) + "_GeekTime"));
    }

    /**
     * MD5 加密
     *
     * @param str 明文
     * @return 密文
     * @throws NoSuchAlgorithmException
     */
    public static String md5(String str) {
        if (str == null || str.length() == 0) {
            throw new IllegalArgumentException("str cannot be null");
        }
        StringBuffer hexString = new StringBuffer();
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(str.getBytes());
            byte[] hash = md.digest();
            for (int i = 0; i < hash.length; i++) {
                if ((0xff & hash[i]) < 0x10) {
                    hexString.append("0" + Integer.toHexString((0xFF & hash[i])));
                } else {
                    hexString.append(Integer.toHexString(0xFF & hash[i]));
                }
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return hexString.toString();
    }
}
```
