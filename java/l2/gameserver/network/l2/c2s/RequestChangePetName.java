package l2.gameserver.network.l2.c2s;

import l2.gameserver.cache.Msg;
import l2.gameserver.model.Player;
import l2.gameserver.model.instances.PetInstance;

public class RequestChangePetName extends L2GameClientPacket
{
	private String _name;
	
	@Override
	protected void readImpl()
	{
		_name = readS();
	}
	
	@Override
	protected void runImpl()
	{
		PetInstance pet;
		Player activeChar = getClient().getActiveChar();
		PetInstance petInstance = pet = activeChar.getPet() != null && activeChar.getPet().isPet() ? (PetInstance) activeChar.getPet() : null;
		if(pet == null)
		{
			return;
		}
		if(pet.isDefaultName())
		{
			if(_name.length() < 1 || _name.length() > 8)
			{
				sendPacket(Msg.YOUR_PETS_NAME_CAN_BE_UP_TO_8_CHARACTERS);
				return;
			}
			pet.setName(_name);
			pet.broadcastCharInfo();
			pet.updateControlItem();
		}
	}
}