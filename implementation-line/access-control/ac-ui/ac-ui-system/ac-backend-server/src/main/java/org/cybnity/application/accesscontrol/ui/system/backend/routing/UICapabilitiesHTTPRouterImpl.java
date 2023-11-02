package org.cybnity.application.accesscontrol.ui.system.backend.routing;

import io.netty.handler.codec.http.HttpStatusClass;
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.SharedData;
import io.vertx.ext.web.AllowForwardHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.ResponseContentTypeHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.impl.RouterImpl;
import org.cybnity.application.accesscontrol.ui.api.experience.CollectionResourceArchetype;
import org.cybnity.application.accesscontrol.ui.system.backend.AppConfigurationVariable;
import org.cybnity.framework.IContext;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Router implementation which define the list of routes supported by a UI
 * capability perimeters accessible via the backend endpoint.
 * It's a Rest API router.
 */
public class UICapabilitiesHTTPRouterImpl extends RouterImpl {

    private final IContext context;
    private final Vertx vertx;

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
        this.vertx = vertx;
        createRoutes(vertx);
    }

    /**
     * Initialize all the routes supported by this routing service.
     */
    public void initRoutes(Router router) {
        // Mount the handlers for all incoming requests at every path and HTTP method
        // via creation of the several routes supported
        String apiRootURL = context.get(AppConfigurationVariable.ENDPOINT_HTTP_RESOURCE_API_ROOT_URL);

        /* The handler gets the appropriate content type from
         * getAcceptableContentType. As a consequence, you can easily share the same handler to produce data of different types
         */
        router.route(apiRootURL + "/*").handler(ResponseContentTypeHandler.create());
        // Allow to consume the HTTP request body (e.g json received command event)
        router.route(apiRootURL + "/*").handler(BodyHandler.create());

        // ---- ROUTES CONFIGURATION ---
        router.get(apiRootURL).handler(roundingCtx -> {
            roundingCtx.fail(404);
            /*
             * 404 If no route matches the path
             * 405 If a route matches the path but don’t match the HTTP Method
             * 406 If a route matches the path and the method but It can’t provide a response with a content type matching Accept header
             * 415 If a route matches the path and the method but It can’t accept the Content-type
             * 400 If a route matches the path and the method but It can’t accept an empty body */

        });

        // services according to MIME types that each handler can consume

        // New tenant registration resource
        router.post(apiRootURL + "/" + CollectionResourceArchetype.ORGANIZATIONS.label()).consumes("application/json").handler(routingCtx -> {
            // e.g. content-type header set to `text/json` or `application/json` will both match
            // This handler is called for any POST request regarding creation of organization as tenant
            JsonObject commandEvent = routingCtx.body().asJsonObject();

            if (commandEvent == null) {
                // Unprocessable entity: the provided entity in request is not understanding or not complete
                sendError(422, routingCtx.response());
            } else {
                // TODO : Publish command event to UIS space
                System.out.println("received object= " + commandEvent.toString());

                // Answer to requester that demand have been taken and is currently processing
                // for future return of reserved organization name or reject via async protocol
                HttpServerResponse response = routingCtx.response();
                response.setStatusCode(102 /* Accepted  - notifying client that treatment is in progress */);
                response.end();
            }
        }).failureHandler(failure -> {
            // Method failure: a transaction method have been failed
            // None potential help information is provided to avoid cyber-security breach exposure based on attempt of service failures to identify supported data/behaviors
            sendError(424, failure.response());
        });

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
            System.out.println("static route failure: " + failure.toString());
        });

        // Add possible API routes supported by the HTTP layer as a JSON api (e.g public REST api exposed to Cockpit UI client-side components)
        initRoutes(this);

        // Create Vert.x application data store
        SharedData sessionStore = vertx.sharedData();

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
        allowedMethods.add(HttpMethod.POST);
        allowedMethods.add(HttpMethod.OPTIONS);
        allowedMethods.add(HttpMethod.PUT);

        // Restrict cross calls only for server domains using the backend server
        // (e.g ReactJS frontend server)
        List<String> authorizedWhitelistOrigins = new LinkedList<>();
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
        Set<String> allowedHeaders = new HashSet<>();
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

    /**
     * Simulate a JSON answer provided on HTTP protocol by the API service.
     *
     * @param context            Mandatory context of the original call.
     * @param calledResourceName Mandatory resource name which have been called.
     */
    static public void sendJSONUICapabilityResponse(RoutingContext context, String calledResourceName) {
        // Get the address of the request
        String address = context.request().connection().remoteAddress().toString();
        // Get the query parameter "name"
        MultiMap queryParams = context.queryParams();
        String name = queryParams.contains("name") ? queryParams.get("name") : "unknown";
        // Write a json response (returns a JSON object containing the address of
        // the request, the query parameter name, and a greeting message)
        String json = new JsonObject().put("name", name).put("address", address).put("message",
                "Hello " + name + " (connected from " + address) + "), welcome on the called resource ("
                + calledResourceName + ")";
        HttpServerResponse response = context.response();
        response.putHeader("Content-Type", "application/json");
        response.end(json);
    }
}