package me.aurelium.szworkshop.client.model;

import me.aurelium.szworkshop.SZWorkshop;
import me.aurelium.szworkshop.entity.QuiverEntity;
import net.minecraft.util.Identifier;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class QuiverEntityModel extends AnimatedGeoModel<QuiverEntity> {
	@Override
	public Identifier getModelResource(QuiverEntity object) {
		return new Identifier(SZWorkshop.modid, "geo/quiver.geo.json");
	}

	@Override
	public Identifier getTextureResource(QuiverEntity object) {
		return new Identifier(SZWorkshop.modid, "textures/entity/quiver.png");
	}

	@Override
	public Identifier getAnimationResource(QuiverEntity animatable) {
		return new Identifier(SZWorkshop.modid, "animations/quiver.animation.json");
	}
}
