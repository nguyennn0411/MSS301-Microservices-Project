param([string]$MavenPath = 'C:/Program Files/JetBrains/IntelliJ IDEA 2025.3.1/plugins/maven/lib/maven3/bin/mvn.cmd')
$ErrorActionPreference = 'Stop'
$repoDir = (Resolve-Path (Join-Path $PSScriptRoot '../..')).Path
if (!(Test-Path -LiteralPath $MavenPath)) { $MavenPath = (Get-Command mvn -ErrorAction Stop).Source }
$logDir = Join-Path $repoDir 'logs/order-payment'
New-Item -ItemType Directory -Force $logDir | Out-Null
# Dedicated smoke databases; never use the application's regular databases here.
foreach ($serviceName in @('order','payment')) {
 $databaseName = "ecommerce_${serviceName}_postman_smoke"
 $runArgs = "--eureka.client.enabled=false --spring.flyway.enabled=false --spring.jpa.hibernate.ddl-auto=update --spring.datasource.url=jdbc:mysql://localhost:3306/${databaseName}?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
 $mavenArgs = "-B -pl services/$serviceName-service spring-boot:run -Dmaven.test.skip=true `"-Dspring-boot.run.arguments=$runArgs`""
 Start-Process -FilePath $MavenPath -ArgumentList $mavenArgs -WorkingDirectory $repoDir -WindowStyle Hidden -RedirectStandardOutput (Join-Path $logDir "$serviceName.log") -RedirectStandardError (Join-Path $logDir "$serviceName.err.log")
}
Write-Output "Logs: $logDir"
