package webtoy;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.Selector;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.LinkedList;

public class Server {
    private final Selector selector;
    private final ServerSocketChannel listener;
    private final List<SocketChannel> connections;
    private final Application application;

    public final InetSocketAddress address;

    public Server(String address, Integer port, Application application) throws IOException {
        this.listener = ServerSocketChannel.open();
        this.address = new InetSocketAddress(address, port);
        this.listener.bind(this.address);
        this.listener.configureBlocking(false);
        this.selector = Selector.open();
        this.listener.register(selector, SelectionKey.OP_ACCEPT);

        // Private variables
        this.connections = new LinkedList<>();
        this.application = application;
    }

}
