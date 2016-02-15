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
package org.eclipse.che.ide.ext.github.client.authenticator;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.api.ssh.gwt.client.SshServiceClient;
import org.eclipse.che.api.ssh.shared.dto.SshPairDto;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.ext.github.client.GitHubLocalizationConstant;
import org.eclipse.che.ide.ext.git.ssh.client.GitSshKeyUploaderRegistry;
import org.eclipse.che.ide.ext.git.ssh.client.SshKeyUploader;
import org.eclipse.che.ide.ext.git.ssh.client.manage.SshKeyManagerPresenter;
import org.eclipse.che.ide.rest.RestContext;
import org.eclipse.che.ide.ui.dialogs.DialogFactory;
import org.eclipse.che.ide.util.loging.Log;
import org.eclipse.che.security.oauth.JsOAuthWindow;
import org.eclipse.che.security.oauth.OAuthCallback;
import org.eclipse.che.security.oauth.OAuthStatus;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author Roman Nikitenko
 */
public class GitHubAuthenticatorImpl implements GitHubAuthenticator, OAuthCallback, GitHubAuthenticatorViewImpl.ActionDelegate {
    public static final String GITHUB_HOST = "github.com";

    AsyncCallback<OAuthStatus> callback;

    private final GitSshKeyUploaderRegistry  registry;
    private final SshServiceClient           sshServiceClient;
    private final DialogFactory              dialogFactory;
    private final GitHubAuthenticatorView    view;
    private final NotificationManager        notificationManager;
    private final GitHubLocalizationConstant locale;
    private final String                     baseUrl;
    private final AppContext                 appContext;

    @Inject
    public GitHubAuthenticatorImpl(GitSshKeyUploaderRegistry registry,
                                   SshServiceClient sshServiceClient,
                                   GitHubAuthenticatorView view,
                                   DialogFactory dialogFactory,
                                   GitHubLocalizationConstant locale,
                                   @RestContext String baseUrl,
                                   NotificationManager notificationManager,
                                   AppContext appContext) {
        this.registry = registry;
        this.sshServiceClient = sshServiceClient;
        this.view = view;
        this.view.setDelegate(this);
        this.locale = locale;
        this.baseUrl = baseUrl;
        this.dialogFactory = dialogFactory;
        this.notificationManager = notificationManager;
        this.appContext = appContext;
    }

    @Override
    public void authorize(@NotNull final AsyncCallback<OAuthStatus> callback) {
        this.callback = callback;
        view.showDialog();
    }

    @Override
    public void onCancelled() {
        callback.onFailure(new Exception("Authorization request rejected by user."));
    }

    @Override
    public void onAccepted() {
        showAuthWindow();
    }

    @Override
    public void onAuthenticated(OAuthStatus authStatus) {
        if (view.isGenerateKeysSelected()) {
            generateSshKeys(authStatus);
            return;
        }
        callback.onSuccess(authStatus);
    }

    private void showAuthWindow() {
        JsOAuthWindow authWindow = new JsOAuthWindow(getAuthUrl(), "error.url", 500, 980, this);
        authWindow.loginWithOAuth();
    }

    private String getAuthUrl() {
        String userId = appContext.getCurrentUser().getProfile().getId();
        return baseUrl
               + "/oauth/authenticate?oauth_provider=github"
               + "&scope=user,repo,write:public_key&userId=" + userId
               + "&redirect_after_login="
               + Window.Location.getProtocol() + "//"
               + Window.Location.getHost() + "/ws/"
               + appContext.getWorkspace().getConfig().getName();
    }

    private void generateSshKeys(final OAuthStatus authStatus) {
        final SshKeyUploader githubKeyUploader = registry.getUploader(GITHUB_HOST);
        if (githubKeyUploader != null) {
            String userId = appContext.getCurrentUser().getProfile().getId();
            githubKeyUploader.uploadKey(userId, new AsyncCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    callback.onSuccess(authStatus);
                    notificationManager.notify(locale.authMessageKeyUploadSuccess(), appContext.getCurrentProject().getRootProject());
                }

                @Override
                public void onFailure(Throwable exception) {
                    dialogFactory.createMessageDialog(locale.authorizationDialogTitle(), locale.authMessageUnableCreateSshKey(), null)
                                 .show();
                    callback.onFailure(new Exception(locale.authMessageUnableCreateSshKey()));
                    getFailedKey();
                }
            });
        } else {
            dialogFactory.createMessageDialog(locale.authorizationDialogTitle(), locale.authMessageUnableCreateSshKey(), null).show();
            callback.onFailure(new Exception(locale.authMessageUnableCreateSshKey()));
        }
    }

    /** Need to remove failed uploaded pair from local storage if they can't be uploaded to github */
    private void getFailedKey() {
        sshServiceClient.getPairs(SshKeyManagerPresenter.GIT_SSH_SERVICE)
                        .then(new Operation<List<SshPairDto>>() {
                            @Override
                            public void apply(List<SshPairDto> result) throws OperationException {
                                for (SshPairDto key : result) {
                                    if (key.getName().equals(GITHUB_HOST)) {
                                        removeFailedKey(key);
                                        return;
                                    }
                                }
                            }
                        })
                        .catchError(new Operation<PromiseError>() {
                            @Override
                            public void apply(PromiseError arg) throws OperationException {
                                Log.error(GitHubAuthenticator.class, arg.getCause());
                            }
                        });
    }

    /**
     * Remove failed pair.
     *
     * @param pair
     *         failed pair
     */
    private void removeFailedKey(@NotNull final SshPairDto pair) {
        sshServiceClient.deletePair(pair.getService(), pair.getName())
                        .catchError(new Operation<PromiseError>() {
                            @Override
                            public void apply(PromiseError arg) throws OperationException {
                                Log.error(GitHubAuthenticator.class, arg.getCause());
                            }
                        });
    }
}
