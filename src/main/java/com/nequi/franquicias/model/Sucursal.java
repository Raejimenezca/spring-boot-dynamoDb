package com.nequi.franquicias.model;

import java.util.List;
import java.util.UUID;

import lombok.Data;

@Data
public class Sucursal {
    private String id = UUID.randomUUID().toString();
    private String nombre;
    private List<Producto> productos;
}
