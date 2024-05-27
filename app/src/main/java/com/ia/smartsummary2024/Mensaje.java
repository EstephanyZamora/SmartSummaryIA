package com.ia.smartsummary2024;

public class Mensaje {
    private String message;

    public Mensaje() {
    }

    public Mensaje(String message) {
        this.message = message;

    }


    public String getMessage() {
        return message;
    }
    public String getNombreMensaje() {
        return "message";
    }

    public void setMessage(String message) {
        this.message = message;
    }


}
