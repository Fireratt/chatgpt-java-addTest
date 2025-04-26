package com.plexpt.chatgpt;

import com.plexpt.chatgpt.util.Proxys;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.net.Proxy;

import static org.junit.jupiter.api.Assertions.*;

class ProxysTest {

    @Test
    void testHttpProxy() {
        String ip = "127.0.0.1";
        int port = 8080;
        Proxy proxy = Proxys.http(ip, port);

        assertNotNull(proxy, "Proxy should not be null");
        assertEquals(Proxy.Type.HTTP, proxy.type(), "Proxy type should be HTTP");

        InetSocketAddress address = (InetSocketAddress) proxy.address();
        assertEquals(ip, address.getHostString(), "IP address should match");
        assertEquals(port, address.getPort(), "Port should match");
    }

    @Test
    void testSocks5Proxy() {
        String ip = "192.168.1.1";
        int port = 1080;
        Proxy proxy = Proxys.socks5(ip, port);

        assertNotNull(proxy, "Proxy should not be null");
        assertEquals(Proxy.Type.SOCKS, proxy.type(), "Proxy type should be SOCKS");

        InetSocketAddress address = (InetSocketAddress) proxy.address();
        assertEquals(ip, address.getHostString(), "IP address should match");
        assertEquals(port, address.getPort(), "Port should match");
    }
}

