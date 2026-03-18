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
import com.proyectS1.warehouse_management.services.support.WarehouseAccessService;

import lombok.RequiredArgsConstructor;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductMapper productMapper;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    private final WarehouseAccessService warehouseAccessService;

    @Override
    public ProductResponseDTO saveProduct(ProductRequestDTO dto) {
        AppUser currentUser = warehouseAccessService.getCurrentUser();
        warehouseAccessService.requireWarehouseAccess(currentUser, dto.warehouseId());
        Product newProduct = this.productMapper.DTOToEntity(dto);
        newProduct.setWarehouse(resolveWarehouse(dto.warehouseId()));
        Product saveProduct = this.productRepository.save(newProduct);
        return this.productMapper.entityToDTO(saveProduct);
    }

    @Override
    public ProductResponseDTO updateProduct(ProductRequestDTO dto, Long id) {
        AppUser currentUser = warehouseAccessService.getCurrentUser();
        Product currentProduct = findProductById(id);
        requireProductAccess(currentUser, currentProduct);
        if (!java.util.Objects.equals(dto.warehouseId(), currentProduct.getWarehouse() != null ? currentProduct.getWarehouse().getId() : null)) {
            throw new ResponseStatusException(
                org.springframework.http.HttpStatus.BAD_REQUEST,
                "The current warehouse of an asset must be changed from the movements module"
            );
        }
        this.productMapper.updateEntityFromDTO(currentProduct, dto);
        Product updatedProduct = this.productRepository.save(currentProduct);
        return this.productMapper.entityToDTO(updatedProduct);
    }

    @Override
    public void deleteProduct(Long id) {
        AppUser currentUser = warehouseAccessService.getCurrentUser();
        Product currentProduct = findProductById(id);
        requireProductAccess(currentUser, currentProduct);
        this.productRepository.delete(currentProduct);
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
