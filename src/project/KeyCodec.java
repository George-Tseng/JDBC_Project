package project;

public class KeyCodec {

    /**
     * 執行加密
     *
     * @param inputTime       簡易加密過的時戳(String)
     * @param inputMacAddress 簡易加密過的網卡資訊(String)
     * @param inputKey        使用者輸入的訊息(String)
     *
     * @return 較高強度加密後的資訊(String)
     */
    /* 執行加密 */
    protected static String getEncodeKeyLong(String inputTime, String inputMacAddress, String inputKey) {
        String result;

        /* 用split拆裝String */
        String[] timeSpace = inputTime.split(",");
        String[] macAddressSpace = inputMacAddress.split(",");

        /* 用toCharArray方法把匯入的String拆成Char陣列 */
        char[] keySpace = inputKey.toCharArray();

        /* 參數 */
        int paraTime = Integer.parseInt(timeSpace[0]);
        int paraMacAddress = Integer.parseInt(macAddressSpace[0]);

        /* 運算產物 */
        long paraResult, paraRest;

        /* 生成StringBuilder物件 */
        StringBuilder sb0 = new StringBuilder();

        /* 利用迴圈執行轉換 */
        for (int i = 0; i < keySpace.length; i++) {
            int paraInputTmp = Translator.getASCII(keySpace[i]);
            int paraInputPrime = PrimeNumber.getMaxPrimeNumI(paraInputTmp);
            int paraInput = paraInputPrime * 10 + (paraInputTmp - paraInputPrime);

            int paraT, paraM;
            long paraTotalUp, paraTotalDown;

            if (keySpace.length <= paraTime) {
                int paraTTmp = Integer.parseInt(timeSpace[i + 1]);
                int paraTPrime = PrimeNumber.getMaxPrimeNumI(paraTTmp);
                paraT = paraTPrime * 10 + (paraTTmp - paraTPrime);
            } else {
                int paraTTmp = Integer.parseInt(timeSpace[i % paraTime + 1]);
                int paraTPrime = PrimeNumber.getMaxPrimeNumI(paraTTmp);
                paraT = paraTPrime * 10 + (paraTTmp - paraTPrime);
            }

            if (keySpace.length <= paraMacAddress) {
                int paraMTmp = Integer.parseInt(macAddressSpace[i + 1]);
                int paraMPrime = PrimeNumber.getMaxPrimeNumI(paraMTmp);
                paraM = paraMPrime * 10 + (paraMTmp - paraMPrime);
            } else {
                int paraMTmp = Integer.parseInt(macAddressSpace[i % paraMacAddress + 1]);
                int paraMPrime = PrimeNumber.getMaxPrimeNumI(paraMTmp);
                paraM = paraMPrime * 10 + (paraMTmp - paraMPrime);
            }

            long paraTmp = (long) (Math.pow((paraMacAddress * paraM), 2) - Math.pow(paraInput, 2));
            paraTotalUp = (long) ((2 * (paraTime * paraT) * Math.pow((paraMacAddress * paraM), 2) * paraInput)
                    + (2 * paraInput + 1) * (paraTime * paraT) * paraTmp
                    + (Math.pow(paraInput, 2) * (paraTime * paraT) * paraTmp));
            paraTotalDown = paraMacAddress * paraM * paraInput * (paraTmp);

            paraResult = paraTotalUp / paraTotalDown;
            paraRest = paraTotalUp % paraTotalDown + paraResult;

            sb0.append(paraResult);
            sb0.append(":");
            sb0.append(Long.toHexString(paraRest));
            if (i < keySpace.length - 1) {
                sb0.append(",");
            }
        }
        result = sb0.toString();
        return result;
    }

    /**
     * 執行解密
     *
     * @param inputTime       時戳(String)
     * @param inputMacAddress 網卡資訊(String)
     * @param inputText       先前較高強度加密後的資訊(String)
     *
     * @return 使用者輸入的原始訊息(String)
     */
    /* 執行解密 */
    protected static String getDecodeKeyLong(String inputTime, String inputMacAddress, String inputText) {
        String result;

        /* 字串轉換 */
        String inputParaT = Translator.getMessageASCII(inputTime);
        String inputParaM = Translator.getMessageASCII(inputMacAddress);

        /* 用split拆裝String */
        String[] timeSpace = inputParaT.split(",");
        String[] macAddressSpace = inputParaM.split(",");
        String[] textSpace = inputText.split(",");

        /* 參數 */
        int paraTime = Integer.parseInt(timeSpace[0]);
        int paraMacAddress = Integer.parseInt(macAddressSpace[0]);

        /* 反運算參數 */
        long paraResult, paraRest;

        /* 生成StringBuilder物件 */
        StringBuilder sb0 = new StringBuilder();

        /* 利用迴圈執行轉換 */
        for (int i = 0; i < textSpace.length; i++) {
            /* 用split拆裝String */
            String[] keySpace = textSpace[i].split(":");

            int paraT, paraM;
            long paraTotalUp, paraTotalDown;

            if (textSpace.length <= paraTime) {
                int paraTTmp = Integer.parseInt(timeSpace[i + 1]);
                int paraTPrime = PrimeNumber.getMaxPrimeNumI(paraTTmp);
                paraT = paraTPrime * 10 + (paraTTmp - paraTPrime);
            } else {
                int paraTTmp = Integer.parseInt(timeSpace[i % paraTime + 1]);
                int paraTPrime = PrimeNumber.getMaxPrimeNumI(paraTTmp);
                paraT = paraTPrime * 10 + (paraTTmp - paraTPrime);
            }

            if (textSpace.length <= paraMacAddress) {
                int paraMTmp = Integer.parseInt(macAddressSpace[i + 1]);
                int paraMPrime = PrimeNumber.getMaxPrimeNumI(paraMTmp);
                paraM = paraMPrime * 10 + (paraMTmp - paraMPrime);
            } else {
                int paraMTmp = Integer.parseInt(macAddressSpace[i % paraMacAddress + 1]);
                int paraMPrime = PrimeNumber.getMaxPrimeNumI(paraMTmp);
                paraM = paraMPrime * 10 + (paraMTmp - paraMPrime);
            }

            paraResult = Long.parseLong(keySpace[0]);
            paraRest = Long.valueOf(keySpace[1], 16) - paraResult;

            for (int j = 0; j < 128; j++) {
                int jPrime = PrimeNumber.getMaxPrimeNumI(j);
                int jTmp = jPrime * 10 + (j - jPrime);
                long paraTmp = (long) (Math.pow((paraMacAddress * paraM), 2) - Math.pow(jTmp, 2));
                paraTotalUp = (long) ((2 * (paraTime * paraT) * Math.pow((paraMacAddress * paraM), 2) * jTmp)
                        + (2 * jTmp + 1) * (paraTime * paraT) * paraTmp
                        + (Math.pow(jTmp, 2) * (paraTime * paraT) * paraTmp));
                paraTotalDown = paraMacAddress * paraM * jTmp * (paraTmp);

                if (paraTotalDown != 0) {
                    if ((paraResult == paraTotalUp / paraTotalDown) && (paraRest == paraTotalUp % paraTotalDown)) {
                        sb0.append(Translator.getChar(j));
                        break;
                    }
                }
            }
        }
        result = sb0.toString();
        return result;
    }
}

