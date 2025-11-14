package com.example.firebaseapp.model;

public class Mensaje {

    private String de;
    private String para;
    private String texto;
    private long timestamp;

    // Constructor vacío requerido por Firebase
    public Mensaje() {
    }

    // Constructor completo
    public Mensaje(String de, String para, String texto, long timestamp) {
        this.de = de;
        this.para = para;
        this.texto = texto;
        this.timestamp = timestamp;
    }

    public String getDe() {
        return de;
    }

    public String getPara() {
        return para;
    }

    public String getTexto() {
        return texto;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setDe(String de) {
        this.de = de;
    }

    public void setPara(String para) {
        this.para = para;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
