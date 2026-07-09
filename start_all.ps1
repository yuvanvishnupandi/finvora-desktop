$MavenPath = "$PWD\expense-tracker-client\apache-maven-3.9.6\bin"

if (-Not (Test-Path $MavenPath)) {
    Write-Host "Downloading Portable Maven to launch your apps..."
    Invoke-WebRequest -Uri "https://archive.apache.org/dist/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.zip" -OutFile "maven.zip"
    Expand-Archive -Path "maven.zip" -DestinationPath "expense-tracker-client"
    Remove-Item "maven.zip"
}

# Add Maven to path temporarily for this session
$env:Path += ";$MavenPath"
$env:JAVA_HOME = $env:JAVA_HOME

Write-Host "1. Starting Backend Spring Boot Server in a new window..."
Start-Process powershell -ArgumentList "-NoExit -Command `"`$env:Path += ';$MavenPath'; cd expense-tracker-springboot-server; mvn spring-boot:run`""

Write-Host "Waiting 15 seconds for the database and server to boot up..."
Start-Sleep -Seconds 15

Write-Host "2. Starting JavaFX Client App..."
cd expense-tracker-client
mvn compile javafx:run
