package com.ecms.web.api.tokenservice.util;

public class MessageVarList {

    //-------------------------- start common messages----------------------------------------------------------------//
    public static final String COMMON_NOT_FOUND = "The requested resource does not exist.";
    public static final String COMMON_ACCESS_DENIED = "Access denied.";
    public static final String COMMON_INVALID_REQUEST_BODY = "Required request body is missing.";
    public static final String COMMON_INVALID_DATA = "Invalid Data.";
    public static final String COMMON_INVALID_REQUEST = "Invalid Request.";
    public static final String COMMON_INTERNAL_SERVER_ERROR = "Cannot process your request. Please contact administrator.";
    //-------------------------- end common messages------------------------------------------------------------------//

    //-------------------------- start user login messages------------------------------------------------------------//
    public static final String LOGIN_USERNAME_EMPTY = "Please enter your username and password.";
    public static final String LOGIN_PASSWORD_EMPTY = "Please enter your username and password.";
    public static final String LOGIN_INVALID = "Your login attempt was not successful. Please try again.";
    public static final String LOGIN_DEACTIVE = "Your account has been deactivated. Please contact administrator.";
    public static final String LOGIN_IDLEDEACTIVE = "Your account has been deactivated due to account been idle. Please contact administrator.";
    public static final String PASSWORDRESET_NEWUSER = "Welcome. !!! please change your password.";
    public static final String PASSWORDRESET_RESETUSER = "Please reset your new password.";
    public static final String PASSWORDRESET_EXPPWD = "Password expired. Please reset your password.";
    //-------------------------- end user login messages------------------------------------------------------------//

    //-------------------------- start jwt token messages------------------------------------------------------------//
    public static final String JWT_TOKEN_SIGNATURE_EXCEPTION = "Invalid JWT signature.";
    public static final String JWT_TOKEN_MALFORMED_EXCEPTION = "Invalid JWT token.";
    public static final String JWT_TOKEN_EXPIRED_EXCEPTION = "JWT token is expired.";
    public static final String JWT_TOKEN_UNSUPPORTED_EXCEPTION = "JWT token is unsupported.";
    public static final String JWT_TOKEN_ILLEGALARGUMENT_EXCEPTION = "JWT claims string is empty.";
    public static final String JWT_TOKEN_EXCEPTION = "JWT token validation is failed.";
    //-------------------------- end jwt token messages------------------------------------------------------------//

}
