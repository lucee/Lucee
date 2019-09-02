package lucee.runtime.type.scope;

public interface CSRFTokenSupport {

    public String generateToken(String key, boolean forceNew);

    public boolean verifyToken(String token, String key);

}
