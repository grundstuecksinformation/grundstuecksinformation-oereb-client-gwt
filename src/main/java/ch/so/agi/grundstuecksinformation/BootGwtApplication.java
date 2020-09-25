package ch.so.agi.grundstuecksinformation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import ch.so.agi.grundstuecksinformation.server.EgridServiceImpl;
import ch.so.agi.grundstuecksinformation.server.ExtractServiceImpl;
import ch.so.agi.grundstuecksinformation.server.SettingsServiceImpl;

@ServletComponentScan
@SpringBootApplication
@Configuration
public class BootGwtApplication {
	public static void main(String[] args) {
		SpringApplication.run(BootGwtApplication.class, args);
	}
  
    @Bean
    public ServletRegistrationBean egridServletBean() {
        ServletRegistrationBean bean = new ServletRegistrationBean(new EgridServiceImpl(), "/module1/egrid");
        bean.setLoadOnStartup(1);
        return bean;
    }  
    
    @Bean
    public ServletRegistrationBean extractServletBean() {
        ServletRegistrationBean bean = new ServletRegistrationBean(new ExtractServiceImpl(), "/module1/extract");
        bean.setLoadOnStartup(1);
        return bean;
    }     
    
    @Bean
    public ServletRegistrationBean settingsServletBean() {
        ServletRegistrationBean bean = new ServletRegistrationBean(new SettingsServiceImpl(), "/module1/settings");
        bean.setLoadOnStartup(1);
        return bean;
    }
}
