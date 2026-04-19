package com.example.addon.modules;

import com.example.addon.AddonCategory;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.enums.ChestType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DoubleChestESP extends Module {

    // ── Grupos de configuración ───────────────────────────────────────────
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgRender  = settings.createGroup("Render");
    private final SettingGroup sgNotify  = settings.createGroup("Notify");

    // ── General ───────────────────────────────────────────────────────────

    private final Setting<Integer> range = sgGeneral.add(new IntSetting.Builder()
        .name("range")
        .description("Radio de detección horizontal en bloques.")
        .defaultValue(50).min(5).max(200).sliderRange(5, 100)
        .build()
    );

    private final Setting<Integer> updateDelay = sgGeneral.add(new IntSetting.Builder()
        .name("update-delay")
        .description("Ticks entre cada búsqueda (20 = 1 segundo).")
        .defaultValue(20).min(1).max(100).sliderRange(1, 60)
        .build()
    );

    private final Setting<Boolean> includeTrapped = sgGeneral.add(new BoolSetting.Builder()
        .name("include-trapped")
        .description("Incluir cofres trampa dobles.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Integer> minY = sgGeneral.add(new IntSetting.Builder()
        .name("min-y")
        .description("Y mínima a escanear.")
        .defaultValue(-64).min(-64).max(320).sliderRange(-64, 320)
        .build()
    );

    private final Setting<Integer> maxY = sgGeneral.add(new IntSetting.Builder()
        .name("max-y")
        .description("Y máxima a escanear.")
        .defaultValue(320).min(-64).max(320).sliderRange(-64, 320)
        .build()
    );

    // ── Render ────────────────────────────────────────────────────────────

    private final Setting<ShapeMode> shapeMode = sgRender.add(new EnumSetting.Builder<ShapeMode>()
        .name("shape-mode")
        .description("Both, Sides o Lines.")
        .defaultValue(ShapeMode.Both)
        .build()
    );

    private final Setting<Double> lineWidth = sgRender.add(new DoubleSetting.Builder()
        .name("line-width")
        .description("Grosor del contorno.")
        .defaultValue(1.5).min(0.1).max(5.0).sliderRange(0.1, 5.0)
        .build()
    );

    private final Setting<SettingColor> sideColor = sgRender.add(new ColorSetting.Builder()
        .name("side-color")
        .description("Relleno del box (cofre normal).")
        .defaultValue(new SettingColor(255, 165, 0, 55))
        .build()
    );

    private final Setting<SettingColor> lineColor = sgRender.add(new ColorSetting.Builder()
        .name("line-color")
        .description("Contorno del box (cofre normal).")
        .defaultValue(new SettingColor(255, 165, 0, 230))
        .build()
    );

    private final Setting<SettingColor> trappedSide = sgRender.add(new ColorSetting.Builder()
        .name("trapped-side-color")
        .description("Relleno del box (cofre trampa).")
        .defaultValue(new SettingColor(220, 50, 50, 55))
        .build()
    );

    private final Setting<SettingColor> trappedLine = sgRender.add(new ColorSetting.Builder()
        .name("trapped-line-color")
        .description("Contorno del box (cofre trampa).")
        .defaultValue(new SettingColor(220, 50, 50, 230))
        .build()
    );

    // ── Notify ────────────────────────────────────────────────────────────

    private final Setting<Boolean> chatNotify = sgNotify.add(new BoolSetting.Builder()
        .name("chat-notify")
        .description("Avisar por chat al detectar un nuevo cofre doble.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> soundNotify = sgNotify.add(new BoolSetting.Builder()
        .name("sound-notify")
        .description("Reproducir un ping al detectar un nuevo cofre doble.")
        .defaultValue(false)
        .build()
    );

    // ── Cache interno ─────────────────────────────────────────────────────

    private final List<ChestEntry> found      = new ArrayList<>();
    private final Set<Long>        knownChests = new HashSet<>();
    private int ticker = 0;

    // ── Constructor ───────────────────────────────────────────────────────

    public DoubleChestESP() {
        super(AddonCategory.DCE, "double-chest-esp",
              "Resalta cofres dobles a través de paredes.");
    }

    // ── Ciclo de vida ─────────────────────────────────────────────────────

    @Override
    public void onActivate() {
        found.clear();
        knownChests.clear();
        ticker = 0;
    }

    @Override
    public void onDeactivate() {
        found.clear();
        knownChests.clear();
    }

    /** Acceso público para que ChestTracer reutilice el mismo cache. */
    public List<Box> getDetectedBoxes() {
        List<Box> out = new ArrayList<>(found.size());
        for (ChestEntry e : found) out.add(e.box);
        return out;
    }

    // ── Lógica de búsqueda (cada N ticks) ────────────────────────────────

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.world == null || mc.player == null) return;
        if (++ticker < updateDelay.get()) return;
        ticker = 0;

        found.clear();

        int r = range.get();
        BlockPos center = mc.player.getBlockPos();

        int yMin = Math.max(minY.get(), mc.world.getBottomY());
        int yMax = Math.min(maxY.get(), mc.world.getTopY() - 1);
        if (yMin > yMax) return;

        BlockPos from = new BlockPos(center.getX() - r, yMin, center.getZ() - r);
        BlockPos to   = new BlockPos(center.getX() + r, yMax, center.getZ() + r);

        Set<Long> currentTick = new HashSet<>();

        for (BlockPos pos : BlockPos.iterate(from, to)) {
            var state = mc.world.getBlockState(pos);
            Block block = state.getBlock();

            boolean isNormal  = (block == Blocks.CHEST);
            boolean isTrapped = (block == Blocks.TRAPPED_CHEST);

            if (!isNormal && !(isTrapped && includeTrapped.get())) continue;

            // Solo procesamos el bloque LEFT para no dibujar el mismo cofre dos veces.
            ChestType type;
            try {
                type = state.get(ChestBlock.CHEST_TYPE);
            } catch (Exception ex) {
                continue;
            }
            if (type != ChestType.LEFT) continue;

            Direction facing   = state.get(ChestBlock.FACING);
            BlockPos  otherPos = pos.offset(facing.rotateYClockwise());

            double x1 = Math.min(pos.getX(), otherPos.getX());
            double z1 = Math.min(pos.getZ(), otherPos.getZ());
            double x2 = Math.max(pos.getX(), otherPos.getX()) + 1.0;
            double z2 = Math.max(pos.getZ(), otherPos.getZ()) + 1.0;

            BlockPos immut = pos.toImmutable();
            long key = immut.asLong();
            currentTick.add(key);

            found.add(new ChestEntry(
                new Box(x1, pos.getY(), z1, x2, pos.getY() + 1.0, z2),
                isTrapped,
                immut
            ));

            if (!knownChests.contains(key)) {
                if (chatNotify.get()) {
                    ChatUtils.sendMsg(net.minecraft.text.Text.literal(
                        "§eDoubleChestESP §7| §fNuevo cofre " +
                        (isTrapped ? "§ctrampa" : "§6normal") +
                        " §7en §a" + immut.toShortString()
                    ));
                }
                if (soundNotify.get() && mc.player != null) {
                    MinecraftClient.getInstance().getSoundManager().play(
                        net.minecraft.client.sound.PositionedSoundInstance.master(
                            SoundEvents.BLOCK_NOTE_BLOCK_PLING.value(), 1.5f, 1.0f
                        )
                    );
                }
            }
        }

        knownChests.clear();
        knownChests.addAll(currentTick);
    }

    // ── Renderizado ───────────────────────────────────────────────────────

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (mc.world == null || mc.player == null) return;
        for (ChestEntry e : found) {
            SettingColor sc = e.trapped ? trappedSide.get() : sideColor.get();
            SettingColor lc = e.trapped ? trappedLine.get() : lineColor.get();
            event.renderer.box(e.box, sc, lc, shapeMode.get(), 0);
        }
    }

    /** Grosor de línea expuesto para otros módulos. */
    public double lineWidth() {
        return lineWidth.get();
    }

    // ── Clase interna ─────────────────────────────────────────────────────

    private static class ChestEntry {
        final Box      box;
        final boolean  trapped;
        final BlockPos pos;
        ChestEntry(Box box, boolean trapped, BlockPos pos) {
            this.box = box;
            this.trapped = trapped;
            this.pos = pos;
        }
    }
}
