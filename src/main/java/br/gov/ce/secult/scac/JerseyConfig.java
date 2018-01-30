package br.gov.ce.secult.scac;

import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.stereotype.Component;

@Component
public class JerseyConfig extends ResourceConfig {

	public JerseyConfig() {
//		register(OAuth2LoginCulturaController.class);
		packages("br.gov.ce.secult.scac.controller");
	}
	
}