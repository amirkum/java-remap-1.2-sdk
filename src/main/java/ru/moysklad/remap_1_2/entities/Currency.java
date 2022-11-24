package ru.moysklad.remap_1_2.entities;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Валюта
 */

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Currency extends AbstractCurrency {

    /**
     * Основана ли эта Валюта на Валюте из системного справочника
     */
    final private Boolean system = false;
}

