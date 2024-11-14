package com.nequi.franquicias.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.nequi.franquicias.model.Franquicia;
import com.nequi.franquicias.service.FranquiciasServices;

@RestController
@RequestMapping("/api/franquicias")
public class FranquiciasController {

    @Autowired
    private FranquiciasServices franquiciaService;
    
    @PostMapping("/agregarFranquicia")
    public ResponseEntity<String> agregarFranquicia(@RequestBody Franquicia franquicia) {
        return franquiciaService.agregarFranquicia(franquicia);
    }

    @PutMapping("/agregarSucursal")
    public ResponseEntity<String> agregarSucursalFranquicia(@RequestParam String franquiciaId, @RequestParam String idSucursal) {
        return franquiciaService.agregarSucursalFranquicia(franquiciaId, idSucursal);
    }

    @PutMapping("/actualizarNombre")
    public ResponseEntity<String> actualizarNombreFranquicia(@RequestParam String franquiciaId, @RequestParam String nuevoNombre) {
        return franquiciaService.actualizarNombreFranquicia(franquiciaId, nuevoNombre);
    }
}
