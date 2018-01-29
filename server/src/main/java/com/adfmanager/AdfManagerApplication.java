package com.adfmanager;

import java.io.File;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.embedded.ConfigurableEmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;



@EnableAutoConfiguration
@ComponentScan
public class AdfManagerApplication extends SpringBootServletInitializer {
	
	public static final String STORAGE_ROOT = System.getProperty("user.home")+File.separator+"SceneManager";
	public static final String STORAGE_SCENE = STORAGE_ROOT+File.separator+"Scene";
	public static final String STORAGE_ADF = STORAGE_ROOT+File.separator+"ADF";

	
    public static void main(final String[] args) {
    	File sceneDir = new File(STORAGE_SCENE);
    	if(!sceneDir.exists()){
    		sceneDir.mkdirs();
    		System.out.println("Directory "+sceneDir.getAbsolutePath()+" created");
    	}
    	File adfDir = new File(STORAGE_ADF);
    	if(!adfDir.exists()){
    		adfDir.mkdirs();
    		System.out.println("Directory "+adfDir.getAbsolutePath()+" created");
    	}
    	
        SpringApplication.run(AdfManagerApplication.class, args);
    }
    
    @Bean 
	  EmbeddedServletContainerCustomizer containerCustomizer(
	        ) throws Exception {
	 
	      
	      return (ConfigurableEmbeddedServletContainer container) -> {
	 
	          if (container instanceof TomcatEmbeddedServletContainerFactory ) {
	 
	              TomcatEmbeddedServletContainerFactory tomcat = (TomcatEmbeddedServletContainerFactory) container;
	              tomcat.addConnectorCustomizers(
	                      (connector) -> {
	                    	  connector.setMaxPostSize(100000000);//100MB
	                      }
	              );
	          }
	      };
}
  
}



