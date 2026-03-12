package com.proyectS1.warehouse_management.mapper;

import org.springframework.stereotype.Component;

import com.proyectS1.warehouse_management.dtos.request.ProductRequestDTO;
import com.proyectS1.warehouse_management.dtos.response.ProductResponseDTO;
import com.proyectS1.warehouse_management.model.Product;

@Component
public class ProductMapper {
    public ProductResponseDTO entityToDTO(Product product){
        if (product == null) return null;
        return new ProductResponseDTO(
            product.getId(),
            product.getName(),
            product.getCategory(),
            product.getPrice(),
            product.getWarehouse() != null ? product.getWarehouse().getId() : null,
            product.getWarehouse() != null ? product.getWarehouse().getName() : null
        );
    }

    public Product DTOToEntity(ProductRequestDTO dto){
        if (dto == null) return null;
        Product product = new Product();
        product.setName(dto.name());
        product.setCategory(dto.category());
        product.setPrice(dto.price());
        return product;
    }

    public void updateEntityFromDTO(Product product, ProductRequestDTO dto){
        if (product == null || dto == null) return;
        product.setName(dto.name());
        product.setCategory(dto.category());
        product.setPrice(dto.price());
    }
}
