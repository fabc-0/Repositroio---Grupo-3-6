package com.misiontic.peligrapp.models;

public class RiesgoModel {

    //public Integer idriesgo;
    public String idriesgo;
    public String descripcion;
    public String latitud;
    public String longitud;
    public String idusuario;
    public String estado;
    public String imagen;

    public RiesgoModel() {
    }

    public RiesgoModel(String idriesgo, String descripcion, String latitud, String longitud, String idusuario, String estado, String imagen) {
        this.idriesgo = idriesgo;
        this.descripcion = descripcion;
        this.latitud = latitud;
        this.longitud = longitud;
        this.idusuario = idusuario;
        this.estado = estado;
        this.imagen = imagen;
    }

    public String getIdriesgo() {
        return idriesgo;
    }

    public void setIdriesgo(String idriesgo) {
        this.idriesgo = idriesgo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getLatitud() {
        return latitud;
    }

    public void setLatitud(String latitud) {
        this.latitud = latitud;
    }

    public String getLongitud() {
        return longitud;
    }

    public void setLongitud(String longitud) {
        this.longitud = longitud;
    }

    public String getIdusuario() {
        return idusuario;
    }

    public void setIdusuario(String idusuario) {
        this.idusuario = idusuario;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getImagen() {
        return imagen;
    }

    public void setImagen(String imagen) {
        this.imagen = imagen;
    }
}
