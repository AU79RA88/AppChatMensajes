package com.example.firebaseapp.model;

import java.util.HashMap;
import java.util.Map;

public class Usuario {

    private String uid;
    private String nombre;
    private String correo;
    private String fotoUrl;
    private Map<String, Boolean> contacts = new HashMap<>();

    public Usuario() {}

    public Usuario(String uid, String nombre, String correo, String fotoUrl) {
        this.uid = uid;
        this.nombre = nombre;
        this.correo = correo;
        this.fotoUrl = fotoUrl;
        this.contacts = new HashMap<>();
    }

    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getFotoUrl() { return fotoUrl; }
    public void setFotoUrl(String fotoUrl) { this.fotoUrl = fotoUrl; }

    public Map<String, Boolean> getContacts() { return contacts; }
    public void setContacts(Map<String, Boolean> contacts) { this.contacts = contacts; }

    public void addContact(String contactUid) {
        if (!contacts.containsKey(contactUid)) {
            contacts.put(contactUid, true);
        }
    }
}
