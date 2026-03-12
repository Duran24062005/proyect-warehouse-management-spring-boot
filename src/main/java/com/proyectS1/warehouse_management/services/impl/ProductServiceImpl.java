package com.proyectS1.warehouse_management.services.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.proyectS1.warehouse_management.dtos.request.ProductRequestDTO;
import com.proyectS1.warehouse_management.dtos.response.ProductResponseDTO;
import com.proyectS1.warehouse_management.mapper.ProductMapper;
import com.proyectS1.warehouse_management.model.Product;
import com.proyectS1.warehouse_management.model.Warehouse;
import com.proyectS1.warehouse_management.repositories.ProductRepository;
import com.proyectS1.warehouse_management.repositories.WarehouseRepository;
import com.proyectS1.warehouse_management.services.ProductService;

import lombok.RequiredArgsConstructor;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductServiceImpl implements ProductService {

    private static final int LOW_STOCK_THRESHOLD = 5;

    private final ProductMapper productMapper;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;

    @Override
    public ProductResponseDTO saveProduct(ProductRequestDTO dto) {
        Product newProduct = this.productMapper.DTOToEntity(dto);
        newProduct.setWarehouse(resolveWarehouse(dto.warehouseId()));
        Product saveProduct = this.productRepository.save(newProduct);
        return this.productMapper.entityToDTO(saveProduct);
    }

    @Override
    public ProductResponseDTO updateProduct(ProductRequestDTO dto, Long id) {
        Product currentProduct = findProductById(id);
        this.productMapper.updateEntityFromDTO(currentProduct, dto);
        currentProduct.setWarehouse(resolveWarehouse(dto.warehouseId()));
        Product updatedProduct = this.productRepository.save(currentProduct);
        return this.productMapper.entityToDTO(updatedProduct);
    }

    @Override
    public void deleteProduct(Long id) {
        Product currentProduct = findProductById(id);
        this.productRepository.delete(currentProduct);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponseDTO> findAll() {
        List<Product> products = this.productRepository.findAll();
        return products.stream().map(this.productMapper::entityToDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponseDTO findOne(Long id) {
        Product product = findProductById(id);
        return this.productMapper.entityToDTO(product);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponseDTO> findLowStock() {
        return this.productRepository.findLowStockProducts(LOW_STOCK_THRESHOLD)
            .stream()
            .map(this.productMapper::entityToDTO)
            .toList();
    }

    private Product findProductById(Long id) {
        return this.productRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Product not found with id " + id));
    }

    private Warehouse resolveWarehouse(Long warehouseId) {
        if (warehouseId == null) {
            return null;
        }

        return this.warehouseRepository.findById(warehouseId)
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Warehouse not found with id " + warehouseId));
    }
    
}
