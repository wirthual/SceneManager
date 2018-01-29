package com.adfmanager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.autoconfigure.security.oauth2.client.EnableOAuth2Sso;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationManager;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationProcessingFilter;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

@Configuration
@EnableOAuth2Sso
@Order(SecurityProperties.ACCESS_OVERRIDE_ORDER) //https://spring.io/guides/topicals/spring-security-architecture/
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired
	private ResourceServerTokenServices tokenServices;
	
	@Override
	 protected void configure(HttpSecurity http) throws Exception {
	    http
	      .csrf().disable()//Security issue?
	      .antMatcher("/**")
	      .authorizeRequests()
	        .antMatchers("/", "/login/**","/images/*","/webjars/**","/overview","/fordeveloper","/about","/docapi/**","/api/**","/videos/**").permitAll()
	        .anyRequest()
	        .authenticated().and().addFilterAfter(new ApiTokenAccessFilter(tokenServices), AbstractPreAuthenticatedProcessingFilter.class).logout().logoutSuccessUrl("/").permitAll();
	 }
	
	
}


