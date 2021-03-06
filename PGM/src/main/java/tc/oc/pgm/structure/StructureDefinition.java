package tc.oc.pgm.structure;

import javax.annotation.Nullable;

import org.bukkit.World;
import org.bukkit.block.BlockImage;
import org.bukkit.region.BlockRegion;
import org.bukkit.region.CuboidBlockRegion;
import org.bukkit.geometry.Cuboid;
import org.bukkit.util.ImVector;
import org.bukkit.util.Vector;
import tc.oc.pgm.features.FeatureDefinition;
import tc.oc.pgm.features.FeatureFactory;
import tc.oc.pgm.features.FeatureInfo;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.regions.Region;

import static com.google.common.base.Preconditions.checkNotNull;

@FeatureInfo(name = "structure")
public interface StructureDefinition extends FeatureDefinition, FeatureFactory<Structure> {

    Vector origin();

    Region region();

    Cuboid bounds();

    boolean includeAir();

    boolean clearSource();

    BlockRegion staticBlocks();
}

class StructureDefinitionImpl extends FeatureDefinition.Impl implements StructureDefinition {

    private final @Inspect Region region;
    private final @Inspect boolean includeAir;
    private final @Inspect boolean clearSource;

    // Lazy init because of feature proxies
    private @Nullable ImVector origin;
    private Cuboid bounds;
    private BlockRegion staticBlocks;

    public StructureDefinitionImpl(@Nullable Vector origin, Region region, boolean includeAir, boolean clearSource) {
        this.origin = origin == null ? null : ImVector.copyOf(origin);
        this.region = checkNotNull(region);
        this.includeAir = includeAir;
        this.clearSource = clearSource;
    }

    @Override
    public Vector origin() {
        if(origin == null) {
            origin = region.getBounds().minimum();
        }
        return origin;
    }

    @Override
    public Region region() {
        return region;
    }

    @Override
    public boolean includeAir() {
        return includeAir;
    }

    @Override
    public boolean clearSource() {
        return clearSource;
    }

    @Override
    public Cuboid bounds() {
        if(bounds == null) {
            bounds = region.getBounds();
        }
        return bounds;
    }

    @Override
    public BlockRegion staticBlocks() {
        if(staticBlocks == null) {
            this.staticBlocks = CuboidBlockRegion.fromMinAndSize(bounds().minimumBlockInside(),
                                                                 bounds().blockSize());
        }
        return staticBlocks;
    }

    @Override
    public void load(Match match) {
        match.features().get(this);
    }

    @Override
    public Structure createFeature(Match match) {
        return new StructureImpl(match.getWorld());
    }

    class StructureImpl implements Structure {
        private final BlockImage image;
        private final BlockRegion dynamicBlocks;

        StructureImpl(World world) {
            this.image = world.copyBlocks(staticBlocks(),
                                          includeAir(),
                                          clearSource());
            this.dynamicBlocks = image.region();
        }

        @Override
        public BlockRegion dynamicBlocks() {
            return dynamicBlocks;
        }

        @Override
        public StructureDefinition getDefinition() {
            return StructureDefinitionImpl.this;
        }

        @Override
        public void place(World world, Vector offset) {
            world.pasteBlocks(image, offset);
        }
    }
}
