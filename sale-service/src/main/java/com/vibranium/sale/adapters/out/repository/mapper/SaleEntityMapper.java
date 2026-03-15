package com.vibranium.sale.adapters.out.repository.mapper;

import com.vibranium.sale.adapters.out.repository.entity.SaleEntity;
import com.vibranium.sale.application.core.domain.Sale;
import com.vibranium.sale.application.core.domain.enums.SaleStatus;
import com.vibranium.sale.application.core.domain.enums.TypeSale;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface SaleEntityMapper {

    // Domain -> Entity
    @Mapping(source = "status", target = "statusId", qualifiedByName = "setStatusId")
    @Mapping(source = "type",   target = "typeSaleId", qualifiedByName = "setTypeSaleId")
    SaleEntity toSaleEntity(Sale sale);

    @Named("setStatusId")
    default Integer setStatusId(SaleStatus saleStatus) {
        return saleStatus != null ? saleStatus.getStatusId() : null;
    }

    @Named("setTypeSaleId")
    default Integer setTypeSaleId(TypeSale typeSale) {
        return typeSale != null ? typeSale.getTypeId() : null;
    }

    // Entity -> Domain
    @Mapping(source = "statusId",   target = "status", qualifiedByName = "setStatus")
    @Mapping(source = "typeSaleId", target = "type",   qualifiedByName = "setTypeSale")
    Sale toSale(SaleEntity saleEntity);

    @Named("setStatus")
    default SaleStatus setStatus(Integer saleStatusId) {
        return saleStatusId != null ? SaleStatus.toEnum(saleStatusId) : null;
    }

    @Named("setTypeSale")
    default TypeSale setTypeSale(Integer typeSaleId) {
        return typeSaleId != null ? TypeSale.toEnum(typeSaleId) : null;
    }
}
