create index idx_product_cursor
    on product (created_at desc, product_id desc);

create index idx_product_name
    on product (name, created_at desc, product_id desc);
