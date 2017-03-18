package io.vertx.twitter;

import com.google.gson.annotations.SerializedName;

public class TweetCustom {

    @SerializedName("user")
    private String mUser;
    @SerializedName("text")
    private String mTweetText;
    @SerializedName("date")
    private String mTweetDate;

    public TweetCustom(String user, String tweetText, String tweetDate) {
        mUser = user;
        mTweetText = tweetText;
        mTweetDate = tweetDate;
    }

    public String getUser() {
        return mUser;
    }

    public void setUser(String mUser) {
        this.mUser = mUser;
    }

    public String getTweetText() {
        return mTweetText;
    }

    public void setTweetText(String mTweetText) {
        this.mTweetText = mTweetText;
    }

    public String getTweetDate() {
        return mTweetDate;
    }

    public void setTweetDate(String mTweetDate) {
        this.mTweetDate = mTweetDate;
    }
}
