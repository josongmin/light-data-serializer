Super light data serializer.
============

It supports 3byte, 5byte, 6byte and 7byte decimal also.
Let's save your precious storage space!

Compared with gson, It can reduce the data size by more than 4 times, and the speed is more than 2 times faster.


Benchmark result of serizlizing and unserializing 1,000,000 data.
- GSON : 335 bytes per object, It takes 6151ms
- JO DATA SERIALIZER : 96 bytes per object, It took (1,000,000 data) 3265ms.

Remark
- It only supports primitive type of Java now.


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



