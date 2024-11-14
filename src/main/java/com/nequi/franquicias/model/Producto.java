package com.nequi.franquicias.model;

import java.util.UUID;

import lombok.Data;

@Data
public class Producto {
    private String id = UUID.randomUUID().toString();
    private String nombre;
    private int stock;
}
