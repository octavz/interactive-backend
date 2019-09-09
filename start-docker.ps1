#!/usr/bin/pwsh

$env:POSTGRES_HOST="localhost"
$env:POSTGRES_PASSWORD="admin"

#$random=-join ((48..57) + (97..122) | Get-Random -Count 10 | % {[char]$_})
$name="postgres_service"

#docker ps -aq | %{docker stop $_}
#docker ps -aq | %{docker rm $_}

"Stopping and removing previous service..."
docker stop postgres_service
docker rm postgres_service

"Running new service..."
docker run -d -p 5432 --name $name -e POSTGRES_PASSWORD=$env:POSTGRES_PASSWORD postgres

"Extracting port and start..."
$env:POSTGRES_PORT=(docker port $name 5432/tcp).Split(":")[1]
docker ps

"Exported POSTGRES_PORT=$env:POSTGRES_PORT"
