package com.mwl.characters;

import com.mwl.environment.Item;
import com.mwl.environment.Room;

import java.util.Collection;
import java.util.List;

public class Wolverine extends Player{

    public Wolverine(String name, int life, Room currentRoom, List<Item> itemsInventory){
        super(name, life, currentRoom, itemsInventory);
    }

    @Override
    public void attack() {

    }

    @Override //health boost
    public void useSpecialPower() {
        int lifeValue = getLife();
        lifeValue += 50;
        setLife(lifeValue);
    }

}