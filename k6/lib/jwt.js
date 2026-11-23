import crypto from 'k6/crypto';
import encoding from 'k6/encoding';

const HMAC_ALGORITHM = {
    HS256: 'sha256',
    HS384: 'sha384',
    HS512: 'sha512',
};

function base64UrlEncode(text) {
    return encoding.b64encode(text, 'rawurl');
}

export function createAccessToken(secret, algorithm, userId, email, roles, expirySeconds) {
    const header = base64UrlEncode(JSON.stringify({ alg: algorithm, typ: 'JWT' }));

    const issuedAt = Math.floor(Date.now() / 1000);
    const payload = base64UrlEncode(
        JSON.stringify({
            sub: String(userId),
            email: email,
            roles: roles,
            iat: issuedAt,
            exp: issuedAt + expirySeconds,
        })
    );

    const signingInput = `${header}.${payload}`;
    const signature = crypto.hmac(HMAC_ALGORITHM[algorithm], secret, signingInput, 'base64rawurl');

    return `${signingInput}.${signature}`;
}
