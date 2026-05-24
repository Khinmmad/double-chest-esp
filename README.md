# DoubleChestESP Addon

Addon para [Meteor Client](https://meteorclient.com/) en Minecraft **26.1.2** (Fabric).
Empezó como un ESP de cofres y hoy es un set de **ESP + utilidades pensadas para survival**:
detección de contenedores, minerales, ítems, trial chambers y varias ayudas de calidad de vida.

> **Todo es 100% del lado del cliente.** Los mensajes de chat se imprimen solo en *tu* chat
> (no se envían al servidor) y los sonidos se reproducen solo para ti. Ningún otro jugador
> ve ni oye nada, y no se manda ningún paquete: es seguro y discreto.

Todos los módulos viven en la categoría propia **`DCE`** dentro de Meteor.

## Módulos

### Contenedores
| Módulo            | Descripción                                                                       |
| ----------------- | --------------------------------------------------------------------------------- |
| `DoubleChestESP`  | Resalta cofres dobles (normales y trampa) con nametags y notificaciones opcionales. |
| `BarrelESP`       | Resalta barriles cercanos.                                                        |
| `ShulkerESP`      | Resalta shulker boxes cercanos.                                                   |
| `ChestTracer`     | Líneas desde la **mira** a cada cofre doble detectado por `DoubleChestESP`.        |
| `StashFinder`     | Avisa cuando un chunk tiene muchos contenedores (detector de stashes). Procesa los chunks en cola, unos pocos por tick, para no causar tirones al teletransportarse. |

### Trial chambers
| Módulo            | Descripción                                                                       |
| ----------------- | --------------------------------------------------------------------------------- |
| `TrialChamberESP` | Resalta **trial spawners** (cian) y **vaults / cofres de llaves** (dorado), normales y ominosos. |

### Ítems
| Módulo            | Descripción                                                                       |
| ----------------- | --------------------------------------------------------------------------------- |
| `ItemTracer`      | Tracer (desde la mira) + ESP para ítems dropeados, con **whitelist**, nametags (nombre/cantidad/distancia) y posición interpolada. |

### Detección (survival)
| Módulo             | Descripción                                                                      |
| ------------------ | -------------------------------------------------------------------------------- |
| `OreESP`           | Resalta minerales valiosos a través de paredes (diamante, netherite, esmeralda… configurable). Estilo xray para minar dirigido. |
| `SpawnerESP`       | Resalta spawners de mazmorra (para montar farms de XP).                          |
| `ValuableBlockESP` | Resalta bloques clave: camas, end portal frames, respawn anchors, budding amethyst. |

### Utilidad / QoL
| Módulo              | Descripción                                                                     |
| ------------------- | ------------------------------------------------------------------------------- |
| `DeathCoords`       | Al morir, anuncia tus coordenadas y las copia al portapapeles para volver por tus cosas. |
| `ToolBreakAlert`    | Avisa cuando una herramienta, elytra o armadura equipada está a punto de romperse. |
| `MobProximityAlert` | Aviso (sonido/chat) cuando un mob hostil se acerca demasiado; sonido distinto para creepers. |
| `CoordsMark`        | Marca tu posición actual con una tecla configurable y la manda al chat/portapapeles. |

## Nametags

Todos los ESP de bloques/entidades (`DoubleChestESP`, `BarrelESP`, `ShulkerESP`,
`SpawnerESP`, `TrialChamberESP`, `OreESP`, `ValuableBlockESP`, `ItemTracer`) muestran un
**nametag con el nombre y la distancia** sobre cada objeto. Se activa/desactiva por módulo
con la opción **`nametags`** en sus ajustes (botón derecho / flecha del módulo en el ClickGUI).
En los ESP de bloques masivos (p. ej. `OreESP`) los nametags se limitan a una distancia
cercana configurable (`nametag-range`) para no saturar la pantalla.

## Rendimiento

Pensado para no provocar tirones:
- Los ESP de contenedores/spawners/vaults iteran **block entities de los chunks cargados**, no bloques uno a uno.
- `OreESP` y `ValuableBlockESP` escanean bloques con una **cola de chunks** (pocos por tick), saltando secciones de solo-aire y secciones cuya paleta no contiene el bloque buscado.
- `StashFinder` también encola los chunks y procesa unos pocos por tick (sin congelarse al teletransportarse).
- Los tracers parten de la **cámara** (la mira) y las posiciones de entidades se **interpolan** al renderizar.

## Compilar

Requisitos: **JDK 25** y conexión a Internet (descarga dependencias de Fabric y Meteor).

```bash
./gradlew build
```

El JAR final se genera en `build/libs/double-chest-esp-2.2.0.jar`.

> Nota técnica: MC 26.1+ usa el nuevo plugin `net.fabricmc.fabric-loom` (sin remapeo) y los
> nombres oficiales de Mojang; por eso el `build.gradle` no lleva línea `mappings`.

## Instalación

1. Instala [Fabric Loader 0.19.2+](https://fabricmc.net/use/) para Minecraft 26.1.2.
2. Instala [Meteor Client para 26.1.2](https://meteorclient.com/) en tu carpeta `mods`.
3. Copia `double-chest-esp-2.2.0.jar` en la misma carpeta `mods`.
4. Abre Minecraft y busca los módulos en la categoría **DCE** dentro de Meteor Client.

## Estructura

```
src/main/java/com/example/addon/
├── AddonMain.java                # Entry point, registra categoría + módulos
├── AddonCategory.java            # Categoría DCE
├── NametagHelper.java            # Utilidad compartida de nametags 2D
└── modules/
    ├── DoubleChestESP.java       # ESP de cofres dobles
    ├── BarrelESP.java            # ESP de barriles
    ├── ShulkerESP.java           # ESP de shulker boxes
    ├── ChestTracer.java          # Tracers a cofres dobles
    ├── StashFinder.java          # Detector de stashes (cola por chunk)
    ├── TrialChamberESP.java      # ESP de trial spawners y vaults
    ├── ItemTracer.java           # Tracer + ESP de ítems dropeados
    ├── BlockScanESP.java         # Base de ESP por escaneo de bloques (cola + paleta)
    ├── OreESP.java               # ESP de minerales (xray-style)
    ├── ValuableBlockESP.java     # ESP de camas / end frames / anchors…
    ├── SpawnerESP.java           # ESP de spawners de mazmorra
    ├── DeathCoords.java          # Coordenadas de muerte
    ├── ToolBreakAlert.java       # Aviso de durabilidad baja
    ├── MobProximityAlert.java    # Aviso de hostiles cercanos
    └── CoordsMark.java           # Marcar posición con tecla
```

## Changelog

### v2.2.0
- **Nametags en todos los ESP**: nombre + distancia sobre cada objeto, con toggle por módulo (`nametags`).
- En los ESP de bloques masivos, límite de distancia para nametags (`nametag-range`).

### v2.1.0
- Nuevos ESP de survival: **`OreESP`**, **`SpawnerESP`**, **`ValuableBlockESP`**.
- Nuevos módulos de utilidad: **`DeathCoords`**, **`ToolBreakAlert`**, **`MobProximityAlert`**, **`CoordsMark`**.
- `ItemTracer` mejorado: nametags (nombre/cantidad/distancia), escaneo más responsivo y mayor alcance.

### v2.0.x
- `2.0.5`/`2.0.4`: módulo `ItemTracer` (tracer + ESP de ítems, con whitelist).
- `2.0.3`: `TrialChamberESP` + tracer desde la mira.
- `2.0.2`: arreglo de tirones (StashFinder en cola) y migración completa a MC **26.1.2** (loom sin remapeo, Mojang mappings, JDK 25).
- `2.0.1`/`2.0.0`: optimización del escaneo a block entities; base con `DoubleChestESP`, `BarrelESP`, `ShulkerESP`, `ChestTracer`, `StashFinder`.

## Licencia

MIT
