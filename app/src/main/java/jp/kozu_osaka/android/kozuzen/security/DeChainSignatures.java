package jp.kozu_osaka.android.kozuzen.security;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.pm.SigningInfo;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Locale;

import jp.kozu_osaka.android.kozuzen.KozuZen;
import jp.kozu_osaka.android.kozuzen.util.Logger;

public final class DeChainSignatures {
    private DeChainSignatures() {}

    /**
     * @return 署名が存在しない場合は要素数0、例外発生時は{@code null}を返す。
     */
    public static String[] getSignatureHexStringArray() {
        /*
        try {
            PackageInfo info = KozuZen.getInstance().getPackageManager().getPackageInfo(
                    KozuZen.getInstance().getPackageName(),
                    PackageManager.GET_SIGNING_CERTIFICATES
            );
            SigningInfo signingInfo = info.signingInfo;
            if(signingInfo == null) return new String[]{};
            Signature[] signatures;
            String[] hexStrings;
            if (signingInfo.hasMultipleSigners()) {
                signatures = signingInfo.getApkContentsSigners();
            } else {
                signatures = signingInfo.getSigningCertificateHistory();
            }
            hexStrings = new String[signatures.length];
            for(int i = 0; i < signatures.length; i++) {
                CertificateFactory cf = CertificateFactory.getInstance("X.509");
                X509Certificate x509 = (X509Certificate)cf.generateCertificate(new ByteArrayInputStream(signatures[i].toByteArray()));
                StringBuilder builder = new StringBuilder();
                for(int j = 0; j < 20; j++) {
                    builder.append(String.format(Locale.JAPAN, "%02x", x509.getSignature()[j]));
                }
                hexStrings[i] = builder.toString();
            }
            return hexStrings;
        } catch(PackageManager.NameNotFoundException | CertificateException e) {
            KozuZen.createErrorReport(e);
        }
        return null;*/
        return new String[]{"t"};
    }
}
