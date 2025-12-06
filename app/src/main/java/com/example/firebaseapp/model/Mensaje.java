package com.example.firebaseapp.model;

public class Mensaje {

    private String id;
    private String de;
    private String deName;
    private String texto;
    private long timestamp;
    private String imageUrl;

    public Mensaje() {

    }

    public Mensaje(String id, String de, String deName, String texto, long timestamp, String imageUrl) {
        this.id = id;
        this.de = de;
        this.deName = deName;
        this.texto = texto;
        this.timestamp = timestamp;
        this.imageUrl = imageUrl;
    }



    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDe() {
        return de;
    }

    public void setDe(String de) {
        this.de = de;
    }

    public String getDeName() {
        return deName;
    }

    public void setDeName(String deName) {
        this.deName = deName;
    }

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
