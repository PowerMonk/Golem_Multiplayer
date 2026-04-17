# Contexto del Proyecto: Golem Multiplayer 3D

## Tecnologías

- **Lenguaje:** Java 17 (con argumentos de exportación para Java 3D).
- **Gráficos:** Java 3D (librerías j3dcore, j3dutils, vecmath).
- **Base de Datos:** H2 Database en **Modo Servidor (TCP)**.
- **Networking:** Java Sockets (`java.net.Socket` y `java.net.ServerSocket`).

## Arquitectura del Sistema

1. **Server.java (Híbrido):**
   - Inicia el servidor TCP de H2 (`org.h2.tools.Server`).
   - Mantiene la conexión JDBC a `jdbc:h2:tcp://localhost/~/golem_multiplayer`.
   - Inicia un `ServerSocket` (Puerto 5000) para recibir conexiones de jugadores.
   - Gestiona el estado del mundo en memoria (HashMap de Golems) para máxima velocidad.
   - Guarda el estado en la DB H2 de forma asíncrona o por eventos (no en cada movimiento).

2. **Client.java:**
   - Renderiza el mundo usando Java 3D.
   - Captura eventos de teclado (AWTEvents).
   - Se conecta al `ServerSocket` del Host mediante una IP dinámica.
   - Envía su posición X, Y, Z mediante Strings cortos (ej: "MOVE|12.5|0.0|-5.0").
   - Recibe las posiciones de los demás golems para actualizarlos en el Canvas3D.

## Reglas de Programación para Copilot

- **Eficiencia:** No realices consultas SQL `UPDATE` o `INSERT` en el bucle principal de renderizado o en cada paquete de red recibido.
- **Networking:** Usa hilos (Threads) separados para la lectura de Sockets para no bloquear el hilo de renderizado de Java 3D.
- **Java 3D:** El objeto `BranchGroup` principal debe permitir `ALLOW_CHILD_DETACH` y `ALLOW_CHILD_WRITE` para mover/añadir golems dinámicamente.
- **Estructura de Datos:** Usa un protocolo de mensajes simple basado en Strings o serialización ligera.

## Configuración de Entorno

- Librerías en carpeta: `/lib`
- DLL nativa: `j3dcore-ogl.dll` en `/lib`
- VM Args necesarios: `--add-exports java.desktop/sun.awt=ALL-UNNAMED -Djava.library.path="lib"`

## Restricción Crítica: Código Base Único (Listen Server)

- **Regla Estricta:** NO separar el proyecto en aplicaciones Cliente y Servidor distintas. Todos los integrantes del equipo ejecutarán exactamente el mismo código base (`Main.java`).
- **Flujo de Inicio:** Al arrancar el programa, se debe mostrar un diálogo (ej. `JOptionPane` o consola) preguntando al usuario: "¿Deseas ser Host o Unirte a una partida?".

### Lógica de Roles (Mismo Código):

- **Si el usuario elige "Host":**
  1. Inicia el servidor TCP de H2 localmente.
  2. Crea la conexión JDBC (`jdbc:h2:tcp://localhost/...`) e inicializa las tablas si no existen.
  3. Inicia el `ServerSocket` en un hilo (Thread) separado para escuchar a otros jugadores.
  4. Inicia su propio Cliente 3D en el hilo principal conectándose a sí mismo (`localhost`).
- **Si el usuario elige "Unirse (Client)":**
  1. Pide la IP del Host mediante un diálogo.
  2. IGNORA por completo cualquier código de H2 o JDBC (no levanta base de datos local).
  3. Inicia el Cliente 3D conectándose vía Sockets a la IP proporcionada.

## Manejo de la Base de Datos (H2)

- El archivo de la base de datos (ej. `golem_db.mv.db`) se generará automáticamente en la carpeta raíz del proyecto del usuario que actúe como Host. Los clientes puros no deben interactuar con la librería de H2.
