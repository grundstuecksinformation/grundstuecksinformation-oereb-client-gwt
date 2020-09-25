package ch.so.agi.grundstuecksinformation;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

@Component
public class GWTCacheControlFilter implements Filter {

    // TODO: understand GWT caching
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String requestURI = httpRequest.getRequestURI();

        if (requestURI.contains(".nocache.")) {
            Date now = new Date();
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            httpResponse.setDateHeader("Date", now.getTime());
            // one day old
            httpResponse.setDateHeader("Expires", now.getTime() - 86400000L);
            httpResponse.setHeader("Pragma", "no-cache");
            httpResponse.setHeader("Cache-control", "no-cache, no-store, must-revalidate");
        }
        
        // FIXME
        if (requestURI.contains(".css.")) {
            Date now = new Date();
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            httpResponse.setDateHeader("Date", now.getTime());
            httpResponse.setDateHeader("Expires", now.getTime());
            httpResponse.setHeader("Pragma", "no-cache");
            httpResponse.setHeader("Cache-control", "no-cache, no-store, must-revalidate");
        }
        
        if (requestURI.contains(".cache.")) {
//            Date now = new Date();
            HttpServletResponse httpResponse = (HttpServletResponse) response;
//            httpResponse.setDateHeader("Date", now.getTime());
//            httpResponse.setDateHeader("Expires", now.getTime());
//            httpResponse.setHeader("Pragma", "no-cache");
            httpResponse.setHeader(HttpHeaders.CACHE_CONTROL, CacheControl.maxAge(1, TimeUnit.DAYS).getHeaderValue());
        }
        
        chain.doFilter(request, response);
    }

}
