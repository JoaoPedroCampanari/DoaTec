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

REM Copiar docker-compose.yml e SERVICOS.md
echo [5/6] Copiando arquivos de configuracao...
copy docker-compose.yml %EXPORT_DIR%\ >nul
copy SERVICOS.md %EXPORT_DIR%\ >nul 2>nul

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
