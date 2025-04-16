@echo off
set "FORGE_JAR=forge-1.20.6-50.1.0-client.jar"
set "MC_JAR=1.20.6.jar"
set "SRC_FILE=src\com\netease\mc\mod\filter\FilterHelper.java"

echo === コンパイル中... ===
javac -cp "%FORGE_JAR%;%MC_JAR%" -d bin %SRC_FILE%

echo === .class ファイルが bin\ に出力されました！ ===
pause
