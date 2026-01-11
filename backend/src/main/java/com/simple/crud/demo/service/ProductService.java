package com.simple.crud.demo.service;

import com.simple.crud.demo.model.dto.ProductCreateDto;
import com.simple.crud.demo.model.dto.ProductResponseDto;
import com.simple.crud.demo.model.entity.Product;
import com.simple.crud.demo.repository.ProductRepository;
import com.simple.crud.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import com.simple.crud.demo.mapper.ProductMapper;

@Service
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final FileStorageService fileStorageService;
    private final UserRepository userRepository;

    @Autowired
    public ProductService(ProductRepository productRepository, ProductMapper productMapper, FileStorageService fileStorageService, UserRepository userRepository) {
        this.productRepository = productRepository;
        this.productMapper = productMapper;
        this.fileStorageService = fileStorageService;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<ProductResponseDto> getAllProducts(org.springframework.data.domain.Pageable pageable) {
        return productRepository.findAll(pageable)
            .map(productMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Optional<ProductResponseDto> getProductById(Long id) {
        return productRepository.findById(id)
            .map(productMapper::toDto);
    }

    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<ProductResponseDto> searchProductsByName(String name, org.springframework.data.domain.Pageable pageable) {
        return productRepository.findByNameContainingIgnoreCase(name, pageable)
            .map(productMapper::toDto);
    }

    @Transactional(readOnly = true)
    public List<ProductResponseDto> getProductsInStock() {
        return productRepository.findProductsInStock()
            .stream()
            .map(productMapper::toDto)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProductResponseDto> getProductsWithLowStock(Integer threshold) {
        return productRepository.findProductsWithLowStock(threshold)
            .stream()
            .map(productMapper::toDto)
            .collect(Collectors.toList());
    }

    @PreAuthorize("isAuthenticated()")
    public ProductResponseDto createProduct(ProductCreateDto productCreateDto) {
        Product product = productMapper.toEntity(productCreateDto);
        if (product.getStockQuantity() == null) product.setStockQuantity(0);
        var owner = getCurrentUser();
        product.setOwner(owner);

        Product savedProduct = productRepository.save(product);
        return productMapper.toDto(savedProduct);
    }

    @PreAuthorize("isAuthenticated()")
    public ProductResponseDto createProduct(ProductCreateDto productCreateDto, MultipartFile imageFile) {
        Product product = productMapper.toEntity(productCreateDto);
        if (product.getStockQuantity() == null) product.setStockQuantity(0);
        try {
            String imagePath = imageFile != null ? fileStorageService.storeImage(imageFile) : null;
            product.setImagePath(imagePath);
        } catch (Exception ex) {
            throw new RuntimeException("Image upload failed: " + ex.getMessage());
        }
        var owner = getCurrentUser();
        product.setOwner(owner);
        Product saved = productRepository.save(product);
        return productMapper.toDto(saved);
    }

    @PreAuthorize("isAuthenticated()")
    public Optional<ProductResponseDto> updateProduct(Long id, ProductCreateDto productCreateDto) {
        return productRepository.findById(id)
                .map(existingProduct -> {
                    ensureOwnerOrAdmin(existingProduct);
                    // MapStruct will update fields; handle stock default if null
                    Integer qty = productCreateDto.getStockQuantity();
                    if (qty == null && existingProduct.getStockQuantity() == null) {
                        existingProduct.setStockQuantity(0);
                    }
                    productMapper.updateEntityFromDto(productCreateDto, existingProduct);
                    Product updatedProduct = productRepository.save(existingProduct);
                    return productMapper.toDto(updatedProduct);
                });
    }

    @PreAuthorize("isAuthenticated()")
    public Optional<ProductResponseDto> updateProduct(Long id, ProductCreateDto productCreateDto, MultipartFile imageFile) {
        return productRepository.findById(id)
                .map(existingProduct -> {
                    ensureOwnerOrAdmin(existingProduct);
                    Integer qty = productCreateDto.getStockQuantity();
                    existingProduct.setStockQuantity(qty != null ? qty : (existingProduct.getStockQuantity() == null ? 0 : existingProduct.getStockQuantity()));
                    if (imageFile != null && !imageFile.isEmpty()) {
                        try {
                            String imagePath = fileStorageService.storeImage(imageFile);
                            existingProduct.setImagePath(imagePath);
                        } catch (Exception ex) {
                            throw new RuntimeException("Image upload failed: " + ex.getMessage());
                        }
                    }
                    productMapper.updateEntityFromDto(productCreateDto, existingProduct);
                    Product updatedProduct = productRepository.save(existingProduct);
                    return productMapper.toDto(updatedProduct);
                });
    }

    @PreAuthorize("isAuthenticated()")
    public boolean deleteProduct(Long id) {
        return productRepository.findById(id).map(p -> {
            ensureOwnerOrAdmin(p);
            productRepository.delete(p);
            return true;
        }).orElse(false);
    }

    @PreAuthorize("isAuthenticated()")
    public Optional<ProductResponseDto> updateStock(Long id, Integer newQuantity) {
        return productRepository.findById(id)
                .map(product -> {
                    ensureOwnerOrAdmin(product);
                    product.setStockQuantity(newQuantity);
                    Product updatedProduct = productRepository.save(product);
                    return productMapper.toDto(updatedProduct);
                });
    }

    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<ProductResponseDto> getProductsByOwner(Long ownerId, org.springframework.data.domain.Pageable pageable) {
        return productRepository.findByOwner_Id(ownerId, pageable).map(productMapper::toDto);
    }

    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<ProductResponseDto> getMyProducts(org.springframework.data.domain.Pageable pageable) {
        var me = getCurrentUser();
        return productRepository.findByOwner_Id(me.getId(), pageable).map(productMapper::toDto);
    }

    private com.simple.crud.demo.model.entity.User getCurrentUser() {
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new org.springframework.security.core.AuthenticationException("Not authenticated") {};
        }
        String username = auth.getName();
        return userRepository.findByUsername(username)
                .or(() -> userRepository.findByEmail(username))
                .orElseThrow(() -> new org.springframework.security.core.userdetails.UsernameNotFoundException("User not found"));
    }

    private void ensureOwnerOrAdmin(Product product) {
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new org.springframework.security.core.AuthenticationException("Not authenticated") {};
        }
        boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (isAdmin) return;
        var current = getCurrentUser();
        Long ownerId = product.getOwner() != null ? product.getOwner().getId() : null;
        if (ownerId == null || !ownerId.equals(current.getId())) {
            throw new org.springframework.security.access.AccessDeniedException("Only owner or admin can modify this product");
        }
    }
}
