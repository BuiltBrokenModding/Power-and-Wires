//=======================================================
//DISCLAIMER: THIS IS A GENERATED CLASS FILE
//THUS IS PROVIDED 'AS-IS' WITH NO WARRANTY
//FUNCTIONALITY CAN NOT BE GUARANTIED IN ANY WAY 
//USE AT YOUR OWN RISK 
//-------------------------------------------------------
//Built on: Rober
//=======================================================
package com.builtbroken.paw.content.battery;

import com.builtbroken.paw.content.battery.TileNodeBattery;
import com.builtbroken.mc.api.energy.IEnergyBuffer;
import com.builtbroken.mc.api.energy.IEnergyBufferProvider;
import com.builtbroken.mc.api.tile.node.ITileNode;
import com.builtbroken.mc.framework.logic.wrapper.TileEntityWrapper;
import net.minecraftforge.common.util.ForgeDirection;

public class TileWrappedBattery extends TileEntityWrapper implements IEnergyBufferProvider
{
	public TileWrappedBattery()
	{
		super(new TileNodeBattery());
	}

	//============================
	//==Methods:EnergyWrapped
	//============================


    @Override
    public IEnergyBuffer getEnergyBuffer(ForgeDirection side)
    {
        if (getTileNode() instanceof IEnergyBufferProvider)
        {
            return ((IEnergyBufferProvider) getTileNode()).getEnergyBuffer(side);
        }
        return null;
    }
    
}