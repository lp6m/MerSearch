package jobs;

import akka.actor.*;

public class MyTaskActor extends UntypedActor{
	@Override
	public void onReceive(Object message){
		if(message.equals("Call")){
			//処理を記述
			System.out.println("call is called");
		}else{
			unhandled(message);
		}
	}
		
}
