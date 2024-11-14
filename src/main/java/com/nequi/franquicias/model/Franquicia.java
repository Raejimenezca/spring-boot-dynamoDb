package com.nequi.franquicias.model;

import java.util.List;
import java.util.UUID;

import lombok.Data;

@Data
public class Franquicia {
    private String id = UUID.randomUUID().toString();
    private String nombre;
    private List<Sucursal> sucursales;
}
