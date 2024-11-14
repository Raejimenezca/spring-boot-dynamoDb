package com.nequi.franquicias.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.nequi.franquicias.service.FranquiciasServices;

@RestController
@RequestMapping("/api/sucursales")
public class SucursalesController {
    @Autowired
    private FranquiciasServices franquiciaService;

    @PutMapping("/agregarProducto")
    public ResponseEntity<String> agregarProductoSucursal(@RequestParam String sucursalId, @RequestParam String productoId) {
        return franquiciaService.agregarProductoSucursal(sucursalId, productoId);
    }

    @PutMapping("/actualizarNombre")
    public ResponseEntity<String> actualizarNombreSucursal(@RequestParam String sucursalId, @RequestParam String nuevoNombre) {
        return franquiciaService.actualizarNombreSucursal(sucursalId, nuevoNombre);
    }

    @DeleteMapping("/eliminarProducto")
    public ResponseEntity<String> eliminarProductoSucursal(@RequestParam String sucursalId, @RequestParam String productoId) {
        return franquiciaService.eliminarProductoSucursal(sucursalId, productoId);
    }
}
