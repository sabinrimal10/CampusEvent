package com.edap.campusevents.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;

// Wraps CookieCsrfTokenRepository to keep the CSRF cookie stable once issued.
//
// With SessionCreationPolicy.STATELESS there is no HttpSession to tell
// SessionManagementFilter "this request is already authenticated" versus "a
// login just happened" - every authenticated request looks like a fresh
// login to it, so it invokes CsrfAuthenticationStrategy on every single one.
// That strategy unconditionally clears and reissues the CSRF cookie, and
// Spring Security always appends it to the session-authentication chain
// regardless of what SessionAuthenticationStrategy is configured, so it
// can't be turned off through the HttpSecurity DSL.
//
// The result without this wrapper: the CSRF cookie changes on every
// authenticated page view, so a page rendered a moment earlier (with the
// previous token baked into its hidden form field) fails CSRF validation the
// instant another request - even a background one - rotates the cookie
// first. That's the "stuck on the login page" / random 403 bug.
//
// A CSRF cookie doesn't need to rotate per-login to do its job (it isn't a
// secret from the browser, only from other origins), so this repository
// just refuses to let anything overwrite an already-valid cookie.
public class StableCsrfTokenRepository implements CsrfTokenRepository {

    private final CookieCsrfTokenRepository delegate = CookieCsrfTokenRepository.withHttpOnlyFalse();

    @Override
    public CsrfToken generateToken(HttpServletRequest request) {
        return delegate.generateToken(request);
    }

    @Override
    public void saveToken(CsrfToken token, HttpServletRequest request, HttpServletResponse response) {
        if (delegate.loadToken(request) != null) {
            return;
        }
        delegate.saveToken(token, request, response);
    }

    @Override
    public CsrfToken loadToken(HttpServletRequest request) {
        return delegate.loadToken(request);
    }
}
