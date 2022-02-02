package uniregistrar.driver.did.sov.util;

import org.abstractj.kalium.NaCl;

public class Ed25519Util {

    static {
        NaCl.init();
    }

    public static void generateKeypair(byte[] publicKeyBytesBuffer, byte[] privateKeyBytesBuffer, byte[] seedBytes) {
        NaCl.sodium().crypto_sign_ed25519_seed_keypair(publicKeyBytesBuffer, privateKeyBytesBuffer, seedBytes);
    }
}
