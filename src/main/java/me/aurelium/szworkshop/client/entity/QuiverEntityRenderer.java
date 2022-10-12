package me.aurelium.szworkshop.client.entity;

import com.mojang.blaze3d.vertex.VertexConsumer;
import me.aurelium.szworkshop.client.model.QuiverEntityModel;
import me.aurelium.szworkshop.entity.QuiverEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

public class QuiverEntityRenderer extends GeoEntityRenderer<QuiverEntity> {
	public QuiverEntityRenderer(EntityRendererFactory.Context ctx) {
		super(ctx, new QuiverEntityModel());
		this.shadowRadius = 0.6f;
	}

	@Override
	public RenderLayer getRenderType(QuiverEntity animatable, float partialTicks, MatrixStack stack, @Nullable VertexConsumerProvider renderTypeBuffer, @Nullable VertexConsumer vertexBuilder, int packedLightIn, Identifier textureLocation) {
		return RenderLayer.getEntityTranslucent(getGeoModelProvider().getTextureResource(animatable));
	}
}
