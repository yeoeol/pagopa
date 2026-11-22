import exec from 'k6/execution';
import { createAccessToken } from './jwt.js';

export const TEST_USER_COUNT = Number(__ENV.TEST_USER_COUNT);

const JWT_SECRET = __ENV.JWT_SECRET;
const JWT_ALGORITHM = __ENV.JWT_ALGORITHM || 'HS256';
const TEST_USER_ID_START = Number(__ENV.TEST_USER_ID_START);
const TEST_USER_ROLE = __ENV.TEST_USER_ROLE;
const ACCESS_TOKEN_EXPIRY_SECONDS = 3600;

if (!JWT_SECRET || !TEST_USER_ROLE) {
    throw new Error('JWT_SECRET, TEST_USER_ROLE 환경변수가 필요합니다.');
}

if (
    !Number.isSafeInteger(TEST_USER_ID_START) ||
    TEST_USER_ID_START < 1 ||
    !Number.isSafeInteger(TEST_USER_COUNT) ||
    TEST_USER_COUNT < 1 ||
    !Number.isSafeInteger(TEST_USER_ID_START + TEST_USER_COUNT - 1)
) {
    throw new Error('TEST_USER_ID_START와 TEST_USER_COUNT는 유효한 양의 정수여야 합니다.');
}

export function createTestUserToken() {
    const vuId = exec.vu.idInTest;

    if (!Number.isSafeInteger(vuId) || vuId < 1 || vuId > TEST_USER_COUNT) {
        const message = `VU ID ${vuId}에 할당할 테스트 사용자가 없습니다. TEST_USER_COUNT=${TEST_USER_COUNT}`;
        exec.test.abort(message);
        throw new Error(message);
    }

    const testUserIndex = vuId - 1;
    const userId = TEST_USER_ID_START + testUserIndex;
    const email = `loadtest-${testUserIndex + 1}@pagopa.test`;

    return createAccessToken(
        JWT_SECRET,
        JWT_ALGORITHM,
        userId,
        email,
        [TEST_USER_ROLE],
        ACCESS_TOKEN_EXPIRY_SECONDS
    );
}
