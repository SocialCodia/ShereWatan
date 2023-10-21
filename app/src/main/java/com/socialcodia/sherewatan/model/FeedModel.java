package com.socialcodia.sherewatan.model;

public class FeedModel {
    public String feed_content, feed_image, name, uid, image,feed_timestamp, feed_id;


    public FeedModel() {
    }

    public FeedModel(String feed_content, String feed_image, String name, String uid, String image, String feed_timestamp, String feed_id) {
        this.feed_content = feed_content;
        this.feed_image = feed_image;
        this.name = name;
        this.uid = uid;
        this.image = image;
        this.feed_timestamp = feed_timestamp;
        this.feed_id = feed_id;
    }

    public String getFeed_content() {
        return feed_content;
    }

    public void setFeed_content(String feed_content) {
        this.feed_content = feed_content;
    }

    public String getFeed_image() {
        return feed_image;
    }

    public void setFeed_image(String feed_image) {
        this.feed_image = feed_image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getFeed_timestamp() {
        return feed_timestamp;
    }

    public void setFeed_timestamp(String feed_timestamp) {
        this.feed_timestamp = feed_timestamp;
    }

    public String getFeed_id() {
        return feed_id;
    }

    public void setFeed_id(String feed_id) {
        this.feed_id = feed_id;
    }
}
