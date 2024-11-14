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

import com.nequi.franquicias.model.Franquicia;
import com.nequi.franquicias.model.SucursalProducto;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeAction;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.AttributeValueUpdate;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
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
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Servicio para agregar una nueva sucursal a una franquicia
    public ResponseEntity<String> agregarSucursalFranquicia(String franquiciaId, String idSucursal) {

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
                    return new ResponseEntity<>("Sucursales de la franquicia " + franquiciaId + " han sido actualizadas", HttpStatus.OK);
                } catch(Exception e) {
                    return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
                }
    }

    // Servicio para agregar un nuevo producto a una sucursal
    public ResponseEntity<String> agregarProductoSucursal(String sucursalId, String productoId) {

        GetItemRequest getRequest = GetItemRequest.builder()
                .tableName("sucursales")
                .key(Collections.singletonMap("id", AttributeValue.builder().s(sucursalId).build()))
                .build();
        
        GetItemResponse getResponse = dynamoDbClient.getItem(getRequest);
        
        if (!getResponse.hasItem()) {
            return new ResponseEntity<>("Sucursal con ID " + sucursalId + " no encontrada", HttpStatus.NOT_FOUND);
        }
        
        // Obtener la lista de productos actual y actualizarla
        List<AttributeValue> productsList = new ArrayList<>(getResponse.item().getOrDefault("productos", AttributeValue.builder().l(new ArrayList<>()).build()).l());
        productsList.add(AttributeValue.builder().s(productoId).build());
 
        // Actualizar la sucursal con la nueva lista de productos
        Map<String, AttributeValueUpdate> updateSucursal = new HashMap<>();
        updateSucursal.put("productos", AttributeValueUpdate.builder()
                .value(AttributeValue.builder().l(productsList).build())
                .action(AttributeAction.PUT)
                .build());
 
        UpdateItemRequest updateRequest = UpdateItemRequest.builder()
                .tableName("sucursales")
                .key(Collections.singletonMap("id", AttributeValue.builder().s(sucursalId).build()))
                .attributeUpdates(updateSucursal)
                .build();
        
                try {
                    dynamoDbClient.updateItem(updateRequest);
                    return new ResponseEntity<>("Productos de la sucursal " + sucursalId + " han sido actualizados", HttpStatus.OK);
                } catch(Exception e) {
                    return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
                }
    }

    // Servicio para eliminar un producto de una sucursal
    public ResponseEntity<String> eliminarProductoSucursal(String sucursalId, String productoId) {

        GetItemRequest getRequest = GetItemRequest.builder()
                .tableName("sucursales")
                .key(Collections.singletonMap("id", AttributeValue.builder().s(sucursalId).build()))
                .build();
        
        GetItemResponse getResponse = dynamoDbClient.getItem(getRequest);
        
        if (!getResponse.hasItem()) {
            return new ResponseEntity<>("Sucursal con ID " + sucursalId + " no encontrada", HttpStatus.NOT_FOUND);
        }
        
        // Obtener la lista de productos actual y actualizarla
        List<AttributeValue> productsList = new ArrayList<>(getResponse.item().getOrDefault("productos", AttributeValue.builder().l(new ArrayList<>()).build()).l());
        
        // Filtrar para eliminar el producto
        List<AttributeValue> productosActualizados = productsList.stream()
        .filter(producto -> !producto.s().equals(productoId))
        .collect(Collectors.toList());
 
        // Actualizar la sucursal con la nueva lista de productos
        Map<String, AttributeValueUpdate> updateSucursal = new HashMap<>();
        updateSucursal.put("productos", AttributeValueUpdate.builder()
                .value(AttributeValue.builder().l(productosActualizados).build())
                .action(AttributeAction.PUT)
                .build());
 
        UpdateItemRequest updateRequest = UpdateItemRequest.builder()
                .tableName("sucursales")
                .key(Collections.singletonMap("id", AttributeValue.builder().s(sucursalId).build()))
                .attributeUpdates(updateSucursal)
                .build();
        
                try {
                    dynamoDbClient.updateItem(updateRequest);
                    return new ResponseEntity<>("Productos de la sucursal " + sucursalId + " han sido actualizados", HttpStatus.OK);
                } catch(Exception e) {
                    return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
                }
    }

    // Servicio para modificar el stock de un producto
    public ResponseEntity<String> modificarStockProducto(String productoId, int nuevoStock) {

        GetItemRequest getRequest = GetItemRequest.builder()
                .tableName("productos")
                .key(Collections.singletonMap("id", AttributeValue.builder().s(productoId).build()))
                .build();
        
        GetItemResponse getResponse = dynamoDbClient.getItem(getRequest);
        
        if (!getResponse.hasItem()) {
            return new ResponseEntity<>("Producto con ID " + productoId + " no encontrado", HttpStatus.NOT_FOUND);
        }
        
        // Obtener el stock del producto para modificarlo
        UpdateItemRequest updateRequest = UpdateItemRequest.builder()
                .tableName("productos")
                .key(Map.of("id", AttributeValue.builder().s(productoId).build()))
                .updateExpression("SET stock = :nuevoStock")
                .expressionAttributeValues(Map.of(":nuevoStock", AttributeValue.builder().n(String.valueOf(nuevoStock)).build()))
                .build();
 
        try {
            // Ejecutar la solicitud de actualización
            dynamoDbClient.updateItem(updateRequest);
            return new ResponseEntity<>("Stock del producto " + productoId + " ha sido actualizado", HttpStatus.OK);
        } catch (DynamoDbException e) {
            e.printStackTrace();
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    // Servicio para actualizar el nombre de una franquicia
    public ResponseEntity<String> actualizarNombreFranquicia(String franquiciaId, String nuevoNombre) {

        GetItemRequest getRequest = GetItemRequest.builder()
                .tableName("franquicias")
                .key(Collections.singletonMap("id", AttributeValue.builder().s(franquiciaId).build()))
                .build();
        
        GetItemResponse getResponse = dynamoDbClient.getItem(getRequest);
        
        if (!getResponse.hasItem()) {
            return new ResponseEntity<>("Franquicia con ID " + franquiciaId + " no encontrada", HttpStatus.NOT_FOUND);
        }
        
        // Obtener el nombre de la franquicia para actualizarlo
        UpdateItemRequest updateRequest = UpdateItemRequest.builder()
                .tableName("franquicias")
                .key(Map.of("id", AttributeValue.builder().s(franquiciaId).build()))
                .updateExpression("SET nombre = :nuevoNombre")
                .expressionAttributeValues(Map.of(":nuevoNombre", AttributeValue.builder().s(String.valueOf(nuevoNombre)).build()))
                .build();
 
        try {
            // Ejecutar la solicitud de actualización
            dynamoDbClient.updateItem(updateRequest);
            return new ResponseEntity<>("Nombre de la franquicia " + franquiciaId + " ha sido actualizado", HttpStatus.OK);
        } catch (DynamoDbException e) {
            e.printStackTrace();
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Servicio para actualizar el nombre de una sucursal
    public ResponseEntity<String> actualizarNombreSucursal(String sucursalId, String nuevoNombre) {

        GetItemRequest getRequest = GetItemRequest.builder()
                .tableName("sucursales")
                .key(Collections.singletonMap("id", AttributeValue.builder().s(sucursalId).build()))
                .build();
        
        GetItemResponse getResponse = dynamoDbClient.getItem(getRequest);
        
        if (!getResponse.hasItem()) {
            return new ResponseEntity<>("Sucursal con ID " + sucursalId + " no encontrada", HttpStatus.NOT_FOUND);
        }
        
        // Obtener el nombre de la sucursal para actualizarlo
        UpdateItemRequest updateRequest = UpdateItemRequest.builder()
                .tableName("sucursales")
                .key(Map.of("id", AttributeValue.builder().s(sucursalId).build()))
                .updateExpression("SET nombre = :nuevoNombre")
                .expressionAttributeValues(Map.of(":nuevoNombre", AttributeValue.builder().s(String.valueOf(nuevoNombre)).build()))
                .build();
 
        try {
            // Ejecutar la solicitud de actualización
            dynamoDbClient.updateItem(updateRequest);
            return new ResponseEntity<>("Nombre de la sucursal " + sucursalId + " ha sido actualizado", HttpStatus.OK);
        } catch (DynamoDbException e) {
            e.printStackTrace();
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Servicio para actualizar el nombre de un producto
    public ResponseEntity<String> actualizarNombreProducto(String productoId, String nuevoNombre) {

        GetItemRequest getRequest = GetItemRequest.builder()
                .tableName("productos")
                .key(Collections.singletonMap("id", AttributeValue.builder().s(productoId).build()))
                .build();
        
        GetItemResponse getResponse = dynamoDbClient.getItem(getRequest);
        
        if (!getResponse.hasItem()) {
            return new ResponseEntity<>("Producto con ID " + productoId + " no encontrado", HttpStatus.NOT_FOUND);
        }
        
        // Obtener el nombre del producto para actualizarlo
        UpdateItemRequest updateRequest = UpdateItemRequest.builder()
                .tableName("productos")
                .key(Map.of("id", AttributeValue.builder().s(productoId).build()))
                .updateExpression("SET nombre = :nuevoNombre")
                .expressionAttributeValues(Map.of(":nuevoNombre", AttributeValue.builder().s(String.valueOf(nuevoNombre)).build()))
                .build();
 
        try {
            dynamoDbClient.updateItem(updateRequest);
            return new ResponseEntity<>("Nombre del producto " + productoId + " ha sido actualizado", HttpStatus.OK);
        } catch (DynamoDbException e) {
            e.printStackTrace();
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Consulta las sucursales con su mayor producto en Stock para una franquicia dada
    public ResponseEntity<List<SucursalProducto>> consultaMaxProductosStock(String franquiciaId) {

        List<SucursalProducto> sucursalProductos = new ArrayList<>();

        GetItemRequest getRequest = GetItemRequest.builder()
                .tableName("franquicias")
                .key(Collections.singletonMap("id", AttributeValue.builder().s(franquiciaId).build()))
                .build();
        
        GetItemResponse getResponse = dynamoDbClient.getItem(getRequest);
        
        if (!getResponse.hasItem()) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }

        List<AttributeValue> sucursalesList = new ArrayList<>(getResponse.item().getOrDefault("sucursalIds", AttributeValue.builder().l(new ArrayList<>()).build()).l());

        for (AttributeValue idSucursal: sucursalesList) {
            GetItemRequest getRequest2 = GetItemRequest.builder()
                .tableName("sucursales")
                .key(Collections.singletonMap("id", AttributeValue.builder().s(idSucursal.s()).build()))
                .build();
        
            GetItemResponse getResponse2 = dynamoDbClient.getItem(getRequest2);
            
            if (!getResponse2.hasItem()) {
                return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
            }

            List<AttributeValue> productosList = new ArrayList<>(getResponse2.item().getOrDefault("productos", AttributeValue.builder().l(new ArrayList<>()).build()).l());

            String productWithMaxStock = null;
            int maxStock = -1; // Inicializamos con un valor bajo

            for (AttributeValue prod: productosList) {

                GetItemRequest getRequest3 = GetItemRequest.builder()
                .tableName("productos")
                .key(Collections.singletonMap("id", AttributeValue.builder().s(prod.s()).build()))
                .build();
        
                GetItemResponse getResponse3 = dynamoDbClient.getItem(getRequest3);
                
                if (!getResponse3.hasItem()) {
                    return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
                }
                    AttributeValue stockAttribute = getResponse3.item().get("stock");
                    if (stockAttribute != null && stockAttribute.n() != null) {
                        int stock = Integer.parseInt(stockAttribute.n());
                        
                        // Verificar si el producto tiene un stock mayor
                        if (stock > maxStock) {
                            maxStock = stock;
                            AttributeValue stockAttribute2 = getResponse3.item().get("nombre");
                            productWithMaxStock = stockAttribute2.s();
                        }
                    }
            }
            SucursalProducto sucProd = new SucursalProducto(getResponse2.item().get("nombre").s(), productWithMaxStock);
            sucursalProductos.add(sucProd);
        }
        return new ResponseEntity<>(sucursalProductos, HttpStatus.OK);
    }
}
