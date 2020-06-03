package co.conexia.negociacion.wap.rest.negociacion;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;

import java.io.UnsupportedEncodingException;
import java.util.Calendar;

public class AuthenticationService {

    public String autenticar(String password) throws UnsupportedEncodingException {
        if (password.equals(RestEnum.PASSWORD_AUTHORIZATION.getDescripcion())) {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.HOUR, 2);
            JWTCreator.Builder tokenBuilder = JWT.create();
            tokenBuilder.withExpiresAt(calendar.getTime());
            tokenBuilder.withClaim("username", "admin");
            return tokenBuilder.sign(Algorithm.HMAC256("ultra-secret"));
        }
        return "";
    }

    public boolean verificar(String token) {
        try {
            if (token == null) {
                return false;
            }
            Algorithm algorithm = Algorithm.HMAC256("ultra-secret");
            JWTVerifier verifier = JWT.require(algorithm).build();
            verifier.verify(token);
            return true;
        } catch (UnsupportedEncodingException | JWTVerificationException exception) {
            return false;
        }
    }
}
