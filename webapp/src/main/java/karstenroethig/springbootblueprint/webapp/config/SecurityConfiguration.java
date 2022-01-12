package karstenroethig.springbootblueprint.webapp.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import karstenroethig.springbootblueprint.webapp.model.domain.Authority;
import karstenroethig.springbootblueprint.webapp.repository.UserRepository;

@Configuration
public class SecurityConfiguration extends WebSecurityConfigurerAdapter
{
	public static class Authorities
	{
		public static final String ADMIN = "ROLE_ADMIN";
		public static final String USER = "ROLE_USER";

		public static final List<String> ALL_AUTHORITIES = Collections.unmodifiableList(
				Stream.of(USER, ADMIN).collect(Collectors.toList()));

		private Authorities() {}
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception
	{
		http
			.formLogin()
			.loginPage("/auth/login")
			.failureUrl("/auth/login?failed=true")
			.defaultSuccessUrl("/dashboard", true)
			.usernameParameter("username")
			.passwordParameter("password")
		.and()
			.logout()
			.logoutRequestMatcher(new AntPathRequestMatcher("/auth/logout"))
			.clearAuthentication(true)
			.invalidateHttpSession(true)
			.deleteCookies("JSESSIONID", "remember-me")
			.logoutSuccessUrl("/auth/logout-success")
		.and()
			.exceptionHandling()
			.accessDeniedPage("/auth/access-denied")
		.and()
			.sessionManagement()
			.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
			.sessionFixation()
				.newSession()
		.and()
			.csrf().disable() // deactivate Cross Site Request Forgery (CSRF), otherwise it is annoying with REST requests
			.headers()
				.frameOptions().disable()
				.cacheControl().disable()
		.and()
			.authorizeRequests() // take care of order, if a condition applies no further rules will be checked
			.antMatchers(
					"/auth/**",
					"/actuator/health")
				.permitAll()
			.antMatchers(
					"/admin/**",
					"/user-admin/**")
				.hasAnyAuthority(Authorities.ADMIN)
			.anyRequest().hasAnyAuthority(Authorities.USER, Authorities.ADMIN);
//			.anyRequest().authenticated();
	}

	@Override
	public void configure(WebSecurity web) throws Exception
	{
		web.ignoring()
			.antMatchers(
				"/webjars/**",
				"/resources/**",
				"/static/**",
				"/css/**",
				"/js/**",
				"/images/**"
			);
	}

	@Bean
	public PasswordEncoder passwordEncoder()
	{
		return PasswordEncoderFactories.createDelegatingPasswordEncoder();
	}

	@Bean
	public UserDetailsService userDetailsService(final UserRepository userRepository)
	{
		return username -> userRepository
			.findOneByEmailIgnoreCase(username)
			.map(entity -> new User(
				entity.getEmail(), // username
				entity.getHashedPassword(), // password
				entity.isEnabled(), // enabled
				true, // accountNonExpired
				true, // credentialsNonExpired
				!entity.isLocked(), // accountNonLocked
				getAuthorities(entity.getAuthorities()))) // authorities
			.orElseThrow(() -> new UsernameNotFoundException(username));
	}

	private Collection<? extends GrantedAuthority> getAuthorities(Collection<Authority> authorities)
	{
		List<GrantedAuthority> grantedAuthorities = new ArrayList<>();

		for (Authority authority : authorities)
		{
			grantedAuthorities.add(new SimpleGrantedAuthority(authority.getName()));
		}

		return grantedAuthorities;
	}
}
