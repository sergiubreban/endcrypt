package com.brb.breban.endcript.dataAccess.interfaces;

import com.brb.breban.endcript.dataAccess.dao.DAO;
import com.brb.breban.endcript.dataAccess.models.Message;

/**
 * Created by breban on 29.04.2017.
 */

public interface IConversation {

    public void sendMessage(DAO dao, Message message);
/*    public void receiveMessage();*/
}
