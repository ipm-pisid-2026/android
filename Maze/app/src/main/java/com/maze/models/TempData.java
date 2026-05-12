package com.maze.models;

import com.google.gson.annotations.SerializedName;

public class TempData {

    // Deve coincidir com 'idtemperatura' do seu SELECT
    @SerializedName("hora")
    private String hora;

    // Deve coincidir com 'temperatura' do seu SELECT
    @SerializedName("temperatura")
    private float value;

    // Construtor vazio (necessário para o GSON)
    public TempData() {
    }

    public TempData(String hora, float value) {
        this.hora = hora;
        this.value = value;
    }

    // Getters
    public String getHora() {
        return hora;
    }

    public float getValue() {
        return value;
    }

    // Setters (opcional, mas recomendado)
    public void setHora(String hora) {
        this.hora = hora;
    }

    public void setValue(float value) {
        this.value = value;
    }
}
