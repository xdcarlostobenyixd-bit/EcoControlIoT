# EcoControl IoT

Aplicación Android nativa en Kotlin para la evaluación sumativa de TI3042 – Unidad 2. Resuelve el escenario de un invernadero inteligente: un Android funciona como nodo sensor/servidor y otro como controlador/cliente. Ambos se comunican por Wi‑Fi local mediante TCP en el puerto 5050.

## Requisitos

- Android Studio Hedgehog 2023.1.1 o superior.
- Android SDK 35 y JDK 17.
- Dos teléfonos Android conectados a la misma red Wi‑Fi (o dos emuladores configurados en una red accesible).

## Abrir y ejecutar

1. Descomprime o abre la carpeta `EcoControlIoT` desde **Android Studio > Open**.
2. Espera la sincronización de Gradle y deja que Android Studio instale los SDK sugeridos si los solicita.
3. Conecta un teléfono con depuración USB o inicia un emulador.
4. Ejecuta `app` con **Run**.
5. Credenciales de demostración: usuario `admin`, contraseña `IoT2026`.

## Demostración con dos dispositivos

1. Instala y abre la app en ambos teléfonos; ambos deben estar en la misma Wi‑Fi.
2. En el teléfono 1 pulsa **ACTUAR COMO SERVIDOR / SENSOR**. Anota la IP que aparece bajo el título, por ejemplo `192.168.1.20`.
3. En el teléfono 2 escribe esa IP y pulsa **CONECTAR COMO CLIENTE / CONTROLADOR**.
4. El servidor genera lecturas simuladas de temperatura y humedad cada cinco segundos, las guarda en SQLite y las envía al cliente.
5. Cambia el interruptor del relé desde cualquiera de los teléfonos. El estado se replica y se registra.
6. Pulsa **ACTUALIZAR HISTORIAL** para mostrar las mediciones guardadas localmente.

Si se usa el emulador, el modo servidor es más fácil en un teléfono físico. Si solo se cuenta con un dispositivo, se puede demostrar el modo servidor, el login, la simulación de sensores y el almacenamiento SQLite.

## Correspondencia con la guía

- **Herramientas Android/Kotlin:** proyecto nativo con Gradle Kotlin DSL, minSdk 26 y targetSdk 35.
- **Conectividad inalámbrica:** Wi‑Fi local con `ServerSocket`/`Socket`, sin servidor externo.
- **Autenticación:** login local con credenciales y comparación mediante hash SHA‑256; la sesión se conserva en preferencias privadas.
- **Persistencia/interoperabilidad:** SQLite almacena mediciones y eventos en tablas simples.
- **Seguridad IoT:** permisos mínimos, sin credenciales en texto plano, separación de roles servidor/cliente, control de acceso por login y comunicación dentro de la red local. Para producción debe añadirse TLS/mTLS y un backend con gestión de identidades.
- **Pruebas:** login válido/inválido, servidor/cliente, envío de lecturas, control remoto del relé, persistencia y cierre de sesión.

## Estructura

- `LoginActivity.kt`: autenticación local.
- `MainActivity.kt`: tablero, servidor/cliente y control del actuador.
- `WifiPeer.kt`: transporte TCP entre Android.
- `IoTDatabase.kt`: SQLite para mediciones y eventos.
- `AuthStore.kt`: sesión y hash de contraseña.
- `docs/Informe_TI3042.md`: memoria técnica y guion de exposición.
