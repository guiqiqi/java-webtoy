## WebToy - 一个简单的玩具 Web 框架

这个项目是一个简单的基于 Java 21 的 HTTP/1.1 协议框架，提供了一个基于 Selector 的 TCPServer，能够同时支持多个客户端连接。该框架实现了 HTTP/1.1 协议支持的全部方法，并根据 RFC 2616 标准，在遇到错误时返回相应的错误代码。

### 功能

- 支持 HTTP/1.1 协议：该框架完全实现了 HTTP/1.1 协议的规范，包括请求方法、状态码、请求头、响应头等；
- 多客户端支持：基于 Selector 的 TCPServer 可以同时处理多个客户端连接，提高了并发处理能力；
- 错误处理：当遇到错误时，框架会根据 RFC 2616 返回相应的错误代码，提供了良好的错误处理机制；
- 简单易用：你只需要将自己的 Handler 绑定到一个 Endpoint 上，就可以启动一个 Server。

### 项目结构

```
├── src/                     # 源代码目录
│   └── main.java.webtoy/      # 框架相关代码
│       ├── App.java             # 一个简单的使用实例
│       ├── Application.java     # 框架应用实现
│       ├── Server.java          # 基于 Selector 的 TCPServer 实现
│       ├── HTTPMethod.java      # 所有 HTTP/1.1 支持的方法 Enum
│       ├── Request.java         # 将字节流解析成 HTTP Request 相关实现
│       ├── Response.java        # HTTP Response 相关实现
│       ├── Router.java          # Endpoint 路由相关实现
│       └── Handler.java         # Handler 接口定义
├── test/                    # 测试代码目录
│   └── main.java.webtoy/      # 服务器测试代码
│       └── AppTest.java         # 单元测试
├── pom.xml                  # Maven 项目配置文件
└── README.md                # 项目说明文档
```

### 快速开始

以下是一段简单的示例代码，演示如何使用该框架创建一个简单的 HTTP 服务器：

```java
Application app = new Application();
Handler handler = (request) -> {
    if (request.method == HTTPMethod.GET)
        return new Response(request.args.toString());
    if (request.method == HTTPMethod.POST)
        return new Response(request.forms.toString());
    return new Response(404);  // Never reached
};
app.route("/foo", List.of(HTTPMethod.GET, HTTPMethod.POST), handler);
try {
    Server server = new Server("localhost", 9009, app);
    Runtime.getRuntime().addShutdownHook(new Thread() {
        @Override
        public void run() {
            System.out.println("\nHave a nice day :)");
            server.close();
        }
    });
    server.start();
} catch (IOException error) {
    System.out.println("error in tcp");
    return;
}
```

这段代码启动了一个 HTTP 服务器，并在 `/foo` 这个 Endpoint 绑定了一个 Handler，该函数会将请求的查询字符串/POST Form数据返回给客户端 —— 这取决于请求方法是 GET 还是 POST。

在退出服务器时，服务器会断开所有已经建立连接的客户端，并祝你心情愉快 :)