package actions;

import models.BasicAuthUser;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;
import play.libs.*;
import play.mvc.SimpleResult;
import org.apache.commons.codec.binary.Base64;

public class BasicAuthAction extends Action.Simple {
	private static final String AUTHORIZATION = "authorization";
    private static final String WWW_AUTHENTICATE = "WWW-Authenticate";
    private static final String REALM = "Basic realm=\"Your Realm Here\"";

    @Override
    public F.Promise<SimpleResult>call(Http.Context context) throws Throwable {

        String authHeader = context.request().getHeader(AUTHORIZATION);
        if (authHeader == null) {
            context.response().setHeader(WWW_AUTHENTICATE, REALM);
            return F.Promise.pure((SimpleResult) unauthorized("unauthorized"));
        }

        String auth = authHeader.substring(6);
        byte[] decodedAuth = new Base64().decode(auth);
        String[] credString = new String(decodedAuth, "UTF-8").split(":");

        if (credString == null || credString.length != 2) {
            return F.Promise.pure((SimpleResult) unauthorized("unauthorized"));
        }

        String username = credString[0];
        String password = credString[1];
        BasicAuthUser authUser = BasicAuthUser.authenticate(username, password);
		
        return (authUser == null) ? F.Promise.pure((SimpleResult) unauthorized("unauthorized")) : delegate.call(context);
    }
}
