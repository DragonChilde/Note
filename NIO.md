# NIO #

NIO的核心在于:**通道(channel)**和**缓冲区(buffer)**,Channel负责传输,Buffer负责存储
缓冲区(Buffer):在Java NIO中负责数据的存取.缓冲区就是数组.用于存储不同数据类型的数据
根据数据类型不同(boolean除外),提供了相应类型的缓冲区:

- ByteBuffer
- CharBuffer
- ShortBuffer
- IntBuffer
- LongBuffer
- FloatBuffer
- DoubleBuffer

上述缓冲区的管理方式几乎一致，通过static xxxBuffer.allocate(int capacity)获取缓冲区

缓冲区存取数据的两个核心方法:
    
    put():存入数据到缓冲区中
    get():获取缓冲区中的数据

缓冲区中的四个核心属性:

	capactiy:容量,表示缓冲区中最大存储数据的容量.一旦声明不能改变
	limit:界限,表示缓冲区中可以操作数据的大小.(limit 后数据不能进行读写)
	position:位置，表示缓冲区中正在操作数据的位置
	mark:标记，表示记录当前postion的位置。可以通过reset()恢复到mark位置0<=mark<=position<=limit<=capacity
	flip():切换读取数据模式	读->写		写->读
			使缓冲区为新的通道写入或相对获取操作序列做好准备：它将limit设置为当前位置，然后将位置设置为零。
	rewind():可重复读数据
	clear():清空缓冲区,但是缓冲区中的数据依然存在，但是处于"被遗忘"状态
	hasRemaining:判断缓冲区中是否还有剩余数据
	remaining:获取缓冲区中可以操作的数量

	ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
	System.out.println(byteBuffer.capacity());		//1024
	System.out.println(byteBuffer.limit());			//1024
	System.out.println(byteBuffer.position());		//0
	
	String str = "abcde";
	byteBuffer.put(str.getBytes());					
	System.out.println(byteBuffer.capacity());		//1024
	System.out.println(byteBuffer.limit());			//1024
	System.out.println(byteBuffer.position());		//5
	
	byteBuffer.flip();
	System.out.println(byteBuffer.capacity());		//1024
	System.out.println(byteBuffer.limit());			//5
	System.out.println(byteBuffer.position());		//0
	
	byte[] bytes = new byte[byteBuffer.limit()];
	byteBuffer.get(bytes);
	System.out.println(new String(bytes,0,byteBuffer.limit()));	//abcde
	System.out.println(byteBuffer.capacity());			//1024
	System.out.println(byteBuffer.limit());				//5
	System.out.println(byteBuffer.position());			//5
	
	byteBuffer.rewind();
	System.out.println(byteBuffer.capacity());			//1024
	System.out.println(byteBuffer.limit());				//5
	System.out.println(byteBuffer.position());			//0
	
	byteBuffer.clear();
	System.out.println(byteBuffer.capacity());			//1024
	System.out.println(byteBuffer.limit());				//1024
	System.out.println(byteBuffer.position());			//0
	System.out.println((char) byteBuffer.get());		//a
	
	String str = "abcd";
	ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
	byteBuffer.put(str.getBytes());
	
	byteBuffer.flip();
	byte[] bytes = new byte[byteBuffer.limit()];
	byteBuffer.get(bytes,0,2);
	System.out.println(new String(bytes,0,2));			//ab
	System.out.println(byteBuffer.position());			//2
	
	byteBuffer.mark();
	byteBuffer.get(bytes,2,2);
	System.out.println(new String(bytes,2,2));			//cd
	System.out.println(byteBuffer.position());			//4
	
	byteBuffer.reset();
	System.out.println(byteBuffer.position());			//2
	
	if(byteBuffer.hasRemaining()){
		System.out.println(byteBuffer.remaining());		//2
	}

**直接缓冲区与非直接缓冲区别:**

- 非直接缓冲区:通过allocate()方法分配缓冲区,将缓冲区建立在JVM的内存中(在内存中进行复制搬运,因此速度不及直接缓冲区)
- 直接缓冲区:通过allocateDirect()方法分配直接缓冲区，将缓冲区建立在物理内存中，可以提高效率(在物理内存建立映射文件,速度快,但不能及时反映出处理结果,会有延时)

**通道(channel)**:用于源节点与目标节点的连接.在Java NIO中负责缓冲区中数据的传输Channel本身不储存数据，因此需要配合缓冲区进行传输

**二通道的主要实现类:**

	java.nio.channels.Channel接口：
		FileChannel
		SocketChannel
		ServerSocketChannel
		DatagramChannel

**三获取通道**

1Java针对支持通道的类提供了getChannel()方法

	本地IO:
		FileInputStream/FileOutputStream
		RandomAccessFile
	网络IO:
		Socket
		ServerSocket
		DatagramSocket
	
 	public static void test01(){
        FileInputStream fileInputStream = null;
        FileOutputStream fileOutputStream = null;
        FileChannel inpuChannel = null;
        FileChannel outChannel = null;

        try {
            fileInputStream = new FileInputStream("C:\\Users\\FYkf\\Desktop\\20160726165633.png");
            fileOutputStream = new FileOutputStream("C:\\Users\\FYkf\\Desktop\\test.png");

            inpuChannel = fileInputStream.getChannel();
            outChannel = fileOutputStream.getChannel();

            ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
            while (inpuChannel.read(byteBuffer)!=-1){
                byteBuffer.flip();
                outChannel.write(byteBuffer);
                byteBuffer.clear();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(outChannel!=null) {
                try {
                    outChannel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(inpuChannel!=null) {
                try {
                    inpuChannel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(fileOutputStream!=null)
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            if (fileInputStream!=null)
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    }
2在JDK1.7中的NIO.2针对各个通道提供了静态方法open()

	//使用直接缓冲区完成文件的复制(内存映射文件)
	 public static void test02(){
        try {
            FileChannel inChannel = FileChannel.open(Paths.get("C:\\Users\\FYkf\\Desktop\\20160726165633.png"), StandardOpenOption.READ);
            FileChannel outChannel = FileChannel.open(Paths.get("C:\\Users\\FYkf\\Desktop\\test.png"), StandardOpenOption.CREATE,StandardOpenOption.WRITE,StandardOpenOption.READ);

            MappedByteBuffer inMap = inChannel.map(FileChannel.MapMode.READ_ONLY, 0, inChannel.size());
            MappedByteBuffer outMap = outChannel.map(FileChannel.MapMode.READ_WRITE, 0, inChannel.size());

            byte[] bytes = new byte[inMap.limit()];
            inMap.get(bytes);
            outMap.put(bytes);

            outChannel.close();
            inChannel.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

3在JDK1.7中的NIO.2的Files工具类的newByteChannel()

**四通道之间的数据传输(直接缓冲区)**

transferForm()

transferTo()

	public static void test03() {
		try {
			FileChannel inChannel = (FileChannel) Files.newByteChannel(Paths.get("C:\\Users\\FYkf\\Desktop\\20160726165633.png"), StandardOpenOption.READ);
			FileChannel outChannel = (FileChannel) Files.newByteChannel(Paths.get("C:\\Users\\FYkf\\Desktop\\test.png"), StandardOpenOption.CREATE,StandardOpenOption.WRITE,StandardOpenOption.READ);
			outChannel.transferFrom(inChannel,0,inChannel.size());
			outChannel.close();
			inChannel.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

**五、分散(Scatter)与聚集(Gather)**

- 分散读取（Scattering Reads）：将通道中的数据分散到多个缓冲区中
- 聚集写入（Gathering Writes）：将多个缓冲区中的数据聚集到通道中

	    public static void test04(){
	    	try {
	    		RandomAccessFile f = new RandomAccessFile("C:\\Users\\FYkf\\Desktop\\Git.txt", "rw");
	    		FileChannel channel = f.getChannel();
	    		ByteBuffer allocate1 = ByteBuffer.allocate(100);
	    		ByteBuffer allocate2 = ByteBuffer.allocate(1024);
	    		ByteBuffer[] byteBuffers = {allocate1,allocate2};
	    		channel.read(byteBuffers);
	    		for (ByteBuffer b :byteBuffers){
	    			b.flip();
	    		}
	    		System.out.println(new String(byteBuffers[0].array(),0,byteBuffers[0].limit()));
	    		System.out.println("===============");
	    		System.out.println(new String(byteBuffers[1].array(),0,byteBuffers[1].limit()));
	    		RandomAccessFile fw = new RandomAccessFile("C:\\Users\\FYkf\\Desktop\\Git2.txt", "rw");
	    		FileChannel wChannel = fw.getChannel();
	    		wChannel.write(byteBuffers);
	    	} catch (IOException e) {
	    		e.printStackTrace();
	    	}
	    }

**字符集：Charset**

- 编码：字符串 -> 字节数组  encode(CharBuffer)
- 解码：字节数组  -> 字符串 decode(ByteBuffer)

		public static void test06(){
			 SortedMap<String, Charset> stringCharsetSortedMap = Charset.availableCharsets();
			Set<Map.Entry<String, Charset>> entries = stringCharsetSortedMap.entrySet();
			for (Map.Entry<String,Charset> entry:entries){
				System.out.println(entry.getKey() +":"+entry.getValue());
			}
		
			Charset charset = Charset.forName("UTF-8");
			CharsetEncoder charsetEncoder = charset.newEncoder();
			CharsetDecoder charsetDecoder = charset.newDecoder();
			String str="测试是没有错的";
			CharBuffer ch = CharBuffer.allocate(1024);
			ch.put(str);
			ch.flip();
		
			try {
				ByteBuffer encode = charsetEncoder.encode(ch);
				for (int i = 0; i < 21; i++) {
					System.out.println(encode.get());
				}
				encode.flip();
				CharBuffer decode = charsetDecoder.decode(encode);
			   System.out.println(decode.toString());
		
		
			} catch (CharacterCodingException e) {
				e.printStackTrace();
			}
		}

**使用 NIO 完成网络通信的三个核心：**

	1. 通道（Channel）：负责连接
	 java.nio.channels.Channel 接口：
	 		|--SelectableChannel
	 			|--SocketChannel
	 			|--ServerSocketChannel
	 			|--DatagramChannel
	 		Pipe管道:是2个线程之间的单向数据连接,Pipe有一个source通道和一个sink通道，数据会被写到sink通道,从source通道读取
	 			|--Pipe.SinkChannel
	 			|--Pipe.SourceChannel
	2. 缓冲区（Buffer）：负责数据的存取
	3. 选择器（Selector）：是 SelectableChannel 的多路复用器。用于监控 SelectableChannel 的 IO 状况
 
**FileChannel无论怎样都是阻塞的**

    // SocketChannel/ServerSocketChanne
    //客户端
    public void client() throws IOException{
    	//1. 获取通道
    	SocketChannel sChannel = SocketChannel.open(new InetSocketAddress("127.0.0.1", 9898));
    	//2. 切换非阻塞模式
    	sChannel.configureBlocking(false);
    	//3. 分配指定大小的缓冲区
    	ByteBuffer buf = ByteBuffer.allocate(1024);
    	//4. 发送数据给服务端
    	Scanner scan = new Scanner(System.in);
    	while(scan.hasNext()){
    		String str = scan.next();
    		buf.put((new Date().toString() + "\n" + str).getBytes());
    		buf.flip();
    		sChannel.write(buf);
    		buf.clear();
    	}
    	//5. 关闭通道
    	sChannel.close();
    }
    
    
    //服务端
    @Test
    public void server() throws IOException{
    	//1. 获取通道
    	ServerSocketChannel ssChannel = ServerSocketChannel.open();
    	//2. 切换非阻塞模式
    	ssChannel.configureBlocking(false);
    	//3. 绑定连接
    	ssChannel.bind(new InetSocketAddress(9898));
    	//4. 获取选择器
    	Selector selector = Selector.open();
    	//5. 将通道注册到选择器上, 并且指定“监听接收事件”
    	ssChannel.register(selector, SelectionKey.OP_ACCEPT);
    	//6. 轮询式的获取选择器上已经“准备就绪”的事件
    	while(selector.select() > 0){	
    		//7. 获取当前选择器中所有注册的“选择键(已就绪的监听事件)”
    		Iterator<SelectionKey> it = selector.selectedKeys().iterator();	
    		while(it.hasNext()){
    			//8. 获取准备“就绪”的是事件
    			SelectionKey sk = it.next();	
    			//9. 判断具体是什么事件准备就绪
    			if(sk.isAcceptable()){
    				//10. 若“接收就绪”，获取客户端连接
    				SocketChannel sChannel = ssChannel.accept();		
    				//11. 切换非阻塞模式
    				sChannel.configureBlocking(false);		
    				//12. 将该通道注册到选择器上
    				sChannel.register(selector, SelectionKey.OP_READ);
    			}else if(sk.isReadable()){
    				//13. 获取当前选择器上“读就绪”状态的通道
    				SocketChannel sChannel = (SocketChannel) sk.channel();		
    				//14. 读取数据
    				ByteBuffer buf = ByteBuffer.allocate(1024);		
    				int len = 0;
    				while((len = sChannel.read(buf)) > 0 ){
    					buf.flip();
    					System.out.println(new String(buf.array(), 0, len));
    					buf.clear();
    				}
    			}	
    			//15. 取消选择键 SelectionKey
    			it.remove();
    		}
    	}
    }
    
    
    //DatagramChannel
    public void send() throws IOException{
    	DatagramChannel dc = DatagramChannel.open();
    	dc.configureBlocking(false);
    	ByteBuffer buf = ByteBuffer.allocate(1024);
    	Scanner scan = new Scanner(System.in);
    	while(scan.hasNext()){
    		String str = scan.next();
    		buf.put((new Date().toString() + ":\n" + str).getBytes());
    		buf.flip();
    		dc.send(buf, new InetSocketAddress("127.0.0.1", 9898));
    		buf.clear();
    	}
    	dc.close();
    }
    
    
    public void receive() throws IOException{
    	DatagramChannel dc = DatagramChannel.open();
    	dc.configureBlocking(false);
    	dc.bind(new InetSocketAddress(9898));
    	Selector selector = Selector.open();
    	dc.register(selector, SelectionKey.OP_READ);
    	while(selector.select() > 0){
    		Iterator<SelectionKey> it = selector.selectedKeys().iterator();
    		while(it.hasNext()){
    			SelectionKey sk = it.next();	
    			if(sk.isReadable()){
    				ByteBuffer buf = ByteBuffer.allocate(1024);
    				dc.receive(buf);
    				buf.flip();
    				System.out.println(new String(buf.array(), 0, buf.limit()));
    				buf.clear();
    			}
    		}
    		it.remove();
    	}
    }
    
    
    //Pipe
    public void test1() throws IOException{
    	//1. 获取管道
    	Pipe pipe = Pipe.open();
    	//2. 将缓冲区中的数据写入管道
    	ByteBuffer buf = ByteBuffer.allocate(1024);
    	Pipe.SinkChannel sinkChannel = pipe.sink();
    	buf.put("通过单向管道发送数据".getBytes());
    	buf.flip();
    	sinkChannel.write(buf);
    	//3. 读取缓冲区中的数据
    	Pipe.SourceChannel sourceChannel = pipe.source();
    	buf.flip();
    	int len = sourceChannel.read(buf);
    	System.out.println(new String(buf.array(), 0, len));
    	sourceChannel.close();
    	sinkChannel.close();
}