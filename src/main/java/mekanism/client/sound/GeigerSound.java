package mekanism.client.sound;

import javax.annotation.Nonnull;
import mekanism.common.Mekanism;
import mekanism.common.lib.radiation.RadiationManager.RadiationScale;
import net.minecraft.entity.player.PlayerEntity;

public class GeigerSound extends PlayerSound {

    private RadiationScale scale;

    public GeigerSound(@Nonnull PlayerEntity player, RadiationScale scale) {
        //TODO - V10: FIXME This will cause a crash if RadiationScale is NONE due to returning a null sound event
        // Having the sound not place should be handled in another way
        super(player, scale.getSoundEvent());
        this.scale = scale;
        setFade(1, 1);
    }

    @Override
    public boolean shouldPlaySound(@Nonnull PlayerEntity player) {
        return scale == Mekanism.radiationManager.getClientScale();
    }

    @Override
    public float getVolume() {
        return super.getVolume() * 0.05F;
    }
}