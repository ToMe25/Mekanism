package mekanism.client.render.item.machine;

import mekanism.client.model.ModelResistiveHeater;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderResistiveHeaterItem {

    private static ModelResistiveHeater resistiveHeater = new ModelResistiveHeater();

    public static void renderStack() {
        GlStateManager.rotate(180F, 0.0F, 0.0F, 1.0F);
        GlStateManager.translate(0.05F, -0.96F, 0.05F);
        MekanismRenderer.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "ResistiveHeater.png"));
        resistiveHeater.render(0.0625F, false, Minecraft.getMinecraft().renderEngine, true);
    }
}