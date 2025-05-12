package com.example.mygpt.infrastructure.dtos;

public class ShareLinkResponse {
    private String link;

    public ShareLinkResponse() {
    }

    public ShareLinkResponse(String link) {
        this.link = link;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }
} 