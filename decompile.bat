@echo off
set "TARGET_JAR=4676951340977830700@3@0.jar"
set "CFR_JAR=cfr-0.152.jar"

:: 解凍するクラス
set "TARGET_CLASS=com/netease/mc/mod/filter/FilterHelper.class"
set "INNER_CLASS1=com/netease/mc/mod/filter/FilterHelper$1.class"
set "INNER_CLASS2=com/netease/mc/mod/filter/FilterHelper$2.class"
set "INNER_CLASS3=com/netease/mc/mod/filter/FilterWrapper$1.class"

echo === 解凍中 (%TARGET_CLASS% とその内部クラス) ===
mkdir temp_class
cd temp_class
jar xf ..\%TARGET_JAR% %TARGET_CLASS%
jar xf ..\%TARGET_JAR% %INNER_CLASS1%
jar xf ..\%TARGET_JAR% %INNER_CLASS2%
jar xf ..\%TARGET_JAR% %INNER_CLASS3%
cd ..

echo === CFRでデコンパイル中... ===
java -jar %CFR_JAR% temp_class\%TARGET_CLASS% --outputdir src
java -jar %CFR_JAR% temp_class\%INNER_CLASS1% --outputdir src
java -jar %CFR_JAR% temp_class\%INNER_CLASS2% --outputdir src
java -jar %CFR_JAR% temp_class\%INNER_CLASS3% --outputdir src

echo === 完了！src に Java ファイルが出力されました ===
pause
