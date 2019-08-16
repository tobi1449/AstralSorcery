/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2019
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.starlight.transmission.base.crystal;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import hellfirepvp.astralsorcery.common.constellation.IWeakConstellation;
import hellfirepvp.astralsorcery.common.constellation.SkyHandler;
import hellfirepvp.astralsorcery.common.constellation.world.DayTimeHelper;
import hellfirepvp.astralsorcery.common.constellation.world.WorldContext;
import hellfirepvp.astralsorcery.common.starlight.IIndependentStarlightSource;
import hellfirepvp.astralsorcery.common.starlight.IStarlightSource;
import hellfirepvp.astralsorcery.common.starlight.transmission.registry.SourceClassRegistry;
import hellfirepvp.astralsorcery.common.tile.TileCollectorCrystal;
import hellfirepvp.astralsorcery.common.tile.base.TileEntityTick;
import hellfirepvp.astralsorcery.common.tile.base.network.TileSourceBase;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.crystal.CrystalCalculations;
import hellfirepvp.astralsorcery.common.util.crystal.CrystalProperties;
import hellfirepvp.astralsorcery.common.util.nbt.NBTHelper;
import hellfirepvp.astralsorcery.common.util.world.SkyCollectionHelper;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.function.Function;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: IndependentCrystalSource
 * Created by HellFirePvP
 * Date: 04.08.2016 / 15:01
 */
public class IndependentCrystalSource implements IIndependentStarlightSource {

    public static final double MIN_DST = 16;

    private IWeakConstellation constellation = null;
    private CrystalProperties crystalProperties = null;
    private boolean doesSeeSky = false;
    private boolean doesAutoLink = false;

    private double collectionDstMultiplier = 1;
    private float posDistribution = -1;
    private boolean enhanced = false;

    @Override
    public float produceStarlightTick(World world, BlockPos pos) {
        if (!doesSeeSky || crystalProperties == null) {
            return 0F;
        }
        IWeakConstellation cst = getStarlightType();
        WorldContext ctx = SkyHandler.getContext(world, Dist.DEDICATED_SERVER);
        if (ctx == null || cst == null) {
            return 0F;
        }

        if(posDistribution == -1) {
            posDistribution = SkyCollectionHelper.getSkyNoiseDistribution(world, pos);
        }

        Function<Float, Float> distrFunction = getDistributionFunc();
        double perc = distrFunction.apply(DayTimeHelper.getCurrentDaytimeDistribution(world));
        perc *= collectionDstMultiplier;
        perc *= 1 + (0.3 * posDistribution);
        return (float) (perc * CrystalCalculations.getCollectionAmt(crystalProperties,
                distrFunction.apply(ctx.getDistributionHandler().getDistribution(cst))));
    }

    private Function<Float, Float> getDistributionFunc() {
        if (enhanced) {
            return (in) -> 0.6F + (1.1F * in);
        } else {
            return (in) -> 0.2F + (0.8F * in);
        }
    }

    @Override
    public boolean providesAutoLink() {
        return this.doesAutoLink;
    }

    @Override
    public <T extends TileEntity> boolean updateFromTileEntity(T tile) {
        if (!(tile instanceof TileCollectorCrystal)) {
            return true;
        }
        TileCollectorCrystal tcc = (TileCollectorCrystal) tile;
        this.doesSeeSky = tcc.doesSeeSky();
        this.doesAutoLink = !tcc.isPlayerMade(); //Structural, not player-placed.
        this.enhanced = tcc.isEnhanced();
        this.constellation = tcc.getAttunedConstellation();
        this.crystalProperties = tcc.getCrystalProperties();
        return false;
    }

    @Override
    public void threadedUpdateProximity(BlockPos thisPos, Map<BlockPos, IIndependentStarlightSource> otherSources) {
        double minDstSq = Double.MAX_VALUE;
        for (BlockPos other : otherSources.keySet()) {
            if (other.equals(thisPos)) {
                continue;
            }
            double dstSq = thisPos.distanceSq(other);
            if (dstSq < minDstSq) {
                minDstSq = dstSq;
            }
        }
        double dst = Math.sqrt(minDstSq);
        if (dst <= MIN_DST) {
            this.collectionDstMultiplier = dst / MIN_DST;
        } else {
            this.collectionDstMultiplier = 1;
        }
    }

    @Nullable
    @Override
    public IWeakConstellation getStarlightType() {
        return this.constellation;
    }

    @Override
    public SourceClassRegistry.SourceProvider getProvider() {
        return new Provider();
    }

    @Override
    public void readFromNBT(CompoundNBT compound) {
        this.constellation = NBTHelper.readOptional(compound, "constellation", (nbt) -> {
            IConstellation cst = IConstellation.readFromNBT(nbt);
            if (cst instanceof IWeakConstellation) {
                return (IWeakConstellation) cst;
            }
            return null;
        });
        this.crystalProperties = NBTHelper.readOptional(compound, "crystalProperties", CrystalProperties::readFromNBT);
        this.doesSeeSky = compound.getBoolean("doesSeeSky");
        this.doesAutoLink = compound.getBoolean("doesAutoLink");
        this.collectionDstMultiplier = compound.getDouble("collectionDstMultiplier");
        this.enhanced = compound.getBoolean("enhanced");
    }

    @Override
    public void writeToNBT(CompoundNBT compound) {
        NBTHelper.writeOptional(compound, "constellation", this.constellation, (nbt, cst) -> cst.writeToNBT(nbt));
        NBTHelper.writeOptional(compound, "crystalProperties", this.crystalProperties, (nbt, prop) -> prop.writeToNBT(nbt));
        compound.putBoolean("doesSeeSky", doesSeeSky);
        compound.putBoolean("doesAutoLink", doesAutoLink);
        compound.putDouble("collectionDstMultiplier", collectionDstMultiplier);
        compound.putBoolean("enhanced", enhanced);
    }

    public static class Provider implements SourceClassRegistry.SourceProvider {

        @Override
        public IIndependentStarlightSource provideEmptySource() {
            return new IndependentCrystalSource();
        }

        @Override
        public String getIdentifier() {
            return AstralSorcery.MODID + ":IndependentCrystalSource";
        }

    }

}
