package com.example.gateway.config;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.pattern.PathPattern;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Map;

@Component
public class RequestGlobalFilter implements GlobalFilter, Ordered {
    @Override
    public int getOrder() {
        return -100;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        URI uri = request.getURI();
        String scheme = uri.getScheme();
        int port = uri.getPort();
        String host = uri.getHost();
        String path = uri.getPath();
        path = path.replaceFirst("edu.php","Edu");

        exchange.getAttributes().put("startTime",System.currentTimeMillis());
        String ip = request.getRemoteAddress().getAddress().getHostAddress();
        URI nUri = URI.create(scheme+"://"+host+":"+port+path);


        ServerHttpRequest newRequest = request.mutate().uri(nUri).build();
        newRequest = new ServerHttpRequestDecorator(newRequest);
        ServerWebExchange newExchange = exchange.mutate().request(newRequest).build();
        exchange = newExchange;
        ServerWebExchange finalExchange = exchange;
        return chain.filter(exchange).then(Mono.fromRunnable(()->{
            Long startTime = finalExchange.getAttribute("startTime");
            if(null != startTime){
                StringBuilder sb = new StringBuilder(finalExchange.getRequest().getURI().getRawPath())
                        .append(": ")
                        .append(System.currentTimeMillis()-startTime)
                        .append("ms");
                System.out.println(sb.toString());
            }
        }));
    }
}
