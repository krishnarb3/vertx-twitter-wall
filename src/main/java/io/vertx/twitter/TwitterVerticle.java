package io.vertx.twitter;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.sockjs.BridgeEventType;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.PermittedOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import twitter4j.*;

public class TwitterVerticle extends AbstractVerticle {

    private String searchQuery;


    public static void main(String[] args) {

    }

    @Override
    public void start(Future<Void> startFuture) throws Exception {

        Router router = Router.router(vertx);

        BridgeOptions opts = new BridgeOptions()
                .addInboundPermitted(new PermittedOptions().setAddress("query.to.server"))
                .addOutboundPermitted(new PermittedOptions().setAddress("publish.to.client"));

        SockJSHandler sockHandler = SockJSHandler.create(vertx).bridge(opts);

        router.route("/eventbus/*").handler(sockHandler);
        router.route().handler(staticHandler());

        vertx.createHttpServer()
                .requestHandler(router::accept)
                .listen(9090, res -> {
                   if(res.succeeded()) {
                       updateSearch();
                       startFuture.complete();
                   }
                   else startFuture.fail("Error occurred");
                });
    }

    private SockJSHandler eventBusHandler() {
        BridgeOptions options = new BridgeOptions();
        return SockJSHandler.create(vertx).bridge(options, event -> {
            if (event.type() == BridgeEventType.SOCKET_CREATED) {
                System.out.println("Socket has been created");
            }
            event.complete(true);
        });
    }

    public void defaultHandler(RoutingContext routingContext) {
        System.out.println("default");
    }

    public void getSearchForQuery(RoutingContext routingContext) {
        searchQuery = routingContext.request().getParam("query");
        System.out.println(searchQuery);
        vertx.<QueryResult>executeBlocking(future -> {
            QueryResult result = null;
            try {
                Twitter twitter = TwitterFactory.getSingleton();
                Query query = new Query("#"+ searchQuery);
                query.setCount(100);
                result = twitter.search(query);

            } catch (TwitterException e) {
                e.printStackTrace();
            }
            future.complete(result);
        }, result -> {
            HttpServerResponse response = routingContext.response();
            response.setStatusCode(200).putHeader("Content-Type", "text/html; utf-8");
            Buffer buffer = Buffer.buffer();
            if(result.succeeded()) {
                for (Status status : result.result().getTweets()) {
                    buffer.appendString(status.getText()+"<br>");
                }
            }
            response.putHeader("Content-Length", buffer.getBytes().length+"");
            response.write(buffer);
            response.end();
        });
    }

    public void updateSearch() {
        vertx.setPeriodic(8000, h -> {
            try {
                Thread.sleep(10000);
                Twitter twitter = TwitterFactory.getSingleton();
                Query query = new Query("#"+ searchQuery);
                query.setCount(100);
                QueryResult result = twitter.search(query);
                vertx.eventBus().publish("publish.to.client", result);
            } catch (InterruptedException | TwitterException e) {
                System.out.println(e.getMessage() + " error occurred");
            }
        });
    }

    private StaticHandler staticHandler() {
        return StaticHandler.create()
                .setCachingEnabled(false);
    }
}
