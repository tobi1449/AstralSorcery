/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2020
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.render.tile;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import hellfirepvp.astralsorcery.client.util.RenderingUtils;
import hellfirepvp.astralsorcery.common.tile.TileTranslucentBlock;
import hellfirepvp.astralsorcery.common.util.ColorUtils;
import hellfirepvp.observerlib.client.util.BufferDecoratorBuilder;
import net.minecraft.block.AirBlock;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.util.math.BlockPos;

import java.awt.*;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: RenderTranslucentBlock
 * Created by HellFirePvP
 * Date: 28.11.2019 / 19:52
 */
public class RenderTranslucentBlock extends CustomTileEntityRenderer<TileTranslucentBlock> {

    @Override
    public void render(TileTranslucentBlock tile, float pTicks, MatrixStack renderStack, IRenderTypeBuffer renderTypeBuffer, int combinedLight, int combinedOverlay) {
        BlockState fakedState = tile.getFakedState();
        if (fakedState.getBlock() instanceof AirBlock) {
            return;
        }
        Color blendColor = ColorUtils.flareColorFromDye(tile.getOverlayColor());
        int[] color = new int[] { blendColor.getRed(), blendColor.getGreen(), blendColor.getBlue(), 128 };

        RenderType type = RenderTypeLookup.getRenderType(fakedState);
        BufferDecoratorBuilder decorator = BufferDecoratorBuilder.withColor(((r, g, b, a) -> color));
        RenderingUtils.renderSimpleBlockModel(fakedState, renderStack, decorator.decorate(renderTypeBuffer.getBuffer(type)), tile.getPos(), tile, true);
    }
}