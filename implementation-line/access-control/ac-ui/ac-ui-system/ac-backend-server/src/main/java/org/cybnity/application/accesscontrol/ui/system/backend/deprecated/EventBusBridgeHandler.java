package org.cybnity.application.accesscontrol.ui.system.backend.deprecated;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.SharedData;
import io.vertx.ext.bridge.BridgeEventType;
import io.vertx.ext.web.handler.sockjs.BridgeEvent;

import java.util.logging.Logger;

/**
 * Handler of bus events exposed between frontend and backend as integration
 * layer.
 */
public abstract class EventBusBridgeHandler implements Handler<BridgeEvent> {
    private final EventBus bus;
    private final SharedData sessionStore;

    /**
     * Technical logging
     */
    private static final Logger logger = Logger.getLogger(EventBusBridgeHandler.class.getName());

    public EventBusBridgeHandler(EventBus eventBus, SharedData sessionStore) {
        this.bus = eventBus;
        this.sessionStore = sessionStore;
    }

    /**
     * Get the session store.
     *
     * @return A store or null.
     */
    protected SharedData sessionStore() {
        return this.sessionStore;
    }

    /**
     * Get the connected event bus.
     *
     * @return A bus.
     */
    protected EventBus bus() {
        return this.bus;
    }

    /**
     * Check the Access Control Layer (ACL) conditions allowing or rejecting the
     * processing of an event. This method is call by handle(BridgeEvent event)
     * method for each even handled, before to delegate treatment via a call to
     * toUsersInteractionsSpace(BridgeEvent event).
     *
     * @param event    The subject to control.
     * @param callback True if authorized (e.g command permitted according to an
     *                 authorized RBAC result). False when resource access not
     *                 authorized.
     * @throws SecurityException When a security problem (e.g missing security
     *                           information required for ACL check) is detected on
     *                           the controlled event.
     */
    protected abstract void authorizedResourceAccess(BridgeEvent event, Handler<AsyncResult<Boolean>> callback)
            throws SecurityException;

    @Override
    public void handle(BridgeEvent event) {
        boolean alreadyCompleted = false;
        if (event != null) {
            if (event.type() == BridgeEventType.SOCKET_IDLE) {
                // This even will occur when SockJS socket is on idle for longer period of time
                // than initially configured
                logger.fine("Socket IDLE occurred");
            } else if (event.type() == BridgeEventType.SOCKET_CREATED) {
                // This event will occur when a SockJS socket is created
                logger.fine("Socket is created");
            } else if (event.type() == BridgeEventType.SOCKET_CLOSED) {
                // This event will occur when a SockJS socket is closed
                logger.fine("Socket is closed");
            } else if (event.type() == BridgeEventType.SOCKET_ERROR) {
                // This event will occur when an underlying transport errors
                logger.fine("Socket error occurred: " + event.getRawMessage().encode());
            }

            // --- CHECK THE CONFORMITY OF THE EVENT STRUCTURE ---
            JsonObject message = event.getRawMessage();

            if (event.type() == BridgeEventType.PUBLISH || event.type() == BridgeEventType.RECEIVE) {
                if (message.getString("body") != null && message.getString("body").contentEquals("violation")) {
                    // Reject event where a specific word exist in body (e.g security, conformity
                    // violation)
                    logger.fine("Event rejected caused by content violation");
                    event.complete(false);
                    return;
                }
                if (event.type() == BridgeEventType.RECEIVE) {
                    // a message is attempted to be delivered from the server to the client
                    // Add security tracking about event transmitted to client side
                    logger.fine("Event delivered from server to client side");
                }
            }

            if (event.type() == BridgeEventType.PUBLISH || event.type() == BridgeEventType.SEND) {
                // A message is attempted to be published from the client to the server (PUBLISH
                // = to all the handler; SEND = only to one of the handler instances)
                try {
                    // --- ASYNCHRONOUSLY CHECK THE AUTHORIZATION OF EVENT TREATMENT ACCORDING TO
                    // ACL ---
                    authorizedResourceAccess(event, new Handler<AsyncResult<Boolean>>() {
                        @Override
                        public void handle(AsyncResult<Boolean> authResult) {
                            if (authResult.result()) {
                                try {
                                    // Delegate to inputs handling about UI capabilities treatment/control by the UI
                                    // interaction logic
                                    toUsersInteractionsSpace(event);
                                } catch (Exception e) {
                                    System.out.println(e);
                                }
                            } else {
                                logger.fine("Event rejected caused by unauthorized resource access");
                            }
                        }
                    });
                } catch (Exception ee) {
                    logger.fine(ee.getMessage());
                    event.complete(false);
                    return;
                }
            }
            if (!alreadyCompleted)
                // Complete the event delegation status
                event.complete(true);
        }
    }

    /**
     * Transmit an interaction event (e.g ui-event, domain-event, command-event)
     * coming from frontend side, to User Interactions Space for
     * analysis/processing.
     *
     * @param event To treat.
     * @throws Exception When failure occurred during transmission to space.
     */
    protected abstract void toUsersInteractionsSpace(BridgeEvent event) throws Exception;

}
