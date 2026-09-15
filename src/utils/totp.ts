// RFC 6238 TOTP (Time-based One-Time Password) Implementation

const BASE32_ALPHABET = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ234567';

function base32ToBytes(base32: string): Uint8Array {
  const clean = base32.toUpperCase().replace(/=+$/, '').replace(/[\s-]/g, '');
  let bits = '';
  for (let i = 0; i < clean.length; i++) {
    const val = BASE32_ALPHABET.indexOf(clean[i]);
    if (val === -1) continue;
    bits += val.toString(2).padStart(5, '0');
  }

  const bytes = new Uint8Array(Math.floor(bits.length / 8));
  for (let i = 0; i < bytes.length; i++) {
    bytes[i] = parseInt(bits.slice(i * 8, i * 8 + 8), 2);
  }
  return bytes;
}

export function generateRandomBase32Secret(byteLength: number = 20): string {
  const bytes = crypto.getRandomValues(new Uint8Array(byteLength));
  let bits = '';
  for (let i = 0; i < bytes.length; i++) {
    bits += bytes[i].toString(2).padStart(8, '0');
  }

  let base32 = '';
  for (let i = 0; i < bits.length; i += 5) {
    const chunk = bits.slice(i, i + 5).padEnd(5, '0');
    base32 += BASE32_ALPHABET[parseInt(chunk, 2)];
  }
  return base32;
}

export async function generateTotpCode(
  secretBase32: string,
  timeMs: number = Date.now(),
  stepSeconds: number = 30
): Promise<string> {
  const keyBytes = base32ToBytes(secretBase32);
  const timeStep = Math.floor(timeMs / 1000 / stepSeconds);

  // 8-byte big-endian counter
  const counterBuffer = new ArrayBuffer(8);
  const counterView = new DataView(counterBuffer);
  counterView.setUint32(0, Math.floor(timeStep / 0x100000000));
  counterView.setUint32(4, timeStep & 0xffffffff);

  const cryptoKey = await crypto.subtle.importKey(
    'raw',
    keyBytes as unknown as BufferSource,
    { name: 'HMAC', hash: 'SHA-1' },
    false,
    ['sign']
  );

  const hmacSig = await crypto.subtle.sign('HMAC', cryptoKey, counterBuffer);
  const hmacBytes = new Uint8Array(hmacSig);

  // Dynamic truncation (RFC 4226)
  const offset = hmacBytes[hmacBytes.length - 1] & 0x0f;
  const binaryCode =
    ((hmacBytes[offset] & 0x7f) << 24) |
    ((hmacBytes[offset + 1] & 0xff) << 16) |
    ((hmacBytes[offset + 2] & 0xff) << 8) |
    (hmacBytes[offset + 3] & 0xff);

  const code = (binaryCode % 1_000_000).toString().padStart(6, '0');
  return code;
}

export async function verifyTotpCode(
  secretBase32: string,
  userCode: string,
  stepSeconds: number = 30,
  windowSteps: number = 1
): Promise<boolean> {
  const cleanCode = userCode.trim().replace(/\s/g, '');
  if (cleanCode.length !== 6 || !/^\d{6}$/.test(cleanCode)) return false;

  const now = Date.now();
  for (let w = -windowSteps; w <= windowSteps; w++) {
    const checkTime = now + w * stepSeconds * 1000;
    const generated = await generateTotpCode(secretBase32, checkTime, stepSeconds);
    if (generated === cleanCode) {
      return true;
    }
  }
  return false;
}

export function getTotpRemainingSeconds(stepSeconds: number = 30): number {
  const currentSeconds = Math.floor(Date.now() / 1000);
  return stepSeconds - (currentSeconds % stepSeconds);
}
