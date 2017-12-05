package com.github.ji4597056.server;

import com.github.ji4597056.WsForwardProperties;
import com.github.ji4597056.utils.CommonUtils;
import com.github.ji4597056.utils.WebsocketConstant;
import io.netty.util.CharsetUtil;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.util.UriUtils;

/**
 * eureka forward handler
 *
 * @author Jeffrey
 * @since 2017/11/29 13:09
 */
public class DiscoveryForwardHandler extends AbstractWsServerHandler {

    /**
     * init flag
     */
    private volatile boolean dirty = true;

    /**
     * key:patternComparator,value:url path
     */
    private Map<String, ServiceHandler> serviceMap = new ConcurrentHashMap<>();

    private PathMatcher matcher = new AntPathMatcher();

    private DiscoveryClient discoveryClient;

    private WsForwardProperties wsForwardProperties;

    public DiscoveryForwardHandler(DiscoveryClient discoveryClient,
        WsForwardProperties wsForwardProperties) {
        this.discoveryClient = discoveryClient;
        this.wsForwardProperties = wsForwardProperties;
    }

    @Override
    public String getForwardUrl(WebSocketSession session) {
        init();
        ServiceHandler handler = getServiceId(session.getUri().getPath());
        String address = getLoadBalanceInstance(handler);
        return getWsForwardUrl(address, handler, session.getUri());
    }

    /**
     * get forward url
     *
     * @param address address
     * @param handler ForwardHandler
     * @param uri uri
     * @return forward url
     */
    private String getWsForwardUrl(String address, ServiceHandler handler, URI uri) {
        String prefix = Optional.ofNullable(handler.getForwardPrefix()).orElse("/");
        try {
            String query = UriUtils.encodeQuery(uri.getQuery(), CharsetUtil.UTF_8.name());
            String url = UriUtils.encodePath(handler.getPrefix() == null ? uri.getPath()
                    : uri.getPath().substring(handler.getPrefix().length()),
                CharsetUtil.UTF_8.name());
            if (query == null) {
                return WebsocketConstant.WS_SCHEME + ":/" + prefix + address + url;
            }
            return WebsocketConstant.WS_SCHEME + ":/" + prefix + address + url + "?" + query;
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * get ForwardHandler
     *
     * @param uri uri
     * @return service id
     */
    private ServiceHandler getServiceId(String uri) {
        return serviceMap.entrySet().stream().filter(entry -> matcher.match(entry.getKey(), uri))
            .findFirst().map(Entry::getValue)
            .orElseThrow(
                () -> new IllegalStateException("Can't find matching patterns for " + uri));
    }

    /**
     * get loadbance ServiceInstance
     *
     * @param handler forward handler
     * @return address
     */
    private String getLoadBalanceInstance(ServiceHandler handler) {
        List<String> addresses;
        if (discoveryClient != null && handler.getServiceId() != null) {
            // if service id isn't null, get address from discoveryClient
            List<ServiceInstance> instances = discoveryClient.getInstances(handler.getServiceId());
            if (instances == null || instances.isEmpty()) {
                throw new IllegalStateException("Can't find service id for " + handler.getId());
            }
            addresses = instances.stream()
                .map(instance -> instance.getHost() + ":" + instance.getPort()).collect(
                    Collectors.toList());
        } else if (handler.getListOfServers() != null && handler.getListOfServers().length > 0) {
            // if service-id is null, get addresses from listOfServers
            addresses = Arrays.asList(handler.getListOfServers());
        } else {
            throw new IllegalStateException(
                "Can't find service id or listOfServers for " + handler.getId());
        }
        return addresses
            .get(CommonUtils.getLoadBlanceIndex(handler.getCounter(), addresses.size()));
    }

    /**
     * init serviceMap
     */
    private void init() {
        if (dirty) {
            synchronized (this) {
                if (this.dirty) {
                    wsForwardProperties.getHandlers().forEach((id, handler) -> {
                        if (handler.isEnabled()) {
                            serviceMap.put(CommonUtils.getWsPattern(handler),
                                new ServiceHandler(id, handler.getServiceId(),
                                    handler.getListOfServers(), handler.getPrefix(),
                                    handler.getForwardPrefix()));
                        }
                    });
                    this.dirty = false;
                }
            }
        }
    }

    static class ServiceHandler {

        private final String id;

        private final AtomicInteger counter;

        private final String serviceId;

        private final String[] listOfServers;

        private final String prefix;

        private final String forwardPrefix;

        public ServiceHandler(String id, String serviceId, String[] listOfServers, String prefix,
            String forwardPrefix) {
            this.id = id;
            this.serviceId = serviceId;
            this.listOfServers = listOfServers;
            this.prefix = prefix;
            this.forwardPrefix = forwardPrefix;
            this.counter = new AtomicInteger();
        }

        public String getId() {
            return id;
        }

        public String getServiceId() {
            return serviceId;
        }

        public String[] getListOfServers() {
            return listOfServers;
        }

        public String getPrefix() {
            return prefix;
        }

        public String getForwardPrefix() {
            return forwardPrefix;
        }

        public AtomicInteger getCounter() {
            return counter;
        }
    }
}
