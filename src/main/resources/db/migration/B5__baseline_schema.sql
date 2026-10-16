create table EVENT_PUBLICATION
(
    ID                     varchar(36)   not null
        primary key,
    LISTENER_ID            varchar(512)  not null,
    EVENT_TYPE             varchar(512)  not null,
    SERIALIZED_EVENT       varchar(4000) not null,
    PUBLICATION_DATE       timestamp(6)  not null,
    COMPLETION_DATE        timestamp(6)  null,
    STATUS                 varchar(20)   null,
    COMPLETION_ATTEMPTS    int           null,
    LAST_RESUBMISSION_DATE timestamp(6)  null
);

create index EVENT_PUBLICATION_BY_COMPLETION_DATE_IDX
    on EVENT_PUBLICATION (COMPLETION_DATE);

create table SPRING_AI_CHAT_MEMORY
(
    conversation_id varchar(36)                                  not null,
    content         text                                         not null,
    type            enum ('USER', 'ASSISTANT', 'SYSTEM', 'TOOL') not null,
    timestamp       timestamp                                    not null,
    sequence_id     bigint                                       not null
);

create index SPRING_AI_CHAT_MEMORY_CONVERSATION_ID_SEQUENCE_ID_IDX
    on SPRING_AI_CHAT_MEMORY (conversation_id, sequence_id);

create index SPRING_AI_CHAT_MEMORY_CONVERSATION_ID_TIMESTAMP_IDX
    on SPRING_AI_CHAT_MEMORY (conversation_id, timestamp);

create table category
(
    category_id bigint auto_increment
        primary key,
    parent_id   bigint                             null,
    name        varchar(50)                        not null,
    created_at  datetime default CURRENT_TIMESTAMP not null,
    updated_at  datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP,
    constraint fk_category_parent
        foreign key (parent_id) references category (category_id)
);

create table recommendation_event
(
    event_id     varchar(50)                        not null
        primary key,
    event_type   varchar(50)                        not null,
    user_id      bigint                             not null,
    aggregate_id bigint                             null,
    payload      json                               not null,
    occurred_at  datetime                           not null,
    processed_at datetime default CURRENT_TIMESTAMP not null,
    constraint uq_recommendation_event_business
        unique (event_type, aggregate_id)
);

create index idx_recommendation_event_processed_at
    on recommendation_event (processed_at);

create table recommendation_interest
(
    user_id       bigint                                      not null,
    interest_type varchar(20)                                 not null,
    interest_key  varchar(100)                                not null,
    score         smallint unsigned default '0'               not null,
    last_event_at datetime                                    not null,
    created_at    datetime          default CURRENT_TIMESTAMP not null,
    updated_at    datetime          default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP,
    primary key (user_id, interest_type, interest_key)
);

create index idx_recommendation_interest_ranking
    on recommendation_interest (user_id asc, interest_type asc, score desc);

create table role
(
    role_id     bigint auto_increment
        primary key,
    code        varchar(20)                          not null,
    description varchar(50)                          not null,
    enabled     tinyint(1) default 1                 not null,
    created_at  datetime   default CURRENT_TIMESTAMP not null,
    updated_at  datetime   default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP,
    constraint uq_role_code
        unique (code)
);

create table user
(
    user_id           bigint auto_increment
        primary key,
    provider          varchar(50)                           not null,
    provider_id       varchar(255)                          not null,
    name              varchar(50)                           not null,
    zipcode           varchar(10)                           null,
    address           varchar(100)                          null,
    detail_address    varchar(100)                          null,
    phone_number      varchar(20)                           null,
    email             varchar(100)                          not null,
    profile_image_url varchar(512)                          not null,
    status            varchar(20) default 'ACTIVE'          not null,
    created_at        datetime    default CURRENT_TIMESTAMP not null,
    updated_at        datetime    default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP,
    status_changed_at datetime    default CURRENT_TIMESTAMP not null,
    constraint uq_user_email
        unique (email),
    constraint uq_user_provider_provider_id
        unique (provider, provider_id)
);

create table assistant_conversation
(
    conversation_id bigint auto_increment
        primary key,
    user_id         bigint                             not null,
    title           varchar(100)                       not null,
    status          varchar(20)                        not null,
    created_at      datetime default CURRENT_TIMESTAMP not null,
    updated_at      datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP,
    constraint fk_assistant_conversation_user
        foreign key (user_id) references user (user_id)
);

create table assistant_message
(
    message_id      bigint auto_increment
        primary key,
    conversation_id bigint                             not null,
    role            varchar(20)                        not null,
    content         text                               not null,
    input_tokens    bigint                             null,
    output_tokens   bigint                             null,
    created_at      datetime default CURRENT_TIMESTAMP not null,
    updated_at      datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP,
    status          varchar(20)                        not null,
    constraint fk_assistant_conversation_conversation
        foreign key (conversation_id) references assistant_conversation (conversation_id)
);

create table cart
(
    cart_id    bigint auto_increment
        primary key,
    user_id    bigint                             not null,
    created_at datetime default CURRENT_TIMESTAMP not null,
    updated_at datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP,
    constraint uq_cart_user_id
        unique (user_id),
    constraint fk_cart_user
        foreign key (user_id) references user (user_id)
);

create table orders
(
    order_id    bigint auto_increment
        primary key,
    user_id     bigint                                not null,
    status      varchar(20) default 'ORDERED'         not null,
    ordered_at  datetime                              not null,
    canceled_at datetime                              null,
    created_at  datetime    default CURRENT_TIMESTAMP not null,
    updated_at  datetime    default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP,
    constraint fk_orders_user
        foreign key (user_id) references user (user_id)
);

create table delivery
(
    delivery_id    bigint auto_increment
        primary key,
    order_id       bigint                                not null,
    status         varchar(20) default 'PREPARING'       not null,
    tracking_no    varchar(50)                           null,
    zipcode        varchar(10)                           not null,
    address        varchar(100)                          not null,
    detail_address varchar(100)                          not null,
    request_memo   varchar(100)                          null,
    created_at     datetime    default CURRENT_TIMESTAMP not null,
    updated_at     datetime    default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP,
    constraint uq_delivery_order_id
        unique (order_id),
    constraint fk_delivery_orders
        foreign key (order_id) references orders (order_id)
);

create index idx_orders_status_ordered_at
    on orders (status, ordered_at);

create table payment
(
    payment_id              bigint auto_increment
        primary key,
    order_id                bigint                                not null,
    payment_method          varchar(50)                           not null,
    amount                  int                                   not null,
    status                  varchar(20) default 'READY'           not null,
    paid_at                 datetime                              null,
    created_at              datetime    default CURRENT_TIMESTAMP not null,
    updated_at              datetime    default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP,
    canceled_at             datetime                              null,
    provider_transaction_id varchar(255)                          null,
    constraint uq_payment_order_id
        unique (order_id),
    constraint uq_payment_provider_transaction_id
        unique (provider_transaction_id),
    constraint fk_payment_orders
        foreign key (order_id) references orders (order_id)
);

create table refresh_token
(
    refresh_token_id bigint auto_increment
        primary key,
    user_id          bigint       not null,
    token            varchar(255) not null,
    constraint uq_refresh_token_token
        unique (token),
    constraint uq_refresh_token_user_id
        unique (user_id),
    constraint fk_refresh_token_user
        foreign key (user_id) references user (user_id)
);

create table search_history
(
    search_history_id bigint auto_increment
        primary key,
    user_id           bigint                             null,
    session_id        varchar(255)                       null,
    keyword           varchar(100)                       not null,
    last_searched_at  datetime default CURRENT_TIMESTAMP not null,
    constraint uq_search_history_session_id_keyword
        unique (session_id, keyword),
    constraint uq_search_history_user_id_keyword
        unique (user_id, keyword),
    constraint fk_search_history_user
        foreign key (user_id) references user (user_id)
);

create table seller
(
    seller_id           bigint auto_increment
        primary key,
    user_id             bigint                                not null,
    status              varchar(20) default 'PENDING'         not null,
    verification_status varchar(20) default 'UNVERIFIED'      not null,
    created_at          datetime    default CURRENT_TIMESTAMP not null,
    updated_at          datetime    default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP,
    status_changed_at   datetime    default CURRENT_TIMESTAMP not null,
    constraint uq_seller_user_id
        unique (user_id),
    constraint fk_seller_user
        foreign key (user_id) references user (user_id)
);

create table product
(
    product_id     bigint auto_increment
        primary key,
    category_id    bigint                                not null,
    user_id        bigint                                not null,
    name           varchar(100)                          not null,
    description    varchar(255)                          null,
    price          int                                   not null,
    stock_quantity int         default 0                 not null,
    status         varchar(20) default 'ACTIVE'          not null,
    created_at     datetime    default CURRENT_TIMESTAMP not null,
    updated_at     datetime    default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP,
    constraint fk_product_category
        foreign key (category_id) references category (category_id),
    constraint fk_product_seller
        foreign key (user_id) references seller (seller_id)
);

create table cart_item
(
    cart_item_id  bigint auto_increment
        primary key,
    cart_id       bigint                             not null,
    product_id    bigint                             not null,
    cart_quantity int      default 1                 not null,
    created_at    datetime default CURRENT_TIMESTAMP not null,
    updated_at    datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP,
    constraint uq_cart_item_cart_id_product_id
        unique (cart_id, product_id),
    constraint fk_cart_item_cart
        foreign key (cart_id) references cart (cart_id),
    constraint fk_cart_item_product
        foreign key (product_id) references product (product_id)
);

create table order_item
(
    order_item_id  bigint auto_increment
        primary key,
    order_id       bigint                             not null,
    product_id     bigint                             not null,
    product_name   varchar(100)                       not null,
    order_price    int                                not null,
    order_quantity int                                not null,
    created_at     datetime default CURRENT_TIMESTAMP not null,
    updated_at     datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP,
    constraint uq_order_item_order_id_product_id
        unique (order_id, product_id),
    constraint fk_order_item_orders
        foreign key (order_id) references orders (order_id),
    constraint fk_order_item_product
        foreign key (product_id) references product (product_id)
);

create table product_image
(
    product_image_id bigint auto_increment
        primary key,
    product_id       bigint       not null,
    image_url        varchar(512) not null,
    display_order    int          not null,
    is_thumbnail     tinyint(1)   not null,
    constraint uq_product_image_product_id_display_order
        unique (product_id, display_order),
    constraint fk_product_image_product
        foreign key (product_id) references product (product_id)
);

create table review
(
    review_id     bigint auto_increment
        primary key,
    order_item_id bigint                             not null,
    content       varchar(100)                       not null,
    rating        tinyint                            not null,
    created_at    datetime default CURRENT_TIMESTAMP not null,
    updated_at    datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP,
    user_id       bigint                             not null,
    product_id    bigint                             not null,
    constraint uq_review_order_item_id
        unique (order_item_id),
    constraint fk_review_order_item
        foreign key (order_item_id) references order_item (order_item_id),
    constraint fk_review_product
        foreign key (product_id) references product (product_id),
    constraint fk_review_user
        foreign key (user_id) references user (user_id),
    constraint chk_review_rating
        check (`rating` between 1 and 5)
);

create table review_image
(
    review_image_id bigint auto_increment
        primary key,
    review_id       bigint       not null,
    image_url       varchar(512) not null,
    display_order   int          not null,
    constraint uq_review_image_review_id_display_order
        unique (review_id, display_order),
    constraint fk_review_image_review
        foreign key (review_id) references review (review_id)
);

create table user_role
(
    user_role_id bigint auto_increment
        primary key,
    user_id      bigint                             not null,
    role_id      bigint                             not null,
    created_at   datetime default CURRENT_TIMESTAMP not null,
    updated_at   datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP,
    constraint uq_user_role_user_id_role_id
        unique (user_id, role_id),
    constraint fk_user_role_role
        foreign key (role_id) references role (role_id),
    constraint fk_user_role_user
        foreign key (user_id) references user (user_id)
);

