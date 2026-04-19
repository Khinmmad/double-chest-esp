# DoubleChestESP Addon

Addon para [Meteor Client](https://meteorclient.com/) en Minecraft **1.20.1** (Fabric) que resalta contenedores a través de paredes.

## Módulos incluidos

| Módulo            | Categoría | Descripción                                                              |
| ----------------- | --------- | ------------------------------------------------------------------------ |
| `DoubleChestESP`  | DCE       | Resalta cofres dobles (normales y trampa), con tracers y notificaciones. |
| `BarrelESP`       | DCE       | Resalta barriles cercanos.                                               |
| `ShulkerESP`      | DCE       | Resalta shulker boxes cercanos.                                          |
| `StashFinder`     | DCE       | Notifica cuando un chunk contiene muchos contenedores juntos.            |
| `ChestTracer`     | DCE       | Líneas desde el jugador a cada cofre doble detectado.                    |

Todos los módulos comparten la categoría propia `DCE` para mantenerlos juntos en la lista de Meteor.

## Compilar

Requisitos: **JDK 17** y conexión a Internet (descarga dependencias de Fabric y Meteor).

```bash
./gradlew build
```

El JAR final se genera en `build/libs/double-chest-esp-1.0.0.jar`.

## Instalación

1. Instala [Fabric Loader 0.14.22+](https://fabricmc.net/use/) para Minecraft 1.20.1.
2. Instala [Fabric API](https://modrinth.com/mod/fabric-api) y [Meteor Client 0.5.4+](https://meteorclient.com/) en tu carpeta `mods`.
3. Copia `double-chest-esp-1.0.0.jar` en la misma carpeta `mods`.
4. Abre Minecraft y busca los módulos en la categoría **DCE** dentro de Meteor Client.

## Estructura

```
src/main/java/com/example/addon/
├── AddonMain.java                # Entry point, registra categoría + módulos
└── modules/
    ├── DoubleChestESP.java       # ESP de cofres dobles
    ├── BarrelESP.java            # ESP de barriles
    ├── ShulkerESP.java           # ESP de shulker boxes
    ├── ChestTracer.java          # Tracers a cofres dobles
    └── StashFinder.java          # Detección de stashes
```

## Licencia

MIT
