package com.github.ji4597056;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

/**
 * websocket forward properties
 *
 * @author Jeffrey
 * @since 2017/11/29 11:17
 */
@ConfigurationProperties("ws.forward")
public class WsForwardProperties {

    /**
     * use for prefix and forwardPrefix
     */
    private static final String URL_START_PREFIX = "/";

    /**
     * enabled
     */
    private boolean enabled = true;

    /**
     * key:id, value:ForwardHandler
     */
    private Map<String, ForwardHandler> handlers = new HashMap<>(5);

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Map<String, ForwardHandler> getHandlers() {
        return handlers;
    }

    public void setHandlers(
        Map<String, ForwardHandler> handlers) {
        this.handlers = handlers;
    }

    @PostConstruct
    public void init() {
        handlers.forEach((key, handler) -> {
            if (!StringUtils.hasText(handler.getId())) {
                handler.id = key;
            }
        });
    }

    public static class ForwardHandler {

        /**
         * enabled
         */
        private boolean enabled = true;

        /**
         * id
         */
        private String id;

        /**
         * route prefix
         */
        private String prefix;

        /**
         * forward prefix
         */
        private String forwardPrefix;

        /**
         * websocket handler uri
         */
        private String uri;

        /**
         * service id,used for finding service address from DiscoveryClient
         */
        private String serviceId;

        /**
         * withJs
         */
        private boolean withJs;

        /**
         * list of servers
         */
        private String[] listOfServers;

        /**
         * allowedOrigins
         */
        private String[] allowedOrigins;

        /**
         * bean name of AbstractWsServerHandler
         * if null,use global ForwardHandler.
         */
        private String handlerClass;

        /**
         * bean name array of HandshakeInterceptor.
         * if null,use global HandshakeInterceptor.
         */
        private String[] interceptorClasses;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getPrefix() {
            if (prefix == null || prefix.startsWith(URL_START_PREFIX)) {
                return prefix;
            }
            return URL_START_PREFIX + prefix;
        }

        public void setPrefix(String prefix) {
            this.prefix = prefix;
        }

        public String getForwardPrefix() {
            if (forwardPrefix == null || forwardPrefix.startsWith(URL_START_PREFIX)) {
                return forwardPrefix;
            }
            return URL_START_PREFIX + forwardPrefix;
        }

        public void setForwardPrefix(String forwardPrefix) {
            this.forwardPrefix = forwardPrefix;
        }

        public String getUri() {
            return uri;
        }

        public void setUri(String uri) {
            this.uri = uri;
        }

        public String getServiceId() {
            return serviceId;
        }

        public void setServiceId(String serviceId) {
            this.serviceId = serviceId;
        }

        public boolean isWithJs() {
            return withJs;
        }

        public void setWithJs(boolean withJs) {
            this.withJs = withJs;
        }

        public String[] getListOfServers() {
            return listOfServers;
        }

        public void setListOfServers(String[] listOfServers) {
            this.listOfServers = listOfServers;
        }

        public String[] getAllowedOrigins() {
            return allowedOrigins;
        }

        public void setAllowedOrigins(String[] allowedOrigins) {
            this.allowedOrigins = allowedOrigins;
        }

        public String getHandlerClass() {
            return handlerClass;
        }

        public void setHandlerClass(String handlerClass) {
            this.handlerClass = handlerClass;
        }

        public String[] getInterceptorClasses() {
            return interceptorClasses;
        }

        public void setInterceptorClasses(String[] interceptorClasses) {
            this.interceptorClasses = interceptorClasses;
        }
    }
}
