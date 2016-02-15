/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.ext.git.server.nativegit;

import org.eclipse.che.api.auth.oauth.OAuthTokenProvider;
import org.eclipse.che.api.auth.shared.dto.OAuthToken;
import org.eclipse.che.api.git.GitException;
import org.eclipse.che.api.git.ProviderInfo;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.api.git.CredentialsProvider;
import org.eclipse.che.api.git.UserCredential;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;

/**
 * @author Sergii Kabashniuk
 */
@Singleton
public class GitHubOAuthCredentialProvider implements CredentialsProvider {

    private static final Logger LOG = LoggerFactory.getLogger(GitHubOAuthCredentialProvider.class);

    private static String OAUTH_PROVIDER_NAME = "github";
    private final OAuthTokenProvider oAuthTokenProvider;
    private final String             authorizationServiceUrl;

    @Inject
    public GitHubOAuthCredentialProvider(OAuthTokenProvider oAuthTokenProvider,
                                         @Named("api.endpoint") String apiUrl) {
        this.oAuthTokenProvider = oAuthTokenProvider;
        this.authorizationServiceUrl = apiUrl + "/oauth/authenticate";
    }

    @Override
    public UserCredential getUserCredential() throws GitException {
        try {
            OAuthToken token = oAuthTokenProvider.getToken(OAUTH_PROVIDER_NAME, EnvironmentContext.getCurrent().getUser().getId());
            if (token != null) {
                return new UserCredential(token.getToken(), token.getToken(), OAUTH_PROVIDER_NAME);
            }
        } catch (IOException e) {
            LOG.warn(e.getLocalizedMessage());
        }
        return null;
    }

    @Override
    public String getId() {
        return OAUTH_PROVIDER_NAME;
    }

    @Override
    public boolean canProvideCredentials(String url) {
        return url.contains("github.com");
    }

    @Override
    public ProviderInfo getProviderInfo() {
        return new ProviderInfo(OAUTH_PROVIDER_NAME, UriBuilder.fromUri(authorizationServiceUrl)
                                                               .queryParam("oauth_provider", OAUTH_PROVIDER_NAME)
                                                               .queryParam("userId", EnvironmentContext.getCurrent().getUser().getId())
                                                               .queryParam("scope", "repo")
                                                               .build()
                                                               .toString());
    }

}
