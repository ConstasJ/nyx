package com.mooltiverse.oss.nyx.services.gitea;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mooltiverse.oss.nyx.io.TransportException;
import com.mooltiverse.oss.nyx.services.SecurityException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.mooltiverse.oss.nyx.log.Markers.SERVICE;
import static com.mooltiverse.oss.nyx.services.gitea.Gitea.logger;

public class API {
    private final URI BASE_URI;

    private static final String CONTENT_TYPE = "application/json";

    private final String authToken;

    protected API(URI baseUri, String authToken) {
        super();
        Objects.requireNonNull(baseUri, "The base URL cannot be null");
        this.BASE_URI = baseUri;
        this.authToken = authToken;
    }

    private URI newRequestURI(String path) {
        Objects.requireNonNull(path, "The path cannot be null");
        if(path.isBlank()) {
            return BASE_URI;
        }

        String baseURI = BASE_URI.toString();
        if(baseURI.endsWith("/") && path.startsWith("/"))
            path = path.substring(1);
        return URI.create(baseURI.concat(path));
    }

    /**
     * Returns an HTTP client builder with all reusable attributes set. The returned object is not aware of any authentication
     * method.<br>
     * After retrieving the client builder with this method you can set custom properties if you need to and then get a new client
     * instance with {@link HttpClient.Builder#build()}.
     *
     * @return an HTTP client builder with all reusable attributes set.
     */
    private synchronized HttpRequest.Builder getRequestBuilder(boolean authenticate)
            throws SecurityException {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .uri(BASE_URI)
                .setHeader("Content-Type", CONTENT_TYPE);
            if(authenticate) {
                if (Objects.isNull(authToken))
                    throw new SecurityException("No authentication token provided");
                builder.setHeader("Authorization", "token " + authToken);
            }
            return builder.copy();
    }

    /**
     * Parses the given string as a JSON object tree and returns each item in the resulting map.
     *
     * @param body the JSON string to parse
     *
     * @return the map of properties parsed from the given body
     *
     * @throws TransportException in case unmarshalling fails
     */
    protected Map<String, Object> unmarshalJSONBody(String body)
            throws TransportException {
        try {
            JsonNode rootNode = new ObjectMapper().readTree(body);
            if (rootNode == null)
                throw new TransportException("Unmarshalling JSON content returned a null object");

            return GiteaEntity.toAttributeMap(rootNode);
        }
        catch (JsonProcessingException jpe) {
            throw new TransportException("An error occurred while unmarshalling JSON response", jpe);
        }
    }

    /**
     * Parses the given string as a collection of JSON object tree and returns each item in the resulting list of maps.
     *
     * @param body the JSON collection to parse
     *
     * @return the list of map of properties parsed from the given body
     *
     * @throws TransportException in case unmarshalling fails
     */
    protected List<Map<String, Object>> unmarshalJSONBodyAsCollection(String body)
            throws TransportException {
        try {
            JsonNode rootNode = new ObjectMapper().readTree(body);
            if (rootNode == null)
                throw new TransportException("Unmarshalling JSON content returned a null object");

            return GiteaEntity.toAttributeMaps(rootNode);
        }
        catch (JsonProcessingException jpe) {
            throw new TransportException("An error occurred while unmarshalling JSON response", jpe);
        }
    }

    /**
     * Parses the given string as a JSON object tree, selects the element with the given name and returns its attributes
     * item in the resulting map.
     *
     * @param body the JSON string to parse
     *
     * @return the map of properties parsed from the given element in the given body. It's {@code null} if no element
     * with the gibven name is available in the JSON body
     *
     * @throws TransportException in case unmarshalling fails
     */
    protected Map<String, Object> unmarshalJSONBodyElement(String body, String element)
            throws TransportException {
        try {
            JsonNode rootNode = new ObjectMapper().readTree(body);
            if (rootNode == null)
                throw new TransportException("Unmarshalling JSON content returned a null object");

            JsonNode elementNode = rootNode.get(element);
            return Objects.isNull(elementNode) ? null : GiteaEntity.toAttributeMap(elementNode);
        }
        catch (JsonProcessingException jpe) {
            throw new TransportException("An error occurred while unmarshalling JSON response", jpe);
        }
    }

    /**
     * Sends the given request and returns the response, logging as needed.
     *
     * @param request the request to send
     *
     * @return the response
     *
     * @throws IOException if thrown by the underlying implementation
     * @throws InterruptedException if thrown by the underlying implementation
     * @throws IllegalArgumentException if thrown by the underlying implementation
     */
    protected HttpResponse<String> sendRequest(HttpRequest request)
            throws IOException, InterruptedException, IllegalArgumentException {
        logger.debug(SERVICE, "HTTP request: '{}' '{}'", request.method(), request.uri());
        logger.trace(SERVICE, "HTTP request headers: '{}'", request.headers().toString());
        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        logger.debug(SERVICE, "HTTP response code: '{}'", response.statusCode());
        logger.trace(SERVICE, "HTTP response headers: '{}'", response.headers().toString());
        logger.trace(SERVICE, "HTTP response body:");
        logger.trace(SERVICE, response.body());
        return response;
    }

    /**
     * Creates a new repository for the currently authenticated user.
     * <br>
     * Please note that if the service has been configured with repository owner and name those attributes are ignored
     * by this method as the owner is always the authenticated user (the one owning the configured credentials) and the
     * name is always the {@code name} attribute.
     *
     * @param name the repository name. Cannot be {@code null}
     * @param description the repository description. It may be {@code null}
     * @param restricted when {@code true} the repository will have private visibility, otherwise it will be public
     * @param initialize when {@code true} the repository is also initialized with a default README file
     *
     * @return the attributes of the new repository. Never {@code null}
     *
     * @throws TransportException if a transport related error occurs while communicating with the server
     * @throws SecurityException if authentication fails
     */
    Map<String, Object> createRepository(String name, String description, boolean restricted, boolean initialize)
    throws TransportException, SecurityException {
        Objects.requireNonNull(name, "The repository name cannot be null");
        URI uri = newRequestURI("/user/repos");

        Map<String, Object> requestParameters = new HashMap<String, Object>();
        requestParameters.put("name", name);
        requestParameters.put("description", description);
        requestParameters.put("private", restricted);
        requestParameters.put("auto_init", initialize);

        HttpResponse<String> response = null;
        try {
            response = sendRequest(getRequestBuilder(true)
                    .POST(HttpRequest.BodyPublishers.ofString(requestParameters.toString()))
                    .uri(uri)
                    .build());
        } catch(IOException| InterruptedException e) {
            throw new TransportException(e);
        }

        if(response.statusCode() != 201){
            if (response.statusCode() == 401 || response.statusCode() == 403)
                throw new SecurityException(String.format("Request returned a status code '%d': %s", response.statusCode(), response.body()));
            throw new TransportException(String.format("Request returned a status code '%d': %s", response.statusCode(), response.body()));
        }

        return unmarshalJSONBody(response.body());
    }

    /**
     * Deletes a repository for the currently authenticated user.
     * <br>
     * Please note that if the service has been configured with repository owner and name those attributes are ignored
     * by this method as the owner is always the authenticated user (the one owning the configured credentials) and the
     * name is always the {@code name} attribute.
     *
     * @param name the repository name. Cannot be {@code null}
     *
     * @throws TransportException if a transport related error occurs while communicating with the server
     * @throws SecurityException if authentication fails
     */
    void deleteRepository(String name)
        throws TransportException, SecurityException {
        Objects.requireNonNull(name, "The name of the repository to delete cannot be null");
        URI uri = newRequestURI("/repos/" + name);

        HttpResponse<String> response = null;
        try {
            response = sendRequest(getRequestBuilder(true)
                    .DELETE()
                    .uri(uri)
                    .build());
        } catch(IOException| InterruptedException e) {
            throw new TransportException(e);
        }

        if(response.statusCode() != 204){
            if (response.statusCode() == 401 || response.statusCode() == 403)
                throw new SecurityException(String.format("Request returned a status code '%d': %s", response.statusCode(), response.body()));
            throw new TransportException(String.format("Request returned a status code '%d': %s", response.statusCode(), response.body()));
        }
    }

    /**
     * Finds the release in the given repository by the release tag.
     *
     * @param owner the name of the owner of the repository to get the release for. It can't be {@code null}
     * @param repository the name of the repository to get the release for. It can't be {@code null}
     * @param tag the release tag (i.e. {@code 1.2.3}, {@code v4.5.6}). It can't be {@code null}
     *
     * @return the attributes of the requested release, if available, or {@code null} otherwise
     *
     * @throws SecurityException if authentication or authorization fails
     * @throws TransportException if communication to the remote endpoint fails
     * @throws UnsupportedOperationException if the underlying implementation does not
     * {@link #supports(Service.Feature) support} the {@link Service.Feature#RELEASES} feature.
     */
    Map<String, Object> getReleaseByTag(String owner, String repository, String tag)
            throws SecurityException, TransportException {
        Objects.requireNonNull(owner, "The release repository owner cannot be null");
        Objects.requireNonNull(repository, "The release repository name cannot be null");
        Objects.requireNonNull(tag, "The release tag cannot be null");
        URI uri = newRequestURI("/repos/" + owner + "/" + repository + "/releases/tags/" + tag);

        HttpResponse<String> response = null;
        try {
            response = sendRequest(getRequestBuilder(true).uri(uri).GET().build());
        }
        catch (IOException | InterruptedException e) {
            throw new TransportException(e);
        }

        if (response.statusCode() != 200) {
            if (response.statusCode() == 404)
                return null; // the object just doesn't exist
            else if (response.statusCode() == 401 || response.statusCode() == 403)
                throw new SecurityException(String.format("Request returned a status code '%d': %s", response.statusCode(), response.body()));
            else throw new TransportException(String.format("Request returned a status code '%d': %s", response.statusCode(), response.body()));
        }

        return unmarshalJSONBody(response.body());
    }
}
