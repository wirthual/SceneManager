package com.adfmanager;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;

@Configuration
@EnableResourceServer
public class ResourceServerConfig
    extends ResourceServerConfigurerAdapter {
	
	@Override
	public void configure(ResourceServerSecurityConfigurer resources) {
		resources.stateless(false); //https://github.com/spring-projects/spring-security-oauth/issues/907
	}
	
  @Override
  public void configure(HttpSecurity http) throws Exception {
    http.
	// Since we want the protected resources to be accessible in the UI as well we need 
	// session creation to be allowed (it's disabled by default in 2.0.6)
    sessionManagement().sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED).
      and().requestMatchers()
      .antMatchers("/api/adf/user","/api/file/upload/adf").and() //Set ressources to protect here
      .authorizeRequests().anyRequest().authenticated();
  }
}


