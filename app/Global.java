import play.*;
import play.data.*;
import play.mvc.*;
import play.mvc.Results.*;
import play.mvc.Results.Status;
import jobs.*;
import play.libs.*;
import views.*;
import play.mvc.Http.RequestHeader;

import play.*;
import play.mvc.*;
import play.mvc.Http.*;
import play.libs.F.*;

import static play.mvc.Results.*;

public class Global extends GlobalSettings{
	@Override
	public void onStart(Application app){
		MyTaskActorBase.getInstance().start();
	}
	@Override
	public void onStop(Application app){
		MyTaskActorBase.getInstance().shutdown();
	}
	@Override
	public Promise<SimpleResult> onError(RequestHeader request, Throwable t) {
		return Promise.<SimpleResult> pure(internalServerError(
            views.html.error.render()
        ));
	}

	@Override
	public Promise<SimpleResult> onHandlerNotFound(RequestHeader request) {
        return Promise.<SimpleResult>pure(notFound(
            views.html.notFoundPage.render()
        ));
    }
}
