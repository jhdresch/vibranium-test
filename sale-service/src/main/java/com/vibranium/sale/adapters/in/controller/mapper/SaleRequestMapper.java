package com.vibranium.sale.adapters.in.controller.mapper;

import com.vibranium.sale.adapters.in.controller.request.SaleRequest;
import com.vibranium.sale.application.core.domain.Sale;
import com.vibranium.sale.application.core.domain.enums.TypeSale;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface SaleRequestMapper {

    @Mapping(source = "type", target = "type", qualifiedByName = "mapTypeSale")
    Sale toSale(SaleRequest saleRequest);

    @Named("mapTypeSale")
    default TypeSale mapTypeSale(Integer typeId) {
        return TypeSale.toEnum(typeId);
    }

}
