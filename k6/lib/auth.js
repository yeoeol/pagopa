import { createAccessToken } from './jwt.js';

const JWT_SECRET = __ENV.JWT_SECRET;
const JWT_ALGORITHM = __ENV.JWT_ALGORITHM || 'HS256';
const TEST_USER_ID_START = Number(__ENV.TEST_USER_ID_START);
const TEST_USER_COUNT = Number(__ENV.TEST_USER_COUNT);
const TEST_USER_ROLE = __ENV.TEST_USER_ROLE;
const ACCESS_TOKEN_EXPIRY_SECONDS = 3600;

if (!JWT_SECRET || !TEST_USER_ROLE || !TEST_USER_ID_START || !TEST_USER_COUNT) {
    throw new Error('JWT_SECRET, TEST_USER_ROLE, TEST_USER_ID_START, TEST_USER_COUNT 환경변수가 필요합니다.');
}

export function createTestUserToken() {
    const testUserIndex = (__VU - 1) % TEST_USER_COUNT;
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
