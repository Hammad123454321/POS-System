@echo off
if "%~1" neq "_start_" (
  cmd /c "%~f0" _start_ %*
  popd
  exit /b
)
gradlew jfxJar && pushd .\build\jfx\POSLinkJavaDemo && java -jar POSLinkJavaDemo.jar