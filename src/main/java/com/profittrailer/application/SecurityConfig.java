package com.profittrailer.application;

import com.profittrailer.interceptors.CustomFilter;
import com.profittrailer.utils.StaticUtil;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.File;

@Log4j2
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.authorizeRequests()
				.anyRequest().authenticated()
				.and().httpBasic()
				.and().addFilterBefore(getCustomFilter(), UsernamePasswordAuthenticationFilter.class)
				.formLogin()
				.and();
	}

	public CustomFilter getCustomFilter() throws Exception {
		CustomFilter filter = new CustomFilter("/login", "POST");
		filter.setAuthenticationManager(authenticationManagerBean());
		filter.setAuthenticationFailureHandler((request, response, exception) -> {
			response.sendRedirect("/login?error");
		});
		return filter;
	}

	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
		if (!new File("password").exists()) {
			auth.inMemoryAuthentication().withUser("user")
					.password(passwordEncoder().encode(StaticUtil.defaultPassword))
					.roles("USER", "ADMIN");

			log.info("Your random initial password {}", StaticUtil.defaultPassword);
		}
	}
}
