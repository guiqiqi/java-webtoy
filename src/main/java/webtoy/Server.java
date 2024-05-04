package webtoy;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.Selector;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;

import webtoy.Request.InvalidRequest;

/**
 * A simple fixed-length queue for matching two CRLFs in header.
 */
class BoundedQueue<T> {
    private final Integer maxsize;
    private final List<T> items;

    public BoundedQueue(Integer maxsize, T[] initial) {
        this.maxsize = maxsize;
        this.items = new ArrayList<>();
        for (Integer index = 0; index < maxsize; index++)
            this.add(initial[index]);
    }

    /**
     * Return if contained data equals to another array, length of given data array must be maxsize.
     * @param target need to be compared
     * @return if data equaled
     */
    public Boolean equals(T[] target) {
        for (Integer index = 0; index < this.maxsize; index++)
            if (this.items.get(index) != target[index])
                return false;
        return true;
    }

    public Boolean add(T item) {
        if (this.items.size() == maxsize)
            this.items.remove(0);
        return this.items.add(item);
    }
}

public class Server {
    private final Selector selector;
    private final ServerSocketChannel listener;
    private final Application application;
    private final Set<SocketChannel> connections;
    private Boolean status;

    public final InetSocketAddress address;

    // Max data size for a single HTTP/1.1 request header
    public static Integer MaxRequestHeaderSize = 8190;
    public static final Integer MaxRequestBodySize = 1024 * 1024 * 10;

    public Server(String address, Integer port, Application application) throws IOException {
        this.listener = ServerSocketChannel.open();
        this.address = new InetSocketAddress(address, port);
        this.listener.bind(this.address);
        this.listener.configureBlocking(false);
        this.selector = Selector.open();
        this.listener.register(selector, SelectionKey.OP_ACCEPT);

        // Private variables
        this.status = false;
        this.connections = new HashSet<>();
        this.application = application;
    }

    /**
     * Serve request from client.
     * 
     * Here using CRLF = "\r\n";
     * 
     * Read data from client socket to header buffer firstly, 
     * the max size should be MaxRequestHeaderSize, which is 8192 bytes.
     * The header should contain: **request line, headers, CRLF**.
     * 
     * If header size >= 8192, means in buffer we could not find CRLF in buffer,
     * then we throw an HTTP 431 (request header is too large) to client and close connection;
     * 
     * If header size smaller than 8192, means some part of body data we cached in buffer,
     * we need to find the DOUBLE CRLF pass the first part as header to Request,
     * then the header will be parsed by Request for knowning Content-Length indicates body size.
     * 
     * The rest length we should read from client socket will be calculated by Request,
     * then we combine the body in header and rest of body line together for adding into request.
     * 
     * @param key is select key contains socket channel from client
     */
    private void serve(SelectionKey key) throws IOException {
        SocketChannel client = (SocketChannel) key.channel();
        ByteBuffer headerBuffer = ByteBuffer.allocate(MaxRequestHeaderSize);
        Integer headerRead = client.read(headerBuffer);

        // If client closed connection
        if (headerRead == -1) {
            this.disconnectClient(key);
        }

        // Read until got double CRLF
        Integer headerSize = findUntil2CRLF(headerBuffer) + 1;
        if (headerSize == -1) {
            this.log(client, null, 451);
            client.write(ByteBuffer.wrap(Response.HeaderTooLargeResponse.getBytes()));
            this.disconnectClient(key);
        }
        String header = new String(headerBuffer.array(), 0, headerSize);

        // Try to parse header of request and get Content-Length
        Request request = null;
        try {
            request = new Request(header);
        } catch (InvalidRequest error) {
            this.log(client, null, 411);
            client.write(ByteBuffer.wrap(Response.LengthRequiredResponse.getBytes()));
            this.disconnectClient(key);
        }

        // Try to get last part of body if it
        Integer bodysize = request.bodysize;
        if (bodysize >= MaxRequestBodySize) {
            this.log(client, request, 413);
            client.write(ByteBuffer.wrap(Response.ContentTooLargeResponse.getBytes()));
            this.disconnectClient(key);
        }
        String body = new String(headerBuffer.array(), headerSize, headerBuffer.limit() - headerSize);
        Integer restLengthOfBody = bodysize - (headerBuffer.limit() - headerSize);
        if (restLengthOfBody > 0) {
            ByteBuffer bodyBuffer = ByteBuffer.allocate(restLengthOfBody);
            Integer bodyRead = client.read(bodyBuffer);
            if (bodyRead == -1)
                this.disconnectClient(key);
            body += new String(bodyBuffer.array(), 0, restLengthOfBody);
        }

        // Attach body into request
        try {
            request.parseBody(body);
        } catch (InvalidRequest error) {
            this.log(client, request, 400);
            client.write(ByteBuffer.wrap(Response.InvalidRequestResponse.getBytes()));
            this.disconnectClient(key);
        }

        // Application handle this request and generate response
        Response response = this.application.respond(request);
        try {
            this.log(client, request, response);
            client.write(ByteBuffer.wrap(response.toString().getBytes()));
        } catch (IOException error) {
            this.disconnectClient(key);
        }
    }

    /**
     * Disconnect client socket connection and cancel listen on given key.
     * @param key of current event from selector
     */
    private void disconnectClient(SelectionKey key) {
        SocketChannel client = (SocketChannel) key.channel();
        try {
            key.cancel();
            client.close();
        } catch (IOException error) {
            // Do nothing here
        }
        if (this.connections.contains(client))
            this.connections.remove(client);
    }

    /**
     * Return end index of 2 "\r\n" in byte buffer, if not contains, return -1.
     * @param buffer read from client socket
     * @return end index of 2 CRLF in byte buffer
     */
    private static Integer findUntil2CRLF(ByteBuffer buffer) {
        buffer.flip();
        BoundedQueue<Byte> queue = new BoundedQueue<>(4, new Byte[] { 0, 0, 0, 0 });
        Byte[] CRLFs = { 13, 10, 13, 10 };
        for (Integer index = 0; index < buffer.limit(); index++) {
            queue.add(buffer.get());
            if (queue.equals(CRLFs))
                return index;
        }
        buffer.flip();
        return -1;
    }

    /**
     * Accept new connection from client.
     * @param key is select key contains socket channel from client
     */
    private void accept(SelectionKey key) throws IOException {
        SocketChannel client = this.listener.accept();
        client.configureBlocking(false);
        client.register(this.selector, SelectionKey.OP_READ);
        this.connections.add(client);
    }

    /**
     * Start server and waiting events from selector.
     */
    public void start() {
        while (true) {
            try {
                this.selector.select();
            } catch (ClosedSelectorException error) {
                return;
            } catch (IOException error) {
                return;
            }
            this.status = true;

            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> iter = selectedKeys.iterator();
            while (iter.hasNext()) {
                SelectionKey key = iter.next();
                try {
                    // Create a new connection for serving client
                    if (key.isAcceptable())
                        this.accept(key);

                    // Serving client's request
                    if (key.isReadable())
                        this.serve(key);
                } catch (IOException error) {
                    // Key already canceld, so do nothing
                    continue;
                }
                iter.remove();
            }
        }
    }

    /**
     * Close server and close all client connections.
     */
    public void close() {
        try {
            this.listener.close();
            for (SocketChannel connection : this.connections)
                connection.close();
        } catch (IOException error) {
            return;
        }
    }

    /**
     * Shortcut of log for response.
     */
    private void log(SocketChannel client, Request request, Response response) {
        this.log(client, request, response.code);
    }

    /**
     * Log server action.
     * @param client socket channel
     * @param request parsed from client
     * @param response code generated from application
     */
    private void log(SocketChannel client, Request request, Integer responseCode) {
        String url = "?";
        String method = "?";
        if (request != null) {
            url = request.url;
            method = request.method.toString();
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        try {
            System.out.println(String.format("%s %s - %s %s > %d",
                    formatter.format(now),
                    client.getRemoteAddress().toString().substring(1),
                    method,
                    url,
                    responseCode));
        } catch (IOException error) {
            return;
        }
    }

    /**
     * Check if server is running.
     * @return if server is running
     */
    public Boolean running() {
        return this.status;
    }

}
