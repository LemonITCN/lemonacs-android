package cn.lemage_scanlib.decode;

import android.content.Intent;
import android.net.Uri;

import com.google.zxing.BarcodeFormat;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * @author zhaoguangyang
 */
public final class DecodeFormatManager {

    private static final Pattern COMMA_PATTERN = Pattern.compile(",");
    public static final Set<BarcodeFormat> PRODUCT_FORMATS;
    static final Set<BarcodeFormat> INDUSTRIAL_FORMATS;
    static final Set<BarcodeFormat> ONE_D_FORMATS;
    static final Set<BarcodeFormat> QR_CODE_FORMATS;
    static final Set<BarcodeFormat> DATA_MATRIX_FORMATS;
    static final Set<BarcodeFormat> AZTEC_FORMATS;
    static final Set<BarcodeFormat> PDF417_FORMATS;
    private static final Map<String, Set<BarcodeFormat>> FORMATS_FOR_MODE;

    private DecodeFormatManager() {
    }

    public static Set<BarcodeFormat> parseDecodeFormats(Intent intent) {
        Iterable<String> scanFormats = null;
        CharSequence scanFormatsString = intent.getStringExtra("SCAN_FORMATS");
        if (scanFormatsString != null) {
            scanFormats = Arrays.asList(COMMA_PATTERN.split(scanFormatsString));
        }

        return parseDecodeFormats(scanFormats, intent.getStringExtra("SCAN_MODE"));
    }

    public static Set<BarcodeFormat> parseDecodeFormats(Uri inputUri) {
        List<String> formats = inputUri.getQueryParameters("SCAN_FORMATS");
        if (formats != null && formats.size() == 1 && formats.get(0) != null) {
            formats = Arrays.asList(COMMA_PATTERN.split((CharSequence)formats.get(0)));
        }

        return parseDecodeFormats(formats, inputUri.getQueryParameter("SCAN_MODE"));
    }

    private static Set<BarcodeFormat> parseDecodeFormats(Iterable<String> scanFormats, String decodeMode) {
        if (scanFormats != null) {
            EnumSet formats = EnumSet.noneOf(BarcodeFormat.class);

            try {
                Iterator var3 = scanFormats.iterator();

                while(var3.hasNext()) {
                    String format = (String)var3.next();
                    formats.add(BarcodeFormat.valueOf(format));
                }

                return formats;
            } catch (IllegalArgumentException var5) {
                ;
            }
        }

        return decodeMode != null ? (Set)FORMATS_FOR_MODE.get(decodeMode) : null;
    }

    static {
        QR_CODE_FORMATS = EnumSet.of(BarcodeFormat.QR_CODE);
        DATA_MATRIX_FORMATS = EnumSet.of(BarcodeFormat.DATA_MATRIX);
        AZTEC_FORMATS = EnumSet.of(BarcodeFormat.AZTEC);
        PDF417_FORMATS = EnumSet.of(BarcodeFormat.PDF_417);
        PRODUCT_FORMATS = EnumSet.of(BarcodeFormat.UPC_A, BarcodeFormat.UPC_E, BarcodeFormat.EAN_13, BarcodeFormat.EAN_8, BarcodeFormat.RSS_14, BarcodeFormat.RSS_EXPANDED);
        INDUSTRIAL_FORMATS = EnumSet.of(BarcodeFormat.CODE_39, BarcodeFormat.CODE_93, BarcodeFormat.CODE_128, BarcodeFormat.ITF, BarcodeFormat.CODABAR);
        ONE_D_FORMATS = EnumSet.copyOf(PRODUCT_FORMATS);
        ONE_D_FORMATS.addAll(INDUSTRIAL_FORMATS);
        FORMATS_FOR_MODE = new HashMap();
        FORMATS_FOR_MODE.put("ONE_D_MODE", ONE_D_FORMATS);
        FORMATS_FOR_MODE.put("PRODUCT_MODE", PRODUCT_FORMATS);
        FORMATS_FOR_MODE.put("QR_CODE_MODE", QR_CODE_FORMATS);
        FORMATS_FOR_MODE.put("DATA_MATRIX_MODE", DATA_MATRIX_FORMATS);
        FORMATS_FOR_MODE.put("AZTEC_MODE", AZTEC_FORMATS);
        FORMATS_FOR_MODE.put("PDF417_MODE", PDF417_FORMATS);
    }
}
