package com.maze.models;

import com.google.gson.annotations.SerializedName;

public class RoomData {
    @SerializedName("sala")
    private int room;

    @SerializedName("even")
    private int numberEven;

    @SerializedName("odd")
    private int numberOdd;

    @SerializedName("hora")
    private String hora;

    // Construtor vazio necessário para o GSON
    public RoomData() {
    }

    public RoomData(int room, int numberEven, int numberOdd, String hora) {
        this.room = room;
        this.numberEven = numberEven;
        this.numberOdd = numberOdd;
        this.hora = hora;
    }

    public int getRoom() { return room; }
    public int getNumberEven() { return numberEven; }
    public int getNumberOdd() { return numberOdd; }
    public String getHora() { return hora; }

    public void setRoom(int room) { this.room = room; }
    public void setNumberEven(int numberEven) { this.numberEven = numberEven; }
    public void setNumberOdd(int numberOdd) { this.numberOdd = numberOdd; }
    public void setHora(String hora) { this.hora = hora; }
}
