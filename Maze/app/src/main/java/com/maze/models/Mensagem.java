package com.maze.models;

import com.google.gson.annotations.SerializedName;

public class Mensagem {
    @SerializedName("id")
    private int id;

    @SerializedName("tipo_alerta")
    private String tipoalerta;

    @SerializedName("hora")
    private String date;

    @SerializedName("msg")
    private String text;

    @SerializedName("leitura")
    private float value;

    @SerializedName("sensor")
    private String sensor;

    @SerializedName("sala")
    private int sala;

    @SerializedName("hora_escrita")
    private String horaEscrita;

    // Construtor vazio necessário para o GSON
    public Mensagem() {}

    // Getters
    public int getId() { return id; }
    public String getDate() { return date; }
    public String getText() { return text; }
    public float getValue() { return value; }
    public String getSensor() { return sensor; }
    public String getTipoAlerta() { return tipoalerta; }
    public int getSala() { return sala; }
    public String getHoraEscrita() { return horaEscrita; }

    public int getMessagetype() {
        if (tipoalerta == null) return 0;
        if (tipoalerta.contains("ALARMING")) return 1;
        if (tipoalerta.contains("CRITICAL")) return 2;
        return 3;
    }
}