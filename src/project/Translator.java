package project;

public class Translator {

    public static byte getASCII(char inputChar){
        byte resultNum = (byte)inputChar;
        return resultNum;
    }

    public static char getChar(int inputByte){
        char resultChar = (char)inputByte;
        return resultChar;
    }

    public static String getMessageASCII(String inputParas){
        /*用toCharArray方法把匯入的String拆成Char陣列*/
        char [] parasSpace = inputParas.toCharArray();
        /*根據Char陣列的大小生成對應用來裝ASCII的陣列*/
        byte [] parasEncode = new byte[parasSpace.length];
        /*生成StringBuilder物件*/
        StringBuilder sb0 = new StringBuilder();
        /*先存入整個String的長度在最前頭*/
        sb0.append(parasEncode.length);

        /*用迴圈進行逐個字元的轉換*/
        for(int i = 0; i < parasEncode.length; i++) {
            /*用逗號隔開每一項*/
            sb0.append(",");
            /*使用getASCII()方法轉換*/
            parasEncode[i] = getASCII(parasSpace[i]);
            /*填入StringBuilder*/
            sb0.append(parasEncode[i]);
        }
        /*用toString()方法讓StringBuilder轉回String*/
        return sb0.toString();
    }

    public static String getMessageChar(String inputParas){
        /*用split拆裝String*/
        String [] parasSpace = inputParas.split(",");
        /*先解開陣列的第一項，作為實際字串的長度*/
        char [] parasDecode = new char[Integer.parseInt(parasSpace[0])];
        /*用迴圈進行逐個字元的轉換*/
        for(int i = 0; i < parasDecode.length; i++){
            /*使用getChar()方法轉換*/
            parasDecode[i] = getChar(Byte.parseByte(parasSpace[i+1]));
        }
        /*new String(Char Array) = String*/
        return new String(parasDecode);
    }
}