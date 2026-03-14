package com.proyectS1.warehouse_management.services.impl;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.proyectS1.warehouse_management.dtos.request.ProductRequestDTO;
import com.proyectS1.warehouse_management.dtos.response.ProductResponseDTO;
import com.proyectS1.warehouse_management.mapper.ProductMapper;
import com.proyectS1.warehouse_management.model.AppUser;
import com.proyectS1.warehouse_management.model.Product;
import com.proyectS1.warehouse_management.model.Warehouse;
import com.proyectS1.warehouse_management.repositories.ProductRepository;
import com.proyectS1.warehouse_management.repositories.WarehouseRepository;
import com.proyectS1.warehouse_management.services.ProductService;
import com.proyectS1.warehouse_management.services.support.AuditService;
import com.proyectS1.warehouse_management.services.support.WarehouseAccessService;

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
    private final WarehouseAccessService warehouseAccessService;
    private final AuditService auditService;

    @Override
    public ProductResponseDTO saveProduct(ProductRequestDTO dto) {
        AppUser currentUser = warehouseAccessService.getCurrentUser();
        warehouseAccessService.requireWarehouseAccess(currentUser, dto.warehouseId());
        Product newProduct = this.productMapper.DTOToEntity(dto);
        newProduct.setWarehouse(resolveWarehouse(dto.warehouseId()));
        Product saveProduct = this.productRepository.save(newProduct);
        ProductResponseDTO response = this.productMapper.entityToDTO(saveProduct);
        auditService.logInsert("product", "Catalog for products", currentUser, response);
        return response;
    }

    @Override
    public ProductResponseDTO updateProduct(ProductRequestDTO dto, Long id) {
        AppUser currentUser = warehouseAccessService.getCurrentUser();
        Product currentProduct = findProductById(id);
        requireProductAccess(currentUser, currentProduct);
        ProductResponseDTO oldValues = this.productMapper.entityToDTO(currentProduct);
        warehouseAccessService.requireWarehouseAccess(currentUser, dto.warehouseId());
        this.productMapper.updateEntityFromDTO(currentProduct, dto);
        currentProduct.setWarehouse(resolveWarehouse(dto.warehouseId()));
        Product updatedProduct = this.productRepository.save(currentProduct);
        ProductResponseDTO newValues = this.productMapper.entityToDTO(updatedProduct);
        auditService.logUpdate("product", "Catalog for products", currentUser, oldValues, newValues);
        return newValues;
    }

    @Override
    public void deleteProduct(Long id) {
        AppUser currentUser = warehouseAccessService.getCurrentUser();
        Product currentProduct = findProductById(id);
        requireProductAccess(currentUser, currentProduct);
        ProductResponseDTO oldValues = this.productMapper.entityToDTO(currentProduct);
        this.productRepository.delete(currentProduct);
        auditService.logDelete("product", "Catalog for products", currentUser, oldValues);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponseDTO> findAll() {
        AppUser currentUser = warehouseAccessService.getCurrentUser();
        List<Product> products;
        if (warehouseAccessService.isAdmin(currentUser)) {
            products = this.productRepository.findAll();
        } else {
            Set<Long> managedWarehouseIds = warehouseAccessService.getManagedWarehouseIds(currentUser);
            products = managedWarehouseIds.isEmpty()
                ? List.of()
                : this.productRepository.findByWarehouseIdIn(managedWarehouseIds);
        }
        return products.stream().map(this.productMapper::entityToDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponseDTO findOne(Long id) {
        AppUser currentUser = warehouseAccessService.getCurrentUser();
        Product product = findProductById(id);
        requireProductAccess(currentUser, product);
        return this.productMapper.entityToDTO(product);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponseDTO> findLowStock() {
        AppUser currentUser = warehouseAccessService.getCurrentUser();
        Set<Long> managedWarehouseIds = warehouseAccessService.getManagedWarehouseIds(currentUser);
        if (!warehouseAccessService.isAdmin(currentUser) && managedWarehouseIds.isEmpty()) {
            return List.of();
        }

        return this.productRepository.findLowStockProducts(
                LOW_STOCK_THRESHOLD,
                managedWarehouseIds,
                warehouseAccessService.isAdmin(currentUser)
            )
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

    private void requireProductAccess(AppUser currentUser, Product product) {
        Long warehouseId = product.getWarehouse() != null ? product.getWarehouse().getId() : null;
        warehouseAccessService.requireWarehouseAccess(currentUser, warehouseId);
    }
    
}
