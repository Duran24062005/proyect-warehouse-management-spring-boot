package com.proyectS1.warehouse_management.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.proyectS1.warehouse_management.model.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByCategory(String category);

    @Query(value = """
        SELECT p.*
        FROM product p
        LEFT JOIN movement m ON m.product_id = p.id
        GROUP BY p.id
        HAVING COALESCE(SUM(CASE WHEN m.destination_warehouse_id IS NOT NULL THEN m.quantity ELSE 0 END), 0)
             - COALESCE(SUM(CASE WHEN m.origin_warehouse_id IS NOT NULL THEN m.quantity ELSE 0 END), 0) <= :threshold
        """, nativeQuery = true)
    List<Product> findLowStockProducts(int threshold);
}
