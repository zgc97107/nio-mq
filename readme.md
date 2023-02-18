## NIO网络通信

**Java nio中，主要有三大组件：Buffer，Channel和Selector。**

- Buffer提供了一个字节缓冲区，其可以不断的从Channel中读取接收到的数据。Buffer的优点主要在于其提供了一系列的Api，能够让用户更方便的对数据进行读取和写入；
- Channel简单来说就是一个信道，也就是客户端与服务器的一个连接，而且每个客户端都会对应一个Channel对象；
- Selector是Java nio能够支持高并发数据处理一个关键，其核心理念就是IO多路复用的原理，简单的说就是当多个客户端（Channel）连接服务器时，可以通过Selector同时对这些客户端请求进行监听，当客户端发送数据到服务器之后由Selector对这些Channel进行分发处理。

### Buffer

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

### Channel

Java NIO的通道类似流，但又有些不同。

- 通道是双向的，既可以从通道中读取数据，又可以写数据到通道。流的读写通常是单向的。

- 通道可以异步地读写。

- 通道中的数据读写需要借助buffer进行，无法直接读写。

**Channel类型：**

- FileChannel：从文件中读写数据。
- DatagramChannel：能通过UDP读写网络中的数据。
- SocketChannel：能通过TCP读写网络中的数据。
- ServerSocketChannel：可以监听新进来的TCP连接，像Web服务器那样。对每一个新进来的连接都会创建一个SocketChannel。

### Selector

一般来说Channel都是和Selector配合使用，将channel注册到selector上，实现一个线程同时监听多个channel。与Selector一起使用时，Channel必须处于非阻塞模式下。

在将Channel注册到Selector上时，需要指定关注的事件，可以关注四种不同类型的事件：

- SelectionKey.OP_CONNECT
- SelectionKey.OP_ACCEPT
- SelectionKey.OP_READ
- SelectionKey.OP_WRITE

Selector关注的事件类型通过八个byte位标记，如果需要关注多个事件，或从多个事件中取消关注某个事件，可以使用与操作及或操作进行。

