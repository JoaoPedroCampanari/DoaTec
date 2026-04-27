@echo off
REM Exporta as imagens Docker do DoaTec para transferir para outro computador

echo ============================================
echo   DoaTec - Exportar imagens Docker
echo ============================================
echo.

REM Criar pasta de exportacao
set EXPORT_DIR=doatec-docker-export
if exist %EXPORT_DIR% rmdir /s /q %EXPORT_DIR%
mkdir %EXPORT_DIR%

REM Buildar a imagem com tag definida
echo [1/6] Construindo imagem do app...
docker build -t doatec-app:latest .
if %errorlevel% neq 0 (
    echo ERRO: Falha ao construir a imagem.
    pause
    exit /b 1
)

REM Exportar imagem do app
echo [2/6] Exportando imagem do app...
docker save -o %EXPORT_DIR%\doatec-app.tar doatec-app:latest
if %errorlevel% neq 0 (
    echo ERRO: Falha ao exportar imagem do app.
    pause
    exit /b 1
)

REM Baixar e exportar imagem do PostgreSQL
echo [3/6] Baixando e exportando imagem do PostgreSQL...
docker pull postgres:16-alpine
docker save -o %EXPORT_DIR%\postgres.tar postgres:16-alpine
if %errorlevel% neq 0 (
    echo ERRO: Falha ao exportar imagem do PostgreSQL.
    pause
    exit /b 1
)

REM Baixar e exportar imagem do pgAdmin
echo [4/6] Baixando e exportando imagem do pgAdmin...
docker pull dpage/pgadmin4:latest
docker save -o %EXPORT_DIR%\pgadmin.tar dpage/pgadmin4:latest
if %errorlevel% neq 0 (
    echo ERRO: Falha ao exportar imagem do pgAdmin.
    pause
    exit /b 1
)

REM Copiar SERVICOS.md e criar docker-compose.yml de producao (image em vez de build)
echo [5/6] Copiando arquivos de configuracao...
copy SERVICOS.md %EXPORT_DIR%\ >nul 2>nul

echo services: > %EXPORT_DIR%\docker-compose.yml
echo   app: >> %EXPORT_DIR%\docker-compose.yml
echo     image: doatec-app:latest >> %EXPORT_DIR%\docker-compose.yml
echo     ports: >> %EXPORT_DIR%\docker-compose.yml
echo       - "8080:8080" >> %EXPORT_DIR%\docker-compose.yml
echo     volumes: >> %EXPORT_DIR%\docker-compose.yml
echo       - ./uploads:/app/uploads >> %EXPORT_DIR%\docker-compose.yml
echo     environment: >> %EXPORT_DIR%\docker-compose.yml
echo       - SPRING_PROFILES_ACTIVE=docker >> %EXPORT_DIR%\docker-compose.yml
echo     depends_on: >> %EXPORT_DIR%\docker-compose.yml
echo       db: >> %EXPORT_DIR%\docker-compose.yml
echo         condition: service_healthy >> %EXPORT_DIR%\docker-compose.yml
echo     restart: on-failure >> %EXPORT_DIR%\docker-compose.yml
echo. >> %EXPORT_DIR%\docker-compose.yml
echo   db: >> %EXPORT_DIR%\docker-compose.yml
echo     image: postgres:16-alpine >> %EXPORT_DIR%\docker-compose.yml
echo     environment: >> %EXPORT_DIR%\docker-compose.yml
echo       POSTGRES_DB: doatec >> %EXPORT_DIR%\docker-compose.yml
echo       POSTGRES_USER: doatec_user >> %EXPORT_DIR%\docker-compose.yml
echo       POSTGRES_PASSWORD: doatec_password >> %EXPORT_DIR%\docker-compose.yml
echo     ports: >> %EXPORT_DIR%\docker-compose.yml
echo       - "5432:5432" >> %EXPORT_DIR%\docker-compose.yml
echo     volumes: >> %EXPORT_DIR%\docker-compose.yml
echo       - postgres_data:/var/lib/postgresql/data >> %EXPORT_DIR%\docker-compose.yml
echo     healthcheck: >> %EXPORT_DIR%\docker-compose.yml
echo       test: ["CMD-SHELL", "pg_isready -U doatec_user -d doatec"] >> %EXPORT_DIR%\docker-compose.yml
echo       interval: 5s >> %EXPORT_DIR%\docker-compose.yml
echo       timeout: 5s >> %EXPORT_DIR%\docker-compose.yml
echo       retries: 10 >> %EXPORT_DIR%\docker-compose.yml
echo. >> %EXPORT_DIR%\docker-compose.yml
echo   pgadmin: >> %EXPORT_DIR%\docker-compose.yml
echo     image: dpage/pgadmin4:latest >> %EXPORT_DIR%\docker-compose.yml
echo     environment: >> %EXPORT_DIR%\docker-compose.yml
echo       PGADMIN_DEFAULT_EMAIL: admin@doatec.com >> %EXPORT_DIR%\docker-compose.yml
echo       PGADMIN_DEFAULT_PASSWORD: admin123 >> %EXPORT_DIR%\docker-compose.yml
echo     ports: >> %EXPORT_DIR%\docker-compose.yml
echo       - "5050:80" >> %EXPORT_DIR%\docker-compose.yml
echo     depends_on: >> %EXPORT_DIR%\docker-compose.yml
echo       db: >> %EXPORT_DIR%\docker-compose.yml
echo         condition: service_healthy >> %EXPORT_DIR%\docker-compose.yml
echo     volumes: >> %EXPORT_DIR%\docker-compose.yml
echo       - pgadmin_data:/var/lib/pgadmin >> %EXPORT_DIR%\docker-compose.yml
echo. >> %EXPORT_DIR%\docker-compose.yml
echo volumes: >> %EXPORT_DIR%\docker-compose.yml
echo   postgres_data: >> %EXPORT_DIR%\docker-compose.yml
echo   pgadmin_data: >> %EXPORT_DIR%\docker-compose.yml

REM Criar script de importacao
echo [6/6] Criando script de importacao...
echo @echo off > %EXPORT_DIR%\import-docker.bat
echo REM Importa e roda as imagens Docker do DoaTec >> %EXPORT_DIR%\import-docker.bat
echo. >> %EXPORT_DIR%\import-docker.bat
echo echo ============================================ >> %EXPORT_DIR%\import-docker.bat
echo echo   DoaTec - Importar e iniciar aplicacao >> %EXPORT_DIR%\import-docker.bat
echo echo ============================================ >> %EXPORT_DIR%\import-docker.bat
echo echo. >> %EXPORT_DIR%\import-docker.bat
echo echo [1/4] Carregando imagem do app... >> %EXPORT_DIR%\import-docker.bat
echo docker load -i doatec-app.tar >> %EXPORT_DIR%\import-docker.bat
echo if %%errorlevel%% neq 0 ^( >> %EXPORT_DIR%\import-docker.bat
echo     echo ERRO: Falha ao carregar imagem do app. >> %EXPORT_DIR%\import-docker.bat
echo     pause >> %EXPORT_DIR%\import-docker.bat
echo     exit /b 1 >> %EXPORT_DIR%\import-docker.bat
echo ^) >> %EXPORT_DIR%\import-docker.bat
echo. >> %EXPORT_DIR%\import-docker.bat
echo echo [2/4] Carregando imagem do PostgreSQL... >> %EXPORT_DIR%\import-docker.bat
echo docker load -i postgres.tar >> %EXPORT_DIR%\import-docker.bat
echo if %%errorlevel%% neq 0 ^( >> %EXPORT_DIR%\import-docker.bat
echo     echo ERRO: Falha ao carregar imagem do PostgreSQL. >> %EXPORT_DIR%\import-docker.bat
echo     pause >> %EXPORT_DIR%\import-docker.bat
echo     exit /b 1 >> %EXPORT_DIR%\import-docker.bat
echo ^) >> %EXPORT_DIR%\import-docker.bat
echo. >> %EXPORT_DIR%\import-docker.bat
echo echo [3/4] Carregando imagem do pgAdmin... >> %EXPORT_DIR%\import-docker.bat
echo docker load -i pgadmin.tar >> %EXPORT_DIR%\import-docker.bat
echo if %%errorlevel%% neq 0 ^( >> %EXPORT_DIR%\import-docker.bat
echo     echo ERRO: Falha ao carregar imagem do pgAdmin. >> %EXPORT_DIR%\import-docker.bat
echo     pause >> %EXPORT_DIR%\import-docker.bat
echo     exit /b 1 >> %EXPORT_DIR%\import-docker.bat
echo ^) >> %EXPORT_DIR%\import-docker.bat
echo. >> %EXPORT_DIR%\import-docker.bat
echo echo [4/4] Iniciando aplicacao... >> %EXPORT_DIR%\import-docker.bat
echo docker compose up -d >> %EXPORT_DIR%\import-docker.bat
echo. >> %EXPORT_DIR%\import-docker.bat
echo echo. >> %EXPORT_DIR%\import-docker.bat
echo echo DoaTec esta rodando! >> %EXPORT_DIR%\import-docker.bat
echo echo   App:     http://localhost:8080 >> %EXPORT_DIR%\import-docker.bat
echo echo   pgAdmin: http://localhost:5050 >> %EXPORT_DIR%\import-docker.bat
echo echo. >> %EXPORT_DIR%\import-docker.bat
echo echo Parar: docker compose down >> %EXPORT_DIR%\import-docker.bat
echo echo Logs: docker compose logs -f app >> %EXPORT_DIR%\import-docker.bat
echo pause >> %EXPORT_DIR%\import-docker.bat

echo.
echo ============================================
echo   Exportacao concluida!
echo ============================================
echo.
echo   Pasta: %EXPORT_DIR%\
echo.
echo   Conteudo:
dir /b %EXPORT_DIR%
echo.
echo   Copie a pasta "%EXPORT_DIR%" para o pendrive
echo   No outro computador, execute import-docker.bat
echo.
pause
