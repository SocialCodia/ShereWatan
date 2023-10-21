package com.socialcodia.sherewatan.model;

public class ChatModel {
    public String fromUid, toUid, msg, timestamp, mid,type, image;
    public Integer chat_status;

    public ChatModel() {
    }

    public ChatModel(String fromUid, String toUid, String msg, String timestamp, String mid, String type, String image, Integer chat_status) {
        this.fromUid = fromUid;
        this.toUid = toUid;
        this.msg = msg;
        this.timestamp = timestamp;
        this.mid = mid;
        this.type = type;
        this.image = image;
        this.chat_status = chat_status;
    }

    public String getFromUid() {
        return fromUid;
    }

    public void setFromUid(String fromUid) {
        this.fromUid = fromUid;
    }

    public String getToUid() {
        return toUid;
    }

    public void setToUid(String toUid) {
        this.toUid = toUid;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getMid() {
        return mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Integer getChat_status() {
        return chat_status;
    }

    public void setChat_status(Integer chat_status) {
        this.chat_status = chat_status;
    }
}
