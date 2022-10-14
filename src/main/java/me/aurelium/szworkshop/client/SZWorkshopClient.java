package me.aurelium.szworkshop.client;

import me.aurelium.szworkshop.client.entity.QuiverEntityRenderer;
import me.aurelium.szworkshop.entity.SZEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public class SZWorkshopClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		EntityRendererRegistry.register(SZEntities.QUIVER, QuiverEntityRenderer::new);
	}
}
