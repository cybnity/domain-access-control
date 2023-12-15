package org.cybnity.application.accesscontrol.ui.system.backend.routing;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.AllowForwardHeaders;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.impl.RouterImpl;
import org.cybnity.application.accesscontrol.ui.system.backend.AppConfigurationVariable;
import org.cybnity.framework.IContext;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Router implementation which define the list of routes supported by a UI
 * capability perimeters accessible via the backend endpoint.
 */
public class UICapabilitiesHTTPRouterImpl extends RouterImpl {

    private final IContext context;

    /**
     * Default constructor.
     *
     * @param vertx Mandatory base vertx context.
     * @param ctx   Mandatory base context providing environment resources.
     * @throws IllegalArgumentException When mandatory parameter is missing.
     */
    public UICapabilitiesHTTPRouterImpl(Vertx vertx, IContext ctx) throws IllegalArgumentException {
        super(vertx);
        if (vertx == null) throw new IllegalArgumentException("vertx parameter is required!");
        if (ctx == null) throw new IllegalArgumentException("ctx parameter is required!");
        this.context = ctx;
        createRoutes(vertx);
    }

    /**
     * Define input/outputs permitted resources.
     *
     * @param vertx Mandatory base vertx context.
     */
    private void createRoutes(Vertx vertx) {
        // Some public routes are provided regarding resources exposed without mandatory logged credential check

        // Add the UI static contents route supported by the HTTP layer about url path
        // and static contents directory eventually provided by this domain UI layer
        StaticHandler staticWebContentsHandler = StaticHandler.create("static");
        // Configure the static files delivery
        staticWebContentsHandler.setCachingEnabled(false);
        staticWebContentsHandler.setDefaultContentEncoding("UTF-8");
        staticWebContentsHandler.setIncludeHidden(false);
        staticWebContentsHandler.setDirectoryListing(false);
        // Handle static resources
        route("/static/*").handler(staticWebContentsHandler).failureHandler(failure -> {
            sendError(404, failure.response());
        });

        // DEFINE FORWARD SUPPORT (Cross Origin from ReactJS frontend server)
        this.allowForward(AllowForwardHeaders.FORWARD);
        // we can now allow forward header parsing
        // and in this case only the "X-Forward" headers will be considered
        this.allowForward(AllowForwardHeaders.X_FORWARD);
        // we can now allow forward header parsing
        // and in this case both the "Forward" header and "X-Forward" headers
        // will be considered, yet the values from "Forward" take precedence
        // this means if case of a conflict (2 headers for the same value)
        // the "Forward" value will be taken and the "X-Forward" ignored.
        this.allowForward(AllowForwardHeaders.ALL);

        // Define safe mechanism for allowing resources to be requested from one domain
        // (e.g ReactJS frontend server) and served from another (e.g this resource provider)
        // USE VERT.X-WEB CorsHandler handling the CORS protocol
        Set<String> allowedHeaders = getAllowedHeaders();

        // Define supported HTTP methods
        Set<HttpMethod> allowedMethods = new HashSet<>();
        allowedMethods.add(HttpMethod.GET);

        // Restrict cross calls only for server domains using the backend server
        // (e.g ReactJS frontend server)
        List<String> authorizedWhitelistOrigins = new LinkedList<String>();
        String serverURLs = context.get(AppConfigurationVariable.AUTHORIZED_WHITE_LIST_ORIGIN_SERVER_URLS);
        if (serverURLs != null && !serverURLs.isEmpty()) {
            // Identify each server url to authorize from the list
            for (String url : serverURLs.split(",")) {
                if (!"".equals(url)) {
                    authorizedWhitelistOrigins.add(url); // add server url authorization
                }
            }
        }

        // Set route options
        route().handler(CorsHandler.create().addOrigins(authorizedWhitelistOrigins
                /* Allowed origin pattern */
        ).allowCredentials(true /* Allow credentials property on XMLHttpRequest */
        ).allowedHeaders(allowedHeaders).allowedMethods(allowedMethods));

        // Add BodyHandler before the SockJS handler which is required to process POST
        // requests by sub-router
        this.post().handler(BodyHandler.create());
    }

    /**
     * Get the list of headers allowed regarding the requests treated by this backend server.
     *
     * @return A list of headers.
     */
    private static Set<String> getAllowedHeaders() {
        Set<String> allowedHeaders = new HashSet<String>();
        allowedHeaders.add("x-requested-with");
        allowedHeaders.add("Access-Control-Allow-Origin");// All to consume the content
        allowedHeaders.add("origin");
        allowedHeaders.add("Content-Type");
        allowedHeaders.add("accept");
        allowedHeaders.add("Authorization");
        allowedHeaders.add("X-Requested-With");
        return allowedHeaders;
    }

    /**
     * Send error status code and close response.
     *
     * @param statusCode Status code based on <a href="https://en.wikipedia.org/wiki/List_of_HTTP_status_codes">standard HTTP status codes</a>
     * @param response   Mandatory response not ended and not closed. When already ended or closed, this method do nothing.
     * @throws IllegalArgumentException When required parameter is missing.
     */
    private void sendError(int statusCode, HttpServerResponse response) throws IllegalArgumentException {
        if (response == null) throw new IllegalArgumentException("response parameter is required!");
        if (!response.ended() && !response.closed()) {
            if (statusCode > 99 && statusCode < 600) {
                /*
                 * 1xx informational response – the request was received, continuing process
                 * 2xx successful – the request was successfully received, understood, and accepted
                 * 3xx redirection – further action needs to be taken in order to complete the request
                 * 4xx client error – the request contains bad syntax or cannot be fulfilled
                 * 5xx server error – the server failed to fulfil an apparently valid request
                 */
                response.setStatusCode(statusCode);
            }
            // End the response
            response.end();
        }
    }
}