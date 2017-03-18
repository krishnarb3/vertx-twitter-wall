package io.vertx.twitter;

import io.vertx.core.Future;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.PermittedOptions;
import io.vertx.rxjava.core.RxHelper;
import io.vertx.rxjava.ext.web.Router;
import io.vertx.rxjava.ext.web.handler.StaticHandler;
import io.vertx.rxjava.ext.web.handler.sockjs.SockJSHandler;
import rx.Observable;
import twitter4j.*;

public class TwitterVerticle extends io.vertx.rxjava.core.AbstractVerticle {

    //TODO: Get searchQuery from user
    private static final String searchQuery = "vertx";

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
                    if (res.succeeded()) {
                        updateSearch();
                        startFuture.complete();
                    } else startFuture.fail("Error occurred");
                });
    }

//    public void getSearchForQuery(RoutingContext routingContext) {
//        searchQuery = routingContext.request().getParam("query");
//        System.out.println(searchQuery);
//        vertx.<QueryResult>executeBlocking(future -> {
//            QueryResult result = null;
//            try {
//                Twitter twitter = TwitterFactory.getSingleton();
//                Query query = new Query("#"+ searchQuery);
//                query.setCount(100);
//                result = twitter.search(query);
//
//            } catch (TwitterException e) {
//                e.printStackTrace();
//            }
//            future.complete(result);
//        }, result -> {
//            HttpServerResponse response = routingContext.response();
//            response.setStatusCode(200).putHeader("Content-Type", "text/html; utf-8");
//            Buffer buffer = Buffer.buffer();
//            if(result.succeeded()) {
//                for (Status status : result.result().getTweets()) {
//                    buffer.appendString(status.getText()+"<br>");
//                }
//            }
//            response.putHeader("Content-Length", buffer.getBytes().length+"");
//            response.write(buffer);
//            response.end();
//        });
//    }

    public void updateSearch() {

        vertx.periodicStream(4000).toObservable()
            .observeOn(RxHelper.schedulerHook(vertx).getNewThreadScheduler())
            .map(v -> {
                QueryResult result = null;
                try {
                    Twitter twitter = TwitterFactory.getSingleton();
                    Query query = new Query("#" + searchQuery);
                    query.setCount(100);
                    result = twitter.search(query);
                } catch (TwitterException e) {
                    Observable.error(e);
                }
                return result;
            })
            .subscribeOn(RxHelper.scheduler(vertx))
            .subscribe(v -> vertx.eventBus().publish("publish.to.client", "Hello")
                    , e -> System.out.println("Error occurred")
            );
    }

    private StaticHandler staticHandler() {
        return StaticHandler.create()
                .setCachingEnabled(false);
    }
}
