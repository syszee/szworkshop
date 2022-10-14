package me.aurelium.szworkshop.client;

import me.aurelium.szworkshop.client.entity.QuiverEntityRenderer;
import me.aurelium.szworkshop.client.screen.SawmillScreen;
import me.aurelium.szworkshop.entity.SZEntities;
import me.aurelium.szworkshop.ui.screen.SZScreenHandlers;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.minecraft.client.gui.screen.ingame.HandledScreens;

public class SZWorkshopClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		EntityRendererRegistry.register(SZEntities.QUIVER, QuiverEntityRenderer::new);

		HandledScreens.register(SZScreenHandlers.SAWMILL_SCREEN_HANDLER, SawmillScreen::new);
	}
}
