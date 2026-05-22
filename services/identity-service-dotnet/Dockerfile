FROM mcr.microsoft.com/dotnet/sdk:8.0 AS build
WORKDIR /src
COPY ["IdentityService.csproj", "./"]
RUN dotnet restore "IdentityService.csproj"
COPY . .
RUN dotnet publish "IdentityService.csproj" -c Release -o /app/publish

FROM mcr.microsoft.com/dotnet/aspnet:8.0
WORKDIR /app
RUN apt-get update && apt-get install -y --no-install-recommends curl && rm -rf /var/lib/apt/lists/*
COPY --from=build /app/publish .
EXPOSE 80
ENTRYPOINT ["dotnet", "IdentityService.dll"]
