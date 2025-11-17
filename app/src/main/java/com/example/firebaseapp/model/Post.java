package com.example.firebaseapp.model;

public class Post {
    private String id;
    private String authorId;
    private String authorName;
    private String texto;
    private long timestamp;

    public Post() {}

    public Post(String authorId, String authorName, String texto, long timestamp) {
        this.authorId = authorId;
        this.authorName = authorName;
        this.texto = texto;
        this.timestamp = timestamp;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getAuthorId() { return authorId; }
    public void setAuthorId(String authorId) { this.authorId = authorId; }

    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }

    public String getTexto() { return texto; }
    public void setTexto(String texto) { this.texto = texto; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
