package org.woehlke.javaee7.petclinic.web.keycloak;

import static org.jboss.resteasy.util.Encode.encodeQueryParam;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

import org.keycloak.KeycloakSecurityContext;
import org.keycloak.adapters.AdapterUtils;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.RefreshableKeycloakSecurityContext;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.AccessToken.Access;
import org.keycloak.representations.IDToken;

@ManagedBean(name = "keycloak")
@SessionScoped
public class KeycloakBean {

	public String getUsername() {
		return getIdToken().getPreferredUsername();
	}

	public String getUserId() {
		return getIdToken().getId();
	}

	public String getUserDisplayName() {
		return getIdToken().getName();
	}

	public Set<String> getRoles() {

		Set<String> roles = AdapterUtils.getRolesFromSecurityContext(getCurrentKeycloakSecurityContext());

		if (roles != null) {
			return roles;
		}

		return Collections.emptySet();
	}

	public Set<String> getRealmRoles() {

		RefreshableKeycloakSecurityContext context = getCurrentKeycloakSecurityContext();
		if (context == null) {
			return Collections.emptySet();
		}

		Access realmAccess = context.getToken().getRealmAccess();
		if (realmAccess == null) {
			return Collections.emptySet();
		}

		return realmAccess.getRoles();
	}

	public Map<String, Set<String>> getSiblingRoles() {

		RefreshableKeycloakSecurityContext context = getCurrentKeycloakSecurityContext();
		if (context == null) {
			return Collections.emptyMap();
		}

		Map<String, Access> resourceAccess = context.getToken().getResourceAccess();

		String currentClientName = context.getDeployment().getResourceName();
		Predicate<Map.Entry<String, Access>> excludeCurrentClient = entry -> !entry.getKey().equals(currentClientName);

		Map<String, Set<String>> roles = new HashMap<>();
		Consumer<Map.Entry<String, Access>> extractRoleSet = entry -> roles.put(entry.getKey(),
				entry.getValue().getRoles());

		resourceAccess.entrySet().stream().filter(excludeCurrentClient).forEach(extractRoleSet);

		if (roles.isEmpty()) {
			return Collections.emptyMap();
		}

		return roles;
	}

	public String getLogoutUri() {

		HttpServletRequest request = getCurrentHttpRequest();

		String logoutUri = getKeycloakDeployment(request).getLogoutUrl().build() //
				+ "?redirect_uri=" + encodeQueryParam(getContextRootUri(request));

		return logoutUri;
	}

	public void logout() throws Exception {

		ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();

		externalContext.invalidateSession();
		externalContext.redirect(getLogoutUri());
	}

	public String getAccountUri() {

		HttpServletRequest request = getCurrentHttpRequest();

		String acctUri = getKeycloakDeployment(request).getAccountUrl() //
				+ "?referrer=" + encodeQueryParam(getKeycloakDeployment(request).getResourceName()) //
				+ "&referrer_uri=" + encodeQueryParam(getContextRootUri(request)) //
		;

		return acctUri;
	}

	public Map<String, Object> getCustomAttributes() {
		return getAccessToken().getOtherClaims();
	}

	private KeycloakDeployment getKeycloakDeployment(HttpServletRequest request) {
		return getKeycloakSecurityContext(request).getDeployment();
	}

	private String getContextRootUri(HttpServletRequest request) {
		return URI.create(request.getRequestURL().toString()).resolve(request.getContextPath()).toString();
	}

	private RefreshableKeycloakSecurityContext getKeycloakSecurityContext(HttpServletRequest httpRequest) {
		return (RefreshableKeycloakSecurityContext) httpRequest.getAttribute(KeycloakSecurityContext.class.getName());
	}

	private AccessToken getAccessToken() {
		return getCurrentKeycloakSecurityContext().getToken();
	}

	private IDToken getIdToken() {
		return getCurrentKeycloakSecurityContext().getIdToken();
	}

	private RefreshableKeycloakSecurityContext getCurrentKeycloakSecurityContext() {
		return getKeycloakSecurityContext(getCurrentHttpRequest());
	}

	private HttpServletRequest getCurrentHttpRequest() {

		FacesContext context = FacesContext.getCurrentInstance();
		ExternalContext externalContext = context.getExternalContext();

		return (HttpServletRequest) externalContext.getRequest();
	}

}