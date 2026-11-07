package com.sathya.restaurant_management.dto;

import java.util.List;

/** Everything the /shop page needs to render in one request. categories are plain names, like the React app uses. */
public record ShopMenuDto(List<ShopProductDto> products, List<String> categories) {
}
