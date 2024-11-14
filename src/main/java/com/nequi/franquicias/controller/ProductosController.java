package com.nequi.franquicias.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.nequi.franquicias.service.FranquiciasServices;

@RestController
@RequestMapping("/api/productos")
public class ProductosController {

    @Autowired
    private FranquiciasServices franquiciaService;
    
    @PutMapping("/modificarStock")
    public ResponseEntity<String> modificarStockProducto(@RequestParam String productoId, @RequestParam int stock) {
        return franquiciaService.modificarStockProducto(productoId, stock);
    }

    @PutMapping("/actualizarNombre")
    public ResponseEntity<String> actualizarNombreProducto(@RequestParam String productoId, @RequestParam String nuevoNombre) {
        return franquiciaService.actualizarNombreProducto(productoId, nuevoNombre);
    }
}
