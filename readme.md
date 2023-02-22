## NIO网络通信

### ByteBuffer

**ByteBuffer类型**

|                  | 描述                                                         | 特点                                                         |
| ---------------- | ------------------------------------------------------------ | ------------------------------------------------------------ |
| HeapByteBuffer   | 在jvm堆上面的一个buffer，底层的本质是一个数组                | 由于内容维护在jvm里，所以把内容写进buffer里速度会快些；并且，可以更容易回收 |
| DirectByteBuffer | 底层的数据其实是维护在操作系统的内存中，而不是jvm里，DirectByteBuffer里维护了一个引用address指向了数据，从而操作数据 | 跟外设（IO设备）打交道时会快很多，因为外设读取jvm堆里的数据时，不是直接读取的，而是把jvm里的数据读到一个内存块里，再在这个块里读取的，如果使用DirectByteBuffer，则可以省去这一步，实现zero copy |

**ByteBuffer属性**

- mark：记录了当前所标记的索引下标；
- position：对于写入模式，表示当前可写入数据的下标，对于读取模式，表示接下来可以读取的数据的下标；
- limit：对于写入模式，表示当前可以写入的数组大小，默认为数组的最大长度，对于读取模式，表示当前最多可以读取的数据的位置下标；
- capacity：表示当前数组的容量大小；
- array：保存了当前写入的数据。

**ByteBuffer方法**

- Buffer clear()：把position设为0，把limit设为capacity，复用Buffer时调用。
- Buffer flip()：把limit设为当前position，把position设为0，buffer填充数据后，读取时调用。
- Buffer rewind()：把position设为0，limit不变，buffer填充数据后，读取时调用。
- compact()：将 position 与 limit之间的数据复制到buffer的开始位置，复制后 position = limit -position，limit = capacity，删除已读取数据时调用。
- mark() & reset()：通过调用Buffer.mark()方法，可以标记Buffer中的一个特定position。之后可以通过调用Buffer.reset()方法恢复到这个position。

![img](https://upload-images.jianshu.io/upload_images/1115848-13f9d29c65eda2cd.png?imageMogr2/auto-orient/strip|imageView2/2/w/910)

### Selector

BIO服务端与客户端进行通信时，Socket的accept、getInputStream、getOutputStream方法都会阻塞当前线程，所以服务端对于每个客户端的连接，都需要一个额外的线程去处理。

NIO服务端与客户端通信的channel可以注册到selector上并设置自己需要的事件，比如connection、read，selector的select方法可以拿到符合要求的channel。理论上来说可以由一个线程去select，完成与多个客户端的通信。

## NIO性能优化

### Reactor线程模型

虽然单线程就可以完成与客户端的通信，但业务比较耗时的情况下效率相当感人。对于多核机器也会出现一核有难，多核围观的情况。

所以基于NIO再封装的Netty就提供了BossGroup与WorkGroup的两个参数，一组线程用来处理新连接，一组线程用来处理其他事件，这其实就是对Reactor线程模型的实现。

其实Reactor线程模型也很简单，首先把网络通信的逻辑跟业务逻辑分开，负责网络逻辑的叫Acceptor、负责业务逻辑的叫Handler。单线程Reactor模型其实还是一个线程负责所有逻辑，多线程模型就是将Acceptor跟Processor交由不同的线程组。主从多线程模型就是对Acceptor的再次拆分，主Acceptor只负责分配新连接，从Acceptor（Processor）负责完成连接与读写事件。

### Channel的读写事件

一般来说只要读取完毕一次请求，需要暂时取消对读事件的关注，将读取到的请求放入Handler之后，在进行继续关注读事件。但一直关注读事件也不会有性能浪费的情况，读事件只有在接到消息时才会触发。

写事件只有在channel写满时写事件才不会被触发，如果持续关注写事件，但是数据一直没准备好，会浪费一些性能，所以一般只有在数据准备好时，在进行写事件的触发，但原生nio对这种操作支持的并不是很友好，一般需要一个channel的代理或者继承channel来实现。

### 批量发送

网络IO的速度一般要远远小于内存的速度，离硬盘速度其实也有很大差距，所以批量发送消息要远比单次发送要合算得多。但是需要系统能够容忍批量发送带来的延时，以及数据丢失问题，一般来说支持批量发送的中间件都会提供两个参数：批量发送的最大大小以及最大等待时间。

### BufferPool

对于ByteBuffer的申请和释放都是比较昂贵的，大多数BufferPool的大小要远大于普通对象的大小，频繁申请会导致FullGC次数的明显提升，如果申请的是DirectBuffer，而且没有对JVM能够使用的直接内存进行限制，很容易造成堆外内存泄露。所以一般来说都会对ByteBuffer进行统一管理。

### 内存文件映射

NIO可以将磁盘中的文件映射到内存中，实现接近内存的读写速度。如果内存不足就只会缓存文件的一部分页，其他部分的在虚拟内存中。kafka的稀疏索引的读写用到了这个技术。

[NIO之内存映射文件原理 - 枫树湾河桥 - 博客园 (cnblogs.com)](https://www.cnblogs.com/fswhq/p/12141574.html)

### 最后

NIO的技术繁杂而又深邃，涉及到很多操作系统内核的知识，今天分享的大多是应用的东西，全都避开了这部分理论知识，对于这些我知道的很少而且没法确定是否准确，比如线程的内核态跟用户态、进程的私有空间、虚拟空间，所以今天的分享不过是望其门墙而不入于其宫，门外汉而已。
