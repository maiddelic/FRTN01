package server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class Server {
	private static String clientChannel = "clientChannel";
	private static String serverChannel = "serverChannel";
	private static String channelType = "channelType";

	/**
	 * 
	 * Server side. Listens to the channel to receive data. Sends control data
	 * to the client.
	 * 
	 */
	public static void main(String[] args) {
		try {
			int port = 4446;
			String host = "localhost"; // Add IP

			Regulator reg = new Regulator();

			ServerSocketChannel channel = ServerSocketChannel.open();
			channel.socket().bind(new InetSocketAddress(host, port));
			channel.configureBlocking(false);

			Selector selector;
			selector = Selector.open();
			SelectionKey socketServerSelectionKey = channel.register(selector,
					SelectionKey.OP_ACCEPT);
			Map<String, String> properties = new HashMap<String, String>();
			properties.put(channelType, serverChannel);
			socketServerSelectionKey.attach(properties);
			for (;;) {
				if (selector.select() == 0)
					continue;
				Set<SelectionKey> selectedKeys = selector.selectedKeys();
				Iterator<SelectionKey> iterator = selectedKeys.iterator();
				while (iterator.hasNext()) {
					SelectionKey key = iterator.next();
					if (((Map<String, String>) key.attachment()).get(
							channelType).equals(serverChannel)) {
						ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key
								.channel();
						SocketChannel clientSocketChannel = serverSocketChannel
								.accept();

						if (clientSocketChannel != null) {
							clientSocketChannel.configureBlocking(false);
							SelectionKey clientKey = clientSocketChannel
									.register(selector, SelectionKey.OP_READ,
											SelectionKey.OP_WRITE);
							Map<String, String> clientproperties = new HashMap<String, String>();
							clientproperties.put(channelType, clientChannel);
							clientKey.attach(clientproperties);
						}

					} else {
						ByteBuffer buffer = ByteBuffer.allocate(8);
						CharBuffer control;
						byte[] bytes;
						SocketChannel clientChannel = (SocketChannel) key
								.channel();
						String s;
						int bytesRead = 0;
						if (key.isReadable()) {

							// Reading from the Client
							if ((bytesRead = clientChannel.read(buffer)) > 0) {
								buffer.flip();
								byte[] b = buffer.array();
								System.out.println(clientChannel.isOpen());

								// Calculating the control signal
								String s2 = reg.set(b);

								control = CharBuffer.wrap(s2);
								buffer.clear();

								// Writing to the Client/Raspberry Pi
								while (control.hasRemaining()) {
									clientChannel.write(Charset
											.defaultCharset().encode(control));
								}
							}
							if (bytesRead < 0) {
								clientChannel.socket().close();
								clientChannel.close();
							}
						}

					}
					iterator.remove();

				}
			}
		} catch (IOException e) {

			e.printStackTrace();
		}

	}
}