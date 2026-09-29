package com.treeminigame.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.phys.BlockHitResult;
import org.lwjgl.glfw.GLFW;

public final class TreeChopMiniGameClient implements ClientModInitializer {
    private static boolean active;
    private static boolean previousAttack;
    private static boolean previousSpace;
    private static long startedAt;
    private static float rotation;

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(TreeChopMiniGameClient::tick);
        HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT, "treechopminigame:minigame", TreeChopMiniGameClient::render);
    }

    private static void tick(Minecraft client) {
        if (client.player == null || client.level == null) {
            active = false;
            previousAttack = false;
            previousSpace = false;
            return;
        }

        boolean attack = client.options.keyAttack.isDown();
        boolean space = InputConstants.isKeyDown(client.getWindow().handle(), GLFW.GLFW_KEY_SPACE);

        if (!active && attack && !previousAttack && isLookingAtLogWithAxe(client)) {
            active = true;
            startedAt = Util.getMillis();
            rotation = 0.0f;
        }

        if (active) {
            rotation = (rotation + 4.8f) % 360.0f;
            if (space && !previousSpace) {
                startedAt = Util.getMillis();
            }
        }

        previousAttack = attack;
        previousSpace = space;
    }

    private static boolean isLookingAtLogWithAxe(Minecraft client) {
        if (!(client.hitResult instanceof BlockHitResult hit)) return false;
        if (!(client.player.getMainHandItem().getItem() instanceof AxeItem)) return false;
        BlockPos pos = hit.getBlockPos();
        return client.level.getBlockState(pos).is(BlockTags.LOGS);
    }

    private static void render(GuiGraphicsExtractor graphics, DeltaTracker delta) {
        if (!active) return;
        Minecraft client = Minecraft.getInstance();
        int cx = client.getWindow().getGuiScaledWidth() / 2;
        int cy = client.getWindow().getGuiScaledHeight() / 2 - 8;
        int radius = Math.max(64, Math.min(client.getWindow().getGuiScaledWidth(), client.getWindow().getGuiScaledHeight()) / 10);

        drawBackdrop(graphics, cx, cy, radius);
        drawRing(graphics, cx, cy, radius);
        drawZones(graphics, cx, cy, radius);
        drawAxe(graphics, cx, cy, radius, rotation);
        drawPrompt(graphics, client, cx, cy + radius + 22);
    }

    private static void drawBackdrop(GuiGraphicsExtractor graphics, int cx, int cy, int radius) {
        graphics.fill(cx - radius - 12, cy - radius - 12, cx + radius + 12, cy + radius + 12, 0xB0202020);
        graphics.fill(cx - radius - 8, cy - radius - 8, cx + radius + 8, cy + radius + 8, 0xD08C8589);
        graphics.fill(cx - radius + 3, cy - radius + 3, cx + radius - 3, cy + radius - 3, 0xD05C6267);
    }

    private static void drawRing(GuiGraphicsExtractor graphics, int cx, int cy, int radius) {
        for (int a = 0; a < 360; a += 2) {
            double r = Math.toRadians(a);
            int x = cx + (int) (Math.cos(r) * radius);
            int y = cy + (int) (Math.sin(r) * radius);
            graphics.fill(x - 2, y - 2, x + 3, y + 3, 0xFFB8BEC2);
        }
        int inner = radius - 13;
        for (int a = 0; a < 360; a += 2) {
            double r = Math.toRadians(a);
            int x = cx + (int) (Math.cos(r) * inner);
            int y = cy + (int) (Math.sin(r) * inner);
            graphics.fill(x - 2, y - 2, x + 3, y + 3, 0xFF6B7378);
        }
        graphics.fill(cx - 6, cy - 6, cx + 7, cy + 7, 0xFFD4D8D9);
        graphics.fill(cx - 3, cy - 3, cx + 4, cy + 4, 0xFF777F83);
    }

    private static void drawZones(GuiGraphicsExtractor graphics, int cx, int cy, int radius) {
        drawArc(graphics, cx, cy, radius + 4, 24, 58, 0xFF35E66D);
        drawArc(graphics, cx, cy, radius + 4, 204, 238, 0xFF35E66D);
        drawArc(graphics, cx, cy, radius + 15, 24, 58, 0xAA35E66D);
        drawArc(graphics, cx, cy, radius + 15, 204, 238, 0xAA35E66D);
    }

    private static void drawArc(GuiGraphicsExtractor graphics, int cx, int cy, int radius, int start, int end, int color) {
        for (int a = start; a <= end; a += 2) {
            double r = Math.toRadians(a);
            int x = cx + (int) (Math.cos(r) * radius);
            int y = cy + (int) (Math.sin(r) * radius);
            graphics.fill(x - 3, y - 3, x + 4, y + 4, color);
        }
    }

    private static void drawAxe(GuiGraphicsExtractor graphics, int cx, int cy, int radius, float angle) {
        double r = Math.toRadians(angle - 90.0f);
        int tipX = cx + (int) (Math.cos(r) * (radius - 17));
        int tipY = cy + (int) (Math.sin(r) * (radius - 17));
        drawLine(graphics, cx, cy, tipX, tipY, 5, 0xFF3C3A3B);

        double side = r + Math.PI / 2.0;
        int hx = tipX;
        int hy = tipY;
        int h1x = hx + (int) (Math.cos(side) * 13);
        int h1y = hy + (int) (Math.sin(side) * 13);
        int h2x = hx - (int) (Math.cos(side) * 13);
        int h2y = hy - (int) (Math.sin(side) * 13);
        drawLine(graphics, h1x, h1y, h2x, h2y, 8, 0xFF202426);
        drawLine(graphics, h1x, h1y, h2x, h2y, 4, 0xFF596166);
        drawLine(graphics, cx, cy, tipX, tipY, 2, 0xFFB5B7B8);
    }

    private static void drawLine(GuiGraphicsExtractor graphics, int x1, int y1, int x2, int y2, int width, int color) {
        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);
        int sx = x1 < x2 ? 1 : -1;
        int sy = y1 < y2 ? 1 : -1;
        int err = dx - dy;
        int x = x1;
        int y = y1;
        while (true) {
            graphics.fill(x - width / 2, y - width / 2, x + width / 2 + 1, y + width / 2 + 1, color);
            if (x == x2 && y == y2) break;
            int e2 = err * 2;
            if (e2 > -dy) {
                err -= dy;
                x += sx;
            }
            if (e2 < dx) {
                err += dx;
                y += sy;
            }
        }
    }

    private static void drawPrompt(GuiGraphicsExtractor graphics, Minecraft client, int x, int y) {
        int width = 76;
        int height = 22;
        graphics.fill(x - width / 2, y, x + width / 2, y + height, 0xE8E5E8E8);
        graphics.fill(x - width / 2 + 2, y + 2, x + width / 2 - 2, y + height - 4, 0xFFBFC4C5);
        String text = "SPACE";
        int textWidth = client.font.width(text);
        graphics.text(client.font, text, x - textWidth / 2, y + 6, 0xFF4D565A, false);
    }
}
