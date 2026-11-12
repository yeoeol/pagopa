create index idx_orders_user_id_ordered_at
    on orders (user_id, ordered_at);
