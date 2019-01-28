<h1 align="center">
  <a href="libp2p.io"><img width="250" src="https://github.com/libp2p/libp2p/blob/master/logo/black-bg-2.png?raw=true" alt="libp2p hex logo" /></a>
</h1>

<h3 align="center">The JVM (Java, Android, Kotlin) implementation of the libp2p Networking Stack.</h3>

<p align="center">
  <a href="http://www.web3labs.com"><img src="https://img.shields.io/badge/made%20by-Web3%20Labs-blue.svg?style=flat-square" /></a>
  <a href="https://gitter.im/web3j/libp2p"><img src="https://badges.gitter.im/web3j/libp2p.svg" /></a>
</p>

<p align="center">
  <!-- TODO Add CI and codecov badges -->
  <br>
</p>


## Background

web3j-libp2p is an implementation of the libp2p modular network stack for the Java virtual machine.

Its goal is to provide a single implementation that meets the needs of Java, Android and Kotlin developers. It is 
written in Kotlin which was designed with Java and Android interop in mind.

The project was created by [Web3 Labs](https://www.web3labs.com) with the support of the 
[Ethereum Community Fund](https://ecf.network) and advisory from members of the libp2p team at 
[Protocol Labs](https://protocol.ai). 


## Getting started

Versioned releases and regular snapshots will be available shortly for the various 
libp2p modules. Until that time, please clone the repo and you can build the artifacts yourself:

```bash
git clone https://github.com/web3j/libp2p.git
cd libp2p
./gradlew build publishToMavenLocal
```


## Contribute

We welcome contributions from everyone.

We are currently prioritising implementation of the libp2p modules that are required for eth2.0's Serenity release. As it currently stands
they are (taken from the following [issue](https://github.com/ethresearch/p2p/issues/4#issuecomment-436702674)):

| Library             | Module                                        | Available                |
|---------------------|-----------------------------------------------|--------------------------|
| Multiformat         | Multiaddr                                     | :white_check_mark:       |
|                     | Multihash                                     | :white_check_mark:       |
| General Purpose     | Net                                           | :white_check_mark:       |
|                     | Peer                                          | :white_check_mark:       |
|                     | Crypto                                        | :white_check_mark:       |
| Protocol Muxer      | Multistream(-select)                          |                          |
| Stream Muxer        | Multiplex                                     |                          |
| Crypto Channels     | SecIO                                         |                          |
| Transport           | TCP                                           | :hourglass_flowing_sand: |
| Network Abstraction | Switch/Swarm                                  |                          |
| Peerstore           | Local database backend (KV store or SQLite)   |                          |
| Protocols           | Ping                                          |                          |
|                     | Identify                                      |                          |
| Host Abstraction    |                                               | :white_check_mark:       |
| NAT traversal       |                                               |                          |
| Others              | Floodsub/Gossipsub (w/signed message records) |                          |
| Peer Routing        | kad-dht                                       |                          |
| Discovery           | Find_node (or whatever is chosen)             |                          |


We recommend before starting work on a contribution that you communicate with the libp2p JVM community on the 
[Gitter](https://gitter.im/web3j/libp2p) channel to express your intent to ensure no-one is already working on it.

Additionally, keep an eye on the open [Pull Requests](https://github.com/web3j/libp2p/issues) and 
[branches](https://github.com/web3j/web3j/branches) to see what others are working on.

Also, please do review the project [issues](https://github.com/web3j/libp2p/issues) to identify any low hanging fruit 
you can get started with!   

## Build instructions

```bash
git clone https://github.com/web3j/libp2p.git
cd libp2p
./gradlew build publishToMavenLocal
```
