package com.builtbroken.paw.content.battery;

import com.builtbroken.mc.api.energy.IEnergyBuffer;
import com.builtbroken.mc.api.energy.IEnergyBufferProvider;
import com.builtbroken.mc.api.tile.client.IJsonIconState;
import com.builtbroken.mc.api.tile.listeners.IActivationListener;
import com.builtbroken.mc.codegen.annotations.EnergyWrapped;
import com.builtbroken.mc.codegen.annotations.TileWrapped;
import com.builtbroken.mc.core.Engine;
import com.builtbroken.mc.framework.logic.TileNode;
import com.builtbroken.mc.imp.transform.vector.Pos;
import com.builtbroken.mc.lib.energy.UniversalEnergySystem;
import com.builtbroken.mc.lib.helper.LanguageUtility;
import com.builtbroken.mc.prefab.energy.EnergyBuffer;
import com.builtbroken.paw.PowerAndWiresMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBlock;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.common.util.ForgeDirection;

/**
 * Super simple battery
 *
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 5/30/2017.
 */
@TileWrapped(className = "TileWrappedBattery")
@EnergyWrapped()
public class TileNodeBattery extends TileNode implements IEnergyBufferProvider, IActivationListener, IJsonIconState
{
    //Settings
    public static int maxEnergy = 1000000;

    protected IEnergyBuffer buffer;

    //Internal switches
    private boolean energyHadChanged = true;
    private boolean infinite = false;

    private int textureIndex = 0;

    /** Bitmask use to check if a wire can connect on a side **/
    private byte canConnectSide = 0;

    public TileNodeBattery()
    {
        super("battery", PowerAndWiresMod.DOMAIN);
    }

    @Override
    public void update(long ticks)
    {
        super.update(ticks);
        if (isServer())
        {
            IEnergyBuffer buffer = getEnergyBuffer(ForgeDirection.UNKNOWN);
            if (infinite)
            {
                buffer.addEnergyToStorage(Integer.MAX_VALUE, true);
            }
            //If we have energy attempt to give it away
            if (buffer.getEnergyStored() > 0)
            {
                //TODO move to handler or listener for reuse
                int prev = buffer.getEnergyStored();
                for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS)
                {
                    if (canExportSide(direction))
                    {
                        Pos pos = toPos().add(direction);
                        TileEntity tile = pos.getTileEntity(world());
                        if (UniversalEnergySystem.isHandler(tile, direction.getOpposite()))
                        {
                            //Test remove, or actual remove call if infinite
                            double removed = UniversalEnergySystem.fill(tile, direction.getOpposite(), buffer.getEnergyStored(), !infinite);
                            if (!infinite)
                            {
                                //Second remove call removes the actual energy to ensure buffer drained correctly
                                UniversalEnergySystem.fill(tile, direction.getOpposite(), buffer.removeEnergyFromStorage((int) Math.ceil(removed), true), true);
                                if (buffer.getEnergyStored() <= 0)
                                {
                                    break;
                                }
                            }
                        }
                    }
                }
                //Trigger update on next tick if energy value changed
                if (prev != buffer.getEnergyStored())
                {
                    energyHadChanged = true;
                }
            }

            if (ticks % 3 == 0)
            {
                //Updates render state
                if (energyHadChanged)
                {
                    energyHadChanged = false;
                    sendDescPacket();
                }
            }
        }
    }

    protected boolean canExportSide(ForgeDirection direction)
    {
        return true; //TODO config sides
    }

    @Override
    public void readDescPacket(ByteBuf buf)
    {
        super.readDescPacket(buf);
        getEnergyBuffer(ForgeDirection.UNKNOWN).setEnergyStored(buf.readInt());
        textureIndex = (int) Math.floor(((float) buffer.getEnergyStored() / (float) buffer.getMaxBufferSize()) * 15);  //TODO add a json data file to the texture file to get max number of states, potentially use animation file
    }

    @Override
    public void writeDescPacket(ByteBuf buf)
    {
        super.writeDescPacket(buf);
        buf.writeInt(getEnergyBuffer(ForgeDirection.UNKNOWN).getEnergyStored());
    }

    @Override
    public boolean onPlayerActivated(EntityPlayer player, int side, float hitX, float hitY, float hitZ)
    {
        //Ignore clicks with block to allow easy building
        if (player.getHeldItem() == null || !(player.getHeldItem().getItem() instanceof ItemBlock))
        {
            if (isServer())
            {
                if (Engine.runningAsDev && player.getHeldItem() != null) //TODO maybe do a creative mode or admin check in place of dev mode check
                {
                    if (player.getHeldItem().getItem() == Items.redstone)
                    {
                        getEnergyBuffer(ForgeDirection.UNKNOWN).addEnergyToStorage(maxEnergy, true);
                        player.addChatComponentMessage(new ChatComponentText("Energy has been restored to max"));
                    }
                    else if (player.getHeldItem().getItem() == Items.glowstone_dust)
                    {
                        infinite = !infinite;
                        player.addChatComponentMessage(new ChatComponentText("Power set to infinite: " + infinite));
                    }
                }
                player.addChatComponentMessage(new ChatComponentText(LanguageUtility.getLocal("text.ue.power.amount").replace("%1", "" + getEnergyBuffer(ForgeDirection.UNKNOWN).getEnergyStored()).replace("%2", "" + maxEnergy)));
            }
            return true;
        }
        return false;
    }

    @Override
    public void load(NBTTagCompound nbt)
    {
        super.load(nbt);
        if (nbt.hasKey("energy"))
        {
            getEnergyBuffer(ForgeDirection.UNKNOWN).addEnergyToStorage(nbt.getInteger("energy"), true);
        }
        infinite = nbt.getBoolean("infiniteEnergy");
        canConnectSide = nbt.getByte("connections");
    }

    @Override
    public NBTTagCompound save(NBTTagCompound nbt)
    {
        super.save(nbt);
        nbt.setInteger("energy", getEnergyBuffer(ForgeDirection.UNKNOWN).getEnergyStored());
        nbt.setBoolean("infiniteEnergy", infinite);
        nbt.setByte("connections", canConnectSide);
        return nbt;
    }

    @Override
    public IEnergyBuffer getEnergyBuffer(ForgeDirection side)
    {
        if (buffer == null)
        {
            buffer = new EnergyBuffer(maxEnergy);
        }
        return buffer;
    }

    @Override
    public String getContentStateForSide(int side, int meta)
    {
        if (getEnergyBuffer(ForgeDirection.UNKNOWN).getEnergyStored() <= 0)
        {
            return "";
        }
        return "power." + textureIndex;
    }
}
