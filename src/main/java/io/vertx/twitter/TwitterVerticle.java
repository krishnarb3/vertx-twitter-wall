package io.vertx.twitter;

import com.google.gson.Gson;
import io.vertx.core.Future;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.PermittedOptions;
import io.vertx.rxjava.core.RxHelper;
import io.vertx.rxjava.ext.web.Router;
import io.vertx.rxjava.ext.web.RoutingContext;
import io.vertx.rxjava.ext.web.handler.StaticHandler;
import io.vertx.rxjava.ext.web.handler.sockjs.SockJSHandler;
import rx.Observable;
import rx.Subscription;
import twitter4j.*;

import java.util.List;
import java.util.stream.Collectors;

public class TwitterVerticle extends io.vertx.rxjava.core.AbstractVerticle {

    Subscription subscription;

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
        router.route("/api/:query").handler(this::getSearchForQuery);
        router.route().handler(staticHandler());


        vertx.createHttpServer()
                .requestHandler(router::accept)
                .listen(9090, res -> {
                    if (res.succeeded()) {
                        updateSearch("vertx");
                        startFuture.complete();
                    } else startFuture.fail("Error occurred");
                });
    }

    public void getSearchForQuery(RoutingContext routingContext) {
        updateSearch(routingContext.request().getParam("query"));
    }

    public void updateSearch(String searchQuery) {

        if(subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
        }

        subscription = vertx.periodicStream(4000).toObservable()
                .observeOn(RxHelper.schedulerHook(vertx).getNewThreadScheduler())
                .map(v -> {
                    String result = null;
                    try {
                        Twitter twitter = TwitterFactory.getSingleton();
                        Query query = new Query("#" + searchQuery);
                        query.setCount(100);
                        QueryResult queryResult = twitter.search(query);
                        List<TweetCustom> tweets = queryResult
                                .getTweets().stream()
                                .map(tweet -> new TweetCustom(tweet.getUser().getName(), tweet.getText(), tweet.getCreatedAt().toString()))
                                .collect(Collectors.toList());
                        result = new Gson().toJson(tweets);
                    } catch (TwitterException e) {
                        Observable.error(e);
                    }
                    return result;
                })
                .subscribeOn(RxHelper.scheduler(vertx))
                .subscribe(res -> vertx.eventBus().publish("publish.to.client", res)
                        , e -> System.out.println("Error occurred")
                );
    }

    private StaticHandler staticHandler() {
        return StaticHandler.create()
                .setCachingEnabled(false);
    }
}
