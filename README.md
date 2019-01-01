Super light data serializer.
============

It supports 3byte, 5byte, 6byte and 7byte decimal also.
Let's save your precious storage space!

Compared with json type string(using gson), It can reduce the data size by more than 4 times, and the speed is more than 2 times faster.


Benchmark result of serizlizing and unserializing 1,000,000 data.
- Gson : 335 bytes per object, It takes 6151ms
- JoDataSerializerUtil : 96 bytes per object, It takes (1,000,000 data) 3265ms.


Usage 
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

4. benchmark
```java
TestData testData = new TestData();
        
testData.int1 = Integer.MAX_VALUE;
testData.int2 = Integer.MIN_VALUE;
testData.short1 = Short.MIN_VALUE;
testData.short2 = Short.MAX_VALUE;
testData.int1 = Integer.MIN_VALUE;
testData.int2 = Integer.MAX_VALUE;
testData.long1 = Long.MIN_VALUE;
testData.long2 = Long.MAX_VALUE;

testData.number3byte = 16777216;
testData.number5byte = 4294967296L;
testData.number6byte = 281474976710656L;
testData.number7byte = 72057594037927900L;

testData.tinyString = "It supports 256 bytes string";
testData.string = "It supports 65,536 bytes string";
testData.mediumString = "It supports 16,777,216 bytes string";

//begin
Gson gson = new Gson();

long begin = System.currentTimeMillis();
String json = null;
TestData t = null;

for(long i = 0; i < 1000000L; i++){
    json = gson.toJson(testData);
    t = gson.fromJson(json, TestData.class);
}
System.out.println("gson data size : " + json.getBytes().length + " bytes / speed(1,000,000 data) " + (System.currentTimeMillis() - begin) + "ms");


begin = System.currentTimeMillis();
byte[] rawBytes = JoDataSerializerUtil.serialize(testData);
t = JoDataSerializerUtil.deserialize(rawBytes, TestData.class);
for(long i = 0; i < 1000000L; i++){
    rawBytes = JoDataSerializerUtil.serialize(testData);
    t = JoDataSerializerUtil.deserialize(rawBytes, TestData.class);
}

System.out.println("jo data size : " + rawBytes.length + " bytes / speed(1,000,000 data) " + (System.currentTimeMillis() - begin) + "ms");


```

Remark
- It only supports primitive type of Java now.



