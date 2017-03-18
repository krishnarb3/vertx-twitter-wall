function init() {
    loadTweets();
}

function loadTweets() {
    var eventBus = new EventBus('/eventbus/');
    eventBus.onopen = function () {
        console.log("Inside onopen");
        eventBus.registerHandler("publish.to.client", function (error, message) {
            console.log("Inside register Handler");
        });
    };
};