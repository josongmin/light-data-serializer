Super light data serializer.
============
It supports 3byte, 5byte, 6byte and 7byte decimal also.
Let's save your precious storage space!

Compared with gson, It can reduce the data size by more than 4 times, and the speed is more than 2 times faster.

Result of serizlizing and unserializing 1,000,000 data.
 -> gson :: 335 bytes per object, It takes 6151ms
 -> jo data serializer :: 96 bytes per object, It took (1,000,000 data) 3265ms.



1. Serialize object to byte[]
```java
byte[] rawBytes = JoDataSerializerUtil.serialize(data);
```

2. Unserialize byte[] to object 
```java
MyData mydata = JoDataSerializerUtil.unserialize(rawBytes, MyData.class);
```

3. Custom byte decimal
```java
@JoDataColumn(byteSize = 3)
public int number3byte = 16777216;

@JoDataColumn(byteSize = 5)
public long number5byte =  1099511627776L;

@JoDataColumn(byteSize = 6)
public long number6byte =  281474976710656L;

@JoDataColumn(byteSize = 7)
public long number7byte =  72057594037927900L;
```

4. Performance result
Below is sample code for benchmarking
```java
public class TestData {
    public byte byte1 = Byte.MIN_VALUE;
    public byte byte2 = Byte.MAX_VALUE;
    public short short1 = Short.MIN_VALUE;
    public short short2 = Short.MAX_VALUE;
    public int int1 = Integer.MIN_VALUE;
    public int int2 = Integer.MAX_VALUE;
    public long long1 = Long.MIN_VALUE;
    public long long2 = Long.MAX_VALUE;

    @JoDataColumn(byteSize = 3)
    public int number3byte = Integer.MAX_VALUE;

    @JoDataColumn(byteSize = 5)
    public long number5byte = 4294967296L;

    @JoDataColumn(byteSize = 6)
    public long number6byte = 4294967296L;

    @JoDataColumn(byteSize = 7)
    public long number7byte = 4294967296L;


    public String tinyString = "asfasfasdf";

    @JoDataColumn(textType = JoDataColumn.TEXT)
    public String string = "asfasfasdf";

    @JoDataColumn(textType = JoDataColumn.MEDIUMTEXT)
    public String mediumString = "asfasfasdfasdfasdf";
    
}


public void test(){
    Gson gson = new Gson();

    long begin = System.currentTimeMillis();
    String json = gson.toJson(testData);
    TestData t = gson.fromJson(json, TestData.class);

    for(long i = 0; i < 1000000L; i++){
        json = gson.toJson(testData);
        t = gson.fromJson(json, TestData.class);
    }
    System.out.println("GSON SERIALIZER RESULT");
    System.out.println("gson data size : " + json.getBytes().length + " bytes / speed(1,000,000 data) " + (System.currentTimeMillis() - begin) + "ms");


    begin = System.currentTimeMillis();
    byte[] rawBytes = JoDataSerializerUtil.serialize(testData);
    t = JoDataSerializerUtil.deserialize(rawBytes, TestData.class);
    for(long i = 0; i < 1000000L; i++){
        rawBytes = JoDataSerializerUtil.serialize(testData);
        t = JoDataSerializerUtil.deserialize(rawBytes, TestData.class);
    }

    System.out.println("JO SERIALIZER RESULT");
    System.out.println("jo data size : " + rawBytes.length + " bytes / speed(1,000,000 data) " + (System.currentTimeMillis() - begin) + "ms");
}
```

GSON SERIALIZER RESULT
gson data size : 335 bytes / It took (1,000,000 data) 6151ms

JO SERIALIZER RESULT
jo data size : 96 bytes / It took (1,000,000 data) 3265ms.


5. Remark
It only supports primitive type of Java now.

