# Cluster Membership Common
This project holds common interface and configuration between protocol modules

### Protocol Configuration
The protocol configuration is based on [this](src/main/resources/config/app.properties) file and has two sections, both are described bellow

#### Protocol section

```bash
#the interval to make request to other nodes to keep state synchronized
iteration.interval.ms=3000

#connection time out to reach other nodes
connection.timeout.ms=1000

#the factor to multiply by iteration.interval.ms * (iterations=max.expected.node.log.2 || log2(cluster size)), and consider to send an update request
read.iddle.iteration.factor=3

#the time to wait for a node sends a keep alive signal, to avoid removing from cluster
failing.node.expiration.time.ms=86400000

#a constant to generated random numbers
max.expected.node.log.2=32

#the max number of rumors stored in memory for later use in restoring a node, like a commit log of the cluster state
max.rumor.log.size=1000000

#the maximal number of byte that is allowed to send over the network, max int value by default
max.object.size=2147483647

```
#### Node section

```bash 
#the id of the node, if not provided an randomly UUID is generated
id=A

address=localhost

#the netty socket port for protocol
protocol.port=7001

#the server port for rest service
server.port=6001

time.zone=Europe/Madrid
```