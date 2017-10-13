package com.lognex.api.util;

import com.lognex.api.model.base.AbstractEntity;
import com.lognex.api.model.document.Demand;
import com.lognex.api.model.document.FactureOut;
import com.lognex.api.model.document.PaymentIn;
import com.lognex.api.model.entity.*;
import lombok.Getter;

import java.util.Arrays;

public enum Type {

    COUNTERPARTY("counterparty", Counterparty.class),
    PAYMENT_IN("paymentin", PaymentIn.class),
    EMPLOYEE("employee", Employee.class),
    CUSTOM_ENTITY("customentity", CustomEntity.class),
    PRODUCT("product", Product.class),
    SERVICE("service", Service.class),
    CONSIGNMENT("consignment", Consignment.class),
    BUNDLE("bundle", Bundle.class),
    VARIANT("variant", Variant.class),
    STORE("store", Store.class),
    CURRENCY("currency", Currency.class),
    DEMAND("demand", Demand.class),
    ORGANIZATION("organization", Organization.class),
    FACTURE_OUT("factureout", FactureOut.class),
    GROUP("group", Group.class)
    ;


    @Getter
    private final Class<? extends AbstractEntity> modelClass;

    private @Getter final String apiName;

    Type(String apiName, Class<? extends AbstractEntity> clazz){
        this.modelClass = clazz;
        this.apiName = apiName;
    }

    public static Type find(Class<? extends AbstractEntity> clazz){
        return Arrays.stream(values())
                        .filter(t -> t.modelClass.getSimpleName().equals(clazz.getSimpleName()))
                        .findFirst().orElseThrow(() -> new IllegalStateException("No type found for class: " + clazz.getSimpleName()));
    }

    public static Type find(String name){
        return Arrays.stream(values()).filter(t -> t.apiName.equals(name)).findFirst().orElseThrow(() -> new IllegalStateException("No type found for string: " + name));
    }
}
