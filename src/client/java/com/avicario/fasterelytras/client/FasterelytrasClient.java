package com.avicario.fasterelytras.client;

import com.avicario.fasterelytras.config.FlightConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

public class FasterelytrasClient implements ClientModInitializer {
    private static final List<Double> speedSamples = new ArrayList<>();
    private static double averageSpeed = 0.0;

    @Override
    public void onInitializeClient() {
        HudRenderCallback.EVENT.register((context, tickCounter) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            FlightConfig config = FlightConfig.getOrCreateInstance();

            if (client.player != null && config.isShowSpeedometer()) {
                double currentSpeed = calculatePlayerSpeed(client, config.isEnableVertical());
                updateAverageSpeed(currentSpeed);

                String formattedSpeed = String.format("%.2f", averageSpeed);
                String speedText = Text.translatable("hud.fasterelytras.speed", formattedSpeed).getString();

                context.drawText(
                        client.textRenderer,
                        speedText,
                        10,
                        200,
                        0xFFFFFFFF,
                        true
                );
            }
        });
    }

    private double calculatePlayerSpeed(MinecraftClient client, boolean enableVertical) {
        if (client.player == null) return 0.0;

        Vec3d velocity = client.player.getVelocity();

        if (enableVertical) {
            if (client.player.isOnGround()){
                return Math.sqrt(velocity.x * velocity.x + velocity.z * velocity.z) * 20.0;
            }
            return velocity.length() * 20.0;
        } else {
            return Math.sqrt(velocity.x * velocity.x + velocity.z * velocity.z) * 20.0;
        }
    }

    private void updateAverageSpeed(double currentSpeed) {
        speedSamples.add(currentSpeed);

        if (speedSamples.size() > 20) {
            // 修复 Java 21 removeFirst() 到 Java 17 的 remove(0)
            speedSamples.remove(0);
        }

        double sum = 0.0;
        for (double speed : speedSamples) {
            sum += speed;
        }
        averageSpeed = speedSamples.isEmpty() ? 0.0 : sum / speedSamples.size();
    }
}