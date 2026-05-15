# Maze Android - Step by Step

Este guia explica como correr a app Android do projeto Maze, como gerar o APK, e como testar no emulador ou num telemóvel físico.

Nota sem rebuild: se a APK já estiver gerada e só quiseres instalar e abrir a app, podes usar os scripts de arranque sem refazer a build manualmente. O fluxo normal continua a ser `launch-emulator.ps1` ou `launch-phone.ps1`, que tratam do processo completo.

## 1. O que precisas antes de começar

- Android Studio instalado
- Android SDK instalado
- JDK 21 disponível
- `adb` no PATH
- A API/backend em execução

Se estiveres a usar a API local do projeto, confirma primeiro que ela responde:

```powershell
curl http://localhost:5000/api/temperature?limit=5
```

## 2. Abrir o projeto Android

O módulo Android está em `android/Maze`.

1. Abre o Android Studio.
2. Clica em `Open`.
3. Seleciona a pasta `android\Maze`.
4. Espera o Gradle sincronizar.

## 3. Configurar o SDK

Se o Android Studio pedir SDK, instala estas partes no `SDK Manager`:

- `Android SDK Platform`
- `Android SDK Build-Tools`
- `Android SDK Platform-Tools`
- `Android Emulator`

Se o projeto não detetar o SDK automaticamente, executa este script na pasta `android/Maze`:

```powershell
.\configure-android-sdk.ps1
```

Isto cria ou corrige o ficheiro `local.properties` com o caminho do SDK.

## 4. Gerar a APK

Na pasta `android/Maze`, corre:

```powershell
.\gradlew.bat assembleDebug
```

Se correr bem, a APK fica em:

```text
app\build\outputs\apk\debug\app-debug.apk
```

Se quiseres instalar logo a APK num device ligado, usa:

```powershell
.\build-and-install.ps1
```

## 6. Como testar no emulador

### 5.1 Criar o emulador

1. No Android Studio, abre `Device Manager`.
2. Clica em `Create device`.
3. Escolhe um modelo, por exemplo um Pixel.
4. Clica em `Next`.
5. Escolhe uma imagem do sistema, de preferência API 35 ou a versão pedida pelo projeto.
6. Faz `Download` se a imagem ainda não estiver instalada.
7. Clica em `Finish`.

### 5.2 Arrancar o emulador

1. No `Device Manager`, clica em `Run` no dispositivo criado.
2. Espera que o Android termine o boot.

### 5.3 Confirmar que o `adb` vê o emulador

```powershell
adb devices
```

Deves ver um device em estado `device`.

### 5.4 Instalar a app no emulador

Na pasta `android/Maze`, corre:

```powershell
.\build-and-install.ps1
```

## 7. Como testar num telemóvel físico

### 6.1 Preparar o telemóvel

1. Ativa `Developer options`.
2. Ativa `USB debugging`.
3. Liga o telemóvel ao PC por cabo USB.
4. Aceita o pedido de depuração no telemóvel.

### 6.2 Confirmar ligação

```powershell
adb devices
```

Se o telemóvel aparecer como `device`, está pronto.

### 6.3 Instalar a app

```powershell
.\build-and-install.ps1
```

Se houver mais do que um device ligado, podes indicar o ID manualmente:

```powershell
.\build-and-install.ps1 -DeviceId <device_id>
```

## 8. Como configurar o host da API na app

A app tem um campo para o host da API no ecrã de login.

- Se estiveres no emulador e a API estiver no teu PC, usa `10.0.2.2`.
- Se estiveres num telemóvel físico, usa o IP local do teu PC na rede.
- Se a API estiver noutro servidor, usa o respetivo endereço.

Exemplos:

```text
10.0.2.2
192.168.1.20
http://localhost:5000
```

Nota: no emulador, `localhost` refere-se ao próprio emulador, não ao PC.

## 9. O que testar depois de instalar

1. Abrir a app.
2. Fazer login.
3. Confirmar se a app consegue ler os dados de temperatura, som, mensagens e ocupação.
4. Verificar se os ecrãs abrem sem crash.

Se usares dados da simulação já carregados na base de dados, os gráficos e listas devem mostrar conteúdo logo após o login.

## 12. Problemas comuns

- `adb devices` não mostra nada: o emulador não está a correr ou o telemóvel não está com USB debugging ativo.
- A build falha com erro de JDK: confirma que o projeto está a usar o JDK 21.
- A app instala mas não mostra dados: confirma o host da API e se a API está a responder.
- A app dá erro de ligação no emulador: usa `10.0.2.2` em vez de `localhost`.

## 13. Scripts prontos para correr a app

O projeto inclui scripts PowerShell para tornar o arranque modular:

- `scripts\launch-emulator.ps1` para arrancar um emulador, compilar e instalar a app
- `scripts\launch-phone.ps1` para compilar e instalar a app num telemóvel físico
- `build-and-install.ps1` para o fluxo base de build + instalação

### 13.1 Arrancar emulador e correr a app

1. Cria um AVD no Android Studio, em `Device Manager`.
2. Garante que existe pelo menos um emulador configurado.
3. Corre o script:

```powershell
cd android\Maze
.\scripts\launch-emulator.ps1
```

Se quiseres indicar um AVD específico:

```powershell
.\scripts\launch-emulator.ps1 -AvdName "Pixel_8_API_35"
```

### 13.2 Correr num telemóvel físico

1. Liga o telemóvel por USB.
2. Ativa `USB debugging`.
3. Confirma que aparece em `adb devices`.
4. Corre o script:

```powershell
cd android\Maze
.\scripts\launch-phone.ps1
```

Se houver mais do que um device ligado, indica o ID manualmente:

```powershell
.\scripts\launch-phone.ps1 -DeviceId emulator-5554
```

## 11. Comandos rápidos

```powershell
cd android\Maze
.\gradlew.bat assembleDebug
adb devices
.\build-and-install.ps1
```
