import play.*;
import play.data.*;
import play.mvc.*;
import jobs.*;
public class Global extends GlobalSettings{
	@Override
	public void onStart(Application app){
		MyTaskActorBase.getInstance().start();
	}
	@Override
	public void onStop(Application app){
		MyTaskActorBase.getInstance().shutdown();
	}
}
