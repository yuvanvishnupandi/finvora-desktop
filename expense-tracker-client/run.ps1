Write-Host "Downloading Portable Maven to launch your JavaFX App..."
If (-Not (Test-Path "apache-maven-3.9.6")) {
    Invoke-WebRequest -Uri "https://archive.apache.org/dist/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.zip" -OutFile "maven.zip"
    Expand-Archive -Path "maven.zip" -DestinationPath "."
    Remove-Item "maven.zip"
}


$env:Path += ";$PWD\apache-maven-3.9.6\bin"
$env:JAVA_HOME = $env:JAVA_HOME

Write-Host "Compiling and Launching Finance Tracker APP..."

mvn compile javafx:run
