package app;

import java.util.HashSet;
import java.util.Set;

import org.lwjgl.input.Keyboard;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import app.messages.KeyState;
import app.messages.Message;
import app.messages.RegisterKeys;

/**
 * @author Constantin, Benjamin Reemts
 *
 */
public class Input extends UntypedActor {
	
	private SetMultimap<Integer, ActorRef> keyObservers = HashMultimap.create();
	
    private void initialize() {
        getSender().tell(Message.INITIALIZED, self());
    }

    private void run() {
//    	pressedKeys.clear();
    	Set<Integer> pressedKeys = new HashSet<Integer>();
//    	Set<Integer> releasedKeys = new HashSet<Integer>();
    	
		while (Keyboard.next()) {
			int k = Keyboard.getEventKey();
			if (Keyboard.getEventKeyState()) {
				pressedKeys.add(k);
			} 
//			else {
//				releasedKeys.add(k);
//			}
		}
		
		SetMultimap<ActorRef, Integer> push = HashMultimap.create();
		for(Integer i:pressedKeys)if(keyObservers.containsKey(i))for(ActorRef ar:keyObservers.get(i))push.put(ar, i);
		
		for(ActorRef send:push.keySet())send.tell(new KeyState(push.get(send)), self());
		
        getSender().tell(Message.DONE, self());
    }

    @Override
    public void onReceive(Object message) throws Exception {
        if (message == Message.LOOP) {
            run();
        } else if (message == Message.INIT) {
            initialize();
        } else if (message instanceof RegisterKeys){
        	RegisterKeys rk=(RegisterKeys)message;
        	if(rk.isAdd())for(Integer i:rk.getKeys())keyObservers.put(i, getSender());
        	else for(Integer i:rk.getKeys())keyObservers.remove(i, getSender());
        }
    }

}