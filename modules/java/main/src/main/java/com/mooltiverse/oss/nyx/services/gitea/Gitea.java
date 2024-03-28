package com.mooltiverse.oss.nyx.services.gitea;

import com.mooltiverse.oss.nyx.entities.Attachment;
import com.mooltiverse.oss.nyx.io.TransportException;
import com.mooltiverse.oss.nyx.services.*;
import com.mooltiverse.oss.nyx.services.SecurityException;
import com.mooltiverse.oss.nyx.services.gitlab.GitLab;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;

public class Gitea implements GitHostingService, ReleaseService, UserService {

    static final Logger logger = LoggerFactory.getLogger(Gitea.class);
    private Gitea() {

    }

    /**
     * @param name        the repository name. Cannot be {@code null}
     * @param description the repository description. It may be {@code null}
     * @param restricted  when {@code true} the repository will have private visibility, otherwise it will be public
     * @param initialize  when {@code true} the repository is also initialized (usually with a default README file)
     * @return
     * @throws SecurityException
     * @throws TransportException
     */
    @Override
    public GitHostedRepository createGitRepository(String name, String description, boolean restricted, boolean initialize) throws SecurityException, TransportException {
        return null;
    }

    /**
     * @param name the repository name. Cannot be {@code null}
     * @throws SecurityException
     * @throws TransportException
     */
    @Override
    public void deleteGitRepository(String name) throws SecurityException, TransportException {

    }

    /**
     * @param owner      the name of the repository owner to get the release for. It may be {@code null}, in which case,
     *                   the repository owner must be passed as a service option (see services implementing this interface for more
     *                   details on the options they accept). If not {@code null} this value overrides the option passed to the service.
     * @param repository the name of the repository to get the release for. It may be {@code null}, in which case,
     *                   the repository name must be passed as a service option (see services implementing this interface for more
     *                   details on the options they accept). If not {@code null} this value overrides the option passed to the service.
     * @param tag        the tag the release refers to (i.e. {@code 1.2.3}, {@code v4.5.6}). It can't be {@code null}
     * @return
     * @throws SecurityException
     * @throws TransportException
     */
    @Override
    public Release getReleaseByTag(String owner, String repository, String tag) throws SecurityException, TransportException {
        return null;
    }

    /**
     * @param owner       the name of the repository owner to create the release for. It may be {@code null}, in which case,
     *                    the repository owner must be passed as a service option (see services implementing this interface for more
     *                    details on the options they accept). If not {@code null} this value overrides the option passed to the service.
     * @param repository  the name of the repository to create the release for. It may be {@code null}, in which case,
     *                    the repository name must be passed as a service option (see services implementing this interface for more
     *                    details on the options they accept). If not {@code null} this value overrides the option passed to the service.
     * @param title       the release title, it may be the same of {@code tag} but not necessarily. It may be {@code null}
     * @param tag         tag to publish the release for (i.e. {@code 1.2.3}, {@code v4.5.6}). It can't be {@code null}
     * @param description the release description. This is usually a Markdown text containing release notes or a changelog
     *                    or something like that giving an overall description of the release
     * @param options     the optional map of release options ({@link #RELEASE_OPTION_DRAFT}, {@link #RELEASE_OPTION_PRE_RELEASE}).
     *                    When {@code null} no options are evaluated.
     * @return
     * @throws SecurityException
     * @throws TransportException
     */
    @Override
    public Release publishRelease(String owner, String repository, String title, String tag, String description, Map<String, Object> options) throws SecurityException, TransportException {
        return null;
    }

    /**
     * @param owner      the name of the repository owner to create the assets for. It may be {@code null}, in which case,
     *                   the repository owner must be passed as a service option (see services implementing this interface for more
     *                   details on the options they accept). If not {@code null} this value overrides the option passed to the service.
     * @param repository the name of the repository to create the assets for. It may be {@code null}, in which case,
     *                   the repository name must be passed as a service option (see services implementing this interface for more
     *                   details on the options they accept). If not {@code null} this value overrides the option passed to the service.
     * @param release    the release to publish the assets for. It must be an object created by the same service
     *                   implementation
     * @param assets     the set of assets to publish. Assets may be interpreted differently depending on their
     *                   {@link Attachment#getPath() path} and {@link Attachment#getType() type}. Please check the implementation class
     *                   for restrictions on the supported assets
     * @return
     * @throws SecurityException
     * @throws TransportException
     * @throws IllegalArgumentException
     */
    @Override
    public Release publishReleaseAssets(String owner, String repository, Release release, Set<Attachment> assets) throws SecurityException, TransportException, IllegalArgumentException {
        return null;
    }

    /**
     * @param feature the feature to check for support.
     * @return
     */
    @Override
    public boolean supports(Feature feature) {
        return false;
    }

    /**
     * @return
     * @throws SecurityException
     * @throws TransportException
     */
    @Override
    public User getAuthenticatedUser() throws SecurityException, TransportException {
        return null;
    }
}
