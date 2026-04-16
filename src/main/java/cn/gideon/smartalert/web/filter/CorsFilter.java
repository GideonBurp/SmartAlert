package cn.gideon.smartalert.web.filter;

import cn.gideon.smartalert.common.response.Result;
import com.alibaba.fastjson2.JSON;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * CORS跨域过滤器
 */
@Slf4j
@Component
@Order(0)
public class CorsFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        
        // 允许跨域的域名
        httpResponse.setHeader("Access-Control-Allow-Origin", "*");
        // 允许的HTTP方法
        httpResponse.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        // 允许的请求头
        httpResponse.setHeader("Access-Control-Allow-Headers", "*");
        // 允许携带凭证
        httpResponse.setHeader("Access-Control-Allow-Credentials", "true");
        // 预检请求的有效期（秒）
        httpResponse.setHeader("Access-Control-Max-Age", "3600");
        
        // 处理OPTIONS预检请求
        if ("OPTIONS".equalsIgnoreCase(httpRequest.getMethod())) {
            httpResponse.setStatus(HttpServletResponse.SC_OK);
            return;
        }
        
        chain.doFilter(request, response);
    }
}
