function init() {
    loadTweets();
}

function loadTweets() {
    var eventBus = new EventBus('/eventbus/');
    eventBus.onopen = function () {
        console.log("Inside onopen");
        eventBus.registerHandler("publish.to.client", function (error, message) {
            console.log("Message received: " + JSON.stringify(message));
            document.getElementById('container').innerHTML += '<div class="col s12 m4"> \
                <div class="card blue-grey darken-1"> \
                <div class="card-content white-text"> \
                <span class="card-title">Card Title</span> \
            <p>' + message.body + '</p> \
            </div> \
            <div class="card-action"> \
                <a href="#">This is a link</a> \
            <a href="#">This is a link</a> \
            </div> \
            </div> \
            </div>';
        });
    };
};