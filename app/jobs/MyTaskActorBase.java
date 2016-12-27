package jobs;

//Actorを定期的に実行させるクラス

import akka.actor.*;
import scala.concurrent.duration.Duration;
import play.Play;
import java.util.concurrent.TimeUnit;
import scala.concurrent.ExecutionContext;
	
public class MyTaskActorBase{
	private static MyTaskActorBase instance = new MyTaskActorBase();
	private ActorSystem system = ActorSystem.create("myActor");
	private ActorRef myTaskActor = system.actorOf(new Props(MyTaskActor.class));

	private Cancellable cancellable;

	private MyTaskActorBase(){
		super();
	}

	public static MyTaskActorBase getInstance(){
		return instance;
	}

	public void start(){
		cancellable = system.scheduler().schedule(Duration.Zero(),
												  Duration.create(1, TimeUnit.MINUTES),
												  myTaskActor,
												  "Call",
												  system.dispatcher(), null);
	
	}

	public void shutdown(){
		cancellable.cancel();
	}
}
