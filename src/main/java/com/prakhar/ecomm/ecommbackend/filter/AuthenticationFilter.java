package com.prakhar.ecomm.ecommbackend.filter;

import org.apache.tomcat.util.http.parser.Authorization;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

//public class AuthenticationFilter implements Filter {
//    @Override
//    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
//        HttpServletRequest request = (HttpServletRequest) servletRequest;
//        String token = request.getHeader("Authorization");
//    }
//
//    @Override
//    public void init(FilterConfig filterConfig) throws ServletException {
//        Filter.super.init(filterConfig);
//    }
//
//    @Override
//    public void destroy() {
//        Filter.super.destroy();
//    }
//
//    private String getAuthToken(String token) throws Exception{
//        String splitToken[] = token.split(" ");
//        if (splitToken.length != 2){
//            throw new Exception("Invalid authorization token");
//        }
//    }
//}
