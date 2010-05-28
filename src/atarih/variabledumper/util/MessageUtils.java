package atarih.variabledumper.util;

import com.ibm.icu.text.MessageFormat;

public class MessageUtils {

    public static String getFormattedString(String key, String... args) {
        return MessageFormat.format(OutputConstants.outputMap.get(key), args);
    }

}
