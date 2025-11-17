package com.example.firebaseapp.model;

public class Mensaje {
    private String de;
    private String deName;
    private String texto;
    private long timestamp;

    public Mensaje() {}

    public Mensaje(String de, String deName, String texto, long timestamp) {
        this.de = de;
        this.deName = deName;
        this.texto = texto;
        this.timestamp = timestamp;
    }

    public String getDe() { return de; }
    public void setDe(String de) { this.de = de; }

    public String getDeName() { return deName; }
    public void setDeName(String deName) { this.deName = deName; }

    public String getTexto() { return texto; }
    public void setTexto(String texto) { this.texto = texto; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
