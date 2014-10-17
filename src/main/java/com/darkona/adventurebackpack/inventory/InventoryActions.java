package com.darkona.adventurebackpack.inventory;

import com.darkona.adventurebackpack.common.IAdvBackpack;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;

/**
 * Created on 16/10/2014
 *
 * @author Darkona
 */
public class InventoryActions
{

    /**
     * What a complicated mess. I hated every minute of coding this.
     * This code takes a fluid container item. If its filled, it empties it out into a tank.
     * If its empty, it drains the tank into the item. Then it puts the resulting filled or empty item
     * into a different slot, consuming the first one. If there is no empty container, such as the Forestry Cells,
     * it simply fills the tank.
     *
     * @param backpack The backpack type thing that will have its tank updated.
     * @param tank     The tank that's going to be updated.
     * @param slotIn   The slot in which the fluid container item must be to update the tank.
     * @return True if the tank was filled and the resulting filled or empty container item was placed in the other slot.
     */
    public static boolean transferContainerTank(IAdvBackpack backpack, FluidTank tank, int slotIn)
    {
        ItemStack stackIn = backpack.getStackInSlot(slotIn);
        //Set slot out for whatever number the output slot should be.
        int slotOut = slotIn + 1;
        if (tank == null || stackIn == null) return false;

        //CONTAINER ===========> TANK
        if (FluidContainerRegistry.isFilledContainer(stackIn))
        {
            int fill = tank.fill(FluidContainerRegistry.getFluidForFilledItem(stackIn), false);

            if (fill > 0)
            {
                ItemStack stackOut = FluidContainerRegistry.drainFluidContainer(stackIn);

                if (backpack.getStackInSlot(slotOut) == null || stackOut == null)
                {
                    backpack.setInventorySlotContentsNoSave(slotOut, stackOut);
                    tank.fill(FluidContainerRegistry.getFluidForFilledItem(stackIn), true);
                    backpack.decrStackSizeNoSave(slotIn, 1);
                    return true;
                } else if (backpack.getStackInSlot(slotOut).getItem() == stackOut.getItem())
                {
                    int maxStack = backpack.getStackInSlot(slotOut).getMaxStackSize();
                    if (maxStack > 1 && (backpack.getStackInSlot(slotOut).stackSize + 1) <= maxStack)
                    {
                        backpack.getStackInSlot(slotOut).stackSize++;
                        tank.fill(FluidContainerRegistry.getFluidForFilledItem(stackIn), true);
                        backpack.decrStackSizeNoSave(slotIn, 1);
                        return true;
                    }
                }
            }
        }

        //TANK =====> CONTAINER
        if (tank.getFluid() != null && FluidContainerRegistry.isEmptyContainer(stackIn))
        {
            int amount = FluidContainerRegistry.getContainerCapacity(tank.getFluid(), stackIn);
            FluidStack drain = tank.drain(amount, false);
            ItemStack stackOut = FluidContainerRegistry.fillFluidContainer(drain, stackIn);

            if (drain.amount > 0)
            {
                if (backpack.getStackInSlot(slotOut) == null)
                {
                    backpack.decrStackSizeNoSave(slotIn, 1);
                    tank.drain(amount, true);
                    backpack.setInventorySlotContentsNoSave(slotOut, stackOut);
                    return true;
                } else if (stackOut.getItem() == backpack.getStackInSlot(slotOut).getItem())
                {
                    int maxStack = backpack.getStackInSlot(slotOut).getMaxStackSize();
                    if (maxStack > 1 && (backpack.getStackInSlot(slotOut).stackSize + 1) <= maxStack)
                    {
                        backpack.decrStackSizeNoSave(slotIn, 1);
                        tank.drain(amount, true);
                        backpack.getStackInSlot(slotOut).stackSize++;
                        return true;
                    }
                }

            }
        }
        return false;
    }

    public static void consumeItemInBackpack(IAdvBackpack backpack, Item item)
    {
        ItemStack[] inventory = backpack.getInventory();
        for (int i = 0; i < inventory.length; i++)
        {
            if (inventory[i] != null && inventory[i].getItem().equals(item))
            {
                inventory[i] = backpack.decrStackSize(i, 1);
                return;
            }
        }
    }

    public static boolean hasItem(IAdvBackpack backpack, Item item)
    {
        ItemStack[] inventory = backpack.getInventory();
        for (int i = 0; i < inventory.length; i++)
        {
            if (inventory[i] != null &&
                    inventory[i].getItem().equals(item))
            {
                return true;
            }
        }
        return false;
    }

   /* FluidStack drain = tank.drain(FluidContainerRegistry.getContainerCapacity(tank.getFluid(),stackIn),false);
    if(drain.amount > 0){
        for (FluidContainerRegistry.FluidContainerData data : FluidContainerRegistry.getRegisteredFluidContainerData()){
            if(data.fluid.getFluid() == drain.getFluid()){
                if(data.emptyContainer.getItem() == stackIn.getItem()){
                    tank.drain(drain.amount,true);
                    stackOut = data.filledContainer.copy();
                    return stackOut;
                }
            }
        }
    }
    */
}