param([string]$MavenPath='C:/Program Files/JetBrains/IntelliJ IDEA 2025.3.1/plugins/maven/lib/maven3/bin/mvn.cmd',[switch]$SkipBuild)
$ErrorActionPreference='Stop'
$repoDir=(Resolve-Path (Join-Path $PSScriptRoot '..')).Path
Set-Location $repoDir
if (Test-Path -LiteralPath (Join-Path $repoDir '.env')) {
    foreach ($line in Get-Content -LiteralPath (Join-Path $repoDir '.env')) {
        if ($line -match '^([A-Z][A-Z0-9_]*)=(.*)$' -and
            -not [Environment]::GetEnvironmentVariable($Matches[1], 'Process')) {
            [Environment]::SetEnvironmentVariable($Matches[1], $Matches[2], 'Process')
        }
    }
}
if(!(Test-Path $MavenPath)){$MavenPath=(Get-Command mvn -ErrorAction Stop).Source}
docker compose -f docker-compose.infra.yml up -d
if($LASTEXITCODE -ne 0){throw 'Docker infrastructure failed'}
New-Item -ItemType Directory -Force logs/shop | Out-Null
if(!$SkipBuild){& $MavenPath -B -pl services/order-service,services/payment-service,services/product-catalog-service -am install '-Dmaven.test.skip=true';if($LASTEXITCODE -ne 0){throw 'Build failed'}}
foreach($serviceName in @('product-catalog','payment','order')){
 $arguments="-B -pl services/$serviceName-service spring-boot:run -Dmaven.test.skip=true -Dspring-boot.run.profiles=shop"
 Start-Process -FilePath $MavenPath -ArgumentList $arguments -WorkingDirectory $repoDir -WindowStyle Hidden -RedirectStandardOutput "$repoDir/logs/shop/$serviceName.log" -RedirectStandardError "$repoDir/logs/shop/$serviceName.err.log"
}
Write-Output 'Shop profile started: Product 8082, Order 8084, Payment 8085. Logs: logs/shop'
