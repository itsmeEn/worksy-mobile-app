$wrapperJar = "gradle/wrapper/gradle-wrapper.jar"
$wrapperUrl = "https://raw.githubusercontent.com/gradle/gradle/v8.2.0/gradle/wrapper/gradle-wrapper.jar"

if (!(Test-Path -Path "gradle/wrapper")) {
    New-Item -ItemType Directory -Path "gradle/wrapper" -Force
}

Invoke-WebRequest -Uri $wrapperUrl -OutFile $wrapperJar
