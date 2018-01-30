package br.gov.ce.secult.scac.controller;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.oltu.oauth2.client.OAuthClient;
import org.apache.oltu.oauth2.client.URLConnectionClient;
import org.apache.oltu.oauth2.client.request.OAuthBearerClientRequest;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.client.response.OAuthAuthzResponse;
import org.apache.oltu.oauth2.client.response.OAuthJSONAccessTokenResponse;
import org.apache.oltu.oauth2.client.response.OAuthResourceResponse;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.types.GrantType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Path("/")
public class IndexController {

	@Value("${logincultura.client.id}")
	private String clientId;

	@Value("${logincultura.client.secret}")
	private String clientSecret;

	@Value("${logincultura.scope}")
	private String scope;

	@Value("${logincultura.redirect-uri}")
	private String redirectUri;

	@Value("${logincultura.user-info.url}")
	private String userInfoUrl;

	@Value("${logincultura.authz.endpoint}")
	private String authzEndpoint;

	@Value("${logincultura.token.endpoint}")
	private String tokenEndpoint;

	public static String accessToken = null;

	@GET
	@Path("/login")
	public Response login() throws OAuthSystemException, MalformedURLException, URISyntaxException {

		OAuthClientRequest request = OAuthClientRequest.authorizationLocation(authzEndpoint).setClientId(clientId)
				.setRedirectURI(redirectUri).setResponseType("code").setScope(scope).buildQueryMessage();

		URL url = new URL(request.getLocationUri());

		return Response.seeOther(url.toURI()).build();
	}

	@GET
	@Path("/redirect")
	public String backFeedback(@Context HttpServletRequest httpRequest)
			throws OAuthProblemException, OAuthSystemException {
		OAuthAuthzResponse oar = OAuthAuthzResponse.oauthCodeAuthzResponse(httpRequest);
		String code = oar.getCode();

		OAuthClientRequest request = OAuthClientRequest.tokenLocation(tokenEndpoint)
				.setGrantType(GrantType.AUTHORIZATION_CODE).setClientId(clientId).setClientSecret(clientSecret)
				.setRedirectURI(redirectUri).setCode(code).buildBodyMessage();

		OAuthClient oAuthClient = new OAuthClient(new URLConnectionClient());

		OAuthJSONAccessTokenResponse accessTokenResponse = oAuthClient.accessToken(request);

		String accessToken = accessTokenResponse.getAccessToken();

		IndexController.accessToken = accessToken;

		return accessToken;
	}

	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("/info")
	public String info() throws OAuthSystemException, OAuthProblemException {
		OAuthClientRequest bearerClientRequest = new OAuthBearerClientRequest(userInfoUrl)
				.setAccessToken(IndexController.accessToken).buildQueryMessage();

		OAuthClient oAuthClient = new OAuthClient(new URLConnectionClient());
		OAuthResourceResponse resourceResponse = oAuthClient.resource(bearerClientRequest, OAuth.HttpMethod.GET,
				OAuthResourceResponse.class);

		/*
		 * System.out.println("Google **********************"); Map responseMap =
		 * JSONUtils.parseJSON(resourceResponse.getBody()); Set<String> keys =
		 * responseMap.keySet(); for(String key: keys) { System.out.println(key + " = "
		 * + responseMap.get(key)); }
		 */

		return resourceResponse.getBody();
	}

}
