# DoubleChestESP Addon

Addon para [Meteor Client](https://meteorclient.com/) en Minecraft **26.1.2** (Fabric) que resalta contenedores y objetos a través de paredes.

## Módulos incluidos

| Módulo            | Categoría | Descripción                                                              |
| ----------------- | --------- | ------------------------------------------------------------------------ |
| `DoubleChestESP`  | DCE       | Resalta cofres dobles (normales y trampa), con tracers y notificaciones. |
| `BarrelESP`       | DCE       | Resalta barriles cercanos.                                               |
| `ShulkerESP`      | DCE       | Resalta shulker boxes cercanos.                                          |
| `StashFinder`     | DCE       | Notifica cuando un chunk contiene muchos contenedores juntos.            |
| `ChestTracer`     | DCE       | Líneas desde el jugador a cada cofre doble detectado.                    |
| `TrialChamberESP` | DCE       | Resalta trial chambers cercanos.                                         |
| `ItemTracer`      | DCE       | Tracer + ESP para ítems dropeados en el suelo (visible a través de paredes). |

Todos los módulos comparten la categoría propia `DCE` para mantenerlos juntos en la lista de Meteor.

### ItemTracer — Detalles

- **Tracer**: líneas desde la vista del jugador a cada ítem dropeado, con color configurable.
- **ESP**: caja semitransparente alrededor de cada ítem, visible a través de paredes, con colores de relleno y contorno configurables por separado.
- **Whitelist**: filtra para mostrar solo los ítems que te interesan.
- **Rendimiento optimizado**: escaneo throttled cada N ticks, cache de IDs, distancia² (sin `sqrt`), interpolación de posiciones solo al renderizar.

## Compilar

Requisitos: **JDK 25** y conexión a Internet (descarga dependencias de Fabric y Meteor).

```bash
./gradlew build
```

El JAR final se genera en `build/libs/double-chest-esp-2.0.5.jar`.

## Instalación

1. Instala [Fabric Loader 0.19.2+](https://fabricmc.net/use/) para Minecraft 26.1.2.
2. Instala [Meteor Client para 26.1.2](https://meteorclient.com/) en tu carpeta `mods`.
3. Copia `double-chest-esp-2.0.5.jar` en la misma carpeta `mods`.
4. Abre Minecraft y busca los módulos en la categoría **DCE** dentro de Meteor Client.

## Estructura

```
src/main/java/com/example/addon/
├── AddonMain.java                # Entry point, registra categoría + módulos
├── AddonCategory.java            # Categoría DCE
└── modules/
    ├── DoubleChestESP.java       # ESP de cofres dobles
    ├── BarrelESP.java            # ESP de barriles
    ├── ShulkerESP.java           # ESP de shulker boxes
    ├── ChestTracer.java          # Tracers a cofres dobles
    ├── StashFinder.java          # Detección de stashes
    ├── TrialChamberESP.java      # ESP de trial chambers
    └── ItemTracer.java           # Tracer + ESP de ítems dropeados
```

## Changelog

### v2.0.5
- **Nuevo módulo `ItemTracer`**: detecta ítems dropeados y dibuja tracers + ESP (caja visible a través de paredes) con colores configurables.
- Tracer y ESP se pueden activar/desactivar independientemente.
- Soporte de whitelist para filtrar ítems específicos.
- Optimizado para rendimiento: escaneo throttled, cache de IDs, distancia², interpolación al render.

### v2.0.4
- Añadido módulo `ItemTracer` (solo tracer, sin ESP).

### v2.0.3
- Añadido `TrialChamberESP`.

### v2.0.2
- Añadido `StashFinder`.

### v2.0.1
- Añadido `ChestTracer`.

### v2.0.0
- Release inicial con `DoubleChestESP`, `BarrelESP`, `ShulkerESP`.

## Licencia

MIT
