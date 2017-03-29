package org.woehlke.javaee7.petclinic.services.backend;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.inject.Inject;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.woehlke.javaee7.petclinic.web.keycloak.KeycloakBean;

@ManagedBean(name = "greeting")
@SessionScoped
public class GreetingFacade {

	@Inject
	KeycloakBean keycloak;

	public String greet(String name) {

		HttpGet greetingRequest = new HttpGet("http://apps.tdlabs.local:20000/api/user/greet");
		greetingRequest.addHeader("Authorization", "Bearer " + keycloak.getAccessTokenString());

		try (CloseableHttpClient client = HttpClientBuilder.create() //
				.setDefaultRequestConfig(newRequestConfigWithDefaultTimeouts()) //
				.setUserAgent("javaee7-petclinic") //
				.build()) {
			try (CloseableHttpResponse response = client.execute(greetingRequest)) {
				String greeting = IOUtils.toString(response.getEntity().getContent());
				return greeting;
			}

		} catch (Exception ex) {
			return "Fallback greeting for " + name;
		}
	}









	private RequestConfig newRequestConfigWithDefaultTimeouts() {

		RequestConfig.Builder requestConfigBuilder = RequestConfig.custom();
		requestConfigBuilder = requestConfigBuilder.setConnectTimeout(1000);
		requestConfigBuilder = requestConfigBuilder.setConnectionRequestTimeout(1000);
		RequestConfig requestConfig = requestConfigBuilder.build();
		return requestConfig;
	}
}
