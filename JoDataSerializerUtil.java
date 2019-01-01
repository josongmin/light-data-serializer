
public class JoDataSerializerUtil {

    /**디시리얼라이징 */
    public final static <T> T deserialize(byte[] rawBytes, Class cls){

        try{
            T obj = (T)cls.getConstructor().newInstance();
            Field[] fields = obj.getClass().getDeclaredFields();

            int offset = 0;

            for (Field field : fields) {
                JoDataColumn column = field.getAnnotation(JoDataColumn.class);
                field.setAccessible(true);

                switch (field.getType().getName()) {

                    case "byte": {
                        int byteSize = 1;
                        byte value = (byte)intFromBytes(copyBytes(rawBytes, offset, byteSize));
                        offset += byteSize;
                        field.setByte(obj, value);
                        break;
                    }

                    case "short": {
                        int byteSize = 2;
                        short value = (short)intFromBytes(copyBytes(rawBytes, offset, byteSize));
                        offset += byteSize;
                        field.setShort(obj, value);
                        break;
                    }

                    case "int": {
                        int byteSize = 4;
                        int value = intFromBytes(copyBytes(rawBytes, offset, byteSize));
                        offset += byteSize;
                        field.setInt(obj, value);
                        break;
                    }

                    case "long":{

                        int byteSize = 8;
                        if(column != null && column.byteSize() != -1){
                            byteSize = column.byteSize();
                        }

                        long value = longFromBytes(copyBytes(rawBytes, offset, byteSize));
                        offset += byteSize;
                        field.setLong(obj, value);

                        break;
                    }

                    case "java.lang.String": {
                        int maxLength = 256;
                        if(column != null){
//                            maxLength = getTextHeaderBytes(column.textType());
                            maxLength = getTextHeaderBytes(column.textType());
                        }
                        int bits = (int)Math.ceil(Math.log(maxLength) / Math.log(2)); //비트길이
                        int lengthIndicatorByteSize = (bits / 8) + (bits % 8 > 0 ? 1 : 0); //길이헤더 바이트

                        //실제 문자열 길이 가져오기
                        int stringLength = intFromBytes(copyBytes(rawBytes, offset, lengthIndicatorByteSize));
                        offset += lengthIndicatorByteSize;

                        //실제 값
                        String value = new String(copyBytes(rawBytes, offset, stringLength), StandardCharsets.UTF_8);
                        offset += stringLength;

                        field.set(obj, value);
                        break;
                    }
                }
            }

            return obj;
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    /**시리얼 라이즈 함*/
    public static byte[] serialize(Object obj){
        try{
            DataSchemeInfo dataSchemeInfo = getInfoForSerializing(obj);
            ByteBuffer resultByteBuffer = ByteBuffer.allocate(dataSchemeInfo.totalBytes);

            //실제 할당
            for(int i = 0; i < dataSchemeInfo.dataMap.size(); i++){
                DataSchemeInfo.Data data = dataSchemeInfo.dataMap.get(i);
                resultByteBuffer.put(data.bytes);
            }

            return resultByteBuffer.array();
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    //정보 조회
    private static DataSchemeInfo getInfoForSerializing(Object obj) throws Exception{

        DataSchemeInfo sippDataSchemeInfo = new DataSchemeInfo();

        Field[] fields = obj.getClass().getDeclaredFields();
        int totalBytes = 0;

        int pos = 0;
        for (Field field : fields) {
            JoDataColumn column = field.getAnnotation(JoDataColumn.class);
            field.setAccessible(true);

            DataSchemeInfo.Data data = new DataSchemeInfo.Data();
            sippDataSchemeInfo.dataMap.put(pos, data);

            data.value = field.get(obj); //값
            data.name = field.getName();

            Class cType = field.getType();
            switch (field.getType().getName()){

                case "byte":{
                    data.byteSize = 1;
                    data.bytes = bytesToInt((byte)data.value, data.byteSize);
                    break;
                }
                case "short":{
                    data.byteSize = 2;
                    data.bytes = bytesToInt((short)data.value, data.byteSize);
                    break;
                }

                case "int":{
                    data.byteSize = 4;
                    data.bytes = bytesToInt((int)data.value, data.byteSize);
                    break;
                }

                case "long":{
                    data.byteSize = 8;
                    if(column != null && column.byteSize() != -1){
                        data.byteSize = column.byteSize();
                    }
                    data.bytes = bytesToLong((long)data.value, data.byteSize);
                    break;
                }

                case "java.lang.String":{
                    int maxLength = 256; //default
                    if(column != null){ //column태그 있으면.
                        maxLength = getTextHeaderBytes(column.textType());
                    }

                    int bits = (int)Math.ceil(Math.log(maxLength) / Math.log(2)); //최대 비트 개수
                    int stringLengthByteSize = (bits / 8) + (bits % 8 > 0 ? 1 : 0); //사이즈용 헤더 길이
                    int stringLength = Math.min(maxLength, ((String)data.value).getBytes().length); //실제 문자열길이

                    data.byteSize = stringLengthByteSize + stringLength; //전체 길이

                    ByteBuffer localByteBuffer = ByteBuffer.allocate(data.byteSize);
                    localByteBuffer.put(bytesToInt(stringLength, stringLengthByteSize)); //길이 넣고
                    localByteBuffer.put(copyBytes(((String)(data.value)).getBytes(), 0, stringLength)); //실제 데이터 넣고

                    data.bytes = localByteBuffer.array();

                    break;
                }
            }
            //전체 사이즈
            pos++;
            sippDataSchemeInfo.totalBytes += data.byteSize;
        }

        return sippDataSchemeInfo;
    }

    ///스트링헤더
    private final static int getTextHeaderBytes(int textType){
        switch (textType){
            case JoDataColumn.TINYTEXT:
                return 256;
            case JoDataColumn.TEXT:
                return 65536;
            case JoDataColumn.MEDIUMTEXT:
                return 16777216;
//            case JoDataColumn.LONGTEXT:
//                return 4294967296L;
        }
        return 256;
    }

    /**Convert bytes -> int */
    private static byte[] bytesToInt(int value, int byteSize){
        byte[] bytes = new byte[byteSize];

        int bitLength = byteSize * 8;
        for(int i = 0; i < byteSize; i++){
            bytes[i] = (byte)((value >> (bitLength - (8 * (i + 1)))) & 0xFF) ;
        }
        return bytes;
    }

    /**Convert bytes -> long */
    private static byte[] bytesToLong(long value, int byteSize){
        byte[] bytes = new byte[byteSize];

        int bitLength = byteSize * 8;
        for(int i = 0; i < byteSize; i++){
            bytes[i] = (byte)((value >> (bitLength - (8 * (i + 1)))) & 0xFF) ;
        }
        return bytes;

    }

    /**byte[] -> long*/
    private static int intFromBytes(byte[] bytes){
        ByteBuffer bf = ByteBuffer.allocate(Integer.BYTES);
        bf.put(new byte[Integer.BYTES - bytes.length]);
        bf.put(bytes);
        byte[] rbytes = bf.array();
        return (
                ( (int) rbytes[3]) & 0xFF) +
                ( ( ( (int) rbytes[2]) & 0xFF) << 8) +
                ( ( ( (int) rbytes[1]) & 0xFF) << 16) +
                ( ( ( (int) rbytes[0]) & 0xFF) << 24)
                ;
    }

    /**byte[] -> long*/
    private static long longFromBytes(byte[] bytes){
        ByteBuffer bf = ByteBuffer.allocate(Long.BYTES);
        bf.put(new byte[Long.BYTES - bytes.length]);
        bf.put(bytes);
        byte[] rbytes = bf.array();
        return ( ( (long) rbytes[7]) & 0xFF) +
                ( ( ( (long) rbytes[6]) & 0xFF) << 8) +
                ( ( ( (long) rbytes[5]) & 0xFF) << 16) +
                ( ( ( (long) rbytes[4]) & 0xFF) << 24) +
                ( ( ( (long) rbytes[3]) & 0xFF) << 32) +
                ( ( ( (long) rbytes[2]) & 0xFF) << 40) +
                ( ( ( (long) rbytes[1]) & 0xFF) << 48) +
                ( ( ( (long) rbytes[0]) & 0xFF) << 56);
    }

    /**copy*/
    private static byte[] copyBytes(byte[] source, int from, int length){
        byte[] dest = new byte[length];
        for(int i = 0; i < length; i++){
            dest[i] = source[from + i];
        }
        return dest;
    }

    //정보
    public static class DataSchemeInfo{
        public int totalBytes;
        public Map<Integer, Data> dataMap = new HashMap<>();

        public static class Data{
            public String name;
            public Object value;
            public byte[] bytes;
            public int byteSize;
        }
    }

}
