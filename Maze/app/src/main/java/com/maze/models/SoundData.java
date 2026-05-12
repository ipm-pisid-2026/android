package com.maze.models;

import com.google.gson.annotations.SerializedName;

public class SoundData {

    @SerializedName("hora")
    private String hora;

    @SerializedName("som")
    private float value;

    // Construtor vazio (importante para o GSON)
    public SoundData() {
    }

    public SoundData(String hora, float value) {
        this.hora = hora;
        this.value = value;
    }

    public String getHora() {
        return hora;
    }

    public float getValue() {
        return value;
    }

    public void setHora(String hora) {
        this.hora = hora;
    }

    public void setValue(float value) {
        this.value = value;
    }
}