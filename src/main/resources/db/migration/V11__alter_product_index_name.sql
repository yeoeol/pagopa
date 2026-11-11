alter table product
    rename index idx_product_name to idx_product_name_created_at_product_id,
    rename index idx_product_cursor to idx_product_created_at_product_id;
