package com.nequi.franquicias.service;

import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.nequi.franquicias.model.AgregarSucursal;
import com.nequi.franquicias.model.Franquicia;
import com.nequi.franquicias.model.GeneralResponse;
import com.nequi.franquicias.model.Sucursal;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeAction;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.AttributeValueUpdate;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest;

@Service
public class FranquiciasServices {

    @Autowired
    private DynamoDbClient dynamoDbClient;

    // Servicio para agregar una nueva franquicia
    public ResponseEntity<String> agregarFranquicia(Franquicia franquicia) {

        GetItemRequest getRequest = GetItemRequest.builder()
                .tableName("franquicias")
                .key(Collections.singletonMap("id", AttributeValue.builder().s(franquicia.getId()).build()))
                .build();
        
        GetItemResponse getResponse = dynamoDbClient.getItem(getRequest);
        
        if (getResponse.hasItem()) {
            return new ResponseEntity<>("La franquicia con id: " + franquicia.getId() + " ya existe", HttpStatus.BAD_REQUEST);
        }
        
        Map<String, AttributeValue> item = new HashMap<>();
        item.put("id", AttributeValue.builder().s(franquicia.getId()).build());
        item.put("nombre", AttributeValue.builder().s(franquicia.getNombre()).build());

        // Serializar las sucursales si están presentes
        if (franquicia.getSucursalIds() != null && !franquicia.getSucursalIds().isEmpty()) {
            List<AttributeValue> sucursalIds = 
            franquicia.getSucursalIds().stream().map(id -> AttributeValue.builder().s(id).build())
            .collect(Collectors.toList());

            item.put("sucursalIds", AttributeValue.builder().l(sucursalIds).build());
        }

        PutItemRequest request = PutItemRequest.builder()
            .tableName("franquicias")
            .item(item)
            .build();   
        try {
            dynamoDbClient.putItem(request);
            return new ResponseEntity<>("La franquicia " + franquicia.getId() + " ha sido agregada correctamente", HttpStatus.OK);
        } catch(Exception e) {
            System.out.println("Exception: " + e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Servicio para agregar una nueva sucursal a una franquicia
    public ResponseEntity<String> agregarSucursalFranquicia(String franquiciaId, String idSucursal) {

        System.out.println(franquiciaId + idSucursal);
        GetItemRequest getRequest = GetItemRequest.builder()
                .tableName("franquicias")
                .key(Collections.singletonMap("id", AttributeValue.builder().s(franquiciaId).build()))
                .build();
        
        GetItemResponse getResponse = dynamoDbClient.getItem(getRequest);
        
        if (!getResponse.hasItem()) {
            return new ResponseEntity<>("Franquicia con ID " + franquiciaId + " no encontrada", HttpStatus.NOT_FOUND);
        }
        
        // Obtener la lista de sucursales actual y actualizarla
        List<AttributeValue> sucursalIdlist = new ArrayList<>(getResponse.item().getOrDefault("sucursalIds", AttributeValue.builder().l(new ArrayList<>()).build()).l());
        System.out.println("SucursalIds" + sucursalIdlist);
        sucursalIdlist.add(AttributeValue.builder().s(idSucursal).build());
 
        // Actualizar la franquicia con la nueva lista de sucursales
        Map<String, AttributeValueUpdate> updateFranquicia = new HashMap<>();
        updateFranquicia.put("sucursalIds", AttributeValueUpdate.builder()
                .value(AttributeValue.builder().l(sucursalIdlist).build())
                .action(AttributeAction.PUT)
                .build());
 
        UpdateItemRequest updateRequest = UpdateItemRequest.builder()
                .tableName("franquicias")
                .key(Collections.singletonMap("id", AttributeValue.builder().s(franquiciaId).build()))
                .attributeUpdates(updateFranquicia)
                .build();
        
                try {
                    dynamoDbClient.updateItem(updateRequest);
                    System.out.println("Sucursales de la franquicia " + franquiciaId + " ha sido actualizada");
                    return new ResponseEntity<>("Sucursales de la franquicia " + franquiciaId + " han sido actualizadas", HttpStatus.OK);
                } catch(Exception e) {
                    return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
                }
    }
    
}
