# Week6 总结
## 数据分片

当数据量增长到一定程度，超过了一台服务器的存储能力或者超过了一张表的存储上限之后就需要考虑
数据分片

### 数据分片的实现类型
* 硬编码实现（实际不常用）: 客户端对 id 取余
* 映射表外部存储（实际不常用）: 用一张第三方表记录分片方案
* 分布式数据库中间件 : Mycat, Amoeba/Cobar 架构

    提前预设好 schema 的数量，将多个 schema 存储在一台服务器上，当单台服务器无法满足时，
    将一部分 schema 迁移到其他服务器上
## NoSQL
### CAP 原理
* 一致性
* 可用性
* 分区耐受性

在分布式系统必须满足分区耐受性的前提下，可用性和一致性无法同时满足。

最终一致性写冲突的处理策略：
* 简单暴力：根据时间戳，最后写入的覆盖之前的
* 客户端解决：例如购物车合并
* 投票解决：Cassandra
* 仲裁节点保证：Hbase 中，由 HMaster 决定写入到哪些节点，
    HMaster 的一致性使用 Zookeeper 保证，Hbase 使用 LSM Tree 提升写入性能
    
## Zookeeper
### 分布式系统脑裂问题
在一个分布式系统中，不同服务器获得了互相冲突的数据信息或者执行指令，导致整个集群陷入混乱，
数据损坏，称作分布式系统脑裂

### 分布式一致性算法 Paxos
三个角色：
* Proposer 提议者
* Acceptor 接收者
* Leaner 学习者

决策过程：
* 第一阶段：Prepare 阶段。 Proposer 想 Acceptors 发出 Prepare 请求，Acceptors
针对收到的 Prepare 请求进行 Promise 承诺
* 第二阶段：Accept 阶段。Proposer 收到多个 Acceptors 承诺的 Promise 后，向 Acceptors 
发出 Propose 请求，Acceptors 针对收到的 Propose 请求进行 Accept 处理
* 第三阶段：Learn 阶段。Proposer 在收到的多数 Acceptors 的 Accept 之后，标志着本次
Accept 成功，决议形成，将形成的决议发送给所有 Learners


Proposer 生成全局唯一且递增的 Proposal ID （可使用时间戳加 Server ID），向所有 Acceptors
发送 Prepare 请求，这里无需携带提案内容，值携带 Proposal ID 即可。

Acceptors 收到 Prepare 和 Propose 请求后：

    1. 不再接受 Proposal ID 小于等于当前请求的 Prepare 请求
    2. 不再接受 Proposal ID 小于当前请求的 Propose 请求
 
### Zab 协议
角色
* Leader 领导者 : 整个集群只能有一个 Leader，负责发起和维护 Follwer 和 Observer 间的心跳，
所有写操作必须由 Leader 完成
* Follower 跟随者 : 可以有多个，可以直接处理读请求，将写请求发送给 Leader，并在 Leader 处理
写请求时投票
* Observer 观察者 : 与 Follwer 类似，不过没有投票权


## Doris - 海量 KV Engine
在公司立项的套路：
* 1. 说明现状，列举现在面临的困难与挑战
* 2. 说明现有方案的痛点
* 3. 提出需求
* 4. 给出产品设计目标，需要满足现有需求
* 5. 给出设计架构，说明如何满足现有需求，说明其优点
* 6. 给出关键技术点
* 7. 给出产品规划（功能和版本迭代）
* 8. 给出研发计划和所需资源
* 9. 除产品之外的项目产出，比如专利等