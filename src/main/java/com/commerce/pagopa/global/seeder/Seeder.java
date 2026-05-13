package com.commerce.pagopa.global.seeder;

public interface Seeder {

    String name();

    /** 이미 시드되어 있으면 false. 멱등성 보장용 */
    boolean shouldRun();

    void seed();
}
