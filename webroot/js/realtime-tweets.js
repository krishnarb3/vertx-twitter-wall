function init() {
    loadTweets();
}

function loadTweets() {
    var eventBus = new EventBus('/eventbus/');
    eventBus.onopen = function () {
        console.log("Inside onopen");
        eventBus.registerHandler("publish.to.client", function (error, response) {
            document.getElementById('container').innerHTML = '';
            tweetlist = JSON.parse(response.body);
            console.log(tweetlist);
            for(i=0; i<tweetlist.length; i++) {
                appendTweet(tweetlist[i]);
            }

        });
    };
};

function appendTweet(tweet) {
    document.getElementById('container').innerHTML += '<div class="col s12 m4"> \
                <div class="card blue-grey darken-1"> \
                <div class="card-content white-text"> \
                <span class="card-title">' + tweet.user + '</span> \
            <p>' + tweet.text + '</p> \
            </div> \
            <div class="card-action"> \
                <a href="#">' + tweet.date + '</a> \
                <a href="#">This is a link</a> \
            </div> \
            </div> \
            </div>';
}