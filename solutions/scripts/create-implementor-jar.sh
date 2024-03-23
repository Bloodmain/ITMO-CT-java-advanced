#!/bin/sh
tmpDir="$(pwd)/compiled"
javac -d "$tmpDir" "$(pwd)/../java-solutions/info/kgeorgiy/ja/dunaev/implementor/Implementor.java" \
      -cp "$(pwd)/../../tests/modules/info.kgeorgiy.java.advanced.implementor:$(pwd)/../java-solutions"
jar cfm "$(pwd)/implementor.jar" "$(pwd)/MANIFEST.MF" -C "$tmpDir" .
rm -rf "$tmpDir"
