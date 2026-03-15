package com.vibranium.sale.application.core.domain.enums;

public enum TypeSale {

    BUY(1),
    SELL(2);

    private final int typeId;

    TypeSale(int typeId) {
        this.typeId = typeId;
    }

    public int getTypeId() {
        return typeId;
    }

    public static TypeSale toEnum(Integer id) {
        if (id == null) {
            return null;
        }
        for (TypeSale ts : TypeSale.values()) {
            if (ts.getTypeId() == id) {
                return ts;
            }
        }
        throw new IllegalArgumentException("Invalid TypeSale id: " + id);
    }
}
