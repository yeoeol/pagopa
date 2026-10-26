-- 기존 단일 컬럼 유니크 인덱스 제거
ALTER TABLE payment
DROP INDEX uq_payment_provider_transaction_id;

-- provider_transaction_id -> provider_payment_id 변경
-- provider는 기존 데이터 보정을 위해 우선 NULL 허용
ALTER TABLE payment
    CHANGE COLUMN provider_transaction_id
        provider_payment_id VARCHAR(255) NULL,
    ADD COLUMN provider VARCHAR(20) NULL;

-- 현재 기존 결제 구현이 InternalPaymentGateway이므로 기존 데이터 보정
UPDATE payment
SET provider = 'INTERNAL'
WHERE provider IS NULL;

-- 최종 스키마 반영
ALTER TABLE payment
    MODIFY COLUMN provider VARCHAR(20) NOT NULL,
    ADD CONSTRAINT uq_payment_provider_payment_id
    UNIQUE (provider, provider_payment_id);
