# Build (сборка, если кто не понял)
## Как перегенерить protocol.java

В директории ```space-expansion-client\src\main\java\``` выполняем:
```
E:\third-party\protobuf\bin\protoc.exe --java_out=spex .\com\github\evstafeeva\spaceexp\Protocol.proto
```

Предварительно нужно обновить Protocol.proto файл.
