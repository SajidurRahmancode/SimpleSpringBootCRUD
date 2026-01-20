package com.simple.crud.demo.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.simple.crud.demo.model.dto.ProductCreateDto;
import com.simple.crud.demo.model.dto.ProductResponseDto;
import com.simple.crud.demo.model.entity.Product;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    @Mapping(target = "ownerId", source = "owner.id")
    @Mapping(target = "ownerUsername", source = "owner.username")
    @Mapping(target = "supplierId", source = "supplier.id")
    @Mapping(target = "supplierUsername", source = "supplier.username")
    ProductResponseDto toDto(Product entity);
    Product toEntity(ProductCreateDto dto);
    void updateEntityFromDto(ProductCreateDto dto, @MappingTarget Product entity);
}



