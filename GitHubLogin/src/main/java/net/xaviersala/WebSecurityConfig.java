package net.xaviersala;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.Filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.resource.ResourceServerProperties;
import org.springframework.boot.autoconfigure.security.oauth2.resource.UserInfoTokenServices;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.boot.web.servlet.ErrorPage;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.filter.OAuth2ClientAuthenticationProcessingFilter;
import org.springframework.security.oauth2.client.filter.OAuth2ClientContextFilter;
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeResourceDetails;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableOAuth2Client;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.filter.CompositeFilter;

/**
 * Configuració de Spring Security però en comptes de fer-ho a l'estil antic
 * (via XML) amb una classe de tipus @Configuration
 * 
 * 
 * @author xavier
 *
 *
 */

// @EnableOAuth2Sso

@Configuration
@EnableWebSecurity
@EnableOAuth2Client
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired
	OAuth2ClientContext oauth2ClientContext;

	/**
	 * Definim quin és l'accés que cal per les determinades url.
	 * 
	 * La part final defineix que s'hi posa un filtre per evitar problemes.
	 * 
	 */
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.antMatcher("/**").authorizeRequests().antMatchers("/", "/login**").permitAll().anyRequest().authenticated()
				.and().exceptionHandling().authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/")).and()
				.logout().logoutSuccessUrl("/").permitAll().and()
				.addFilterBefore(ssoFilter(), BasicAuthenticationFilter.class);
	}

	/**
	 * Definim quines són les adreces que no necessiten seguretat.
	 * 
	 * No és necessàri accés segur per les adreces de recursos bàsics: CSS,
	 * Javascript, Imatges, tipus de lletres.
	 */
	@Override
	public void configure(WebSecurity web) throws Exception {
		web.ignoring().antMatchers("/js/**", "/css/**", "/images/**", "/fonts/**");
	}
	
	/**
	 * Col·loco filtres abans del funcionament de Security per poder fer login
	 * en diferents proveïdors abans de res.
	 * 
	 **/
	@Bean
	public FilterRegistrationBean oauth2ClientFilterRegistration(OAuth2ClientContextFilter filter) {
		FilterRegistrationBean registration = new FilterRegistrationBean();
		registration.setFilter(filter);
		registration.setOrder(-100);
		return registration;
	}

	/**
	 * Defineix el filtre a aplicar abans de fer servir Spring Security.
	 * Bàsicament es fa servir per fer login amb OAuth.
	 * 
	 * @return
	 */
	private Filter ssoFilter() {
		CompositeFilter filter = new CompositeFilter();
		List<Filter> filters = new ArrayList<>();

		filters.add(ssoFilter(github(), "/login/github"));
		filters.add(ssoFilter(twitter(), "/login/twitter"));
		filters.add(ssoFilter(facebook(), "/login/facebook"));
		filters.add(ssoFilter(google(), "/login/google"));

		filter.setFilters(filters);
		return filter;
	}

	/**
	 * Crea un filtre a partir de les dades de configuració del proveïdor i de
	 * la URL que ha de login.
	 * 
	 * @param client
	 *            Dades per fer OAuth
	 * @param path
	 *            URL de login
	 * @return
	 */
	private Filter ssoFilter(ClientResources client, String path) {
		OAuth2ClientAuthenticationProcessingFilter oAuth2ClientAuthenticationFilter = new OAuth2ClientAuthenticationProcessingFilter(
				path);
		OAuth2RestTemplate oAuth2RestTemplate = new OAuth2RestTemplate(client.getClient(), oauth2ClientContext);
		oAuth2ClientAuthenticationFilter.setRestTemplate(oAuth2RestTemplate);
		UserInfoTokenServices tokenServices = new UserInfoTokenServices(client.getResource().getUserInfoUri(),
				client.getClient().getClientId());
		tokenServices.setRestTemplate(oAuth2RestTemplate);
		oAuth2ClientAuthenticationFilter.setTokenServices(tokenServices);
		return oAuth2ClientAuthenticationFilter;
	}

	/**
	 * 
	 * Carrega les dades de configuració del fitxer application.yml.
	 *
	 * @Return Dades de configuració 
	 **/
	@Bean
	@ConfigurationProperties("github")
	public ClientResources github() {
		return new ClientResources();
	}

	@Bean
	@ConfigurationProperties("google")
	public ClientResources google() {
		return new ClientResources();
	}

	@Bean
	@ConfigurationProperties("facebook")
	public ClientResources facebook() {
		return new ClientResources();
	}
	
	@Bean
	@ConfigurationProperties("twitter")
	public ClientResources twitter() {
		return new ClientResources();
	}



	/**
	 * Classe per generar els missatges d'error d'autenticació
	 * dels proveïdors.
	 * 
	 * @author xavier
	 *
	 */
	@Configuration
	protected static class ServletCustomizer {
		@Bean
		public EmbeddedServletContainerCustomizer customizer() {
			return container -> {
				container.addErrorPages(new ErrorPage(HttpStatus.UNAUTHORIZED, "/unauthenticated"));
			};
		}
	}

	/**
	 * Classe interna per emmagatzemar les dades de cada proveïdor diferent i no
	 * haver de repetir el codi.
	 * 
	 * @author xavier
	 *
	 */
	class ClientResources {

		@NestedConfigurationProperty
		private AuthorizationCodeResourceDetails client = new AuthorizationCodeResourceDetails();

		@NestedConfigurationProperty
		private ResourceServerProperties resource = new ResourceServerProperties();

		public AuthorizationCodeResourceDetails getClient() {
			return client;
		}

		public ResourceServerProperties getResource() {
			return resource;
		};
	};

}
