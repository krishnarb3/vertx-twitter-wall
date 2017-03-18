# vertx-twitter-wall
Prerequisite task for GSOC-2017 under Eclipse Vertx

<h2>The Twitter wall application:</h2>

* Retrieves tweets (matching configurable query) from Twitter using the Twitter API,
* Displays the tweets in a web page,
* Polls the API periodically (configurable) to update the tweets,
* Send the new tweets to the web page using the SockJS bridge,
* Update the displayed tweets on the web page dynamically (no reload of the page, consume the messages from the SockJS bridge).

Uses RxJava, twitter4j and Gson.

TODO: 
- [ ] Multiple hashtags subscription
- [ ] Multiple user support (Distinct subscriptions)
- [x] Basic UI
- [ ] Profile pic and urls

Usage:
```
./gradlew run
```
