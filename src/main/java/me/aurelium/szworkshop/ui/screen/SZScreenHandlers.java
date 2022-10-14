package me.aurelium.szworkshop.ui.screen;

import me.aurelium.szworkshop.SZWorkshop;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class SZScreenHandlers {
	public static ScreenHandlerType<SawmillScreenHandler> SAWMILL_SCREEN_HANDLER;

	public static void initialize() {
		SAWMILL_SCREEN_HANDLER = Registry.register(Registry.SCREEN_HANDLER, new Identifier(SZWorkshop.MODID, "saw_mill"), new ScreenHandlerType<>(SawmillScreenHandler::new));
	}
}
