# MOOC

MOOC is a distributed coordination service that implements Raft consensus algorithm, featuring:

* Master election.
* State machine (log) replication.
* Tolerance of no more than half cluster nodes down.
* Majority read/write.

Created distributed server system based on the Raft Paper (on the lines on PAXOS) and coded logic for dynamic leader election.
- Maintained consistent state between servers using Log replication.

Technologies used-  Java, Socket Programming, Netty IO, Google Protobuf, JMeter, Multi-threading
