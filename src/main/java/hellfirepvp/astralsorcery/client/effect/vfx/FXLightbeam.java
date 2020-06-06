/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2020
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.effect.vfx;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import hellfirepvp.astralsorcery.client.effect.EntityVisualFX;
import hellfirepvp.astralsorcery.client.effect.context.base.BatchRenderContext;
import hellfirepvp.astralsorcery.client.effect.function.VFXAlphaFunction;
import hellfirepvp.astralsorcery.client.resource.SpriteSheetResource;
import hellfirepvp.astralsorcery.client.util.draw.BufferContext;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.util.Tuple;

import java.awt.*;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: FXLightbeam
 * Created by HellFirePvP
 * Date: 17.07.2019 / 23:43
 */
public class FXLightbeam extends EntityVisualFX {

    private Vector3 from, to, aim, aimPerp;
    private double fromSize, toSize;

    public FXLightbeam(Vector3 pos) {
        super(pos);
        this.from = pos;
        this.alpha(VFXAlphaFunction.PYRAMID);
    }

    public FXLightbeam setup(Vector3 to, double fromSize, double toSize) {
        this.to = to;
        this.aim = to.clone().subtract(this.getPosition());
        this.aimPerp = aim.clone().perpendicular().normalize();
        this.fromSize = fromSize;
        this.toSize = toSize;
        return this;
    }

    @Override
    public <T extends EntityVisualFX> void render(BatchRenderContext<T> ctx, MatrixStack renderStack, IVertexBuilder vb, float pTicks) {
        Color c = this.getColor(pTicks);
        int r = c.getRed();
        int g = c.getGreen();
        int b = c.getBlue();
        int a = this.getAlpha(pTicks);
        r *= a;
        g *= a;
        b *= a;
        float scale = this.getScale(pTicks);

        renderCurrentTextureAroundAxis(vb, ctx, Math.toRadians(0F), scale, r, g, b, a);
        renderCurrentTextureAroundAxis(vb, ctx, Math.toRadians(120F), scale, r, g, b, a);
        renderCurrentTextureAroundAxis(vb, ctx, Math.toRadians(240F), scale, r, g, b, a);
    }

    private <T extends EntityVisualFX> void renderCurrentTextureAroundAxis(IVertexBuilder vb, BatchRenderContext<T> ctx, double angle, float scale, int r, int g, int b, int a) {
        Vector3 perp = aimPerp.clone().rotate(angle, aim).normalize();
        Vector3 perpTo = perp.clone().multiply(toSize * scale);
        Vector3 perpFrom = perp.multiply(fromSize * scale);

        SpriteSheetResource ssr = ctx.getSprite();
        Tuple<Float, Float> uvOffset = ssr.getUVOffset(age);
        float u = uvOffset.getA();
        float v = uvOffset.getB();
        float uWidth = ssr.getULength();
        float vHeight = ssr.getVLength();

        Vector3 vec = to.clone().add(perpTo.clone().multiply(-1));
        vec.drawPos(vb).color(r, g, b, a).tex(u, v + vHeight).endVertex();
        vec = to.clone().add(perpTo);
        vec.drawPos(vb).color(r, g, b, a).tex(u + uWidth, v + vHeight).endVertex();
        vec = from.clone().add(perpFrom);
        vec.drawPos(vb).color(r, g, b, a).tex(u + uWidth, v).endVertex();
        vec = from.clone().add(perpFrom.clone().multiply(-1));
        vec.drawPos(vb).color(r, g, b, a).tex(u, v).endVertex();
    }

}