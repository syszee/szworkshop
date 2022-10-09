package me.aurelium.szworkshop.client;

import me.aurelium.szworkshop.SZWorkshop;
import me.aurelium.szworkshop.client.entity.QuiverEntityRenderer;
import me.aurelium.szworkshop.client.model.QuiverEntityModel;
import me.aurelium.szworkshop.entity.SZEntities;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.util.Identifier;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer;

public class SZWorkshopClient implements ClientModInitializer {
	@Override
	public void onInitializeClient(ModContainer mod) {
		EntityRendererRegistry.register(SZEntities.QUIVER, QuiverEntityRenderer::new);
	}
}
