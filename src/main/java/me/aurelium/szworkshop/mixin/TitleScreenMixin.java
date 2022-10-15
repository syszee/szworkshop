package me.aurelium.szworkshop.mixin;

import net.minecraft.client.gui.screen.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(TitleScreen.class)
public class TitleScreenMixin {
	@ModifyConstant(method = "render(Lnet/minecraft/client/util/math/MatrixStack;IIF)V", constant = @Constant(intValue = 98))
	public int changeEditionWidth(int value) {
		return 107;
	}

	@ModifyConstant(method = "render(Lnet/minecraft/client/util/math/MatrixStack;IIF)V", constant = @Constant(intValue = 88))
	public int changeEditionStart(int value) {
		return 84;
	}
}
