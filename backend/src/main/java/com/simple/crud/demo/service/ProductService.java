package com.simple.crud.demo.service;

import com.simple.crud.demo.model.dto.ProductCreateDto;
import com.simple.crud.demo.model.dto.ProductResponseDto;
import com.simple.crud.demo.model.entity.Product;
import com.simple.crud.demo.model.entity.User;
import com.simple.crud.demo.repository.ProductRepository;
import com.simple.crud.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
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
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final FileStorageService fileStorageService;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<ProductResponseDto> getAllProducts(org.springframework.data.domain.Pageable pageable) {
        log.debug("Fetching all products - page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());
        org.springframework.data.domain.Page<ProductResponseDto> result = productRepository.findAll(pageable)
            .map(productMapper::toDto);
        log.info("Retrieved {} products out of {} total", result.getNumberOfElements(), result.getTotalElements());
        return result;
    }

    @Transactional(readOnly = true)
    public Optional<ProductResponseDto> getProductById(Long id) {
        log.debug("Fetching product by id: {}", id);
        Optional<ProductResponseDto> result = productRepository.findById(id)
            .map(productMapper::toDto);
        if (result.isEmpty()) {
            log.warn("Product not found with id: {}", id);
        }
        return result;
    }

    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<ProductResponseDto> searchProductsByName(String name, org.springframework.data.domain.Pageable pageable) {
        log.info("Searching products by name: '{}' - page: {}, size: {}", name, pageable.getPageNumber(), pageable.getPageSize());
        org.springframework.data.domain.Page<ProductResponseDto> result = productRepository.findByNameContainingIgnoreCase(name, pageable)
            .map(productMapper::toDto);
        log.debug("Search returned {} products", result.getTotalElements());
        return result;
    }

    @Transactional(readOnly = true)
    public List<ProductResponseDto> getProductsInStock() {
        log.debug("Fetching products in stock");
        List<ProductResponseDto> result = productRepository.findProductsInStock()
            .stream()
            .map(productMapper::toDto)
            .collect(Collectors.toList());
        log.info("Found {} products in stock", result.size());
        return result;
    }

    @Transactional(readOnly = true)
    public List<ProductResponseDto> getProductsWithLowStock(Integer threshold) {
        log.info("Fetching products with low stock (threshold: {})", threshold);
        List<ProductResponseDto> result = productRepository.findProductsWithLowStock(threshold)
            .stream()
            .map(productMapper::toDto)
            .collect(Collectors.toList());
        log.warn("Found {} products with low stock", result.size());
        return result;
    }

    @PreAuthorize("isAuthenticated()")
    public ProductResponseDto createProduct(ProductCreateDto productCreateDto) {
        return createProduct(productCreateDto, (String) null);
    }

    @PreAuthorize("isAuthenticated()")
    public ProductResponseDto createProduct(ProductCreateDto productCreateDto, String sellerIdentifier) {
        log.info("Creating product: '{}', sellerIdentifier: {}", productCreateDto.getName(), sellerIdentifier);
        Product product = prepareProduct(productCreateDto, sellerIdentifier);
        Product savedProduct = productRepository.save(product);
        log.info("AUDIT: Product created successfully - productId: {}, name: '{}', ownerId: {}, supplierId: {}", 
                savedProduct.getId(), savedProduct.getName(), 
                savedProduct.getOwner() != null ? savedProduct.getOwner().getId() : null,
                savedProduct.getSupplier() != null ? savedProduct.getSupplier().getId() : null);
        return productMapper.toDto(savedProduct);
    }

    @PreAuthorize("isAuthenticated()")
    public ProductResponseDto createProduct(ProductCreateDto productCreateDto, MultipartFile imageFile) {
        return createProduct(productCreateDto, imageFile, null);
    }

    @PreAuthorize("isAuthenticated()")
    public ProductResponseDto createProduct(ProductCreateDto productCreateDto, MultipartFile imageFile, String sellerIdentifier) {
        log.info("Creating product with image: '{}', hasImage: {}, sellerIdentifier: {}", 
                productCreateDto.getName(), imageFile != null && !imageFile.isEmpty(), sellerIdentifier);
        Product product = prepareProduct(productCreateDto, sellerIdentifier);
        try {
            String imagePath = imageFile != null ? fileStorageService.storeImage(imageFile) : null;
            product.setImagePath(imagePath);
            log.debug("Product image stored successfully at: {}", imagePath);
        } catch (Exception ex) {
            log.error("Image upload failed for product: '{}' - error: {}", productCreateDto.getName(), ex.getMessage(), ex);
            throw new RuntimeException("Image upload failed: " + ex.getMessage());
        }
        Product saved = productRepository.save(product);
        log.info("AUDIT: Product created with image - productId: {}, name: '{}', imagePath: {}", 
                saved.getId(), saved.getName(), saved.getImagePath());
        return productMapper.toDto(saved);
    }

    @PreAuthorize("isAuthenticated()")
    public Optional<ProductResponseDto> updateProduct(Long id, ProductCreateDto productCreateDto) {
        log.info("Updating product - productId: {}, newName: '{}'", id, productCreateDto.getName());
        return productRepository.findById(id)
                .map(existingProduct -> {
                    ensureOwnerOrAdmin(existingProduct);
                    log.debug("Authorization passed for productId: {}", id);
                    // MapStruct will update fields; handle stock default if null
                    Integer qty = productCreateDto.getStockQuantity();
                    if (qty == null && existingProduct.getStockQuantity() == null) {
                        existingProduct.setStockQuantity(0);
                    }
                    productMapper.updateEntityFromDto(productCreateDto, existingProduct);
                    Product updatedProduct = productRepository.save(existingProduct);
                    log.info("AUDIT: Product updated - productId: {}, name: '{}', stock: {}", 
                            updatedProduct.getId(), updatedProduct.getName(), updatedProduct.getStockQuantity());
                    return productMapper.toDto(updatedProduct);
                });
    }

    @PreAuthorize("isAuthenticated()")
    public Optional<ProductResponseDto> updateProduct(Long id, ProductCreateDto productCreateDto, MultipartFile imageFile) {
        log.info("Updating product with image - productId: {}, hasNewImage: {}", id, imageFile != null && !imageFile.isEmpty());
        return productRepository.findById(id)
                .map(existingProduct -> {
                    ensureOwnerOrAdmin(existingProduct);
                    Integer qty = productCreateDto.getStockQuantity();
                    existingProduct.setStockQuantity(qty != null ? qty : (existingProduct.getStockQuantity() == null ? 0 : existingProduct.getStockQuantity()));
                    if (imageFile != null && !imageFile.isEmpty()) {
                        try {
                            String imagePath = fileStorageService.storeImage(imageFile);
                            existingProduct.setImagePath(imagePath);
                            log.debug("Product image updated successfully - productId: {}, newImagePath: {}", id, imagePath);
                        } catch (Exception ex) {
                            log.error("Image upload failed during product update - productId: {}, error: {}", id, ex.getMessage(), ex);
                            throw new RuntimeException("Image upload failed: " + ex.getMessage());
                        }
                    }
                    productMapper.updateEntityFromDto(productCreateDto, existingProduct);
                    Product updatedProduct = productRepository.save(existingProduct);
                    log.info("AUDIT: Product updated with image - productId: {}, name: '{}'", 
                            updatedProduct.getId(), updatedProduct.getName());
                    return productMapper.toDto(updatedProduct);
                });
    }

    @PreAuthorize("isAuthenticated()")
    public boolean deleteProduct(Long id) {
        log.info("Attempting to delete product - productId: {}", id);
        return productRepository.findById(id).map(p -> {
            ensureOwnerOrAdmin(p);
            productRepository.delete(p);
            log.info("AUDIT: Product deleted - productId: {}, name: '{}'", id, p.getName());
            return true;
        }).orElseGet(() -> {
            log.warn("Delete failed - product not found: {}", id);
            return false;
        });
    }

    @PreAuthorize("isAuthenticated()")
    public Optional<ProductResponseDto> updateStock(Long id, Integer newQuantity) {
        log.info("Updating stock - productId: {}, newQuantity: {}", id, newQuantity);
        return productRepository.findById(id)
                .map(product -> {
                    ensureOwnerOrAdmin(product);
                    Integer oldQuantity = product.getStockQuantity();
                    product.setStockQuantity(newQuantity);
                    Product updatedProduct = productRepository.save(product);
                    log.info("AUDIT: Stock updated - productId: {}, oldStock: {}, newStock: {}", 
                            id, oldQuantity, newQuantity);
                    return productMapper.toDto(updatedProduct);
                });
    }

    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<ProductResponseDto> getProductsByOwner(Long ownerId, org.springframework.data.domain.Pageable pageable) {
        log.debug("Fetching products by ownerId: {}", ownerId);
        return productRepository.findByOwner_Id(ownerId, pageable).map(productMapper::toDto);
    }

    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<ProductResponseDto> getMyProducts(org.springframework.data.domain.Pageable pageable) {
        var me = getCurrentUser();
        log.debug("Fetching my products for userId: {}", me.getId());
        return productRepository.findByOwner_Id(me.getId(), pageable).map(productMapper::toDto);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('SUPPLIER')")
    public org.springframework.data.domain.Page<ProductResponseDto> getSuppliedProducts(org.springframework.data.domain.Pageable pageable) {
        var me = getCurrentUser();
        log.debug("Fetching supplied products for supplierId: {}", me.getId());
        return productRepository.findBySupplier_Id(me.getId(), pageable).map(productMapper::toDto);
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
            log.error("SECURITY: Unauthorized access attempt to product - productId: {}", 
                    product.getId());
            throw new org.springframework.security.core.AuthenticationException("Not authenticated") {};
        }
        boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (isAdmin) {
            log.debug("Admin access granted for productId: {}", product.getId());
            return;
        }
        boolean isSupplier = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_SUPPLIER"));
        var current = getCurrentUser();
        Long ownerId = product.getOwner() != null ? product.getOwner().getId() : null;
        if (ownerId == null || !ownerId.equals(current.getId())) {
            if (isSupplier && product.getSupplier() != null && product.getSupplier().getId() != null
                    && product.getSupplier().getId().equals(current.getId())) {
                log.debug("Supplier access granted for productId: {}, supplierId: {}", 
                        product.getId(), current.getId());
                return;
            }
            log.warn("SECURITY: Access denied to product - productId: {}, userId: {}, username: {}", 
                    product.getId(), current.getId(), current.getUsername());
            throw new org.springframework.security.access.AccessDeniedException("Only owner, supplier, or admin can modify this product");
        }
        log.debug("Owner access granted for productId: {}, userId: {}", product.getId(), current.getId());
    }

    private Product prepareProduct(ProductCreateDto productCreateDto, String sellerIdentifier) {
        Product product = productMapper.toEntity(productCreateDto);
        if (product.getStockQuantity() == null) {
            product.setStockQuantity(0);
        }
        var currentUser = getCurrentUser();
        String normalizedIdentifier = sellerIdentifier != null ? sellerIdentifier.trim() : null;
        User owner = resolveOwner(currentUser, normalizedIdentifier);
        product.setOwner(owner);
        boolean supplyingForAnotherUser = normalizedIdentifier != null && !normalizedIdentifier.isBlank();
        if (supplyingForAnotherUser && currentUser.getRole() == User.Role.SUPPLIER) {
            product.setSupplier(currentUser);
        } else {
            product.setSupplier(null);
        }
        return product;
    }

    private User resolveOwner(User currentUser, String sellerIdentifier) {
        if (sellerIdentifier == null || sellerIdentifier.isBlank()) {
            log.debug("Product owner set to current user - userId: {}", currentUser.getId());
            return currentUser;
        }
        if (currentUser.getRole() != User.Role.SUPPLIER && currentUser.getRole() != User.Role.ADMIN) {
            log.warn("SECURITY: Unauthorized seller assignment attempt - userId: {}, role: {}", 
                    currentUser.getId(), currentUser.getRole());
            throw new AccessDeniedException("Only suppliers or admins can assign products to other sellers");
        }
        User seller = findUserByIdentifier(sellerIdentifier)
                .orElseThrow(() -> {
                    log.error("Seller not found for identifier: {}", sellerIdentifier);
                    return new IllegalArgumentException("Seller not found");
                });
        if (seller.getRole() == User.Role.ADMIN) {
            log.warn("Invalid seller assignment - cannot assign to admin account: {}", sellerIdentifier);
            throw new IllegalArgumentException("Cannot assign products to admin accounts");
        }
        log.info("Product owner assigned to sellerId: {} by userId: {} (role: {})", 
                seller.getId(), currentUser.getId(), currentUser.getRole());
        return seller;
    }

    private Optional<User> findUserByIdentifier(String identifier) {
        if (identifier == null || identifier.isBlank()) {
            return Optional.empty();
        }
        try {
            Long id = Long.parseLong(identifier);
            return userRepository.findById(id);
        } catch (NumberFormatException ex) {
            // ignore
        }
        return userRepository.findByUsername(identifier)
                .or(() -> userRepository.findByEmail(identifier));
    }
}
