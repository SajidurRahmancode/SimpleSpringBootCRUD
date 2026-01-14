package com.simple.crud.demo.repository;

import com.simple.crud.demo.model.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByNameContainingIgnoreCase(String name);
    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.stockQuantity > 0")
    List<Product> findProductsInStock();

    @Query("SELECT p FROM Product p WHERE p.stockQuantity <= :threshold")
    List<Product> findProductsWithLowStock(@Param("threshold") Integer threshold);

    Page<Product> findByOwner_Id(Long ownerId, Pageable pageable);

    Page<Product> findBySupplier_Id(Long supplierId, Pageable pageable);
}
