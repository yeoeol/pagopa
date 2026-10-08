package com.commerce.pagopa.catalog.api;

import java.util.List;

public interface ProductStockApi {
    List<ProductStockResult> decreaseStocks(List<ProductStockRequest> requests);

    List<ProductStockResult> restoreStocks(List<ProductStockRequest> requests);
}
