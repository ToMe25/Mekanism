package mekanism.common.item.block.machine;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import mekanism.api.EnumColor;
import mekanism.client.MekanismClient;
import mekanism.common.Upgrade;
import mekanism.common.base.ISustainedInventory;
import mekanism.common.block.machine.BlockQuantumEntangloporter;
import mekanism.common.capabilities.ItemCapabilityWrapper;
import mekanism.common.frequency.Frequency;
import mekanism.common.integration.forgeenergy.ForgeEnergyItemWrapper;
import mekanism.common.integration.tesla.TeslaItemWrapper;
import mekanism.common.item.IItemEnergized;
import mekanism.common.item.block.ItemBlockAdvancedTooltip;
import mekanism.common.security.ISecurityItem;
import mekanism.common.tile.TileEntityQuantumEntangloporter;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.SecurityUtils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemBlockQuantumEntangloporter extends ItemBlockAdvancedTooltip implements IItemEnergized, ISustainedInventory, ISecurityItem {

    public ItemBlockQuantumEntangloporter(BlockQuantumEntangloporter block) {
        super(block);
        setMaxStackSize(1);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addStats(@Nonnull ItemStack itemstack, World world, @Nonnull List<String> list, @Nonnull ITooltipFlag flag) {
        Frequency.Identity freq = Frequency.Identity.load(ItemDataUtils.getCompound(itemstack, "entangleporter_frequency"));
        if (freq != null) {
            list.add(EnumColor.INDIGO + LangUtils.localize("gui.frequency") + ": " + EnumColor.GREY + freq.name);
            list.add(EnumColor.INDIGO + LangUtils.localize("gui.mode") + ": " + EnumColor.GREY + LangUtils.localize("gui." + (!freq.publicFreq ? "private" : "public")));
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addDetails(@Nonnull ItemStack itemstack, World world, @Nonnull List<String> list, @Nonnull ITooltipFlag flag) {
        list.add(SecurityUtils.getOwnerDisplay(Minecraft.getMinecraft().player, MekanismClient.clientUUIDMap.get(getOwnerUUID(itemstack))));
        list.add(EnumColor.GREY + LangUtils.localize("gui.security") + ": " + SecurityUtils.getSecurityDisplay(itemstack, Side.CLIENT));
        if (SecurityUtils.isOverridden(itemstack, Side.CLIENT)) {
            list.add(EnumColor.RED + "(" + LangUtils.localize("gui.overridden") + ")");
        }
        list.add(EnumColor.BRIGHT_GREEN + LangUtils.localize("tooltip.storedEnergy") + ": " + EnumColor.GREY
                 + MekanismUtils.getEnergyDisplay(getEnergy(itemstack), getMaxEnergy(itemstack)));
        list.add(EnumColor.AQUA + LangUtils.localize("tooltip.inventory") + ": " + EnumColor.GREY +
                 LangUtils.transYesNo(getInventory(itemstack) != null && getInventory(itemstack).tagCount() != 0));
        if (ItemDataUtils.hasData(itemstack, "upgrades")) {
            Map<Upgrade, Integer> upgrades = Upgrade.buildMap(ItemDataUtils.getDataMap(itemstack));
            for (Entry<Upgrade, Integer> entry : upgrades.entrySet()) {
                list.add(entry.getKey().getColor() + "- " + entry.getKey().getName() + (entry.getKey().canMultiply() ? ": " + EnumColor.GREY + "x" + entry.getValue() : ""));
            }
        }
    }

    @Override
    public boolean placeBlockAt(@Nonnull ItemStack stack, @Nonnull EntityPlayer player, World world, @Nonnull BlockPos pos, EnumFacing side, float hitX, float hitY,
          float hitZ, @Nonnull IBlockState state) {
        if (super.placeBlockAt(stack, player, world, pos, side, hitX, hitY, hitZ, state)) {
            TileEntityQuantumEntangloporter tile = (TileEntityQuantumEntangloporter) world.getTileEntity(pos);
            if (tile != null) {
                //Security
                tile.getSecurity().setOwnerUUID(getOwnerUUID(stack));
                tile.getSecurity().setMode(getSecurity(stack));
                if (getOwnerUUID(stack) == null) {
                    tile.getSecurity().setOwnerUUID(player.getUniqueID());
                }
                //Upgrades
                if (ItemDataUtils.hasData(stack, "upgrades")) {
                    tile.getComponent().read(ItemDataUtils.getDataMap(stack));
                }
                //Side config
                if (ItemDataUtils.hasData(stack, "sideDataStored")) {
                    tile.getConfig().read(ItemDataUtils.getDataMap(stack));
                    tile.getEjector().read(ItemDataUtils.getDataMap(stack));
                }
                //Sustained inventory
                tile.setInventory(getInventory(stack));
                //Electricity
                tile.electricityStored = getEnergy(stack);
                if (!world.isRemote && ItemDataUtils.hasData(stack, "entangleporter_frequency")) {
                    Frequency.Identity freq = Frequency.Identity.load(ItemDataUtils.getCompound(stack, "entangleporter_frequency"));
                    if (freq != null) {
                        tile.setFrequency(freq.name, freq.publicFreq);
                    }
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public void setInventory(NBTTagList nbtTags, Object... data) {
        if (data[0] instanceof ItemStack) {
            ItemDataUtils.setList((ItemStack) data[0], "Items", nbtTags);
        }
    }

    @Override
    public NBTTagList getInventory(Object... data) {
        if (data[0] instanceof ItemStack) {
            return ItemDataUtils.getList((ItemStack) data[0], "Items");
        }
        return null;
    }

    @Override
    public double getEnergy(ItemStack itemStack) {
        return ItemDataUtils.getDouble(itemStack, "energyStored");
    }

    @Override
    public void setEnergy(ItemStack itemStack, double amount) {
        ItemDataUtils.setDouble(itemStack, "energyStored", Math.max(Math.min(amount, getMaxEnergy(itemStack)), 0));
    }

    @Override
    public double getMaxEnergy(ItemStack itemStack) {
        Item item = itemStack.getItem();
        if (item instanceof ItemBlockQuantumEntangloporter) {
            return MekanismUtils.getMaxEnergy(itemStack, ((BlockQuantumEntangloporter) (((ItemBlockQuantumEntangloporter) item).block)).getStorage());
        }
        return 0;
    }

    @Override
    public double getMaxTransfer(ItemStack itemStack) {
        return getMaxEnergy(itemStack) * 0.005;
    }

    @Override
    public boolean canReceive(ItemStack itemStack) {
        return true;
    }

    @Override
    public boolean canSend(ItemStack itemStack) {
        return false;
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, NBTTagCompound nbt) {
        return new ItemCapabilityWrapper(stack, new TeslaItemWrapper(), new ForgeEnergyItemWrapper());
    }
}