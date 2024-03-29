#!/bin/sh
javadoc -d "$(pwd)/doc" -private -version -author \
        -classpath "$(pwd)/../../tests/modules/info.kgeorgiy.java.advanced.implementor:$(pwd)/../java-solutions" \
        "$(pwd)"/../java-solutions/info/kgeorgiy/ja/dunaev/implementor/*.java
