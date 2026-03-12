package com.proyectS1.warehouse_management.services;

import java.util.List;

import com.proyectS1.warehouse_management.dtos.request.ProductRequestDTO;
import com.proyectS1.warehouse_management.dtos.response.ProductResponseDTO;

public interface  ProductService {
    ProductResponseDTO saveProduct(ProductRequestDTO dto);
    ProductResponseDTO updateProduct(ProductRequestDTO dto, Long id);
    void deleteProduct(Long id);
    List<ProductResponseDTO> findAll();
    ProductResponseDTO findOne(Long id);
    List<ProductResponseDTO> findLowStock();
}
