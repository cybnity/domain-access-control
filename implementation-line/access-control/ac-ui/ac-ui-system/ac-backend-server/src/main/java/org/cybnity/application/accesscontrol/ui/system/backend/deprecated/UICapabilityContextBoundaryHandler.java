package org.cybnity.application.accesscontrol.ui.system.backend.deprecated;

//import org.cybnity.application.asset_control.ui.system.backend.infrastructure.impl.redis.RedisOptionFactory;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.SharedData;
import io.vertx.ext.web.handler.sockjs.BridgeEvent;
import org.cybnity.application.accesscontrol.ui.system.backend.routing.GatewayRoutingPlan;

/**
 * Handler of UI events and interactions regarding one boundary of cockpit
 * capabilities
 */
public class UICapabilityContextBoundaryHandler extends EventBusBridgeHandler {

	private final String cqrsResponseChannel;
	private final Vertx context;
	//private final RedisOptions redisOpts;
	private final GatewayRoutingPlan destinationMap;

	public UICapabilityContextBoundaryHandler(EventBus eventBus, SharedData sessionStore, String cqrsResponseChannel,
			Vertx vertx) {
		super(eventBus, sessionStore);
		this.cqrsResponseChannel = cqrsResponseChannel;
		this.context = vertx;
		this.destinationMap = new GatewayRoutingPlan();
		// Define Redis options allowing capabilities to discuss with users interactions
		// space (don't use pool that avoid possible usable of channels subscription by
		// handlers)
		// TODO ajouter liaison a redis
		//redisOpts = RedisOptionFactory.createUsersInteractionsSpaceOptions();
	}

	/**
	 * Reference name of this handler.
	 * 
	 * @return A name.
	 */
	protected String refName() {
		return this.getClass().getSimpleName();
	}

	/**
	 * Get the current context.
	 * 
	 * @return A context.
	 */
	protected Vertx context() {
		return this.context;
	}

	/**
	 * Default implementation without ACL control, which return always true.
	 */
	@Override
	protected void authorizedResourceAccess(BridgeEvent event, Handler<AsyncResult<Boolean>> callback)
			throws SecurityException {
		if (callback != null)
			callback.handle(new ResourceAccessAuthorizationResult<Boolean>(Boolean.TRUE, null));
	}

	/**
	 * UI capability entry point regarding Vert.x bus events regarding UI
	 * capabilities.
	 */
	@Override
	protected void toUsersInteractionsSpace(BridgeEvent event) throws Exception {
		// regarding ui-event, domain-event, command-event, etc. as CYBNITY bridge
		JsonObject message = event.getRawMessage();
		if (message != null) {
			// TODO changer l'impl pour supporter la réception de ConcreteCommandEvent comme org.cybnity.framework.domain.Command

			// Read the event and contents requiring for context-based routing
			// see https://vertx.io/docs/vertx-tcp-eventbus-bridge/java/ documentation about standard Frame structure
			String eventType = message.getString("type", null);
			final String routingAddress = message.getString("address", null);
			final JsonObject headers = message.getJsonObject("headers", null);
			final String replyAddress = message.getString("replyAddress", null); // optional
			final JsonObject body = message.getJsonObject("body", null);
			if (eventType != null) {
				// - quality of event received and integrity WITHOUT TRANSFORMATION of the
				// message is ensured by parent handle(BridgeEvent event) method

				// - translation into supported event types by the UI interactions layer (Redis
				// space) when need

				// - the identification of channel of space where to push the event to process
				// - the push of event to space for processing by Application layer

				// Collaborate with users interactions space
				/*
				Redis.createClient(context, redisOpts).connect().onSuccess(conn -> {
					String correlationId = (body != null) ? body.getString("correlationId", null) : null;
					Enum<?> recipientChannel = destinationMap.recipient(routingAddress);
					if (recipientChannel != null) {
						// Send event into UI space's channel via
						conn.send(Request.cmd(Command.PUBLISH).arg(/* redis stream channel */ /* recipientChannel.name())
								/*.arg(body.encode())).onSuccess(res -> {
									// Confirm notification about performed routing
									JsonObject transactionResult = new JsonObject();
									transactionResult.put("status", "processing");
									if (correlationId != null) {
										transactionResult.put("correlationId", correlationId);
									}

									// System.out.println("Event forwarded event bus (address: " + routingAddress +
									 // ") to UIS Redis (channel: " + recipientChannel.name() + "): " + body);
									 //
									// Close the connection or return to the pool
									conn.close();

									// Notify the front side via the event bus
									bus().send(cqrsResponseChannel, transactionResult);
								}).onFailure(error -> {
									System.out.println(
											refName() + " connection to UIS broker failed: " + error.getCause());
									error.printStackTrace();
								});
					} else {
						// Unknown channel where to forward the event to Redis space for treatment by UI
						// capabilities
						System.out.println("Ignored event (routing address: " + routingAddress
								+ ") that is not supported by any UI capability API (none known destination)!");
						JsonObject transactionResult = new JsonObject();
						transactionResult.put("status", "rejected");
						if (correlationId != null) {
							transactionResult.put("correlationId", correlationId);
						}
						// Close the connection or return to the pool
						conn.close();
						// Notify the front side via the event bus
						bus().send(cqrsResponseChannel, transactionResult);
					}
				}).onFailure(fail -> {
					System.out.println(refName() + " UIS broker connection failed: ");
					fail.printStackTrace();
				});*/
			}
		}
	}

}
