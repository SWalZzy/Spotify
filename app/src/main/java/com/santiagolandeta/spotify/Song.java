package com.santiagolandeta.spotify;

import android.graphics.Bitmap;

public class Song {
    private String nombre;
    private String artista;
    private String album;
    private final int imagen;
    private final int cancion;

    public Song(String nombre, String artista, String album, int imagen, int cancion) {
        this.nombre = nombre;
        this.artista = artista;
        this.album = album;
        this.imagen = imagen;
        this.cancion = cancion;
    }

    public String Concatenar(){
        return getNombre() + " - " + getArtista() + " - " + getAlbum();
    }
    public String getNombre() {
        return nombre;
    }

    public int getImagen() {
        return imagen;
    }

    public String getArtista() {
        return artista;
    }

    public String getAlbum() {
        return album;
    }

    public int getCancion() {
        return cancion;
    }
}
