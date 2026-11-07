package com.sathya.restaurant_management.util;

import java.util.UUID;

/** Generates ids in the same "prefix_xxxx" style the React app used (p_..., o_..., c_...). */
public final class Ids {

    private Ids() {}

    public static String next(String prefix) {
        return prefix + "_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }

    /** 32 hex characters, same shape as the invoice code the frontend used to fake. */
    public static String invoiceCode() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
