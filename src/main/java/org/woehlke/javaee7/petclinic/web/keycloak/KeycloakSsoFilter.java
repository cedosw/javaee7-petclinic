package org.woehlke.javaee7.petclinic.web.keycloak;

import org.keycloak.adapters.servlet.KeycloakOIDCFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import java.io.IOException;

/**
 * Created by tom on 21.02.16.
 */
@WebFilter(value = "/*", asyncSupported = true)
public class KeycloakSsoFilter extends KeycloakOIDCFilter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {

        //overridden to ease setting breakpoints
        super.doFilter(req, res, chain);
    }
}